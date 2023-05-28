package Java;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class TTTPServer {
    private HashSet<String> availableGames = new HashSet<String>();
    private HashMap<String, String> games;
    private static HashMap<Integer, String> clients;
    private static final HashMap<String, String> COMMANDS = new HashMap<String, String>();

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
        clients = new HashMap<Integer, String>();

        exec = Executors.newFixedThreadPool(10);
        exec.submit(() -> handleTCPRequest());
        exec.submit(() -> handleUDPRequest());

    }

    private static void handleTCPRequest() {

        try (ServerSocket server = new ServerSocket(port)) {
            server.setReuseAddress(true);
            System.out.println("TCP server started and listening on port " + port);

            while (true) {
                Socket tcpSocket = server.accept();
                System.out.println("New TCP client connected: " + tcpSocket.getInetAddress().getHostAddress());

                ClientHandlerTCP clientSock = new ClientHandlerTCP(tcpSocket);
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleUDPRequest() {
        DatagramSocket udpSocket = null;
        try  {
            udpSocket = new DatagramSocket(port);
            System.out.println("UDP server started and listening on port " + port);

            // while (true) {
                byte[] buffer = new byte[256];
                DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                
                ClientHandlerUDP udpClient = new ClientHandlerUDP(udpSocket, requestPacket);
                new Thread(udpClient).start();

            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Example Request:
    // HELO 1 CID1
    // SESS SID2 CID2
    // BORD GID1 CID1 CID2 CID2
    //
    // |*|*|*|
    // |*|X|*|
    // |*|*|*|
    private static String callCommand(String request, int clientID) {
        String[] requestArgs = request.split("\\s+");
        String command = requestArgs[0];
        String[] args = Arrays.copyOfRange(requestArgs, 1, requestArgs.length);
        
        if (clients.keySet()!= null) {
            if (!clients.keySet().contains(clientID)) {
                clients.put(clientID, "");
            }
        } else {
            clients.put(clientID, "");
        }

        CommandHandler ch = new CommandHandler(clients, clientID);
        if (COMMANDS.get(command).equals("request")) {
            return ch.handleRequest(command, args);
        } else {
            System.out.println("Invalid command: " + command);
            return "Error";
        }
    }

    // ClientHandler class
    static class ClientHandlerTCP implements Runnable {
        private Random random;
        private int id;
        private final Socket clientSocket;

        // Constructor
        public ClientHandlerTCP(Socket socket) {
            this.random = new Random();
            this.id = random.nextInt();
            this.clientSocket = socket;
        }

        public void run() {
            PrintWriter out = null;
            BufferedReader in = null;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.printf(" Sent from client " + this.id + ": %s\n",line);
                    String response = callCommand(line, this.id);

                    String[] responseArgs = response.split("\\s+");
                    String command = responseArgs[0];
                    String[] args = Arrays.copyOfRange(responseArgs, 1, responseArgs.length);
                    if (command.equals("SESS")) {
                        clients.put(this.id, args[0]);
                    }

                    out.println(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class ClientHandlerUDP implements Runnable {
        private Random random;
        private int id;
        private final DatagramSocket serverSocket;
        private final DatagramPacket receivePacket;
    
        // Constructor
        public ClientHandlerUDP(DatagramSocket socket, DatagramPacket packet) {
            this.random = new Random();
            this.id = random.nextInt();
            this.serverSocket = socket;
            this.receivePacket = packet;
        }
    
        public void run() {
            try {
                while (true) {
                    serverSocket.receive(receivePacket);
                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();
        
                    byte[] receiveData = receivePacket.getData();
                    int length = receivePacket.getLength();
        
                    // Convert received data to a string
                    String receivedMessage = new String(receiveData, 0, length);
                    
                    // Process the received message
                    System.out.printf("Received from udp client %d: %s%n", this.id, receivedMessage);
        
                    // Send a response back to the client
                    String responseMessage = receivedMessage.toUpperCase(); // Example: Convert to uppercase
                    byte[] responseData = responseMessage.getBytes();
        
                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);
                    serverSocket.send(responsePacket);
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }
    }
}
// }
