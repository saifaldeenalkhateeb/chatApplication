package com.haw.chatapplication.model;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class Server extends Thread {
    private final RoutingTable routingTable;
    private final ServerSocket ss;
    private final String server_name;
    private final String server_ip;
    private final int server_port;
    private final User server_user;
    private final Client serverClient;
    private final RoutingEntry server_routingEntry;
    private Message message;
    private final BlockingQueue<Socket> clientSockets;
    private Socket clientServerSocket;


    public Server(int server_port, String server_name) throws IOException {
        this.server_name = server_name;
        this.server_port = server_port;
        this.ss = new ServerSocket(server_port, 0, InetAddress.getLocalHost());
        this.server_ip = ss.getInetAddress().getHostAddress();
        this.server_user = new User(server_name, server_ip, server_port);
        this.serverClient = new Client(this);
        this.server_routingEntry = new RoutingEntry(server_ip, server_port, server_name);
        this.routingTable = new RoutingTable();
        this.routingTable.add(this.server_routingEntry);
        this.message = null;
        this.clientSockets = new ArrayBlockingQueue<Socket>(30);
        this.clientServerSocket = null;
    }


    /**
     * get the connection form a client
     * put a new created user of the client with the socket the
     */
    @Override
    public void run() {
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                Socket clientSocket = ss.accept();
                clientSocket.setSoTimeout(100);
                System.out.printf("receive connection from %s%n", clientSocket.toString());
                addClientSocket(clientSocket);
            }
        } catch (IOException ignored) {
        }

    }


    void addClientSocket(Socket clientSocket, User user) {
        if (routingTable.getRoutingEntries().stream().anyMatch(routingEntry -> routingEntry.createUser().equals(user))) {
            for (RoutingEntry routingEntry : routingTable.getRoutingEntries()) {
                if (routingEntry.createUser().equals(user)) {
                    routingEntry.setSocket(clientSocket);
                }
            }
        }
        try {
            this.clientSockets.put(clientSocket);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void addClientSocket(Socket clientSocket) {
        try {
            this.clientSockets.put(clientSocket);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getServerInfo() {
        return "" + server_ip + "/" + server_port + "/" + server_name;
    }

    public void connectToPeer(String goalIP, int goalPort) {
        try {
            this.serverClient.connect(goalIP, goalPort);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<User> getParticipants() {
        return routingTable.getRoutingEntries().stream().map(routingEntry ->
                        new User(routingEntry.getName(),
                                routingEntry.getIp(),
                                routingEntry.getPort())).
                collect(Collectors.toList());
    }


    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public String getServer_name() {
        return server_name;
    }

    public String getServer_ip() {
        return server_ip;
    }

    public int getServer_port() {
        return server_port;
    }

    public User getServer_user() {
        return server_user;
    }

    public Message getMessage() {
        return this.message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public RoutingEntry getServer_routingEntry() {
        return server_routingEntry;
    }

    public void disconnect() {
        if (clientServerSocket != null) {
            try {
                clientServerSocket.shutdownInput();
                clientServerSocket.shutdownOutput();
                clientServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            this.ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Socket s : clientSockets) {
            if (!s.isClosed()) {
                try {
                    s.close();
                } catch (IOException ignored) {

                }
            }
        }
    }

    public Socket getClientServerSocket() {
        return this.clientServerSocket;
    }

    public void setClientServerSocket(Socket clientServerSocket) {
        this.clientServerSocket = clientServerSocket;
        routingTable.getRoutingEntries().stream().filter(routingEntry -> routingEntry.createUser().equals(server_user)).findAny().get().setSocket(clientServerSocket);
    }

    public BlockingQueue<Socket> getClientSockets() {
        return this.clientSockets;
    }

}
