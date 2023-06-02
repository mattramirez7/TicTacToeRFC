package Java;

import java.util.*;
import java.io.*;
import java.net.*;

public class Client {

    public static void main(String... args) {
        String host = "localhost";
        int port = 3116;
        String protocolVersion = "1";

        Scanner scanner = new Scanner(System.in);

        System.out.print("TCP or UDP: ");
        String service = scanner.nextLine();

        System.out.print("Enter your Client Identifier: ");
        String clientIdentifier = scanner.nextLine();

        ClientHandler newClient = new ClientHandler("", clientIdentifier, "", false, "|*|*|*|*|*|*|*|*|*|", "", "");

        // System.out.print("Enter Version: ");
        // String version = scanner.nextLine();

        if (service.equalsIgnoreCase("TCP")) {
            try {
                // Establish TCP connection
                Socket socket = new Socket(host, port);
                System.out.println("TCP connection established");

                // Create input and output streams for the socket
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Send greeting message
                out.println("HELO " + protocolVersion + " " + newClient.getClientID() + "\r\n");
                // long startTime = System.currentTimeMillis();
                // long timeout = 3000;

                // String acknowledgment = in.readLine();
                // System.out.println("Received acknowledgment from the server: " +
                // acknowledgment);

                // Receive acknowledgment
                while (!newClient.getTerminated()) {
                    // String awaitingServer = scanner.nextLine();
                    // if()
                    String rawResponse = in.readLine();
                    //String noResponse = "";

                    if (rawResponse == null | rawResponse.equals("\r\n") | rawResponse.equals("")) {
                        // System.out.println("It is null");
                        // if(System.currentTimeMillis() - startTime > timeout) {
                        //     System.out.println("No message received from Server in One Minute. Would you like to \"quit\", \"continue\", or say \"goodbye\" to the session.");
                        //     String nextMove = scanner.nextLine();
                        //     if(nextMove.contains("quit")) {
                        //         noResponse = "QUIT " + newClient.getGameId();
                        //     } else if (nextMove.contains("goodbye")) {
                        //         noResponse = "GDBY " + newClient.getGameId();
                        //     } else {
                        //         startTime = System.currentTimeMillis();
                        //         continue;
                        //     }
                        // } 
                        continue;
                    }
                    System.out.println("Received response from the server: " + rawResponse);
                    // String[] response=in.readLine().split(" ");
                    String[] response = rawResponse.split(" ");
                    System.out.println("parsed response from the server: " + Arrays.toString(response));
                    String message = handleResponse(response, newClient);

                    // if(!noResponse.equals("")) {
                    //     message = noResponse;
                    // }

                    // System.out.println("parsed response from the server: " + response);
                    if (!newClient.getTerminated() && !message.equals("")) {
                        System.out.println("Sending message to server: " + message);
                        out.println(message + "\r\n");
                        //startTime = System.currentTimeMillis();
                        // out.println(message);
                        if(newClient.getLastCall().contains("QUIT")) {
                            newClient.setBoard("|*|*|*|*|*|*|*|*|*|");
                            newClient.setSymbol("");
                            newClient.setGameId("");
                            out.println(getSess(new String[]{"SESS", newClient.getSessionID(), newClient.getClientID()}, newClient));
                            //startTime = System.currentTimeMillis();
                        }
                    } 
                    if (newClient.getTerminated() && !newClient.getLastCall().contains("DONE")) {
                        if (response.length == 4) {
                            if (response[2].equals(newClient.getClientID())) {
                                System.out.println(
                                        "You have been declared the winner and the game is terminated! Congrats!");
                            } else {
                                System.out.println(
                                        "The game has been terminated and you have lost. Better luck next time!");
                            }
                        } else {
                            System.out.println(
                                    "No one was declared a winner, and the game has been terminated. Better luck next time!");
                        }
                    } else if (message.equals("")) {
                        // do something here to handle quit and stat
                    }
                }

                // System.out.print("Would u like to create a game or join an existing game?");
                // String action = scanner.nextLine();

                // Close the TCP connection
                socket.close();
                System.out.println("TCP connection closed.");
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }

        } else if (service.equalsIgnoreCase("UDP")) {
            try {
                InetAddress address = InetAddress.getByName(host);
                DatagramSocket socket = new DatagramSocket();

                String greetingMessage = "HELO " + protocolVersion + " " + newClient.getClientID() + "\r\n";
                byte[] sendData = greetingMessage.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
                socket.send(sendPacket);

                while (!newClient.getTerminated()) {
                    // Receive response from the server
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);

                    // Extract the response message
                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("Received response from the server: " + response);

                    // Handle the response
                    String[] responseParts = response.split(" ");
                    String message = handleResponse(responseParts, newClient);
                    if (!newClient.getTerminated() && !message.isEmpty()) {
                        System.out.println("Sending message to server: " + message);

                        // Send message to the server
                        message = message + "\r\n";
                        sendData = message.getBytes();
                        sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
                        socket.send(sendPacket);
                        if(newClient.getLastCall().contains("QUIT")) {
                            newClient.setBoard("|*|*|*|*|*|*|*|*|*|");
                            newClient.setSymbol("");
                            newClient.setGameId("");
                            message = (getSess(new String[]{"SESS", newClient.getSessionID(), newClient.getClientID()}, newClient)) + "\r\n";
                            sendData = message.getBytes();
                            sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
                            socket.send(sendPacket);
                        }
                    } 
                    if (newClient.getTerminated() && !newClient.getLastCall().contains("DONE")) {
                        if (responseParts.length == 4) {
                            if (responseParts[2].equals(newClient.getClientID())) {
                                System.out.println(
                                        "You have been declared the winner and the game is terminated! Congrats!");
                            } else {
                                System.out.println(
                                        "The game has been terminated and you have lost. Better luck next time!");
                            }
                        } else {
                            System.out.println(
                                    "No one was declared a winner, and the game has been terminated. Better luck next time!");
                        }
                    }
                }

                socket.close();
                System.out.println("UDP connection closed.");

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("Invalid service. Please choose TCP or UDP.");
        }
    }

