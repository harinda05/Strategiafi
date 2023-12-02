package org.uoh.distributed.peer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uoh.distributed.utils.Constants;
import org.uoh.distributed.utils.RequestBuilder;


import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.List;

/**
 * This class responsible for the communication between node and Bootstrap server
 */
public class BootstrapConnector
{
    private static final Logger logger = LoggerFactory.getLogger( BootstrapConnector.class );
    private final int numOfRetries = Constants.BOOTSTRAP_RETRIES_COUNT;

    public BootstrapConnector()
    {
    }

    public List<InetSocketAddress> register( String ipAddress, int port, String username ) throws SocketException
    {
        logger.debug( "Registering node" );

        String msg = String.format( Constants.REG_MSG_FORMAT, ipAddress, port, username );
        String request = RequestBuilder.buildRequest( msg );

        int retriesLeft = numOfRetries;
        while( retriesLeft > 0 )
        {
            try (DatagramSocket datagramSocket = new DatagramSocket())
            {
                String response = RequestBuilder.sendRequest( datagramSocket, request, InetAddress.getByName( Constants.BOOTSTRAP_IP ), Constants.BOOTSTRAP_PORT );
                logger.debug( "Response received : {}", response );
                return RequestBuilder.processRegisterResponse( response );
            }
            catch( IOException e )
            {
                logger.error( "Error occurred when sending the register request", e );
                retriesLeft--;
            }
        }

        return null;
    }

    public boolean unregister( String ipAddress, int port, String username ) throws SocketException
    {
        logger.debug( "Unregistering node" );

        String msg = String.format( Constants.UNREG_MSG_FORMAT, ipAddress, port, username );
        String request = RequestBuilder.buildRequest( msg );

        int retriesLeft = numOfRetries;
        while( retriesLeft > 0 )
        {
            try (DatagramSocket datagramSocket = new DatagramSocket())
            {
                String response = RequestBuilder.sendRequest( datagramSocket, request, InetAddress.getByName( Constants.BOOTSTRAP_IP ), Constants.BOOTSTRAP_PORT );
                logger.debug( "Response received : {}", response );
                if( RequestBuilder.processUnregisterResponse( response ) )
                {
                    logger.info( "Successfully unregistered" );
                    return true;
                }
                else
                {
                    logger.warn( "Unable to unregister" );
                    return false;
                }
            }
            catch( IOException e )
            {
                retriesLeft--;
                logger.error( "Error occurred when sending the unregister request", e );
            }
        }

        return false;
    }

}
