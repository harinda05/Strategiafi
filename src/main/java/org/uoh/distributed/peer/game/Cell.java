package org.uoh.distributed.peer.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Cell implements Serializable
{
    private List<Reward> rewards;
    private Set<String> players;

    public void init()
    {
        rewards = new ArrayList<>();
        players = new HashSet();
    }

    public boolean addReward( Reward reward )
    {
        if( rewards == null )
        {
            rewards = new ArrayList<>();
        }
        return this.rewards.add( reward );
    }

    public boolean removeReward( Reward reward )
    {
        return this.rewards.remove( reward );
    }

    public boolean addPlayer( String playerRef )
    {
        return this.players.add( playerRef );
    }

    public boolean removePlayer( String playerRef )
    {
        return this.players.remove( playerRef );
    }

    public List<Reward> getRewards()
    {
        return rewards;
    }

    public void setRewards( List<Reward> rewards )
    {
        this.rewards = rewards;
    }

    public Set<String> getPlayers()
    {
        return players;
    }

    public void setPlayers( HashSet<String> players )
    {
        this.players = players;
    }
}
