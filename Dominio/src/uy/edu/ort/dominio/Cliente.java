/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uy.edu.ort.dominio;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Renato
 */
public class Cliente extends Usuario {

    private String nombre;
    private String apellido;
    private String tipoDocumento;
    private String numeroDocumento;
    private String paisEmision;
    private Date fechaNacimiento;

    public Cliente(String nombre, String apellido, String tipoDocumento, String numeroDocumento, String paisEmision, Date fechaNacimiento, long id, String user, String pass) {
        super(id, user, pass);
        this.nombre = nombre;
        this.apellido = apellido;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.paisEmision = paisEmision;
        this.fechaNacimiento = fechaNacimiento;
    }

    public Cliente(long id, String user, String pass) {
        super(id, user, pass);
    }

    public Cliente() {
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getPaisEmision() {
        return paisEmision;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public void setPaisEmision(String paisEmision) {
        this.paisEmision = paisEmision;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
}
