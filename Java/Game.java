package Java;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Game {
    private String gameBoard;
    private List<String> players;
    

    public Game() {
        this.gameBoard = "|**|*|*|*|*|*|*|*|";
        this.players = new ArrayList<String>();
    }

    public void addPlayer(String player) {
        this.players.add(player);
    }

    public String getBoard() {
        return this.gameBoard;
    }

    public void updateBoard(String board) {
        this.gameBoard = board;
    }

    public List<String> getPlayers() {
        return this.players;
    }
}