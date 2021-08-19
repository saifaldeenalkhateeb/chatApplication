package com.haw.chatapplication.model;

import java.net.Socket;

public class SocketView {
    private final String socket;
    private final Boolean closed;

    public SocketView(Socket socket) {
        this.socket = socket.toString();
        this.closed = socket.isClosed();
    }

    public String getSocket() {
        return socket;
    }

    public Boolean getClosed() {
        return closed;
    }
}
