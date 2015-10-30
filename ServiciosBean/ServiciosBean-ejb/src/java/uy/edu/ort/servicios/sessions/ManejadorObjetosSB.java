/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uy.edu.ort.servicios.sessions;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import uy.edu.ort.dominio.Aeropuerto;
import uy.edu.ort.servicios.persistencia.AeropuertoEntity;

/**
 *
 * @author user
 */
@Stateless
public class ManejadorObjetosSB implements ManejadorObjetosSBRemote {
    @PersistenceContext(unitName = "PersistenciaObjetosPU")
    private EntityManager em;

    

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")

    @Override
    public void alta(Aeropuerto aeropuerto) {
    AeropuertoEntity aeropuertoEntity= new AeropuertoEntity();
    aeropuertoEntity.setId(aeropuerto.getId());
    aeropuertoEntity.setNombre(aeropuerto.getNombre());
    aeropuertoEntity.setCodigoAeropuerto(aeropuerto.getCodigoAeropuerto());
    aeropuertoEntity.setCiudad(aeropuerto.getCiudad());
    aeropuertoEntity.setPais(aeropuerto.getPais());
    aeropuertoEntity.setLatitud(aeropuerto.getLatitud());
    aeropuertoEntity.setLongitud(aeropuerto.getLongitud());
    em.persist(aeropuertoEntity);
    }
    
}
