/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uy.edu.ort.dominio;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import java.util.Date;

/**
 *
 * @author Renato
 */
public class Vuelo {

    private long id;
    private Aerolinea aerolinea;
    private Aeropuerto aeropuertoOrigen;
    private Aeropuerto aeropuertoDestino;
    private Date fecha;
    private Date duracion;
    private Float tarifa;
    private long disponibles;

    public Vuelo(long id, Aerolinea aerolinea, Aeropuerto aeropuertoOrigen, Aeropuerto aeropuertoDestino, Date fecha, Date duracion, Float tarifa, long disponibles) {
        this.id = id;
        this.aerolinea = aerolinea;
        this.aeropuertoOrigen = aeropuertoOrigen;
        this.aeropuertoDestino = aeropuertoDestino;
        this.fecha = fecha;
        this.duracion = duracion;
        this.tarifa = tarifa;
        this.disponibles = disponibles;
    }

    public Vuelo(Aeropuerto aeropuertoOrigen) {
        this.aeropuertoOrigen = aeropuertoOrigen;
        aeropuertoDestino = new Aeropuerto(-1,"");
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Aerolinea getAerolinea() {
        return aerolinea;
    }

    public void setAerolinea(Aerolinea aerolinea) {
        this.aerolinea = aerolinea;
    }

    public Aeropuerto getAeropuertoOrigen() {
        return aeropuertoOrigen;
    }

    public void setAeropuertoOrigen(Aeropuerto aeropuertoOrigen) {
        this.aeropuertoOrigen = aeropuertoOrigen;
    }

    public Aeropuerto getAeropuertoDestino() {
        return aeropuertoDestino;
    }

    public void setAeropuertoDestino(Aeropuerto aeropuertoDestino) {
        this.aeropuertoDestino = aeropuertoDestino;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Date getDuracion() {
        return duracion;
    }

    public void setDuracion(Date duracion) {
        this.duracion = duracion;
    }

    public long getDisponibles() {
        return disponibles;
    }

    public void setDisponibles(long disponibles) {
        this.disponibles = disponibles;
    }

    public Float getTarifa() {
        return tarifa;
    }

    public void setTarifa(Float tarifa) {
        this.tarifa = tarifa;
    }

    private double Radianes(double grados) {
        double LOCAL_PI = 3.1415926535897932385;
        double radianes = grados * LOCAL_PI / 180;
        return radianes;
    }

    public double distancia() {
        double radioTierra = 6378;
        double dLat = Radianes(aeropuertoDestino.getLatitud() - aeropuertoOrigen.getLatitud());
        double dLng = Radianes(aeropuertoDestino.getLongitud() - aeropuertoOrigen.getLongitud());
        double a = sin(dLat / 2) * sin(dLat / 2)
                + cos(Radianes(aeropuertoOrigen.getLatitud())) * cos(Radianes(aeropuertoDestino.getLatitud()))
                * sin(dLng / 2) * sin(dLng / 2);
        double c = 2 * atan2(sqrt(a), sqrt(1 - a));
        double dist = radioTierra * c;
        return dist;
    }

}
