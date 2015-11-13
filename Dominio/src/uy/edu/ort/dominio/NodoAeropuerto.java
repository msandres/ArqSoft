/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uy.edu.ort.dominio;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NodoAeropuerto implements Comparable<NodoAeropuerto> {

    private Aeropuerto aeropuerto;
    private float tarifa;
    private Date duracion;
    private Vuelo vuelo;
    private NodoAeropuerto procedencia;

    public NodoAeropuerto(Aeropuerto aeropuerto, float tarifa, Date duracion, 
            Vuelo vuelo, NodoAeropuerto procedencia) {
        this.aeropuerto = aeropuerto;
        this.tarifa = tarifa;
        this.duracion = duracion;
        this.vuelo = vuelo;
        this.procedencia = procedencia;
    }
    
    public NodoAeropuerto(Aeropuerto aeropuerto) {
        try {
            Vuelo v = new Vuelo(aeropuerto);
            this.aeropuerto=aeropuerto;
            SimpleDateFormat formatHora = new SimpleDateFormat("kk:mm");
            this.duracion = formatHora.parse("00:00");
            this.tarifa = Integer.MAX_VALUE;
            this.vuelo = v;
            this.procedencia = null;
        } catch (ParseException ex) {
            Logger.getLogger(NodoAeropuerto.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Aeropuerto getAeropuerto() {
        return aeropuerto;
    }

    public float getTarifa() {
        return tarifa;
    }

    public Date getDuracion() {
        return duracion;
    }

    public Vuelo getVuelo() {
        return vuelo;
    }

    public NodoAeropuerto getProcedencia() {
        return procedencia;
    }

    @Override
    public int compareTo(NodoAeropuerto nodoAeropuerto) {
        return (int) this.tarifa - (int) nodoAeropuerto.tarifa;
    }
 
    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NodoAeropuerto other = (NodoAeropuerto) obj;
        return Objects.equals(this.aeropuerto, other.aeropuerto);
    }
}
