package org.uoh.distributed.peer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uoh.distributed.peer.game.Coin;
import org.uoh.distributed.peer.game.GameObject;
import org.uoh.distributed.peer.game.GlobalView;
import org.uoh.distributed.peer.game.Player;
import org.uoh.distributed.peer.game.actionmsgs.GrabResourceMsg;
import org.uoh.distributed.peer.game.actionmsgs.LeaveAction;
import org.uoh.distributed.peer.game.actionmsgs.MoveMsg;
import org.uoh.distributed.peer.game.actionmsgs.PingMsg;
import org.uoh.distributed.peer.game.paxos.*;
import org.uoh.distributed.peer.game.services.ClientToServerSingleton;
import org.uoh.distributed.peer.game.services.ServerMessageConsumerFromClientService;
import org.uoh.distributed.peer.game.utils.MulticastHandler;
import org.uoh.distributed.utils.Constants;
import org.uoh.distributed.utils.RequestBuilder;

import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.uoh.distributed.utils.Constants.CONSUME_RESOURCE_PROPOSAL;

public class NodeServer
{

    private static final Logger logger = LoggerFactory.getLogger( NodeServer.class );
    private final int numOfRetries = Constants.RETRIES_COUNT;

    private boolean started = false;
    private ExecutorService executorService;
    private Node node;
    private final int port;

    private final String multicastAddress = "230.0.0.0"; // ToDo: get from props
    private final int multicastPort = 5383; // ToDo: get from props

    private final Paxos paxos = Paxos.getInstance();

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

        started = true;

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

