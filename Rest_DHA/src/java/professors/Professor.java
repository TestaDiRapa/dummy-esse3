package professors;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import payloads.IdentificationPayload;
import payloads.StandardUserPayload;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Path("professor")
public class Professor {

    public MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://dhagroup:glassfish@45.76.47.94/esse3"));

    @PUT
    @Path("register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String register(StandardUserPayload payload){
        if(payload.name == null) return "{\"status\":\"error\", \"description\":\"name is a mandatory field\"}";
        if(payload.surname == null) return "{\"status\":\"error\", \"description\":\"surname is a mandatory field\"}";
        if(payload.username == null) return "{\"status\":\"error\", \"description\":\"username is a mandatory field\"}";
        if(payload.pwd == null) return "{\"status\":\"error\", \"description\":\"password is a mandatory field\"}";

        MongoCollection<Document> collection = mongoClient.getDatabase("esse3").getCollection("professors");

        MongoCursor<Document> result = collection.find(eq("username", payload.username)).iterator();
        if(result.hasNext()) return "{\"status\":\"error\", \"description\":\"Username already exists\"}";

        Document document = new Document()
                .append("name", payload.name)
                .append("surname", payload.surname)
                .append("username", payload.username)
                .append("pwd", payload.pwd);

        collection.insertOne(document);
        return "{\"status\":\"ok\"}";
    }

    @GET
    @Path("{username}/myevents")
    @Produces(MediaType.APPLICATION_JSON)
    public String myEvents(@PathParam("username") String username, @QueryParam("password") String password){
        if(username == null) return "{\"status\":\"error\", \"description\":\"username is a mandatory field\"}";
        if(password == null) return "{\"status\":\"error\", \"description\":\"password is a mandatory field\"}";

        MongoCollection<Document> professors = mongoClient.getDatabase("esse3").getCollection("professors");

        MongoCursor<Document> results = professors.find(eq("username", username)).iterator();
        if(results.hasNext() && results.next().get("pwd").equals(password)){
            MongoCollection<Document> events = mongoClient.getDatabase("esse3").getCollection("events");
            String ret = "{\"status\":\"ok\", \"results\":[";
            results = events.find(eq("professor", username)).iterator();
            while(results.hasNext()){
                ret += results.next().toString();
                if(results.hasNext()) ret+= ',';
            }
            ret += "]}";
            return ret;
        }
        else return "{\"status\":\"error\", \"description\":\"incorrect username or password\"}";
    }

    @GET
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String login(@QueryParam("username") String username, @QueryParam("password") String pwd){
        if(username == null) return "{\"status\":\"error\", \"description\":\"username is a mandatory field\"}";
        if(pwd == null) return "{\"status\":\"error\", \"description\":\"password is a mandatory field\"}";
        MongoCollection<Document> professors = mongoClient.getDatabase("esse3").getCollection("professors");

        MongoCursor<Document> results = professors.find(and(eq("username", username),
                                                            eq("pwd", pwd))).iterator();

        if(results.hasNext()) return "{\"status\":\"ok\"}";
        else return "{\"status\":\"error\", \"description\":\"Incorrect username or password!\"}";
    }

}
