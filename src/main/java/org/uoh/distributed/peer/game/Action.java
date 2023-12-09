package org.uoh.distributed.peer.game;

import java.io.Serializable;

public class Action implements Serializable {
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
