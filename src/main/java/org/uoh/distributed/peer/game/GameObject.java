package org.uoh.distributed.peer.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public abstract class GameObject implements Serializable {
    protected int x;

    protected int y;

    abstract void interact(GameObject object);
    abstract void paint(Graphics g, int cellSize);
}
