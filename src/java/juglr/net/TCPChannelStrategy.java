package juglr.net;

import java.nio.channels.SocketChannel;

/**
 * Strategy for choosing {@link TCPChannelActor}s from a {@link TCPServerActor}
 * for handling incoming TCP connections. The most common strategies would
 * use a factory- or pool pattern.
 *
 * @see TCPChannelActor
 * @see TCPServerActor
 */
public interface TCPChannelStrategy {

    /**
     * Obtain an actor to handle a TCP connection over a {@link SocketChannel}
     * @param channel the socket channel the actor must handle
     * @return the actor designated to handle {@code channel}
     */
    public TCPChannelActor accept (SocketChannel channel);

}
