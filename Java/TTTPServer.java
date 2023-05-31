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
        while (true) {
            System.out.println(((ThreadPoolExecutor) exec).getActiveCount());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

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

    private static void handleTCPRequest() {
        try (ServerSocket server = new ServerSocket(port)) {
            server.setReuseAddress(true);
            System.out.println("TCP server started and listening on port " + port);
            while (true) {
                Socket tcpSocket = server.accept();
                TCPHandler clientSock = new TCPHandler(tcpSocket);
                System.out.println("New TCP client connected: " + clientSock.id);
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class TCPHandler implements Runnable {
        private Random random;
        private int id;
        private final Socket clientSocket;

        // Constructor
        public TCPHandler(Socket socket) {
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
                String line = "";
                while ((line = in.readLine()) != null && !line.equals("") && line != null) {
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
                    System.out.println("Sending response to user" + response);

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
                        System.out.println(remainingStars);
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

    private static void handleUDPRequest() {
        DatagramSocket udpSocket = null;
        try {
            udpSocket = new DatagramSocket(port);
            System.out.println("UDP server started and listening on port " + port);

            // while (true) {
            byte[] buffer = new byte[256];
            DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);

            UDPHandler udpClient = new UDPHandler(udpSocket, requestPacket);
            new Thread(udpClient).start();

            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static class UDPHandler implements Runnable {
        private Random random;
        private int id;
        private int port;
        private final DatagramSocket serverSocket;
        private final DatagramPacket receivePacket;

        // Constructor
        public UDPHandler(DatagramSocket socket, DatagramPacket packet) {
            this.random = new Random();
            this.id = random.nextInt();
            this.serverSocket = socket;
            this.receivePacket = packet;
            this.port = packet.getPort();
        }

        public void run() {
            try {
                while (true) {
                    serverSocket.receive(receivePacket);
                    System.out.println("Reply address: " + receivePacket.getAddress());
                    System.out.println("Reply port: " + receivePacket.getPort());
                    System.out.println();


                    InetAddress clientIpAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();
                    String clientId = "";
                    byte[] receiveData = receivePacket.getData();
                    int length = receivePacket.getLength();

                    // Convert received data to a string
                    String receivedMessage = new String(receiveData, 0, length);

                    // Process the received message
                    System.out.printf("Received from UDP client %d: %s%n", clientPort, receivedMessage);

                    // Send a response back to the client
                    String responseMessage = receivedMessage.toUpperCase(); // Example: Convert to uppercase
                    byte[] responseData = responseMessage.getBytes();
                    String[] responseArgs = responseMessage.split("\\s+");
                    String command = responseArgs[0];
                    String[] args = Arrays.copyOfRange(responseArgs, 1, responseArgs.length);
                    Boolean startGame = false;


                    // DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientIpAddress,
                    //         clientPort);
                    // serverSocket.send(responsePacket);

                    if (command.equals("SESS")) {
                        int sessionId = Integer.parseInt(args[0]);
                        clientId = args[1];
                        ClientData newClient = new ClientData(sessionId, clientPort, clientIpAddress);
                        clients.put(clientId, newClient);
                    }

                    if (command.equals("JOND")) {
                        clientId = args[0];
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
                        byte[] nextPlayerMoveByte = nextPlayerMove.getBytes();
                        DatagramPacket boardResponsePacket = new DatagramPacket(nextPlayerMoveByte, responseData.length, clientIpAddress, clientPort);
                        serverSocket.send(boardResponsePacket);
                        startGame = true;
                    }

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
                        System.out.println(remainingStars);
                        String currentPlayer = remainingStars % 2 == 0 ? players.get(1) : players.get(0);

                        for (String player : games.get(gameId).getPlayers()) {
                            ClientData curPlayer = clients.get(player);
                            InetAddress playerIpAddress = curPlayer.getIpAddress();
                            int playerPort = curPlayer.getPortUDP();
                            String data = "YMRV " + gameId + " " + currentPlayer;
        
                            DatagramPacket gameStartResponse = new DatagramPacket(data.getBytes(), data.getBytes().length, playerIpAddress, playerPort);
                            serverSocket.send(gameStartResponse);
                            System.out.println("Response sent: " + data);
                        }
                    } 
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}