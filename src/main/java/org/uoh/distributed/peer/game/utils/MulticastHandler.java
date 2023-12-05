package org.uoh.distributed.peer.game.utils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

public class MulticastHandler {

    private static MulticastHandler instance;

    private final MulticastSocket multicastSocket;
    private final InetAddress multicastGroupAddress;
    private final int multicastGroupPort;

    private MulticastHandler(InetAddress multicastGroupAddress, int multicastGroupPort) throws IOException {
        this.multicastSocket = new MulticastSocket(multicastGroupPort);
        this.multicastGroupAddress = multicastGroupAddress;
        this.multicastGroupPort = multicastGroupPort;

        InetSocketAddress groupAddress = new InetSocketAddress(multicastGroupAddress, multicastSocket.getPort());
        multicastSocket.joinGroup(groupAddress, null);
    }

    public static MulticastHandler getInstance(InetAddress multicastGroupAddress, int multicastGroupPort) throws IOException {
        if (instance == null) {
            instance = new MulticastHandler(multicastGroupAddress, multicastGroupPort);
        }
        return instance;
    }

    public void sendMulticastMessage(String message) throws IOException {
        byte[] messageData = message.getBytes();
        DatagramPacket packet = new DatagramPacket(messageData, messageData.length, multicastGroupAddress, multicastGroupPort);
        multicastSocket.send(packet);
    }

    public void listenMulticastMessage(String message) throws IOException {
        // Implement multicast message listening
    }
}