package org.uoh.distributed.peer;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class StateManager
{
    private NodeState state;
    private Map<NodeState, Object> waiters = new HashMap<>();

    public StateManager(NodeState state) {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }
        this.state = state;
    }

    public synchronized void setState(NodeState newState) {
        state = newState;
        synchronized (this) {
            waiters.computeIfAbsent(state, k -> new Object());
        }

        synchronized (waiters.get(state)) {
            waiters.get(state).notify();
        }
    }

    public synchronized void checkState(NodeState... states) {
        if (states != null && Stream.of( states ).noneMatch( s -> this.state.equals( s ) ) ) {
            throw new IllegalStateException("System is at state: " + state);
        }
    }

    public synchronized boolean isState(NodeState state) {
        return this.state.equals(state);
    }


    public NodeState getState() {
        return state;
    }
}
