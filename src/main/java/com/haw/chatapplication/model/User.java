package com.haw.chatapplication.model;

import java.util.Objects;

public class User {

    private final String name;
    private final String ip;
    private final int port;

    public User(String name, String ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public User(String ip, int port) {
        this.name = "unset";
        this.ip = ip;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return getPort() == user.getPort() &&
                //Objects.equals(getName(), user.getName()) &&
                getIp().equals(user.getIp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getIp(), getPort());
        // return Objects.hash(getIp(), getPort());
    }

    @Override
    public String toString() {
        return name + " | " + ip + " | " + port;
    }
}

