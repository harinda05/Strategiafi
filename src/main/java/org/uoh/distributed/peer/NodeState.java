package org.uoh.distributed.peer;

public enum NodeState
{
    /** Program hasn't started yet */
    IDLE,
    /** Registered in the bootstrap server. That means, we have got 2 nodes (max) to connect to */
    REGISTERED,
    /** Connected to first 2 peers and response arrived along with routing tables, etc */
    CONNECTING,
    /** Updated my routing table and chose a node ID */
    CONNECTED,
    /** Have undertaken keywords to be looked after by the node as well */
    CONFIGURED
}
