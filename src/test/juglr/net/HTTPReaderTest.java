package juglr.net;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static org.testng.Assert.fail;

/**
 * FIXME: Missing class docs for juglr.net.HTTPReaderTest
 *
 * @author Mikkel Kamstrup Erlandsen <mailto:mke@statsbiblioteket.dk>
 * @since Feb 19, 2010
 */
public class HTTPReaderTest {

    static int serverCounter = 0;

    // Return a SocketChannel that reads the content of 'msg'
    protected SocketChannel prepareSocket(final ByteBuffer msg)
                                                            throws Exception {
        final ServerSocketChannel serverChannel = ServerSocketChannel.open();
        SocketChannel clientChannel = SocketChannel.open();

        serverChannel.socket().bind(null);
        SocketAddress serverAddr = serverChannel.socket().getLocalSocketAddress();

        Thread server = new Thread(new Runnable() {

            public void run() {
                try {
                    SocketChannel channel = serverChannel.accept();
                    channel.write(msg);
                    channel.close();
                } catch (IOException e) {
                    fail("Failed writing message to socket", e);
                }
            }
        }, "Server"+(++serverCounter));
        server.start();

        clientChannel.connect(serverAddr);
        return clientChannel;
    }

}
