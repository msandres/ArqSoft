/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uy.edu.ort.dominio;

import java.util.Date;

/**
 *
 * @author Renato
 */
public class Transacion {

    private long id;
    private Vuelo vuelo;
    private String estado;
    private Date fecha;
    private String codigoReserva;
    private Cliente cliente;

    public Transacion(long id, Vuelo vuelo, String estado, Date fecha, String codigoReserva, Cliente cliente) {
        this.id = id;
        this.vuelo = vuelo;
        this.estado = estado;
        this.fecha = fecha;
        this.codigoReserva = codigoReserva;
        this.cliente = cliente;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Vuelo getVuelo() {
        return vuelo;
    }

    public void setVuelo(Vuelo vuelo) {
        this.vuelo = vuelo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getCodigoReserva() {
        return codigoReserva;
    }

    public void setCodigoReserva(String codigoReserva) {
        this.codigoReserva = codigoReserva;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

}
