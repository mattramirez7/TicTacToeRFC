package Java;

import java.util.*;
import java.io.*;
import java.net.*;

public class ClientHandler {
    private String sessionID;
    private String clientIdentifier;
    private String symbol;
    private Boolean terminated;
    private String board;

    public ClientHandler (String sessionID, String clientIdentifier, String symbol, Boolean terminated, String board) {
        this.sessionID = sessionID;
        this.clientIdentifier = clientIdentifier;
        this.symbol = symbol;
        this.terminated = terminated;
        this.board = board;
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
}