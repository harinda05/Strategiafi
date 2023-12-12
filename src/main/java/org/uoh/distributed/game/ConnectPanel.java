package org.uoh.distributed.game;

import org.uoh.distributed.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.UUID;

public class ConnectPanel extends JPanel implements KeyListener, ActionListener
{
    protected GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JLabel labelNodeIP = new JLabel();
    private JLabel labelNodeIPValue = new JLabel();
    private JLabel labelNodePort = new JLabel();
    private JLabel labelNodePortValue = new JLabel();
    private JLabel labelNodeID = new JLabel();
    private JLabel labelNodeIDValue = new JLabel();
    private JLabel labelState = new JLabel();
    private JLabel labelStateValue = new JLabel();

    private JLabel labelBoostrapIP = new JLabel();
    private JTextField txtFieldBoostrapIP = new JTextField();
    private JLabel labelBoostrapPort = new JLabel();
    private JTextField txtFieldBoostrapPort = new JTextField();
    private JLabel labelPort = new JLabel();
    private JTextField txtFieldPort = new JTextField();

    private JButton searchButton = new JButton();
    private JButton stopButton = new JButton();
    private JList<String> nodeList;

    public ConnectPanel()
    {
        try
        {
            jbInit();
            setupGUI();
        }
        catch( Exception exception )
        {
            exception.printStackTrace();
        }
    }

    private void jbInit()
    {
        this.setLayout( gridBagLayout1 );
        labelNodeIP.setText( NameConstant.NODE_IP );
        labelNodeIPValue.setPreferredSize( new Dimension( 100, 25 ) );
        try
        {
            if( !Constants.ENABLE_LOCAL_HOST )  // Change this if you want local host
            {
                String systemipaddress = "";
                try
                {
                    URL url_name = new URL( "https://ipv4.icanhazip.com/" );

                    BufferedReader sc = new BufferedReader( new InputStreamReader( url_name.openStream() ) );

                    // reads system IPAddress
                    systemipaddress = sc.readLine().trim();
                }
                catch( Exception e )
                {
                    systemipaddress = "Cannot Execute Properly";
                }
                labelNodeIPValue.setText( systemipaddress );
            }
            else
            {
                labelNodeIPValue.setText( String.valueOf( InetAddress.getLocalHost().getHostAddress() ) );
            }
        }
        catch( UnknownHostException e )
        {
            e.printStackTrace();
        }
        labelNodePort.setText( NameConstant.NODE_PORT );
        labelNodePortValue.setPreferredSize( new Dimension( 60, 25 ) );
        labelNodeID.setText( NameConstant.NODE_ID );
        labelNodeIDValue.setPreferredSize( new Dimension( 60, 25 ) );
        labelState.setText( NameConstant.NODE_STATE );
        labelStateValue.setPreferredSize( new Dimension( 60, 25 ) );


        labelBoostrapIP.setText( NameConstant.BOOTSTRAP_IP );
        txtFieldBoostrapIP.setPreferredSize( new Dimension( 100, 25 ) );
        txtFieldBoostrapIP.setText( Constants.BOOTSTRAP_IP );
        labelBoostrapPort.setText( NameConstant.BOOTSTRAP_PORT );
        txtFieldBoostrapPort.setPreferredSize( new Dimension( 100, 25 ) );
        txtFieldBoostrapPort.setText( String.valueOf( Constants.BOOTSTRAP_PORT ) );
        labelPort.setText( NameConstant.NODE_PORT );
        txtFieldPort.setPreferredSize( new Dimension( 100, 25 ) );
        txtFieldPort.setText( "32050" );

        searchButton.setText( NameConstant.START );
        stopButton.setText( NameConstant.STOP );

        searchButton.addActionListener( new ActionListener()
        {
            @Override public void actionPerformed( ActionEvent e )
            {
                String userName = UUID.randomUUID().toString();
                if( true ) // Based on the requirement
                {
                    Random random = new Random();
                    // Generate a random number between 0 and 1000
                    int randomNumber = random.nextInt( 1001 );

                    userName = String.valueOf( randomNumber );
                }

                GameWindow.getInstance().nodeStart( txtFieldBoostrapIP.getText(), Integer.parseInt( txtFieldBoostrapPort.getText() ), labelNodeIPValue.getText(),
                                                    Integer.parseInt( txtFieldPort.getText() ),
                                                    userName );
            }
        } );

        stopButton.addActionListener( new ActionListener()
        {
            @Override public void actionPerformed( ActionEvent e )
            {
                GameWindow.getInstance().stopNode();
            }
        } );

    }

    private void setupGUI()
    {
        this.add( labelNodeIP, new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 20, 0, 0 ), 0, 0 ) );
        this.add( labelNodeIPValue, new GridBagConstraints( 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 0, 0, 0 ), 0, 0 ) );
        this.add( labelNodePort, new GridBagConstraints( 2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 0, 0, 0 ), 0, 0 ) );
        this.add( labelNodePortValue, new GridBagConstraints( 3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 0, 0, 0 ), 0, 0 ) );
        this.add( labelNodeID, new GridBagConstraints( 4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 0, 0, 0 ), 0, 0 ) );
        this.add( labelNodeIDValue, new GridBagConstraints( 5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 0, 0, 0 ), 0, 0 ) );
        this.add( labelState, new GridBagConstraints( 6, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 0, 0, 0 ), 0, 0 ) );
        this.add( labelStateValue, new GridBagConstraints( 7, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 0, 0, 0 ), 0, 0 ) );

        this.add( labelBoostrapIP, new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 20, 0, 0 ), 0, 0 ) );
        this.add( txtFieldBoostrapIP, new GridBagConstraints( 1, 2, 4, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 5, 0, 0 ), 0, 0 ) );
        this.add( labelBoostrapPort, new GridBagConstraints( 4, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 0, 0, 0 ), 0, 0 ) );
        this.add( txtFieldBoostrapPort, new GridBagConstraints( 5, 2, 4, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 5, 0, 0 ), 0, 0 ) );
        this.add( labelPort, new GridBagConstraints( 0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 20, 0, 0 ), 0, 0 ) );
        this.add( txtFieldPort, new GridBagConstraints( 1, 4, 4, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 5, 5, 0 ), 0, 0 ) );

        this.add( searchButton, new GridBagConstraints( 0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 20, 0, 0 ), 0, 0 ) );
        this.add( stopButton, new GridBagConstraints( 2, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets( 12, 0, 0, 0 ), 0, 0 ) );


    }


    @Override
    public void actionPerformed( ActionEvent e )
    {

    }

    @Override
    public void keyTyped( KeyEvent e )
    {

    }

    @Override
    public void keyPressed( KeyEvent e )
    {

    }

    @Override
    public void keyReleased( KeyEvent e )
    {

    }

    public void updateDetails( String id, int port )
    {
        labelNodeIDValue.setText( id );
        labelNodePortValue.setText( String.valueOf( port ) );
        this.repaint();
    }
}
