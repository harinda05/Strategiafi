package org.uoh.distributed.peer.game.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uoh.distributed.peer.Node;
import org.uoh.distributed.peer.game.Action;
import org.uoh.distributed.peer.game.actionmsgs.ConsumeResourceMsg;
import org.uoh.distributed.peer.game.paxos.Paxos;
import org.uoh.distributed.peer.game.utils.MulticastHandler;
import org.uoh.distributed.utils.Constants;
import org.uoh.distributed.utils.RequestBuilder;

import java.io.IOException;
import java.util.Arrays;

public class ServerMessageConsumerFromClientService implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger( ServerMessageConsumerFromClientService.class );

    private final ClientToServerSingleton clientToServerService;
    private final MulticastHandler multicastHandler;

    private final Node node;

    public ServerMessageConsumerFromClientService(ClientToServerSingleton clientToServerService, MulticastHandler multicastHandler, Node node) {
        this.clientToServerService = clientToServerService;
        this.multicastHandler = multicastHandler;
        this.node = node;
    }
    @Override
    public void run() {
        while (true) {
            Action message = clientToServerService.consume();
            if (message != null) {
                logger.info("Server received message: {}" , message);

                if(message.getType().equals(Constants.CONSUME_RESOURCE)){
                    ConsumeResourceMsg consumeResourceMsg = (ConsumeResourceMsg) message;
                    Paxos.getInstance().initiatePaxosVoteRequest(node.getRoutingTable(),message.getType(),node.getNodeId(), consumeResourceMsg.getResourceId(), node.getIpAddress());
                } else {
                    try {
                        String msg = String.format( Constants.GAME_PAYLOAD_MSG_FORMAT, message.getType(), RequestBuilder.buildObjectRequest(message));
                        multicastHandler.sendMulticastMessage(msg);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
