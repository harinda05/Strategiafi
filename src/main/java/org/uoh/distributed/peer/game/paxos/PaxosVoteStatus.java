package org.uoh.distributed.peer.game.paxos;

public enum PaxosVoteStatus {
    PENDING,
    ACCEPTED,
    REJECTED,

    COMMITTED
}
