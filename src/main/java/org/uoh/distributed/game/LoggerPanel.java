package org.uoh.distributed.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class LoggerPanel extends JPanel implements KeyListener, ActionListener
{
    JScrollPane scrollPane = new JScrollPane();
    protected GridBagLayout gridBagLayout1 = new GridBagLayout();

    public LoggerPanel()
    {
        this.setBorder( javax.swing.BorderFactory.createTitledBorder( "Logger" ) );
        this.setLayout( gridBagLayout1 );
        this.add( scrollPane, new GridBagConstraints( 0, 0, 200, 50, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 12, 10, 0, 10 ), 0, 0 ) );

    }

    @Override public void actionPerformed( ActionEvent e )
    {

    }

    @Override public void keyTyped( KeyEvent e )
    {

    }

    @Override public void keyPressed( KeyEvent e )
    {

    }

    @Override public void keyReleased( KeyEvent e )
    {

    }
}
