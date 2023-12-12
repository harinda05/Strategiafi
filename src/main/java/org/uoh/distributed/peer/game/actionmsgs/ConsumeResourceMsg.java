package org.uoh.distributed.peer.game.actionmsgs;

import lombok.Getter;
import org.uoh.distributed.peer.game.Action;
import org.uoh.distributed.utils.Constants;

public class ConsumeResourceMsg extends Action {

    @Getter
    private String resourceId;

    public ConsumeResourceMsg(String resourceId){
        this.resourceId = resourceId;
        this.setType(Constants.CONSUME_RESOURCE);
    }

}
