package org.uoh.distributed.peer.game.services;
import org.uoh.distributed.peer.game.Action;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientToServerSingleton {

    private static ClientToServerSingleton instance;

    private final ConcurrentLinkedQueue<Action> clientEgressQueue = new ConcurrentLinkedQueue<>();

    private ClientToServerSingleton() {
    }

    /** Returns a singleton instance of the class **/
    public static ClientToServerSingleton getInstance() {
        if (instance == null) {
            instance = new ClientToServerSingleton();
        }
        return instance;
    }
    public void produce(Action message) {
        clientEgressQueue.offer(message); // Non-blocking offer operation
    }

    public Action consume() {
        return clientEgressQueue.poll(); // Non-blocking poll operation
    }

}
