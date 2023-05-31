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

        ClientHandler newClient = new ClientHandler("", clientIdentifier, "", false, "|*|*|*|*|*|*|*|*|*|");

        System.out.print("Enter Version: ");
        String version = scanner.nextLine();

        if (service.equalsIgnoreCase("TCP")) {
            try {
                // Establish TCP connection
                Socket socket = new Socket(host, port);
                System.out.println("TCP connection established");

                // Create input and output streams for the socket
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Send greeting message
                out.println("HELO " + version + " " + newClient.getClientID() + "\r\n");
                // System.out.println("Hi! Welcome to the game of Tic Tac Toe, using the TTTP
                // defined in INFO 314. If at any point during the
                // the game you are playing you want to quit, simply enter \"quit\". If at any
                // time in the process you would like to check the stats of a particular game,
                // type \"stats\" and press enter. Best of luck!");

                // String acknowledgment = in.readLine();
                // System.out.println("Received acknowledgment from the server: " +
                // acknowledgment);

                // Receive acknowledgment
                while (!newClient.getTerminated()) {
                    // String awaitingServer = scanner.nextLine();
                    // if()
                    String rawResponse = in.readLine();
                    if (rawResponse == null | rawResponse.equals("\r\n") | rawResponse.equals("")) {
                        continue;
                    }
                    System.out.println("Received response from the server: " + rawResponse);
                    // String[] response=in.readLine().split(" ");
                    String[] response = rawResponse.split(" ");
                    System.out.println("parsed response from the server: " + Arrays.toString(response));
                    String message = handleResponse(response, newClient);
                    // System.out.println("parsed response from the server: " + response);
                    if (!newClient.getTerminated() && !message.equals("")) {
                        System.out.println("Sending message to server: " + message);
                        out.println(message + "\r\n");
                        // out.println(message);
                    } else if (newClient.getTerminated()) {
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

                String greetingMessage = "HELO " + version + " " + newClient.getClientID();
                byte[] sendData = greetingMessage.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
                socket.send(sendPacket);

                // System.out.println("Hi! Welcome to the game of Tic Tac Toe, using the TTTP
                // defined in INFO 314. If at any point during the
                // the game you are playing you want to quit, simply enter \"quit\". If at any
                // time in the process you would like to check the stats of a particular game,
                // type \"stats\" and press enter. Best of luck!");

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
                    } else if (newClient.getTerminated()) {
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
            newClient.setTerminated(true);
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
            return "CREA " + newClient.getClientID();
        } else if (beginGame.contains("join")) {
            System.out.print("Please enter the Game ID you'd like to join.");
            String gameID = scanner.nextLine();
            return "JOIN " + gameID;
        } else if (beginGame.contains("list")) {
            // add curr and all
            System.out.print(
                    "Would you like to view all games currently \"open\", all games \"currently\" open and in-play, or \"all\" games including those that have concluded?");
            String listType = scanner.nextLine();

            if (listType.contains("currently")) {
                return "LIST CURR";
            } else if (listType.contains("all")) {
                return "LIST ALL";
            } else if (listType.contains("open")) {
                return "LIST";
            }
            return "";
            // } else if (beginGame.contains("stat")) {
            // System.out.print("Please enter the Game ID you would like to see the stats
            // on.");
            // String gameID = scanner.nextLine();

            // return "STAT" + gameID;
            // }
        } else {
            return "";
        }
    }

    public static String getBord(String[] response, ClientHandler newClient) {
        if (response.length == 2) { // If there is not enough players to be playing this game, the command will
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
            newClient.setTerminated(true);
        }

        return "";
    }

    public static String getGams(String[] response, ClientHandler newClient) {
        System.out.println("Here is the list of games:");
        for (int i = 1; i < response.length; i++) {
            System.out.println(response[i]);
        }

        Scanner scanner = new Scanner(System.in);

        System.out.print("Which game listed above would you like to join?");
        String gameID = scanner.nextLine();

        return "JOIN " + gameID;
    }

    public static String getGdby(String[] response, ClientHandler newClient) {
        System.out.println("The server ended the session");
        newClient.setTerminated(true);
        // System.out.println("Do you wish to start a new session? (y/n)");
        return "";
    }

    public static String getJond(String[] response, ClientHandler newClient) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("You have now joined the game: " + response[2]);
        System.out.println("Please wait while we get the game started.");
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
            // System.out.println(newClient.getBoard());
            System.out.println("It is your turn to make a move, which space would you like to occupy?");
            String moveSpace = scanner.nextLine();
            if (moveSpace.contains("quit")) {
                return "QUIT " + response[1];
            }

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
            // System.out.println(newClient.getBoard());
            System.out.println("It is not your move, please wait for player: " + response[2] + " to go.");

            return "";
        }
    }
}