package org.uoh.distributed.peer.game.paxos;

import lombok.Getter;
import lombok.Setter;
import org.uoh.distributed.utils.Constants;

@Getter
@Setter
public class ResourceProposalPaxosObject extends PaxosProposal{
    private final int nodeId;
    private final String resourceId;
    public ResourceProposalPaxosObject( int nodeId, String resourceId, int proposalNumber) {
        super(proposalNumber);
        super.setProposalType(Constants.CONSUME_RESOURCE_PROPOSAL);
        this.nodeId = nodeId;
        this.resourceId = resourceId;
    }
}
