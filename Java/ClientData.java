package Java;

import java.io.PrintWriter;
import java.net.InetAddress;

public class ClientData {
    private int sessionId = -999;
    private String gameId;
    private PrintWriter out;
    private int portUDP = -999;
    private InetAddress ipAddress;

    /**
     * TCP Constructor
     * 
     * @param sessionId - keeps track of a session which corresponds to a client
     * @param gameId    - initial game id (optional)
     * @param out       - PrintWriter object used to send back messages during game
     */
    public ClientData(int sessionId, PrintWriter out) {
        this.sessionId = sessionId;
        this.gameId = null; // Initialize game ID as null (optional)
        this.out = out;
    }

    /**
     * UDP Constructor
     * 
     * @param sessionId - keeps track of a session which corresponds to client
     * @param portUDP   - Datagram packet-specific port (unique for each UDP
     *                  connection)
     * @param ipAddress - client's IP address
     */
    public ClientData(int port, InetAddress ipAddress) {
        this.portUDP = port;
        this.ipAddress = ipAddress;
    }

    // Getters and setters (optional) for sessionId and gameId
    public int getSessionId() {
        if (this.sessionId == -999) {
            return this.portUDP;
        }
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

    public int getPortUDP() {
        return this.portUDP;
    }

    public InetAddress getIpAddress() {
        return this.ipAddress;
    }
}