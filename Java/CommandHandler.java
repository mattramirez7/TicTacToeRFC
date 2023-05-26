package Java;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandHandler {

    private AtomicInteger sessionIdCounter = new AtomicInteger(1);
    private AtomicInteger gameIdCounter = new AtomicInteger(1);
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

    public CommandHandler() {
        
    }

    public String callCommand(String request) {
        String[] requestArgs = request.split("\\s+");
        String command = requestArgs[0];
        String[] args = Arrays.copyOfRange(requestArgs, 1, requestArgs.length);



            if (COMMANDS.get(command).equals("request")) {
                return handleRequest(command, args);
            } else {
                System.out.println("Invalid command: " + command);
                return "Error";
            }
    }

    public String handleRequest(String command, String[] parameters) {
        switch (command) {
            case ("CREA"):
                return createGame(parameters);
            case ("GDBY"):
                // print Goodbye message from client
                quit(command);
                break;
            case ("HELO"):
                return createSession(parameters);
            case ("JOIN"):
                break;
            case ("LIST"):
                break;
            case ("MOVE"):
                move(command);
                break;
            case ("QUIT"):
                break;
            case ("STAT"):
                break;
            default:
                break;
        }
        return "";
    }

    /**
     * CREA
     * Client-sent message
     * 
     * @param sessionMsg - recieved client session message
     * @return void - instantiates new game session for the client given the
     *         specified individual identifier
     */
    private String createGame(String[] parameters) {
        String clientId = parameters[0];
        String gameId = generateGameId();
        System.out.println("Generated Game ID: " + gameId);
        return "JOND " + clientId + " " + gameId;
    }

    private String generateGameId() {
        int gameId = gameIdCounter.getAndIncrement();
        return "SID" + gameId;
    }

    /**
     * HELO
     * Client-sent message
     * 
     * @return session identifier
     */
    private String createSession(String[] parameters) {
        String version = parameters[0];
        String clientId = parameters[1];

        String sessionId = generateSessionId();
        System.out.println("Generated Session ID: " + sessionId);

        return "SESS " + sessionId + " " + clientId;

    }

    private String generateSessionId() {
        int sessionId = sessionIdCounter.getAndIncrement();
        return "SID" + sessionId;
    }

    /**
     * JOIN
     * Client-sent message
     * Connects to an on-going, already-created game specified by the gameIdentifier
     * parameter.
     * 
     * @param gameIdentifier - recieved client session message
     * @return void - instantiates new game session for the client given the
     *         specified individual identifier
     */
    private void joinGame(String gameIdentifier) {


    }

    private String selectedGame() {
        // Select from the listed games
        return "";
    }


    /**
     * LIST
     * Client-sent message
     * 
     * @return HashSet<String>
     */
    private HashSet<String> listAvailableGames() {
        // TODO: Need To Create Globa HashSet of all The Games
        // return avaliableGames;
        return null;
    }

     /**
     * MOVE
     * Storing Cleint Move
     * 
     * @param moveRequest
     */

    private void move(String movRequest) {

    }

    /**
     * QUIT (GDBY)
     * Client-sent message
     * 
     * @param gameIdentifier -
     */
    private void quit(String gameIdentifier) {

    }

    /**
     * STAT: 
     * Client-sent message
     * 
     * @param gameIdentifier -
     */

    // private String[] stats(String gameIdentifier){        
    //     return [] ;
    // }

    

    
    // public String createResponse(String responseType, String[] parameters) {
    //     switch (responseType) {
    //         case ("BORD"):
    //             break;
    //         case ("GAMS"):
    //             break;
    //         case ("GDBY"):
    //             break;
    //         case ("JOND"):
    //             break;
    //         case ("SESS"):
    //             break;
    //         case ("TERM"):
    //             break;
    //         case ("YRMV"):
    //             break;
    //         default:
    //             break;
    //     }
    //     return "";
    // }
}