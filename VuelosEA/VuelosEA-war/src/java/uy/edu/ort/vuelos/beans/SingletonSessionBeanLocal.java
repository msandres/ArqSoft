/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uy.edu.ort.vuelos.beans;

import java.util.List;
import javax.ejb.Local;
import uy.edu.ort.dominio.Aeropuerto;
import uy.edu.ort.dominio.Transacion;
import uy.edu.ort.dominio.Vuelo;

/**
 *
 * @author Renato
 */
@Local
public interface SingletonSessionBeanLocal {
    
    List<Aeropuerto> getAeropuertos();

    List<Vuelo> vuelosSinEscala(String fecha, String aeropuertoOrigen, String aeropuertoDestino);

    List<Vuelo> vuelos(String aeropuertoOrigen, String aeropuertoDestino, String fecha, String cantPersonas);

    String comprarVuelo(String idVuelo, String username, String cantPersonas);

    String reservarVuelo(String idVuelo, String username, String cantPersonas);
    
    String comprarReserva(String codigoReserva, String username);

    String altaAerolinea(String nombreAerolinea, String codigoAerolinea, String informacion);

    String bajaAerolinea(String codigoAerolinea);

    String modificarAerolinea(String codigoAerolinea, String nuevoNombre, String nuevoCodigo, String nuevaInformacion);

    List<Transacion> historialCompras(String username);
}