        // This would actively consume the msgs from the client UI @Victor, in Client create an opposite class to ServerMessageConsumerFromClient and add the action messages to that queue
        ClientToServerSingleton clientToServerService = ClientToServerSingleton.getInstance(); // Gets the instance from singleton class
        MulticastHandler multicastHandler;
        try {
            multicastHandler = MulticastHandler.getInstance(InetAddress.getByName(multicastAddress), multicastPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        executorService.submit( () -> {
            try
            {
                multicastListen(multicastHandler.getMulticastSocket());
            }
            catch( Exception e )
            {
                logger.error( "Error occurred when multicast listening", e );
            }
        } );

        ServerMessageConsumerFromClientService clientToServerServiceThread = new ServerMessageConsumerFromClientService(clientToServerService, multicastHandler, node); // Create a ClientToServerServiceThread instance

        executorService.submit( () -> {
            try
            {
                clientToServerServiceThread.run();
            }
            catch( Exception e )
            {
                logger.error( "Error occurred when multicast sending", e );
            }
        } );


        //ToDo: Start ServerToClientService

        logger.info( "Server started" );
        Runtime.getRuntime().addShutdownHook( new Thread( this::stop ) );
    }

    public void listen()
    {
        try (DatagramSocket datagramSocket = new DatagramSocket( port ))
        {
            handleListen(datagramSocket, "unicast");
        }
        catch( IOException e )
        {
            logger.error( "Error occurred when listening on port {}", port, e );
            throw new IllegalStateException( "Error occurred when listening", e );
        }
    }

    public void multicastListen(MulticastSocket multicastSocket) {
        try{
            handleListen(multicastSocket, "multicast");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleListen(DatagramSocket datagramSocket, String type) throws IOException {
        while( started )
        {
            logger.info( "Node is Listening to incoming requests - {}",  type );
            byte[] buffer = new byte[65536];
            DatagramPacket incoming = new DatagramPacket( buffer, buffer.length );
            datagramSocket.receive( incoming );

            byte[] rawData = incoming.getData();

            byte[] data = RequestBuilder.decompress( rawData );
            String request = new String( data, 0, data.length );
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
            case Constants.UNREG:
                respondToUnreg( incomingResult[2], recipient );
                break;
            case Constants.SYNC:
                handleSyncRequest( incomingResult[2], recipient );
                break;
            case Constants.MOVE:
                handleMoveRequest( incomingResult[2], recipient );
                break;
            case Constants.GRAB:
                handleResourceGrab( incomingResult[2], recipient );
                break;
            case Constants.TYPE_PAYLOAD:
                handlePayload(incomingResult[2], recipient);
                break;
            case Constants.VOTE_REQUEST:
                handlePaxosVoteRequest(incomingResult[2], recipient);

            case Constants.VOTE_RESPONSE:
                handlePaxosVoteResponse(incomingResult[2], recipient);

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
        // Share the Global map with new Node if the received node is Leader
        if( node.isLeader() )
        {
            Optional<RoutingTableEntry> entry = node.getRoutingTable().getEntries().stream().filter( e -> e.getNodeId() == newNodeId ).findFirst();
            System.out.println( entry + " - Received :" + recipient );
            if( entry.isPresent() )
            {
                shareGlobalMap( entry.get().getAddress() );
            }
        }
    }

    private void shareGlobalMap( InetSocketAddress recipient ) throws IOException
    {
        try
        {
            logger.debug( "Returning game Map to -> {}", recipient );
            String mapMsg = String.format( Constants.SYNC_MSG_FORMAT, Constants.TYPE_MAP, RequestBuilder.buildObjectRequest( node.getGameMap() ) );
            String mapResponse = RequestBuilder.buildRequest( mapMsg );
            retryOrTimeout( mapResponse, recipient );
        }
        catch( IOException e )
        {
            logger.error( "Error occurred when building object request: {}", e );
            throw e;
        }

    }

    private void handleSyncRequest( String request, InetSocketAddress recipient )
    {
        logger.debug( "Received sync request -> {}", request );
        String[] parts = request.split( Constants.MSG_SEPARATOR );
        Object obj = null;
        boolean sendData = true;  // need to send data back
        if(parts.length>1)
        {
            sendData = false;
            obj = RequestBuilder.base64StringToObject( parts[1] );
        }
        switch( parts[0] )
        {
            case Constants.TYPE_MAP:
                logger.debug( "Received map to be taken over -> {}", obj );
                try
                {
                    if( sendData )
                    {
                        shareGlobalMap( recipient );
                    }
                    else
                    {
                        syncMap( (GlobalView) obj );
                    }
                }
                catch( Exception e )
                {
                    logger.debug( "Error in converting msg to map -> {}", obj );
                }
                break;
            case Constants.TYPE_ROUTING:
                logger.debug( "Received routing table -> {}", obj );
                break;
            default:
                break;

        }

        retryOrTimeout( Constants.RESPONSE_OK, recipient );
    }

    private void syncMap( GlobalView map )
    {
        logger.debug( "Received map -> {}", map );
        for( GameObject c : map.getGameObjects().values() )
        {
            if( node.getGameMap().getGameObjects().get( c.hashCode() ) == null ) // if the reward not there then add it Otherwise remove it from node map
            {
                node.getGameMap().addObject( c );
            }
            else
            {
                node.getGameMap().getGameObjects().remove( c.hashCode() );
            }
        }
        for( Player c : map.getPlayers() )
        {
            Optional<Player> p = node.getGameMap().getPlayers().stream().filter( a -> a.getName().equals( c.getName() ) ).findFirst();
            if( !p.isPresent() ) // if the reward not there then add it Otherwise remove it from node map
            {
                node.getGameMap().addObject( c );
            }
        }


    }

    private void respondToPing( String request, InetSocketAddress recipient ) throws IOException
    {
        logger.debug( "Received ping -> {}", request );
        String[] parts = request.split( Constants.MSG_SEPARATOR );

        if( parts.length == 1 )
        {
            Object obj = RequestBuilder.base64StringToObject( parts[0] );
            if( obj instanceof PingMsg )
            {
                PingMsg pingMsg = (PingMsg) obj;
                Optional<RoutingTableEntry> recipientPeer = node.getRoutingTable().findByNodeId( Integer.parseInt( pingMsg.getActor() ) );
                if( recipientPeer.isPresent() )
                {
                    recipientPeer.ifPresent( routingTableEntry -> routingTableEntry.setLastUpdated( LocalDateTime.now() ) );
                    logger.debug( " Received ping from -> {}", recipientPeer.get().getNodeId() );
                }
                else
                {
                    logger.debug( "Requester not found -> {}", pingMsg.getActor() );
                }
            }
        }

    }

    private void removeUser( String user )
    {
        Optional<RoutingTableEntry> recipientPeer = node.getRoutingTable().findByNodeId( Integer.parseInt( user ) );
        if( recipientPeer.isPresent() )
        {
            Iterator<RoutingTableEntry> iterator = node.getRoutingTable().getEntries().iterator();

            // Iterate over the set and remove entries that meet certain conditions
            while( iterator.hasNext() )
            {
                RoutingTableEntry element = iterator.next();
                if( element.getNodeId() == recipientPeer.get().getNodeId() )
                {
                    logger.info( "Node ({}:{}) Removed ", element.getAddress().getAddress(), element.getAddress().getPort() );
                    Iterator<Player> playerIterator = node.getGameMap().getPlayers().iterator();
                    while( playerIterator.hasNext() )
                    {
                        Player player = playerIterator.next();
                        if( player.getName().equals( String.valueOf( element.getNodeId() ) ) )
                        {
                            playerIterator.remove(); // Remove the player from the list
                            break;
                        }
                    }
                    iterator.remove();
                    break;
                }
            }

        }
        else
        {
            logger.debug( "Requester not found -> {}", user );
        }
    }


    private void respondToUnreg( String request, InetSocketAddress recipient ) throws IOException
    {
        logger.debug( "Received Leave msg -> {}", request );
        String[] parts = request.split( Constants.MSG_SEPARATOR );

        if( parts.length == 1 )
        {
            Object obj = RequestBuilder.base64StringToObject( parts[0] );
            if( obj instanceof LeaveAction )
            {
                LeaveAction pingMsg = (LeaveAction) obj;
                Optional<RoutingTableEntry> recipientPeer = node.getRoutingTable().findByNodeId( Integer.parseInt( pingMsg.getActor() ) );
                if( recipientPeer.isPresent() )
                {
                    removeUser( pingMsg.getActor() );
                }
                else
                {
                    logger.debug( "Requester not found -> {}", pingMsg.getActor() );
                }
            }
        }

    }

    private void handlePayload(String request, InetSocketAddress recipient ){
        logger.debug( "Received game payload -> {}", request );
        String[] parts = request.split( Constants.MSG_SEPARATOR );

        Object obj = RequestBuilder.base64StringToObject( parts[1] );
        logger.debug( "Received characters to be taken over -> {}", obj );

    }

    private void handleMoveRequest( String request, InetSocketAddress recipient )
    {
        String[] parts = request.split( Constants.MSG_SEPARATOR );
        String ipAddress = recipient.getAddress().getHostAddress();

        MoveMsg moveMsg = (MoveMsg) RequestBuilder.base64StringToObject( parts[0] );

        node.getGameMap().reflectAction( moveMsg );
    }

    private void handleResourceGrab( String request, InetSocketAddress recipient )
    {
        String[] parts = request.split( Constants.MSG_SEPARATOR );
        String ipAddress = recipient.getAddress().getHostAddress();

        GrabResourceMsg resourceMsg = (GrabResourceMsg) RequestBuilder.base64StringToObject( parts[0] );

        node.getGameMap().reflectAction( resourceMsg );
    }

    private void handlePaxosVoteRequest(String request, InetSocketAddress recipient){
        logger.debug( "Received vote request -> {}", request );
        String[] parts = request.split( Constants.MSG_SEPARATOR );

        Object obj = RequestBuilder.base64StringToObject( parts[0]);
        logger.debug( "Received characters to be taken over -> {}", obj );

        if(obj instanceof PaxosProposal){
            paxos.handleIncomingPaxosVoteRequest((PaxosProposal) obj, recipient, node.getRoutingTable(), node.getNodeId());
        } else {
            logger.error("Object is not type of PaxosProposal");
        }
    }

    private void handlePaxosVoteResponse(String request, InetSocketAddress recipient){
        String[] parts = request.split( Constants.MSG_SEPARATOR );

        Object obj = RequestBuilder.base64StringToObject( parts[0] );
        logger.debug( "Received characters to be taken over -> {}", obj );

        if(obj instanceof LocalPaxosVoteLocalObject){
            logger.info( "Received vote response -> propNumber - {}, status - {}", ((LocalPaxosVoteLocalObject) obj).getPaxosProposal().getProposalNumber(), ((LocalPaxosVoteLocalObject) obj).getStatus() );
            PaxosProposal paxosProposal = paxos.receivePaxosVote((LocalPaxosVoteLocalObject) obj);


            if(paxosProposal.getPaxosProposalFinalStatus() == PaxosProposalFinalStatus.QUORUM_RECEIVED){

                switch (paxosProposal.getProposalType()){
                    case CONSUME_RESOURCE_PROPOSAL:
                        ResourceProposalPaxosObject resourceProposalPaxosObject = (ResourceProposalPaxosObject) paxosProposal;

                        Optional<Player> player = node.getGameMap().getPlayers().stream().filter( p -> p.getName().equals(node.getUsername())).findFirst();

                        int x = node.getGameMap().getGameObjects().get(Integer.valueOf(resourceProposalPaxosObject.getResourceId())).getX();
                        int y = node.getGameMap().getGameObjects().get(Integer.valueOf(resourceProposalPaxosObject.getResourceId())).getY();

                        Coin temp = new Coin( x, y );
                        GameObject gameObject = node.getGameMap().getGameObjects().get( temp.hashCode() );
                        if(player.isPresent()){
                            if(gameObject instanceof Coin){
                                player.get().incrementScore(((Coin) gameObject).getValue());
                            }
                        }

                        node.getGameMap().getGameObjects().remove(Integer.valueOf(resourceProposalPaxosObject.getResourceId()));
                        node.getCommunicationProvider().informResourceGrab(x, y, node.getUsername());
                }
            }
        } else {
            logger.error("Object is not type of PaxosProposal");
        }
    }

}
