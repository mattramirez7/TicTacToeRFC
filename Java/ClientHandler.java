package Java;

public class ClientHandler {
    private String sessionID;
    private String clientIdentifier;
    private String symbol;
    private Boolean terminated;
    private String board;
    private String lastCall;

    public ClientHandler(String sessionID, String clientIdentifier, String symbol, Boolean terminated, String board, String lastCall) {
        this.sessionID = sessionID;
        this.clientIdentifier = clientIdentifier;
        this.symbol = symbol;
        this.terminated = terminated;
        this.board = board;
        this.lastCall = lastCall;
    }

    public String getSessionID() {
        return sessionID;
    }

    public String getClientID() {
        return clientIdentifier;
    }

    public String getSymbol() {
        return symbol;
    }

    public Boolean getTerminated() {
        return terminated;
    }

    public String getBoard() {
        return board;
    }

    public String getLastCall() {
        return lastCall;
    }

    public void setSessionID(String sessID) {
        sessionID = sessID;
    }

    public void setClientID(String clientID) {
        clientIdentifier = clientID;
    }

    public void setSymbol(String sym) {
        symbol = sym;
    }

    public void setTerminated(Boolean term) {
        terminated = term;
    }

    public void setBoard(String boardSent) {
        board = boardSent;
    }

    public void setLastCall(String last) {
        lastCall = last;
    }
}