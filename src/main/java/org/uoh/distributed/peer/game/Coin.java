package org.uoh.distributed.peer.game;

import lombok.Getter;
import lombok.Setter;
import org.uoh.distributed.utils.Constants;

import java.awt.*;
import java.io.Serializable;

public class Coin extends Reward implements Serializable
{

    @Getter
    @Setter
    int value = 1; // set default to 1
    public Coin(int x, int y) {
        super(x, y);
    }

    public Coin( int x, int y, int val )
    {
        super( x, y );
        this.value = val;
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
        g.setColor( getColor() );
        g.fillRect(this.getX() * cellSize, this.getY() * cellSize, cellSize, cellSize);
    }

    private Color getColor()
    {
        String[] valRange = Constants.COIN_VALUE_RANGE.split( "~" );
        int minValue = Integer.parseInt( valRange[0] );
        int maxValue = Integer.parseInt( valRange[1] );
        float hue = ( value - 1 ) * ( 360.0f / ( maxValue - minValue + 1 ) ); // Divide the 360 degrees of the color wheel by 5

        return Color.getHSBColor( hue / 360.0f, 1.0f, 1.0f );
    }

    @Override public String toString()
    {
        return super.toString() + " , Value:" + value;
    }
}
