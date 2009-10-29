package juglr.net;

import java.nio.channels.SocketChannel;

/**
 *
 */
public interface TCPChannelActorFactory {

    public TCPChannelActor accept (SocketChannel channel);

}
