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
    
    public ClientSession(String sessionId, Socket clientSocket) {
        this.sessionId = sessionId;
        this.clientSocket = clientSocket;
        // Initialize other session data
    }
    
    // Getter and setter methods for session data
    
    public boolean isConnected() {
        return clientSocket.isConnected();
    }
    
    public void close() throws IOException {
        clientSocket.close();
    }
}

public class ClientSessionThread extends Thread {
    private ClientSession session;
    
    public ClientSessionThread(ClientSession session) {
        this.session = session;
    }
    
    @Override
    public void run() {
        try {
            // Get the input/output streams from the client socket
            InputStream inputStream = session.getClientSocket().getInputStream();
            OutputStream outputStream = session.getClientSocket().getOutputStream();
            
            // Logic for handling the client's session
            while (session.isConnected()) {
                // Receive incoming messages from the client
                // ...
                
                // Process the received message
                // ...
                
                // Send response to the client
                // ...
            }
            
            // Clean up resources when the client disconnects
            inputStream.close();
            outputStream.close();
            session.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class Server {
    private static final int PORT = 1234;
    private ServerSocket serverSocket;
    private Map<String, ClientSessionThread> clientThreads;
    
    public Server() {
        clientThreads = new HashMap<>();
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                String sessionId = generateSessionId();
                ClientSession session = new ClientSession(sessionId, clientSocket);
                
                // Create a new thread for the client's session
                ClientSessionThread clientThread = new ClientSessionThread(session);
                clientThreads.put(sessionId, clientThread);
                
                // Start the thread to handle the client's session
                clientThread.start();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }
    
    public void stop() {
        try {
            serverSocket.close();
            
            // Close all client sessions
            for (ClientSessionThread thread : clientThreads.values()) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String generateSessionId() {
        // Generate a unique session ID
        // ...
        return "";
    }
    
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
