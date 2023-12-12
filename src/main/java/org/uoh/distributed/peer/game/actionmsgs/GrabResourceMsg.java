package org.uoh.distributed.peer.game.actionmsgs;

import lombok.Getter;
import org.uoh.distributed.peer.game.Action;
import org.uoh.distributed.utils.Constants;

public class GrabResourceMsg extends Action
{

    @Getter private String xIndex;
    @Getter private String yIndex;

    public GrabResourceMsg( int x, int y )
    {
        this.xIndex = String.valueOf( x );
        this.yIndex = String.valueOf( y );
        this.setType( Constants.GRAB );
    }
}
