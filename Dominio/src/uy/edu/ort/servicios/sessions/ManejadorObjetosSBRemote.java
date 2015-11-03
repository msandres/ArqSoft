/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uy.edu.ort.servicios.sessions;

import javax.ejb.Remote;
import uy.edu.ort.dominio.Aeropuerto;

/**
 *
 * @author user
 */
@Remote
public interface ManejadorObjetosSBRemote {
 public void alta(Aeropuerto aeropuerto);
}
