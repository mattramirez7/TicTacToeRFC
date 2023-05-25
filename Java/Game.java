package Java;
import java.util.UUID;

public class Game {
    private String gameID; 
    private String[][] gameBoard;
    private String[] players;
    


    public Game(int boardSize, String gameID) {
        this.gameBoard = new String[boardSize][boardSize];
        this.gameID = gameID;
    }

    public String getGameID() {
        return this.gameID;
    }
    
    public String getStringBoard() {
        return null;
    }

    public String[][] getBoard() {
        return null;
    }
}