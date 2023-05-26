package Java;

import java.io.IOException;
import java.net.Socket;

public class ClientSession {
    private String sessionId;
    private Socket clientSocket;
    // Other session data
    
    public ClientSession(String sessionId, Socket clientSocket /*Datagram*/) {
        this.sessionId = sessionId;
        this.clientSocket = clientSocket;
        
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
    
    public void close() throws IOException {
        clientSocket.close();
    } 
}
