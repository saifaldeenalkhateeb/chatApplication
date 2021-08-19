package com.haw.chatapplication.utils;

import com.google.gson.Gson;
import com.haw.chatapplication.model.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HelpFunctions {
    public static final int ACK_FLAG = 1;
    public static final int SEND_FLAG = 0;
    /**
     * Time to leave
     */
    public static int TTL = 10;
    public static final Gson gson = new Gson();


    public static void addNotExistingRoutingEntries(RoutingTable routingTable, Server server, Socket clientSocket) {
        for (RoutingEntry routingEntry : routingTable.getRoutingEntries()) {
            if (!server.getRoutingTable().getRoutingEntries().contains(routingEntry) &&
                    !routingEntry.equals(server.getServer_routingEntry())) {
                RoutingEntry rE = new RoutingEntry(routingEntry.getIp(), routingEntry.getPort(), routingEntry.getName());
                rE.setHopCount(routingEntry.getHopCount() + 1);
                rE.setSocket(clientSocket);
                System.out.printf("add Routing Entry to my routing table %s%n", rE.toString());
                server.getRoutingTable().add(rE);
            }
        }
    }

    public static void updateRoutingTable(RoutingTable routingTable, Server server, Socket clientSocket) {
        boolean removedRoutingEntry = checkIfRoutingEntryRemoved(routingTable, server);
        addNotExistingRoutingEntries(routingTable, server, clientSocket);
        if (removedRoutingEntry) {
            updateRoutingTableRemoved(routingTable, server);
        }
    }

    private static void updateRoutingTableRemoved(RoutingTable routingTable, Server server) {
        for (RoutingEntry routingEntry : server.getRoutingTable().getRoutingEntries()) {
            if (!routingTable.getRoutingEntries().contains(routingEntry)) {
                System.out.printf("remove routing entry from my routing table %s%n", routingEntry.toString());
                server.getRoutingTable().remove(routingEntry);
            }
        }
    }


    private static boolean checkIfRoutingEntryRemoved(RoutingTable routingTable, Server server) {
        if (server.getRoutingTable().getRoutingEntries().contains(routingTable.getRoutingEntries().stream()
                .filter(routingEntry -> routingEntry.getHopCount() == 0).findAny().orElse(null))) {
            for (RoutingEntry routingEntry : server.getRoutingTable().getRoutingEntries()) {
                if (!routingTable.getRoutingEntries().contains(routingEntry)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param toUser
     * @param answerFlag
     * @param message
     * @param server
     * @throws IOException
     */
    public static synchronized void send_message(User toUser, int answerFlag, Message message, Server server) throws IOException {
        Socket socket;
        if (toUser.equals(server.getServer_user())) {
            if (server.getClientServerSocket() == null) {
                server.setClientServerSocket(new Socket(server.getServer_ip(), server.getServer_port()));
            }
            socket = server.getClientServerSocket();
        } else {
            socket = getSocketByUser(toUser, server);
        }
        PrintWriter pr = new PrintWriter(socket.getOutputStream(), true);
        message.setAnswerFlag(answerFlag);
        message.setTimeStamp();
        pr.println(gson.toJson(message, Message.class));
    }


    /**
     * @param server
     * @throws IOException
     */
    public static void send_routing_information(Server server) throws IOException {
        List<Socket> neighboursSockets = getNeighboursSockets(server);
        for (Socket socket : neighboursSockets) {
            send_routing_information(server, socket);
        }
    }

    /**
     * @param content
     * @param server
     * @throws IOException
     */
    public static void send_public_message(String content, Server server) {
        Message message = new Message();
        message.setMessage(content);
        message.setTtl(TTL);
        // send the message to all my neighbours
        server.getRoutingTable().getRoutingEntries().stream().map(RoutingEntry::createUser).collect(Collectors.toList()).stream().filter(user ->
                !user.equals(server.getServer_user())).forEach(toUser -> {
            setMessageToDefaultServerData(message, server.getServer_ip(), server.getServer_port(), server.getServer_name(), toUser.getIp(), toUser.getPort(), toUser.getName());
            try {
                send_message(toUser, SEND_FLAG, message, server);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * @param toUser
     * @param content
     * @param server
     * @throws IOException
     */
    public static void send_private_message(User toUser, String content, Server server) throws IOException {
        // create new message object, the later will be sent
        Message message = new Message();
        // set the message attributes
        setMessageToDefaultServerData(message, server.getServer_ip(), server.getServer_port(), server.getServer_name(),
                toUser.getIp(), toUser.getPort(), toUser.getName());
        // set the message content attribute
        message.setMessage(content);
        // set the time to live attribute
        message.setTtl(TTL);
        send_message(toUser, SEND_FLAG, message, server);
    }

    /**
     * @param server
     * @param socket
     * @throws IOException
     */
    public static void send_routing_information(Server server, Socket socket) throws IOException {
        System.out.printf("sending routing information: %s to: %s%n", gson.toJson(server.getRoutingTable().getRoutingEntries(), ArrayList.class), socket.toString());
        PrintWriter pr = new PrintWriter(socket.getOutputStream(), true);
        pr.println(gson.toJson(server.getRoutingTable().getRoutingEntries(), ArrayList.class));
    }

    private static Socket getSocketByUser(User toUser, Server server) {
        return server.getRoutingTable().getRoutingEntries().stream().filter(routingEntry -> routingEntry.createUser().equals(toUser)).findAny().get().getSocket();
    }

    /**
     * set the attributes of a message object
     */
    public static void setMessageToDefaultServerData(Message sMessage, String fromIP, int fromPort, String fromName, String toIP, int toPort, String toName) {
        sMessage.setFromIP(fromIP);
        sMessage.setFromPort(fromPort);
        sMessage.setFromName(fromName);
        sMessage.setToIP(toIP);
        sMessage.setToPort(toPort);
        sMessage.setToName(toName);
    }

    /**
     * @param server my server
     * @return all sockets, the i have
     */
    static List<Socket> getNeighboursSockets(Server server) {
        return server.getRoutingTable().
                getRoutingEntries().stream().
                filter(routingEntry -> routingEntry.getSocket() != null &&
                        !routingEntry.createUser().equals(server.getServer_user())).
                map(RoutingEntry::getSocket).distinct().collect(Collectors.toList());

    }

    public static void removeRoutingEntries(Server server, Socket socket) {
        server.getRoutingTable().getRoutingEntries().removeIf(entry -> entry.getSocket() != null && entry.getSocket().toString().equals(socket.toString()));
    }


}
