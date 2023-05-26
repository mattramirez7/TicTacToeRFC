package Java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class ClientSessionThread extends Thread {
    private ClientSession session;
    private int PORT = 3116;
    private CommandHandler ch = new CommandHandler();
    
    public ClientSessionThread(ClientSession session) {
        this.session = session;
    }
    
    @Override
    public void run() {
        try {
            if (this.session.connectionType() == "UDP") {
                if (this.session.getInitialPacket() != null) {
                    handleUDPRequest(this.session.getInitialPacket(), this.session.getClientDatagramSocket());
                }
            } else if (this.session.connectionType() == "TCP") {
                if (this.session.getClientSocket() != null) {
                    handleTCPRequest(this.session.getClientSocket());
                }
            } else {
                throw new IOException();
            }
            
            // Clean up resources when the client disconnects
            // inputStream.close();
            // outputStream.close();
            // session.close();
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void handleTCPRequest(Socket TCPSocket) {
        System.out.println("TCP server started and listening on port " + PORT);
        while (session.isConnected()) {
            System.out.println("New TCP client connected: " + TCPSocket.getInetAddress().getHostAddress());

            try (BufferedReader in = new BufferedReader(new InputStreamReader(TCPSocket.getInputStream()));
                PrintWriter out = new PrintWriter(TCPSocket.getOutputStream(), true)) {
        
                String request = in.readLine(); 
                System.out.println("Received: " + request);
                String response = ch.callCommand(request);
                System.out.println("Sending response: " + response);
                out.println(response);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }  
    }

    private void handleUDPRequest(DatagramPacket requestPacket, DatagramSocket UDPSocket) {
        try (UDPSocket) {
            System.out.println("UDP server started and listening on PORT " + PORT);

            while (true) {
                
                // byte[] buffer = new byte[256];
                //requestPacket = new DatagramPacket(buffer, buffer.length);
                UDPSocket.receive(requestPacket);

                System.out.println("Datagram Received.");

                String request = new String(requestPacket.getData(), 0, requestPacket.getLength());
                System.out.println("Received Data: " + request);

                String response = ch.callCommand(request);

                byte[] responseData = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, requestPacket.getAddress(), requestPacket.getPort());
                
                UDPSocket.send(responsePacket);
                UDPSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}