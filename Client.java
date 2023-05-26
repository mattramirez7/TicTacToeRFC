import java.util.*;
import java.io.*;
import java.net.*;

public class Client {
    String sessionID;
    String clientIdentifier;
    String symbol;
    Boolean terminated;
    String board;

    public static void main(String...args){
        String host = "localhost";
        int port = 3116;
        String protocolVersion = "1";
        terminated = false;

        Scanner scanner = new Scanner(System.in);

        System.out.print("TCP or UDP: ");
        String service = scanner.nextLine();

        System.out.print("Enter your Client Identifier: ");
        clientIdentifier = scanner.nextLine();

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
            out.println("HELO " + version + " " + clientId);

            // String acknowledgment = in.readLine();
            // System.out.println("Received acknowledgment from the server: " + acknowledgment);

            // Receive acknowledgment
            while(!gameTermined){
                String[] response=in.readLine().split(" ");
                String message= handleResponse(response);
                if(!gameTerminated && !message.equals("")){
                    out.println(message + "\r\n");
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
            
        } else {
            System.out.println("Invalid service. Please choose TCP or UDP.");
        }

        public static String handleResponse(String[] response){
            String action = response[0];
            String request = "";
            if(action.equals("SESS")){ //emily make sure to prompt for creating, joining or looking at a list of open session ids
                request = getSess(response);
            } else if (action.equals("BORD")){ // audrey
                request = getJOHD(response);
            } else if (action.equals("GAMS")){ // emily
                request = getGams(response);
            } else if (action.equals("GDBY")){ // audrey print goodbye close socket, update boolean
                request = getJOHD(response);
            } else if (action.equals("JOND")){ // emily
                request = getJond(response);
            } else if (action.equals("TERM")){ // audrey
                request = "TERM";
            } else if (action.equals("YRMV")){ //emily
                request = getYrmv(response);
            }else{
                System.out.println("method not supported");
            }
            return request;
        }

        public static String getSess(String[] response){
            //response holds sess info passed from server SESS version uniquesessionid
            sessionID = response[2];

            //make sure to prompt for creating, joining or looking at a list of open session ids
            Scanner scanner = new Scanner(System.in);

            System.out.print("Would you like to \"create\" a game, \"join\" a game, or view a \"list\" of open games?");
            String beginGame = scanner.nextLine();

            if(beginGame.contains("create")) {
                return "CREA " + clientIdentifier;
            } else if(beginGame.contains("join")) {
                System.out.print("Please enter the Game ID you'd like to join.");
                String gameID = scanner.nextLine();
                return "JOIN " + gameID;
            } else if(beginGame.contains("list")) {
                //add curr and all
                System.out.print("Would you like to view all games \"currently\" open and in-play, or \"all\" games including those that have concluded?");
                String listType = scanner.nextLine();

                if(listType.contains("currently")) {
                    return "LIST CURR";
                } else if(listType.contains("all")) {
                    return "LIST ALL";
                }
                return "";
            } else {
                return "";
            }
        }

        public static String getGams(String[] response){
            System.out.println("Here is a list of available games:");
            for(int i = 1; i < response.length, i++) {
                System.out.println(response[i]);
            }

            Scanner scanner = new Scanner(System.in);

            System.out.print("Which game listed above would you like to join?");
            String gameID = scanner.nextLine();

            return "JOIN " + gameID;
        }

        public static String getJond(String[] response){
            System.out.println("You have now joined the game: " + response[2]);
            System.out.println("Please wait while we get the game started.");
            //returns nothing because waiting for yrmv to be sent
            return "";
        }

        public static String getYrmv(String[] response){
            Scanner scanner = new Scanner(System.in);

            //your move, game id, client whose turn it is id
            if(response[2].equals(clientIdentifier)) {
                System.out.println(board);
                System.out.print("It is your turn to make a move, which space would you like to occupy?");
                String moveSpace = scanner.nextLine();

                return "MOVE " + response[1] + " " + moveSpace;
            } else {
                System.out.println(board);
                System.out.println("It is not your move, please wait for player: " + response[2] + " to go.");
                return "";
            }
        }

    
    }



    {/*

    -Client is prompted for TCP or UDP
    -Ask for Self identifying name
    -Establish Connected
    -Start new game or Join existing 
        -if new game send CREA command
        -if Join send Join command
        -or LIST command

    -JOND print out youve joined a game and game Identifier string (store this)+ print out
    -
    -BORD: print out board to command line do nothing else 
    -YRMV
        -first yrmv command check for which symbol 
        -others, check if symbol null 
            -otherwise check if your move

    -prompt print out where to move 
    -send MOVE command
    -at any point client can quit send QUIT GID to server 
    -loop back to do u want to start or join a game

    - create class to check for command 
     */}
}