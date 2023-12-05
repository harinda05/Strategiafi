package org.uoh.distributed.peer.game.actionmsgs;

import org.uoh.distributed.peer.game.Action;
import org.uoh.distributed.utils.Constants;

public class MoveMsg extends Action {

    // Are we emitting cordinates or + - & direction from current cordinates?

    @Override
    public String getType() {
        return Constants.MOVE;
    }

    private String xIndex;
    private String yIndex;

    public String getxIndex() {
        return xIndex;
    }

    public void setxIndex(String xIndex) {
        this.xIndex = xIndex;
    }

    public String getyIndex() {
        return yIndex;
    }

    public void setyIndex(String yIndex) {
        this.yIndex = yIndex;
    }
}
