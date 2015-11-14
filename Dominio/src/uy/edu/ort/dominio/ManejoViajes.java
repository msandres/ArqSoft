/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uy.edu.ort.dominio;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

/**
 *
 * @author Renato
 */
public class ManejoViajes {

    private static ManejoViajes instanciaManejoViajes = null;
    private List<Aeropuerto> aeropuertos;
    private List<Aerolinea> aerolineas;
    private List<Vuelo> vuelos;
    private List<Usuario> usuarios;
    private List<Transacion> compras;
    private List<Transacion> reservas;
    private List<NodoAeropuerto> nodosListos = null; // nodos revisados Dijkstra  

    private ManejoViajes() {
        aeropuertos = new ArrayList<>();
        aerolineas = new ArrayList<>();
        vuelos = new ArrayList<>();
        usuarios = new ArrayList<>();
        compras = new ArrayList<>();
        reservas = new ArrayList<>();
    }

    public static ManejoViajes obtenerInstanciaManejoViajes() {
        if (instanciaManejoViajes == null) {
            instanciaManejoViajes = new ManejoViajes();
        }
        return instanciaManejoViajes;
    }

    public List<Aeropuerto> getAeropuertos() {
        return aeropuertos;
    }

    public List<Aerolinea> getAerolineas() {
        return aerolineas;
    }

