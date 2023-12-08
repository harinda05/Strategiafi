package org.uoh.distributed.peer.game;

import java.awt.*;
import java.io.Serializable;

public class Coin extends Reward implements Serializable
{
    public Coin(int x, int y) {
        super(x, y);
    }

    @Override
    void interact(GameObject object) {
        if (object instanceof Player) {
            Player p = (Player) object;
            p.incrementScore();
        }
    }

    @Override
    void paint(Graphics g, int cellSize) {
        g.fillRect(this.getX() * cellSize, this.getY() * cellSize, cellSize, cellSize);
    }
}
