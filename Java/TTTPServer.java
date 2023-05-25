package Java;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class TTTPServer {
    private HashSet<String> availableGames = new HashSet<String>();
    private HashMap<String, String> games;
    private static HashMap<String, String> clients;
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

    private static void handleTCPRequest() {
        Socket TCPSocket = null;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("TCP server started and listening on port " + port);
            while ((TCPSocket = serverSocket.accept()) != null) {
                System.out.println("New TCP client connected: " + TCPSocket.getInetAddress().getHostAddress());

                try (BufferedReader in = new BufferedReader(new InputStreamReader(TCPSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(TCPSocket.getOutputStream(), true)) {
            
                    String request = in.readLine(); 
                    System.out.println("Received: " + request);
                    String response = callCommand(request);
                    System.out.println("Sending response: " + response);
                    out.println(response);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }  
    }

    

    private static void handleUDPRequest() {
        try (DatagramSocket UDPSocket = new DatagramSocket(port)) {
            System.out.println("UDP server started and listening on port " + port);

            while (true) {
                byte[] buffer = new byte[256];
                DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                UDPSocket.receive(requestPacket);

                System.out.println("Datagram Received.");

                String request = new String(requestPacket.getData(), 0, requestPacket.getLength());
                System.out.println("Received Data: " + request);

                String response = callCommand(request);

                byte[] responseData = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, requestPacket.getAddress(), requestPacket.getPort());
                
                UDPSocket.send(responsePacket);
                UDPSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Example Request: 
            // HELO 1 CID1
            // SESS SID2 CID2
            // BORD GID1 CID1 CID2 CID2
            //
            // |*|*|*|
            // |*|X|*|
            // |*|*|*|
    private static String callCommand(String request) {
        String[] requestArgs = request.split("\\s+");
        String command = requestArgs[0];
        String[] args = Arrays.copyOfRange(requestArgs, 1, requestArgs.length);

        String response = "";
        

        // if (command == "HELO" & args[0] != null) {
        //     String version = args[0];
        //     String clientIdentifier = args[1];
        //     if (clients.containsKey(clientIdentifier)) {
        //         return "";
        //     } else {

        //     }
        // } 
        // else if (command == "CREA" & args[0] != null) {
        //     String clientIdentifier = args[0];
        //     if (clients.containsKey(clientIdentifier)) {
        //         return "";
        //     }
        // } else { 

            if (COMMANDS.get(command).equals("request")) {
                return ch.handleRequest(command, args);
            } else {
                System.out.println("Invalid command: " + command);
                return "Error";
            }
        }

        // if (COMMANDS.get(command).equals("request")) {
        //     response = ch.handleRequest(command, args);
        // } else {
        //     System.out.println("Invalid command: " + command);
        //     return "Error";
        // }

        // String[] responseParts = response.split("\\s+");
        // String responseCommand = responseParts[0];
        // String[] responseArgs = Arrays.copyOfRange(responseParts, 1, responseParts.length);

        // if (responseCommand.equals("SESS")) {
        //     String sessionId = responseArgs[0];
            
        // }

        
    }
//}


