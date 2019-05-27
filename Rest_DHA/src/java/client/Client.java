package client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Client {

    private static final String BASE_URL = "http://localhost:8080/Rest_DHA/webresources/";
    private static final String STUDENT = "student";
    private static final String PROFESSOR = "professor";
    private static String username;
    private static String password;
    private static String type;

    public static void main(String[] args) {
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Client started!");

        do {
            System.out.println("Professor or Student? [P/S]");
            type = keyboard.next();
        } while (!type.equals("P") && !type.equals("S"));

        if (type.equals("P")) {
            type = PROFESSOR;
        } else {
            type = STUDENT;
        }

        int choice;
        boolean continua = false;
        do {
            System.out.println("MENU:\n"
                    + "1. Login\n"
                    + "2. Register\n"
                    + "3. Exit");
            
            choice = keyboard.nextInt();

            if (choice == 1) {
                try {
                    String response = login(keyboard);
                    if (response.equals("")) {
                        System.out.println("Login successful!");
                        continua = true;
                    } else {
                        System.out.println("Error during login! Message: " + response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error during login!");
                }
            } else if (choice == 2) {
                try {
                    String response = register(keyboard);
                    if (response.equals("")) {
                        System.out.println("Registration successful!");
                        continua = true;
                    } else {
                        System.out.println("Error during registration! Message: " + response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error during registration!");
                }
            } else if (choice == 3) {
                System.exit(0);
            }
        } while (!continua);

        //PROFESSOR
        if (type.equals(PROFESSOR)) {
            do {
                System.out.println("\nUsername:" + username + "\n"
                        + "MENU:\n"
                        + "1. Add event\n"
                        + "2. View events\n"
                        + "3. Delete an event\n"
                        + "4. Modify an event\n"
                        + "5. View participants of an event\n"
                        + "6. Confirm participant\n"
                        + "7. Exit");
                choice = keyboard.nextInt();

                if (choice == 1 || choice == 4) {
                    try {
                        String response = addEvent(keyboard);
                        if (response.equals("")) {
                            if (choice == 1) {
                                System.out.println("Event added!");
                            } else {
                                System.out.println("Event Modified");
                            }
                        } else {
                            System.out.println("Error during the creation of event! Message: " + response);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Error during creation of event!");
                    }

                }
                if (choice == 2) {
                    try {
                        viewEvents();
                    } catch (IOException e) {
                        System.out.println("Error");
                    }
                }
                if (choice == 3) {
                    try {
                        String response = deleteEvent(keyboard);
                        if (response.equals("")) {
                            System.out.println("Event deleted!");
                        } else {
                            System.out.println("Error during the deletion of event! Message: " + response);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Error during the deletion of event");
                    }
                }

                if (choice == 5) {
                    try {
                        String response = participant(keyboard);
                         System.out.println( response);
                        
                    } catch (IOException e) {
                        e.printStackTrace();
                       
                    }
                }
                if (choice == 6) {
                    try {
                        String response = confirmParticipant(keyboard);
                        if (response.equals("")) {
                            System.out.println("Student confirmed!");
                        } else {
                            System.out.println("Error during the confirmation! Message: " + response);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Error during the confirmation");
                    }
                }
                if (choice == 7) {
                    System.exit(0);
                }

            } while (true);
        } //STUDENT
        else {
            do {
                System.out.println("\nUsername:" + username + "\n"
                        + "MENU:\n"
                        + "1. View list of professors\n"
                        + "2. View events\n"
                        + "3. View description of an event\n"
                        + "4. Retract participation\n"
                        + "5. Request Participation\n"
                        + "6. Exit");
                choice = keyboard.nextInt();

                if (choice == 1) {
                    try {
                        viewProfessors();
                    } catch (IOException e) {
                        System.out.println("Error!");
                    }
                }
                else if (choice == 2) {
                    try {
                        viewEvents();
                    } catch (IOException e) {
                        System.out.println("Error!");
                    }
                }
                else if (choice == 3) {
                    System.out.print("Insert event ID: ");
                    String eventID = keyboard.next();
                    try {
                        viewEventDescription(eventID);
                    } catch (IOException e) {
                        System.out.println("Error!");
                    }

                }
                else if (choice == 4) {
                    System.out.print("Insert event ID: ");
                    String eventID = keyboard.next();
                    try {
                        retractParticipation(eventID);
                    } catch (IOException e) {
                        System.out.println("Error!");
                    }
                }
                else if (choice == 5) {
                    System.out.print("Insert event ID: ");
                    String eventID = keyboard.next();
                    try {
                        participate(eventID);
                    } catch (IOException e) {
                        System.out.println("Error!");
                    }
                }
                else if (choice == 6) {
                    System.exit(0);
                }
            } while (choice > 0 || choice < 6);
        }

    }

    private static void printArrayOfJson(JsonArray array) {
        for(JsonElement el : array){
            for(String key : el.getAsJsonObject().keySet()){
                if(!key.equals("pwd")) System.out.println(key + ": "+el.getAsJsonObject().get(key)+"\n");
            }
        }

    }

    private static void retractParticipation(String eventID) throws IOException {
        String payload = "{\"username\":\""+username+"\", \"pwd\":\""+password+"\"}";
        URL url = new URL(BASE_URL + "event/" + eventID + "/retract");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
        writer.write(payload);
        writer.close();

        JsonObject response = getResponseAsJSON(connection);
        String status = response.get("status").getAsString();
        if(status.equals("ok")){
            System.out.println("Registration retracted successfully!");
        }
        else System.out.println("Error in retracting!");
    }

    private static void participate(String eventID) throws IOException {
        String payload = "{\"username\":\""+username+"\", \"pwd\":\""+password+"\"}";
        URL url = new URL(BASE_URL + "event/" + eventID + "/participate");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
        writer.write(payload);
        writer.close();

        JsonObject response = getResponseAsJSON(connection);
        String status = response.get("status").getAsString();
        if(status.equals("ok")){
            System.out.println("Registration successful!");
        }
        else System.out.println(response.get("description").getAsString());
    }

    private static void viewEventDescription(String eventID) throws IOException {
        URL url = new URL(BASE_URL + "event/"+eventID);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        JsonObject response = getResponseAsJSON(connection);
        String status = response.get("status").getAsString();
        if(status.equals("ok")) {
            System.out.println("Event ID: " + eventID + "\nDescription: "+response.get("description").getAsString());
        }
        else {
            System.out.println("Error: "+response.get("description").getAsString());
        }
    }

    private static void viewProfessors() throws IOException {
        URL url = new URL(BASE_URL + "professor/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        JsonObject response = getResponseAsJSON(connection);
        String status = response.get("status").getAsString();
        if(status.equals("ok")){
            printArrayOfJson(response.get("results").getAsJsonArray());
        }
        else System.out.println("Error!");

    }

    private static String login(Scanner keyboard) throws IOException {
        System.out.print("Username: ");
        username = keyboard.next();
        System.out.print("Password: ");
        password = keyboard.next();

        URL url = new URL(BASE_URL + type + "/login/?"
                + "username=" + username + "&password=" + password);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        JsonObject response = getResponseAsJSON(connection);
        String status = response.get("status").getAsString();
        if (status.equals("ok")) {
            return "";
        } else {
            return response.get("description").getAsString();
        }

    }

    private static JsonObject getResponseAsJSON(HttpURLConnection conn) {
        String line;
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JsonParser().parse(response.toString()).getAsJsonObject();
    }

    private static String register(Scanner keyboard) throws IOException {
        String name, surname;
        System.out.print("Name: ");
        name = keyboard.next();
        System.out.print("Surname: ");
        surname = keyboard.next();
        System.out.print("Username: ");
        username = keyboard.next();
        System.out.print("Password: ");
        password = keyboard.next();
        String payload = "{\"name\":\"" + name + "\",\"surname\":\"" + surname + "\",\"username\":\"" + username + "\",\"pwd\":\"" + password + "\"}";

        try {
            URL url = new URL(BASE_URL + type + "/register");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writer.write(payload);
            writer.close();
            JsonObject response = getResponseAsJSON(connection);

            String status = response.get("status").getAsString();
            if (status.equals("ok")) {
                return "";
            } else {
                return response.get("description").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error";
    }

    private static String addEvent(Scanner keyboard) throws IOException {

        String eventID, description, typeEvent, date;
        System.out.print("Id Event: ");
        eventID = keyboard.next();
        System.out.print("Date: ");
        date = keyboard.next();
        System.out.print("Type: ");
        typeEvent = keyboard.next();
        System.out.print("Description: ");
        description = keyboard.next();
        String payload = "{\"professor\":\"" + username + "\",\"password\":\"" + password + "\",\"date\":\"" + date
                + "\",\"type\":\"" + typeEvent + "\",\"description\":\"" + description + "\"}";

        try {
            URL url = new URL(BASE_URL + "event/" + eventID);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writer.write(payload);
            writer.close();
            JsonObject response = getResponseAsJSON(connection);

            String status = response.get("status").getAsString();
            if (status.equals("ok")) {
                return "";
            } else {
                return response.get("description").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error";

    }

    private static void viewEvents() throws IOException {

        URL url = new URL(BASE_URL + "event");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        JsonObject response = getResponseAsJSON(connection);

        if(response.get("status").getAsString().equals("ok")){
            printArrayOfJson(response.get("events").getAsJsonArray());
        }
        else System.out.println("Error in getting the events!");
    }

    private static String deleteEvent(Scanner keyboard) throws IOException {
        String event;
        System.out.println("Event Id: ");
        event = keyboard.next();

        try {
            URL url = new URL(BASE_URL + "event/" + event + "?username=" + username + "&"
                    + "password=" + password);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            JsonObject response = getResponseAsJSON(connection);

            String status = response.get("status").getAsString();
            if (status.equals("ok")) {
                return "";
            } else {
                return response.get("description").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error";
    }

    private static String participant(Scanner keyboard) throws IOException {

        String event;
        System.out.print("Id Event: ");
        event = keyboard.next();
        URL url = new URL(BASE_URL + "event/" + event + "/participants");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        JsonObject response = getResponseAsJSON(connection);
      
        String s = response.get("participants").getAsString();
        
        String status = response.get("status").getAsString();
        if (status.equals("ok")) {
            return s;
        } else {
            return response.get("description").getAsString();
        }
    }

    private static String confirmParticipant(Scanner keyboard) throws IOException {
        String event, student;
        System.out.print("Id Event: ");
        event = keyboard.next();
        System.out.print("Student: ");
        student = keyboard.next();
        String payload;
        payload = "{\"username\":\"" + username + "\",\"pwd\":\"" + password + "\", \"student\":\"" + student + "\"}";

        try {
            URL url = new URL(BASE_URL + "event/" + event + "/confirmstudent");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writer.write(payload);
            writer.close();
            JsonObject response = getResponseAsJSON(connection);

            String status = response.get("status").getAsString();
            if (status.equals("ok")) {
                return "";
            } else {
                return response.get("description").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error";
    }

}
