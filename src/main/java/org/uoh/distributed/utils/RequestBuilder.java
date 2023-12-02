package org.uoh.distributed.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.StringTokenizer;

public class RequestBuilder
{
    private static final Logger logger = LoggerFactory.getLogger( RequestBuilder.class );

    private RequestBuilder()
    {
    }

    public static String buildRequest( String request )
    {
        return String.format( Constants.MSG_FORMAT, request.length() + 5, request );
    }

    public static String sendRequest( DatagramSocket datagramSocket, String request,
                                      InetAddress address, int port ) throws IOException
    {

        // Create a datagram packet to send to the Boostrap server
        DatagramPacket datagramPacket = new DatagramPacket( request.getBytes(), request.length(), address, port );
        // Send to bootstrap server
        datagramSocket.send( datagramPacket );
        // Start listening to Bootstrap Server Response. First 4 bytes are read first to identify length of message.
        byte[] buffer = new byte[65536];
        DatagramPacket incoming = new DatagramPacket( buffer, buffer.length );
        datagramSocket.receive( incoming );

        return new String( incoming.getData(), 0, incoming.getLength() );
    }

    public static List<InetSocketAddress> processRegisterResponse( String response )
    {
        System.out.println( "Processing response : " + response );

        StringTokenizer st = new StringTokenizer( response, Constants.MSG_SEPARATOR);
        System.out.println( "Response length: " + st.nextToken() );
        String status = st.nextToken();

        if( !Constants.REGOK.equals( status ) )
        {
            throw new IllegalStateException( Constants.REGOK + " not received" );
        }

        int code = Integer.parseInt( st.nextToken() );

        List<InetSocketAddress> peers = null;

        switch( code )
        {
            case Constants.E0000:
                System.out.println( "Successful - No nodes in the network yet" );
                peers = new ArrayList<>();
                break;
            case Constants.E0001:
            case Constants.E0002:
                System.out.println( "Successful - Found 1/2 other nodes in the network" );
                peers = new ArrayList<>();
                while( st.hasMoreTokens() )
                {
                    peers.add( new InetSocketAddress( st.nextToken(), Integer.parseInt( st.nextToken() ) ) );
                }
                break;
            case Constants.E9999:
                System.out.println( "Failed. There are errors in your command" );
                break;
            case Constants.E9998:
                System.out.println( "Failed, already registered to you, unregister first" );
                break;
            case Constants.E9997:
                System.out.println( "Failed, registered to another user, try a different IP and port" );
                break;
            case Constants.E9996:
                System.out.println( "Failed, can’t register. BS full." );
                break;
            default:
                throw new IllegalStateException( "No proper status code returned" );
        }
        return peers;
    }

    /**
     * Processes the unregister request's response coming from the server
     *
     * @param response response received
     * @return true if successful
     */
    public static boolean processUnregisterResponse( String response )
    {
        System.out.println( "Processing unregister response :" + response );

        StringTokenizer st = new StringTokenizer( response, Constants.MSG_SEPARATOR );
        System.out.println( "Response length: " + st.nextToken() );
        String status = st.nextToken();

        if( !Constants.UNREGOK.equals( status ) )
        {
            throw new IllegalStateException( Constants.UNREGOK + " not received" );
        }

        int code = Integer.parseInt( st.nextToken() );

        List<InetSocketAddress> peers = new ArrayList<>();

        switch( code )
        {
            case Constants.E0000:
                System.out.println( "Successful" );
                return true;
            case Constants.E9999:
                System.out.println( "Error while un-registering. IP and port may not be in the registry or command is incorrect" );
            default:
                return false;
        }
    }

    public static Object base64StringToObject( String base64 )
    {
        if( base64.equals( "" ) )
        {
            return new Object();
        }
        byte[] received = Base64.getDecoder().decode( base64 );
        ByteArrayInputStream bais = new ByteArrayInputStream( received );
        try (ObjectInputStream in = new ObjectInputStream( bais ))
        {
            return in.readObject();
        }
        catch( Exception e )
        {
            logger.error( "Error occurred when obtaining routing table", e );
            return null;
        }
    }

    public static String buildObjectRequest(Object requestObject) throws IOException {
        //create a Byte Stream out of the object
        ByteArrayOutputStream baos = new ByteArrayOutputStream( 6400);
        ObjectOutputStream oos = new ObjectOutputStream( baos);
        oos.writeObject(requestObject);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static void sendResponse(DatagramSocket datagramSocket, String response,
                                    InetAddress address, int port) throws IOException {
        logger.debug("Sending response to recipient {}:{}", address, port);

        // Create a datagram packet to send to the recipient
        DatagramPacket datagramPacket = new DatagramPacket(response.getBytes(), response.length(), address, port);
        // Send to recipient
        datagramSocket.send(datagramPacket);
        logger.debug("Datagram packet sent to {}:{}", address, port);
    }


}
