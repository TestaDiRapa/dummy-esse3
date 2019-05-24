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
import org.bson.Document;
import org.bson.conversions.Bson;
import payloads.ConfirmationPayload;
import payloads.IdentificationPayload;
import payloads.EventPayload;
import payloads.StandardUserPayload;

import static com.mongodb.client.model.Filters.*;

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
     *
     * @return an instance of java.lang.String
     */
    @Path("/")
    @GET
    @Produces("application/json")
    public String getJson() {
        MongoCollection<Document> collection = mongoClient.getDatabase("esse3").getCollection("events");

        String ret = "{\"status\":\"ok\", \"events\":[";

        MongoCursor<Document> results = collection.find().iterator();

        while (results.hasNext()) {
            Document tmp = results.next();
            ret += String.format("{\"id\":\"%s\", \"professor\":\"%s\", \"description\":\"%s\"}",
                    tmp.get("_id"), tmp.get("professor"), tmp.get("description"));
            if (results.hasNext()) {
                ret += ",";
            }
        }

        ret += "]}";

        return ret;
    }

    //Restituisce la descrizione dato l'id
    @GET
    @Path("/{eventID}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String eventDescription(@PathParam("eventID") String eventID) {

        MongoCollection<Document> collection = mongoClient.getDatabase("esse3").getCollection("events");
        MongoCursor<Document> results = collection.find(Filters.eq("_id", eventID)).iterator();

        if (results.hasNext()) {
            return String.format("{\"status\":\"ok\", \"description\":\"%s\"}", results.next().get("description"));
        }
        return "{\"status\":\"error\", \"description\":\"Event ID  doesn't exist\"}";

    }

    //Crea l'event o Modifica
    @PUT
    @Path("/{eventID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    //Quello tra () viene usato nella url mentre dopo il tipo nel codice
    public String update(@PathParam("eventID") String eventID, EventPayload payload) {

        int i = 0;

        if (payload.description != null) {
            i++;
        }
        if (payload.type != null) {
            i++;
        }
        if (payload.date != null) {
            i++;
        }

        if (payload.professor == null) {
            return "{\"status\":\"error\", \"description\":\"username is a mandatory field\"}";
        }
        if (payload.password == null) {
            return "{\"status\":\"error\", \"description\":\"password is a mandatory field\"}";
        }

        MongoCollection<Document> professors = mongoClient.getDatabase("esse3").getCollection("professors");
        MongoCursor<Document> results = professors.find(Filters.eq("username", payload.professor)).iterator();

        MongoCollection<Document> collection = mongoClient.getDatabase("esse3").getCollection("events");
        MongoCursor<Document> result2 = collection.find(and(Filters.eq("_id", eventID), Filters.eq("professor", payload.professor))).iterator();

        if (results.hasNext() && results.next().get("pwd").equals(payload.password)) {
            //QUI SI MODIFICA, Se si modifica si deve fare un controllo se l'evento è del prof
            if (result2.hasNext()) {

                if (i == 0) {
                    return "{\"status\":\"error\", \"description\":\"Event unmodified\"}";
                }
                Bson filter = Filters.eq("_id", eventID);

                if (payload.description != null) {
                    Bson push = Updates.set("description", payload.description);
                    collection.updateOne(filter, push);
                }
                if (payload.type != null) {
                    Bson push = Updates.set("type", payload.type);
                    collection.updateOne(filter, push);
                }
                if (payload.date != null) {
                    Bson push = Updates.set("data", payload.date);
                    collection.updateOne(filter, push);
                }

                return "{\"status\":\"ok\", \"description\":\"Event modified\"}";

            }

            //QUI SI CREA, Si deve associare l'evento al professoere
            MongoCursor<Document> result = collection.find(Filters.eq("_id", eventID)).iterator();
            if (!result.hasNext()) {
                if (i == 3) {

                    Document document = new Document()
                            .append("_id", eventID)
                            .append("type", payload.type)
                            .append("data", payload.date)
                            .append("description", payload.description)
                            .append("professor", payload.professor)
                            .append("participants", students)
                            .append("not_confirmed", studentsToBeConfirmed);

                    collection.insertOne(document);
                    return "{\"status\":\"ok\"}";
                } else {
                    return "{\"status\":\"error\", \"description\":\"Event not created because mandatory field is missing\"}";
                }

            } else {
                return "{\"status\":\"error\", \"description\":\"Event ID already used\"}";
            }

        } else {
            return "{\"status\":\"error\", \"description\":\"Password error\"}";
        }

    }

    //Restituisce i partecipanti dato l'id degli eventi
    @GET
    @Path("{eventID}/participants")
    @Produces("application/json")
    public String participant(@PathParam("eventID") String eventID) {

        MongoCollection<Document> collection = mongoClient.getDatabase("esse3").getCollection("events");
        MongoCursor<Document> results = collection.find(Filters.eq("_id", eventID)).iterator();

        if (results.hasNext()) {
            return String.format("{\"status\":\"ok\", \"participants\":\"%s\"}", results.next().get("participants"));
        }
        return "{\"status\":\"error\", \"description\":\"Event ID  doesn't exist\"}";

    }

    @POST
    @Path("{eventID}/participate")
    @Consumes("application/json")
    @Produces("application/json")
    public String participate(@PathParam("eventID") String eventID, StandardUserPayload payload) {
        if (payload.username == null) {
            return "{\"status\":\"error\", \"description\":\"username is a mandatory field\"}";
        }
        if (payload.pwd == null) {
            return "{\"status\":\"error\", \"description\":\"password is a mandatory field\"}";
        }

        MongoCollection<Document> students = mongoClient.getDatabase("esse3").getCollection("students");
        MongoCursor<Document> results = students.find(Filters.eq("username", payload.username)).iterator();

        if (results.hasNext() && results.next().get("pwd").equals(payload.pwd)) {

            MongoCollection<Document> events = mongoClient.getDatabase("esse3").getCollection("events");

            Bson filter = and(
                    eq("_id", eventID),
                    not(eq("not_confirmed", payload.username)),
                    not(eq("participants", payload.username))
            );
            Bson push = Updates.push("not_confirmed", payload.username);

            if (events.updateOne(filter, push).getModifiedCount() == 0) {
                return "{\"status\":\"error\", \"description\":\"registration error\"}";
            } else {
                return "{\"status\":\"ok\"}";
            }

        } else {
            return "{\"status\":\"error\", \"description\":\"incorrect username or password\"}";
        }
    }

    @POST
    @Path("{eventID}/confirmstudent")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String confirmStudent(@PathParam("eventID") String eventID, ConfirmationPayload payload) {
        if (payload.username == null) {
            return "{\"status\":\"error\", \"description\":\"username is a mandatory field\"}";
        }
        if (payload.pwd == null) {
            return "{\"status\":\"error\", \"description\":\"password is a mandatory field\"}";
        }
        if (payload.student == null) {
            return "{\"status\":\"error\", \"description\":\"student is a mandatory field\"}";
        }

        MongoCollection<Document> professors = mongoClient.getDatabase("esse3").getCollection("professors");
        MongoCursor<Document> results = professors.find(Filters.eq("username", payload.username)).iterator();
        if (results.hasNext() && results.next().get("pwd").equals(payload.pwd)) {

            MongoCollection<Document> events = mongoClient.getDatabase("esse3").getCollection("events");

            Bson filter = and(
                    eq("_id", eventID),
                    eq("professor", payload.username),
                    not(elemMatch("not_confirmed", eq(payload.username)))
            );

            Bson push = Updates.pull("not_confirmed", payload.student);
            Bson pull = Updates.push("participants", payload.student);

            if (events.updateOne(filter, push).getModifiedCount() == 0 || events.updateOne(filter, pull).getModifiedCount() == 0) {
                return "{\"status\":\"error\", \"description\":\"registration error\"}";
            } else {
                return "{\"status\":\"ok\"}";
            }
        } else {
            return "{\"status\":\"error\", \"description\":\"incorrect username or password\"}";
        }
    }

    @DELETE
    @Path("/{eventID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String eventDelete(@PathParam("eventID") String eventID, @QueryParam("username") String username, @QueryParam("password") String pwd) {
        if (username == null) {
            return "{\"status\":\"error\", \"description\":\"username is a mandatory field\"}";
        }
        if (pwd == null) {
            return "{\"status\":\"error\", \"description\":\"password is a mandatory field\"}";
        }

        MongoCollection<Document> professors = mongoClient.getDatabase("esse3").getCollection("professors");
        MongoCursor<Document> results = professors.find(Filters.and(Filters.eq("username", username),
                                                                    Filters.eq("pwd", pwd))).iterator();
        if (results.hasNext()) {
                MongoCollection<Document> collection = mongoClient.getDatabase("esse3").getCollection("events");

            if(collection.deleteOne(and(eq("_id", eventID), eq("professor", username))).getDeletedCount() > 0){
                return "{\"status\":\"ok\"}";
            }
            else return "{\"status\":\"error\", \"description\":\"no event with the specified id or professor\"}";
        }
        else return "{\"status\":\"error\", \"description\":\"incorrect username or password\"}";

    }

    @POST
    @Path("{eventID}/retract")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String retractParticipation(@PathParam("eventID") String eventID,  IdentificationPayload payload){
        if (payload.username == null) {
            return "{\"status\":\"error\", \"description\":\"username is a mandatory field\"}";
        }
        if (payload.pwd == null) {
            return "{\"status\":\"error\", \"description\":\"password is a mandatory field\"}";
        }

        MongoCollection<Document> students = mongoClient.getDatabase("esse3").getCollection("students");
        MongoCursor<Document> results = students.find(Filters.and(Filters.eq("username", payload.username),
                Filters.eq("pwd", payload.pwd))).iterator();

        if(results.hasNext()){
            MongoCollection<Document> events = mongoClient.getDatabase("esse3").getCollection("events");

            Bson filter = and(
                    eq("_id", eventID),
                    or(
                            not(elemMatch("not_confirmed", eq(payload.username))),
                            not(elemMatch("participants", eq(payload.username)))
                    )

            );

            if (events.updateOne(filter, Updates.pull("not_confirmed", payload.username)).getModifiedCount() == 1 ||
                    events.updateOne(filter, Updates.pull("participants", payload.username)).getModifiedCount() == 1) {
                return "{\"status\":\"ok\", \"description\":\"Registration withdrawn successful\"}";
            } else {
                return "{\"status\":\"error\"}";
            }
        }

        return "{\"status\":\"error\", \"description\":\"login error\"}";
    }
}
