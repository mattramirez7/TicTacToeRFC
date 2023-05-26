package Java;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

public class ClientSession {
    private String sessionId;
    private Socket clientSocket;
    private DatagramSocket clientDatagramSocket;
    private DatagramPacket initialPacket;
    // Other session data
    
    public ClientSession(String sessionId, DatagramSocket clientDatagramSocket, DatagramPacket initialPacket) {
        this.sessionId = sessionId;
        this.clientDatagramSocket = clientDatagramSocket;
        this.initialPacket = initialPacket;
    }

    public ClientSession(String sessionId, Socket clientSocket) {
        this.sessionId = sessionId;
        this.clientSocket = clientSocket;
    }

    public String connectionType() {
        if (clientSocket != null) {
            return "TCP";
        }
        if (clientDatagramSocket != null & initialPacket != null) {
            return "UDP";
        }
        else {
            System.out.println("Error: ");
            return "Error";
        }
    }

    public DatagramPacket getInitialPacket() {
        return initialPacket;
    }
        
    public boolean isConnected() {
        return clientSocket.isConnected();
    }

    public String getSessionId() {
        return sessionId;
    }
    
    public Socket getClientSocket() {
        return clientSocket;
    }

    public DatagramSocket getClientDatagramSocket() {
        return clientDatagramSocket;
    }
    
    public void close() throws IOException {
        if (clientSocket != null) {
            clientSocket.close();
        }
        if (clientDatagramSocket != null) {
            clientDatagramSocket.close();
        }
    } 
}
