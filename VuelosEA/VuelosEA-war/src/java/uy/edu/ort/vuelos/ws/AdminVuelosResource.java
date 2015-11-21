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
import uy.edu.ort.vuelos.beans.SessionBeanStatefulLocal;

/**
 * REST Web Service
 *
 * @author Renato
 */
@Path("adminVuelos")
public class AdminVuelosResource {

    @Context
    private UriInfo context;

    @EJB
    private SessionBeanStatefulLocal sesion;

    /**
     * Creates a new instance of AdminVuelosResource
     */
    public AdminVuelosResource() {
    }

    /**
     * Retrieves representation of an instance of
     * uy.edu.ort.vuelos.ws.AdminVuelosResource
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/xml")
    public String getXml() {
        //TODO return proper representation object
        throw new UnsupportedOperationException();
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("altaAerolinea")
    public String altaAerolinea(@FormParam("nombre") String nombre,
            @FormParam("codigoAerolinea") String codigoAerolinea,
            @FormParam("informacion") String informacion) {
        return sesion.altaAerolinea(nombre, codigoAerolinea, informacion);
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("bajaAerolinea")
    public String bajaAerolinea(@FormParam("codigoAerolinea") String codigoAerolinea) {
        return sesion.bajaAerolinea(codigoAerolinea);
    }

    @Consumes("application/x-www-form-urlencoded")
    @Path("modificarAerolinea")
    public String modificarAerolinea(@FormParam("codigoAerolinea") String codigoAerolinea,
            @FormParam("nuevoNombre") String nuevoNombre,
            @FormParam("nuevoCodigoAerolinea") String nuevoCodigoAerolinea,
            @FormParam("nuevaInformacion") String nuevaInformacion) {
        return sesion.modificarAerolinea(codigoAerolinea, nuevoNombre, nuevoCodigoAerolinea, nuevaInformacion);
    }

    /**
     * PUT method for updating or creating an instance of AdminVuelosResource
     *
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/xml")
    public void putXml(String content) {
    }
}
