package org.uoh.distributed.peer.game.actionmsgs;

import org.uoh.distributed.peer.game.Action;
import org.uoh.distributed.utils.Constants;

public class MoveMsg extends Action {

    // Are we emitting cordinates or + - & direction from current cordinates?

    private String xIndex;
    private String yIndex;

    public MoveMsg(int x, int y){
        this.xIndex = String.valueOf(x);
        this.yIndex = String.valueOf(y);
        this.setType(Constants.MOVE);
    }
    @Override
    public String getType() {
        return super.getType();
    }

    @Override
    public void setType(String t) {
        super.setType(t);
    }

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
