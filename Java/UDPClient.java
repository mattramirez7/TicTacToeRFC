package Java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;

public class UDPClient {
    public static void main(String[] args) {

        int port = 3116;
        String host = "localhost";

        System.out.println("UDP Client connected on port " + port + " to server: " + host);
        try {
            DatagramSocket udpSocket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(host);
            Scanner scanner = new Scanner(System.in);

            while(true) {
                System.out.print("Enter a message (or 'quit' to exit): ");
                String message = scanner.nextLine();
    
                if (message.equalsIgnoreCase("quit")) {
                    break; // Exit the loop if the user enters "quit"
                }
    
                byte[] requestData = message.getBytes("UTF-8");
                DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, address, port);
                udpSocket.send(requestPacket);

                byte[] buffer = new byte[512];
                DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(responsePacket);

                String replyContent = new String(buffer);
                System.out.println("Received: " + replyContent);
    
            }
            scanner.close();
            udpSocket.close();
        } catch (IOException err) {
            System.err.println("Error communicating with server");
            err.printStackTrace();
        } 
    }
}
