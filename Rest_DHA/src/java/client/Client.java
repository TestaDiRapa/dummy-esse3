package client;

import java.util.Scanner;

public class Client {

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

        int choice;
        do{
            System.out.println("MENU:\n" +
                                "1. Login\n" +
                                "2. Register");
            choice = keyboard.nextInt();
        }while(choice < 0 || choice > 2);


    }

    private static boolean login(Scanner keyboard, String type){
        System.out.print("Username: ");
        username = keyboard.next();
        System.out.print("Password: ");
        password = keyboard.next();

        if(type.equals("S")){
            
        }
    }

}
