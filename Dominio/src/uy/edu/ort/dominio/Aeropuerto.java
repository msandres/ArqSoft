/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uy.edu.ort.dominio;

/**
 *
 * @author Renato
 */
public class Aeropuerto {

    private long id;
    private String nombre;
    private String codigoAeropuerto;
    private String ciudad;
    private String pais;
    private Float latitud;
    private Float longitud;

    public Aeropuerto() {
    }

    public Aeropuerto(long id, String codigoAeropuerto) {
        this.id = id;
        this.codigoAeropuerto = codigoAeropuerto;
    }

    public Aeropuerto(Aeropuerto temp) {
        this.id = temp.id;
        this.nombre = temp.nombre;
        this.codigoAeropuerto = temp.codigoAeropuerto;
        this.ciudad = temp.ciudad;
        this.pais = temp.pais;
        this.latitud = temp.latitud;
        this.longitud = temp.longitud;
    }

    public Aeropuerto(long id, String nombre, String codigoAeropuerto, String ciudad, String pais, Float latitud, Float longitud) {
        this.id = id;
        this.nombre = nombre;
        this.codigoAeropuerto = codigoAeropuerto;
        this.ciudad = ciudad;
        this.pais = pais;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigoAeropuerto() {
        return codigoAeropuerto;
    }

    public void setCodigoAeropuerto(String codigoAeropuerto) {
        this.codigoAeropuerto = codigoAeropuerto;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public Float getLatitud() {
        return latitud;
    }

    public void setLatitud(Float latitud) {
        this.latitud = latitud;
    }

    public Float getLongitud() {
        return longitud;
    }

    public void setLongitud(Float longitud) {
        this.longitud = longitud;
    }
}
