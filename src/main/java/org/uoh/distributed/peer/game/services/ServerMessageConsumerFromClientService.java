package org.uoh.distributed.peer.game.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uoh.distributed.peer.game.Action;
import org.uoh.distributed.peer.game.utils.MulticastHandler;
import org.uoh.distributed.utils.Constants;
import org.uoh.distributed.utils.RequestBuilder;

import java.io.IOException;
import java.util.Arrays;

public class ServerMessageConsumerFromClientService implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger( ServerMessageConsumerFromClientService.class );

    private final ClientToServerSingleton clientToServerService;
    private final MulticastHandler multicastHandler;

    public ServerMessageConsumerFromClientService(ClientToServerSingleton clientToServerService, MulticastHandler multicastHandler) {
        this.clientToServerService = clientToServerService;
        this.multicastHandler = multicastHandler;
    }
    @Override
    public void run() {
        while (true) {
            Action message = clientToServerService.consume();
            if (message != null) {
                logger.info("Server received message: {}" , message);

                try {
                    String msg = String.format( Constants.GAME_PAYLOAD_MSG_FORMAT, Constants.TYPE_PAYLOAD, Arrays.toString(RequestBuilder.compress(RequestBuilder.buildObjectRequest(message).getBytes())));
                    multicastHandler.sendMulticastMessage(msg);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
