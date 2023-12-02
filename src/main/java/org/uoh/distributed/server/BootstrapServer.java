package org.uoh.distributed.server;

import org.uoh.distributed.utils.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BootstrapServer
{

    private int port;
    private boolean started = false;
    private ExecutorService executorService;

    public BootstrapServer( int port )
    {
        this.port = port;
    }

    public void start()
    {
        if( started )
        {
            throw new IllegalStateException( "Server already running" );
        }

        started = true;
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit( () -> {
            try
            {
                doProcessing();
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        } );

        Runtime.getRuntime().addShutdownHook( new Thread( this::stop ) );  // Server shutdown when there is any exit or user interrupt
    }

    /**
     * Server starting listing to join other Nodes
     */
    private void doProcessing()
    {
        DatagramSocket sock = null;
        String s;
        List<Neighbour> nodes = new ArrayList<>();  // List of Joined nodes

        try
        {
            sock = new DatagramSocket( port );

            echo( "Bootstrap Server created at 55555. Waiting for incoming data..." );

            while( started )
            {
                byte[] buffer = new byte[65536];
                DatagramPacket incoming = new DatagramPacket( buffer, buffer.length );
                sock.receive( incoming );

                byte[] data = incoming.getData();
                s = new String( data, 0, incoming.getLength() );

                //echo the details of incoming data - client ip : client port - client message
                echo( incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s );

                StringTokenizer st = new StringTokenizer( s, Constants.MSG_SEPARATOR );

                String length = st.nextToken();
                String command = st.nextToken();

                if( Constants.REG.equals( command ) )
                {
                    StringJoiner replyJoiner = new StringJoiner( Constants.MSG_SEPARATOR);
                    replyJoiner.add( Constants.REGOK );

                    String ip = st.nextToken();
                    int port = Integer.parseInt( st.nextToken() );
                    String username = st.nextToken();
                    if( nodes.size() == 0 )  // If there is only one node
                    {
                        replyJoiner.add( String.valueOf( Constants.E0000 ) );
                        nodes.add( new Neighbour( ip, port, username ) );
                    }
                    else // If there are many nodes
                    {
                        boolean valid = true;
                        // Validate port and Node username uniqueness
                        for( Neighbour node : nodes )
                        {
                            if( node.getPort() == port )
                            {
                                if( node.getUsername().equals( username ) )
                                {
                                    replyJoiner.add( String.valueOf( Constants.E9998 ) );  // Same username with same port
                                }
                                else
                                {
                                    replyJoiner.add( ( String.valueOf( Constants.E9997 ) ) );  // Same port
                                }
                                valid = false;
                            }
                        }
                        if( valid )
                        {
                            if( nodes.size() == 1 )
                            {
                                replyJoiner.add( String.valueOf( Constants.E0001 ) ).add( nodes.get( 0 ).getIp() ).add( String.valueOf( ( nodes.get( 0 ).getPort() ) ) );
                            }
                            else if( nodes.size() == 2 )
                            {
                                replyJoiner.add( String.valueOf( Constants.E0002 ) ).add( nodes.get( 0 ).getIp() ).add( String.valueOf( nodes.get( 0 ).getPort() ) ).add( nodes.get( 1 ).getIp() ).add(
                                        String.valueOf( nodes.get( 1 ).getPort() ) );
                            }
                            else
                            {
                                // TODO need to update
                                Random r = new Random();
                                int Low = 0;
                                int High = nodes.size();
                                int random_1 = r.nextInt( High - Low ) + Low;
                                int random_2 = r.nextInt( High - Low ) + Low;
                                while( random_1 == random_2 )
                                {
                                    random_2 = r.nextInt( High - Low ) + Low;
                                }
                                echo( random_1 + " " + random_2 );
                                replyJoiner.add( String.valueOf( Constants.E0002 ) ).add( nodes.get( random_1 ).getIp() ).add( String.valueOf( nodes.get( random_1 ).getPort() ) ).add(
                                        nodes.get( random_2 ).getIp() )
                                           .add( String.valueOf( nodes.get( random_2 ).getPort() ) );
                            }
                            nodes.add( new Neighbour( ip, port, username ) );
                        }
                    }

                    String reply = replyJoiner.toString();
                    reply = String.format( "%04d", reply.length() + 5 ) + Constants.MSG_SEPARATOR + reply;

                    DatagramPacket dpReply = new DatagramPacket( reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort() );
                    sock.send( dpReply );
                }
                else if( Constants.UNREG.equals( command ) )
                {
                    String ip = st.nextToken();
                    int port = Integer.parseInt( st.nextToken() );
                    String username = st.nextToken();
                    for( int i = 0; i < nodes.size(); i++ )
                    {
                        if( nodes.get( i ).getPort() == port )
                        {
                            nodes.remove( i );

                            String reply = Constants.UNREGOK;
                            reply = String.format( "%04d", reply.length() + 5 ) + Constants.MSG_SEPARATOR + reply;

                            DatagramPacket dpReply = new DatagramPacket( reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort() );
                            sock.send( dpReply );
                        }
                    }
                }
                else if( Constants.ECHO.equals( command ) )
                {
                    for( int i = 0; i < nodes.size(); i++ )
                    {
                        echo( nodes.get( i ).getIp() + Constants.MSG_SEPARATOR+ nodes.get( i ).getPort() + Constants.MSG_SEPARATOR+ nodes.get( i ).getUsername() );
                    }

                    String reply = Constants.ECHOOK;
                    reply = String.format( "%04d", reply.length() + 5 ) + Constants.MSG_SEPARATOR + reply;

                    DatagramPacket dpReply = new DatagramPacket( reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort() );
                    sock.send( dpReply );
                }
            }
        }
        catch( IOException e )
        {
            System.err.println( "IOException " + e );
        }
        finally
        {
            if( sock != null )
            {
                sock.close();
            }
        }
    }

    public void stop()
    {
        if( started )
        {
            started = false;
            executorService.shutdownNow();
        }
    }

    /**
     * simple function to echo data to terminal Otherwise add to a logger
     */
    private void echo( String msg )
    {
        System.out.println( msg );
    }

    public int getPort()
    {
        return port;
    }

    public void setPort( int port )
    {
        this.port = port;
    }

    public boolean isStarted()
    {
        return started;
    }

    public static void main( String[] args )
    {
        BootstrapServer server = new BootstrapServer( Constants.BOOTSTRAP_PORT );
        server.start();

        while( server.isStarted() )  // Check server up every 2 secs
        {
            try
            {
                Thread.sleep( 2000 );
            }
            catch( InterruptedException ignored )
            {
            }
        }

        server.stop();
    }
}


