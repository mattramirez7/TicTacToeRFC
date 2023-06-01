package Java;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private int boardSize;
    private String[][] gameBoard;
    private String stringBoard;
    private List<String> players;
    private String winnerMarker;
    private String boardStatus;


    public Game() {
        this.boardSize = 3;
        this.gameBoard = new String[3][3];
        this.stringBoard = "|*|*|*|*|*|*|*|*|*|";
        updateBoardMatrix();
        this.players = new ArrayList<String>();
        this.boardStatus = "BORD";
    }

    public Game(int boardSize) {
        if (boardSize < 3 | boardSize > 10) {
            boardSize = 3;
        } else {
            this.boardSize = boardSize;
        }
        this.gameBoard = new String[boardSize][boardSize];
    }

    public void addPlayer(String player) {
        this.players.add(player);
    }

    public String getBoard() {
        return parseArrayToString(gameBoard);
    }

    public void updateBoard(String board) {
        this.stringBoard = board;
        updateBoardMatrix();
    }

    public void setBoardStatus(String boardStatus) {
        this.boardStatus = boardStatus;
    }

    public String getBoardStatus() {
        return boardStatus;
    }

    public String parseArrayToString(String[][] board) {
        int size = board.length;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < size; i++) {
            sb.append("|");

            for (int j = 0; j < size; j++) {
                sb.append(board[i][j]);
                if (j < size - 1) {
                    sb.append("|");
                }
            }
        }
        sb.append("|");
        return sb.toString();
    }

    public List<String> getPlayers() {
        return this.players;
    }

    public boolean gameFinished() {
        if (!checkWinningCondition()) {
            for (String[] row : this.gameBoard) {
                for (int i = 0; i < row.length; i++) {
                    if (row[i].equals("*")) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    public String getWinner() {
        if (!gameFinished()) {
            return null;
        }
        return winnerMarker == "X" ? players.get(1) : players.get(0);
    }

    private boolean checkWinningCondition() {
        int size = gameBoard.length;

        // Check rows
        for (int i = 0; i < size; i++) {
            if (gameBoard[i][0].equals(gameBoard[i][1]) && gameBoard[i][1].equals(gameBoard[i][2])
                    && !gameBoard[i][0].equals("*")) {
                winnerMarker = gameBoard[i][0];
                return true;
            }
        }

        // Check columns
        for (int j = 0; j < size; j++) {
            if (gameBoard[0][j].equals(gameBoard[1][j]) && gameBoard[1][j].equals(gameBoard[2][j])
                    && !gameBoard[0][j].equals("*")) {
                winnerMarker = gameBoard[0][j];
                return true;
            }
        }

        // Check diagonals
        if ((gameBoard[0][0].equals(gameBoard[1][1]) && gameBoard[1][1].equals(gameBoard[2][2])
                && !gameBoard[0][0].equals("*"))
                || (gameBoard[0][2].equals(gameBoard[1][1]) && gameBoard[1][1].equals(gameBoard[2][0])
                        && !gameBoard[0][2].equals("*"))) {
            winnerMarker = gameBoard[0][0];
            return true;
        }

        return false;
    }

    private void updateBoardMatrix() {
        int index = 1;
        for (int i = 0; i < this.boardSize; i++) {
            for (int j = 0; j < this.boardSize; j++) {
                this.gameBoard[i][j] = Character.toString(this.stringBoard.charAt(index));
                index += 2; // Move to the next cell in the string representation
            }
        }
    }

}