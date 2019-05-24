package client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Client {

    private static final String BASE_URL = "http://localhost:8080/dummy_esse3_war_exploded/webresources/";
    private static final String STUDENT = "student";
    private static final String PROFESSOR = "professor";
    private static String username;
    private static String password;

    public static void main(String[] args){
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Client started!");

        String type;

        do{
            System.out.println("Professor or Student? [P/S]");
            type = keyboard.next();
        }while(!type.equals("P") && !type.equals("S"));

        if(type.equals("P")) type = PROFESSOR;
        else type = STUDENT;

        int choice;
        do{
            System.out.println("MENU:\n" +
                                "1. Login\n" +
                                "2. Register");
            choice = keyboard.nextInt();
        }while(choice < 0 || choice > 2);

        if(choice == 1){
            try {
                String response = login(keyboard, type);
                if(response.equals("")) System.out.println("Login successful!");
                else{
                    System.out.println("Error during login! Message: "+response);
                    System.exit(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error during login!");
                System.exit(0);
            }
        }

    }

    private static String login(Scanner keyboard, String type) throws IOException {
        System.out.print("Username: ");
        username = keyboard.next();
        System.out.print("Password: ");
        password = keyboard.next();

        URL url = new URL(BASE_URL+type+"/login/?"+
                            "username="+username+"&password="+password);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        JsonObject response = getResponseAsJSON(connection);
        String status = response.get("status").getAsString();
        if(status.equals("ok")) return "";
        else return response.get("description").getAsString();

    }

    private static JsonObject getResponseAsJSON(HttpURLConnection conn){
        String line;
        StringBuilder response = new StringBuilder();
        try(BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JsonParser().parse(response.toString()).getAsJsonObject();
    }


}
