package org.uoh.distributed.peer.game.paxos;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class PaxosProposal implements Serializable {
    private int proposalNumber;

    @Getter
    @Setter
    private String proposalType;

    public PaxosProposal(int proposalNumber) {
        this.proposalNumber = proposalNumber;
    }

    // You might want to include other methods relevant to Paxos proposals
}