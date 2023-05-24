package Java;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TTTPServer {
    
    public static void main(String... args) {
        int port = 3116; // Port number to listen on

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            
            System.out.println("Server started and listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Perform operations with the clientSocket here
                
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
