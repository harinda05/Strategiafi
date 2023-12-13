package org.uoh.distributed.game;

import lombok.Getter;
import lombok.Setter;
import org.uoh.distributed.peer.RoutingTable;
import org.uoh.distributed.peer.RoutingTableEntry;
import org.uoh.distributed.peer.game.Player;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Optional;

public class NodeSummaryPanel extends JPanel implements KeyListener, ActionListener

{

    protected GridBagLayout gridBagLayout1 = new GridBagLayout();
    private Timer timer;
    @Getter @Setter private RoutingTable routingTable;
    private DefaultTableModel model;

    public NodeSummaryPanel()
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
        model = new DefaultTableModel();

        timer = new Timer( 50, this );
        timer.start();
    }

    private void setupGUI()
    {
        // Create a DefaultTableModel
        model.addColumn( "ID" );
        model.addColumn( "IP Address" );
        model.addColumn( "Port" );
        model.addColumn( "Score" );


        if( routingTable != null )  // Populate the table model with data from the list of objects
        {
            for( RoutingTableEntry entry : routingTable.getEntries() )
            {
                int sccore = 0;
                try
                {
                    Optional<Player> first = GameWindow.getInstance().getNode().getGameMap().getPlayers().stream().filter( p -> p.getName().equals( String.valueOf( entry.getNodeId() ) ) ).findFirst();
                    if( first.isPresent() )
                    {
                        sccore = first.get().getScore();
                    }
                }
                catch( Exception ex )
                {
                    ex.printStackTrace();
                }
                model.addRow( new Object[]{entry.getNodeId(), entry.getAddress().getAddress(), entry.getAddress().getPort(), sccore} );
            }
        }

        // Create a JTable with the populated model
        JTable table = new JTable( model );
        table.setAutoCreateColumnsFromModel( false ); // Prevent auto-creation of columns

        // Define column names using TableColumnModel
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn( 0 ).setHeaderValue( "ID" );
        columnModel.getColumn( 1 ).setHeaderValue( "IP Address" );
        columnModel.getColumn( 2 ).setHeaderValue( "Port" );
        columnModel.getColumn( 3 ).setHeaderValue( "Score" );

        columnModel.getColumn( 0 ).setPreferredWidth( 25 );
        columnModel.getColumn( 1 ).setPreferredWidth( 30 );
        columnModel.getColumn( 2 ).setPreferredWidth( 25 );
        columnModel.getColumn( 3 ).setPreferredWidth( 25 );

        // Add the table to a JScrollPane
        JScrollPane scrollPane = new JScrollPane( table );

        // Add the scroll pane to the panel
        this.add( scrollPane, new GridBagConstraints( 0, 0, 110, 200, 0.5, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 12, 10, 0, 10 ), 0, 0 ) );

    }

    @Override public void actionPerformed( ActionEvent actionEvent )
    {
        if( actionEvent.getSource() == timer )
        {
            updateTable();
        }
    }

    private void updateTable()
    {
        while( model.getRowCount() > 0 )
        {
            model.removeRow( 0 );
        }

        // Repopulate the model with new data (for example, list of updated Person objects)
        if( routingTable != null )
        {
            for( RoutingTableEntry entry : routingTable.getEntries() )
            {
                int sccore = 0;
                try
                {
                    Optional<Player> first = GameWindow.getInstance().getNode().getGameMap().getPlayers().stream().filter( p -> p.getName().equals( String.valueOf( entry.getNodeId() ) ) ).findFirst();
                    if( first.isPresent() )
                    {
                        sccore = first.get().getScore();
                    }
                }
                catch( Exception ex )
                {
                    ex.printStackTrace();
                }
                model.addRow( new Object[]{entry.getNodeId(), entry.getAddress().getAddress(), entry.getAddress().getPort(), sccore} );
            }
        }

        // Notify the table that the data has changed
        model.fireTableDataChanged();
    }

    @Override public void keyTyped( KeyEvent keyEvent )
    {

    }

    @Override public void keyPressed( KeyEvent keyEvent )
    {

    }

    @Override public void keyReleased( KeyEvent keyEvent )
    {

    }

}
