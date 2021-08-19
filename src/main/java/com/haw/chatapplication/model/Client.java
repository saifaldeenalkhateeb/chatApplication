package com.haw.chatapplication.model;

import com.haw.chatapplication.utils.HelpFunctions;

import java.io.IOException;
import java.net.Socket;

public class Client {
    private final Server server;

    public Client(Server server) {
        this.server = server;
    }

    /**
     * to connect to a specify ip address and port
     *
     * @throws IOException
     */
    public synchronized void connect(String goalIP, int goalPort) throws IOException, InterruptedException {
        System.out.printf("create connection to %s %s%n", goalIP, goalPort + "");
        Socket clientSocket = new Socket(goalIP, goalPort);
        System.out.printf("connection to %s %s was created%n", goalIP, goalPort + "");
        clientSocket.setSoTimeout(100);
        server.addClientSocket(clientSocket, new User(goalIP, goalPort));
        System.out.printf("sending routing information to %s %s%n", goalIP, goalPort);
        HelpFunctions.send_routing_information(server, clientSocket);
    }
}