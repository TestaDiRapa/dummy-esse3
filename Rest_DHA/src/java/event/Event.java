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

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.mongodb.client.model.Updates;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import payloads.ConfirmationPayload;
import payloads.StandardUserPayload;


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
//    private String type;
//    private String professor;
//    private String description;
    private List<String> students = new ArrayList<>(), studentsToBeConfirmed = new ArrayList<>();
//    private String data;
//    private String id; 
    
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
        MongoCollection<Document> collection = mongoClient.getDatabase("esse3").getCollection("events");

        String ret = "{\"status\":\"ok\", \"events\":[";

        MongoCursor<Document> results = collection.find().iterator();

        while(results.hasNext()){
            Document tmp = results.next();
            ret += String.format("{\"id\":\"%s\", \"professor\":\"%s\", \"description\":\"%s\"}",
                    tmp.get("_id"), tmp.get("professor"), tmp.get("description"));
            if(results.hasNext()) ret += ",";
        }

        ret += "]}";

        return ret;
    }
    
    //Crea l'event o Modifica
    @PUT
    @Path ("/{eventID}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    //Quello tra () viene usato nella url mentre dopo il tipo nel codice
    public String update(@PathParam("eventID") String eventID, @QueryParam("description") String de, @QueryParam("type") String t, @QueryParam("data")  String da, @QueryParam("professor") String prof) {
       
        
        MongoCollection<Document> collection = mongoClient.getDatabase("esse3").getCollection("events");
        MongoCursor<Document> result = collection.find(Filters.eq("eventID", eventID)).iterator();
        
        
        //QUI SI MODIFICA
        if(result.hasNext()){
            
            
            return "{\"status\":\"error\", \"description\":\"Event ID  already exists\"}";
        }
        
        
        //QUI SI CREA
        if(de == null) return "{\"status\":\"error\", \"description\":\"description is a mandatory field\"}";
        if(t == null) return "{\"status\":\"error\", \"description\":\"type is a mandatory field\"}";
        if(da == null) return "{\"status\":\"error\", \"description\":\"data is a mandatory field\"}";
        if(prof == null) return "{\"status\":\"error\", \"description\":\"professor is a mandatory field\"}";
        Document document = new Document()
                .append("_id", eventID)
                .append("type", t)
                .append("data", da)
                .append("description", de)
                .append("professor", prof)
                .append("participants", students)
                .append("not_confirmed", studentsToBeConfirmed);

        collection.insertOne(document);
        return "{\"status\":\"ok\"}";  
}

    //Restituisce i partecipanti dato l'id degli eventi
    @GET
    @Path("{eventID}/participants")
    @Produces("application/json")
    public String participant(@PathParam("eventID") String eventID){
        
        MongoCollection<Document> collection = mongoClient.getDatabase("esse3").getCollection("events");
        MongoCursor<Document> results = collection.find(Filters.eq("_id", eventID)).iterator();
        
        if (results.hasNext()){
            return String.format("{\"status\":\"ok\", \"participants\":\"%s\"}", results.next().get("participants"));
        }
       return "{\"status\":\"error\", \"description\":\"Event ID  doesn't exist\"}"; 
       
    }

    
    
    @POST
    @Path("{eventID}/participate")
    @Consumes("application/json")
    @Produces("application/json")
    public String participate(@PathParam("eventID") String eventID, StandardUserPayload payload){
        if(payload.username == null) return "{\"status\":\"error\", \"description\":\"username is a mandatory field\"}";
        if(payload.pwd == null) return "{\"status\":\"error\", \"description\":\"password is a mandatory field\"}";

        MongoCollection<Document> students = mongoClient.getDatabase("esse3").getCollection("students");
        MongoCursor<Document> results = students.find(Filters.eq("username", payload.username)).iterator();

        if(results.hasNext() && results.next().get("pwd").equals(payload.pwd)){

            MongoCollection<Document> events = mongoClient.getDatabase("esse3").getCollection("events");

            Bson filter = Filters.eq("_id", eventID);
            Bson push = Updates.push("not_confirmed", payload.username);

            if(events.updateOne(filter, push).getModifiedCount() == 0){
                return "{\"status\":\"error\", \"description\":\"no event with the specified id\"}";
            }
            else return "{\"status\":\"ok\"}";

        }
        else return "{\"status\":\"error\", \"description\":\"incorrect username or password\"}";
    }

    @POST
    @Path("{eventID}/confirmstudent")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String confirmStudent(@PathParam("eventID") String eventID, ConfirmationPayload payload){
        if(payload.username == null) return "{\"status\":\"error\", \"description\":\"username is a mandatory field\"}";
        if(payload.pwd == null) return "{\"status\":\"error\", \"description\":\"password is a mandatory field\"}";
        if(payload.student == null) return "{\"status\":\"error\", \"description\":\"student is a mandatory field\"}";

        MongoCollection<Document> professors = mongoClient.getDatabase("esse3").getCollection("professors");
        MongoCursor<Document> results = professors.find(Filters.eq("username", payload.username)).iterator();
        if(results.hasNext() && results.next().get("pwd").equals(payload.pwd)){

            MongoCollection<Document> events = mongoClient.getDatabase("esse3").getCollection("events");


            BsonDocument filter = new BsonDocument();
            filter.append("_id", new BsonString(eventID));
            filter.append("professor", new BsonString(payload.username));

            Bson push = Updates.push("not_confirmed", payload.student);
            Bson pull = Updates.pull("participants", payload.student);

            if(events.updateOne(filter, push).getModifiedCount() == 0 || events.updateOne(filter, pull).getModifiedCount() == 0){
                return "{\"status\":\"error\", \"description\":\"no event with the specified id or professor\"}";
            }
            else return "{\"status\":\"ok\"}";
        }
        else return "{\"status\":\"error\", \"description\":\"incorrect username or password\"}";
    }
}
