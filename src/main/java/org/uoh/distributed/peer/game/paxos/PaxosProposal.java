package org.uoh.distributed.peer.game.paxos;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
@Getter
@Setter
public class PaxosProposal implements Serializable {
    private int proposalNumber;
    private String proposalType;
    private PaxosProposalFinalStatus paxosProposalFinalStatus;

    public PaxosProposal(int proposalNumber) {
        this.proposalNumber = proposalNumber;
        this.paxosProposalFinalStatus = PaxosProposalFinalStatus.QUORUM_PENDING;
    }

    // You might want to include other methods relevant to Paxos proposals
}