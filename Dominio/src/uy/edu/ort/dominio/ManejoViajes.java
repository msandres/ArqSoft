/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uy.edu.ort.dominio;

import java.text.SimpleDateFormat;
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
    private List<NodoAeropuerto> nodosListos = null; // nodos revisados Dijkstra  

    private ManejoViajes() {
        aeropuertos = new ArrayList<>();
        aerolineas = new ArrayList<>();
        vuelos = new ArrayList<>();
        usuarios = new ArrayList<>();
        compras = new ArrayList<>();

        //borrar cuando se haga la base de datos
        cargarDatos();
    }

    public static ManejoViajes obtenerInstanciaManejoViajes() {
        if (instanciaManejoViajes == null) {
            instanciaManejoViajes = new ManejoViajes();
        }
        return instanciaManejoViajes;
    }

    public String validarUsuario(String userName, String contraseña) {
        String retorno = "ERROR";
        for (Usuario u : usuarios) {
            if (u.getUser().equals(userName))  {
                if ((u.getPass().equals(contraseña))) {
                    retorno = "OK";
                }
                else retorno ="Contraseña incorrecta";
            }
            else retorno = "Usuario: "+userName+" inexistente";
        }
        return retorno;
    }

    public Usuario obtenerUsuaio(String userName, String contraseña) {
        Usuario usuario = null;
        for (Usuario u : usuarios) {
            if ((u.getUser().equals(userName)) && (u.getPass().equals(contraseña))) {
                return u;
            }
        }
        return usuario;
    }

    public String agregarAerolinea(String nombreAerolinea, String codigoAerolinea, String informacion) {
        String retorno = "";
        try {
            if (perteneceAerolinea(codigoAerolinea)) {
                retorno = "Aerolinea existente!!";
            } else {
                Aerolinea aerolinea = new Aerolinea(nombreAerolinea, codigoAerolinea, informacion);
                retorno = "Se ha agregado correctamente!!";
            }

        } catch (Exception e) {
            retorno = e.getMessage();
        }
        return retorno;
    }

    public String modificarAerolinea(String codigoAerolinea, String nuevoNombreAerolinea, String nuevoCodigoAerolinea, String nuevaInformacion) {
        String retorno = "No existe Aerolinea de codigo: " + codigoAerolinea;
        try {
            if (perteneceAerolinea(nuevoCodigoAerolinea)) {
                retorno = "Aerolinea de codigo " + nuevoCodigoAerolinea + " ya existe!!";
            } else {
                for (Aerolinea a : aerolineas) {
                    if (a.getCodigoAerolinea().equals(codigoAerolinea)) {
                        a.setCodigoAerolinea(nuevoCodigoAerolinea);
                        a.setInformacion(nuevaInformacion);
                        a.setNombre(nuevoNombreAerolinea);
                        return "Aerolinea modificada correctamente!!";
                    }
                }
            }
        } catch (Exception e) {
            retorno = e.getMessage();
        }
        return retorno;
    }

    public String bajaAerolinea(String codigoAerolinea) {
        String retorno = "No existe Aerolinea de codigo: " + codigoAerolinea;
        try {
            Aerolinea tmp = null;
            for (Aerolinea a : aerolineas) {
                if (a.getCodigoAerolinea().equals(codigoAerolinea)) {
                    tmp = a;
                }
            }
            if (tmp != null) {
                aerolineas.remove(tmp);
                retorno = "Aerolinea eliminada correctamente!!";
            }
        } catch (Exception e) {
            retorno = e.getMessage();
        }
        return retorno;
    }

    public List<Aeropuerto> getAeropuertos() {
        return aeropuertos;
    }

    public List<Aerolinea> getAerolineas() {
        return aerolineas;
    }

    public void agregarUsuario(Usuario us) {
        if (!perteneceUsuario(us)) {
            usuarios.add(us);
        }
    }

    public List<Transacion> historialTransaciones(Cliente cliente) {
        //devuelve el listado de transaciones que ha efectuado dicho cliente
        List<Transacion> transacionesCliente = new ArrayList<>();
        for (Transacion t : compras) {
            if (t.getCliente().getUser().equals(cliente.getUser())) {
                transacionesCliente.add(t);
            }
        }
        return transacionesCliente;
    }

    public List<Vuelo> consultaVuelosSinEscala(Date fecha,
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

    // encuentra la ruta de menos costo desde un origen a un aeropuerto destino
    public List<Vuelo> consultaVuelos(Date fecha, Aeropuerto origen,
            Aeropuerto destino, int cantPersonas) {
        // Dijkstra calcula la ruta más corta de inicio a los demás aeropuertos
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

    public Cliente getCliente(String username) {
        Cliente cli = new Cliente();
        for (Usuario us : this.usuarios) {
            if (us.getUser().equals(username)) {
                return (Cliente) us;
            }
        }
        return cli;
    }

    public Aeropuerto getAeropuerto(String aeropuerto) {
        Aeropuerto aero = new Aeropuerto();
        for (Aeropuerto a : this.aeropuertos) {
            if (a.getCodigoAeropuerto().equals(aeropuerto)) {
                return a;
            }
        }
        return aero;
    }

    public String reservarVuelo(int idVuelo, Cliente cliente, long cantPersonas) {
        //reservar Vuelo
        String retorno;
        try {
            actualizarReservas();
            Vuelo vuelo = obtenerVuelo(idVuelo);
            Transacion transacion = new Transacion();
            transacion.setEstado("reserva");
            transacion.setCliente(cliente);
            String hex = Long.toHexString(transacion.getId());
            transacion.setCodigoReserva("Res" + hex);
            transacion.setFecha(new Date());
            long disponiblesVuelo = vuelo.getDisponibles();
            vuelo.setDisponibles(disponiblesVuelo - cantPersonas);
            transacion.setVuelo(vuelo);
            //insertar a tabla
            compras.add(transacion);
            retorno = "Reserva realizada exitosamente, código reserva: "
                    + transacion.getCodigoReserva();
        } catch (Exception e) {
            retorno = e.getMessage();
        }
        return retorno;
    }

    public String comprarVuelo(int idVuelo, Cliente cliente, long cantPersonas) {
        String retorno;
        try {
            Transacion transacion = new Transacion();
            Vuelo vuelo = obtenerVuelo(idVuelo);
            long disponiblesVuelo = vuelo.getDisponibles();
            vuelo.setDisponibles(disponiblesVuelo - cantPersonas);
            String hex = Long.toHexString(transacion.getId());
            transacion.setCodigoReserva("C" + hex);
            transacion.setEstado("confirmada");
            transacion.setCliente(cliente);
            transacion.setFecha(new Date());
            transacion.setVuelo(vuelo);
            //insertar a tabla
            compras.add(transacion);
            retorno = "Compra realizada exitosamente, código compra: "
                    + transacion.getCodigoReserva();
        } catch (Exception e) {
            retorno = e.getMessage();
        }
        return retorno;
    }

    public String comprarReserva(String codigoReserva, Cliente cliente) {
        //concretar una reserva
        String retorno;
        try {
            Transacion transacion = obtenerReserva(codigoReserva);
            transacion.setEstado("confirmada");
            transacion.setFecha(new Date());
            String hex = Long.toHexString(transacion.getId());
            transacion.setCodigoReserva("C" + hex);
            //insertar a tabla
            compras.add(transacion);
            retorno = "Compra realizada exitosamente, código compra: "
                    + transacion.getCodigoReserva();
        } catch (Exception e) {
            retorno = e.getMessage();
        }
        return retorno;
    }

    /**
     * Metodos auxiliares
     */
    private Vuelo obtenerVuelo(int id) {
        Vuelo vuelo = new Vuelo();
        for (Vuelo v : vuelos) {
            if (v.getId() == id) {
                return v;
            }
        }
        return vuelo;
    }

    private Transacion obtenerReserva(String codigoReserva) {
        actualizarReservas();
        Transacion transacion = new Transacion();
        for (Transacion t : compras) {
            if (t.getCodigoReserva().equals(codigoReserva)) {
                return t;
            }
        }
        return transacion;
    }

    private boolean perteneceUsuario(Usuario usuario) {
        boolean esta = false;
        for (Usuario us : usuarios) {
            if (us.getUser().equals(usuario.getUser())) {
                return true;
            }
        }

        return esta;
    }

    private boolean perteneceAerolinea(String codigoAerolinea) {
        boolean esta = false;
        for (Aerolinea a : aerolineas) {
            if (a.getCodigoAerolinea().equals(codigoAerolinea)) {
                return true;
            }
        }
        return esta;
    }

    private void actualizarReservas() {
        //borra toda transacion pendiente que se anterior a 72 hs.
        for (Transacion tran : compras) {
            if (!esValidaReserva(tran) && (tran.getEstado().equals("reserva"))) {
                compras.remove(tran);
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

    // encuentra la ruta más corta desde el nodo inicial a todos los demás
    private void devolverRutaMenorCostoDijkstra(Date fecha, Aeropuerto inicio,
            int cantPersonas) {
        Queue<NodoAeropuerto> cola = new PriorityQueue<>(); // cola de prioridad
        NodoAeropuerto ni = new NodoAeropuerto(inicio);          // nodo inicial
        nodosListos = new LinkedList<>();// lista de nodos ya revisados
        cola.add(ni);             // Agregar nodo inicial a la cola de prioridad
        Date fechaV;
        while (!cola.isEmpty()) {        // mientras que la cola no esta vacia
            NodoAeropuerto tmp = cola.poll();     // saca el primer elemento
            nodosListos.add(tmp);           // lo manda a la lista de terminados
            fechaV = sumarHoras(fecha, tmp.getDuracion());
            List<Vuelo> vuelosAdy = vuelosDiarios(fechaV, tmp.getAeropuerto(),
                    cantPersonas);
            // si ya habia revisado un aeropuerto lo elimino
            noVisitados(vuelosAdy);
            for (Vuelo tmpVuelo : vuelosAdy) {// revisa los nodos hijos
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

    //borrar despues que funcione la base de datos
    private void cargarDatos() {
        cargarAeropuertos();
        cargarAerolineas();
        cargarVuelos();
        cargarUsuarios();
//        cargarTransaciones();
    }

    private void cargarAeropuertos() {
        Aeropuerto aerop1 = new Aeropuerto(2564, "Guarulhos Gov Andre Franco Montouro", "GRU", "Sao Paulo", "Brazil", (float) -23.432075, (float) -46.469511);
        Aeropuerto aerop2 = new Aeropuerto(2612, "Santos Dumont", "SDU", "Rio De Janeiro", "Brazil", (float) -22.910461, (float) -43.163133);
        Aeropuerto aerop3 = new Aeropuerto(2599, "Salgado Filho", "POA", "Porto Alegre", "Brazil", (float) -29.994428, (float) -51.171428);
        Aeropuerto aerop4 = new Aeropuerto(3988, "Ministro Pistarini", "EZE", "Buenos Aires", "Argentina", (float) -34.822222, (float) -58.535833);
        Aeropuerto aerop5 = new Aeropuerto(2816, "Carrasco Intl", "MVD", "Montevideo", "Uruguay", (float) -34.838417, (float) -56.030806);
        Aeropuerto aerop6 = new Aeropuerto(2650, "Arturo Merino Benitez Intl", "SCL", "Santiago", "Chile", (float) -33.392975, (float) -70.785803);
        Aeropuerto aerop7 = new Aeropuerto(2699, "Silvio Pettirossi Intl", "ASU", "Asuncion", "Paraguay", (float) -25.23985, (float) -57.519133);
        Aeropuerto aerop8 = new Aeropuerto(2709, "Eldorado Intl", "BOG", "Bogota", "Colombia", (float) 4.701594, (float) -74.146947);
        Aeropuerto aerop9 = new Aeropuerto(2789, "Jorge Chavez Intl", "LIM", "Lima", "Peru", (float) -12.021889, (float) -77.114319);
        Aeropuerto aerop10 = new Aeropuerto(2762, "El Alto Intl", "LPB", "La Paz", "Bolivia", (float) -16.513339, (float) -68.192256);
        Aeropuerto aerop11 = new Aeropuerto(2688, "Mariscal Sucre Intl", "UIO", "Quito", "Ecuador", (float) -0.141144, (float) -78.488214);
        Aeropuerto aerop12 = new Aeropuerto(2851, "Simon Bolivar Intl", "CCS", "Caracas", "Venezuela", (float) 10.603117, (float) -66.990583);
        Aeropuerto aerop13 = new Aeropuerto(1824, "Licenciado Benito Juarez Intl", "MEX", "Mexico City", "Mexico", (float) 19.436303, (float) -99.072097);
        Aeropuerto aerop14 = new Aeropuerto(3797, "John F Kennedy Intl", "JFK", "New York", "United States", (float) 40.639751, (float) -73.778925);
        Aeropuerto aerop15 = new Aeropuerto(1871, "Tocumen Intl", "PTY", "Panama City", "Panama", (float) 9.071364, (float) -79.383453);
        Aeropuerto aerop16 = new Aeropuerto(1229, "Barajas", "MAD", "Madrid", "Spain", (float) 40.493556, (float) -3.566764);
        Aeropuerto aerop17 = new Aeropuerto(1555, "Fiumicino", "FCO", "Rome", "Italy", (float) 41.804475, (float) 12.250797);
        Aeropuerto aerop18 = new Aeropuerto(507, "Heathrow", "LHR", "London", "United Kingdom", (float) 51.4775, (float) -0.461389);
        Aeropuerto aerop19 = new Aeropuerto(1382, "Charles De Gaulle", "CDG", "Paris", "France", (float) 49.012779, (float) 2.55);
        Aeropuerto aerop20 = new Aeropuerto(351, "Tegel", "TXL", "Berlin", "Germany", (float) 52.559686, (float) 13.287711);

        aeropuertos.add(aerop1);
        aeropuertos.add(aerop2);
        aeropuertos.add(aerop3);
        aeropuertos.add(aerop4);
        aeropuertos.add(aerop5);
        aeropuertos.add(aerop6);
        aeropuertos.add(aerop7);
        aeropuertos.add(aerop8);
        aeropuertos.add(aerop9);
        aeropuertos.add(aerop10);
        aeropuertos.add(aerop11);
        aeropuertos.add(aerop12);
        aeropuertos.add(aerop13);
        aeropuertos.add(aerop14);
        aeropuertos.add(aerop15);
        aeropuertos.add(aerop16);
        aeropuertos.add(aerop17);
        aeropuertos.add(aerop18);
        aeropuertos.add(aerop19);
        aeropuertos.add(aerop20);
    }

    private void cargarAerolineas() {
        Aerolinea aerol1 = new Aerolinea(3200, "LAN Airlines", "LAN", "www.LANAirlines.com");
        Aerolinea aerol2 = new Aerolinea(4987, "TAM", "TML", "www.TAM.com");
        Aerolinea aerol3 = new Aerolinea(1889, "Copa Airlines", "CMP", "www.CopaAirlines.com");
        Aerolinea aerol4 = new Aerolinea(412, "Aerolineas Argentinas", "ARG", "www.AerolineasArgentinas.com");
        Aerolinea aerol5 = new Aerolinea(2822, "Iberia Airlines", "IBE", "www.IberiaAirlines.com");
        Aerolinea aerol6 = new Aerolinea(3320, "Lufthansa", "DLH", "www.Lufthansa.com");
        Aerolinea aerol7 = new Aerolinea(5354, "Varig Log", "VLO", "www.VarigLog.com");
        Aerolinea aerol8 = new Aerolinea(474, "AeroTACA", "ATK", "www.AeroTACA.com");
        Aerolinea aerol9 = new Aerolinea(515, "Avianca - Aerovias Nacionales de Colombia", "AVA", "www.Avianca-AeroviasNacionalesdeColombia.com");
        Aerolinea aerol10 = new Aerolinea(4091, "Qatar Airways", "QTR", "www.QatarAirways.com");
        Aerolinea aerol11 = new Aerolinea(4951, "Turkish Airlines", "THY", "www.TurkishAirlines.com");
        Aerolinea aerol12 = new Aerolinea(2183, "Emirates", "UAE", "www.Emirates.com");
        aerolineas.add(aerol1);
        aerolineas.add(aerol2);
        aerolineas.add(aerol3);
        aerolineas.add(aerol4);
        aerolineas.add(aerol5);
        aerolineas.add(aerol6);
        aerolineas.add(aerol7);
        aerolineas.add(aerol8);
        aerolineas.add(aerol9);
        aerolineas.add(aerol10);
        aerolineas.add(aerol11);
        aerolineas.add(aerol12);
    }

    private void cargarVuelos() {
        Aerolinea aerol1 = new Aerolinea(3200, "LAN Airlines", "LAN", "www.LANAirlines.com");
        Aerolinea aerol2 = new Aerolinea(4987, "TAM", "TML", "www.TAM.com");
        Aerolinea aerol3 = new Aerolinea(1889, "Copa Airlines", "CMP", "www.CopaAirlines.com");
        Aerolinea aerol4 = new Aerolinea(412, "Aerolineas Argentinas", "ARG", "www.AerolineasArgentinas.com");
        Aerolinea aerol5 = new Aerolinea(2822, "Iberia Airlines", "IBE", "www.IberiaAirlines.com");
        Aerolinea aerol6 = new Aerolinea(3320, "Lufthansa", "DLH", "www.Lufthansa.com");
        Aerolinea aerol7 = new Aerolinea(5354, "Varig Log", "VLO", "www.VarigLog.com");
        Aerolinea aerol8 = new Aerolinea(474, "AeroTACA", "ATK", "www.AeroTACA.com");
        Aerolinea aerol9 = new Aerolinea(515, "Avianca - Aerovias Nacionales de Colombia", "AVA", "www.Avianca-AeroviasNacionalesdeColombia.com");
        Aerolinea aerol10 = new Aerolinea(4091, "Qatar Airways", "QTR", "www.QatarAirways.com");
        Aerolinea aerol11 = new Aerolinea(4951, "Turkish Airlines", "THY", "www.TurkishAirlines.com");
        Aerolinea aerol12 = new Aerolinea(2183, "Emirates", "UAE", "www.Emirates.com");

        Aeropuerto aerop1 = new Aeropuerto(2564, "Guarulhos Gov Andre Franco Montouro", "GRU", "Sao Paulo", "Brazil", (float) -23.432075, (float) -46.469511);
        Aeropuerto aerop2 = new Aeropuerto(2612, "Santos Dumont", "SDU", "Rio De Janeiro", "Brazil", (float) -22.910461, (float) -43.163133);
        Aeropuerto aerop3 = new Aeropuerto(2599, "Salgado Filho", "POA", "Porto Alegre", "Brazil", (float) -29.994428, (float) -51.171428);
        Aeropuerto aerop4 = new Aeropuerto(3988, "Ministro Pistarini", "EZE", "Buenos Aires", "Argentina", (float) -34.822222, (float) -58.535833);
        Aeropuerto aerop5 = new Aeropuerto(2816, "Carrasco Intl", "MVD", "Montevideo", "Uruguay", (float) -34.838417, (float) -56.030806);
        Aeropuerto aerop6 = new Aeropuerto(2650, "Arturo Merino Benitez Intl", "SCL", "Santiago", "Chile", (float) -33.392975, (float) -70.785803);
        Aeropuerto aerop7 = new Aeropuerto(2699, "Silvio Pettirossi Intl", "ASU", "Asuncion", "Paraguay", (float) -25.23985, (float) -57.519133);
        Aeropuerto aerop8 = new Aeropuerto(2709, "Eldorado Intl", "BOG", "Bogota", "Colombia", (float) 4.701594, (float) -74.146947);
        Aeropuerto aerop9 = new Aeropuerto(2789, "Jorge Chavez Intl", "LIM", "Lima", "Peru", (float) -12.021889, (float) -77.114319);
        Aeropuerto aerop10 = new Aeropuerto(2762, "El Alto Intl", "LPB", "La Paz", "Bolivia", (float) -16.513339, (float) -68.192256);
        Aeropuerto aerop11 = new Aeropuerto(2688, "Mariscal Sucre Intl", "UIO", "Quito", "Ecuador", (float) -0.141144, (float) -78.488214);
        Aeropuerto aerop12 = new Aeropuerto(2851, "Simon Bolivar Intl", "CCS", "Caracas", "Venezuela", (float) 10.603117, (float) -66.990583);
        Aeropuerto aerop13 = new Aeropuerto(1824, "Licenciado Benito Juarez Intl", "MEX", "Mexico City", "Mexico", (float) 19.436303, (float) -99.072097);
        Aeropuerto aerop14 = new Aeropuerto(3797, "John F Kennedy Intl", "JFK", "New York", "United States", (float) 40.639751, (float) -73.778925);
        Aeropuerto aerop15 = new Aeropuerto(1871, "Tocumen Intl", "PTY", "Panama City", "Panama", (float) 9.071364, (float) -79.383453);
        Aeropuerto aerop16 = new Aeropuerto(1229, "Barajas", "MAD", "Madrid", "Spain", (float) 40.493556, (float) -3.566764);
        Aeropuerto aerop17 = new Aeropuerto(1555, "Fiumicino", "FCO", "Rome", "Italy", (float) 41.804475, (float) 12.250797);
        Aeropuerto aerop18 = new Aeropuerto(507, "Heathrow", "LHR", "London", "United Kingdom", (float) 51.4775, (float) -0.461389);
        Aeropuerto aerop19 = new Aeropuerto(1382, "Charles De Gaulle", "CDG", "Paris", "France", (float) 49.012779, (float) 2.55);
        Aeropuerto aerop20 = new Aeropuerto(351, "Tegel", "TXL", "Berlin", "Germany", (float) 52.559686, (float) 13.287711);

        SimpleDateFormat formatDia = new SimpleDateFormat("dd-MM-yyyy kk:mm");
        SimpleDateFormat formatHora = new SimpleDateFormat("kk:mm");
        try {
            Vuelo vuelo1 = new Vuelo(1, aerol1, aerop1, aerop7, formatDia.parse("13-11-2015 16:48"), formatHora.parse("01:43"), (float) 215, 27);
            Vuelo vuelo2 = new Vuelo(2, aerol2, aerop1, aerop7, formatDia.parse("13-11-2015 19:55"), formatHora.parse("01:43"), (float) 197, 48);
            Vuelo vuelo3 = new Vuelo(3, aerol3, aerop1, aerop7, formatDia.parse("13-11-2015 16:04"), formatHora.parse("01:43"), (float) 208, 80);
            Vuelo vuelo4 = new Vuelo(4, aerol4, aerop4, aerop7, formatDia.parse("14-11-2015 13:55"), formatHora.parse("01:37"), (float) 185, 32);
            Vuelo vuelo5 = new Vuelo(5, aerol5, aerop4, aerop7, formatDia.parse("12-11-2015 06:28"), formatHora.parse("01:37"), (float) 200, 54);
            Vuelo vuelo6 = new Vuelo(6, aerol6, aerop4, aerop7, formatDia.parse("10-11-2015 10:04"), formatHora.parse("01:37"), (float) 201, 71);
            Vuelo vuelo7 = new Vuelo(7, aerol7, aerop6, aerop7, formatDia.parse("12-11-2015 12:28"), formatHora.parse("02:23"), (float) 309, 86);
            Vuelo vuelo8 = new Vuelo(8, aerol8, aerop6, aerop7, formatDia.parse("10-11-2015 06:43"), formatHora.parse("02:23"), (float) 321, 13);
            Vuelo vuelo9 = new Vuelo(9, aerol9, aerop9, aerop7, formatDia.parse("14-11-2015 09:36"), formatHora.parse("03:50"), (float) 462, 53);
            Vuelo vuelo10 = new Vuelo(10, aerol10, aerop15, aerop7, formatDia.parse("14-11-2015 23:16"), formatHora.parse("06:48"), (float) 889, 27);
            Vuelo vuelo11 = new Vuelo(11, aerol11, aerop1, aerop8, formatDia.parse("10-11-2015 06:14"), formatHora.parse("06:34"), (float) 796, 0);
            Vuelo vuelo12 = new Vuelo(12, aerol12, aerop1, aerop8, formatDia.parse("13-11-2015 03:21"), formatHora.parse("06:34"), (float) 896, 87);
            Vuelo vuelo13 = new Vuelo(13, aerol1, aerop1, aerop8, formatDia.parse("14-11-2015 16:48"), formatHora.parse("06:34"), (float) 910, 98);
            Vuelo vuelo14 = new Vuelo(14, aerol2, aerop4, aerop8, formatDia.parse("11-11-2015 20:09"), formatHora.parse("07:06"), (float) 893, 89);
            Vuelo vuelo15 = new Vuelo(15, aerol3, aerop4, aerop8, formatDia.parse("14-11-2015 12:28"), formatHora.parse("07:06"), (float) 837, 61);
            Vuelo vuelo16 = new Vuelo(16, aerol4, aerop6, aerop8, formatDia.parse("10-11-2015 23:02"), formatHora.parse("06:26"), (float) 869, 50);
            Vuelo vuelo17 = new Vuelo(17, aerol5, aerop6, aerop8, formatDia.parse("14-11-2015 00:43"), formatHora.parse("06:26"), (float) 770, 42);
            Vuelo vuelo18 = new Vuelo(18, aerol6, aerop9, aerop8, formatDia.parse("14-11-2015 08:38"), formatHora.parse("02:51"), (float) 393, 27);
            Vuelo vuelo19 = new Vuelo(19, aerol7, aerop9, aerop8, formatDia.parse("10-11-2015 06:57"), formatHora.parse("02:51"), (float) 333, 30);
            Vuelo vuelo20 = new Vuelo(20, aerol8, aerop10, aerop8, formatDia.parse("11-11-2015 08:09"), formatHora.parse("03:42"), (float) 507, 35);
            Vuelo vuelo21 = new Vuelo(21, aerol9, aerop11, aerop8, formatDia.parse("10-11-2015 06:00"), formatHora.parse("01:05"), (float) 132, 0);
            Vuelo vuelo22 = new Vuelo(22, aerol10, aerop11, aerop8, formatDia.parse("10-11-2015 11:02"), formatHora.parse("01:05"), (float) 147, 32);
            Vuelo vuelo23 = new Vuelo(23, aerol11, aerop11, aerop8, formatDia.parse("14-11-2015 16:33"), formatHora.parse("01:05"), (float) 151, 38);
            Vuelo vuelo24 = new Vuelo(24, aerol12, aerop11, aerop8, formatDia.parse("12-11-2015 00:28"), formatHora.parse("01:05"), (float) 145, 93);
            Vuelo vuelo25 = new Vuelo(25, aerol1, aerop12, aerop8, formatDia.parse("14-11-2015 22:04"), formatHora.parse("01:33"), (float) 207, 17);
            Vuelo vuelo26 = new Vuelo(26, aerol2, aerop12, aerop8, formatDia.parse("10-11-2015 18:00"), formatHora.parse("01:33"), (float) 180, 34);
            Vuelo vuelo27 = new Vuelo(27, aerol3, aerop12, aerop8, formatDia.parse("13-11-2015 10:33"), formatHora.parse("01:33"), (float) 204, 0);
            Vuelo vuelo28 = new Vuelo(28, aerol4, aerop12, aerop8, formatDia.parse("12-11-2015 11:02"), formatHora.parse("01:33"), (float) 198, 26);
            Vuelo vuelo29 = new Vuelo(29, aerol5, aerop13, aerop8, formatDia.parse("13-11-2015 07:26"), formatHora.parse("04:47"), (float) 616, 84);
            Vuelo vuelo30 = new Vuelo(30, aerol6, aerop13, aerop8, formatDia.parse("10-11-2015 00:14"), formatHora.parse("04:47"), (float) 630, 44);
            Vuelo vuelo31 = new Vuelo(31, aerol7, aerop13, aerop8, formatDia.parse("13-11-2015 16:48"), formatHora.parse("04:47"), (float) 564, 82);
            Vuelo vuelo32 = new Vuelo(32, aerol8, aerop14, aerop8, formatDia.parse("13-11-2015 17:16"), formatHora.parse("06:03"), (float) 814, 77);
            Vuelo vuelo33 = new Vuelo(33, aerol9, aerop14, aerop8, formatDia.parse("10-11-2015 04:33"), formatHora.parse("06:03"), (float) 820, 42);
            Vuelo vuelo34 = new Vuelo(34, aerol10, aerop14, aerop8, formatDia.parse("10-11-2015 08:09"), formatHora.parse("06:03"), (float) 769, 26);
            Vuelo vuelo35 = new Vuelo(35, aerol11, aerop14, aerop8, formatDia.parse("14-11-2015 19:40"), formatHora.parse("06:03"), (float) 767, 69);
            Vuelo vuelo36 = new Vuelo(36, aerol12, aerop15, aerop8, formatDia.parse("11-11-2015 15:21"), formatHora.parse("01:08"), (float) 132, 57);
            Vuelo vuelo37 = new Vuelo(37, aerol1, aerop15, aerop8, formatDia.parse("13-11-2015 16:33"), formatHora.parse("01:08"), (float) 137, 98);
            Vuelo vuelo38 = new Vuelo(38, aerol2, aerop16, aerop8, formatDia.parse("10-11-2015 05:16"), formatHora.parse("12:10"), (float) 1434, 59);
            Vuelo vuelo39 = new Vuelo(39, aerol3, aerop16, aerop8, formatDia.parse("13-11-2015 23:31"), formatHora.parse("12:10"), (float) 1496, 97);
            Vuelo vuelo40 = new Vuelo(40, aerol4, aerop19, aerop8, formatDia.parse("13-11-2015 10:04"), formatHora.parse("13:06"), (float) 1567, 64);
            Vuelo vuelo41 = new Vuelo(41, aerol5, aerop1, aerop12, formatDia.parse("12-11-2015 23:45"), formatHora.parse("06:39"), (float) 771, 70);
            Vuelo vuelo42 = new Vuelo(42, aerol6, aerop1, aerop12, formatDia.parse("13-11-2015 13:12"), formatHora.parse("06:39"), (float) 878, 85);
            Vuelo vuelo43 = new Vuelo(43, aerol7, aerop4, aerop12, formatDia.parse("14-11-2015 23:45"), formatHora.parse("07:46"), (float) 904, 72);
            Vuelo vuelo44 = new Vuelo(44, aerol8, aerop4, aerop12, formatDia.parse("13-11-2015 02:52"), formatHora.parse("07:46"), (float) 954, 34);
            Vuelo vuelo45 = new Vuelo(45, aerol9, aerop6, aerop12, formatDia.parse("10-11-2015 09:21"), formatHora.parse("07:26"), (float) 1005, 45);
            Vuelo vuelo46 = new Vuelo(46, aerol10, aerop8, aerop12, formatDia.parse("14-11-2015 14:52"), formatHora.parse("01:33"), (float) 187, 42);
            Vuelo vuelo47 = new Vuelo(47, aerol11, aerop8, aerop12, formatDia.parse("11-11-2015 07:12"), formatHora.parse("01:33"), (float) 211, 7);
            Vuelo vuelo48 = new Vuelo(48, aerol12, aerop8, aerop12, formatDia.parse("11-11-2015 23:45"), formatHora.parse("01:33"), (float) 204, 35);
            Vuelo vuelo49 = new Vuelo(49, aerol1, aerop8, aerop12, formatDia.parse("12-11-2015 18:00"), formatHora.parse("01:33"), (float) 199, 52);
            Vuelo vuelo50 = new Vuelo(50, aerol2, aerop9, aerop12, formatDia.parse("13-11-2015 02:24"), formatHora.parse("04:10"), (float) 505, 3);
            Vuelo vuelo51 = new Vuelo(51, aerol3, aerop9, aerop12, formatDia.parse("11-11-2015 03:21"), formatHora.parse("04:10"), (float) 579, 20);
            Vuelo vuelo52 = new Vuelo(52, aerol4, aerop13, aerop12, formatDia.parse("13-11-2015 04:33"), formatHora.parse("05:25"), (float) 668, 5);
            Vuelo vuelo53 = new Vuelo(53, aerol5, aerop14, aerop12, formatDia.parse("10-11-2015 16:48"), formatHora.parse("05:09"), (float) 628, 85);
            Vuelo vuelo54 = new Vuelo(54, aerol6, aerop15, aerop12, formatDia.parse("13-11-2015 02:38"), formatHora.parse("02:04"), (float) 264, 83);
            Vuelo vuelo55 = new Vuelo(55, aerol7, aerop15, aerop12, formatDia.parse("10-11-2015 08:24"), formatHora.parse("02:04"), (float) 286, 50);
            Vuelo vuelo56 = new Vuelo(56, aerol8, aerop15, aerop12, formatDia.parse("10-11-2015 12:43"), formatHora.parse("02:04"), (float) 265, 56);
            Vuelo vuelo57 = new Vuelo(57, aerol9, aerop16, aerop12, formatDia.parse("14-11-2015 01:12"), formatHora.parse("10:37"), (float) 1452, 11);
            Vuelo vuelo58 = new Vuelo(58, aerol10, aerop16, aerop12, formatDia.parse("10-11-2015 23:02"), formatHora.parse("10:37"), (float) 1431, 75);
            Vuelo vuelo59 = new Vuelo(59, aerol11, aerop16, aerop12, formatDia.parse("11-11-2015 10:19"), formatHora.parse("10:37"), (float) 1266, 14);
            Vuelo vuelo60 = new Vuelo(60, aerol12, aerop17, aerop12, formatDia.parse("10-11-2015 12:00"), formatHora.parse("12:38"), (float) 1742, 72);
            Vuelo vuelo61 = new Vuelo(61, aerol1, aerop19, aerop12, formatDia.parse("13-11-2015 16:04"), formatHora.parse("11:34"), (float) 1565, 46);
            Vuelo vuelo62 = new Vuelo(62, aerol2, aerop1, aerop19, formatDia.parse("11-11-2015 13:40"), formatHora.parse("14:15"), (float) 1644, 72);
            Vuelo vuelo63 = new Vuelo(63, aerol3, aerop1, aerop19, formatDia.parse("13-11-2015 00:00"), formatHora.parse("14:15"), (float) 1806, 85);
            Vuelo vuelo64 = new Vuelo(64, aerol4, aerop4, aerop19, formatDia.parse("11-11-2015 13:40"), formatHora.parse("16:50"), (float) 2000, 5);
            Vuelo vuelo65 = new Vuelo(65, aerol5, aerop4, aerop19, formatDia.parse("11-11-2015 00:00"), formatHora.parse("16:50"), (float) 2005, 79);
            Vuelo vuelo66 = new Vuelo(66, aerol6, aerop6, aerop19, formatDia.parse("10-11-2015 08:38"), formatHora.parse("17:42"), (float) 2183, 28);
            Vuelo vuelo67 = new Vuelo(67, aerol7, aerop6, aerop19, formatDia.parse("10-11-2015 12:57"), formatHora.parse("17:42"), (float) 2172, 77);
            Vuelo vuelo68 = new Vuelo(68, aerol8, aerop8, aerop19, formatDia.parse("11-11-2015 01:55"), formatHora.parse("13:06"), (float) 1514, 18);
            Vuelo vuelo69 = new Vuelo(69, aerol9, aerop9, aerop19, formatDia.parse("14-11-2015 02:24"), formatHora.parse("15:35"), (float) 2014, 42);
            Vuelo vuelo70 = new Vuelo(70, aerol10, aerop9, aerop19, formatDia.parse("13-11-2015 14:38"), formatHora.parse("15:35"), (float) 1892, 59);
            Vuelo vuelo71 = new Vuelo(71, aerol11, aerop12, aerop19, formatDia.parse("12-11-2015 02:24"), formatHora.parse("11:34"), (float) 1500, 96);
            Vuelo vuelo72 = new Vuelo(72, aerol12, aerop13, aerop19, formatDia.parse("14-11-2015 22:33"), formatHora.parse("13:57"), (float) 1772, 36);
            Vuelo vuelo73 = new Vuelo(73, aerol1, aerop13, aerop19, formatDia.parse("10-11-2015 16:19"), formatHora.parse("13:57"), (float) 1589, 96);
            Vuelo vuelo74 = new Vuelo(74, aerol2, aerop13, aerop19, formatDia.parse("14-11-2015 16:04"), formatHora.parse("13:57"), (float) 1868, 54);
            Vuelo vuelo75 = new Vuelo(75, aerol3, aerop14, aerop19, formatDia.parse("10-11-2015 02:09"), formatHora.parse("08:50"), (float) 1039, 88);
            Vuelo vuelo76 = new Vuelo(76, aerol4, aerop14, aerop19, formatDia.parse("12-11-2015 21:21"), formatHora.parse("08:50"), (float) 1114, 32);
            Vuelo vuelo77 = new Vuelo(77, aerol5, aerop14, aerop19, formatDia.parse("10-11-2015 01:26"), formatHora.parse("08:50"), (float) 1201, 15);
            Vuelo vuelo78 = new Vuelo(78, aerol6, aerop14, aerop19, formatDia.parse("10-11-2015 04:33"), formatHora.parse("08:50"), (float) 1100, 18);
            Vuelo vuelo79 = new Vuelo(79, aerol7, aerop14, aerop19, formatDia.parse("14-11-2015 04:19"), formatHora.parse("08:50"), (float) 1078, 76);
            Vuelo vuelo80 = new Vuelo(80, aerol8, aerop14, aerop19, formatDia.parse("12-11-2015 17:16"), formatHora.parse("08:50"), (float) 1041, 22);
            Vuelo vuelo81 = new Vuelo(81, aerol9, aerop14, aerop19, formatDia.parse("14-11-2015 06:14"), formatHora.parse("08:50"), (float) 1186, 10);
            Vuelo vuelo82 = new Vuelo(82, aerol10, aerop14, aerop19, formatDia.parse("11-11-2015 04:33"), formatHora.parse("08:50"), (float) 1119, 3);
            Vuelo vuelo83 = new Vuelo(83, aerol11, aerop14, aerop19, formatDia.parse("10-11-2015 04:04"), formatHora.parse("08:50"), (float) 1158, 21);
            Vuelo vuelo84 = new Vuelo(84, aerol12, aerop14, aerop19, formatDia.parse("10-11-2015 14:52"), formatHora.parse("08:50"), (float) 1027, 98);
            Vuelo vuelo85 = new Vuelo(85, aerol1, aerop14, aerop19, formatDia.parse("11-11-2015 16:48"), formatHora.parse("08:50"), (float) 1216, 81);
            Vuelo vuelo86 = new Vuelo(86, aerol2, aerop15, aerop19, formatDia.parse("11-11-2015 18:43"), formatHora.parse("13:08"), (float) 1671, 89);
            Vuelo vuelo87 = new Vuelo(87, aerol3, aerop16, aerop19, formatDia.parse("13-11-2015 12:57"), formatHora.parse("01:36"), (float) 202, 72);
            Vuelo vuelo88 = new Vuelo(88, aerol4, aerop16, aerop19, formatDia.parse("12-11-2015 19:12"), formatHora.parse("01:36"), (float) 204, 21);
            Vuelo vuelo89 = new Vuelo(89, aerol5, aerop16, aerop19, formatDia.parse("14-11-2015 01:12"), formatHora.parse("01:36"), (float) 222, 80);
            Vuelo vuelo90 = new Vuelo(90, aerol6, aerop17, aerop19, formatDia.parse("15-11-2015 00:00"), formatHora.parse("01:40"), (float) 228, 76);
            Vuelo vuelo91 = new Vuelo(91, aerol7, aerop17, aerop19, formatDia.parse("10-11-2015 09:07"), formatHora.parse("01:40"), (float) 231, 69);
            Vuelo vuelo92 = new Vuelo(92, aerol8, aerop17, aerop19, formatDia.parse("10-11-2015 13:26"), formatHora.parse("01:40"), (float) 200, 72);
            Vuelo vuelo93 = new Vuelo(93, aerol9, aerop18, aerop19, formatDia.parse("11-11-2015 20:38"), formatHora.parse("00:31"), (float) 68, 29);
            Vuelo vuelo94 = new Vuelo(94, aerol10, aerop18, aerop19, formatDia.parse("11-11-2015 07:55"), formatHora.parse("00:31"), (float) 73, 13);
            Vuelo vuelo95 = new Vuelo(95, aerol11, aerop18, aerop19, formatDia.parse("12-11-2015 23:45"), formatHora.parse("00:31"), (float) 63, 71);
            Vuelo vuelo96 = new Vuelo(96, aerol12, aerop20, aerop19, formatDia.parse("12-11-2015 07:26"), formatHora.parse("01:17"), (float) 153, 3);
            Vuelo vuelo97 = new Vuelo(97, aerol1, aerop20, aerop19, formatDia.parse("11-11-2015 04:19"), formatHora.parse("01:17"), (float) 160, 37);
            Vuelo vuelo98 = new Vuelo(98, aerol2, aerop20, aerop19, formatDia.parse("10-11-2015 04:04"), formatHora.parse("01:17"), (float) 156, 95);
            Vuelo vuelo99 = new Vuelo(99, aerol3, aerop1, aerop4, formatDia.parse("13-11-2015 17:31"), formatHora.parse("02:36"), (float) 343, 62);
            Vuelo vuelo100 = new Vuelo(100, aerol4, aerop1, aerop4, formatDia.parse("12-11-2015 03:50"), formatHora.parse("02:36"), (float) 312, 35);
            Vuelo vuelo101 = new Vuelo(101, aerol5, aerop1, aerop4, formatDia.parse("10-11-2015 20:09"), formatHora.parse("02:36"), (float) 339, 15);
            Vuelo vuelo102 = new Vuelo(102, aerol6, aerop1, aerop4, formatDia.parse("15-11-2015 00:00"), formatHora.parse("02:36"), (float) 333, 98);
            Vuelo vuelo103 = new Vuelo(103, aerol7, aerop1, aerop4, formatDia.parse("14-11-2015 17:16"), formatHora.parse("02:36"), (float) 347, 66);
            Vuelo vuelo104 = new Vuelo(104, aerol8, aerop1, aerop4, formatDia.parse("12-11-2015 21:50"), formatHora.parse("02:36"), (float) 323, 64);
            Vuelo vuelo105 = new Vuelo(105, aerol9, aerop1, aerop4, formatDia.parse("13-11-2015 08:38"), formatHora.parse("02:36"), (float) 321, 89);
            Vuelo vuelo106 = new Vuelo(106, aerol10, aerop3, aerop4, formatDia.parse("10-11-2015 02:52"), formatHora.parse("01:19"), (float) 171, 13);
            Vuelo vuelo107 = new Vuelo(107, aerol11, aerop5, aerop4, formatDia.parse("14-11-2015 12:43"), formatHora.parse("00:20"), (float) 45, 7);
            Vuelo vuelo108 = new Vuelo(108, aerol12, aerop5, aerop4, formatDia.parse("13-11-2015 04:19"), formatHora.parse("00:20"), (float) 41, 95);
            Vuelo vuelo109 = new Vuelo(109, aerol1, aerop5, aerop4, formatDia.parse("13-11-2015 10:33"), formatHora.parse("00:20"), (float) 47, 41);
            Vuelo vuelo110 = new Vuelo(110, aerol2, aerop6, aerop4, formatDia.parse("12-11-2015 14:38"), formatHora.parse("01:43"), (float) 213, 25);
            Vuelo vuelo111 = new Vuelo(111, aerol3, aerop6, aerop4, formatDia.parse("14-11-2015 12:43"), formatHora.parse("01:43"), (float) 213, 92);
            Vuelo vuelo112 = new Vuelo(112, aerol4, aerop6, aerop4, formatDia.parse("14-11-2015 18:00"), formatHora.parse("01:43"), (float) 230, 15);
            Vuelo vuelo113 = new Vuelo(113, aerol5, aerop6, aerop4, formatDia.parse("14-11-2015 00:14"), formatHora.parse("01:43"), (float) 228, 73);
            Vuelo vuelo114 = new Vuelo(114, aerol6, aerop6, aerop4, formatDia.parse("11-11-2015 18:00"), formatHora.parse("01:43"), (float) 215, 64);
            Vuelo vuelo115 = new Vuelo(115, aerol7, aerop7, aerop4, formatDia.parse("14-11-2015 11:45"), formatHora.parse("01:37"), (float) 205, 19);
            Vuelo vuelo116 = new Vuelo(116, aerol8, aerop7, aerop4, formatDia.parse("11-11-2015 13:40"), formatHora.parse("01:37"), (float) 216, 38);
            Vuelo vuelo117 = new Vuelo(117, aerol9, aerop7, aerop4, formatDia.parse("10-11-2015 04:33"), formatHora.parse("01:37"), (float) 189, 37);
            Vuelo vuelo118 = new Vuelo(118, aerol10, aerop8, aerop4, formatDia.parse("10-11-2015 11:16"), formatHora.parse("07:06"), (float) 914, 58);
            Vuelo vuelo119 = new Vuelo(119, aerol11, aerop8, aerop4, formatDia.parse("13-11-2015 08:09"), formatHora.parse("07:06"), (float) 875, 23);
            Vuelo vuelo120 = new Vuelo(120, aerol12, aerop9, aerop4, formatDia.parse("12-11-2015 15:50"), formatHora.parse("04:46"), (float) 634, 11);
            Vuelo vuelo121 = new Vuelo(121, aerol1, aerop9, aerop4, formatDia.parse("11-11-2015 21:36"), formatHora.parse("04:46"), (float) 662, 81);
            Vuelo vuelo122 = new Vuelo(122, aerol2, aerop9, aerop4, formatDia.parse("14-11-2015 17:31"), formatHora.parse("04:46"), (float) 640, 87);
            Vuelo vuelo123 = new Vuelo(123, aerol3, aerop12, aerop4, formatDia.parse("13-11-2015 09:50"), formatHora.parse("07:46"), (float) 946, 12);
            Vuelo vuelo124 = new Vuelo(124, aerol4, aerop12, aerop4, formatDia.parse("10-11-2015 19:26"), formatHora.parse("07:46"), (float) 1050, 11);
            Vuelo vuelo125 = new Vuelo(125, aerol5, aerop13, aerop4, formatDia.parse("12-11-2015 05:02"), formatHora.parse("11:13"), (float) 1332, 78);
            Vuelo vuelo126 = new Vuelo(126, aerol6, aerop14, aerop4, formatDia.parse("11-11-2015 16:48"), formatHora.parse("12:56"), (float) 1513, 96);
            Vuelo vuelo127 = new Vuelo(127, aerol7, aerop14, aerop4, formatDia.parse("14-11-2015 11:45"), formatHora.parse("12:56"), (float) 1721, 74);
            Vuelo vuelo128 = new Vuelo(128, aerol8, aerop14, aerop4, formatDia.parse("14-11-2015 17:45"), formatHora.parse("12:56"), (float) 1509, 42);
            Vuelo vuelo129 = new Vuelo(129, aerol9, aerop15, aerop4, formatDia.parse("13-11-2015 10:04"), formatHora.parse("08:06"), (float) 929, 93);
            Vuelo vuelo130 = new Vuelo(130, aerol10, aerop16, aerop4, formatDia.parse("12-11-2015 19:12"), formatHora.parse("15:18"), (float) 1841, 83);
            Vuelo vuelo131 = new Vuelo(131, aerol11, aerop16, aerop4, formatDia.parse("14-11-2015 15:07"), formatHora.parse("15:18"), (float) 1849, 97);
            Vuelo vuelo132 = new Vuelo(132, aerol12, aerop16, aerop4, formatDia.parse("13-11-2015 10:33"), formatHora.parse("15:18"), (float) 2103, 39);
            Vuelo vuelo133 = new Vuelo(133, aerol1, aerop17, aerop4, formatDia.parse("12-11-2015 01:12"), formatHora.parse("16:55"), (float) 2173, 78);
            Vuelo vuelo134 = new Vuelo(134, aerol2, aerop17, aerop4, formatDia.parse("11-11-2015 03:36"), formatHora.parse("16:55"), (float) 2321, 47);
            Vuelo vuelo135 = new Vuelo(135, aerol3, aerop18, aerop4, formatDia.parse("14-11-2015 08:38"), formatHora.parse("16:53"), (float) 2038, 23);
            Vuelo vuelo136 = new Vuelo(136, aerol4, aerop19, aerop4, formatDia.parse("10-11-2015 01:55"), formatHora.parse("16:50"), (float) 1982, 19);
            Vuelo vuelo137 = new Vuelo(137, aerol5, aerop19, aerop4, formatDia.parse("14-11-2015 02:38"), formatHora.parse("16:50"), (float) 2335, 1);
            Vuelo vuelo138 = new Vuelo(138, aerol6, aerop1, aerop17, formatDia.parse("13-11-2015 03:21"), formatHora.parse("14:18"), (float) 1820, 29);
            Vuelo vuelo139 = new Vuelo(139, aerol7, aerop4, aerop17, formatDia.parse("13-11-2015 22:19"), formatHora.parse("16:55"), (float) 1947, 81);
            Vuelo vuelo140 = new Vuelo(140, aerol8, aerop4, aerop17, formatDia.parse("13-11-2015 01:40"), formatHora.parse("16:55"), (float) 2076, 52);
            Vuelo vuelo141 = new Vuelo(141, aerol9, aerop12, aerop17, formatDia.parse("11-11-2015 22:04"), formatHora.parse("12:38"), (float) 1514, 63);
            Vuelo vuelo142 = new Vuelo(142, aerol10, aerop14, aerop17, formatDia.parse("11-11-2015 22:04"), formatHora.parse("10:24"), (float) 1436, 2);
            Vuelo vuelo143 = new Vuelo(143, aerol11, aerop14, aerop17, formatDia.parse("13-11-2015 13:26"), formatHora.parse("10:24"), (float) 1335, 54);
            Vuelo vuelo144 = new Vuelo(144, aerol12, aerop14, aerop17, formatDia.parse("14-11-2015 19:55"), formatHora.parse("10:24"), (float) 1294, 32);
            Vuelo vuelo145 = new Vuelo(145, aerol1, aerop14, aerop17, formatDia.parse("12-11-2015 08:09"), formatHora.parse("10:24"), (float) 1441, 82);
            Vuelo vuelo146 = new Vuelo(146, aerol2, aerop14, aerop17, formatDia.parse("11-11-2015 05:16"), formatHora.parse("10:24"), (float) 1272, 92);
            Vuelo vuelo147 = new Vuelo(147, aerol3, aerop14, aerop17, formatDia.parse("11-11-2015 00:43"), formatHora.parse("10:24"), (float) 1384, 49);
            Vuelo vuelo148 = new Vuelo(148, aerol4, aerop14, aerop17, formatDia.parse("13-11-2015 04:19"), formatHora.parse("10:24"), (float) 1369, 36);
            Vuelo vuelo149 = new Vuelo(149, aerol5, aerop16, aerop17, formatDia.parse("14-11-2015 20:24"), formatHora.parse("02:01"), (float) 247, 98);
            Vuelo vuelo150 = new Vuelo(150, aerol6, aerop16, aerop17, formatDia.parse("10-11-2015 16:48"), formatHora.parse("02:01"), (float) 233, 62);
            Vuelo vuelo151 = new Vuelo(151, aerol7, aerop16, aerop17, formatDia.parse("11-11-2015 13:12"), formatHora.parse("02:01"), (float) 257, 59);
            Vuelo vuelo152 = new Vuelo(152, aerol8, aerop18, aerop17, formatDia.parse("12-11-2015 09:36"), formatHora.parse("02:11"), (float) 271, 97);
            Vuelo vuelo153 = new Vuelo(153, aerol9, aerop18, aerop17, formatDia.parse("12-11-2015 00:28"), formatHora.parse("02:11"), (float) 293, 89);
            Vuelo vuelo154 = new Vuelo(154, aerol10, aerop18, aerop17, formatDia.parse("10-11-2015 10:48"), formatHora.parse("02:11"), (float) 288, 71);
            Vuelo vuelo155 = new Vuelo(155, aerol11, aerop19, aerop17, formatDia.parse("11-11-2015 10:48"), formatHora.parse("01:40"), (float) 207, 47);
            Vuelo vuelo156 = new Vuelo(156, aerol12, aerop19, aerop17, formatDia.parse("11-11-2015 07:55"), formatHora.parse("01:40"), (float) 198, 5);
            Vuelo vuelo157 = new Vuelo(157, aerol1, aerop19, aerop17, formatDia.parse("10-11-2015 21:50"), formatHora.parse("01:40"), (float) 191, 84);
            Vuelo vuelo158 = new Vuelo(158, aerol2, aerop20, aerop17, formatDia.parse("14-11-2015 07:12"), formatHora.parse("01:49"), (float) 221, 55);
            Vuelo vuelo159 = new Vuelo(159, aerol3, aerop20, aerop17, formatDia.parse("14-11-2015 03:50"), formatHora.parse("01:49"), (float) 225, 5);
            Vuelo vuelo160 = new Vuelo(160, aerol4, aerop20, aerop17, formatDia.parse("10-11-2015 22:33"), formatHora.parse("01:49"), (float) 214, 0);
            Vuelo vuelo161 = new Vuelo(161, aerol5, aerop2, aerop1, formatDia.parse("14-11-2015 00:57"), formatHora.parse("00:31"), (float) 62, 42);
            Vuelo vuelo162 = new Vuelo(162, aerol6, aerop2, aerop1, formatDia.parse("14-11-2015 02:38"), formatHora.parse("00:31"), (float) 63, 10);
            Vuelo vuelo163 = new Vuelo(163, aerol7, aerop3, aerop1, formatDia.parse("14-11-2015 00:14"), formatHora.parse("01:18"), (float) 181, 90);
            Vuelo vuelo164 = new Vuelo(164, aerol8, aerop3, aerop1, formatDia.parse("13-11-2015 22:04"), formatHora.parse("01:18"), (float) 151, 83);
            Vuelo vuelo165 = new Vuelo(165, aerol9, aerop3, aerop1, formatDia.parse("11-11-2015 12:43"), formatHora.parse("01:18"), (float) 165, 97);
            Vuelo vuelo166 = new Vuelo(166, aerol10, aerop3, aerop1, formatDia.parse("13-11-2015 13:55"), formatHora.parse("01:18"), (float) 151, 44);
            Vuelo vuelo167 = new Vuelo(167, aerol11, aerop4, aerop1, formatDia.parse("10-11-2015 22:19"), formatHora.parse("02:36"), (float) 297, 39);
            Vuelo vuelo168 = new Vuelo(168, aerol12, aerop4, aerop1, formatDia.parse("13-11-2015 12:28"), formatHora.parse("02:36"), (float) 303, 30);
            Vuelo vuelo169 = new Vuelo(169, aerol1, aerop4, aerop1, formatDia.parse("14-11-2015 11:45"), formatHora.parse("02:36"), (float) 300, 53);
            Vuelo vuelo170 = new Vuelo(170, aerol2, aerop4, aerop1, formatDia.parse("10-11-2015 16:48"), formatHora.parse("02:36"), (float) 335, 45);
            Vuelo vuelo171 = new Vuelo(171, aerol3, aerop4, aerop1, formatDia.parse("12-11-2015 22:48"), formatHora.parse("02:36"), (float) 330, 4);
            Vuelo vuelo172 = new Vuelo(172, aerol4, aerop4, aerop1, formatDia.parse("14-11-2015 03:50"), formatHora.parse("02:36"), (float) 359, 70);
            Vuelo vuelo173 = new Vuelo(173, aerol5, aerop4, aerop1, formatDia.parse("12-11-2015 11:02"), formatHora.parse("02:36"), (float) 346, 10);
            Vuelo vuelo174 = new Vuelo(174, aerol6, aerop5, aerop1, formatDia.parse("13-11-2015 00:28"), formatHora.parse("02:22"), (float) 301, 61);
            Vuelo vuelo175 = new Vuelo(175, aerol7, aerop5, aerop1, formatDia.parse("12-11-2015 01:12"), formatHora.parse("02:22"), (float) 272, 63);
            Vuelo vuelo176 = new Vuelo(176, aerol8, aerop5, aerop1, formatDia.parse("13-11-2015 11:31"), formatHora.parse("02:22"), (float) 329, 18);
            Vuelo vuelo177 = new Vuelo(177, aerol9, aerop5, aerop1, formatDia.parse("14-11-2015 17:31"), formatHora.parse("02:22"), (float) 322, 5);
            Vuelo vuelo178 = new Vuelo(178, aerol10, aerop6, aerop1, formatDia.parse("11-11-2015 02:52"), formatHora.parse("03:57"), (float) 508, 65);
            Vuelo vuelo179 = new Vuelo(179, aerol11, aerop6, aerop1, formatDia.parse("14-11-2015 23:02"), formatHora.parse("03:57"), (float) 487, 76);
            Vuelo vuelo180 = new Vuelo(180, aerol12, aerop6, aerop1, formatDia.parse("13-11-2015 14:09"), formatHora.parse("03:57"), (float) 479, 80);
            Vuelo vuelo181 = new Vuelo(181, aerol1, aerop7, aerop1, formatDia.parse("10-11-2015 08:38"), formatHora.parse("01:43"), (float) 220, 86);
            Vuelo vuelo182 = new Vuelo(182, aerol2, aerop7, aerop1, formatDia.parse("11-11-2015 10:48"), formatHora.parse("01:43"), (float) 234, 24);
            Vuelo vuelo183 = new Vuelo(183, aerol3, aerop7, aerop1, formatDia.parse("12-11-2015 19:55"), formatHora.parse("01:43"), (float) 207, 71);
            Vuelo vuelo184 = new Vuelo(184, aerol4, aerop8, aerop1, formatDia.parse("14-11-2015 01:12"), formatHora.parse("06:34"), (float) 907, 11);
            Vuelo vuelo185 = new Vuelo(185, aerol5, aerop8, aerop1, formatDia.parse("12-11-2015 23:02"), formatHora.parse("06:34"), (float) 893, 96);
            Vuelo vuelo186 = new Vuelo(186, aerol6, aerop8, aerop1, formatDia.parse("10-11-2015 08:52"), formatHora.parse("06:34"), (float) 818, 67);
            Vuelo vuelo187 = new Vuelo(187, aerol7, aerop9, aerop1, formatDia.parse("11-11-2015 19:55"), formatHora.parse("05:16"), (float) 678, 7);
            Vuelo vuelo188 = new Vuelo(188, aerol8, aerop9, aerop1, formatDia.parse("11-11-2015 18:14"), formatHora.parse("05:16"), (float) 634, 50);
            Vuelo vuelo189 = new Vuelo(189, aerol9, aerop9, aerop1, formatDia.parse("11-11-2015 01:55"), formatHora.parse("05:16"), (float) 623, 90);
            Vuelo vuelo190 = new Vuelo(190, aerol10, aerop9, aerop1, formatDia.parse("15-11-2015 00:00"), formatHora.parse("05:16"), (float) 651, 45);
            Vuelo vuelo191 = new Vuelo(191, aerol11, aerop11, aerop1, formatDia.parse("13-11-2015 13:55"), formatHora.parse("06:33"), (float) 859, 34);
            Vuelo vuelo192 = new Vuelo(192, aerol12, aerop12, aerop1, formatDia.parse("13-11-2015 16:33"), formatHora.parse("06:39"), (float) 757, 76);
            Vuelo vuelo193 = new Vuelo(193, aerol1, aerop12, aerop1, formatDia.parse("12-11-2015 16:19"), formatHora.parse("06:39"), (float) 801, 53);
            Vuelo vuelo194 = new Vuelo(194, aerol2, aerop13, aerop1, formatDia.parse("12-11-2015 19:40"), formatHora.parse("11:16"), (float) 1351, 54);
            Vuelo vuelo195 = new Vuelo(195, aerol3, aerop13, aerop1, formatDia.parse("11-11-2015 00:28"), formatHora.parse("11:16"), (float) 1350, 80);
            Vuelo vuelo196 = new Vuelo(196, aerol4, aerop14, aerop1, formatDia.parse("12-11-2015 20:38"), formatHora.parse("11:37"), (float) 1336, 37);
            Vuelo vuelo197 = new Vuelo(197, aerol5, aerop14, aerop1, formatDia.parse("11-11-2015 23:16"), formatHora.parse("11:37"), (float) 1508, 27);
            Vuelo vuelo198 = new Vuelo(198, aerol6, aerop14, aerop1, formatDia.parse("10-11-2015 10:04"), formatHora.parse("11:37"), (float) 1576, 40);
            Vuelo vuelo199 = new Vuelo(199, aerol7, aerop14, aerop1, formatDia.parse("10-11-2015 07:12"), formatHora.parse("11:37"), (float) 1604, 85);
            Vuelo vuelo200 = new Vuelo(200, aerol8, aerop14, aerop1, formatDia.parse("13-11-2015 20:09"), formatHora.parse("11:37"), (float) 1383, 90);
            Vuelo vuelo201 = new Vuelo(201, aerol9, aerop15, aerop1, formatDia.parse("11-11-2015 18:43"), formatHora.parse("07:42"), (float) 887, 90);
            Vuelo vuelo202 = new Vuelo(202, aerol10, aerop16, aerop1, formatDia.parse("13-11-2015 21:07"), formatHora.parse("12:42"), (float) 1702, 94);
            Vuelo vuelo203 = new Vuelo(203, aerol11, aerop16, aerop1, formatDia.parse("12-11-2015 07:40"), formatHora.parse("12:42"), (float) 1582, 76);
            Vuelo vuelo204 = new Vuelo(204, aerol12, aerop16, aerop1, formatDia.parse("14-11-2015 13:26"), formatHora.parse("12:42"), (float) 1705, 95);
            Vuelo vuelo205 = new Vuelo(205, aerol1, aerop16, aerop1, formatDia.parse("11-11-2015 11:16"), formatHora.parse("12:42"), (float) 1691, 49);
            Vuelo vuelo206 = new Vuelo(206, aerol2, aerop16, aerop1, formatDia.parse("10-11-2015 06:00"), formatHora.parse("12:42"), (float) 1499, 46);
            Vuelo vuelo207 = new Vuelo(207, aerol3, aerop17, aerop1, formatDia.parse("14-11-2015 03:50"), formatHora.parse("14:18"), (float) 1749, 23);
            Vuelo vuelo208 = new Vuelo(208, aerol4, aerop18, aerop1, formatDia.parse("12-11-2015 00:00"), formatHora.parse("14:20"), (float) 1871, 5);
            Vuelo vuelo209 = new Vuelo(209, aerol5, aerop18, aerop1, formatDia.parse("13-11-2015 15:50"), formatHora.parse("14:20"), (float) 1987, 37);
            Vuelo vuelo210 = new Vuelo(210, aerol6, aerop19, aerop1, formatDia.parse("10-11-2015 02:52"), formatHora.parse("14:15"), (float) 1969, 65);
            Vuelo vuelo211 = new Vuelo(211, aerol7, aerop19, aerop1, formatDia.parse("11-11-2015 00:00"), formatHora.parse("14:15"), (float) 1889, 19);
            Vuelo vuelo212 = new Vuelo(212, aerol8, aerop1, aerop14, formatDia.parse("14-11-2015 17:16"), formatHora.parse("11:37"), (float) 1612, 85);
            Vuelo vuelo213 = new Vuelo(213, aerol9, aerop1, aerop14, formatDia.parse("14-11-2015 08:52"), formatHora.parse("11:37"), (float) 1474, 27);
            Vuelo vuelo214 = new Vuelo(214, aerol10, aerop1, aerop14, formatDia.parse("13-11-2015 16:48"), formatHora.parse("11:37"), (float) 1416, 23);
            Vuelo vuelo215 = new Vuelo(215, aerol11, aerop1, aerop14, formatDia.parse("10-11-2015 01:26"), formatHora.parse("11:37"), (float) 1467, 71);
            Vuelo vuelo216 = new Vuelo(216, aerol12, aerop1, aerop14, formatDia.parse("11-11-2015 10:33"), formatHora.parse("11:37"), (float) 1527, 40);
            Vuelo vuelo217 = new Vuelo(217, aerol1, aerop4, aerop14, formatDia.parse("14-11-2015 13:26"), formatHora.parse("12:56"), (float) 1653, 5);
            Vuelo vuelo218 = new Vuelo(218, aerol2, aerop4, aerop14, formatDia.parse("12-11-2015 06:00"), formatHora.parse("12:56"), (float) 1482, 82);
            Vuelo vuelo219 = new Vuelo(219, aerol3, aerop4, aerop14, formatDia.parse("13-11-2015 22:48"), formatHora.parse("12:56"), (float) 1704, 86);
            Vuelo vuelo220 = new Vuelo(220, aerol4, aerop6, aerop14, formatDia.parse("11-11-2015 07:55"), formatHora.parse("12:29"), (float) 1538, 79);
            Vuelo vuelo221 = new Vuelo(221, aerol5, aerop6, aerop14, formatDia.parse("11-11-2015 07:40"), formatHora.parse("12:29"), (float) 1512, 86);
            Vuelo vuelo222 = new Vuelo(222, aerol6, aerop8, aerop14, formatDia.parse("12-11-2015 16:33"), formatHora.parse("06:03"), (float) 702, 42);
            Vuelo vuelo223 = new Vuelo(223, aerol7, aerop8, aerop14, formatDia.parse("13-11-2015 02:52"), formatHora.parse("06:03"), (float) 819, 7);
            Vuelo vuelo224 = new Vuelo(224, aerol8, aerop8, aerop14, formatDia.parse("12-11-2015 18:28"), formatHora.parse("06:03"), (float) 775, 46);
            Vuelo vuelo225 = new Vuelo(225, aerol9, aerop8, aerop14, formatDia.parse("10-11-2015 11:45"), formatHora.parse("06:03"), (float) 715, 81);
            Vuelo vuelo226 = new Vuelo(226, aerol10, aerop9, aerop14, formatDia.parse("12-11-2015 10:48"), formatHora.parse("08:53"), (float) 1056, 80);
            Vuelo vuelo227 = new Vuelo(227, aerol11, aerop9, aerop14, formatDia.parse("12-11-2015 17:02"), formatHora.parse("08:53"), (float) 1090, 23);
            Vuelo vuelo228 = new Vuelo(228, aerol12, aerop9, aerop14, formatDia.parse("13-11-2015 06:00"), formatHora.parse("08:53"), (float) 1231, 22);
            Vuelo vuelo229 = new Vuelo(229, aerol1, aerop12, aerop14, formatDia.parse("11-11-2015 05:02"), formatHora.parse("05:09"), (float) 698, 8);
            Vuelo vuelo230 = new Vuelo(230, aerol2, aerop13, aerop14, formatDia.parse("13-11-2015 14:38"), formatHora.parse("05:06"), (float) 618, 76);
            Vuelo vuelo231 = new Vuelo(231, aerol3, aerop13, aerop14, formatDia.parse("10-11-2015 20:38"), formatHora.parse("05:06"), (float) 601, 80);
            Vuelo vuelo232 = new Vuelo(232, aerol4, aerop13, aerop14, formatDia.parse("12-11-2015 04:33"), formatHora.parse("05:06"), (float) 640, 57);
            Vuelo vuelo233 = new Vuelo(233, aerol5, aerop15, aerop14, formatDia.parse("14-11-2015 07:12"), formatHora.parse("05:23"), (float) 651, 59);
            Vuelo vuelo234 = new Vuelo(234, aerol6, aerop15, aerop14, formatDia.parse("12-11-2015 10:04"), formatHora.parse("05:23"), (float) 738, 72);
            Vuelo vuelo235 = new Vuelo(235, aerol7, aerop16, aerop14, formatDia.parse("13-11-2015 10:33"), formatHora.parse("08:44"), (float) 1131, 89);
            Vuelo vuelo236 = new Vuelo(236, aerol8, aerop16, aerop14, formatDia.parse("13-11-2015 21:50"), formatHora.parse("08:44"), (float) 1148, 84);
            Vuelo vuelo237 = new Vuelo(237, aerol9, aerop16, aerop14, formatDia.parse("13-11-2015 08:09"), formatHora.parse("08:44"), (float) 1157, 47);
            Vuelo vuelo238 = new Vuelo(238, aerol10, aerop16, aerop14, formatDia.parse("10-11-2015 01:40"), formatHora.parse("08:44"), (float) 1125, 46);
            Vuelo vuelo239 = new Vuelo(239, aerol11, aerop16, aerop14, formatDia.parse("11-11-2015 17:31"), formatHora.parse("08:44"), (float) 1016, 20);
            Vuelo vuelo240 = new Vuelo(240, aerol12, aerop16, aerop14, formatDia.parse("11-11-2015 02:09"), formatHora.parse("08:44"), (float) 1107, 60);
            Vuelo vuelo241 = new Vuelo(241, aerol1, aerop16, aerop14, formatDia.parse("12-11-2015 13:40"), formatHora.parse("08:44"), (float) 1183, 17);
            Vuelo vuelo242 = new Vuelo(242, aerol2, aerop16, aerop14, formatDia.parse("13-11-2015 13:12"), formatHora.parse("08:44"), (float) 994, 78);
            Vuelo vuelo243 = new Vuelo(243, aerol3, aerop16, aerop14, formatDia.parse("12-11-2015 00:43"), formatHora.parse("08:44"), (float) 1087, 4);
            Vuelo vuelo244 = new Vuelo(244, aerol4, aerop17, aerop14, formatDia.parse("11-11-2015 00:57"), formatHora.parse("10:24"), (float) 1388, 59);
            Vuelo vuelo245 = new Vuelo(245, aerol5, aerop17, aerop14, formatDia.parse("10-11-2015 08:09"), formatHora.parse("10:24"), (float) 1229, 55);
            Vuelo vuelo246 = new Vuelo(246, aerol6, aerop17, aerop14, formatDia.parse("13-11-2015 01:12"), formatHora.parse("10:24"), (float) 1353, 83);
            Vuelo vuelo247 = new Vuelo(247, aerol7, aerop17, aerop14, formatDia.parse("10-11-2015 08:09"), formatHora.parse("10:24"), (float) 1347, 17);
            Vuelo vuelo248 = new Vuelo(248, aerol8, aerop17, aerop14, formatDia.parse("10-11-2015 20:38"), formatHora.parse("10:24"), (float) 1228, 44);
            Vuelo vuelo249 = new Vuelo(249, aerol9, aerop17, aerop14, formatDia.parse("10-11-2015 13:55"), formatHora.parse("10:24"), (float) 1276, 33);
            Vuelo vuelo250 = new Vuelo(250, aerol10, aerop17, aerop14, formatDia.parse("11-11-2015 06:00"), formatHora.parse("10:24"), (float) 1322, 61);
            Vuelo vuelo251 = new Vuelo(251, aerol11, aerop18, aerop14, formatDia.parse("11-11-2015 17:45"), formatHora.parse("08:24"), (float) 1016, 83);
            Vuelo vuelo252 = new Vuelo(252, aerol12, aerop18, aerop14, formatDia.parse("10-11-2015 20:09"), formatHora.parse("08:24"), (float) 1013, 41);
            Vuelo vuelo253 = new Vuelo(253, aerol1, aerop18, aerop14, formatDia.parse("12-11-2015 03:50"), formatHora.parse("08:24"), (float) 1146, 92);
            Vuelo vuelo254 = new Vuelo(254, aerol2, aerop18, aerop14, formatDia.parse("10-11-2015 18:14"), formatHora.parse("08:24"), (float) 1134, 75);
            Vuelo vuelo255 = new Vuelo(255, aerol3, aerop18, aerop14, formatDia.parse("10-11-2015 17:16"), formatHora.parse("08:24"), (float) 1146, 36);
            Vuelo vuelo256 = new Vuelo(256, aerol4, aerop18, aerop14, formatDia.parse("13-11-2015 00:57"), formatHora.parse("08:24"), (float) 984, 65);
            Vuelo vuelo257 = new Vuelo(257, aerol5, aerop18, aerop14, formatDia.parse("14-11-2015 14:38"), formatHora.parse("08:24"), (float) 1038, 63);
            Vuelo vuelo258 = new Vuelo(258, aerol6, aerop18, aerop14, formatDia.parse("14-11-2015 13:12"), formatHora.parse("08:24"), (float) 1111, 68);
            Vuelo vuelo259 = new Vuelo(259, aerol7, aerop18, aerop14, formatDia.parse("11-11-2015 14:24"), formatHora.parse("08:24"), (float) 1099, 21);
            Vuelo vuelo260 = new Vuelo(260, aerol8, aerop18, aerop14, formatDia.parse("11-11-2015 00:43"), formatHora.parse("08:24"), (float) 1100, 0);
            Vuelo vuelo261 = new Vuelo(261, aerol9, aerop18, aerop14, formatDia.parse("12-11-2015 22:48"), formatHora.parse("08:24"), (float) 1043, 50);
            Vuelo vuelo262 = new Vuelo(262, aerol10, aerop18, aerop14, formatDia.parse("10-11-2015 22:19"), formatHora.parse("08:24"), (float) 1093, 62);
            Vuelo vuelo263 = new Vuelo(263, aerol11, aerop19, aerop14, formatDia.parse("13-11-2015 21:50"), formatHora.parse("08:50"), (float) 1228, 12);
            Vuelo vuelo264 = new Vuelo(264, aerol12, aerop19, aerop14, formatDia.parse("10-11-2015 01:55"), formatHora.parse("08:50"), (float) 1212, 84);
            Vuelo vuelo265 = new Vuelo(265, aerol1, aerop19, aerop14, formatDia.parse("14-11-2015 20:09"), formatHora.parse("08:50"), (float) 1033, 92);
            Vuelo vuelo266 = new Vuelo(266, aerol2, aerop19, aerop14, formatDia.parse("10-11-2015 23:45"), formatHora.parse("08:50"), (float) 1150, 28);
            Vuelo vuelo267 = new Vuelo(267, aerol3, aerop19, aerop14, formatDia.parse("14-11-2015 04:19"), formatHora.parse("08:50"), (float) 1130, 39);
            Vuelo vuelo268 = new Vuelo(268, aerol4, aerop19, aerop14, formatDia.parse("12-11-2015 12:00"), formatHora.parse("08:50"), (float) 1091, 34);
            Vuelo vuelo269 = new Vuelo(269, aerol5, aerop19, aerop14, formatDia.parse("10-11-2015 18:28"), formatHora.parse("08:50"), (float) 1122, 68);
            Vuelo vuelo270 = new Vuelo(270, aerol6, aerop19, aerop14, formatDia.parse("11-11-2015 06:00"), formatHora.parse("08:50"), (float) 1159, 52);
            Vuelo vuelo271 = new Vuelo(271, aerol7, aerop19, aerop14, formatDia.parse("12-11-2015 18:14"), formatHora.parse("08:50"), (float) 1026, 58);
            Vuelo vuelo272 = new Vuelo(272, aerol8, aerop19, aerop14, formatDia.parse("13-11-2015 13:55"), formatHora.parse("08:50"), (float) 1028, 88);
            Vuelo vuelo273 = new Vuelo(273, aerol9, aerop19, aerop14, formatDia.parse("11-11-2015 07:26"), formatHora.parse("08:50"), (float) 1035, 63);
            Vuelo vuelo274 = new Vuelo(274, aerol10, aerop20, aerop14, formatDia.parse("10-11-2015 14:38"), formatHora.parse("09:39"), (float) 1294, 96);
            Vuelo vuelo275 = new Vuelo(275, aerol11, aerop20, aerop14, formatDia.parse("12-11-2015 10:33"), formatHora.parse("09:39"), (float) 1118, 5);
            Vuelo vuelo276 = new Vuelo(276, aerol12, aerop1, aerop18, formatDia.parse("13-11-2015 21:21"), formatHora.parse("14:20"), (float) 1767, 22);
            Vuelo vuelo277 = new Vuelo(277, aerol1, aerop1, aerop18, formatDia.parse("10-11-2015 15:50"), formatHora.parse("14:20"), (float) 1890, 42);
            Vuelo vuelo278 = new Vuelo(278, aerol2, aerop4, aerop18, formatDia.parse("14-11-2015 00:00"), formatHora.parse("16:53"), (float) 2094, 49);
            Vuelo vuelo279 = new Vuelo(279, aerol3, aerop13, aerop18, formatDia.parse("12-11-2015 15:50"), formatHora.parse("13:30"), (float) 1600, 80);
            Vuelo vuelo280 = new Vuelo(280, aerol4, aerop13, aerop18, formatDia.parse("10-11-2015 08:24"), formatHora.parse("13:30"), (float) 1568, 98);
            Vuelo vuelo281 = new Vuelo(281, aerol5, aerop14, aerop18, formatDia.parse("11-11-2015 01:40"), formatHora.parse("08:24"), (float) 1007, 96);
            Vuelo vuelo282 = new Vuelo(282, aerol6, aerop14, aerop18, formatDia.parse("14-11-2015 03:07"), formatHora.parse("08:24"), (float) 1042, 11);
            Vuelo vuelo283 = new Vuelo(283, aerol7, aerop14, aerop18, formatDia.parse("12-11-2015 05:16"), formatHora.parse("08:24"), (float) 1058, 73);
            Vuelo vuelo284 = new Vuelo(284, aerol8, aerop14, aerop18, formatDia.parse("10-11-2015 16:19"), formatHora.parse("08:24"), (float) 1055, 58);
            Vuelo vuelo285 = new Vuelo(285, aerol9, aerop14, aerop18, formatDia.parse("14-11-2015 03:50"), formatHora.parse("08:24"), (float) 1087, 39);
            Vuelo vuelo286 = new Vuelo(286, aerol10, aerop14, aerop18, formatDia.parse("10-11-2015 21:07"), formatHora.parse("08:24"), (float) 1034, 30);
            Vuelo vuelo287 = new Vuelo(287, aerol11, aerop14, aerop18, formatDia.parse("14-11-2015 18:57"), formatHora.parse("08:24"), (float) 1092, 44);
            Vuelo vuelo288 = new Vuelo(288, aerol12, aerop14, aerop18, formatDia.parse("12-11-2015 08:38"), formatHora.parse("08:24"), (float) 986, 14);
            Vuelo vuelo289 = new Vuelo(289, aerol1, aerop14, aerop18, formatDia.parse("14-11-2015 05:02"), formatHora.parse("08:24"), (float) 989, 21);
            Vuelo vuelo290 = new Vuelo(290, aerol2, aerop14, aerop18, formatDia.parse("10-11-2015 03:50"), formatHora.parse("08:24"), (float) 1032, 12);
            Vuelo vuelo291 = new Vuelo(291, aerol3, aerop14, aerop18, formatDia.parse("11-11-2015 03:21"), formatHora.parse("08:24"), (float) 1067, 71);
            Vuelo vuelo292 = new Vuelo(292, aerol4, aerop14, aerop18, formatDia.parse("11-11-2015 07:40"), formatHora.parse("08:24"), (float) 1039, 52);
            Vuelo vuelo293 = new Vuelo(293, aerol5, aerop16, aerop18, formatDia.parse("12-11-2015 04:48"), formatHora.parse("01:53"), (float) 244, 20);
            Vuelo vuelo294 = new Vuelo(294, aerol6, aerop16, aerop18, formatDia.parse("13-11-2015 16:19"), formatHora.parse("01:53"), (float) 260, 3);
            Vuelo vuelo295 = new Vuelo(295, aerol7, aerop17, aerop18, formatDia.parse("14-11-2015 11:31"), formatHora.parse("02:11"), (float) 291, 65);
            Vuelo vuelo296 = new Vuelo(296, aerol8, aerop17, aerop18, formatDia.parse("13-11-2015 18:43"), formatHora.parse("02:11"), (float) 283, 32);
            Vuelo vuelo297 = new Vuelo(297, aerol9, aerop17, aerop18, formatDia.parse("10-11-2015 19:55"), formatHora.parse("02:11"), (float) 249, 95);
            Vuelo vuelo298 = new Vuelo(298, aerol10, aerop19, aerop18, formatDia.parse("14-11-2015 22:04"), formatHora.parse("00:31"), (float) 69, 92);
            Vuelo vuelo299 = new Vuelo(299, aerol11, aerop19, aerop18, formatDia.parse("12-11-2015 04:19"), formatHora.parse("00:31"), (float) 72, 66);
            Vuelo vuelo300 = new Vuelo(300, aerol12, aerop19, aerop18, formatDia.parse("13-11-2015 17:45"), formatHora.parse("00:31"), (float) 61, 95);
            Vuelo vuelo301 = new Vuelo(301, aerol1, aerop20, aerop18, formatDia.parse("10-11-2015 05:31"), formatHora.parse("01:26"), (float) 196, 14);
            Vuelo vuelo302 = new Vuelo(302, aerol2, aerop20, aerop18, formatDia.parse("11-11-2015 21:21"), formatHora.parse("01:26"), (float) 178, 78);
            Vuelo vuelo303 = new Vuelo(303, aerol3, aerop20, aerop18, formatDia.parse("10-11-2015 17:02"), formatHora.parse("01:26"), (float) 187, 46);
            Vuelo vuelo304 = new Vuelo(304, aerol4, aerop20, aerop18, formatDia.parse("14-11-2015 06:57"), formatHora.parse("01:26"), (float) 189, 80);
            Vuelo vuelo305 = new Vuelo(305, aerol5, aerop1, aerop9, formatDia.parse("12-11-2015 10:48"), formatHora.parse("05:16"), (float) 619, 67);
            Vuelo vuelo306 = new Vuelo(306, aerol6, aerop1, aerop9, formatDia.parse("14-11-2015 15:07"), formatHora.parse("05:16"), (float) 660, 6);
            Vuelo vuelo307 = new Vuelo(307, aerol7, aerop1, aerop9, formatDia.parse("14-11-2015 11:02"), formatHora.parse("05:16"), (float) 614, 34);
            Vuelo vuelo308 = new Vuelo(308, aerol8, aerop1, aerop9, formatDia.parse("12-11-2015 11:45"), formatHora.parse("05:16"), (float) 731, 58);
            Vuelo vuelo309 = new Vuelo(309, aerol9, aerop3, aerop9, formatDia.parse("11-11-2015 23:45"), formatHora.parse("05:03"), (float) 603, 8);
            Vuelo vuelo310 = new Vuelo(310, aerol10, aerop3, aerop9, formatDia.parse("13-11-2015 12:43"), formatHora.parse("05:03"), (float) 686, 52);
            Vuelo vuelo311 = new Vuelo(311, aerol11, aerop4, aerop9, formatDia.parse("13-11-2015 06:14"), formatHora.parse("04:46"), (float) 624, 94);
            Vuelo vuelo312 = new Vuelo(312, aerol12, aerop4, aerop9, formatDia.parse("13-11-2015 10:19"), formatHora.parse("04:46"), (float) 640, 7);
            Vuelo vuelo313 = new Vuelo(313, aerol1, aerop4, aerop9, formatDia.parse("11-11-2015 18:28"), formatHora.parse("04:46"), (float) 621, 1);
            Vuelo vuelo314 = new Vuelo(314, aerol2, aerop5, aerop9, formatDia.parse("10-11-2015 18:57"), formatHora.parse("05:01"), (float) 685, 5);
            Vuelo vuelo315 = new Vuelo(315, aerol3, aerop6, aerop9, formatDia.parse("11-11-2015 22:04"), formatHora.parse("03:44"), (float) 462, 17);
            Vuelo vuelo316 = new Vuelo(316, aerol4, aerop6, aerop9, formatDia.parse("11-11-2015 16:33"), formatHora.parse("03:44"), (float) 462, 48);
            Vuelo vuelo317 = new Vuelo(317, aerol5, aerop6, aerop9, formatDia.parse("11-11-2015 19:55"), formatHora.parse("03:44"), (float) 517, 96);
            Vuelo vuelo318 = new Vuelo(318, aerol6, aerop6, aerop9, formatDia.parse("12-11-2015 21:21"), formatHora.parse("03:44"), (float) 441, 84);
            Vuelo vuelo319 = new Vuelo(319, aerol7, aerop6, aerop9, formatDia.parse("11-11-2015 19:55"), formatHora.parse("03:44"), (float) 458, 77);
            Vuelo vuelo320 = new Vuelo(320, aerol8, aerop6, aerop9, formatDia.parse("12-11-2015 12:28"), formatHora.parse("03:44"), (float) 459, 9);
            Vuelo vuelo321 = new Vuelo(321, aerol9, aerop7, aerop9, formatDia.parse("11-11-2015 21:07"), formatHora.parse("03:50"), (float) 478, 0);
            Vuelo vuelo322 = new Vuelo(322, aerol10, aerop8, aerop9, formatDia.parse("10-11-2015 20:09"), formatHora.parse("02:51"), (float) 337, 66);
            Vuelo vuelo323 = new Vuelo(323, aerol11, aerop8, aerop9, formatDia.parse("10-11-2015 01:12"), formatHora.parse("02:51"), (float) 352, 83);
            Vuelo vuelo324 = new Vuelo(324, aerol12, aerop10, aerop9, formatDia.parse("13-11-2015 04:19"), formatHora.parse("01:38"), (float) 195, 40);
            Vuelo vuelo325 = new Vuelo(325, aerol1, aerop10, aerop9, formatDia.parse("11-11-2015 20:24"), formatHora.parse("01:38"), (float) 212, 1);
            Vuelo vuelo326 = new Vuelo(326, aerol2, aerop10, aerop9, formatDia.parse("10-11-2015 08:52"), formatHora.parse("01:38"), (float) 207, 64);
            Vuelo vuelo327 = new Vuelo(327, aerol3, aerop11, aerop9, formatDia.parse("14-11-2015 10:19"), formatHora.parse("02:01"), (float) 240, 53);
            Vuelo vuelo328 = new Vuelo(328, aerol4, aerop11, aerop9, formatDia.parse("12-11-2015 08:52"), formatHora.parse("02:01"), (float) 256, 30);
            Vuelo vuelo329 = new Vuelo(329, aerol5, aerop11, aerop9, formatDia.parse("10-11-2015 00:57"), formatHora.parse("02:01"), (float) 252, 66);
            Vuelo vuelo330 = new Vuelo(330, aerol6, aerop12, aerop9, formatDia.parse("12-11-2015 05:45"), formatHora.parse("04:10"), (float) 526, 63);
            Vuelo vuelo331 = new Vuelo(331, aerol7, aerop12, aerop9, formatDia.parse("11-11-2015 12:57"), formatHora.parse("04:10"), (float) 557, 81);
            Vuelo vuelo332 = new Vuelo(332, aerol8, aerop13, aerop9, formatDia.parse("13-11-2015 04:04"), formatHora.parse("06:26"), (float) 836, 0);
            Vuelo vuelo333 = new Vuelo(333, aerol9, aerop13, aerop9, formatDia.parse("11-11-2015 15:07"), formatHora.parse("06:26"), (float) 760, 99);
            Vuelo vuelo334 = new Vuelo(334, aerol10, aerop13, aerop9, formatDia.parse("12-11-2015 11:45"), formatHora.parse("06:26"), (float) 747, 15);
            Vuelo vuelo335 = new Vuelo(335, aerol11, aerop14, aerop9, formatDia.parse("12-11-2015 03:50"), formatHora.parse("08:53"), (float) 1059, 22);
            Vuelo vuelo336 = new Vuelo(336, aerol12, aerop14, aerop9, formatDia.parse("13-11-2015 05:02"), formatHora.parse("08:53"), (float) 1197, 33);
            Vuelo vuelo337 = new Vuelo(337, aerol1, aerop14, aerop9, formatDia.parse("11-11-2015 08:24"), formatHora.parse("08:53"), (float) 1181, 7);
            Vuelo vuelo338 = new Vuelo(338, aerol2, aerop15, aerop9, formatDia.parse("14-11-2015 23:31"), formatHora.parse("03:34"), (float) 431, 14);
            Vuelo vuelo339 = new Vuelo(339, aerol3, aerop16, aerop9, formatDia.parse("13-11-2015 08:38"), formatHora.parse("14:27"), (float) 1839, 11);
            Vuelo vuelo340 = new Vuelo(340, aerol4, aerop16, aerop9, formatDia.parse("14-11-2015 20:09"), formatHora.parse("14:27"), (float) 1900, 79);
            Vuelo vuelo341 = new Vuelo(341, aerol5, aerop16, aerop9, formatDia.parse("11-11-2015 16:48"), formatHora.parse("14:27"), (float) 1719, 43);
            Vuelo vuelo342 = new Vuelo(342, aerol6, aerop19, aerop9, formatDia.parse("10-11-2015 03:36"), formatHora.parse("15:35"), (float) 1775, 39);
            Vuelo vuelo343 = new Vuelo(343, aerol7, aerop19, aerop9, formatDia.parse("10-11-2015 23:02"), formatHora.parse("15:35"), (float) 2055, 54);
            Vuelo vuelo344 = new Vuelo(344, aerol8, aerop8, aerop10, formatDia.parse("11-11-2015 00:00"), formatHora.parse("03:42"), (float) 507, 81);
            Vuelo vuelo345 = new Vuelo(345, aerol9, aerop9, aerop10, formatDia.parse("11-11-2015 20:09"), formatHora.parse("01:38"), (float) 193, 10);
            Vuelo vuelo346 = new Vuelo(346, aerol10, aerop9, aerop10, formatDia.parse("12-11-2015 12:57"), formatHora.parse("01:38"), (float) 188, 30);
            Vuelo vuelo347 = new Vuelo(347, aerol11, aerop9, aerop10, formatDia.parse("11-11-2015 08:38"), formatHora.parse("01:38"), (float) 209, 35);
            Vuelo vuelo348 = new Vuelo(348, aerol12, aerop1, aerop16, formatDia.parse("12-11-2015 18:57"), formatHora.parse("12:42"), (float) 1556, 22);
            Vuelo vuelo349 = new Vuelo(349, aerol1, aerop1, aerop16, formatDia.parse("12-11-2015 09:07"), formatHora.parse("12:42"), (float) 1646, 50);
            Vuelo vuelo350 = new Vuelo(350, aerol2, aerop1, aerop16, formatDia.parse("12-11-2015 09:50"), formatHora.parse("12:42"), (float) 1513, 42);
            Vuelo vuelo351 = new Vuelo(351, aerol3, aerop1, aerop16, formatDia.parse("13-11-2015 01:12"), formatHora.parse("12:42"), (float) 1601, 92);
            Vuelo vuelo352 = new Vuelo(352, aerol4, aerop1, aerop16, formatDia.parse("13-11-2015 16:19"), formatHora.parse("12:42"), (float) 1712, 23);
            Vuelo vuelo353 = new Vuelo(353, aerol5, aerop4, aerop16, formatDia.parse("13-11-2015 18:14"), formatHora.parse("15:18"), (float) 1764, 70);
            Vuelo vuelo354 = new Vuelo(354, aerol6, aerop4, aerop16, formatDia.parse("14-11-2015 01:55"), formatHora.parse("15:18"), (float) 1826, 53);
            Vuelo vuelo355 = new Vuelo(355, aerol7, aerop4, aerop16, formatDia.parse("13-11-2015 03:07"), formatHora.parse("15:18"), (float) 2124, 58);
            Vuelo vuelo356 = new Vuelo(356, aerol8, aerop5, aerop16, formatDia.parse("11-11-2015 17:45"), formatHora.parse("15:05"), (float) 2041, 87);
            Vuelo vuelo357 = new Vuelo(357, aerol9, aerop6, aerop16, formatDia.parse("13-11-2015 15:36"), formatHora.parse("16:15"), (float) 2215, 52);
            Vuelo vuelo358 = new Vuelo(358, aerol10, aerop6, aerop16, formatDia.parse("14-11-2015 05:02"), formatHora.parse("16:15"), (float) 2064, 48);
            Vuelo vuelo359 = new Vuelo(359, aerol11, aerop8, aerop16, formatDia.parse("14-11-2015 23:31"), formatHora.parse("12:10"), (float) 1557, 1);
            Vuelo vuelo360 = new Vuelo(360, aerol12, aerop8, aerop16, formatDia.parse("13-11-2015 15:36"), formatHora.parse("12:10"), (float) 1500, 12);
            Vuelo vuelo361 = new Vuelo(361, aerol1, aerop9, aerop16, formatDia.parse("10-11-2015 07:55"), formatHora.parse("14:27"), (float) 1923, 17);
            Vuelo vuelo362 = new Vuelo(362, aerol2, aerop9, aerop16, formatDia.parse("13-11-2015 16:33"), formatHora.parse("14:27"), (float) 1779, 69);
            Vuelo vuelo363 = new Vuelo(363, aerol3, aerop9, aerop16, formatDia.parse("10-11-2015 11:45"), formatHora.parse("14:27"), (float) 1847, 45);
            Vuelo vuelo364 = new Vuelo(364, aerol4, aerop11, aerop16, formatDia.parse("14-11-2015 13:55"), formatHora.parse("13:16"), (float) 1654, 21);
            Vuelo vuelo365 = new Vuelo(365, aerol5, aerop11, aerop16, formatDia.parse("10-11-2015 00:28"), formatHora.parse("13:16"), (float) 1613, 78);
            Vuelo vuelo366 = new Vuelo(366, aerol6, aerop12, aerop16, formatDia.parse("10-11-2015 09:36"), formatHora.parse("10:37"), (float) 1227, 61);
            Vuelo vuelo367 = new Vuelo(367, aerol7, aerop12, aerop16, formatDia.parse("11-11-2015 00:00"), formatHora.parse("10:37"), (float) 1311, 45);
            Vuelo vuelo368 = new Vuelo(368, aerol8, aerop12, aerop16, formatDia.parse("12-11-2015 12:43"), formatHora.parse("10:37"), (float) 1352, 34);
            Vuelo vuelo369 = new Vuelo(369, aerol9, aerop13, aerop16, formatDia.parse("14-11-2015 00:14"), formatHora.parse("13:45"), (float) 1589, 90);
            Vuelo vuelo370 = new Vuelo(370, aerol10, aerop13, aerop16, formatDia.parse("10-11-2015 11:45"), formatHora.parse("13:45"), (float) 1743, 81);
            Vuelo vuelo371 = new Vuelo(371, aerol11, aerop13, aerop16, formatDia.parse("13-11-2015 16:04"), formatHora.parse("13:45"), (float) 1799, 16);
            Vuelo vuelo372 = new Vuelo(372, aerol12, aerop13, aerop16, formatDia.parse("10-11-2015 05:02"), formatHora.parse("13:45"), (float) 1634, 9);
            Vuelo vuelo373 = new Vuelo(373, aerol1, aerop14, aerop16, formatDia.parse("14-11-2015 03:07"), formatHora.parse("08:44"), (float) 1184, 4);
            Vuelo vuelo374 = new Vuelo(374, aerol2, aerop14, aerop16, formatDia.parse("11-11-2015 23:02"), formatHora.parse("08:44"), (float) 1041, 20);
            Vuelo vuelo375 = new Vuelo(375, aerol3, aerop14, aerop16, formatDia.parse("10-11-2015 22:19"), formatHora.parse("08:44"), (float) 1204, 17);
            Vuelo vuelo376 = new Vuelo(376, aerol4, aerop14, aerop16, formatDia.parse("12-11-2015 21:07"), formatHora.parse("08:44"), (float) 1167, 35);
            Vuelo vuelo377 = new Vuelo(377, aerol5, aerop14, aerop16, formatDia.parse("14-11-2015 14:38"), formatHora.parse("08:44"), (float) 1112, 73);
            Vuelo vuelo378 = new Vuelo(378, aerol6, aerop14, aerop16, formatDia.parse("12-11-2015 15:50"), formatHora.parse("08:44"), (float) 1152, 63);
            Vuelo vuelo379 = new Vuelo(379, aerol7, aerop14, aerop16, formatDia.parse("11-11-2015 06:28"), formatHora.parse("08:44"), (float) 1106, 39);
            Vuelo vuelo380 = new Vuelo(380, aerol8, aerop14, aerop16, formatDia.parse("12-11-2015 15:07"), formatHora.parse("08:44"), (float) 1202, 79);
            Vuelo vuelo381 = new Vuelo(381, aerol9, aerop14, aerop16, formatDia.parse("12-11-2015 08:38"), formatHora.parse("08:44"), (float) 1003, 99);
            Vuelo vuelo382 = new Vuelo(382, aerol10, aerop15, aerop16, formatDia.parse("10-11-2015 18:00"), formatHora.parse("12:22"), (float) 1698, 64);
            Vuelo vuelo383 = new Vuelo(383, aerol11, aerop17, aerop16, formatDia.parse("14-11-2015 00:14"), formatHora.parse("02:01"), (float) 238, 84);
            Vuelo vuelo384 = new Vuelo(384, aerol12, aerop17, aerop16, formatDia.parse("11-11-2015 05:02"), formatHora.parse("02:01"), (float) 274, 13);
            Vuelo vuelo385 = new Vuelo(385, aerol1, aerop17, aerop16, formatDia.parse("13-11-2015 18:57"), formatHora.parse("02:01"), (float) 230, 92);
            Vuelo vuelo386 = new Vuelo(386, aerol2, aerop18, aerop16, formatDia.parse("11-11-2015 15:36"), formatHora.parse("01:53"), (float) 243, 55);
            Vuelo vuelo387 = new Vuelo(387, aerol3, aerop18, aerop16, formatDia.parse("14-11-2015 11:02"), formatHora.parse("01:53"), (float) 244, 62);
            Vuelo vuelo388 = new Vuelo(388, aerol4, aerop19, aerop16, formatDia.parse("10-11-2015 11:02"), formatHora.parse("01:36"), (float) 215, 18);
            Vuelo vuelo389 = new Vuelo(389, aerol5, aerop19, aerop16, formatDia.parse("13-11-2015 20:38"), formatHora.parse("01:36"), (float) 214, 24);
            Vuelo vuelo390 = new Vuelo(390, aerol6, aerop19, aerop16, formatDia.parse("10-11-2015 20:09"), formatHora.parse("01:36"), (float) 204, 72);
            Vuelo vuelo391 = new Vuelo(391, aerol7, aerop20, aerop16, formatDia.parse("13-11-2015 22:33"), formatHora.parse("02:48"), (float) 338, 43);
            Vuelo vuelo392 = new Vuelo(392, aerol8, aerop20, aerop16, formatDia.parse("13-11-2015 19:26"), formatHora.parse("02:48"), (float) 336, 16);
            Vuelo vuelo393 = new Vuelo(393, aerol9, aerop1, aerop13, formatDia.parse("14-11-2015 10:19"), formatHora.parse("11:16"), (float) 1523, 37);
            Vuelo vuelo394 = new Vuelo(394, aerol10, aerop1, aerop5, formatDia.parse("10-11-2015 13:12"), formatHora.parse("02:22"), (float) 329, 86);
            Vuelo vuelo395 = new Vuelo(395, aerol11, aerop1, aerop13, formatDia.parse("12-11-2015 09:07"), formatHora.parse("11:16"), (float) 1336, 51);
            Vuelo vuelo396 = new Vuelo(396, aerol12, aerop1, aerop5, formatDia.parse("13-11-2015 13:26"), formatHora.parse("02:22"), (float) 324, 6);
            Vuelo vuelo397 = new Vuelo(397, aerol1, aerop1, aerop5, formatDia.parse("14-11-2015 00:57"), formatHora.parse("02:22"), (float) 311, 0);
            Vuelo vuelo398 = new Vuelo(398, aerol2, aerop1, aerop5, formatDia.parse("11-11-2015 19:55"), formatHora.parse("02:22"), (float) 292, 88);
            Vuelo vuelo399 = new Vuelo(399, aerol3, aerop3, aerop5, formatDia.parse("11-11-2015 04:33"), formatHora.parse("01:04"), (float) 143, 66);
            Vuelo vuelo400 = new Vuelo(400, aerol4, aerop4, aerop5, formatDia.parse("12-11-2015 03:36"), formatHora.parse("00:20"), (float) 45, 100);
            Vuelo vuelo401 = new Vuelo(401, aerol5, aerop4, aerop13, formatDia.parse("14-11-2015 23:16"), formatHora.parse("11:13"), (float) 1339, 17);
            Vuelo vuelo402 = new Vuelo(402, aerol6, aerop4, aerop5, formatDia.parse("12-11-2015 06:43"), formatHora.parse("00:20"), (float) 48, 1);
            Vuelo vuelo403 = new Vuelo(403, aerol7, aerop4, aerop5, formatDia.parse("13-11-2015 18:43"), formatHora.parse("00:20"), (float) 43, 43);
            Vuelo vuelo404 = new Vuelo(404, aerol8, aerop6, aerop13, formatDia.parse("11-11-2015 19:12"), formatHora.parse("10:00"), (float) 1354, 24);
            Vuelo vuelo405 = new Vuelo(405, aerol9, aerop6, aerop13, formatDia.parse("13-11-2015 14:24"), formatHora.parse("10:00"), (float) 1254, 8);
            Vuelo vuelo406 = new Vuelo(406, aerol10, aerop6, aerop5, formatDia.parse("13-11-2015 07:55"), formatHora.parse("02:04"), (float) 277, 53);
            Vuelo vuelo407 = new Vuelo(407, aerol11, aerop8, aerop13, formatDia.parse("10-11-2015 06:14"), formatHora.parse("04:47"), (float) 643, 4);
            Vuelo vuelo408 = new Vuelo(408, aerol12, aerop8, aerop13, formatDia.parse("12-11-2015 22:33"), formatHora.parse("04:47"), (float) 563, 90);
            Vuelo vuelo409 = new Vuelo(409, aerol1, aerop8, aerop13, formatDia.parse("13-11-2015 00:00"), formatHora.parse("04:47"), (float) 623, 55);
            Vuelo vuelo410 = new Vuelo(410, aerol2, aerop9, aerop13, formatDia.parse("10-11-2015 17:16"), formatHora.parse("06:26"), (float) 771, 47);
            Vuelo vuelo411 = new Vuelo(411, aerol3, aerop9, aerop13, formatDia.parse("12-11-2015 10:19"), formatHora.parse("06:26"), (float) 758, 19);
            Vuelo vuelo412 = new Vuelo(412, aerol4, aerop9, aerop5, formatDia.parse("12-11-2015 04:19"), formatHora.parse("05:01"), (float) 581, 46);
            Vuelo vuelo413 = new Vuelo(413, aerol5, aerop9, aerop13, formatDia.parse("12-11-2015 03:21"), formatHora.parse("06:26"), (float) 851, 67);
            Vuelo vuelo414 = new Vuelo(414, aerol6, aerop11, aerop13, formatDia.parse("14-11-2015 10:48"), formatHora.parse("04:44"), (float) 598, 55);
            Vuelo vuelo415 = new Vuelo(415, aerol7, aerop12, aerop13, formatDia.parse("10-11-2015 00:43"), formatHora.parse("05:25"), (float) 660, 100);
            Vuelo vuelo416 = new Vuelo(416, aerol8, aerop14, aerop13, formatDia.parse("14-11-2015 23:16"), formatHora.parse("05:06"), (float) 618, 88);
            Vuelo vuelo417 = new Vuelo(417, aerol9, aerop14, aerop13, formatDia.parse("11-11-2015 17:16"), formatHora.parse("05:06"), (float) 681, 65);
            Vuelo vuelo418 = new Vuelo(418, aerol10, aerop14, aerop13, formatDia.parse("13-11-2015 13:55"), formatHora.parse("05:06"), (float) 593, 96);
            Vuelo vuelo419 = new Vuelo(419, aerol11, aerop15, aerop13, formatDia.parse("13-11-2015 01:40"), formatHora.parse("03:39"), (float) 480, 38);
            Vuelo vuelo420 = new Vuelo(420, aerol12, aerop15, aerop13, formatDia.parse("14-11-2015 23:02"), formatHora.parse("03:39"), (float) 441, 67);
            Vuelo vuelo421 = new Vuelo(421, aerol1, aerop15, aerop5, formatDia.parse("10-11-2015 07:12"), formatHora.parse("08:17"), (float) 1047, 2);
            Vuelo vuelo422 = new Vuelo(422, aerol2, aerop16, aerop13, formatDia.parse("11-11-2015 10:04"), formatHora.parse("13:45"), (float) 1734, 91);
            Vuelo vuelo423 = new Vuelo(423, aerol3, aerop16, aerop13, formatDia.parse("12-11-2015 10:33"), formatHora.parse("13:45"), (float) 1581, 18);
            Vuelo vuelo424 = new Vuelo(424, aerol4, aerop16, aerop13, formatDia.parse("14-11-2015 00:14"), formatHora.parse("13:45"), (float) 1583, 39);
            Vuelo vuelo425 = new Vuelo(425, aerol5, aerop16, aerop13, formatDia.parse("12-11-2015 17:02"), formatHora.parse("13:45"), (float) 1749, 37);
            Vuelo vuelo426 = new Vuelo(426, aerol6, aerop16, aerop5, formatDia.parse("10-11-2015 07:26"), formatHora.parse("15:05"), (float) 2004, 75);
            Vuelo vuelo427 = new Vuelo(427, aerol7, aerop18, aerop13, formatDia.parse("14-11-2015 04:48"), formatHora.parse("13:30"), (float) 1866, 31);
            Vuelo vuelo428 = new Vuelo(428, aerol8, aerop18, aerop13, formatDia.parse("13-11-2015 17:45"), formatHora.parse("13:30"), (float) 1721, 98);
            Vuelo vuelo429 = new Vuelo(429, aerol9, aerop18, aerop13, formatDia.parse("14-11-2015 06:14"), formatHora.parse("13:30"), (float) 1824, 73);
            Vuelo vuelo430 = new Vuelo(430, aerol10, aerop19, aerop13, formatDia.parse("14-11-2015 00:43"), formatHora.parse("13:57"), (float) 1598, 69);
            Vuelo vuelo431 = new Vuelo(431, aerol11, aerop19, aerop13, formatDia.parse("11-11-2015 20:52"), formatHora.parse("13:57"), (float) 1656, 57);
            Vuelo vuelo432 = new Vuelo(432, aerol12, aerop19, aerop13, formatDia.parse("12-11-2015 15:50"), formatHora.parse("13:57"), (float) 1811, 27);
            Vuelo vuelo433 = new Vuelo(433, aerol1, aerop1, aerop3, formatDia.parse("14-11-2015 12:43"), formatHora.parse("01:18"), (float) 179, 23);
            Vuelo vuelo434 = new Vuelo(434, aerol2, aerop1, aerop15, formatDia.parse("11-11-2015 15:21"), formatHora.parse("07:42"), (float) 891, 72);
            Vuelo vuelo435 = new Vuelo(435, aerol3, aerop1, aerop3, formatDia.parse("14-11-2015 23:45"), formatHora.parse("01:18"), (float) 152, 48);
            Vuelo vuelo436 = new Vuelo(436, aerol4, aerop1, aerop3, formatDia.parse("13-11-2015 00:14"), formatHora.parse("01:18"), (float) 166, 52);
            Vuelo vuelo437 = new Vuelo(437, aerol5, aerop1, aerop3, formatDia.parse("13-11-2015 20:24"), formatHora.parse("01:18"), (float) 176, 38);
            Vuelo vuelo438 = new Vuelo(438, aerol6, aerop2, aerop3, formatDia.parse("10-11-2015 04:04"), formatHora.parse("01:41"), (float) 211, 66);
            Vuelo vuelo439 = new Vuelo(439, aerol7, aerop3, aerop15, formatDia.parse("12-11-2015 14:09"), formatHora.parse("08:01"), (float) 1002, 67);
            Vuelo vuelo440 = new Vuelo(440, aerol8, aerop4, aerop15, formatDia.parse("11-11-2015 16:19"), formatHora.parse("08:06"), (float) 954, 68);
            Vuelo vuelo441 = new Vuelo(441, aerol9, aerop4, aerop3, formatDia.parse("14-11-2015 01:26"), formatHora.parse("01:19"), (float) 151, 12);
            Vuelo vuelo442 = new Vuelo(442, aerol10, aerop5, aerop15, formatDia.parse("11-11-2015 20:09"), formatHora.parse("08:17"), (float) 1140, 50);
            Vuelo vuelo443 = new Vuelo(443, aerol11, aerop5, aerop3, formatDia.parse("12-11-2015 21:36"), formatHora.parse("01:04"), (float) 137, 70);
            Vuelo vuelo444 = new Vuelo(444, aerol12, aerop6, aerop15, formatDia.parse("12-11-2015 18:14"), formatHora.parse("07:17"), (float) 853, 99);
            Vuelo vuelo445 = new Vuelo(445, aerol1, aerop7, aerop15, formatDia.parse("14-11-2015 09:21"), formatHora.parse("06:48"), (float) 934, 62);
            Vuelo vuelo446 = new Vuelo(446, aerol2, aerop8, aerop15, formatDia.parse("13-11-2015 18:00"), formatHora.parse("01:08"), (float) 134, 70);
            Vuelo vuelo447 = new Vuelo(447, aerol3, aerop8, aerop15, formatDia.parse("13-11-2015 23:02"), formatHora.parse("01:08"), (float) 151, 44);
            Vuelo vuelo448 = new Vuelo(448, aerol4, aerop9, aerop3, formatDia.parse("10-11-2015 15:50"), formatHora.parse("05:03"), (float) 701, 13);
            Vuelo vuelo449 = new Vuelo(449, aerol5, aerop9, aerop15, formatDia.parse("10-11-2015 13:40"), formatHora.parse("03:34"), (float) 493, 53);
            Vuelo vuelo450 = new Vuelo(450, aerol6, aerop9, aerop3, formatDia.parse("10-11-2015 03:21"), formatHora.parse("05:03"), (float) 671, 49);
            Vuelo vuelo451 = new Vuelo(451, aerol7, aerop11, aerop15, formatDia.parse("11-11-2015 22:33"), formatHora.parse("01:33"), (float) 201, 14);
            Vuelo vuelo452 = new Vuelo(452, aerol8, aerop12, aerop15, formatDia.parse("11-11-2015 04:48"), formatHora.parse("02:04"), (float) 285, 91);
            Vuelo vuelo453 = new Vuelo(453, aerol9, aerop12, aerop15, formatDia.parse("12-11-2015 14:24"), formatHora.parse("02:04"), (float) 286, 43);
            Vuelo vuelo454 = new Vuelo(454, aerol10, aerop12, aerop15, formatDia.parse("10-11-2015 00:57"), formatHora.parse("02:04"), (float) 279, 63);
            Vuelo vuelo455 = new Vuelo(455, aerol11, aerop13, aerop15, formatDia.parse("13-11-2015 13:12"), formatHora.parse("03:39"), (float) 466, 18);
            Vuelo vuelo456 = new Vuelo(456, aerol12, aerop13, aerop15, formatDia.parse("10-11-2015 13:12"), formatHora.parse("03:39"), (float) 506, 70);
            Vuelo vuelo457 = new Vuelo(457, aerol1, aerop14, aerop15, formatDia.parse("12-11-2015 20:38"), formatHora.parse("05:23"), (float) 727, 27);
            Vuelo vuelo458 = new Vuelo(458, aerol2, aerop14, aerop15, formatDia.parse("13-11-2015 12:57"), formatHora.parse("05:23"), (float) 741, 49);
            Vuelo vuelo459 = new Vuelo(459, aerol3, aerop15, aerop3, formatDia.parse("10-11-2015 00:43"), formatHora.parse("08:01"), (float) 924, 65);
            Vuelo vuelo460 = new Vuelo(460, aerol4, aerop16, aerop15, formatDia.parse("12-11-2015 08:52"), formatHora.parse("12:22"), (float) 1628, 41);
            Vuelo vuelo461 = new Vuelo(461, aerol5, aerop19, aerop15, formatDia.parse("13-11-2015 21:50"), formatHora.parse("13:08"), (float) 1753, 22);
            Vuelo vuelo462 = new Vuelo(462, aerol6, aerop1, aerop2, formatDia.parse("14-11-2015 17:45"), formatHora.parse("00:31"), (float) 64, 23);
            Vuelo vuelo463 = new Vuelo(463, aerol7, aerop1, aerop2, formatDia.parse("14-11-2015 23:45"), formatHora.parse("00:31"), (float) 62, 43);
            Vuelo vuelo464 = new Vuelo(464, aerol8, aerop1, aerop6, formatDia.parse("11-11-2015 20:24"), formatHora.parse("03:57"), (float) 505, 69);
            Vuelo vuelo465 = new Vuelo(465, aerol9, aerop1, aerop6, formatDia.parse("11-11-2015 22:19"), formatHora.parse("03:57"), (float) 548, 21);
            Vuelo vuelo466 = new Vuelo(466, aerol10, aerop1, aerop6, formatDia.parse("13-11-2015 18:28"), formatHora.parse("03:57"), (float) 491, 93);
            Vuelo vuelo467 = new Vuelo(467, aerol11, aerop3, aerop2, formatDia.parse("12-11-2015 04:04"), formatHora.parse("01:41"), (float) 235, 8);
            Vuelo vuelo468 = new Vuelo(468, aerol12, aerop4, aerop6, formatDia.parse("14-11-2015 20:52"), formatHora.parse("01:43"), (float) 226, 86);
            Vuelo vuelo469 = new Vuelo(469, aerol1, aerop4, aerop6, formatDia.parse("14-11-2015 04:48"), formatHora.parse("01:43"), (float) 230, 96);
            Vuelo vuelo470 = new Vuelo(470, aerol2, aerop4, aerop6, formatDia.parse("10-11-2015 06:28"), formatHora.parse("01:43"), (float) 206, 26);
            Vuelo vuelo471 = new Vuelo(471, aerol3, aerop4, aerop6, formatDia.parse("11-11-2015 06:14"), formatHora.parse("01:43"), (float) 200, 92);
            Vuelo vuelo472 = new Vuelo(472, aerol4, aerop4, aerop6, formatDia.parse("10-11-2015 03:36"), formatHora.parse("01:43"), (float) 219, 71);
            Vuelo vuelo473 = new Vuelo(473, aerol5, aerop5, aerop6, formatDia.parse("14-11-2015 10:19"), formatHora.parse("02:04"), (float) 251, 4);
            Vuelo vuelo474 = new Vuelo(474, aerol6, aerop7, aerop6, formatDia.parse("14-11-2015 22:48"), formatHora.parse("02:23"), (float) 283, 33);
            Vuelo vuelo475 = new Vuelo(475, aerol7, aerop7, aerop6, formatDia.parse("14-11-2015 03:21"), formatHora.parse("02:23"), (float) 289, 28);
            Vuelo vuelo476 = new Vuelo(476, aerol8, aerop8, aerop6, formatDia.parse("11-11-2015 23:16"), formatHora.parse("06:26"), (float) 842, 16);
            Vuelo vuelo477 = new Vuelo(477, aerol9, aerop8, aerop6, formatDia.parse("11-11-2015 17:02"), formatHora.parse("06:26"), (float) 876, 4);
            Vuelo vuelo478 = new Vuelo(478, aerol10, aerop9, aerop6, formatDia.parse("13-11-2015 18:14"), formatHora.parse("03:44"), (float) 493, 91);
            Vuelo vuelo479 = new Vuelo(479, aerol11, aerop9, aerop6, formatDia.parse("13-11-2015 20:24"), formatHora.parse("03:44"), (float) 460, 34);
            Vuelo vuelo480 = new Vuelo(480, aerol12, aerop9, aerop6, formatDia.parse("13-11-2015 01:12"), formatHora.parse("03:44"), (float) 499, 9);
            Vuelo vuelo481 = new Vuelo(481, aerol1, aerop9, aerop6, formatDia.parse("12-11-2015 03:50"), formatHora.parse("03:44"), (float) 454, 92);
            Vuelo vuelo482 = new Vuelo(482, aerol2, aerop9, aerop6, formatDia.parse("11-11-2015 14:24"), formatHora.parse("03:44"), (float) 427, 80);
            Vuelo vuelo483 = new Vuelo(483, aerol3, aerop9, aerop6, formatDia.parse("14-11-2015 07:40"), formatHora.parse("03:44"), (float) 493, 5);
            Vuelo vuelo484 = new Vuelo(484, aerol4, aerop12, aerop6, formatDia.parse("10-11-2015 06:14"), formatHora.parse("07:26"), (float) 929, 63);
            Vuelo vuelo485 = new Vuelo(485, aerol5, aerop13, aerop6, formatDia.parse("10-11-2015 00:14"), formatHora.parse("10:00"), (float) 1225, 1);
            Vuelo vuelo486 = new Vuelo(486, aerol6, aerop13, aerop6, formatDia.parse("11-11-2015 01:12"), formatHora.parse("10:00"), (float) 1180, 17);
            Vuelo vuelo487 = new Vuelo(487, aerol7, aerop14, aerop6, formatDia.parse("14-11-2015 15:21"), formatHora.parse("12:29"), (float) 1684, 62);
            Vuelo vuelo488 = new Vuelo(488, aerol8, aerop14, aerop6, formatDia.parse("14-11-2015 10:04"), formatHora.parse("12:29"), (float) 1464, 16);
            Vuelo vuelo489 = new Vuelo(489, aerol9, aerop15, aerop6, formatDia.parse("11-11-2015 16:48"), formatHora.parse("07:17"), (float) 863, 10);
            Vuelo vuelo490 = new Vuelo(490, aerol10, aerop16, aerop6, formatDia.parse("10-11-2015 17:02"), formatHora.parse("16:15"), (float) 1852, 46);
            Vuelo vuelo491 = new Vuelo(491, aerol11, aerop16, aerop6, formatDia.parse("14-11-2015 01:55"), formatHora.parse("16:15"), (float) 2199, 62);
            Vuelo vuelo492 = new Vuelo(492, aerol12, aerop19, aerop6, formatDia.parse("13-11-2015 19:55"), formatHora.parse("17:42"), (float) 2205, 42);
            Vuelo vuelo493 = new Vuelo(493, aerol1, aerop19, aerop6, formatDia.parse("14-11-2015 17:31"), formatHora.parse("17:42"), (float) 2011, 21);
            Vuelo vuelo494 = new Vuelo(494, aerol2, aerop14, aerop20, formatDia.parse("13-11-2015 23:31"), formatHora.parse("09:39"), (float) 1267, 42);
            Vuelo vuelo495 = new Vuelo(495, aerol3, aerop14, aerop20, formatDia.parse("13-11-2015 08:38"), formatHora.parse("09:39"), (float) 1302, 13);
            Vuelo vuelo496 = new Vuelo(496, aerol4, aerop16, aerop20, formatDia.parse("13-11-2015 11:02"), formatHora.parse("02:48"), (float) 356, 61);
            Vuelo vuelo497 = new Vuelo(497, aerol5, aerop16, aerop20, formatDia.parse("14-11-2015 23:16"), formatHora.parse("02:48"), (float) 386, 76);
            Vuelo vuelo498 = new Vuelo(498, aerol6, aerop17, aerop20, formatDia.parse("13-11-2015 15:21"), formatHora.parse("01:49"), (float) 215, 40);
            Vuelo vuelo499 = new Vuelo(499, aerol7, aerop17, aerop20, formatDia.parse("11-11-2015 03:36"), formatHora.parse("01:49"), (float) 223, 44);
            Vuelo vuelo500 = new Vuelo(500, aerol8, aerop17, aerop20, formatDia.parse("14-11-2015 14:24"), formatHora.parse("01:49"), (float) 222, 32);
            Vuelo vuelo501 = new Vuelo(501, aerol9, aerop18, aerop20, formatDia.parse("10-11-2015 06:28"), formatHora.parse("01:26"), (float) 173, 100);
            Vuelo vuelo502 = new Vuelo(502, aerol10, aerop18, aerop20, formatDia.parse("12-11-2015 04:48"), formatHora.parse("01:26"), (float) 181, 52);
            Vuelo vuelo503 = new Vuelo(503, aerol11, aerop18, aerop20, formatDia.parse("11-11-2015 07:55"), formatHora.parse("01:26"), (float) 183, 56);
            Vuelo vuelo504 = new Vuelo(504, aerol12, aerop18, aerop20, formatDia.parse("11-11-2015 20:52"), formatHora.parse("01:26"), (float) 198, 99);
            Vuelo vuelo505 = new Vuelo(505, aerol1, aerop19, aerop20, formatDia.parse("14-11-2015 04:04"), formatHora.parse("01:17"), (float) 176, 66);
            Vuelo vuelo506 = new Vuelo(506, aerol2, aerop19, aerop20, formatDia.parse("11-11-2015 11:16"), formatHora.parse("01:17"), (float) 158, 82);
            Vuelo vuelo507 = new Vuelo(507, aerol3, aerop19, aerop20, formatDia.parse("11-11-2015 12:57"), formatHora.parse("01:17"), (float) 162, 46);
            Vuelo vuelo508 = new Vuelo(508, aerol4, aerop1, aerop11, formatDia.parse("14-11-2015 04:19"), formatHora.parse("06:33"), (float) 885, 66);
            Vuelo vuelo509 = new Vuelo(509, aerol5, aerop6, aerop11, formatDia.parse("11-11-2015 00:00"), formatHora.parse("05:44"), (float) 670, 79);
            Vuelo vuelo510 = new Vuelo(510, aerol6, aerop8, aerop11, formatDia.parse("11-11-2015 17:45"), formatHora.parse("01:05"), (float) 145, 64);
            Vuelo vuelo511 = new Vuelo(511, aerol7, aerop8, aerop11, formatDia.parse("12-11-2015 14:09"), formatHora.parse("01:05"), (float) 141, 90);
            Vuelo vuelo512 = new Vuelo(512, aerol8, aerop8, aerop11, formatDia.parse("12-11-2015 11:02"), formatHora.parse("01:05"), (float) 143, 69);
            Vuelo vuelo513 = new Vuelo(513, aerol9, aerop8, aerop11, formatDia.parse("10-11-2015 08:09"), formatHora.parse("01:05"), (float) 133, 63);
            Vuelo vuelo514 = new Vuelo(514, aerol10, aerop9, aerop11, formatDia.parse("10-11-2015 14:24"), formatHora.parse("02:01"), (float) 260, 55);
            Vuelo vuelo515 = new Vuelo(515, aerol11, aerop9, aerop11, formatDia.parse("13-11-2015 01:26"), formatHora.parse("02:01"), (float) 272, 58);
            Vuelo vuelo516 = new Vuelo(516, aerol12, aerop9, aerop11, formatDia.parse("13-11-2015 23:45"), formatHora.parse("02:01"), (float) 265, 55);
            Vuelo vuelo517 = new Vuelo(517, aerol1, aerop9, aerop11, formatDia.parse("11-11-2015 08:24"), formatHora.parse("02:01"), (float) 258, 42);
            Vuelo vuelo518 = new Vuelo(518, aerol2, aerop13, aerop11, formatDia.parse("13-11-2015 12:57"), formatHora.parse("04:44"), (float) 636, 50);
            Vuelo vuelo519 = new Vuelo(519, aerol3, aerop15, aerop11, formatDia.parse("13-11-2015 18:43"), formatHora.parse("01:33"), (float) 195, 51);
            Vuelo vuelo520 = new Vuelo(520, aerol4, aerop16, aerop11, formatDia.parse("10-11-2015 07:40"), formatHora.parse("13:16"), (float) 1781, 50);
            Vuelo vuelo521 = new Vuelo(521, aerol5, aerop16, aerop11, formatDia.parse("11-11-2015 13:12"), formatHora.parse("13:16"), (float) 1813, 92);

            vuelos.add(vuelo1);
            vuelos.add(vuelo2);
            vuelos.add(vuelo3);
            vuelos.add(vuelo4);
            vuelos.add(vuelo5);
            vuelos.add(vuelo6);
            vuelos.add(vuelo7);
            vuelos.add(vuelo8);
            vuelos.add(vuelo9);
            vuelos.add(vuelo10);
            vuelos.add(vuelo11);
            vuelos.add(vuelo12);
            vuelos.add(vuelo13);
            vuelos.add(vuelo14);
            vuelos.add(vuelo15);
            vuelos.add(vuelo16);
            vuelos.add(vuelo17);
            vuelos.add(vuelo18);
            vuelos.add(vuelo19);
            vuelos.add(vuelo20);
            vuelos.add(vuelo21);
            vuelos.add(vuelo22);
            vuelos.add(vuelo23);
            vuelos.add(vuelo24);
            vuelos.add(vuelo25);
            vuelos.add(vuelo26);
            vuelos.add(vuelo27);
            vuelos.add(vuelo28);
            vuelos.add(vuelo29);
            vuelos.add(vuelo30);
            vuelos.add(vuelo31);
            vuelos.add(vuelo32);
            vuelos.add(vuelo33);
            vuelos.add(vuelo34);
            vuelos.add(vuelo35);
            vuelos.add(vuelo36);
            vuelos.add(vuelo37);
            vuelos.add(vuelo38);
            vuelos.add(vuelo39);
            vuelos.add(vuelo40);
            vuelos.add(vuelo41);
            vuelos.add(vuelo42);
            vuelos.add(vuelo43);
            vuelos.add(vuelo44);
            vuelos.add(vuelo45);
            vuelos.add(vuelo46);
            vuelos.add(vuelo47);
            vuelos.add(vuelo48);
            vuelos.add(vuelo49);
            vuelos.add(vuelo50);
            vuelos.add(vuelo51);
            vuelos.add(vuelo52);
            vuelos.add(vuelo53);
            vuelos.add(vuelo54);
            vuelos.add(vuelo55);
            vuelos.add(vuelo56);
            vuelos.add(vuelo57);
            vuelos.add(vuelo58);
            vuelos.add(vuelo59);
            vuelos.add(vuelo60);
            vuelos.add(vuelo61);
            vuelos.add(vuelo62);
            vuelos.add(vuelo63);
            vuelos.add(vuelo64);
            vuelos.add(vuelo65);
            vuelos.add(vuelo66);
            vuelos.add(vuelo67);
            vuelos.add(vuelo68);
            vuelos.add(vuelo69);
            vuelos.add(vuelo70);
            vuelos.add(vuelo71);
            vuelos.add(vuelo72);
            vuelos.add(vuelo73);
            vuelos.add(vuelo74);
            vuelos.add(vuelo75);
            vuelos.add(vuelo76);
            vuelos.add(vuelo77);
            vuelos.add(vuelo78);
            vuelos.add(vuelo79);
            vuelos.add(vuelo80);
            vuelos.add(vuelo81);
            vuelos.add(vuelo82);
            vuelos.add(vuelo83);
            vuelos.add(vuelo84);
            vuelos.add(vuelo85);
            vuelos.add(vuelo86);
            vuelos.add(vuelo87);
            vuelos.add(vuelo88);
            vuelos.add(vuelo89);
            vuelos.add(vuelo90);
            vuelos.add(vuelo91);
            vuelos.add(vuelo92);
            vuelos.add(vuelo93);
            vuelos.add(vuelo94);
            vuelos.add(vuelo95);
            vuelos.add(vuelo96);
            vuelos.add(vuelo97);
            vuelos.add(vuelo98);
            vuelos.add(vuelo99);
            vuelos.add(vuelo100);
            vuelos.add(vuelo101);
            vuelos.add(vuelo102);
            vuelos.add(vuelo103);
            vuelos.add(vuelo104);
            vuelos.add(vuelo105);
            vuelos.add(vuelo106);
            vuelos.add(vuelo107);
            vuelos.add(vuelo108);
            vuelos.add(vuelo109);
            vuelos.add(vuelo110);
            vuelos.add(vuelo111);
            vuelos.add(vuelo112);
            vuelos.add(vuelo113);
            vuelos.add(vuelo114);
            vuelos.add(vuelo115);
            vuelos.add(vuelo116);
            vuelos.add(vuelo117);
            vuelos.add(vuelo118);
            vuelos.add(vuelo119);
            vuelos.add(vuelo120);
            vuelos.add(vuelo121);
            vuelos.add(vuelo122);
            vuelos.add(vuelo123);
            vuelos.add(vuelo124);
            vuelos.add(vuelo125);
            vuelos.add(vuelo126);
            vuelos.add(vuelo127);
            vuelos.add(vuelo128);
            vuelos.add(vuelo129);
            vuelos.add(vuelo130);
            vuelos.add(vuelo131);
            vuelos.add(vuelo132);
            vuelos.add(vuelo133);
            vuelos.add(vuelo134);
            vuelos.add(vuelo135);
            vuelos.add(vuelo136);
            vuelos.add(vuelo137);
            vuelos.add(vuelo138);
            vuelos.add(vuelo139);
            vuelos.add(vuelo140);
            vuelos.add(vuelo141);
            vuelos.add(vuelo142);
            vuelos.add(vuelo143);
            vuelos.add(vuelo144);
            vuelos.add(vuelo145);
            vuelos.add(vuelo146);
            vuelos.add(vuelo147);
            vuelos.add(vuelo148);
            vuelos.add(vuelo149);
            vuelos.add(vuelo150);
            vuelos.add(vuelo151);
            vuelos.add(vuelo152);
            vuelos.add(vuelo153);
            vuelos.add(vuelo154);
            vuelos.add(vuelo155);
            vuelos.add(vuelo156);
            vuelos.add(vuelo157);
            vuelos.add(vuelo158);
            vuelos.add(vuelo159);
            vuelos.add(vuelo160);
            vuelos.add(vuelo161);
            vuelos.add(vuelo162);
            vuelos.add(vuelo163);
            vuelos.add(vuelo164);
            vuelos.add(vuelo165);
            vuelos.add(vuelo166);
            vuelos.add(vuelo167);
            vuelos.add(vuelo168);
            vuelos.add(vuelo169);
            vuelos.add(vuelo170);
            vuelos.add(vuelo171);
            vuelos.add(vuelo172);
            vuelos.add(vuelo173);
            vuelos.add(vuelo174);
            vuelos.add(vuelo175);
            vuelos.add(vuelo176);
            vuelos.add(vuelo177);
            vuelos.add(vuelo178);
            vuelos.add(vuelo179);
            vuelos.add(vuelo180);
            vuelos.add(vuelo181);
            vuelos.add(vuelo182);
            vuelos.add(vuelo183);
            vuelos.add(vuelo184);
            vuelos.add(vuelo185);
            vuelos.add(vuelo186);
            vuelos.add(vuelo187);
            vuelos.add(vuelo188);
            vuelos.add(vuelo189);
            vuelos.add(vuelo190);
            vuelos.add(vuelo191);
            vuelos.add(vuelo192);
            vuelos.add(vuelo193);
            vuelos.add(vuelo194);
            vuelos.add(vuelo195);
            vuelos.add(vuelo196);
            vuelos.add(vuelo197);
            vuelos.add(vuelo198);
            vuelos.add(vuelo199);
            vuelos.add(vuelo200);
            vuelos.add(vuelo201);
            vuelos.add(vuelo202);
            vuelos.add(vuelo203);
            vuelos.add(vuelo204);
            vuelos.add(vuelo205);
            vuelos.add(vuelo206);
            vuelos.add(vuelo207);
            vuelos.add(vuelo208);
            vuelos.add(vuelo209);
            vuelos.add(vuelo210);
            vuelos.add(vuelo211);
            vuelos.add(vuelo212);
            vuelos.add(vuelo213);
            vuelos.add(vuelo214);
            vuelos.add(vuelo215);
            vuelos.add(vuelo216);
            vuelos.add(vuelo217);
            vuelos.add(vuelo218);
            vuelos.add(vuelo219);
            vuelos.add(vuelo220);
            vuelos.add(vuelo221);
            vuelos.add(vuelo222);
            vuelos.add(vuelo223);
            vuelos.add(vuelo224);
            vuelos.add(vuelo225);
            vuelos.add(vuelo226);
            vuelos.add(vuelo227);
            vuelos.add(vuelo228);
            vuelos.add(vuelo229);
            vuelos.add(vuelo230);
            vuelos.add(vuelo231);
            vuelos.add(vuelo232);
            vuelos.add(vuelo233);
            vuelos.add(vuelo234);
            vuelos.add(vuelo235);
            vuelos.add(vuelo236);
            vuelos.add(vuelo237);
            vuelos.add(vuelo238);
            vuelos.add(vuelo239);
            vuelos.add(vuelo240);
            vuelos.add(vuelo241);
            vuelos.add(vuelo242);
            vuelos.add(vuelo243);
            vuelos.add(vuelo244);
            vuelos.add(vuelo245);
            vuelos.add(vuelo246);
            vuelos.add(vuelo247);
            vuelos.add(vuelo248);
            vuelos.add(vuelo249);
            vuelos.add(vuelo250);
            vuelos.add(vuelo251);
            vuelos.add(vuelo252);
            vuelos.add(vuelo253);
            vuelos.add(vuelo254);
            vuelos.add(vuelo255);
            vuelos.add(vuelo256);
            vuelos.add(vuelo257);
            vuelos.add(vuelo258);
            vuelos.add(vuelo259);
            vuelos.add(vuelo260);
            vuelos.add(vuelo261);
            vuelos.add(vuelo262);
            vuelos.add(vuelo263);
            vuelos.add(vuelo264);
            vuelos.add(vuelo265);
            vuelos.add(vuelo266);
            vuelos.add(vuelo267);
            vuelos.add(vuelo268);
            vuelos.add(vuelo269);
            vuelos.add(vuelo270);
            vuelos.add(vuelo271);
            vuelos.add(vuelo272);
            vuelos.add(vuelo273);
            vuelos.add(vuelo274);
            vuelos.add(vuelo275);
            vuelos.add(vuelo276);
            vuelos.add(vuelo277);
            vuelos.add(vuelo278);
            vuelos.add(vuelo279);
            vuelos.add(vuelo280);
            vuelos.add(vuelo281);
            vuelos.add(vuelo282);
            vuelos.add(vuelo283);
            vuelos.add(vuelo284);
            vuelos.add(vuelo285);
            vuelos.add(vuelo286);
            vuelos.add(vuelo287);
            vuelos.add(vuelo288);
            vuelos.add(vuelo289);
            vuelos.add(vuelo290);
            vuelos.add(vuelo291);
            vuelos.add(vuelo292);
            vuelos.add(vuelo293);
            vuelos.add(vuelo294);
            vuelos.add(vuelo295);
            vuelos.add(vuelo296);
            vuelos.add(vuelo297);
            vuelos.add(vuelo298);
            vuelos.add(vuelo299);
            vuelos.add(vuelo300);
            vuelos.add(vuelo301);
            vuelos.add(vuelo302);
            vuelos.add(vuelo303);
            vuelos.add(vuelo304);
            vuelos.add(vuelo305);
            vuelos.add(vuelo306);
            vuelos.add(vuelo307);
            vuelos.add(vuelo308);
            vuelos.add(vuelo309);
            vuelos.add(vuelo310);
            vuelos.add(vuelo311);
            vuelos.add(vuelo312);
            vuelos.add(vuelo313);
            vuelos.add(vuelo314);
            vuelos.add(vuelo315);
            vuelos.add(vuelo316);
            vuelos.add(vuelo317);
            vuelos.add(vuelo318);
            vuelos.add(vuelo319);
            vuelos.add(vuelo320);
            vuelos.add(vuelo321);
            vuelos.add(vuelo322);
            vuelos.add(vuelo323);
            vuelos.add(vuelo324);
            vuelos.add(vuelo325);
            vuelos.add(vuelo326);
            vuelos.add(vuelo327);
            vuelos.add(vuelo328);
            vuelos.add(vuelo329);
            vuelos.add(vuelo330);
            vuelos.add(vuelo331);
            vuelos.add(vuelo332);
            vuelos.add(vuelo333);
            vuelos.add(vuelo334);
            vuelos.add(vuelo335);
            vuelos.add(vuelo336);
            vuelos.add(vuelo337);
            vuelos.add(vuelo338);
            vuelos.add(vuelo339);
            vuelos.add(vuelo340);
            vuelos.add(vuelo341);
            vuelos.add(vuelo342);
            vuelos.add(vuelo343);
            vuelos.add(vuelo344);
            vuelos.add(vuelo345);
            vuelos.add(vuelo346);
            vuelos.add(vuelo347);
            vuelos.add(vuelo348);
            vuelos.add(vuelo349);
            vuelos.add(vuelo350);
            vuelos.add(vuelo351);
            vuelos.add(vuelo352);
            vuelos.add(vuelo353);
            vuelos.add(vuelo354);
            vuelos.add(vuelo355);
            vuelos.add(vuelo356);
            vuelos.add(vuelo357);
            vuelos.add(vuelo358);
            vuelos.add(vuelo359);
            vuelos.add(vuelo360);
            vuelos.add(vuelo361);
            vuelos.add(vuelo362);
            vuelos.add(vuelo363);
            vuelos.add(vuelo364);
            vuelos.add(vuelo365);
            vuelos.add(vuelo366);
            vuelos.add(vuelo367);
            vuelos.add(vuelo368);
            vuelos.add(vuelo369);
            vuelos.add(vuelo370);
            vuelos.add(vuelo371);
            vuelos.add(vuelo372);
            vuelos.add(vuelo373);
            vuelos.add(vuelo374);
            vuelos.add(vuelo375);
            vuelos.add(vuelo376);
            vuelos.add(vuelo377);
            vuelos.add(vuelo378);
            vuelos.add(vuelo379);
            vuelos.add(vuelo380);
            vuelos.add(vuelo381);
            vuelos.add(vuelo382);
            vuelos.add(vuelo383);
            vuelos.add(vuelo384);
            vuelos.add(vuelo385);
            vuelos.add(vuelo386);
            vuelos.add(vuelo387);
            vuelos.add(vuelo388);
            vuelos.add(vuelo389);
            vuelos.add(vuelo390);
            vuelos.add(vuelo391);
            vuelos.add(vuelo392);
            vuelos.add(vuelo393);
            vuelos.add(vuelo394);
            vuelos.add(vuelo395);
            vuelos.add(vuelo396);
            vuelos.add(vuelo397);
            vuelos.add(vuelo398);
            vuelos.add(vuelo399);
            vuelos.add(vuelo400);
            vuelos.add(vuelo401);
            vuelos.add(vuelo402);
            vuelos.add(vuelo403);
            vuelos.add(vuelo404);
            vuelos.add(vuelo405);
            vuelos.add(vuelo406);
            vuelos.add(vuelo407);
            vuelos.add(vuelo408);
            vuelos.add(vuelo409);
            vuelos.add(vuelo410);
            vuelos.add(vuelo411);
            vuelos.add(vuelo412);
            vuelos.add(vuelo413);
            vuelos.add(vuelo414);
            vuelos.add(vuelo415);
            vuelos.add(vuelo416);
            vuelos.add(vuelo417);
            vuelos.add(vuelo418);
            vuelos.add(vuelo419);
            vuelos.add(vuelo420);
            vuelos.add(vuelo421);
            vuelos.add(vuelo422);
            vuelos.add(vuelo423);
            vuelos.add(vuelo424);
            vuelos.add(vuelo425);
            vuelos.add(vuelo426);
            vuelos.add(vuelo427);
            vuelos.add(vuelo428);
            vuelos.add(vuelo429);
            vuelos.add(vuelo430);
            vuelos.add(vuelo431);
            vuelos.add(vuelo432);
            vuelos.add(vuelo433);
            vuelos.add(vuelo434);
            vuelos.add(vuelo435);
            vuelos.add(vuelo436);
            vuelos.add(vuelo437);
            vuelos.add(vuelo438);
            vuelos.add(vuelo439);
            vuelos.add(vuelo440);
            vuelos.add(vuelo441);
            vuelos.add(vuelo442);
            vuelos.add(vuelo443);
            vuelos.add(vuelo444);
            vuelos.add(vuelo445);
            vuelos.add(vuelo446);
            vuelos.add(vuelo447);
            vuelos.add(vuelo448);
            vuelos.add(vuelo449);
            vuelos.add(vuelo450);
            vuelos.add(vuelo451);
            vuelos.add(vuelo452);
            vuelos.add(vuelo453);
            vuelos.add(vuelo454);
            vuelos.add(vuelo455);
            vuelos.add(vuelo456);
            vuelos.add(vuelo457);
            vuelos.add(vuelo458);
            vuelos.add(vuelo459);
            vuelos.add(vuelo460);
            vuelos.add(vuelo461);
            vuelos.add(vuelo462);
            vuelos.add(vuelo463);
            vuelos.add(vuelo464);
            vuelos.add(vuelo465);
            vuelos.add(vuelo466);
            vuelos.add(vuelo467);
            vuelos.add(vuelo468);
            vuelos.add(vuelo469);
            vuelos.add(vuelo470);
            vuelos.add(vuelo471);
            vuelos.add(vuelo472);
            vuelos.add(vuelo473);
            vuelos.add(vuelo474);
            vuelos.add(vuelo475);
            vuelos.add(vuelo476);
            vuelos.add(vuelo477);
            vuelos.add(vuelo478);
            vuelos.add(vuelo479);
            vuelos.add(vuelo480);
            vuelos.add(vuelo481);
            vuelos.add(vuelo482);
            vuelos.add(vuelo483);
            vuelos.add(vuelo484);
            vuelos.add(vuelo485);
            vuelos.add(vuelo486);
            vuelos.add(vuelo487);
            vuelos.add(vuelo488);
            vuelos.add(vuelo489);
            vuelos.add(vuelo490);
            vuelos.add(vuelo491);
            vuelos.add(vuelo492);
            vuelos.add(vuelo493);
            vuelos.add(vuelo494);
            vuelos.add(vuelo495);
            vuelos.add(vuelo496);
            vuelos.add(vuelo497);
            vuelos.add(vuelo498);
            vuelos.add(vuelo499);
            vuelos.add(vuelo500);
            vuelos.add(vuelo501);
            vuelos.add(vuelo502);
            vuelos.add(vuelo503);
            vuelos.add(vuelo504);
            vuelos.add(vuelo505);
            vuelos.add(vuelo506);
            vuelos.add(vuelo507);
            vuelos.add(vuelo508);
            vuelos.add(vuelo509);
            vuelos.add(vuelo510);
            vuelos.add(vuelo511);
            vuelos.add(vuelo512);
            vuelos.add(vuelo513);
            vuelos.add(vuelo514);
            vuelos.add(vuelo515);
            vuelos.add(vuelo516);
            vuelos.add(vuelo517);
            vuelos.add(vuelo518);
            vuelos.add(vuelo519);
            vuelos.add(vuelo520);
            vuelos.add(vuelo521);

        } catch (Exception e) {
        }

    }

    private void cargarUsuarios() {
        Usuario cliente = new Cliente();
        cliente.setUser("cliente");
        usuarios.add(cliente);
        Usuario cliente2 = new Cliente();
        cliente2.setUser("Cliente");
        usuarios.add(cliente2);
    }

    private void cargarTransaciones() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
