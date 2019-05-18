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
    private List<String> students = new ArrayList<>(), studentsToBeConfirmed = new ArrayList<>();

    
    /**
     * Creates a new instance of Event
     */
    public Event() {
        
    }

     //Mostra eventi
    /**
     * Retrieves representation of an instance of event.Event
     * @return an instance of java.lang.String
     */
   
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
    
    
    //Restituisce la descrizione dato l'id
    @GET
    @Path ("/{eventID}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})    
    public String eventDescription(@PathParam("eventID") String eventID){
        
        MongoCollection<Document> collection = mongoClient.getDatabase("esse3").getCollection("events");
        MongoCursor<Document> results = collection.find(Filters.eq("_id", eventID)).iterator();
        
        if (results.hasNext()){
            return String.format("{\"status\":\"ok\", \"description\":\"%s\"}", results.next().get("description"));
        }
       return "{\"status\":\"error\", \"description\":\"Event ID  doesn't exist\"}"; 
       
    }
    
    //Crea l'event o Modifica
    @PUT
    @Path ("/{eventID}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    //Quello tra () viene usato nella url mentre dopo il tipo nel codice
    public String update(@PathParam("eventID") String eventID, @QueryParam("description") String de, @QueryParam("type") String t, @QueryParam("data")  String da, @QueryParam("professor") String prof) {
       
        int i = 0;

        if(de != null) i++;
        if(t != null) i++;
        if(da != null) i++;
        if(prof != null) i++;
        
        MongoCollection<Document> collection = mongoClient.getDatabase("esse3").getCollection("events");
        MongoCursor<Document> result = collection.find(Filters.eq("_id", eventID)).iterator();
        
        
        //QUI SI MODIFICA
        if(result.hasNext()){
      
           if(i==0)  return "{\"status\":\"error\", \"description\":\"Event unmodified\"}";
            Bson filter = Filters.eq("_id", eventID);
            
            
            if(de!=null){ 
            Bson push = Updates.push("description", de);
            collection.updateOne(filter, push);
            }
            if(t!=null){ 
            Bson push = Updates.push("type", t);
            collection.updateOne(filter, push);
            }
            if(da!=null){ 
            Bson push = Updates.push("data", da);
            collection.updateOne(filter, push);
            }
       
            return "{\"status\":\"error\", \"description\":\"Event modified\"}";
        }
        
        
        //QUI SI CREA
        if(i==4){
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
        return "{\"status\":\"error\", \"description\":\"Event not creat because mandatory field\"}";
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
