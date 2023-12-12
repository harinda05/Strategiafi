package org.uoh.distributed.peer.game.actionmsgs;

import lombok.Getter;
import org.uoh.distributed.peer.game.Action;
import org.uoh.distributed.utils.Constants;

public class PingMsg extends Action
{
    @Getter private String actor;

    public PingMsg( String user )
    {
        this.setType( Constants.PING );
        this.actor = user;
    }

}
