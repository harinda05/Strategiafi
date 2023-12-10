package org.uoh.distributed.peer.game.paxos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uoh.distributed.peer.RoutingTable;
import org.uoh.distributed.peer.game.GameObject;
import org.uoh.distributed.peer.game.Player;
import org.uoh.distributed.peer.game.services.ServerMessageConsumerFromClientService;
import org.uoh.distributed.peer.game.utils.MulticastHandler;
import org.uoh.distributed.utils.Constants;
import org.uoh.distributed.utils.RequestBuilder;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Paxos {
    private static final Logger logger = LoggerFactory.getLogger( Paxos.class );

    private int proposalNumberOut = 0;

    // The Paxos instance needs to know about all nodes in the distributed system.
    private Map<String, ResourceProposalPaxosObject> voteRequestsSent = new ConcurrentHashMap<>();

    private Map<String, LocalPaxosVoteLocalObject> voteRequestsReceived = new ConcurrentHashMap<>();
    private static Paxos instance;

    public static Paxos getInstance(){
        if (instance == null) {
            instance = new Paxos();
        }
        return instance;
    }

    // Method to initiate Paxos voting
    public void initiatePaxosVoteRequest(RoutingTable routingTable, String msgType, int nodeId, String resourceId) {
        // Logic to initiate Paxos voting with other nodes
        proposalNumberOut++;
        routingTable.getEntries().parallelStream().forEach(routingTableEntry -> {
            PaxosProposal paxosProposal = null;
            try(DatagramSocket datagramSocket = new DatagramSocket()){
                switch(msgType){
                    case Constants.CONSUME_RESOURCE:
                        paxosProposal = new ResourceProposalPaxosObject(nodeId, resourceId, proposalNumberOut);
                }

                if(paxosProposal == null){
                    throw new Exception(new RuntimeException("Unsupported Paxos Vote Type"));
                }

                String msg = String.format(Constants.PAXOS_VOTE_REQUEST_MSG_FORMAT, msgType, RequestBuilder.buildObjectRequest(paxosProposal));
                RequestBuilder.sendRequestAsync(datagramSocket, msg, routingTableEntry.getAddress().getAddress(), routingTableEntry.getAddress().getPort());

            } catch (Exception e){
                logger.error("Error occurred when sending voting request -> {}", e.getMessage());
            }
        });
    }

    // Method to handle receiving Paxos vote request
    public void handleIncomingPaxosVoteRequest(PaxosProposal paxosProposal) {
        // Logic to handle receiving Paxos vote request

        switch (paxosProposal.getProposalType()){
            case Constants.CONSUME_RESOURCE_PROPOSAL:
                ResourceProposalPaxosObject resourceProposalPaxosObject = (ResourceProposalPaxosObject) paxosProposal;
                if(voteRequestsReceived.get(resourceProposalPaxosObject.getResourceId()) == null){
                    voteRequestsReceived.put(resourceProposalPaxosObject.getResourceId(), new LocalPaxosVoteLocalObject(resourceProposalPaxosObject, PaxosVoteStatus.PENDING));

                    logger.info("Votinggggggggggggggggggggggg");
                    //Todo: send the vote response here
                    voteRequestsReceived.get(resourceProposalPaxosObject.getResourceId()).setStatus(PaxosVoteStatus.ACCEPTED);
                    System.out.println(voteRequestsReceived.get(resourceProposalPaxosObject.getResourceId()).getStatus());
                } else {
                    //Todo: implement
                }
        }
    }

    // Method to handle receiving Paxos vote
    private void receivePaxosVote(Player proposer, GameObject resource, boolean vote) {
        // Logic to handle receiving Paxos vote
        // You may need to implement the Paxos algorithm here

        // For simplicity, let's assume the proposer collects votes and decides
    }
}

