package org.uoh.distributed.peer.game.actionmsgs;

import lombok.Getter;
import org.uoh.distributed.peer.game.Action;
import org.uoh.distributed.utils.Constants;

public class GrabResourceMsg extends Action
{

    @Getter private int xIndex;
    @Getter private int yIndex;
    @Getter private String actor;


    public GrabResourceMsg( int x, int y, String user )
    {
        this.xIndex = x;
        this.yIndex = y;
        this.setType( Constants.GRAB );
        this.actor = user;
    }
}
