package Java;

import java.util.HashMap;
import java.util.List;

public class CommandHandler {

    private HashMap<String, ClientData> clientList;
    private int currentSessionId;
    private HashMap<String, Game> games;
    private int protocolVersion = 9999;

    public CommandHandler(HashMap<String, ClientData> clientList, int sessionID, HashMap<String, Game> games) {
        this.clientList = clientList;
        this.currentSessionId = sessionID;
        this.games = games;
        
    }

    public String handleRequest(String command, String[] parameters) {
        switch (command) {
            case ("CREA"):
                return createGame(parameters);
            case ("GDBY"):  
                // return quit(command);
                break;
            case ("HELO"):
                return createSession(parameters);
            case ("JOIN"):
                return joinGame(parameters);
            case ("LIST"):
                return listAvailableGames(parameters);
            case ("MOVE"):
                return move(parameters);
            case ("QUIT"):
                return quit(parameters);
            case ("STAT"):
                return getGameStatus(parameters);
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

        String gameId = "GID" + games.size();
        return "JOND " + clientId + " " + gameId;
    }

    public int getVersion() {
        return this.protocolVersion;
    }


    /**
     * HELO
     * Client-sent message
     * 
     * @return session identifier
     */
    private String createSession(String[] parameters) {
        if (parameters.length < 2) {
            return "ERROR: Invalid Parameters";
        }
        this.protocolVersion = Integer.valueOf(parameters[0]);
        System.out.println("INITIAL CLIENT VERSION: " + protocolVersion);
        String clientId = parameters[1];

        for (String client : clientList.keySet()) {
            if (clientList.get(client).getSessionId() == currentSessionId) {
                return "ERROR: Session has already been created";
            }
        }
        if (clientList.keySet().contains(clientId)) {
            return "ERROR: Identifier \'" + clientId + "\' is unavailable";
        }        

        return "SESS " + protocolVersion + " " + this.currentSessionId;
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

    private String joinGame(String[] parameters) {
        String gameId = parameters[0];
        String clientId = "";
        for (String id : clientList.keySet()) {
            if (clientList.get(id).getSessionId() == currentSessionId) {
                clientId = id;
            }
        }
        if (clientId.equals("")) {
            return "ERROR: Session has not been created";
        }
        if (games.get(gameId) == null) {
            return "ERROR: Game " + "\'" + gameId + "\' does not exist";
        }
        if (games.get(gameId).getPlayers().size() > 1) {
            return "ERROR: Max 2 players per game";
        }

        return "JOND " + clientId + " " + gameId;
    }

    /**
     * LIST
     * Client-sent message
     * 
     */
    private String listAvailableGames(String[] parameters) {
        String body = "";
        if (parameters.length == 1) {
            body = parameters[0];
        }
        String response = "GAMS";

        for (String gameId : games.keySet()) {
            if (body.equals("CURR")) {
                String boardStatus = games.get(gameId).getBoardStatus();
                String[] args = boardStatus.split("\\s+");
                if (args.length == 3 || args.length == 5) {
                    response += " " + gameId;
                }
            } else if (body.equals("ALL")) {
                response += " " + gameId;
            } else {
                if (games.get(gameId).getPlayers().size() < 2) {
                    response += " " + gameId;
                }
            }
        }

        // if (response.equals("GAMS")) {
        //     response = "ERROR: No games available to join";
        // }
        return response;
    }

    /**
     * MOVE
     * Storing Client Move
     * 
     * @param moveRequest
     */

    private String move(String[] parameters) {
        String gameId = parameters[0];
        if (games.get(gameId) == null) {
            return "ERROR: Game does not exist";
        }
        int move = Integer.parseInt(parameters[1]);
        List<String> players = games.get(gameId).getPlayers();
        Game game = games.get(gameId);
        String gameBoard = game.getBoard();
        String[] boardContent = gameBoard.substring(1).split("\\|");
        int remainingStars = gameBoard.length() - gameBoard.replace("*", "").length();
        String currentPlayer = remainingStars % 2 == 0 ? players.get(1) : players.get(0);
        String nextPlayer = currentPlayer.equals(players.get(0)) ? players.get(1) : players.get(0);

        String marker = currentPlayer.equals(players.get(0)) ? "X" : "O";

        if (move < 1 || move > 9) {
            return "YMRV " + gameId + " " + currentPlayer;
        }
        if (!boardContent[move - 1].trim().equals("*")) {
            return "YMRV " + gameId + " " + currentPlayer;
        }
        String updatedGameBoard = gameBoard.substring(0, (move * 2) - 1) + marker + gameBoard.substring(move * 2);

        game.updateBoard(updatedGameBoard);

        if (game.gameFinished()) {
            String winner = game.getWinner(updatedGameBoard);
            return "BORD " + gameId + " " + players.get(0) + " " + players.get(1) + " " + nextPlayer + " "
                + updatedGameBoard + " " + winner;
        }

        return "BORD " + gameId + " " + players.get(0) + " " + players.get(1) + " " + nextPlayer + " "
                + updatedGameBoard;
    }

    /**
     * QUIT 
     * Client-sent message
     * 
     */
    private String quit(String[] parameters) {
        String gameId = parameters[0];
        String clientId = "";
        for (String id : clientList.keySet()) {
            if (clientList.get(id).getSessionId() == currentSessionId) {
                clientId = id;
            }
        }
        String defaultWinner = "";
        for (String player: games.get(gameId).getPlayers()) {
            if (!player.equals(clientId)) {
                defaultWinner = player;
            }
        }
        return "QUIT " + gameId + " " + defaultWinner;
    }

    /**
     * STAT:
     * Client-sent message
     * 
     */

    private String getGameStatus(String[] parameters) {
        String gameId = parameters[0];

        if (games.get(gameId) == null) {
            return "ERROR: Game " + "\'" + gameId + "\' does not exist";
        }
       
        return games.get(gameId).getBoardStatus();
    }

}