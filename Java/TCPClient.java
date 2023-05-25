package Java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class TCPClient {
    public static void main(String[] args) {
        String serverAddress = "localhost"; // Server address
        int serverPort = 3116; // Server port

        while (true) {
            try (Socket socket = new Socket(serverAddress, serverPort)) {
            
                System.out.println("Connected to server: " + serverAddress + ":" + serverPort);
    
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
    
                // Send a message to the server
                writer.println("HELO 1 CID1");
    
    
                String response = reader.readLine();
                System.out.println("Server response: " + response);
    
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }

        
    }
}
