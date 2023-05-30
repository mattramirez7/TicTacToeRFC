package Java;

import java.io.PrintWriter;

public class ClientData {
    private int sessionId;
    private String gameId;
    private PrintWriter out;

    public ClientData(int sessionId, PrintWriter out) {
        this.sessionId = sessionId;
        this.gameId = null; // Initialize game ID as null (optional)
        this.out = out;
    }

    // Getters and setters (optional) for sessionId and gameId
    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public PrintWriter getOut() {
        return out;
    }
}