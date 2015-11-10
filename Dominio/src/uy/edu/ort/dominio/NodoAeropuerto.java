/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uy.edu.ort.dominio;

public class NodoAeropuerto implements Comparable<NodoAeropuerto> {

    Aeropuerto id;
    float tarifa = Integer.MAX_VALUE;
    NodoAeropuerto procedencia = null;

    public NodoAeropuerto(Aeropuerto x, float d, NodoAeropuerto p) {
        id = x;
        tarifa = d;
        procedencia = p;
    }

    public NodoAeropuerto() {
    }

    public NodoAeropuerto(Aeropuerto x) {
        this(x, 0, null);
    }

    @Override
    public int compareTo(NodoAeropuerto tmp) {
        return (int) this.tarifa - (int) tmp.tarifa;
    }

    @Override
    public boolean equals(Object o) {
        NodoAeropuerto tmp = (NodoAeropuerto) o;
        if (tmp.id == this.id) {
            return true;
        }
        return false;
    }
}
