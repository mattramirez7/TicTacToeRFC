import java.util.*;
import java.io.*;
import java.net.*;

public class Client {
    String sessionID;
    String clientIdentifier;
    String symbol;
    Boolean terminated;

    public static void main(String...args){
        String host="localhost";
        int port=3116;
        String protocolVersion = "1";
        terminated=false;

        Scanner scanner = new Scanner(System.in);

        System.out.print("TCP or UDP: ");
        String service = scanner.nextLine();

        System.out.print("Enter your Client Identifier: ");
        String clientId = scanner.nextLine();

        System.out.print("Enter Version: ");
        String version = scanner.nextLine();

        if (service.equalsIgnoreCase("TCP")) {
            try {
            // Establish TCP connection
            Socket socket = new Socket(host, port);
            System.out.println("TCP connection established");

            // Create input and output streams for the socket
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Send greeting message
            out.println("HELO " + version + " " + clientId);

            // String acknowledgment = in.readLine();
            // System.out.println("Received acknowledgment from the server: " + acknowledgment);

            // Receive acknowledgment
            while(!gameTermined){
                String[] response=in.readLine().split(" ");
                String message= handleResponse(response);
                if(!gameTerminated){
                    out.println(message);
                }
            }

            // System.out.print("Would u like to create a game or join an existing game?");
            // String action = scanner.nextLine();

            

            // Close the TCP connection
            socket.close();
            System.out.println("TCP connection closed.");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

        } else if (service.equalsIgnoreCase("UDP")) {
            
        } else {
            System.out.println("Invalid service. Please choose TCP or UDP.");
        }

        public static String handleResponse(String[] response){
            String action=response[0];
            String request="";
            if(action.equals("SESS")){ //emily
                request=getSession(response);
            } else if (action.equals("BORD")){ // audrey
                request=getJOHD(response);
            } else if (action.equals("GAMS")){ // emily
                request=getJOHD(response);
            } else if (action.equals("GDBY")){ // audrey print goodbye close socket, update boolean
                request=getJOHD(response);
            } else if (action.equals("JOND")){ // emily
                request=getJOHD(response);
            } else if (action.equals("TERM")){ // audrey
                request="TERM";
            } else if (action.equals("YRMV")){ //emily
                request=getJOHD(response);
            }else{
                System.out.println("method not supported");
            }
            return request;
        }

        public static String getSession(String[] response){


        }

    
    }



    {/*

    -Client is prompted for TCP or UDP
    -Ask for Self identifying name
    -Establish Connected
    -Start new game or Join existing 
        -if new game send CREA command
        -if Join send Join command
        -or LIST command

    -JOND print out youve joined a game and game Identifier string (store this)+ print out
    -
    -BORD: print out board to command line do nothing else 
    -YRMV
        -first yrmv command check for which symbol 
        -others, check if symbol null 
            -otherwise check if your move

    -prompt print out where to move 
    -send MOVE command
    -at any point client can quit send QUIT GID to server 
    -loop back to do u want to start or join a game

    - create class to check for command 
     */}
}