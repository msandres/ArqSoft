/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uy.edu.ort.vuelos.beans;

import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import uy.edu.ort.dominio.Aeropuerto;
import uy.edu.ort.dominio.Transacion;
import uy.edu.ort.dominio.Vuelo;

/**
 *
 * @author Renato
 */
@Stateless
public class SessionBeanStateful implements SessionBeanStatefulLocal {

    @EJB
    SingletonSessionBeanLocal singletonBean;

    @Override
    public String getAeropuertos() {
        List<Aeropuerto> aeropuertos = singletonBean.getAeropuertos();
        String strAerop = "";
        for (Aeropuerto aero : aeropuertos) {
            strAerop = strAerop + aero.getCiudad() + " (" + aero.getCodigoAeropuerto() + ")\n";
        }
        return strAerop;
    }

    @Override
    public String vuelos(String aeropuertoOrigen,
            String aeropuertoDestino, String ida_Vuelta, String fechaIda,
            String fechaVuelta, String cantPersonas) {
        List<Vuelo> vuelos = singletonBean.vuelos(aeropuertoOrigen, aeropuertoDestino, fechaIda, cantPersonas);
        String strVuelos = "Vuelos de Ida:\n" + toStringVuelos(vuelos);
        if ("SI".equals(ida_Vuelta.toUpperCase())) {
            vuelos = singletonBean.vuelos(aeropuertoDestino, aeropuertoOrigen, fechaVuelta, cantPersonas);
            strVuelos = strVuelos + "Vuelos de Vuelta:\n" + toStringVuelos(vuelos);
        }
        return strVuelos;
    }

    @Override
    public String vuelosSinEscala(String fechaIda, String aeropuertoOrigen, String aeropuertoDestino) {
        List<Vuelo> vuelos = singletonBean.vuelosSinEscala(fechaIda, aeropuertoOrigen, aeropuertoDestino);
        return toStringVuelos(vuelos);
    }

    @Override
    public String comprarVuelo(String idVuelo, String cantPersonas) {
        //ver donde va a estar el cliente logueado.
        return singletonBean.comprarVuelo(idVuelo, "cliente", cantPersonas);
    }

    @Override
    public String reservarVuelo(String idVuelo, String cantPersonas) {
        //ver donde va a estar el cliente logueado.
        return singletonBean.reservarVuelo(idVuelo, "cliente", cantPersonas);
    }

    @Override
    public String comprarReserva(String codigoReserva) {
        //ver donde va a estar el cliente logueado.
        return singletonBean.comprarReserva(codigoReserva, "cliente");
    }

    private String toStringVuelos(List<Vuelo> vuelos) {
        String strVuelos = "";
        for (Vuelo v : vuelos) {
            strVuelos = strVuelos + "id:" + v.getId() + "-"
                    + v.getAeropuertoOrigen().getCiudad() + "-"
                    + v.getAeropuertoDestino().getCiudad() + "-"
                    + v.getAerolinea().getNombre() + "-"
                    + v.getFecha() + "-"
                    + v.getTarifa() + "\n";
        }
        return strVuelos;
    }

    @Override
    public String altaAerolinea(String nombreAerolinea, String codigoAerolinea, String informacion) {
        return singletonBean.altaAerolinea(nombreAerolinea, codigoAerolinea, informacion);
    }

    @Override
    public String bajaAerolinea(String codigoAerolinea) {
        return singletonBean.bajaAerolinea(codigoAerolinea);
    }

    @Override
    public String modificarAerolinea(String codigoAerolinea, String nuevoNombre, String nuevoCodigo, String nuevaInformacion) {
        return null;
    }

    @Override
    public String historialCompras() {
        try {
            List<Transacion> transaciones = singletonBean.historialCompras("cliente");
            String strTransaciones = "";
            if (transaciones != null) {
                for (Transacion t : transaciones) {
                    strTransaciones = strTransaciones
                            + t.getVuelo().getAeropuertoOrigen().getCiudad() + "-"
                            + t.getVuelo().getAeropuertoDestino().getCiudad() + "-"
                            + t.getEstado() + "-" + t.getFecha() + ")\n";
                }
            } else {
                strTransaciones = "No existen transaciones";
            }
            return strTransaciones;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}


