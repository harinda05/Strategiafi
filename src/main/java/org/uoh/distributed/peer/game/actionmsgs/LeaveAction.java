package org.uoh.distributed.peer.game.actionmsgs;

import lombok.Getter;
import org.uoh.distributed.peer.game.Action;
import org.uoh.distributed.utils.Constants;

public class LeaveAction extends Action
{
    @Getter private String actor;

    public LeaveAction( String user )
    {
        this.setType( Constants.UNREG );
        this.actor = user;
    }
}
