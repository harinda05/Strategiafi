package org.uoh.distributed.peer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


public class RoutingTable
{
    private static final Logger logger = LoggerFactory.getLogger( RoutingTable.class );
    private final Set<RoutingTableEntry> entries = new HashSet<>();

    public Set<RoutingTableEntry> getEntries()
    {
        return new HashSet<>( entries );
    }

    public synchronized void addEntry( RoutingTableEntry entry )
    {
        List<RoutingTableEntry> duplicates = this.entries.stream()
                                                         .filter( e -> e.getAddress().equals( entry.getAddress() ) )
                                                         .collect( Collectors.toList() );

        if( duplicates.size() == 0 )
        {
            logger.debug( "Adding entry: {} to the routing table", entry );
            this.entries.add( entry );
        }
        else if( duplicates.stream().filter( e -> e.getNodeId() == entry.getNodeId() ).count() == 1 )
        {
            logger.warn( "Entry : {} already exists", entry );
        }
        else
        {
            // Found an erroneous entry. Correct it.
            RoutingTableEntry e = duplicates.get( 0 );
            e.setNodeId( entry.getNodeId() );
            logger.warn( "Correcting entry {} to {}", e, entry );
        }
    }

    public synchronized boolean removeEntry( RoutingTableEntry e )
    {
        if( this.entries.remove( e ) )
        {
            logger.info("Removed entry -> {}", e);
            return true;
        }
        return false;
    }

    public synchronized boolean removeEntry( InetSocketAddress node )
    {
        Optional<RoutingTableEntry> entry = this.entries.stream()
                                                        .filter( e -> e.getAddress().getAddress().equals( node.getAddress() ) &&
                                                                e.getAddress().getPort() == node.getPort() )
                                                        .findFirst();

        if( entry.isPresent() )
        {
            this.entries.remove( entry.get() );
            logger.info("Removed entry -> {}", entry);
            return true;
        }
        return false;
    }

    /**
     * Removes all the entries in the routing table and clears it.
     */
    public synchronized void clear()
    {
        this.entries.clear();
    }

    /**
     * Finds the {@link InetSocketAddress} of a given node. Searched by the
     *
     * @param nodeId ID of the node of which IP-port info is required to be found
     * @return Optional of {@link InetSocketAddress}
     */
    public Optional<RoutingTableEntry> findByNodeId( int nodeId )
    {
        return this.entries.stream()
                           .filter( e -> e.getNodeId() == nodeId )
                           .findFirst();
    }

}
