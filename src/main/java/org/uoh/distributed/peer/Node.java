package org.uoh.distributed.peer;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uoh.distributed.peer.game.Coin;
import org.uoh.distributed.peer.game.GameObject;
import org.uoh.distributed.peer.game.GlobalView;
import org.uoh.distributed.peer.game.Player;
import org.uoh.distributed.utils.Constants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Node
{
    private static final Logger logger = LoggerFactory.getLogger( Node.class );

    private final StateManager state = new StateManager( NodeState.IDLE );

    @Getter private final String username;
    @Getter private final String ipAddress;
    @Getter private final int port;
    private final String bootstrapIpAddress;
    private final int bootstrapPort;
    @Setter @Getter private int nodeId;
    @Getter private final RoutingTable routingTable = new RoutingTable();
    private boolean isLeader;
    private String leaderNode;

    private final NodeServer server;
    @Getter private final Communicator communicationProvider;   //  Peer communication provider

    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> periodicTask;

    private BootstrapConnector bootstrapProvider = new BootstrapConnector();

    //----- Game related stuff -----
    @Setter @Getter private GlobalView gameMap;


    public Node( int port )
    {
        this( port, new Communicator(), new NodeServer( port ) );
    }

    public Node( int port, Communicator communicationProvider, NodeServer server )
    {
        this( port, "localhost", communicationProvider, server );
    }

    public Node( int port, String ipAddress, Communicator communicationProvider, NodeServer server )
    {
        this( port, ipAddress, UUID.randomUUID().toString(), communicationProvider, server , null, -1);
    }

    public Node( int port, String ipAddress, String username, Communicator communicationProvider, NodeServer server , String boostrapIp, int bootstrapPort)
    {
        this.port = port;
        this.ipAddress = ipAddress;
        this.username = username;
        this.communicationProvider = communicationProvider;
        this.server = server;
        this.bootstrapPort = bootstrapPort;
        this.bootstrapIpAddress = boostrapIp;
    }

    public void start()
    {
        state.checkState( NodeState.IDLE );
        Runtime.getRuntime().addShutdownHook( new Thread( () -> stop( true ) ) );

        executorService = Executors.newScheduledThreadPool( 3 );
        server.start( this );
        communicationProvider.start( this );


        logger.debug( "Connecting to the distributed network" );
        while( !state.isState( NodeState.CONNECTING ) )
        {
            if( state.isState( NodeState.REGISTERED ) )
            {
                unregister();
            }

            List<InetSocketAddress> peers = register();  // Get 2 peers

            if( state.isState( NodeState.REGISTERED ) )
            {
                Set<RoutingTableEntry> entries = connect( peers );
                // peer size become 0 only when we connected successfully
                if( peers.isEmpty() || !entries.isEmpty() )
                {
                    this.updateRoutingTable( entries );
                    state.setState( NodeState.CONNECTING );
                    logger.info( "Successfully connected to the network and created routing table" );
                }
            }

        }
        // 1. Select a Node Name
//        this.nodeId = selectNodeName();
        this.nodeId = Integer.parseInt( username );
        logger.info( "Selected node ID -> {}", this.nodeId );

        // 2. Add my node to my routing table
        routingTable.addEntry( new RoutingTableEntry( new InetSocketAddress( ipAddress, port ), this.nodeId ) );
        logger.info( "My routing table is -> {}", routingTable.getEntries() );
        state.setState( NodeState.CONNECTED );


        configure();
        state.setState( NodeState.CONFIGURED );

        // TODO: Periodic synchronization
        /*
         * 1. Find 2 predecessors of mine.
         * 2. Then periodically ping them and synchronize with their entry tables.
         */
        periodicTask = executorService.scheduleAtFixedRate( () -> {
            try
            {
                runPeriodically();
            }
            catch( Exception e )
            {
                logger.error( "Error occurred when running periodic check", e );
            }
        }, Constants.HEARTBEAT_INITIAL_DELAY, Constants.HEARTBEAT_FREQUENCY_MS, TimeUnit.MILLISECONDS );
    }

    /**
     * Register and fetch 2 random peers from Bootstrap Server. Also retries until registration becomes successful.
     *
     * @return peers sent from Bootstrap server
     */
    private List<InetSocketAddress> register()
    {
        logger.debug( "Registering node" );
        List<InetSocketAddress> peers = null;
        try
        {
            peers = bootstrapProvider.register( ipAddress, port, username, bootstrapIpAddress, bootstrapPort );
        }
        catch( IOException e )
        {
            logger.error( "Error occurred when registering node", e );
        }

        if( peers == null )
        {
            logger.warn( "Peers are null" );
        }
        else
        {
            if( peers.isEmpty() )
            {
                // Automatically become the leader and initialize the game base map
                setLeader( true );
                initializeGlobalMap();
            }
            state.setState( NodeState.REGISTERED );
            logger.info( "Node ({}:{}) registered successfully. Peers -> {}", ipAddress, port, peers );
        }

        return peers;
    }


    private void configure()
    {
        // Broadcast that I have joined the network to all entries in the routing table
        this.routingTable.getEntries().parallelStream()
                         .filter( entry -> entry.getNodeId() != this.nodeId )
                         .forEach( entry -> {
                             Object toBeUndertaken = communicationProvider.notifyNewNode( entry.getAddress(), ipAddress, port, this.nodeId );
                         } );
        /*
            Do some specific work
            1) Load global map
            2) If need to join to chord then join
         */

        RoutingTableEntry entry = responsiblePeers();
        if( entry != null )
        {
            Object map = communicationProvider.sync( entry.getAddress(), Constants.TYPE_MAP );
            if( map != null )
            {
                gameMap.getGameObjects().putAll( ( (GlobalView) map ).getGameObjects() );
            }
        }

    }

    private void runPeriodically()
    {
        /*
           1) ping to other Nodes
           2) Synchronize Map
         */
        validateRoutingTableEntries();
        communicationProvider.pingMulticast( username );

    }

    /**
     * Validate routing table entries updated and if any entry exceed the maximum time period then remove it
     */
    private void validateRoutingTableEntries()
    {
        Iterator<RoutingTableEntry> iterator = routingTable.getEntries().iterator();

        // Iterate over the set and remove entries that meet certain conditions
        while( iterator.hasNext() )
        {
            RoutingTableEntry element = iterator.next();
            Instant instant = LocalDateTime.now().atZone( java.time.ZoneId.systemDefault() ).toInstant();
            Instant instant2 = element.getLastUpdated().atZone( java.time.ZoneId.systemDefault() ).toInstant();

            if( ( instant.toEpochMilli() - instant2.toEpochMilli() ) > Constants.HEARTBEAT_LIVENESS_LIMIT )
            {
                logger.info( "Node ({}:{}) Removed ", element.getAddress().getAddress(), element.getAddress().getPort() );
                Iterator<Player> playerIterator = getGameMap().getPlayers().iterator();
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
            }
        }
    }


    /**
     * Connect to the peers send by BS and fetch their routing tables. This method will later be reused for
     * synchronization purposes.
     *
     * @param peers peers to be connected
     * @return true if connecting successful and got at least one entry
     */
    private Set<RoutingTableEntry> connect( List<InetSocketAddress> peers )
    {
        logger.debug( "Collecting routing table from peers: {}", peers );
        Set<RoutingTableEntry> entries = new HashSet<>();
        for( InetSocketAddress peer : peers )
        {
            Set<RoutingTableEntry> received = communicationProvider.connect( peer );
            logger.debug( "Received routing table: {} from -> {}", received, peer );
            if( received.size() == 0 )
            {
                logger.error( "Failed to obtain routing table from -> {}", peer );
                entries.clear();
                break;
            }

            entries.addAll( received );
        }
        return entries;
    }

    /**
     * Unregister
     */
    private void unregister()
    {
        try
        {
            bootstrapProvider.unregister( ipAddress, port, username );
            state.setState( NodeState.IDLE );
            logger.debug( "Unregistered from Bootstrap Server" );
        }
        catch( IOException e )
        {
            logger.error( "Error occurred when unregistering", e );
        }
    }


    private int selectNodeName()
    {
        Set<Integer> usedNodes = this.routingTable.getEntries().stream().map( RoutingTableEntry::getNodeId ).collect( Collectors.toSet() );
        Random random = new Random();
        while( true )
        {
            int candidate = 1 + random.nextInt( Constants.ADDRESS_SPACE_SIZE );
            if( !usedNodes.contains( candidate ) )
            {
                return candidate;
            }
        }
    }

    /**
     * Generate initial global map for game
     */
    private void initializeGlobalMap()
    {
        if( this.gameMap == null )
        {
            this.gameMap = new GlobalView( Constants.MAP_WIDTH, Constants.MAP_HEIGHT, Constants.MAP_CELL_PIXEL );
        }

        try
        {
            int rewardsCount = ( Constants.MAP_WIDTH * Constants.MAP_HEIGHT ) * Constants.REWARDS_PERCENTAGE / 100;
            String[] valRange = Constants.COIN_VALUE_RANGE.split( "~" );
            gameMap.getGameObjects().clear();
            int i = 0;
            while( i < rewardsCount )
            {
                Coin c = generateRandomCoin( Constants.MAP_WIDTH, Constants.MAP_HEIGHT, Integer.parseInt( valRange[0] ), Integer.parseInt( valRange[1] ) );
                if( !gameMap.getGameObjects().containsKey( c.hashCode() ) )  // add unique coins
                {
                    gameMap.addObject( c );
                    i++;
                }
            }
            logger.debug( "Initialized the global map -> {}", gameMap.getGameObjects().size() );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Generate random coins based on the grid parameters
     * @param limitX coin x max limit
     * @param limitY coin y max limit
     * @param minVal coin min value
     * @param maxVal coin max value
     * @return
     */
    private Coin generateRandomCoin( int limitX, int limitY, int minVal, int maxVal )
    {
        Random random = new Random();
        int x = random.nextInt( limitX );
        int y = random.nextInt( limitY );
        int val = random.nextInt( maxVal ) + minVal;

        Coin c = new Coin( x, y, val );
        return c;
    }

    /**
     * Updates the {@link #routingTable} with entries coming from another node's routing table
     *
     * @param entries routing table entries received from another node
     */
    public void updateRoutingTable( Set<RoutingTableEntry> entries )
    {
        logger.debug( "Adding routing table entries -> {}", entries );
        // Should we sync? Remove what is not present?
        entries.forEach( routingTable::addEntry );
    }

    public void addNewNode( String ipAddress, int newNodePort, int newNodeId )
    {
        state.checkState( NodeState.CONNECTED, NodeState.CONFIGURED );
        InetSocketAddress inetSocketAddress = new InetSocketAddress( ipAddress, newNodePort );
        RoutingTableEntry routingTableEntry = new RoutingTableEntry( inetSocketAddress, newNodeId );
        routingTable.addEntry( routingTableEntry );
        logger.info( "Added routing table entry -> {} from routing table", inetSocketAddress );

    }


    public void removeNode( InetSocketAddress node )
    {
        logger.warn( "Attempting to remove routing table entry -> {} from routing table", node );
        this.routingTable.removeEntry( node );
    }


    public void stop( boolean withUnregister )
    {
        // TODO: graceful departure
        logger.debug( "Stopping node" );
        if( state.getState().compareTo( NodeState.REGISTERED ) >= 0 )
        {
            if( state.getState().compareTo( NodeState.CONNECTED ) >= 0 )
            {
                this.routingTable.getEntries().parallelStream().forEach( entry -> {
                    if( communicationProvider.disconnect( entry.getAddress() ) )
                    {
                        logger.debug( "Successfully disconnected from {}", entry );
                    }
                    else
                    {
                        logger.warn( "Unable to disconnect from {}", entry );
                    }
                } );

                this.routingTable.clear();
                state.setState( NodeState.REGISTERED );
            }
            if( withUnregister )
            {
                unregister();
            }
        }

        communicationProvider.stop( withUnregister );
        server.stop();

        logger.debug( "Shutting down periodic tasks" );
        executorService.shutdownNow();
        try
        {
            executorService.awaitTermination( Constants.GRACE_PERIOD_MS, TimeUnit.MILLISECONDS );
        }
        catch( InterruptedException e )
        {
            executorService.shutdownNow();
        }

        state.setState( NodeState.IDLE );
        logger.info( "Distributed node stopped" );
    }


    /**
     * Find a Leader or some other peer to sync with the current map
     * @return
     */
    private RoutingTableEntry responsiblePeers(){

        // Assume there are n multiple leaders at once
        Optional<RoutingTableEntry> leader =  routingTable.getEntries().stream().filter(RoutingTableEntry::isLeader).findFirst();

        if(leader.isPresent()){
            return leader.get();
        }else {
            Optional<RoutingTableEntry> peer = routingTable.getEntries().stream().filter(e -> e.getNodeId() != nodeId).findFirst();
            if(peer.isPresent()){
                return peer.get();
            }
        }
        return null;
    }

    public void grabResource( int resourceHash )
    {
        /* This may be a blocking call for the current user
            Can perform a voting and make sure resource is available for acquire
         */
        GameObject obj = gameMap.getGameObjects().get( resourceHash );
        if( obj != null )
        {
            logger.info( "Resource is available {} ", obj.toString() );
            Optional<Player> p = gameMap.getPlayers().stream().filter( player -> player.getName().equals( username ) ).findFirst();
            communicationProvider.informInitialGrab( String.valueOf(resourceHash));


            //            p.ifPresent( player -> player.incrementScore( ( (Coin) obj ).getValue() ) );
//
//            // if success remove the resource from the map and announce it to others
//            gameMap.getGameObjects().remove( resourceHash );
//            communicationProvider.informResourceGrab(obj.getX(), obj.getY());
        }




    }
    public NodeState getState()
    {
        return state.getState();
    }

    public boolean isLeader()
    {
        return isLeader;
    }

    public void setLeader( boolean leader )
    {
        isLeader = leader;
    }

}
