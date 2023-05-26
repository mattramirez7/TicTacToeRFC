package Java;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientSession {
    private String sessionId;
    private Socket clientSocket;
    // Other session data
    
    public ClientSession(String sessionId, Socket clientSocket, Datagr) {
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
