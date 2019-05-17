package professors;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import professors.payloads.ProfessorRegistrationPayload;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("professor")
public class Professor {

    public MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://dhagroup:glassfish@45.76.47.94/esse3"));

    @PUT
    @Path("register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String register(ProfessorRegistrationPayload payload){
        if(payload.name == null) return "{\"status\":\"error\", \"description\":\"name is a mandatory field\"}";
        if(payload.surname == null) return "{\"status\":\"error\", \"description\":\"surname is a mandatory field\"}";
        if(payload.username == null) return "{\"status\":\"error\", \"description\":\"username is a mandatory field\"}";
        if(payload.pwd == null) return "{\"status\":\"error\", \"description\":\"password is a mandatory field\"}";

        MongoCollection<Document> collection = mongoClient.getDatabase("esse3").getCollection("professors");

        MongoCursor<Document> result = collection.find(Filters.eq("username", payload.username)).iterator();
        if(result.hasNext()) return "{\"status\":\"error\", \"description\":\"Username already exists\"}";

        Document document = new Document()
                .append("name", payload.name)
                .append("surname", payload.surname)
                .append("username", payload.username)
                .append("pwd", payload.pwd);

        collection.insertOne(document);
        return "{\"status\":\"ok\"}";
    }

}
