package Java;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TTTPServer {
    private HashSet<String> avaliableGames = new HashSet<String>();
    private HashMap<String, HashMap<String, String[]>> clientMoves;
    private static final HashMap<String, String> COMMANDS = new HashMap<String, String>();
    private static CommandHandler ch = new CommandHandler();

    static {
        COMMANDS.put("CREA", "request");
        COMMANDS.put("GDBY", "request");
        COMMANDS.put("HELO", "request");
        COMMANDS.put("JOIN", "request");
        COMMANDS.put("LIST", "request");
        COMMANDS.put("MOVE", "request");
        COMMANDS.put("QUIT", "request");
        COMMANDS.put("STAT", "request");
        COMMANDS.put("BORD", "response");
        COMMANDS.put("GAMS", "response");
        COMMANDS.put("JOND", "response");
        COMMANDS.put("SESS", "response");
        COMMANDS.put("TERM", "response");
        COMMANDS.put("YRMV", "response");
        
    }

    private static int port = 3116;
    static ExecutorService exec = null;
    public static void main(String[] args) {

        exec = Executors.newFixedThreadPool(10);
        exec.submit(() -> handleTCPRequest());
        exec.submit(() -> handleUDPRequest());
        

    }

    /**
     * 
     */
    private static void handleTCPRequest() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started and listening on port " + port);
            Socket TCPSocket;
            while ((TCPSocket = serverSocket.accept()) != null) {
                System.out.println("New TCP client connected: " + TCPSocket.getInetAddress().getHostAddress());

                try (BufferedReader in = new BufferedReader(new InputStreamReader(TCPSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(TCPSocket.getOutputStream(), true)) {
            
                        String request;
                        while ((request = in.readLine()) != null) { 
                            System.out.println("Received: " + request);
                            callCommand(request);
                            out.println("Sending client response...");
                        }
                        
                        in.close();
                        out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }  
    }
    
    private static void handleUDPRequest() {
        try {
            while(true) {
                byte[] buffer = new byte[256];
                DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                DatagramSocket UDPSocket = new DatagramSocket(port);
                
                UDPSocket.receive(requestPacket);
                System.out.println("Datagram Received.");

                String request = new String(requestPacket.getData(), 0, requestPacket.getLength());
                System.out.println("Received Data: " + request);
                
                callCommand(request);

                UDPSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Example Request: 
            //HELO 1 CID1
            //SESS SID2 CID2
            //BORD GID1 CID1 CID2 CID2 
            // |*|*|*|
            // |*|X|*|
            // |*|*|*|
    private static void callCommand(String request) {
        String[] requestArgs = request.split("\\s+");
        String command = requestArgs[0];
        String[] args = Arrays.copyOfRange(requestArgs, 1, requestArgs.length);

        if (COMMANDS.get(command).equals("request")) {
            ch.handleRequest(command, args);
        } else if (COMMANDS.get(command).equals("response"))  {
            ch.createResponse(command, args);
        } else {
            System.out.println("Invalid command: " + command);
        }
    }
}


