package Java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        String serverAddress = "localhost"; // Server address
        int serverPort = 3116; // Server port

        try {
            Socket socket = new Socket(serverAddress, serverPort);
            System.out.println("Connected to server: " + serverAddress + ":" + serverPort);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // Send a message to the server
            writer.println("HELO 1 CID1");

            // Receive the server's response
            // String response = reader.readLine();
            // System.out.println("Server response: " + response);

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String message = "CREA CID1";
        int port = 3116;
        String host = "localhost";
        DatagramSocket udpSocket = null;
        System.out.println("UDP Client connected on port " + port + " to server: " + host);
        try {
            InetAddress address = InetAddress.getByName(host);
            
            udpSocket = new DatagramSocket();
            byte[] requestData = message.getBytes("UTF-8");
            DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, address, port);
            udpSocket.send(requestPacket);

            // byte[] buffer = new byte[512];
            // DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            // udpSocket.receive(responsePacket);

            // String replyContent = new String(buffer);
            // System.out.println("Received: " + replyContent);
            // udpSocket.close();
        
        } catch (IOException err) {
            System.err.println("Error communicating with server");
            err.printStackTrace();
        } 
    }
}
