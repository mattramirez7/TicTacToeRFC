import java.util.*;
import java.io.*;
import java.net.*;

public class Client {
    public static void main(String...args){
        Scanner scanner = new Scanner(System.in);

        System.out.print("TCP or UDP: ");
        String service = scanner.nextLine();

        System.out.print("Would u like to create a game or join an existing game?");
        String action = scanner.nextLine();

        if (service.equalsIgnoreCase("TCP")) {
            try {
            // Establish TCP connection
            Socket socket = new Socket("localhost", 3116);
            System.out.println("TCP connection established");

            // Create input and output streams for the socket
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Send greeting message
            String protocolVersion = "1";
            String clientIdentifier = "CLIENT123"; // change this based on client name
            out.println("HELO " + protocolVersion + " " + clientIdentifier);
            System.out.println("Sent greeting message to the server.");

            // Receive acknowledgment
            String acknowledgment = in.readLine();
            System.out.println("Received acknowledgment from the server: " + acknowledgment);

            

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