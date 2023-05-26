package Java;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.UUID;

public class Game {
    private String gameId; 
    private String[][] gameBoard;
    private Map<String, String> players;
    private int boardSize;
    

    public Game(String gameId) {
        this.gameBoard = new String[3][3];
        this.gameId = gameId;
    }

    public Game(int boardSize, String gameId) {
        this.boardSize = boardSize;
        this.gameBoard = new String[boardSize][boardSize];
        this.gameId = gameId;
    }


    public String getGameId() {
        return this.gameId;
    }

    public String[][] getBoard() {
        return gameBoard;
    }

    public String getStringBoard() {
        String stringBoard = "";
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                stringBoard = stringBoard + gameBoard[i][j];
            }
        }
        return stringBoard;
    }

    private boolean gameIsOver() {
        // LOGIC
        return false;
    }

    public void move(String playerId, int x, int y) {
        if (x - 1 < 0 | x - 1 > boardSize | y - 1 < 0 | y - 1 > boardSize) {
            throw new InvalidParameterException("Parameters of out board game range.");
        }
        String markType = players.get(playerId); // Get either X or O depending on the player
        gameBoard[x][y] = markType;
    }

    public void move(String playerId, String X, String Y) {
        int x = Integer.parseInt(X);
        int y = Integer.parseInt(Y);
        if (x - 1 < 0 | x - 1 > boardSize | y - 1 < 0 | y - 1 > boardSize) {
            throw new InvalidParameterException("Parameters of out board game range.");
        }
        String markType = players.get(playerId); // Get either X or O depending on the player
        gameBoard[x][y] = markType;
    }
}