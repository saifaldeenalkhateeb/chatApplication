package com.haw.chatapplication.model;

public class Message {
    private int answerFlag;
    private String toIP;
    private int toPort;
    private String toName;
    private String fromIP;
    private int fromPort;
    private String fromName;
    private int ttl;
    private long timeStamp;
    private String message;

    public int getAnswerFlag() {
        return answerFlag;
    }

    public void setAnswerFlag(int answerFlag) {
        this.answerFlag = answerFlag;
    }

    public String getToIP() {
        return toIP;
    }

    public void setToIP(String toIP) {
        this.toIP = toIP;
    }

    public int getToPort() {
        return toPort;
    }

    public void setToPort(int toPort) {
        this.toPort = toPort;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getFromIP() {
        return fromIP;
    }

    public void setFromIP(String fromIP) {
        this.fromIP = fromIP;
    }

    public int getFromPort() {
        return fromPort;
    }

    public void setFromPort(int fromPort) {
        this.fromPort = fromPort;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp() {
        this.timeStamp = System.currentTimeMillis() / 10000;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Message{" +
                "answerFlag=" + answerFlag +
                ", toIP='" + toIP + '\'' +
                ", toPort=" + toPort +
                ", toName='" + toName + '\'' +
                ", fromIP='" + fromIP + '\'' +
                ", fromPort=" + fromPort +
                ", fromName='" + fromName + '\'' +
                ", ttl=" + ttl +
                ", timeStamp=" + timeStamp +
                ", message='" + message + '\'' +
                '}';
    }
}