    public List<Vuelo> getVuelos() {
        return vuelos;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public List<Transacion> getCompras() {
        return compras;
    }

    public List<Transacion> getReservas() {
        return reservas;
    }

    public List<Vuelo> devolverVuelosSinEscala(Date fecha,
            Aeropuerto aeropuertoOrigen, Aeropuerto aeropuertoDestino) {
        //devuelve una lista de vuelos entre origen y destino
        //posteriores a la fecha "fecha"
        List<Vuelo> adyacentes = new ArrayList<>();
        Calendar fechaICalendario = Calendar.getInstance();
        fechaICalendario.setTime(fecha); // Configuramos la fecha que se recibe
        Calendar fechaFCalendario = Calendar.getInstance();
        actualizarReservas();
        for (Vuelo temp : vuelos) {
            if (temp.getAeropuertoOrigen().getCodigoAeropuerto().equals(
                    aeropuertoOrigen.getCodigoAeropuerto())) {
                if (temp.getAeropuertoDestino().getCodigoAeropuerto().equals(
                        aeropuertoDestino.getCodigoAeropuerto())) {
                    Calendar tempfecha = Calendar.getInstance();
                    tempfecha.setTime(temp.getFecha());
                    if (tempfecha.after(fechaICalendario)) {
                        adyacentes.add(temp);
                    }
                }
            }
        }
        return adyacentes;
    }

    public List<Transacion> devolverTransaciones(Cliente cliente) {
        //devuelve el listado de transaciones que ha efectuado dicho cliente
        return cliente.getTransaciones();
    }

    public List<Vuelo> devolverRutaMenorTiempo(Date fecha,
            Aeropuerto origenAeropuerto, Aeropuerto destinoAeropuerto) {
        // devuelve la ruta de menor tiempo de escalas entre origen y destino
        // misma complejidad que ruta menor costo, por ahora no lo voy a implementar
        return null;
    }

    public List<Transacion> consultaVuelos(Cliente cliente, Aeropuerto origen, Aeropuerto destino,
            boolean ida_vuelta, Date fechaIda, Date fechaVuelta,
            int cantidadPersonas) {
        //consulta vuelos origen destino disponibles y a menor costo.
        actualizarReservas();
        List<Transacion> transacionesConsulta = new ArrayList<>();
        List<Vuelo> ida = devolverRutaMenorCosto(fechaIda, origen, destino, cantidadPersonas);
        for (Vuelo temp : ida) {
            Transacion tran = new Transacion(temp, "libre", new Date(), "-", cliente, cantidadPersonas);
            transacionesConsulta.add(tran);
        }
        if (ida_vuelta) {
            List<Vuelo> vuelta = devolverRutaMenorCosto(fechaVuelta, destino, origen, cantidadPersonas);
            for (Vuelo temp : vuelta) {
                Transacion tran = new Transacion(temp, "libre", fechaIda, "-", cliente, cantidadPersonas);
                transacionesConsulta.add(tran);
            }
        }
        return transacionesConsulta;
    }

    public void reservarPasajes(List<Transacion> transaciones, Cliente cliente) {
        for (Transacion temp : transaciones) {
            reservaTramo(temp, cliente);
        }
    }

    public void comprarPasajes(List<Transacion> transaciones, Cliente cliente) {
        for (Transacion temp : transaciones) {
            compraTramo(temp, cliente);
        }
    }

    /**
     * Metodos auxiliares
     */
    private void reservaTramo(Transacion transacion, Cliente cliente) {
        transacion.setEstado("reserva");
        transacion.setCliente(cliente);
        transacion.setFecha(new Date());
        long disponiblesVuelo = transacion.getVuelo().getDisponibles();
        Vuelo vuelo = transacion.getVuelo();
        vuelo.setDisponibles(disponiblesVuelo - transacion.getCantReserva());
        reservas.add(transacion);
    }

    private void compraTramo(Transacion transacion, Cliente cliente) {
        if (!transacion.getEstado().equals("reserva")) {
            long disponiblesVuelo = transacion.getVuelo().getDisponibles();
            Vuelo vuelo = transacion.getVuelo();
            vuelo.setDisponibles(disponiblesVuelo - transacion.getCantReserva());
            reservas.remove(transacion);
        }
        transacion.setEstado("confirmada");
        transacion.setCliente(cliente);
        transacion.setFecha(new Date());
        compras.add(transacion);
    }

    private void actualizarReservas() {
        //borra toda transacion pendiente que se anterior a 72 hs.
        for (Transacion tran : reservas) {
            if (!esValidaReserva(tran)) {
                reservas.remove(tran);
                long disponiblesVuelo = tran.getVuelo().getDisponibles();
                Vuelo vuelo = tran.getVuelo();
                vuelo.setDisponibles(disponiblesVuelo + tran.getCantReserva());
            }

        }
    }

    private boolean esValidaReserva(Transacion transacion) {
        //verifica si la fecha de la reserva es valida
        Date hoy = new Date();
        Calendar fechaVto = Calendar.getInstance();
        fechaVto.setTime(hoy);
        fechaVto.add(Calendar.DAY_OF_YEAR, -3);

        Calendar fechaTransacion = Calendar.getInstance();
        fechaTransacion.setTime(transacion.getFecha());

        return fechaTransacion.after(fechaVto);
    }

    private List<Vuelo> vuelosDiarios(Date fecha, Aeropuerto aeropuertoOrigen,
            int cantPersonas) {
        //devuelve una lista de aeropuertos destinos de vuelos en el día
        //que tengan disponibles para la cantidad de personas
        List<Vuelo> adyacentes = new ArrayList<>();
        Calendar fechaICalendario = Calendar.getInstance();
        fechaICalendario.setTime(fecha); // Configuramos la fecha que se recibe
        Calendar fechaFCalendario = Calendar.getInstance();
        fechaFCalendario.setTime(fecha);
        fechaFCalendario.add(Calendar.DAY_OF_YEAR, 1); //agregamos un dia
        vuelos.stream().filter((temp) -> (temp.getAeropuertoOrigen().getCodigoAeropuerto().equals(
                aeropuertoOrigen.getCodigoAeropuerto()))).forEach((temp) -> {
                    Calendar tempfecha = Calendar.getInstance();
                    tempfecha.setTime(temp.getFecha());
                    if (tempfecha.after(fechaICalendario)
                    && tempfecha.before(fechaFCalendario)
                    && temp.getDisponibles() >= cantPersonas) {
                        adyacentes.add(temp);
                    }
                });
        return adyacentes;
    }

    private NodoAeropuerto buscarNodoFin(Aeropuerto fin) {
        //busca el nodo con el aeropuerto destino, para luego armar ruta
        NodoAeropuerto nodo = null;
        for (NodoAeropuerto temp : nodosListos) {
            if (temp.getAeropuerto().getCodigoAeropuerto().equals(
                    fin.getCodigoAeropuerto())) {
                nodo = temp;
            }
        }
        return nodo;
    }

    private boolean fueVisitadoNodo(Aeropuerto aeropuerto) {
        // verifica si un aeropuerto ya fue visitado
        boolean nodo = false;
        for (NodoAeropuerto temp : nodosListos) {
            if (temp.getAeropuerto().getCodigoAeropuerto().equals(
                    aeropuerto.getCodigoAeropuerto())) {
                nodo = true;
            }
        }
        return nodo;
    }

    // encuentra la ruta de menos costo desde un origen a un aeropuerto destino
    private List<Vuelo> devolverRutaMenorCosto(Date fecha, Aeropuerto origen,
            Aeropuerto destino, int cantPersonas) {
        // calcula la ruta más corta del inicio a todos los demás aeropuertos
        devolverRutaMenorCostoDijkstra(fecha, origen, cantPersonas);
        List<Vuelo> ruta = new ArrayList<>();
        // recupera el nodo final de la lista de terminados
        NodoAeropuerto nodo;
        nodo = buscarNodoFin(destino);
        if (nodo == null) {
            return ruta;
        }
        // crea una pila para almacenar la ruta desde el nodo final al origen
        Stack<NodoAeropuerto> pila = new Stack<>();
        while (nodo.getProcedencia() != null) {
            pila.add(nodo);
            nodo = nodo.getProcedencia();
        }
        // recorre la pila para armar la ruta en el orden correcto
        while (!pila.isEmpty()) {
            ruta.add(pila.pop().getVuelo());
        }
        return ruta;
    }

    // encuentra la ruta más corta desde el nodo inicial a todos los demás
    private void devolverRutaMenorCostoDijkstra(Date fecha, Aeropuerto inicio, int cantPersonas) {
        Queue<NodoAeropuerto> cola = new PriorityQueue<>(); // cola de prioridad
        NodoAeropuerto ni = new NodoAeropuerto(inicio);          // nodo inicial
        nodosListos = new LinkedList<>();// lista de nodos ya revisados
        cola.add(ni);             // Agregar nodo inicial a la cola de prioridad
        Date fechaVuelo;
        while (!cola.isEmpty()) {        // mientras que la cola no esta vacia
            NodoAeropuerto tmp = cola.poll();     // saca el primer elemento
            nodosListos.add(tmp);           // lo manda a la lista de terminados
            fechaVuelo = sumarHoras(fecha, tmp.getDuracion());
            List<Vuelo> vuelosAdy = vuelosDiarios(fechaVuelo, tmp.getAeropuerto(), cantPersonas);
            // si ya habia revisado un aeropuerto lo elimino
            noVisitados(vuelosAdy);
            for (Vuelo tmpVuelo : vuelosAdy) {// revisa los nodos hijos del nodo nodo
                NodoAeropuerto nod = new NodoAeropuerto(
                        tmpVuelo.getAeropuertoDestino(),
                        tmp.getTarifa() + tmpVuelo.getTarifa(),
                        sumarHoras(tmp.getDuracion(), tmpVuelo.getDuracion()),
                        tmpVuelo, tmp);
                // si no está en la cola de prioridad, lo agrega
                if (!cola.contains(nod)) {
                    cola.add(nod);
                    continue;
                }
                // si ya está en la cola de actualiza la distancia menor
                for (NodoAeropuerto x : cola) {
                    // si la distancia es mayor que la distancia calculada
                    if (x.getAeropuerto() == nod.getAeropuerto()
                            && x.getTarifa() > nod.getTarifa()) {
                        cola.remove(x); // remueve el nodo de la cola
                        cola.add(nod);  // agrega el nodo con la nueva distancia
                        break;          // no sigue revisando
                    }
                }
            }
        }
    }

    private Date sumarHoras(Date date1, Date date2) {
        //suma las fechas u horas
        Calendar fechaCal = Calendar.getInstance();
        fechaCal.setTime(date1); // Configuramos la date1 que se recibe

        Calendar horaCal = Calendar.getInstance();
        horaCal.setTime(date2);

        fechaCal.set(Calendar.MINUTE, fechaCal.get(Calendar.MINUTE)
                + horaCal.get(Calendar.MINUTE));
        fechaCal.set(Calendar.HOUR_OF_DAY, fechaCal.get(Calendar.HOUR_OF_DAY)
                + horaCal.get(Calendar.HOUR_OF_DAY));
        return fechaCal.getTime();
    }

    private void noVisitados(List<Vuelo> lista) {
        //elimina los aeropuertos que ya fueron visitados de la lista       
        List<Vuelo> listaRemove = new ArrayList<>();
        for (Vuelo tmp : lista) {
            if (fueVisitadoNodo(tmp.getAeropuertoDestino())) {
                listaRemove.add(tmp);
            }
        }
        for (Vuelo tmp : listaRemove) {
            lista.remove(tmp);
        }
    }
}
