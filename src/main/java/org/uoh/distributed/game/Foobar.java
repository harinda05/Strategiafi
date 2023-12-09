package org.uoh.distributed.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uoh.distributed.peer.game.Coin;
import org.uoh.distributed.peer.game.GlobalView;
import org.uoh.distributed.peer.game.Player;
import org.uoh.distributed.peer.game.actionmsgs.MoveMsg;
import org.uoh.distributed.peer.game.services.ClientToServerSingleton;
import org.uoh.distributed.peer.game.services.ServerMessageConsumerFromClientService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


/**
 * @brief Main class for the game
 */
public class Foobar extends JFrame {

    ClientToServerSingleton clientToServerService = ClientToServerSingleton.getInstance(); // Gets the instance from singleton class

    private static final Logger logger = LoggerFactory.getLogger( Foobar.class );
    private final int gridSize = 10; // size of the grid
    private final int cellSize = 40; // pixel size of each grid cell

    private class Game extends JPanel implements KeyListener, ActionListener {
        private Player localPlayer;
        private GlobalView map;

        /** Implements the refresh rate) */
        private Timer timer;

        public Game(int cellSize, int gridSize) {
            localPlayer = new Player("testing", 0, 0);
            map = new GlobalView(gridSize, gridSize, cellSize);
            map.addObject(localPlayer);

            setFocusable(true);
            requestFocus();
            addKeyListener(this);
            Coin coin = new Coin(5, 5);
            map.addObject(coin);
            timer = new Timer(50, this);
            timer.start();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            map.drawMap(g);
        }

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            localPlayer.move(e.getKeyCode());
            logger.info("current location: {} , {}", localPlayer.getX(), localPlayer.getY());
            clientToServerService.produce(new MoveMsg(localPlayer.getX(), localPlayer.getY()));
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
    }

    public Foobar() {
        setTitle("Grid Game");
        setSize((gridSize + 1) * cellSize, (gridSize + 1) * cellSize+1 + 50);
        setLayout(new BorderLayout());
        add(new Game(cellSize, gridSize));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setResizable(false);
    }
}
