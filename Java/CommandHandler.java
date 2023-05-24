package Java;

import java.util.HashSet;

public class CommandHandler {

    public CommandHandler() {

    }

    public void handleRequest(String command, String[] parameters) {
        switch (command) {
            case ("CREA"):
                System.out.println("Create called");
                createConnection(command);
                break;
            case ("GDBY"):
                // print Goodbye message from client
                quit(command);
                break;
            case ("HELO"):
                System.out.println("Helo called");
                break;
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
    }

    public String createResponse(String responseType, String[] parameters) {
        switch (responseType) {
            case ("BORD"):
                createConnection(responseType);
                break;
            case ("GAMS"):
                // print Goodbye message from client
                quit(responseType);
                break;
            case ("GDBY"):
                break;
            case ("JOND"):
                break;
            case ("SESS"):
                break;
            case ("TERM"):
                break;
            case ("YRMV"):
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
    private void createConnection(String sessionMsg) {
        // Listen until there are at-least two clients in that sess
        // if the game does not exist create a new session

        // add it to the hashset
        // else have them connect to existing session
        // respond with a joined an existing game message
    }

    /**
     * HELO
     * Client-sent message
     * 
     * @return session identifier
     */
    private void createSession() {
        // TODO
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
     * QUIT (GDBY)
     * Client-sent message
     * 
     * @param gameIdentifier -
     */
    private void quit(String gameIdentifier) {

    }

    /**
     * MOVE
     * Storing Cleint Move
     * 
     * @param moveRequest
     */

    private void move(String movRequest) {

    }

}