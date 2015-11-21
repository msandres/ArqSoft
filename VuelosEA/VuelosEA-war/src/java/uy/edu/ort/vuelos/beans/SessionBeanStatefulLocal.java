/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uy.edu.ort.vuelos.beans;

import java.util.HashMap;
import javax.ejb.Local;

/**
 *
 * @author Renato
 */
@Local
public interface SessionBeanStatefulLocal {

    String getAeropuertos();

    String vuelosSinEscala(String fechaIda, String aeropuertoOrigen, String aeropuertoDestino);

    String vuelos(String aeropuertoOrigen, String aeropuertoDestino, String ida_Vuelta, String fechaIda, String fechaVuelta, String cantPersonas);

    String comprarVuelo(String idVuelo, String cantPersonas);
    
    String reservarVuelo(String idVuelo, String cantPersonas);
    
    String comprarReserva(String codigoReserva);

    String altaAerolinea(String nombreAerolinea, String codigoAerolinea, String informacion);

    String bajaAerolinea(String codigoAerolinea);

    String modificarAerolinea(String codigoAerolinea, String nuevoNombre, String nuevoCodigo, String nuevaInformacion);

    String historialCompras();
}
