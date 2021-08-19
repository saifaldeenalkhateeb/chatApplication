package com.haw.chatapplication.model;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.haw.chatapplication.utils.HelpFunctions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import static com.haw.chatapplication.utils.HelpFunctions.*;

public class MessageHandler extends Thread {
    Server server;


    public MessageHandler(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        while (true) {
            BlockingQueue<Socket> list = server.getClientSockets();
            for (Socket socket : list) {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    String input = "";
                    input = bufferedReader.readLine();
                    handle_coming_message(input, socket);
                } catch (SocketTimeoutException ignored) {
                } catch (IOException | NullPointerException e) {
                    if (list.remove(socket)) {
                        System.out.println("Connection: " + socket + " is not available.");
                        System.out.printf("remove routing entries, the reachable vie the socket %s%n", socket.toString());
                        removeRoutingEntries(server, socket);
                        try {
                            System.out.printf("send routing information to my Neighbours after removing routing entries, the reachable vie the socket %s%n", socket.toString());
                            HelpFunctions.send_routing_information(server);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }

                    }
                }
            }
        }
    }

    /**
     * handle the coming message of the clients
     *
     * @throws IOException
     */
    private void handle_coming_message(String input, Socket socket) throws IOException {
        System.out.printf("receiving message: %s from socket %s%n", input, socket);
        Message message = null;
        RoutingTable routingTable;
        try {
            message = gson.fromJson(input, Message.class);
        } catch (JsonSyntaxException ignored) {
        }
        if (message != null) {
            System.out.println("message was a Message Form");
            User toUser;
            toUser = new User(message.getToName(), message.getToIP(), message.getToPort());
            User fromUser;
            fromUser = new User(message.getFromName(), message.getFromIP(), message.getFromPort());
            // FALS DIE NACHICHT EINE EMPFANGSBESTÄTIGUNG 0 ODER SENDEBESTÄTIGUNG 1
            // Erstens: Ist die Nachricht für mich? oder soll ich die Nachricht weiterleiten ?
            if (toUser.equals(server.getServer_user())) {
                switch (message.getAnswerFlag()) {
                    case ACK_FLAG -> {
                        System.out.println("message was an ACK");
                        message.setMessage(message.getMessage() + " was successfully delivered " + message.getTimeStamp());
                        server.setMessage(message);
                    }
                    case SEND_FLAG -> {
                        server.setMessage(message);
                        Message message_to_sent = new Message();
                        message_to_sent.setTtl(TTL);
                        message_to_sent.setMessage(message.getMessage());
                        setMessageToDefaultServerData(message_to_sent, server.getServer_ip(), server.getServer_port(), server.getServer_name(),
                                fromUser.getIp(), fromUser.getPort(), fromUser.getName());
                        System.out.printf("send an ACK Message to: %s%n", fromUser.toString());
                        send_message(fromUser, ACK_FLAG, message_to_sent, server);

                    }
                    default -> {
                    }
                }

            } else {
                // Know we've to forward the message
                if (message.getTtl() - 1 == 0) {
                    System.out.println("Message deleted: " + message);
                    message.setMessage(String.format("your message with content: %s to %s was unreachable", message.getMessage(), message.getToIP()));
                    System.out.printf("send a Message to %s for deleting his Message%n", fromUser);
                    send_message(fromUser, ACK_FLAG, message, server);
                } else {
                    message.setTtl(message.getTtl() - 1);
                    System.out.printf("forwarding message %s%n", message.toString());
                    send_message(toUser, message.getAnswerFlag(), message, server);
                }
            }
        } else {
            System.out.println("message was a Routing Table Form");
            routingTable = new RoutingTable();
            routingTable.getRoutingEntries().addAll(gson.fromJson(input, new TypeToken<List<RoutingEntry>>() {
            }.getType()));
            System.out.printf("receive Routing information %s%n", routingTable.getRoutingEntries().toString());
            // if the message contains routing information
            // update routing information
            int s1 = server.getRoutingTable().getRoutingEntries().size();
            System.out.println("update routing information");
            System.out.printf("updated routing information: %s%n", server.getRoutingTable().getRoutingEntries().toString());
            updateRoutingTable(routingTable, server, socket);
            int s2 = server.getRoutingTable().getRoutingEntries().size();
            if (s1 != s2) {
                // send the new updated routing information to all my neighbours
                System.out.println("send routing information to my Neighbours");
                send_routing_information(server);
            }
        }
    }
}