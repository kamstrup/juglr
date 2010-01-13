package juglr.net;

import juglr.Actor;

import java.nio.channels.SocketChannel;

/**
 * An {@link Actor} handling a TCP connection via a {@link SocketChannel}.
 * Normally the life cycle of {@code TCPChannelActor}s are controlled by a
 * {@link TCPChannelStrategy}.
 *
 * @see TCPServerActor
 * @see TCPChannelStrategy
 */
public abstract class TCPChannelActor extends Actor {

    /**
     * The channel to handle
     */
    protected SocketChannel channel;

    // This is an abstract class in order to make it possible
    // to keep private state around if we need to do so in the future
    public TCPChannelActor(SocketChannel channel) {
        this.channel = channel;
    }
}
