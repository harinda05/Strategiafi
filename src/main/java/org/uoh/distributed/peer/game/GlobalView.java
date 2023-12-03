package org.uoh.distributed.peer.game;


import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.sql.Timestamp;

@XmlRootElement
public class GlobalView implements Serializable
{
    private final int width;
    private final int height;
    private final Cell[][] grid; // Grid of cells representing the map
    private Timestamp lastRefreshTime;
    private long logicClock;

    public GlobalView( int width, int height )
    {
        this.width = width;
        this.height = height;
        this.grid = new Cell[width][height];
        initializeMap();
    }

    private void initializeMap()
    {
        // Initialize each cell in the grid ? Its good to initialize cells if there is any rewards or player
        for( int i = 0; i < width; i++ )
        {
            for( int j = 0; j < height; j++ )
            {
                grid[i][j] = new Cell();
            }
        }
        // add rewards at specific coordinates ; just for testing
        addRewardAt( 2, 3, new Reward( true, 10, "c1", Reward.RewardType.COINS ) ); // Example reward at position (2, 3)
    }

    public void addRewardAt( int x, int y, Reward reward )
    {
        grid[x][y].addReward( reward );
    }

    public Cell getCellAt( int x, int y )
    {
        return grid[x][y];
    }

    /**
     * Generate rewards on the map based on the existing rewards
     */
    public void reGenerateRewards()
    {
        // TODO : Add rewards generation algorithm here
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public Cell[][] getGrid()
    {
        return grid;
    }

    public Timestamp getLastRefreshTime()
    {
        return lastRefreshTime;
    }

    public void setLastRefreshTime( Timestamp lastRefreshTime )
    {
        this.lastRefreshTime = lastRefreshTime;
    }

    public long getLogicClock()
    {
        return logicClock;
    }

    public void setLogicClock( long logicClock )
    {
        this.logicClock = logicClock;
    }
}
