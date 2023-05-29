package Java;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class TTTPServer {
    private static HashMap<String, Game> games;
    private static HashMap<String, ClientData> clients;
    private static final List<String> COMMANDS = new ArrayList<String>();

    static {
        COMMANDS.add("CREA");
        COMMANDS.add("GDBY");
        COMMANDS.add("HELO");
        COMMANDS.add("JOIN");
        COMMANDS.add("LIST");
        COMMANDS.add("MOVE");
        COMMANDS.add("QUIT");
        COMMANDS.add("STAT");
    }

    private static int port = 3116;
    static ExecutorService exec = null;

    public static void main(String[] args) {
        clients = new HashMap<String, ClientData>();
        games = new HashMap<String, Game>();

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

                ClientHandlerTCP clientSock = new ClientHandlerTCP(tcpSocket);
                System.out.println("New TCP client connected: " + clientSock.id);
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleUDPRequest() {
        DatagramSocket udpSocket = null;
        try {
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
    private static String callCommand(String request, int sessionID) {
        String[] requestArgs = request.split("\\s+");
        String command = requestArgs[0];
        String[] args = Arrays.copyOfRange(requestArgs, 1, requestArgs.length);

        CommandHandler ch = new CommandHandler(clients, sessionID, games);
        if (COMMANDS.contains(command)) {
            return ch.handleRequest(command, args);
        } else {
            System.out.println("Invalid command: " + command);
            return "Error";
        }
    }

    // ClientHandler class
    static class ClientHandlerTCP implements Runnable {
        private static final Boolean False = null;
        private Random random;
        private int id;
        private final Socket clientSocket;

        // Constructor
        public ClientHandlerTCP(Socket socket) {
            this.random = new Random();
            this.id = random.nextInt(Integer.MAX_VALUE) + 1;
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
                    System.out.printf("Client " + this.id + "sent: %s\n", line);
                    String response = callCommand(line, this.id);

                    String[] responseArgs = response.split("\\s+");
                    String command = responseArgs[0];
                    String[] args = Arrays.copyOfRange(responseArgs, 1, responseArgs.length);
                    Boolean startGame = false;

                    if (command.equals("SESS")) {
                        int sessionId = Integer.parseInt(args[0]);
                        String clientId = args[1];
                        ClientData newClient = new ClientData(sessionId, out);
                        clients.put(clientId, newClient);
                    }

                    if (command.equals("JOND")) {
                        String clientId = args[0];
                        String gameId = args[1];
                        if (games.keySet().contains(gameId)) {
                            games.get(gameId).addPlayer(clientId);
                            startGame = true;
                        } else {
                            Game newGame = new Game();
                            newGame.addPlayer(clientId);
                            games.put(gameId, newGame);
                        }
                        clients.get(clientId).setGameId(gameId);
                    }
                    if (command.equals("BORD") && args.length > 4) {
                        String gameId = args[0];
                        String nextPlayerMove = args[3];
                        String gameBoard = args[4];
                        games.get(gameId).updateBoard(gameBoard);
                        clients.get(nextPlayerMove).getOut().println(response);
                        startGame = true;
                    }
                    out.println(response);

                    if (startGame) {
                        String gameId = "";
                        if (args.length > 4) {
                            gameId = args[0];
                        } else {
                            gameId = args[1];
                        }
                        String gameBoard = games.get(gameId).getBoard();
                        List<String> players = games.get(gameId).getPlayers();

                        int remainingStars = gameBoard.length() - gameBoard.replace("*", "").length();
                        String currentPlayer = remainingStars % 2 == 0 ? players.get(1) : players.get(0);

                        for (String player : games.get(gameId).getPlayers()) {
                            PrintWriter playerOut = clients.get(player).getOut();
                            playerOut.println("YMRV " + gameId + " " + currentPlayer);
                        }
                    }

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

                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddress,
                            clientPort);
                    serverSocket.send(responsePacket);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class ClientData {
        private int sessionId;
        private String gameId;
        private PrintWriter out;
    
        public ClientData(int sessionId, PrintWriter out) {
            this.sessionId = sessionId;
            this.gameId = null; // Initialize game ID as null (optional)
            this.out = out;
        }
    
        // Getters and setters (optional) for sessionId and gameId
        public int getSessionId() {
            return sessionId;
        }
    
        public void setSessionId(int sessionId) {
            this.sessionId = sessionId;
        }
    
        public String getGameId() {
            return gameId;
        }
    
        public void setGameId(String gameId) {
            this.gameId = gameId;
        }

        public PrintWriter getOut() {
            return out;
        }
    }
    
}
// }
