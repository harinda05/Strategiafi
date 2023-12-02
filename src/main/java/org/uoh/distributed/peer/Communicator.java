package org.uoh.distributed.peer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uoh.distributed.utils.Constants;
import org.uoh.distributed.utils.RequestBuilder;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Communicator
{
    private static final Logger logger = LoggerFactory.getLogger( Node.class );
    private final int numOfRetries = Constants.RETRIES_COUNT;

    private boolean started = false;
    private ExecutorService executorService;
    private Node node;


    public void start( Node node )
    {
        this.node = node;
        executorService = Executors.newCachedThreadPool();
        started = true;
        logger.info("Communication between peers started");

    }

    public Set<RoutingTableEntry> connect( InetSocketAddress peer )
    {
        String request = RequestBuilder.buildRequest( Constants.GET_ROUTING_TABLE );
        logger.debug( "Sending request ({}) to get routing table from {}", request, peer );
        String response = retryOrTimeout( request, peer );
        logger.debug( "Received response : {}", response );
        if( response != null )
        {
            Object obj = RequestBuilder.base64StringToObject( response.split( Constants.MSG_SEPARATOR)[3] );
            logger.debug( "Received routing table entries -> {}", obj );
            if( obj != null )
            {
                return (HashSet<RoutingTableEntry>) obj;
            }
        }

        // If failed we return an empty set to not to break operations.
        return new HashSet<>();
    }


    public boolean disconnect( InetSocketAddress peer )
    {
        return false;
    }

    public Object notifyNewNode( InetSocketAddress peer, InetSocketAddress me, int nodeId )
    {
        String msg = String.format( Constants.NEWNODE_MSG_FORMAT, me.getAddress(), me.getPort(), nodeId );
        String request = RequestBuilder.buildRequest( msg );
        logger.debug( "Notifying new node to {} as message: {}", peer, request );
        String response = retryOrTimeout( request, peer );
        logger.debug( "Received response : {}", response );
        if( response != null )
        {
            Object obj = RequestBuilder.base64StringToObject( response.split( Constants.MSG_SEPARATOR )[3] );
            logger.debug( "Received characters to be taken over -> {}", obj );
            if( obj != null )
            {
                return  obj;
            }
        }

        return new HashMap<>();
    }

    public Object notifyNewNode( InetSocketAddress peer, String ip , int port, int nodeId )
    {
        String msg = String.format( Constants.NEWNODE_MSG_FORMAT, ip, port, nodeId );
        String request = RequestBuilder.buildRequest( msg );
        logger.debug( "Notifying new node to {} as message: {}", peer, request );
        String response = retryOrTimeout( request, peer );
        logger.debug( "Received response : {}", response );
        if( response != null )
        {
            Object obj = RequestBuilder.base64StringToObject( response.split( Constants.MSG_SEPARATOR )[3] );
            logger.debug( "Received characters to be taken over -> {}", obj );
            if( obj != null )
            {
                return  obj;
            }
        }

        return new HashMap<>();
    }

    public void stop()
    {
        if( executorService != null )
        {
            executorService.shutdownNow();
            try
            {
                executorService.awaitTermination( Constants.GRACE_PERIOD_MS, TimeUnit.MILLISECONDS );
            }
            catch( InterruptedException e )
            {
                executorService.shutdownNow();
            }

            executorService = null;
        }

        started = false;
    }

    private String retryOrTimeout( String request, InetSocketAddress peer )
    {
        return retryOrTimeout( numOfRetries, request, peer );
    }

    private String retryOrTimeout( int retries, String request, InetSocketAddress peer )
    {
        int retriesLeft = retries;

        while( retriesLeft > 0 && started )
        {
            Future<String> task = executorService.submit( () -> {
                try (DatagramSocket datagramSocket = new DatagramSocket())
                {
                    return RequestBuilder.sendRequest( datagramSocket, request, peer.getAddress(), peer.getPort() );
                }
            } );

            try
            {
                String response = task.get( Constants.RETRY_TIMEOUT_MS, TimeUnit.MILLISECONDS );
                if( !response.contains( Constants.RESPONSE_FAILURE ) )
                {
                    return response;
                }
            }
            catch( Exception e )
            {
                logger.error( "Error occurred when completing request({}) to peer -> {}. Error: {}", request, peer, e );
                task.cancel( true );
                retriesLeft--;
            }
        }

        logger.error( "REQUEST FAILED !!! ({} -> {})", request, peer );
        if( retries == numOfRetries )
        {
            this.node.removeNode( peer );
        }
        return null;
    }


    public Object ping( InetSocketAddress peer, Object toBeHandedOver )
    {
        String base64 = null;
        try
        {
            base64 = RequestBuilder.buildObjectRequest( toBeHandedOver );
        }
        catch( IOException e )
        {
            logger.error( "Error occurred when encoding entries to be handed over to -> {}", peer, e );
            throw new IllegalArgumentException( "Unable to encode entries", e );
        }

        String msg = String.format( Constants.PING_MSG_FORMAT, this.node.getNodeId(), base64 );
        String request = RequestBuilder.buildRequest( msg );
        logger.debug( "Pinging -> {}", peer );
        String response = retryOrTimeout( 1, request, peer );
        logger.debug( "Received response : {}", response );
        if( response != null )
        {
            Object obj = RequestBuilder.base64StringToObject( response );
            logger.debug( "Received entry table of ({}) -> {}", peer, obj );
            if( obj != null )
            {
                return (Object) obj;
            }
        }
        return null;
    }

}
