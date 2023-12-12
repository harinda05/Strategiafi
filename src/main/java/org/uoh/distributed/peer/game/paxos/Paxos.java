package org.uoh.distributed.peer.game.paxos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uoh.distributed.peer.RoutingTable;
import org.uoh.distributed.peer.RoutingTableEntry;
import org.uoh.distributed.peer.game.GameObject;
import org.uoh.distributed.peer.game.Player;
import org.uoh.distributed.peer.game.services.ServerMessageConsumerFromClientService;
import org.uoh.distributed.peer.game.utils.MulticastHandler;
import org.uoh.distributed.utils.Constants;
import org.uoh.distributed.utils.RequestBuilder;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.uoh.distributed.utils.Constants.CONSUME_RESOURCE_PROPOSAL;
import static org.uoh.distributed.utils.Constants.VOTE_RESPONSE;

public class Paxos {
    private static final Logger logger = LoggerFactory.getLogger( Paxos.class );

    private int proposalNumberOut = 0;

    // The Paxos instance needs to know about all nodes in the distributed system.
    private Map<Integer, Integer> proposalRequiredQuorumMaintainer = new ConcurrentHashMap<>();
    private Map<Integer, Integer> actualQuorumMaintainer = new ConcurrentHashMap<>();

    private Map<String, LocalPaxosVoteLocalObject> voteRequestsReceived = new ConcurrentHashMap<>();
    private static Paxos instance;

    public static Paxos getInstance(){
        if (instance == null) {
            instance = new Paxos();
        }
        return instance;
    }

    // Method to initiate Paxos voting
    public void initiatePaxosVoteRequest(RoutingTable routingTable, String msgType, int nodeId, String resourceId, String myAddress) {
        // Logic to initiate Paxos voting with other nodes
        proposalNumberOut++;

        proposalRequiredQuorumMaintainer.put(proposalNumberOut, routingTable.getEntries().size()/2 + 1);
        actualQuorumMaintainer.put(proposalNumberOut, 1);

        routingTable.getEntries().parallelStream().forEach(routingTableEntry -> {
            if(routingTableEntry.getNodeId()!= nodeId){

                PaxosProposal paxosProposal = null;
                try(DatagramSocket datagramSocket = new DatagramSocket()){
                    switch(msgType){
                        case Constants.CONSUME_RESOURCE:
                            paxosProposal = new ResourceProposalPaxosObject(nodeId, resourceId, proposalNumberOut);
                    }

                    if(paxosProposal == null){
                        throw new Exception(new RuntimeException("Unsupported Paxos Vote Type"));
                    }

                    String msg = String.format(Constants.PAXOS_VOTE_REQUEST_MSG_FORMAT, Constants.VOTE_REQUEST, RequestBuilder.buildObjectRequest(paxosProposal));
                    RequestBuilder.sendRequestAsync(datagramSocket, msg, routingTableEntry.getAddress().getAddress(), routingTableEntry.getAddress().getPort());

                } catch (Exception e){
                    logger.error("Error occurred when sending voting request -> {}", e.getMessage());
                }
            }
        });
    }

    // Method to handle receiving Paxos vote request
    public void handleIncomingPaxosVoteRequest(PaxosProposal paxosProposal, InetSocketAddress recipient, RoutingTable routingTable, int nodeId) {
        // Logic to handle receiving Paxos vote request
        Optional<RoutingTableEntry> recipientRoutingTableEntry;

        logger.info("Vote request received");
        switch (paxosProposal.getProposalType()){
            case CONSUME_RESOURCE_PROPOSAL:
                ResourceProposalPaxosObject resourceProposalPaxosObject = (ResourceProposalPaxosObject) paxosProposal;

                recipientRoutingTableEntry = routingTable.findByNodeId(resourceProposalPaxosObject.getNodeId());

                if(recipientRoutingTableEntry.isEmpty()){
                    logger.error("Routing table entry not found");
                    return;
                }

                if(voteRequestsReceived.get(resourceProposalPaxosObject.getResourceId()) == null){
                    voteRequestsReceived.put(resourceProposalPaxosObject.getResourceId(), new LocalPaxosVoteLocalObject(resourceProposalPaxosObject, PaxosVoteStatus.PENDING));

                    logger.info("Votinggggggggggggggggggggggg");
                    voteRequestsReceived.get(resourceProposalPaxosObject.getResourceId()).setStatus(PaxosVoteStatus.ACCEPTED);
                    try(DatagramSocket datagramSocket = new DatagramSocket()){
                        String request = String.format(Constants.PAXOS_VOTE_RESPONSE_MSG_FORMAT, VOTE_RESPONSE, RequestBuilder.buildObjectRequest(voteRequestsReceived.get(resourceProposalPaxosObject.getResourceId())));
                        RequestBuilder.sendRequest(datagramSocket, request, recipientRoutingTableEntry.get().getAddress().getAddress(), recipientRoutingTableEntry.get().getAddress().getPort());
                    } catch (Exception e){
                        logger.error("Exception occurred when sending vote to Paxos Initializer -> {}", e.getMessage());
                    }
                    System.out.println(voteRequestsReceived.get(resourceProposalPaxosObject.getResourceId()).getStatus());
                } else {
                    //Todo: implement
                }
        }
    }

    // Method to handle receiving Paxos vote
    public void receivePaxosVote(LocalPaxosVoteLocalObject localPaxosVoteLocalObject) {
        logger.info("Received paxos vote: proposal {}", localPaxosVoteLocalObject.getPaxosProposal().getProposalNumber());
        // Logic to handle receiving Paxos vote
        int minimumVoteCount = proposalRequiredQuorumMaintainer.get(localPaxosVoteLocalObject.getPaxosProposal().getProposalNumber());
        if (localPaxosVoteLocalObject.getStatus() == PaxosVoteStatus.ACCEPTED){
            Integer currentVoteCount = actualQuorumMaintainer.get(localPaxosVoteLocalObject.getPaxosProposal().getProposalNumber());
            if (currentVoteCount + 1 >= minimumVoteCount){

                logger.info("Quorum Received");

                //ToDo: Vote Succeeded ----> Do the task for UI
                //ToDo: Commit the message to teh network. // can multicast here
            } else {
                logger.info("Incrementing actualQuoromMaintainer");
                actualQuorumMaintainer.put(localPaxosVoteLocalObject.getPaxosProposal().getProposalNumber(), currentVoteCount + 1);
            }
        }
    }
}