    public static String handleResponse(String[] response, ClientHandler newClient) {
        String action = response[0];
        String request = "";
        if (action.equals("SESS")) { // emily make sure to prompt for creating, joining or looking at a list of open
                                     // session ids
            request = getSess(response, newClient);
        } else if (action.equals("BORD")) { // audrey
            request = getBord(response, newClient);
        } else if (action.equals("GAMS")) { // emily
            request = getGams(response, newClient);
        } else if (action.equals("GDBY")) { // audrey
            request = getGdby(response, newClient);
        } else if (action.equals("JOND")) { // emily
            request = getJond(response, newClient);
        } else if (action.equals("TERM")) { // audrey
            //newClient.setTerminated(true);
            if(response.length == 3) {
                System.out.println("This game has tied and the session will now shut.");
            }
            request = "";
            // request = "TERM";
        } else if (action.equals("YMRV")) { // emily
            request = getYrmv(response, newClient);
        } else {
            System.out.println("method not supported");
        }
        return request;
    }

    public static String getSess(String[] response, ClientHandler newClient) {
        // response holds sess info passed from server SESS version uniquesessionid
        newClient.setSessionID(response[2]);

        // make sure to prompt for creating, joining or looking at a list of open
        // session ids
        Scanner scanner = new Scanner(System.in);

        System.out.print("Would you like to \"create\" a game, \"join\" a game, or view a \"list\" of open games? ");
        String beginGame = scanner.nextLine();

        if (beginGame.contains("create")) {
            newClient.setLastCall("CREA");
            return "CREA " + newClient.getClientID();
        } else if (beginGame.contains("join")) {
            System.out.print("Please enter the Game ID you'd like to join: ");
            String gameID = scanner.nextLine();
            newClient.setLastCall("JOIN");
            return "JOIN " + gameID;
        } else if (beginGame.contains("list")) {
            // add curr and all
            System.out.print(
                    "Would you like to view all games currently \"open\", all games \"currently\" open and in-play, or \"all\" games including those that have concluded? ");
            String listType = scanner.nextLine();

            if (listType.contains("currently")) {
                newClient.setLastCall("LIST CURR");
                return "LIST CURR";
            } else if (listType.contains("all")) {
                newClient.setLastCall("LIST ALL");
                return "LIST ALL";
            } else if (listType.contains("open")) {
                newClient.setLastCall("LIST");
                return "LIST";
            }
            return "";
            
        } else {
            return "";
        }
    }

