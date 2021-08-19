package com.haw.chatapplication.model;

import java.net.Socket;
import java.util.Objects;

public class RoutingEntry {
    private final String ip;
    private int port;
    private final String name;
    private int hopCount;
    private transient Socket socket;

    public RoutingEntry(String ip, int port, String name) {
        this.ip = ip;
        this.port = port;
        this.name = name;
        this.hopCount = 0;
        this.socket = null;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getHopCount() {
        return hopCount;
    }

    public synchronized RoutingEntry setHopCount(int hopCount) {
        this.hopCount = hopCount;
        return this;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoutingEntry routingEntry)) return false;
        return getPort() == routingEntry.getPort() &&
                getIp().equals(routingEntry.getIp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIp(), getPort());
    }

    public User createUser() {
        return new User(this.getName(), this.getIp(), this.getPort());
    }

    @Override
    public String toString() {
        return "RoutingEntry{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", name='" + name + '\'' +
                ", hopCount=" + hopCount +
                ", socket=" + socket +
                '}';
    }
}