package org.uoh.distributed.game;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uoh.distributed.peer.game.Coin;
import org.uoh.distributed.peer.game.GlobalView;
import org.uoh.distributed.peer.game.Player;
import org.uoh.distributed.peer.game.actionmsgs.MoveMsg;
import org.uoh.distributed.peer.game.services.ClientToServerSingleton;
import org.uoh.distributed.peer.game.services.ServerMessageConsumerFromClientService;
import org.uoh.distributed.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


/**
 * @brief Main class for the game
 */
public class GamePanel extends JPanel implements KeyListener, ActionListener {

    ClientToServerSingleton clientToServerService = ClientToServerSingleton.getInstance(); // Gets the instance from singleton class
    private static final Logger logger = LoggerFactory.getLogger( GamePanel.class );

        private Player localPlayer;
        @Getter @Setter private GlobalView map;
        @Setter @Getter private String playerName;

        /** Implements the refresh rate) */
        private Timer timer;

        public GamePanel(int cellSize, int gridSize) {
//            localPlayer = new Player(playerName, 0, 0);
            map = new GlobalView(gridSize, gridSize, cellSize);
//            map.addObject(localPlayer);

            setFocusable(true);
            requestFocus();
            addKeyListener(this);
//            Coin coin = new Coin(5, 5);
//            map.addObject(coin);
            timer = new Timer(50, this);
            timer.start();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            map.drawMap(g);
            if( localPlayer != null )
            {
                g.setColor( Color.BLACK );
                g.drawString( localPlayer.getName() + " Score: " + localPlayer.getScore(), 5, Constants.MAP_CELL_PIXEL * Constants.MAP_HEIGHT + 15 );
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed( KeyEvent e )
        {
            if( e.getKeyChar() == 'w' || e.getKeyChar() == 'W' || e.getKeyCode() == KeyEvent.VK_SPACE )
            {
                grabResource();
            }
            else
            {
                boolean moved = localPlayer.move( e.getKeyCode() );
                if( moved )  // fine move message for only success moves
                {
                    logger.info( "current location: {} , {}", localPlayer.getX(), localPlayer.getY() );
                    clientToServerService.produce( new MoveMsg( localPlayer.getX(), localPlayer.getY(), playerName ) );
                }
            }

        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == timer) {
                repaint();
            }
        }

    public void addLocalPlayer( String playerName, int x, int y){
            localPlayer = new Player( playerName,x,y );
            map.getPlayers().add( localPlayer);
    }

    public boolean grabResource()
    {
        if( map.getGameObjects().get( localPlayer.gameObjectHash() ) != null )
        {
            // both local player and resource in same cell
            GameWindow.getInstance().garbResource( localPlayer.gameObjectHash() );
        }
        return false;
    }
}