    public static String getBord(String[] response, ClientHandler newClient) {
        if(newClient.getLastCall().contains("STAT")) {
            Scanner scanner = new Scanner(System.in);
            if(response.length == 3) {
                System.out.println("This game: " + response[1] + " only has one player in it. Would you like to \"join\"? ");
                String joinGame = scanner.nextLine();

                if(joinGame.contains("join")) {
                    newClient.setLastCall("JOIN " + response[1]);
                    return "JOIN " + response[1];
                } else {
                    //check if this length is right
                    return getSess(new String[]{"SESS", newClient.getSessionID(), newClient.getClientID()}, newClient);
                }
            } else if(response.length == 7) {
                System.out.println("This game has finished. The winner was: " + response[6]);
                return getSess(new String[]{"SESS", newClient.getSessionID(), newClient.getClientID()}, newClient);
            } else if(response.length > 3) {
                System.out.println("This game is currently in the middle of play.");
                return getSess(new String[]{"SESS", newClient.getSessionID(), newClient.getClientID()}, newClient);
            }
        }
        else if (response.length == 3) { // If there is not enough players to be playing this game, the command will
                                    // respond solely with the game-identifier and the client-identifier of the
                                    // other player.
            System.out.println("There are not enough players playing the game.");
        } else if (response.length == 6) { // [gameid,clientid of X player,clientid of o player, clientid whose turn,
                                           // board symbols]
            newClient.setBoard(response[5]);
            System.out.println("Current Board: " + response[5]);
            System.out.println("X player: " + response[2]);
            System.out.println("O player: " + response[3]);
        } else if (response.length == 7) {// [gameid,clientid of X player,clientid of o player, clientid whose turn,
                                          // board symbols, clientid who won]
            System.out.println("The game has been won by " + response[6]);
            System.out.println("The final game board: " + response[5]);

            Scanner scanner = new Scanner(System.in);
            System.out.println("Would you like to \"end\" the session or start a \"new\" game? ");
            String nextStep = scanner.nextLine();
            if(nextStep.contains("end")) {
                newClient.setTerminated(true);
                newClient.setLastCall("DONE");
                return "GDBY " + response[1];
            } else {
                newClient.setBoard("|*|*|*|*|*|*|*|*|*|");
                newClient.setSymbol("");
                newClient.setGameId("");
                return getSess(new String[]{"SESS", newClient.getSessionID(), newClient.getClientID()}, newClient);
            }

        }
        return "";
    }

    public static String getGams(String[] response, ClientHandler newClient) {
        System.out.println("Here is the list of games:");
        for (int i = 1; i < response.length; i++) {
            System.out.println(response[i]);
        }
        if(response.length == 1) {
            System.out.println("There is no open games.");
            return getSess(new String[]{"SESS", newClient.getSessionID(), newClient.getClientID()}, newClient);
        }

        Scanner scanner = new Scanner(System.in);

        System.out.print("Would you like to view the \"status\" of a game, or \"join\" a game? ");
        String gameStat = scanner.nextLine();

        String gameID = "";
        if(gameStat.contains("status")) {
            System.out.print("Which game id would you like to view? ");
            gameID = scanner.nextLine();
            newClient.setLastCall("STAT " + gameID);

            return "STAT " + gameID;
        } else if (gameStat.contains("join")){
            System.out.print("Which game id would you like to join? ");
            gameID = scanner.nextLine();
        }
        newClient.setLastCall("JOIN " + gameID);

        return "JOIN " + gameID;
    }

    public static String getGdby(String[] response, ClientHandler newClient) {
        System.out.println("The server ended the session");
        newClient.setTerminated(true);
        // System.out.println("Do you wish to start a new session? (y/n)");
        return "";
    }

    public static String getJond(String[] response, ClientHandler newClient) {
        //Scanner scanner = new Scanner(System.in);
        newClient.setGameId(response[2]);
        System.out.println("You have now joined the game: " + response[2]);
        System.out.println("Please wait while we get the game started. If at any point once the game starts, you'd like to quit. Please enter \"quit\". If you'd like to leave the session with the server after the game starts altogether, please enter \"goodbye\".");
        // returns nothing because waiting for yrmv to be sent

        return "";
    }

    public static String getYrmv(String[] response, ClientHandler newClient) {
        Scanner scanner = new Scanner(System.in);

        // your move, game id, client whose turn it is id
        if (response[2].equals(newClient.getClientID())) {
            String boardCurr = newClient.getBoard();
            for (int i = 0; i < boardCurr.length(); i++) {
                System.out.print(boardCurr.charAt(i));
                if (i == 6 || i == 12) {
                    System.out.print("\n|");
                }
            }
            System.out.println("");
            System.out.println("It is your turn to make a move, which space would you like to occupy? ");
            String moveSpace = scanner.nextLine();

            if (moveSpace.contains("quit")) {
                newClient.setLastCall("QUIT " + response[1]);
                return "QUIT " + response[1];
            } else if (moveSpace.contains("goodbye")) {
                return "GDBY " + newClient.getGameId();
            }
            newClient.setLastCall("MOVE");

            return "MOVE " + response[1] + " " + moveSpace;
        } else {
            String boardCurr = newClient.getBoard();
            for (int i = 0; i < boardCurr.length(); i++) {
                System.out.print(boardCurr.charAt(i));
                if (i == 6 || i == 12) {
                    System.out.print("\n|");
                }
            }
            System.out.println("");
            System.out.println("It is not your move, please wait for player: " + response[2] + " to go.");

            return "";
        }
    }
}