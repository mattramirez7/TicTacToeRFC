package Java;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.IOException;

public class TTTPServer {
    private static HashMap<String, Game> games;
    private static HashMap<String, ClientData> clients;
    private static final List<String> COMMANDS = new ArrayList<String>();
    private static HashMap<Integer, Integer> sessionVersions = new HashMap<Integer, Integer>();

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
            System.out.println("*********");
            System.out.println("TCP server started and listening at t3tcp://localhost:" + port);
            System.out.println("*********");
            System.out.println();

            while (true) {
                Socket tcpSocket = server.accept();

                tcpHandler clientSock = new tcpHandler(tcpSocket);
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
            System.out.println("*********");
            System.out.println("UDP server started and listening t3udp://localhost: " + port);
            System.out.println("*********");
            System.out.println();

            byte[] buffer = new byte[256];
            DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);

            UDPhandler udpClient = new UDPhandler(udpSocket, requestPacket);
            new Thread(udpClient).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String callCommand(String request, int sessionID) {
        String[] requestArgs = request.split("\\s+");
        String command = requestArgs[0];
        String[] args = Arrays.copyOfRange(requestArgs, 1, requestArgs.length);

        CommandHandler ch = new CommandHandler(clients, sessionID, games);
        if (COMMANDS.contains(command)) {
            String output = ch.handleRequest(command, args);
            if (command.equals("HELO")) {
                sessionVersions.put(sessionID, ch.getVersion());
                System.out.print(sessionVersions);
            }
            return output;
        } else {
            return "Error";
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
                    System.out.printf("***TCP CLIENT SAYS: " + line);
                    String response = callCommand(line, this.id);

                    String[] responseArgs = response.split("\\s+");
                    String command = responseArgs[0];
                    String[] args = Arrays.copyOfRange(responseArgs, 1, responseArgs.length);
                    Boolean startGame = false;

                    if (command.equals("SESS")) {
                        int protocolVersion = Integer.parseInt(args[0]);
                        int sessionId = Integer.valueOf(args[1]);
                        String clientId = line.split("\\s+")[2];
                        ClientData newClient = new ClientData(protocolVersion, sessionId, out);
                        clients.put(clientId, newClient);
                    }

                    startGame = updateData(response);
                    if (args.length == 6) {
                        String winner = args[5];
                        if (winner.equals("CATS")) {
                            response = response.substring(0, response.lastIndexOf("CATS")).trim();
                        }
                    }

                    if (!command.equals("QUIT")) {
                        out.println(response + "\r\n");
                        System.out.println("***TCP SERVER SAYS: " + response);
                    }

                    if (args.length == 6) {
                        String nextPlayerMove = args[3];
                        String currentPlayerMove = args[1];
                        String gameId = args[0];
                        String winner = args[5];
                        clients.get(currentPlayerMove).setGameId(null);
                        clients.get(nextPlayerMove).setGameId(null);
    
                        String termGameMsg = "TERM " + gameId;
                        if (!winner.equals("CATS")) {
                            termGameMsg = "TERM " + gameId + " " + winner + " KTHXBYE";
                        } else {
                            termGameMsg += " KTHXBYE";
                        }
                        for (String player : games.get(gameId).getPlayers()) {
                            ClientData curPlayer = clients.get(player);
                            InetAddress playerIpAddress = curPlayer.getIpAddress();
                            int playerPort = curPlayer.getPortUDP();
    
                            if (curPlayer.getPortUDP() != -999) {
                                DatagramPacket termMsg = new DatagramPacket(termGameMsg.getBytes(),
                                        termGameMsg.getBytes().length, playerIpAddress, playerPort);
                                curPlayer.getUDPSocket().send(termMsg);
                            } else {
                                curPlayer.getOut().println(termGameMsg);
                            }
                            System.out.println("***TCP SERVER SAYS: " + termGameMsg);
                        }
                        startGame = false;
                    } 

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
                    byte[] receiveData = receivePacket.getData();
                    int length = receivePacket.getLength();

                    String receivedMessage = new String(receiveData, 0, length);

                    if (receivedMessage == null | receivedMessage.equals("\r\n") | receivedMessage.equals("")) {
                        continue;
                    }
                    System.out.printf("***UDP CLIENT SAYS: " + receivedMessage);

                    String response = callCommand(receivedMessage, port);

                    byte[] responseData = response.getBytes();
                    String[] responseArgs = response.split("\\s+");
                    String command = responseArgs[0];
                    String[] args = Arrays.copyOfRange(responseArgs, 1, responseArgs.length);
                    boolean startGame = false;

                    if (command.equals("SESS")) {
                        int protocolVersion = Integer.parseInt(args[0]);
                        String clientId = receivedMessage.split("\\s+")[2];
                        ClientData newClient = new ClientData(protocolVersion, clientPort, clientIpAddress,
                                serverSocket);
                        clients.put(clientId, newClient);
                    }
                    startGame = updateData(response);
                    if (args.length == 6) {
                        String winner = args[5];
                        if (winner.equals("CATS")) {
                            response = response.substring(0, response.lastIndexOf("CATS")).trim();
                        }
                    }

                    if (!command.equals("QUIT")) {
                        DatagramPacket generalResponsePacket = new DatagramPacket(responseData, responseData.length,
                                clientIpAddress, clientPort);
                        serverSocket.send(generalResponsePacket);
                    }

                    if (args.length == 6) {
                        String nextPlayerMove = args[3];
                        String currentPlayerMove = args[1];
                        String gameId = args[0];
                        String winner = args[5];
                        clients.get(currentPlayerMove).setGameId(null);
                        clients.get(nextPlayerMove).setGameId(null);
    
                        String termGameMsg = "TERM " + gameId;
                        if (!winner.equals("CATS")) {
                            termGameMsg = "TERM " + gameId + " " + winner + " KTHXBYE";
                        } else {
                            termGameMsg += " KTHXBYE";
                        }
                        for (String player : games.get(gameId).getPlayers()) {
                            ClientData curPlayer = clients.get(player);
                            InetAddress playerIpAddress = curPlayer.getIpAddress();
                            int playerPort = curPlayer.getPortUDP();
    
                            if (curPlayer.getPortUDP() != -999) {
                                DatagramPacket termMsg = new DatagramPacket(termGameMsg.getBytes(),
                                        termGameMsg.getBytes().length, playerIpAddress, playerPort);
                                curPlayer.getUDPSocket().send(termMsg);
                            } else {
                                curPlayer.getOut().println(termGameMsg);
                            }
                            System.out.println("***UDP SERVER SAYS: " + termGameMsg);
                        }
                        startGame = false;
                    }

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
            String winner = null;

            if (command.equals("JOND")) {
                String clientId = args[0];
                String gameId = args[1];

                List<String> players = new ArrayList<String>();
                if (games.keySet().contains(gameId)) {
                    Game game = games.get(gameId);
                    game.addPlayer(clientId);
                            if (games.size() > 0 && games.values().stream().anyMatch(gamez -> game.getPlayers().contains(clientId))) {
                                players.addAll(games.get(gameId).getPlayers());
                            }
    
                            int[] playerVersions = new int[players.size()];
                            int i = 0;
    
                            for (String player : players) {
                                int curPlayerSessionId = clients.get(player).getSessionId();
                                int curPlayersVersion = sessionVersions.get(curPlayerSessionId);
                                playerVersions[i] = curPlayersVersion;
                                i++;
                            }

                            if (playerVersions.length > 0) {
                                int oldestVersion = playerVersions[0]; // Assume the first element is the minimum
        
                                for (int j = 1; j < playerVersions.length; j++) {
                                    if (playerVersions[j] < oldestVersion) {
                                        oldestVersion = playerVersions[j]; // Update the minimum if a smaller value is found
                                    }
                                }
                                game.setVersion(oldestVersion);
                                System.out.println("Joined Game Version:" + game.getVersion());
                            }
                    startGame = true;
                } else {
                    Game newGame = new Game();
                    int sessionId = clients.get(clientId).getSessionId();
                    newGame.setVersion(sessionVersions.get(sessionId));
                    newGame.addPlayer(clientId);
                    games.put(gameId, newGame);
                    newGame.setBoardStatus("BORD " + gameId + " " + clientId);
                }
                clients.get(clientId).setGameId(gameId);
            } else if (command.equals("BORD") && args.length > 4) {
                String gameId = args[0];
                String nextPlayerMove = args[3];
                String gameBoard = args[4];
                if (args.length == 6) {
                    winner = args[5];
                    if (winner.equals("CATS")) {
                        response = response.substring(0, response.lastIndexOf("CATS")).trim();
                        System.out.println("***CATS RESPONSE: " + response);
                    }

                }
                games.get(gameId).setBoardStatus(response);
                games.get(gameId).updateBoard(gameBoard);
                ClientData nextPlayer = clients.get(nextPlayerMove);
                startGame = true;
                if (nextPlayer.getPortUDP() != -999) {
                    DatagramPacket gameUpdateResponse = new DatagramPacket(response.getBytes(),
                            response.getBytes().length, nextPlayer.getIpAddress(), nextPlayer.getPortUDP());
                    nextPlayer.getUDPSocket().send(gameUpdateResponse);
                } else {
                    clients.get(nextPlayerMove).getOut().println(response);
                }


            } else if (command.equals("QUIT")) {
                String gameId = args[0];
                String defaultWinner = args[1];
                
                for (String player : games.get(gameId).getPlayers()) {
                    clients.get(player).setGameId(null);
                
                    if (player.equals(defaultWinner)) {
                        ClientData curPlayer = clients.get(player);
                        InetAddress playerIpAddress = curPlayer.getIpAddress();
                        int playerPort = curPlayer.getPortUDP();
                        String gameWonBordMsg = games.get(gameId).getBoardStatus() + " " + defaultWinner;
                        games.get(gameId).setBoardStatus(response);
                        if (clients.get(player).getPortUDP() == -999) {
                            clients.get(player).getOut().println(gameWonBordMsg);
                        } else {
                            DatagramPacket bordMsg = new DatagramPacket(gameWonBordMsg.getBytes(),
                                    gameWonBordMsg.getBytes().length, playerIpAddress, playerPort);
                            curPlayer.getUDPSocket().send(bordMsg);
                        }
                    }
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
                System.out.println("***SERVER SAYS " + data);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }

    }
}