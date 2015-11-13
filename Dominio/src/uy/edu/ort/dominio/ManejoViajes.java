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
    private List<Transacion> transaciones;
    private List<NodoAeropuerto> nodosListos = null; // nodos revisados Dijkstra  

    private ManejoViajes() {
        aeropuertos = new ArrayList<>();
        aerolineas = new ArrayList<>();
        vuelos = new ArrayList<>();
        usuarios = new ArrayList<>();
        transaciones = new ArrayList<>();
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

    public List<Transacion> getTransaciones() {
        return transaciones;
    }

    private List<Vuelo> vuelosDiarios(Date fecha, Aeropuerto aeropuertoOrigen){
        //devuelve una lista de aeropuertos destinos de vuelos en el día
        List<Vuelo> adyacentes = new ArrayList<>();
        Calendar fechaICalendario = Calendar.getInstance();
        fechaICalendario.setTime(fecha); // Configuramos la fecha que se recibe
        Calendar fechaFCalendario = Calendar.getInstance();
        fechaFCalendario.setTime(fecha);
        fechaFCalendario.add(Calendar.DAY_OF_YEAR, 1); //agregamos un dia
        for (Vuelo temp : vuelos) {
            if (temp.getAeropuertoOrigen().getCodigoAeropuerto().equals(
                    aeropuertoOrigen.getCodigoAeropuerto())) {
                Calendar tempfecha = Calendar.getInstance();
                tempfecha.setTime(temp.getFecha());
                if ((tempfecha.after(fechaICalendario) && tempfecha.before(
                        fechaFCalendario))) {
                    adyacentes.add(temp);
                }
            }
        }
        return adyacentes;
    }

    public List<Vuelo> devolverVuelosSinEscala(Date fecha, 
            Aeropuerto aeropuertoOrigen, Aeropuerto aeropuertoDestino) {
        //devuelve una lista de vuelos entre origen y destino
        //posterior a la fecha
        List<Vuelo> adyacentes = new ArrayList<>();
        Calendar fechaICalendario = Calendar.getInstance();
        fechaICalendario.setTime(fecha); // Configuramos la fecha que se recibe
        Calendar fechaFCalendario = Calendar.getInstance();
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

        return null;
    }

    public List<Vuelo> devolverRutaMenorDistancia(Date fecha, 
            Aeropuerto origenAeropuerto, Aeropuerto destinoAeropuerto) {
        // devuelve la ruta de menor distancia entre dos aeropuertos (origen, destino)

        return null;
    }

    // encuentra la ruta de menos costo desde un origen a un aeropuerto destino
    public List<Vuelo> devolverRutaMenorCosto(Date fecha, Aeropuerto origen, 
            Aeropuerto destino) {
        // calcula la ruta más corta del inicio a los demás
        encontrarRutaMinimaVueloDijkstra(fecha, origen);
        List<Vuelo> ruta = new ArrayList<>();
        // recupera el nodo final de la lista de terminados
        NodoAeropuerto tmp;
        tmp = buscarNodoFin(destino);
        if (tmp == null) {
            return ruta;
        }
        // crea una pila para almacenar la ruta desde el nodo final al origen
        Stack<NodoAeropuerto> pila = new Stack<>();
        while (tmp.getProcedencia() != null)
        {
            pila.add(tmp);
            tmp = tmp.getProcedencia();
        }
        // recorre la pila para armar la ruta en el orden correcto
        while (!pila.isEmpty()) {
            ruta.add(pila.pop().getVuelo());
        }
        return ruta;
    }

    private NodoAeropuerto buscarNodoFin(Aeropuerto fin) {
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

    // encuentra la ruta más corta desde el nodo inicial a todos los demás
    private void encontrarRutaMinimaVueloDijkstra(Date fecha, Aeropuerto inicio) {
        Queue<NodoAeropuerto> cola = new PriorityQueue<>(); // cola de prioridad
        NodoAeropuerto ni = new NodoAeropuerto(inicio);          // nodo inicial
        nodosListos = new LinkedList<>();// lista de nodos ya revisados
        cola.add(ni);             // Agregar nodo inicial a la cola de prioridad
        Date fechaAux;
        while (!cola.isEmpty()) {        // mientras que la cola no esta vacia
            NodoAeropuerto tmp = cola.poll();     // saca el primer elemento
            nodosListos.add(tmp);           // lo manda a la lista de terminados
            fechaAux = sumarHoras(fecha, tmp.getDuracion());
            List<Vuelo> vuelosAdy = vuelosDiarios(fechaAux, tmp.getAeropuerto());
            // si ya habia revisado un aeropuerto lo elimino
            noVisitados(vuelosAdy);
            for (Vuelo tmpVuelo : vuelosAdy) {// revisa los nodos hijos del nodo tmp
                NodoAeropuerto nod = new NodoAeropuerto(
                        tmpVuelo.getAeropuertoDestino(),
                        tmp.getTarifa() + tmpVuelo.getTarifa(),
                        sumarHoras(tmp.getDuracion(),tmpVuelo.getDuracion()),
                        tmpVuelo , tmp);
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
    
    public Date sumarHoras(Date date1, Date date2){
        //suma las fechas u horas
        Calendar fechaCal = Calendar.getInstance();
        fechaCal.setTime(date1); // Configuramos la date1 que se recibe
        
        Calendar horaCal = Calendar.getInstance();
        horaCal.setTime(date2);
        
        fechaCal.set(Calendar.MINUTE,fechaCal.get(Calendar.MINUTE)+ 
                horaCal.get(Calendar.MINUTE));
        fechaCal.set(Calendar.HOUR_OF_DAY,fechaCal.get(Calendar.HOUR_OF_DAY)+ 
                horaCal.get(Calendar.HOUR_OF_DAY));
        
        return fechaCal.getTime();
    }

    private void noVisitados(List<Vuelo> lista) {
        //remueve los aeropuertos que ya fueron visitados en los adyacentes        
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
