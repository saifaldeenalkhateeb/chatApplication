package com.haw.chatapplication.model;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class RoutingTable {
    private final BlockingQueue<RoutingEntry> routingEntries;

    /**
     * create an new routing table object
     */
    public RoutingTable() {
        routingEntries = new ArrayBlockingQueue<>(100);
    }

    /**
     * to add a new RoutingEntry
     *
     * @param routingEntry the information about a routingEntry
     */
    public void add(RoutingEntry routingEntry) {
        try {
            routingEntries.put(routingEntry);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void remove(RoutingEntry routingEntry) {
        routingEntries.removeIf(routingEntry::equals);
    }


    public BlockingQueue<RoutingEntry> getRoutingEntries() {
        return routingEntries;
    }
}
