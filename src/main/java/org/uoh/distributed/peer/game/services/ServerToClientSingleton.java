package org.uoh.distributed.peer.game.services;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerToClientSingleton {
    private final ConcurrentLinkedQueue<String> clientIngressQueue = new ConcurrentLinkedQueue<>();

    public void produce(String message) {
        clientIngressQueue.offer(message); // Non-blocking offer operation
    }

    public String consume() {
        return clientIngressQueue.poll(); // Non-blocking poll operation
    }
}
