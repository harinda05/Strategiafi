package org.uoh.distributed.peer.game.utils;
import org.uoh.distributed.utils.RequestBuilder;

import java.io.IOException;
import java.net.*;

public class MulticastHandler {

    private static MulticastHandler instance;

    private final MulticastSocket multicastSocket;
    private final InetAddress multicastGroupAddress;
    private final int multicastGroupPort;

    public MulticastSocket getMulticastSocket() {
        return multicastSocket;
    }
    private MulticastHandler(InetAddress multicastGroupAddress, int multicastGroupPort) throws IOException {
        this.multicastSocket = new MulticastSocket(multicastGroupPort);
        this.multicastGroupAddress = multicastGroupAddress;
        this.multicastGroupPort = multicastGroupPort;
        InetAddress group = InetAddress.getByName(multicastGroupAddress.getHostAddress());
        multicastSocket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, false);
        multicastSocket.joinGroup(group);
    }

    public static MulticastHandler getInstance(InetAddress multicastGroupAddress, int multicastGroupPort) throws IOException {
        if (instance == null) {
            instance = new MulticastHandler(multicastGroupAddress, multicastGroupPort);
        }
        return instance;
    }

    public void sendMulticastMessage(String message) throws IOException {
        byte[] messageData = RequestBuilder.compress( message.getBytes() );
        DatagramPacket packet = new DatagramPacket(messageData, messageData.length, multicastGroupAddress, multicastGroupPort);
        multicastSocket.send(packet);
    }

}