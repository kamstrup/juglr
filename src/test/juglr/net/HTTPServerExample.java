package juglr.net;

import juglr.Actor;
import juglr.Message;
import static juglr.net.HTTP.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 *
 */
public class HTTPServerExample {

    static class ActorFactory implements TCPChannelActorFactory {

        public TCPChannelActor accept(final SocketChannel channel) {
            return new TCPChannelActor() {

                HTTPRequestReader req = new HTTPRequestReader(channel);

                @Override
                public void react(Message msg) {

                }

                @Override
                public void start() {
                    try {
                        byte[] buf = new byte[1024];
                        Method method = req.readMethod();
                        int uriLength = req.readURI(buf);
                        String uri = new String(buf, 0, uriLength);
                        Version ver = req.readVersion();
                        System.out.println("METHOD: " + method);
                        System.out.println("URI: " + uri + " (length " + uriLength +")");
                        System.out.println("Version: " + ver);

                        int headerLength;
                        while ((headerLength = req.readHeaderField(buf)) > 0) {
                            System.out.println(
                                 "Header: " + new String(buf, 0, headerLength));
                        }
                        System.out.println("Last header length: " + headerLength);

                        int bodyLength = req.readBody(buf);
                        System.out.println("BODY (length " + bodyLength + "):");
                        String body = new String(buf, 0, bodyLength);
                        System.out.println(body);
                    } finally {
                        try {
                            channel.close();
                        } catch (IOException e) {
                            e.printStackTrace(); // FIXME
                        }
                    }
                    System.out.println("CONNECTION HANDLED");
                    // FIXME disconnect from bus to clean up address space!
                }
            };
        }
    }

    public static void main (String[] args) throws Exception {
        Actor server = new TCPServerActor(3333, new ActorFactory());
        server.start();

        // Keep alive for 1 minute
        Thread.sleep(1000*600);
        System.out.println("Timed out");
    }

}
