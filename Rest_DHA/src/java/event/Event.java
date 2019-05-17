/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package event;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import org.bson.Document;


/**
 * REST Web Service
 *
 * @author AntonioBho
 */
@Path("event")
public class Event {

    
    public MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://dhagroup:glassfish@45.76.47.94/esse3"));

    @Context
    private UriInfo context;
    private String type;
    private String professor;
    private String description;
    private List<String> students;
    private String data;
    private String id; //data+description
    
    /**
     * Creates a new instance of Event
     */
    public Event() {
        
    }

    /**
     * Retrieves representation of an instance of event.Event
     * @return an instance of java.lang.String
     */
    //Mostra eventi
    @GET
    @Produces("application/json")
    public String getJson() {
        
        //Bisogna fare un'iterazione
//        MongoCollection<Document> collection = mongoClient.getDatabase("esse3").getCollection("events");
//        MongoCursor<Document> result =  
        String pattern= "{ \"professor\":\"%s\", \"description\":\"%s\"}";
        return String.format(pattern,professor,description);
    }
    
    //Crea l'event o Modifica
    @PUT
    @Path ("/{eventID}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    //Quello tra () viene usato nella url mentre dopo il tipo nel codice
    public String update(@PathParam("eventID") String eventID, @QueryParam("description") String de, @QueryParam("type") String t, @QueryParam("data")  String da, @QueryParam("professor") String prof) {
        if(de == null) return "{\"status\":\"error\", \"description\":\"description is a mandatory field\"}";
        if(t == null) return "{\"status\":\"error\", \"description\":\"type is a mandatory field\"}";
        if(da == null) return "{\"status\":\"error\", \"description\":\"data is a mandatory field\"}";
        if(prof == null) return "{\"status\":\"error\", \"description\":\"professor is a mandatory field\"}";
        
        MongoCollection<Document> collection = mongoClient.getDatabase("esse3").getCollection("events");
        MongoCursor<Document> result = collection.find(Filters.eq("eventID", eventID)).iterator();
        if(result.hasNext()) return "{\"status\":\"error\", \"description\":\"Event ID  already exists\"}";

        Document document = new Document()
                .append("eventID", eventID)
                .append("type", t)
                .append("data", da)
                .append("description", de)
                .append("professor", prof);

        collection.insertOne(document);
        return "{\"status\":\"ok\"}";  
}

    //Restituisce i partecipanti
    @GET
    @Path("/participants")
    @Produces("application/json")
    public String participant(){
        return String.format(" %s ", students);
       
    }
    
    
  
}
