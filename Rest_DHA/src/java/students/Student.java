/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package students;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import org.bson.Document;
import payloads.StandardUserPayload;

/**
 * REST Web Service
 *
 * @author Alex1
 */
@Path("student")
public class Student {

    public MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://dhagroup:glassfish@45.76.47.94/esse3"));
    
    @Context
    private UriInfo context;

    /**
     * Creates a new instance of Student
     */
    public Student() {
    }

    /**
     * Retrieves representation of an instance of students.Student
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson() {
        //TODO return proper representation object
        throw new UnsupportedOperationException();
    }

    @PUT
    @Path("register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String register(StandardUserPayload payload){
        
        if(payload.name == null) return "{\"status\":\"error\", \"description\":\"name is a mandatory field\"}";
        if(payload.surname == null) return "{\"status\":\"error\", \"description\":\"surname is a mandatory field\"}";
        if(payload.username == null) return "{\"status\":\"error\", \"description\":\"username is a mandatory field\"}";
        if(payload.pwd == null) return "{\"status\":\"error\", \"description\":\"password is a mandatory field\"}";
        
        MongoCollection<Document> collection = mongoClient.getDatabase("esse3").getCollection("students");
        
        MongoCursor<Document> result = collection.find(Filters.eq("username", payload.username)).iterator();
        if(result.hasNext()) return "{\"status\":\"error\", \"description\":\"Username already exists\"}";
        
        Document documento = new Document()
                .append("name", payload.name)
                .append("surname", payload.surname)
                .append("username", payload.username)
                .append("pwd", payload.pwd);
        
        collection.insertOne(documento);
        return "{\"status\":\"ok\"}";
        
    }
    
    
}
