package org.uoh.distributed.peer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uoh.distributed.utils.Constants;
import org.uoh.distributed.utils.RequestBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class NodeServer
{

    private static final Logger logger = LoggerFactory.getLogger( Node.class );
    private final int numOfRetries = Constants.RETRIES_COUNT;

    private boolean started = false;
    private ExecutorService executorService;
    private Node node;
    private final int port;


    public NodeServer( int port )
    {
        this.port = port;
    }

    public void start( Node node )
    {
        if( started )
        {
            logger.warn( "Listener already running" );
            return;
        }

        this.node = node;
        executorService = Executors.newCachedThreadPool();
        executorService.submit( () -> {
            try
            {
                listen();
            }
            catch( Exception e )
            {
                logger.error( "Error occurred when listening", e );
            }
        } );

        started = true;
        logger.info( "Server started" );
        Runtime.getRuntime().addShutdownHook( new Thread( this::stop ) );
    }

    public void listen()
    {
        try (DatagramSocket datagramSocket = new DatagramSocket( port ))
        {
            logger.debug( "Node is Listening to incoming requests" );

            while( started )
            {
                byte[] buffer = new byte[65536];
                DatagramPacket incoming = new DatagramPacket( buffer, buffer.length );
                datagramSocket.receive( incoming );

                byte[] data = incoming.getData();
                String request = new String( data, 0, incoming.getLength() );
                logger.debug( "Received from {}:{} -> {}", incoming.getAddress(), incoming.getPort(), request );

                executorService.submit( () -> {
                    try
                    {
                        handleRequest( request, incoming );
                    }
                    catch( Exception e )
                    {
                        logger.error( "Error occurred when handling request ({})", request, e );
                        retryOrTimeout( Constants.RESPONSE_FAILURE, new InetSocketAddress( incoming.getAddress(), incoming.getPort() ) );
                    }
                } );
            }
        }
        catch( IOException e )
        {
            logger.error( "Error occurred when listening on port {}", port, e );
            throw new IllegalStateException( "Error occurred when listening", e );
        }
    }

    /**
     * Handles requests coming to this node.
     *
     * @param request  Request received
     * @param incoming incoming datagram packet
     * @throws IOException
     */
    private void handleRequest( String request, DatagramPacket incoming ) throws IOException
    {
        String[] incomingResult = request.split( Constants.MSG_SEPARATOR, 3 );
        logger.debug( "Request length -> {}", incomingResult[0] );
        String command = incomingResult[1];
        logger.debug( "Command -> {}", command );

        InetSocketAddress recipient = new InetSocketAddress( incoming.getAddress(), incoming.getPort() );
        switch( command )
        {
            case Constants.GET_ROUTING_TABLE:
                // Here, we are purposefully preventing sending a response if I'm not configured yet
                if( node.getState().compareTo( NodeState.CONNECTED ) < 0 )
                {
                    logger.warn( "Not responding to request '{}' because I'm at state -> {}", request, node.getState() );
                    return;
                }
                provideRoutingTable( recipient );
                break;
            case Constants.NEW_NODE:
                handleNewNodeRequest( incomingResult[2], recipient );
                break;

            case Constants.PING:
                respondToPing( incomingResult[2], recipient );
                break;
            case Constants.SYNC:
                handleSyncRequest( incomingResult[2], recipient );
                break;
            default:
                break;
        }
    }

    private void provideRoutingTable( InetSocketAddress recipient ) throws IOException
    {
        logger.debug( "Returning routing table to -> {}", recipient );
        String response;
        try
        {
            String msg = String.format( Constants.SYNC_MSG_FORMAT, Constants.TYPE_ROUTING, RequestBuilder.buildObjectRequest( this.node.getRoutingTable().getEntries() ) );
            response = RequestBuilder.buildRequest( msg );
        }
        catch( IOException e )
        {
            logger.error( "Error occurred when building object request: {}", e );
            throw e;
        }

        retryOrTimeout( response, recipient );
        logger.debug( "Routing table entries provided to the recipient: {}", recipient );
    }

    public void stop()
    {
        if( started )
        {
            started = false;
            executorService.shutdownNow();
            try
            {
                executorService.awaitTermination( Constants.GRACE_PERIOD_MS, TimeUnit.MILLISECONDS );
            }
            catch( InterruptedException e )
            {
                executorService.shutdownNow();
            }
            logger.info( "Server stopped" );
        }
    }

    private boolean retryOrTimeout( String response, InetSocketAddress peer )
    {
        int retriesLeft = numOfRetries;
        while( retriesLeft > 0 && started )
        {
            Future<Void> task = executorService.submit( () -> {
                try (DatagramSocket datagramSocket = new DatagramSocket())
                {
                    RequestBuilder.sendResponse( datagramSocket, response, peer.getAddress(), peer.getPort() );
                    return null;
                }
            } );

            try
            {
                task.get( Constants.RETRY_TIMEOUT_MS, TimeUnit.MILLISECONDS );
                return true;
            }
            catch( Exception e )
            {
                logger.error( "Error occurred when completing response({}) to peer- {}. Error: {}", response, peer, e );
                task.cancel( true );
                retriesLeft--;
            }
        }
        logger.error( "RESPONSE FAILED !!! ({} -> {})", response, peer );
        return false;
    }

    private void handleNewNodeRequest( String request, InetSocketAddress recipient ) throws IOException
    {
        String[] parts = request.split( Constants.MSG_SEPARATOR );
        String ipAddress = parts[0];
        int port = Integer.parseInt( parts[1] );
        int newNodeId = Integer.parseInt( parts[2] );

        this.node.addNewNode( ipAddress, port, newNodeId );

        String msg = String.format( Constants.SYNC_MSG_FORMAT, Constants.TYPE_ENTRIES, RequestBuilder.buildObjectRequest( "OK" ) );
        String response = RequestBuilder.buildRequest( msg );
        retryOrTimeout( response, recipient );
    }

    private void handleSyncRequest( String request, InetSocketAddress recipient )
    {
        logger.debug( "Received sync request -> {}", request );
        String[] parts = request.split( Constants.MSG_SEPARATOR );

        Object obj = RequestBuilder.base64StringToObject( parts[1] );
        switch( parts[0] )
        {
            case Constants.TYPE_ENTRIES:
                logger.debug( "Received characters to be taken over -> {}", obj );
                break;
            case Constants.TYPE_ROUTING:
                logger.debug( "Received routing table -> {}", obj );
                break;
        }

        retryOrTimeout( Constants.RESPONSE_OK, recipient );
    }

    private void respondToPing( String request, InetSocketAddress recipient ) throws IOException
    {
        logger.debug( "Responding to ping with my table entries to -> {}", request );
        /*
              Need to implement what we need to share
         */
    }
}
