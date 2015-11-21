/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pruebasdominio;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import uy.edu.ort.dominio.Aerolinea;
import uy.edu.ort.dominio.Aeropuerto;
import uy.edu.ort.dominio.Cliente;
import uy.edu.ort.dominio.ManejoViajes;
import uy.edu.ort.dominio.Transacion;
import uy.edu.ort.dominio.Usuario;
import uy.edu.ort.dominio.Vuelo;

/**
 *
 * @author Renato
 */
public class PruebasDominio {

    private static List<Aeropuerto> aeropuertos;
    private static List<Aerolinea> aerolineas;
    private static List<Usuario> usuarios;
    private static List<Transacion> reservas;
private static List<Transacion> compras;
    
    public static void main(String[] args) throws ParseException {
        ManejoViajes mv = ManejoViajes.obtenerInstanciaManejoViajes();
        aeropuertos = mv.getAeropuertos();
        aerolineas = mv.getAerolineas();

        SimpleDateFormat formatDia = new SimpleDateFormat("dd-MM-yyyy kk:mm");
        SimpleDateFormat formatHora = new SimpleDateFormat("kk:mm");
        
        Date fechaIda = formatDia.parse("10-11-2015 00:00");
        Date fechaVuelta = formatDia.parse("13-11-2015 00:00");
        System.out.println(fechaIda);

        Aeropuerto aeropuertoOrigen = obtenerAeropuerto("MAD");
        Aeropuerto aeropuertoDestino = obtenerAeropuerto("MVD");
        List<Vuelo> vuelos = mv.consultaVuelos(fechaIda, aeropuertoOrigen, aeropuertoDestino,4);
        
        imprimirVuelos(vuelos);
        Cliente cliente = mv.getCliente("cliente");
        
        mv.comprarVuelo(1, cliente, 1);
        mv.reservarVuelo(12, cliente, 1);
        mv.comprarVuelo(10, cliente, 1);
        mv.reservarVuelo(112, cliente, 1);
        
        System.out.println("largo lista transaciones: "+mv.historialTransaciones(cliente).size());
       
//        fecha = formatDia.parse("10-11-2015 00:00");
//        aeropuertoOrigen = obtenerAeropuerto("POA");
//        aeropuertoDestino = obtenerAeropuerto("GRU");
//        List<Vuelo> vuelos = mv.devolverVuelosSinEscala(fecha,
//                aeropuertoOrigen, aeropuertoDestino);
//        imprimirVuelos(vuelos);
    }

    public static void imprimirTransaciones(List<Transacion> transaciones){
        int i = 1;
        for (Transacion temp : transaciones) {
            System.out.println(i + ") " 
                    + temp.getVuelo().getAeropuertoOrigen().getCodigoAeropuerto()+ 
                    " - " 
                    + temp.getVuelo().getAeropuertoDestino().getCodigoAeropuerto() 
                    + " - " 
                    + temp.getVuelo().getTarifa()+ " - " + temp.getFecha()+ " - " 
                    + temp.getVuelo().getDuracion()+ " - " + 
                    temp.getVuelo().getDisponibles());
            i++;
        }
    }
    
    public static void imprimirVuelos(List<Vuelo> vueloDestinos) {
        int i = 1;
        for (Vuelo temp : vueloDestinos) {
            System.out.println(i + ") " + temp.getAeropuertoOrigen().getCodigoAeropuerto()+ " - " 
                    + temp.getAeropuertoDestino().getCodigoAeropuerto() + " - " 
                    + temp.getTarifa()+ " - " + temp.getFecha()+ " - " + temp.getDuracion()+ " - " + temp.getDisponibles());
            i++;
        }

    }

    public static void imprimirAeropuertos(List<Aeropuerto> aeropuertoDestinos) {
        int i = 1;
        for (Aeropuerto temp : aeropuertoDestinos) {
            System.out.println(i + ") " + temp.getNombre() + " - " + temp.getCodigoAeropuerto());
            i++;
        }

    }

    public static Aeropuerto obtenerAeropuerto(String codigoAeropuerto) {
        Aeropuerto aux = null;
        for (Aeropuerto temp : aeropuertos) {
            if (temp.getCodigoAeropuerto().equals(codigoAeropuerto)) {
                aux = temp;
            }
        }
        return aux;
    }

  

}
