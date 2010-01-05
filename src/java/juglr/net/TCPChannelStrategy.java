package juglr.net;

import java.nio.channels.SocketChannel;

/**
 *
 */
public interface TCPChannelStrategy {

    public TCPChannelActor accept (SocketChannel channel);

}
