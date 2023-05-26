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

public class ClientSessionThread extends Thread {
    private ClientSession session;
    
    public ClientSessionThread(ClientSession session) {
        this.session = session;
    }
    
    @Override
    public void run() {
        try {
            InputStream inputStream = session.getClientSocket().getInputStream();
            OutputStream outputStream = session.getClientSocket().getOutputStream();
            
            while (session.isConnected()) {
                // Receive incoming messages from the client
                // ...
                
                // Process the received message
                // ...
                
                // Send response to the client
                // ...
                System.out.println("********************");
            }
            
            // Clean up resources when the client disconnects
            inputStream.close();
            outputStream.close();
            session.close();
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    // private void handleTCPRequest() {
    //     Socket TCPSocket = null;
    //     try (ServerSocket serverSocket = new ServerSocket(PORT)) {
    //         System.out.println("TCP server started and listening on port " + PORT);
    //         while ((TCPSocket = serverSocket.accept()) != null) {
    //             System.out.println("New TCP client connected: " + TCPSocket.getInetAddress().getHostAddress());

    //             try (BufferedReader in = new BufferedReader(new InputStreamReader(TCPSocket.getInputStream()));
    //                 PrintWriter out = new PrintWriter(TCPSocket.getOutputStream(), true)) {
            
    //                 String request = in.readLine(); 
    //                 System.out.println("Received: " + request);
    //                 String response = callCommand(request);
    //                 System.out.println("Sending response: " + response);
    //                 out.println(response);

    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }  
    // }

    // private void handleUDPRequest() {
    //     try (DatagramSocket UDPSocket = new DatagramSocket(PORT)) {
    //         System.out.println("UDP server started and listening on PORT " + PORT);

    //         while (true) {
    //             byte[] buffer = new byte[256];
    //             DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
    //             UDPSocket.receive(requestPacket);

    //             System.out.println("Datagram Received.");

    //             String request = new String(requestPacket.getData(), 0, requestPacket.getLength());
    //             System.out.println("Received Data: " + request);

    //             String response = callCommand(request);

    //             byte[] responseData = response.getBytes();
    //             DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, requestPacket.getAddress(), requestPacket.getPort());
                
    //             UDPSocket.send(responsePacket);
    //             UDPSocket.close();
    //         }
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }


}