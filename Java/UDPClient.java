package Java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class UDPClient {
    public static void main(String[] args) {

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
