package org.uoh.distributed.peer.game;

import lombok.*;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Player extends GameObject {

    @Getter
    private final String name;

    @Getter
    private int score;

    public Player(String name, int x, int y) {
        super(x, y);
        this.name = name;
        score = 0;
    }

    @Override
    void interact(GameObject object) {

    }

    @Override
    void paint(Graphics g, int cellSize) {
        g.fillOval(this.getX() * cellSize, this.getY() * cellSize, cellSize, cellSize);
    }

    public void move(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:
                this.setY(this.getY() - 1);
                break;

            case KeyEvent.VK_DOWN:
                this.setY(this.getY() + 1);
                break;

            case KeyEvent.VK_LEFT:
                this.setX(this.getX() - 1);
                break;

            case KeyEvent.VK_RIGHT:
                this.setX(this.getX() + 1);
                break;
        }
    }

    public void incrementScore() {
        score++;
    }

    public int gameObjectHash() {
        int tmp = (y + ((x + 1) / 2));
        return x + (tmp * tmp);
    }
}
