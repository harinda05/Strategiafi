package org.uoh.distributed.peer.game.paxos;

import lombok.Getter;
import lombok.Setter;


public class LocalPaxosVoteLocalObject {
    private final PaxosProposal paxosProposal;

    @Setter
    @Getter
    private PaxosVoteStatus status;

    LocalPaxosVoteLocalObject(PaxosProposal paxosProposal, PaxosVoteStatus status){
        this.paxosProposal = paxosProposal;
        this.status = status;
    }
}
