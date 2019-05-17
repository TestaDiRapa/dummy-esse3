/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package event;

import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.*;


/**
 * REST Web Service
 *
 * @author AntonioBho
 */
@Path("event")
public class Event {

    @Context
    private UriInfo context;
    private String type;
    private String professor;
    private String description;
    private List<String> students;
    private String data;
    
    /**
     * Creates a new instance of Event
     */
    public Event() {
        
    }

    /**
     * Retrieves representation of an instance of event.Event
     * @return an instance of java.lang.String
     */
    //Mostra l'evento
    @GET
    @Produces("application/json")
    public String getJson() {
        String pattern= "{ \"professor\":\"%s\", \"description\":\"%s\"}";
        return String.format(pattern,professor,description);
    }
    
    //Modifica o Crea l'event
    @PUT
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    //Quello tra () viene usato nella url mentre dopo il tipo nel codice
    public String update(@QueryParam("description") String de, @QueryParam("type") String t, @QueryParam("data")  String da, @QueryParam("professor") String prof) {
        description=de;
        type=t;
        data=da;
        professor=prof;
        String pattern = "{\"status\":\"%s\", \"professor\":\"%s\", \"description\":\"%s\", \"type\":\"%s\", \"data\": \"%s\"}";
        return String.format(pattern,"OK",professor, description, type, data );   
}

    //Restituisce i partecipanti
    @GET
    @Path("/participants")
    @Produces("application/json")
    public String participant(){
        return String.format(" %s ", students);
       
    }
    
    
  
}
