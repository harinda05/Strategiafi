package org.uoh.distributed.peer.game.actionmsgs;

import lombok.Getter;
import lombok.Setter;
import org.uoh.distributed.peer.game.Action;
import org.uoh.distributed.utils.Constants;

public class ConsumeResourceMsg extends Action {

    @Getter
    private String resourceId;

    @Getter
    private String playerId;

    public ConsumeResourceMsg(String resourceId, String playerId){
        this.playerId = playerId;
        this.resourceId = resourceId;
        this.setType(Constants.CONSUME_RESOURCE);
    }

}
