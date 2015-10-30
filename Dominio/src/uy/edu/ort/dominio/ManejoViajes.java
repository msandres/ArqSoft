/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uy.edu.ort.dominio;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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

    private ManejoViajes() {
        vuelos = new ArrayList<>(vuelos);
    }
    
    public static ManejoViajes obtenerInstanciaManejoViajes(){
        if (instanciaManejoViajes == null){
            instanciaManejoViajes = new ManejoViajes();
        }
        return instanciaManejoViajes;
    }
 
    public List<Aeropuerto> devolverAeropuertosDestino(Date fecha, Aeropuerto aeropuertoOrigen){
        //devuelve una lista de aeropuertos de los cuales hay un vuelo directo de origen a detino
        //dentro de las 24 horas posterior a la fecha
        List<Aeropuerto> adyacentes = new ArrayList<>();
        Calendar fechaICalendario = Calendar.getInstance();
	fechaICalendario.setTime(fecha); // Configuramos la fecha que se recibe
        Calendar fechaFCalendario = Calendar.getInstance();
        fechaFCalendario.setTime(fecha); 
        fechaFCalendario.add(Calendar.DAY_OF_YEAR, 1); //agregamos un dia
        Iterator it = vuelos.iterator();
        while(it.hasNext()) {
           Vuelo vuelo = (Vuelo)it;
            if((vuelo.getAeropuertoOrigen().equals(aeropuertoOrigen))&&(fechaFCalendario.before(vuelo.getFecha().getTime()) && fechaICalendario.after(vuelo.getFecha().getTime()))){
                adyacentes.add(vuelo.getAeropuertoDestino());
            }            
        }        
        return adyacentes;
    }

    public List<Transacion> devolverTransaciones(Cliente cliente){
        //devuelve el listado de transaciones que ha efectuado dicho cliente
        
        return null;
    }
    
    public List<Vuelo> devolverRutaMenorCosto(Date fecha, Aeropuerto origenAeropuerto, Aeropuerto destinoAeropuerto){
       // devuelve la ruta de menor costo entre dos aeropuertos (origen, destino)
              
        return null;
    }
  
    public List<Vuelo> devolverRutaMenorDistancia(Date fecha, Aeropuerto origenAeropuerto, Aeropuerto destinoAeropuerto){
       // devuelve la ruta de menor distancia entre dos aeropuertos (origen, destino)
              
        return null;
    }
    
}
