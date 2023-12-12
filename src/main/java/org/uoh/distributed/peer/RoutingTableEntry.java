package org.uoh.distributed.peer;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an entry in the routing table. Consists of IP, port and Node name.
 */
@XmlRootElement
public class RoutingTableEntry implements Serializable
{

    @Setter @Getter private InetSocketAddress address;
    @Getter @Setter private int nodeId;
    @Getter @Setter private LocalDateTime lastUpdated;
    private boolean isLeader;

    public RoutingTableEntry() { }

    public RoutingTableEntry( InetSocketAddress address, int nodeId) {
        if (address == null || nodeId <= 0) {
            throw new IllegalArgumentException( "Address and Node name should not be null");
        }

        this.address = address;
        this.nodeId = nodeId;
        this.lastUpdated = LocalDateTime.now();
    }

    @Override public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        RoutingTableEntry that = (RoutingTableEntry) o;
        return nodeId == that.nodeId &&
                Objects.equals( address, that.address );
    }

    @Override public int hashCode()
    {
        return Objects.hash( address, nodeId );
    }

    @Override
    public String toString() {
        return String.format( "[%d -> %s]", nodeId, address);
    }

    public boolean isLeader() {
        return isLeader;
    }

    public void setLeader(boolean leader) {
        isLeader = leader;
    }
}
