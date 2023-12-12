package org.uoh.distributed.peer.game.paxos;

public enum PaxosProposalFinalStatus {
    QUORUM_RECEIVED,
    QUORUM_REJECTED,
    QUORUM_PENDING,
}
