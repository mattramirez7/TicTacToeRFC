package Java;

import java.util.HashMap;
import java.util.HashSet;


public class CommandHandler {

    private HashMap<String, TTTPServer.ClientData> clientList;
    private int currentSessionId;
    private HashMap<String, Game> games;

    public CommandHandler(HashMap<String, TTTPServer.ClientData> clientList, int sessionID, HashMap<String, Game> games) {
        this.clientList = clientList;
        this.currentSessionId = sessionID;
        this.games = games;
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
        if (clientList.get(clientId) == null) {
            return "Please start a session first";
        }

        if (clientList.get(clientId).getGameId() != null) {
            return "ERROR: Already in game";
        }

        String gameId = "GID" + games.size() ;
        return "JOND " + clientId + " " + gameId;
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
        

            for (String client : clientList.keySet()) {
                if (clientList.get(client).getSessionId() == currentSessionId) {
                    return "ERROR: Session has already been created"; 
                }
            }
            if (clientList.keySet().contains(clientId)) {
                return "ERROR: Identifier \'" + clientId + "\' is unavailable";
            }
            return "SESS " + currentSessionId + " " + clientId;
        

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
    // return [] ;
    // }

}