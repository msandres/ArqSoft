/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uy.edu.ort.vuelos.ws;

import javax.ejb.EJB;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import org.jboss.logging.Param;
import javax.ws.rs.core.Response;
import uy.edu.ort.vuelos.beans.SessionBeanStatefulLocal;

/**
 * REST Web Service
 *
 * @author Renato
 */
@Path("vuelos")
public class VuelosResource {

    @Context
    private UriInfo context;

    @EJB
    private SessionBeanStatefulLocal sesion;
    /**
     * Creates a new instance of FlightsResource
     */
    public VuelosResource() {
    }

    /**
     * Retrieves representation of an instance of uy.edu.ort.ws.VuelosResource
     * @return an instance of java.lang.String
     */
    
    @GET
    @Produces("application/xml")
    public String getXml() {
        //TODO return proper representation object
        throw new UnsupportedOperationException();
    }

    @GET
    @Produces("application/json")
    @Path("aeropuertos")
    public String getAeropuertos() {
        return sesion.getAeropuertos();
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("vuelosMenorCosto")
    public String vuelosMenorCosto(@FormParam("aeropuertoOrigen") String origen,
            @FormParam("aeropuertoDestino") String destino,
            @FormParam("ida_Vuelta") String idaVuelta,
            @FormParam("fechaIda") String fechaIda,
            @FormParam("fechaVuelta") String fechaVuelta,
            @FormParam("cantPersonas") String cantPersonas) {
        return sesion.vuelos(origen, destino, idaVuelta, fechaIda, fechaVuelta, cantPersonas);
    }
    
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("vuelosSinEscala")
    public String vuelosSinEscala(@FormParam("fecha") String fechaIda,
            @FormParam("aeropuertoOrigen") String origen,
            @FormParam("aeropuertoDestino") String destino){
        return sesion.vuelosSinEscala(fechaIda, origen, destino);
    }
    
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("comprarVuelo")
    public String comprarVuelo(@FormParam("idVuelo") String idVuelo,
            @FormParam("cantPersonas") String cantPersonas) {
        return sesion.comprarVuelo(idVuelo, cantPersonas);
    }
    
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("reservarVuelo")
    public String reservarVuelo(@FormParam("idVuelo") String idVuelo,
            @FormParam("cantPersonas") String cantPersonas) {
        return sesion.reservarVuelo(idVuelo, cantPersonas);
    }
    
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("comprarReserva")
    public String comprarReserva(@FormParam("codigoReserva") String codigo) {
        return sesion.comprarReserva(codigo);
    }
    
    @GET
    @Consumes("application/x-www-form-urlencoded")
    @Path("historialCompras")
    public String historialCompras() {
        return sesion.historialCompras();
    }
    
    /**
     * PUT method for updating or creating an instance of VuelosResource
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/xml")
    public void putXml(String content) {
    }
}
