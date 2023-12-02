package org.uoh.distributed.peer.game;


import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlRootElement;
import java.awt.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement
public class GlobalView implements Serializable
{
    private final int width;

    private final int height;

    private final int cellSize;

    @Getter
    private final HashMap<Integer, GameObject> gameObjects;

    @Getter
    private final Set<Player> players;

    @Getter
    @Setter
    private Timestamp lastRefreshTime;

    @Getter
    @Setter
    private long logicClock;

    public GlobalView(int width, int height, int cellSize)
    {
        this.width = width;
        this.height = height;
        this.cellSize = cellSize;
        this.gameObjects = new HashMap<>();
        this.players = new HashSet<>();
        initializeMap();
    }

    private void initializeMap()
    {
    }

    /**
     * Draws the map
     * @param g canvas to draw on
     */
    public void drawMap(Graphics g) {
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                g.drawRect(i * cellSize, j * cellSize, cellSize, cellSize);
            }
        }

        for (Player p : players) {
            p.paint(g, cellSize);
            final int key = p.gameObjectHash();
            if (gameObjects.containsKey(key)) {
                GameObject object = gameObjects.get(key);
                object.interact(p);
                gameObjects.remove(key);
            }

            g.drawString(p.getName() + " Score: " + p.getScore(), 5, cellSize * height + 15);
        }

        for (GameObject o : gameObjects.values()) {
            o.paint(g, cellSize);
        }
    }

    public void addObject(GameObject object) {
        if (object instanceof Player) {
            players.add((Player) object);
        } else {
            gameObjects.put(object.hashCode(), object);
        }
    }
}
