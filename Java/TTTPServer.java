package Java;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.*;
import Java.ClientSession;
import Java.ClientSessionThread;

public class TTTPServer {
    private ServerSocketChannel tcpServerChannel;
    private DatagramChannel udpServerChannel;
    private static HashMap<String, ClientSessionThread> clientThreads;
    private Set<Socket> activeTcpConnections = new HashSet<>();  
    private static int PORT = 3116;

    public static void main(String[] args) {
        TTTPServer tttpserver = new TTTPServer();
        tttpserver.start();      
    }

    
    public void start() {
        try {
            // Establish TCP Server Channel - Set non-blocking property to avoid connection timeout
            tcpServerChannel = ServerSocketChannel.open();
            tcpServerChannel.configureBlocking(false);
            tcpServerChannel.socket().bind(new InetSocketAddress(PORT));

            // Establish TCP Server Channel - Set non-blocking property to avoid connection timeout
            udpServerChannel = DatagramChannel.open();
            udpServerChannel.configureBlocking(false);
            udpServerChannel.socket().bind(new InetSocketAddress(PORT));

            // Declare map for storing client thread information
            clientThreads = new HashMap<>();
            System.out.println("Server started on port " + PORT + ".");

            // Open selector to process TCP/UDP server channels
            Selector selector = Selector.open();
            tcpServerChannel.register(selector, SelectionKey.OP_ACCEPT);
            udpServerChannel.register(selector, SelectionKey.OP_READ);

            // Continuously listen for new viable connections.
            while (true) {
                selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                for (SelectionKey key : selectedKeys) {
                    if (key.isAcceptable()) {
                        //handleTCPRequest();
                        try {
                            SocketChannel tcpClientChannel = tcpServerChannel.accept();
                            if (tcpClientChannel != null) {
                                tcpClientChannel.configureBlocking(false);
                                System.out.println("New TCP client connected: " + tcpClientChannel.socket().getInetAddress().getHostAddress());
                                handleTCPClientConnection(tcpClientChannel);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (key.isReadable()) {
                        try {
                            ByteBuffer buffer = ByteBuffer.allocate(256);
                            InetSocketAddress clientAddress = (InetSocketAddress) udpServerChannel.receive(buffer);
                            if (clientAddress != null) {
                                handleUDPClientConnection(buffer, clientAddress);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                selectedKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleTCPClientConnection(SocketChannel clientChannel) {
        try {
            String sessionId = ""; // Obtain session ID
            if (clientThreads.size() >= 10) {
                String message = "10 of 10 sessions already in use. Please wait for another user to disconnect.";
                clientChannel.write(ByteBuffer.wrap(message.getBytes()));
            } else if (clientThreads.containsKey(sessionId)) {
                String message = "Client session already in use.";
                clientChannel.write(ByteBuffer.wrap(message.getBytes()));
            } else {
                ClientSession session = new ClientSession(sessionId, clientChannel.socket());
                ClientSessionThread clientThread = new ClientSessionThread(session);
                clientThreads.put(sessionId, clientThread);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleUDPClientConnection(ByteBuffer buffer, InetSocketAddress clientAddress) {
        try {
            String sessionId = ""; // Obtain session ID
            if (clientThreads.size() >= 10) {
                String message = "10 of 10 sessions already in use. Please wait for another user to disconnect.";
                ByteBuffer responseBuffer = ByteBuffer.wrap(message.getBytes());
                udpServerChannel.send(responseBuffer, clientAddress);
            } else if (clientThreads.containsKey(sessionId)) {
                String message = "Client session already in use.";
                ByteBuffer responseBuffer = ByteBuffer.wrap(message.getBytes());
                udpServerChannel.send(responseBuffer, clientAddress);
            } else {
                SocketChannel udpClientChannel = SocketChannel.open();
                udpClientChannel.configureBlocking(false);
                udpClientChannel.connect(clientAddress);
    
                ClientSession session = new ClientSession(sessionId, udpClientChannel.socket());
                ClientSessionThread clientThread = new ClientSessionThread(session);
                clientThreads.put(sessionId, clientThread);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    

    // public void start2() {        
    //     try {
    //         tcpServerSocket = new ServerSocket(PORT);
    //         udpServerSocket = new DatagramSocket(PORT);
    //         ClientSession session = null;
    //         Socket tcpClientSocket = null;
    //         System.out.println("Server started on port 3116.");
            
    //         // Constantly listen to new client connections:
    //         while (true) {
    //             // Listen for any new connection
    //             // Check for TCP or UDP, if neither, continue looping and do nothing
    //             // If TCP: go into TCP
    //             // start thread
    //             // if UDP: go into UDP
    //             // start trhead
    //             String sessionId = ""; // -- // callCommand();

    //             // try catch - try both tcp and udp, pick non-null one, send to Client Session, create Client Session Thread thereafter...
    //             try {
    //                 tcpServerSocket.setSoTimeout(SOCKET_TIMEOUT);
    //                 tcpClientSocket = tcpServerSocket.accept();
    //                 System.out.println("New TCP client connected: " + tcpClientSocket.getInetAddress().getHostAddress());
    //                 if (clientThreads.size() >= 10) {
    //                     String message = "10 of 10 sessions already in use. Please wait for another user to disconnect.";
    //                     tcpClientSocket.getOutputStream().write(message.getBytes());
    //                     DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length);
    //                     udpServerSocket.send(packet);
    //                 } else if (clientThreads.containsKey(sessionId)) {
    //                     tcpClientSocket.getOutputStream().write(("Client session already in use.").getBytes());
    //                 } else {
    //                     session = new ClientSession(sessionId, tcpClientSocket); // TCP session
    //                     ClientSessionThread clientThread = new ClientSessionThread(session);
    //                     clientThreads.put(sessionId, clientThread);
    //                     clientThread.start();
    //                 }
    //             } catch (SocketTimeoutException e) {
    //                 // Timeout reached, continue loop without accepting TCP connection
    //             }

    //             // at Java.ClientSessionThread.run(ClientSessionThread.java:33)
    //             // New TCP client connected: 127.0.0.1
    //             // java.net.SocketException: Socket is closed
    //             //         at java.base/java.net.Socket.getInputStream(Socket.java:986)
    //             //         at Java.ClientSessionThread.handleTCPRequest(ClientSessionThread.java:54)

    //             byte[] buffer = new byte[256];
    //             DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
    //             try {
    //                 udpServerSocket.setSoTimeout(SOCKET_TIMEOUT);
    //                 udpServerSocket.receive(requestPacket);
    //                 System.out.println("Datagram Received.");
    //                 if (clientThreads.size() >= 10) {
    //                     String message = "10 of 10 sessions already in use. Please wait for another user to disconnect.";
    //                     tcpClientSocket.getOutputStream().write(message.getBytes());
    //                     DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length);
    //                     udpServerSocket.send(packet);
    //                 } else if (clientThreads.containsKey(sessionId)) {
    //                     tcpClientSocket.getOutputStream().write(("Client session already in use.").getBytes());
    //                 } else {
    //                     session = new ClientSession(sessionId, udpServerSocket, requestPacket); // UDP session
    //                     ClientSessionThread clientThread = new ClientSessionThread(session);
    //                     clientThreads.put(sessionId, clientThread);
    //                     clientThread.start();
    //                 }
    //             } catch (SocketTimeoutException e) {
    //                 // Timeout reached, continue loop without receiving UDP packet
    //             }

    //             // // if (tcpClientSocket == null &)
    //             // if (tcpClientSocket != null) {
    //             //     session = new ClientSession(sessionId, tcpClientSocket); // TCP session
    //             // } else if (requestPacket.getLength() > 0) {
    //             //     session = new ClientSession(sessionId, udpServerSocket, requestPacket);
    //             //     // UDP session
    //             // } else {
    //             //     break;
                  
    //             // }

                
    //         }
    //     } catch (IOException ioe) {
    //         ioe.printStackTrace();
    //     }
    // }

    // private static void handleTCPRequest() {
    //     Socket TCPSocket = null;
    //     try (ServerSocket serverSocket = new ServerSocket(PORT)) {
    //         System.out.println("TCP server started and listening on port " + PORT);
    //         while ((TCPSocket = serverSocket.accept()) != null) {
    //             System.out.println("New TCP client connected: " + TCPSocket.getInetAddress().getHostAddress());

    //             try (BufferedReader in = new BufferedReader(new InputStreamReader(TCPSocket.getInputStream()));
    //                 PrintWriter out = new PrintWriter(TCPSocket.getOutputStream(), true)) {
            
    //                 String request = in.readLine(); 
    //                 System.out.println("Received: " + request);
    //                 String response = callCommand(request);
    //                 System.out.println("Sending response: " + response);
    //                 out.println(response);

    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }  
    // }

    // private static void handleUDPRequest() {
    //     try (DatagramSocket UDPSocket = new DatagramSocket(PORT)) {
    //         System.out.println("UDP server started and listening on PORT " + PORT);

    //         while (true) {
    //             byte[] buffer = new byte[256];
    //             DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
    //             UDPSocket.receive(requestPacket);

    //             System.out.println("Datagram Received.");

    //             String request = new String(requestPacket.getData(), 0, requestPacket.getLength());
    //             System.out.println("Received Data: " + request);

    //             String response = callCommand(request);

    //             byte[] responseData = response.getBytes();
    //             DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, requestPacket.getAddress(), requestPacket.getPort());
                
    //             UDPSocket.send(responsePacket);
    //             UDPSocket.close();
    //         }
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    //Example Request: 
            // HELO 1 CID1
            // SESS SID2 CID2
            // BORD GID1 CID1 CID2 CID2
            //
            // |*|*|*|
            // |*|X|*|
            // |*|*|*|
            //

    // private static String callCommand(String request) {
    //     String[] requestArgs = request.split("\\s+");
    //     String command = requestArgs[0];
    //     String[] args = Arrays.copyOfRange(requestArgs, 1, requestArgs.length);

    //     String response = "";
        

        // if (command == "HELO" & args[0] != null) {
        //     String version = args[0];
        //     String clientIdentifier = args[1];
        //     if (clients.containsKey(clientIdentifier)) {
        //         return "";
        //     } else {

        //     }
        // } 
        // else if (command == "CREA" & args[0] != null) {
        //     String clientIdentifier = args[0];
        //     if (clients.containsKey(clientIdentifier)) {
        //         return "";
        //     }
        // } else { 

        //     if (COMMANDS.get(command).equals("request")) {
        //         return ch.handleRequest(command, args);
        //     } else {
        //         System.out.println("Invalid command: " + command);
        //         return "Error";
        //     }
        // }

        // if (COMMANDS.get(command).equals("request")) {
        //     response = ch.handleRequest(command, args);
        // } else {
        //     System.out.println("Invalid command: " + command);
        //     return "Error";
        // }

        // String[] responseParts = response.split("\\s+");
        // String responseCommand = responseParts[0];
        // String[] responseArgs = Arrays.copyOfRange(responseParts, 1, responseParts.length);

        // if (responseCommand.equals("SESS")) {
        //     String sessionId = responseArgs[0];
            
        // }

        
    // }
}


