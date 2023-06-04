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

                tcpHandler clientSock = new tcpHandler(tcpSocket);
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

            byte[] buffer = new byte[256];
            DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);

            UDPhandler udpClient = new UDPhandler(udpSocket, requestPacket);
            new Thread(udpClient).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Output callCommand(String request, int sessionID) {
        String[] requestArgs = request.split("\\s+");
        String command = requestArgs[0];
        String[] args = Arrays.copyOfRange(requestArgs, 1, requestArgs.length);

        CommandHandler ch = new CommandHandler(clients, sessionID, games);
        if (COMMANDS.contains(command)) {
            return new Output(args, ch.handleRequest(command, args));
        } else {
            return new Output("Error");
        }
    }

    private static class Output {
        private String[] args;
        private String commandResponse;      
        private String error;  

        private Output(String[] args, String commandResponse) {
            this.args = args;
            this.commandResponse = commandResponse;
        }
        
        private Output(String error) {
            this.error = error;
        }

        public String[] getArgs() {
            return args;
        }

        public String getResponse() {
            return this.commandResponse;            
        }
    }

    static class tcpHandler implements Runnable {
        private Random random;
        private int id;
        private final Socket clientSocket;

        public tcpHandler(Socket socket) {
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
                while (true) {
                    if ((line = in.readLine()) == null || line.equals("\r\n") || line.equals("")) {
                        continue;
                    }
                    System.out.printf("TCP Client " + this.id + "sent: %s\n ", line);
                    Output data = callCommand(line, this.id);

                    String[] responseArgs = data.getResponse().split("\\s+");
                    String command = responseArgs[0];
                    String[] args = Arrays.copyOfRange(responseArgs, 1, responseArgs.length);
                    Boolean startGame = false;

                    if (command.equals("SESS")) {
                        int protocolVersion = Integer.parseInt(args[0]);
                        int sessionId = Integer.valueOf(args[1]);
                        String[] params = data.getArgs();
                        String clientId = params[1];
                        ClientData newClient = new ClientData(protocolVersion, sessionId, out);
                        clients.put(clientId, newClient);
                    }
                    
                    startGame = updateData(data.getResponse());

                    out.println(data.getResponse() + "\r\n");
                    System.out.println("Sending response: " + data.getResponse());

                    if (startGame) {
                        sendYRMVUpdates(args);
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

    static class UDPhandler implements Runnable {
        private int port;
        private final DatagramSocket serverSocket;
        private final DatagramPacket receivePacket;

        public UDPhandler(DatagramSocket socket, DatagramPacket packet) {
            this.port = -1;
            this.serverSocket = socket;
            this.receivePacket = packet;
        }

        public void run() {
            try {
                while (true) {
                    serverSocket.receive(receivePacket);

                    InetAddress clientIpAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();
                    this.port = clientPort;
                    String clientId = "";
                    byte[] receiveData = receivePacket.getData();
                    int length = receivePacket.getLength();

                    // Convert received data to a string
                    String receivedMessage = new String(receiveData, 0, length);

                    if (receivedMessage == null | receivedMessage.equals("\r\n") | receivedMessage.equals("")) {
                        continue;
                    }
                    // Process the received message
                    System.out.printf("Received from UDP client %d: %s%n", clientPort, receivedMessage);

                    Output data = callCommand(receivedMessage, port);

                    // Send a response back to the client
                    byte[] responseData = data.getResponse().getBytes();
                    String[] responseArgs = data.getResponse().split("\\s+");
                    String command = responseArgs[0];
                    String[] args = Arrays.copyOfRange(responseArgs, 1, responseArgs.length);
                    boolean startGame = false;

                    if (command.equals("SESS")) {
                        clientId = args[1];
                        ClientData newClient = new ClientData(clientPort, clientIpAddress, serverSocket);
                        clients.put(clientId, newClient);
                    }
                    startGame = updateData(data.getResponse());

                    DatagramPacket generalResponsePacket = new DatagramPacket(responseData, responseData.length,
                            clientIpAddress, clientPort);
                    serverSocket.send(generalResponsePacket);

                    if (startGame) {
                        sendYRMVUpdates(args);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean updateData(String response) {
        try {
            String[] responseArgs = response.split("\\s+");
            String command = responseArgs[0];
            String[] args = Arrays.copyOfRange(responseArgs, 1, responseArgs.length);
            Boolean startGame = false;

            if (command.equals("JOND")) {
                String clientId = args[0];
                String gameId = args[1];
                if (games.keySet().contains(gameId)) {
                    Game game = games.get(gameId);
                    game.addPlayer(clientId);
                    startGame = true;
                } else {
                    Game newGame = new Game();
                    newGame.addPlayer(clientId);
                    games.put(gameId, newGame);
                    newGame.setBoardStatus("BORD " + gameId + " " + clientId);
                }
                clients.get(clientId).setGameId(gameId);
            } else if (command.equals("BORD") && args.length > 4) {
                String gameId = args[0];
                String nextPlayerMove = args[3];
                String gameBoard = args[4];
                games.get(gameId).setBoardStatus(response);
                games.get(gameId).updateBoard(gameBoard);
                ClientData nextPlayer = clients.get(nextPlayerMove);
                if (nextPlayer.getPortUDP() != -999) {
                    DatagramPacket gameUpdateResponse = new DatagramPacket(response.getBytes(),
                            response.getBytes().length, nextPlayer.getIpAddress(), nextPlayer.getPortUDP());
                    nextPlayer.getUDPSocket().send(gameUpdateResponse);
                } else {
                    clients.get(nextPlayerMove).getOut().println(response);
                }

                if (args.length == 6) {
                    String currentPlayerMove = args[1];
                    clients.get(currentPlayerMove).setGameId(null);
                    clients.get(nextPlayerMove).setGameId(null);
                } else {
                    startGame = true;
                }

            }
            return startGame;
        } catch (Exception err) {
            err.printStackTrace();
            return false;
        }

    }

    private static void sendYRMVUpdates(String[] args) {
        try {
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
                ClientData curPlayer = clients.get(player);
                InetAddress playerIpAddress = curPlayer.getIpAddress();
                int playerPort = curPlayer.getPortUDP();
                String data = "YMRV " + gameId + " " + currentPlayer;

                if (curPlayer.getPortUDP() != -999) {
                    DatagramPacket gameStartResponse = new DatagramPacket(data.getBytes(),
                            data.getBytes().length, playerIpAddress, playerPort);
                    curPlayer.getUDPSocket().send(gameStartResponse);
                } else {
                    curPlayer.getOut().println(data);
                }

                System.out.println("Response sent: " + data);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }

    }
}