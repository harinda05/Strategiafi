package org.uoh.distributed.peer.game;

import lombok.*;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Player extends GameObject {

    @Getter
    private final String name;

    @Getter @Setter
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
        g.setColor( getColor() );
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

    public void incrementScore( int value )
    {
        score = score + value;
    }

    public int gameObjectHash() {
        int tmp = (y + ((x + 1) / 2));
        return x + (tmp * tmp);
    }

    private Color getColor(){

        // Map the number to a hue value in the range of 0.0 to 1.0
        float hue = 0;
        if( name == null )
        {
            hue = 10;
        }
        else
        {
            hue = Integer.parseInt( name ) / 1000.0f;
        }

        // Create a color with the mapped hue, maximum saturation, and brightness
        Color color = Color.getHSBColor( hue, 1.0f, 1.0f );
        return color;
    }
}
