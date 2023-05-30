// package Java;

// import java.io.BufferedReader;
// import java.io.IOException;
// import java.io.InputStreamReader;
// import java.io.PrintWriter;
// import java.net.*;
// import java.util.Scanner;

// public class TCPClient {
//     public static void main(String[] args) {
//         String serverAddress = "localhost"; // Server address
//         int serverPort = 3116; // Server port

//         try (Socket socket = new Socket(serverAddress, serverPort)) {
        
//             System.out.println("Connected to server: " + serverAddress + ":" + serverPort);

//             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            
//             Scanner sc = new Scanner(System.in);
//             String line = null;
  
//             while (!"exit".equalsIgnoreCase(line)) {
                
//                 // reading from user
//                 line = sc.nextLine();
  
//                 // sending the user input to server
//                 writer.println(line);
//                 writer.flush();
  
//                 // displaying server reply
//                 System.out.println(reader.readLine());
//             }


//         } catch (IOException e) {
//             e.printStackTrace();
//         } 
        
//     }
// }
package Java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TCPClient {
    public static void main(String[] args) {
        String serverAddress = "localhost"; // Server address
        int serverPort = 3116; // Server port

        try (Socket socket = new Socket(serverAddress, serverPort)) {
            System.out.println("Connected to server: " + serverAddress + ":" + serverPort);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            Thread responseThread = new Thread(() -> {
                try {
                    String response;
                    while ((response = reader.readLine()) != null) {
                        System.out.println(response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            responseThread.start();

            Scanner sc = new Scanner(System.in);
            String line;

            do {
                line = sc.nextLine();
                writer.println(line);
                writer.flush();
            } while (!"exit".equalsIgnoreCase(line));

            responseThread.interrupt();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
