package org.uoh.distributed.game;

import org.uoh.distributed.peer.Communicator;
import org.uoh.distributed.peer.Node;
import org.uoh.distributed.peer.NodeServer;
import org.uoh.distributed.peer.RoutingTableEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameWindow extends JFrame
{
    private final int gridSize = 10; // size of the grid
    private final int cellSize = 40; // pixel size of each grid cell

    private static GameWindow thisInstance;

    private Node node;
    Thread listUpdateThread;
    private JList<String> nodeList;

    private ConnectPanel connectPanel = new ConnectPanel();
    private GamePanel gamePanel;
    private LoggerPanel loggerPanel;

    JButton btnPlay = new JButton( "Play" ); // for get focus

    public GameWindow()
    {
        initGUI();
    }

    public static GameWindow getInstance()
    {
        if( thisInstance == null )
        {
            thisInstance = new GameWindow();
        }

        return thisInstance;
    }

    private void initGUI()
    {
        this.setLayout( new GridBagLayout() );
        gamePanel = new GamePanel( cellSize, gridSize, node );
        loggerPanel = new LoggerPanel();
        nodeList = new javax.swing.JList<>();

        setTitle( NameConstant.GAME_NAME );
        setSize( 800, 800 );

        this.add( connectPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 0, 0, 0 ), 0, 0 ) );
        this.add( btnPlay, new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 20, 0, 0 ), 0, 0 ) );
        this.add( gamePanel, new GridBagConstraints( 0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 12, 20, 0, 0 ), 0, 0 ) );
        //        this.add( loggerPanel, new GridBagConstraints( 0, 3, 1, 1, 0.5, 0.2, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 12, 20, 0, 0 ), 0, 0 )  );


        btnPlay.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                gamePanel.requestFocusInWindow(); // Shift focus to game panel
            }
        } );

        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        setVisible( true );
        setResizable( true );
    }


    public void nodeStart( String ipBootstrap, int portBootstrap, String nodeIp, int nodePort, String userName )
    {
        try
        {
            Communicator cp = new Communicator();
            NodeServer ns = new NodeServer( nodePort );

            node = new Node( nodePort, nodeIp, userName, cp, ns, ipBootstrap, portBootstrap );
            node.start();
            gamePanel.setPlayerName( userName );
            gamePanel.addLocalPlayer( userName, 0, 0 );
            if( node.getGameMap()!=null )
            {
                gamePanel.setMap(node.getGameMap());
            }
            else{
                node.setGameMap( gamePanel.getMap() );
            }
            connectPanel.updateDetails( userName, nodePort );
            System.out.println( "Node started ..." );
            Runtime.getRuntime().addShutdownHook( new Thread( node::stop ) );
        }
        catch( Exception e )
        {
            System.err.println( "Error occurred: " + e );
            return;
        }

        listUpdateThread = new Thread()
        {
            public void run()
            {
                while( true )
                {
                    String[] nodeArray = new String[50];
                    int i = 0;

                    for( RoutingTableEntry entry : node.getRoutingTable().getEntries() )
                    {
                        nodeArray[i] = ( entry.getAddress().toString() );
                        i++;
                    }

                    nodeList.setListData( nodeArray );
                    try
                    {
                        Thread.sleep( 2000 );
                    }
                    catch( InterruptedException e )
                    {
                        e.printStackTrace();
                    }

                }
            }
        };

        listUpdateThread.setDaemon( true );
        listUpdateThread.start();
    }

    public Node getNode()
    {
        return node;
    }

    public void setNode( Node node )
    {
        this.node = node;
    }

    /**
     * GUI main
     *
     * @param args
     */
    public static void main( String args[] )
    {

        /* Create and display the game window */
        java.awt.EventQueue.invokeLater( new Runnable()
        {
            public void run()
            {
                GameWindow.getInstance().setVisible( true );
            }
        } );
    }

}
