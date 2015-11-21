/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uy.edu.ort.vuelos.beans;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.ejb.Singleton;
import uy.edu.ort.dominio.Aeropuerto;
import uy.edu.ort.dominio.Cliente;
import uy.edu.ort.dominio.ManejoViajes;
import uy.edu.ort.dominio.Transacion;
import uy.edu.ort.dominio.Vuelo;

/**
 *
 * @author Renato
 */
@Singleton
public class SingletonSessionBean implements SingletonSessionBeanLocal {

    ManejoViajes mv = ManejoViajes.obtenerInstanciaManejoViajes();

    @Override
    public List<Aeropuerto> getAeropuertos() {
        return mv.getAeropuertos();
    }

    @Override
    public List<Vuelo> vuelos(String aeropuertoOrigen, String aeropuertoDestino, String fecha, String cantPersonas) {
        try {
            Aeropuerto aeroOrigen = mv.getAeropuerto(aeropuertoOrigen);
            Aeropuerto aeroDestino = mv.getAeropuerto(aeropuertoDestino);
            SimpleDateFormat formatDia = new SimpleDateFormat("dd-MM-yyyy kk:mm");
            Date fechaI = formatDia.parse(fecha);
            int cant = Integer.parseInt(cantPersonas);
            return mv.consultaVuelos(fechaI, aeroOrigen, aeroDestino, cant);
        } catch (Exception e) {//manejar excepciones
            return null;
        }
    }

    @Override
    public List<Vuelo> vuelosSinEscala(String fecha, String aeropuertoOrigen, String aeropuertoDestino) {
        try {
            Aeropuerto aeroOrigen = mv.getAeropuerto(aeropuertoOrigen);
            Aeropuerto aeroDestino = mv.getAeropuerto(aeropuertoDestino);
            SimpleDateFormat formatDia = new SimpleDateFormat("dd-MM-yyyy kk:mm");
            Date fechaI = formatDia.parse(fecha);
            return mv.consultaVuelosSinEscala(fechaI, aeroOrigen, aeroDestino);
        } catch (Exception e) {//manejar excepciones
            return null;
        }
    }

    @Override
    public String comprarVuelo(String idVuelo, String username, String cantPersonas) {
        Cliente cliente = mv.getCliente(username);
        int id = Integer.parseInt(idVuelo);
        long cantidad = Integer.parseInt(cantPersonas);
        return mv.comprarVuelo(id, cliente, cantidad);
    }

    @Override
    public String reservarVuelo(String idVuelo, String username, String cantPersonas) {
        Cliente cliente = mv.getCliente(username);
        int id = Integer.parseInt(idVuelo);
        long cantidad = Integer.parseInt(cantPersonas);
        return mv.reservarVuelo(id, cliente, cantidad);
    }

    @Override
    public String comprarReserva(String codigoReserva, String username) {
        Cliente cliente = mv.getCliente(username);
        return mv.comprarReserva(codigoReserva, cliente);
    }

    @Override
    public String altaAerolinea(String nombreAerolinea, String codigoAerolinea, String informacion) {
        return mv.agregarAerolinea(nombreAerolinea, codigoAerolinea, informacion);
    }

    @Override
    public String bajaAerolinea(String codigoAerolinea) {
        return mv.bajaAerolinea(codigoAerolinea);
    }
    
    @Override
    public String modificarAerolinea(String codigoAerolinea, String nuevoNombre, String nuevoCodigo, String nuevaInformacion) {
        return mv.modificarAerolinea(codigoAerolinea, nuevoNombre, nuevoCodigo, nuevaInformacion);
    }

    @Override
    public List<Transacion> historialCompras(String username) {
        Cliente cliente = mv.getCliente(username);
        return mv.historialTransaciones(cliente);
    }
    
}
