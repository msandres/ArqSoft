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
public class Aerolinea {

    private static long cont = 5000;

    private long id;
    private String nombre;
    private String codigoAerolinea;
    private String informacion;

    public Aerolinea() {
    }

    public Aerolinea(long id, String nombre, String codigoAerolinea, String informacion) {
        this.id = id;
        this.nombre = nombre;
        this.codigoAerolinea = codigoAerolinea;
        this.informacion = informacion;
    }

    public Aerolinea(String nombre, String codigoAerolinea, String informacion) {
        this.id = cont;
        cont++;
        this.nombre = nombre;
        this.codigoAerolinea = codigoAerolinea;
        this.informacion = informacion;
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

    public String getCodigoAerolinea() {
        return codigoAerolinea;
    }

    public void setCodigoAerolinea(String codigoAerolinea) {
        this.codigoAerolinea = codigoAerolinea;
    }

    public String getInformacion() {
        return informacion;
    }

    public void setInformacion(String informacion) {
        this.informacion = informacion;
    }
}
