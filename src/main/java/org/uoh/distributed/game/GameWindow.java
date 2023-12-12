package org.uoh.distributed.game;

import lombok.Getter;
import lombok.Setter;
import org.uoh.distributed.peer.Communicator;
import org.uoh.distributed.peer.Node;
import org.uoh.distributed.peer.NodeServer;
import org.uoh.distributed.peer.RoutingTableEntry;
import org.uoh.distributed.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameWindow extends JFrame
{
    private final int gridSize = Constants.MAP_HEIGHT; // size of the grid
    private final int cellSize = Constants.MAP_CELL_PIXEL; // pixel size of each grid cell

    private static GameWindow thisInstance;

    @Setter @Getter private Node node;
    Thread listUpdateThread;
    private JList<String> nodeList;

    private ConnectPanel connectPanel = new ConnectPanel();
    private GamePanel gamePanel;
    private LoggerPanel loggerPanel;

    JButton btnPlay = new JButton( "Play" ); // for get focus
    JButton btnGrab = new JButton( "Collect" ); // for grab recourse

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
        gamePanel = new GamePanel( cellSize, gridSize );
        loggerPanel = new LoggerPanel();
        nodeList = new javax.swing.JList<>();
        btnGrab.setToolTipText( "Click to collect/grab coins" );

        setTitle( NameConstant.GAME_NAME );
        setSize( 850, 800 );

        this.add( connectPanel, new GridBagConstraints( 0, 0, 4, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 0, 0, 0 ), 0, 0 ) );
        this.add( btnPlay, new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 20, 0, 0 ), 0, 0 ) );
        this.add( btnGrab, new GridBagConstraints( 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 20, 0, 0 ), 0, 0 ) );
        this.add( gamePanel, new GridBagConstraints( 0, 2, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 12, 20, 0, 0 ), 0, 0 ) );
        //        this.add( loggerPanel, new GridBagConstraints( 0, 3, 1, 1, 0.5, 0.2, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 12, 20, 0, 0 ), 0, 0 )  );


        btnPlay.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                gamePanel.requestFocusInWindow(); // Shift focus to game panel
            }
        } );

        btnGrab.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                gamePanel.grabResource();
                gamePanel.requestFocusInWindow();
            }
        } );

        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        this.addWindowListener( new WindowAdapter()
        {
            @Override public void windowClosing( WindowEvent e )
            {
                try
                {
                    stopNode();
                } catch( Exception ex ){
                    ex.printStackTrace();
                }
                super.windowClosing( e );
            }
        } );
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
            node.setGameMap( gamePanel.getMap() );  // Both node and game panel share same object; Otherwise need to implement another mechanism to sync
            node.start();
            gamePanel.setPlayerName( userName );
            gamePanel.addLocalPlayer( userName, 0, 0 );

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

    public void stopNode(){
        this.node.stop();
    }

    public void garbResource( int resourceHash )
    {
        node.grabResource( resourceHash );
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
