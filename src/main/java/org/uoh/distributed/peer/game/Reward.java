package org.uoh.distributed.peer.game;

import java.io.Serializable;

public class Reward implements Serializable
{
    // Enum representing different types of rewards
    public enum RewardType
    {
        COINS,
        GEMS,
        POWER_UP,
        BOMBS
        // Add more reward types as needed
    }

    private boolean positive = true; // Positive and negative rewards ; negative once like bombs
    private int value;
    private String name;
    private RewardType type;

    public Reward( boolean positive, int value, String name, RewardType type )
    {
        this.positive = positive;
        this.value = value;
        this.name = name;
        this.type = type;
    }

    public boolean isPositive()
    {
        return positive;
    }

    public void setPositive( boolean positive )
    {
        this.positive = positive;
    }

    public int getValue()
    {
        return value;
    }

    public void setValue( int value )
    {
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public RewardType getType()
    {
        return type;
    }

    public void setType( RewardType type )
    {
        this.type = type;
    }
}
