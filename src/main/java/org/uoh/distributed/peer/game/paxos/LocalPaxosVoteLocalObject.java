package org.uoh.distributed.peer.game.paxos;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class LocalPaxosVoteLocalObject implements Serializable {
    private final PaxosProposal paxosProposal;
    private PaxosVoteStatus status;

    LocalPaxosVoteLocalObject(PaxosProposal paxosProposal, PaxosVoteStatus status){
        this.paxosProposal = paxosProposal;
        this.status = status;
    }
}
