package org.uoh.distributed.game;

import lombok.Getter;
import lombok.Setter;
import org.uoh.distributed.peer.RoutingTable;
import org.uoh.distributed.peer.RoutingTableEntry;
import org.uoh.distributed.peer.game.Player;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
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
        table.getColumnModel().getColumn(0).setCellRenderer(new ColumnColorRenderer("Red", Color.YELLOW));

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

    // Custom cell renderer to set cell color based on column value
    class ColumnColorRenderer extends DefaultTableCellRenderer
    {
        private String targetValue;
        private Color backgroundColor;

        public ColumnColorRenderer(String targetValue, Color backgroundColor) {
            this.targetValue = targetValue;
            this.backgroundColor = backgroundColor;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Get the value of the cell in the 'Color' column
            Object cellValue = table.getValueAt(row, 0); // 'Color' column index

            // Determine the color based on the cell value
            if( cellValue != null )
            {
                cellComponent.setBackground( getColor(  cellValue.toString() ) );
            }
            else
            {
                cellComponent.setBackground( table.getBackground() );
            }

            return cellComponent;
        }
        private Color getColor( String name){

            // Map the number to a hue value in the range of 0.0 to 1.0
            float hue = 0;
            if( name == null )
            {
                hue = 10;
            }
            else
            {
                hue = Integer.parseInt( name ) / 1000.0f;
            }

            // Create a color with the mapped hue, maximum saturation, and brightness
            Color color = Color.getHSBColor( hue, 1.0f, 1.0f );
            return color;
        }
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
