package juglr.net;

import juglr.Actor;
import juglr.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 *
 */
public class TCPServerExample {

    static class Strategy implements TCPChannelStrategy {

        public TCPChannelActor accept(final SocketChannel channel) {
            return new TCPChannelActor() {

                ByteBuffer in = ByteBuffer.allocate(1024);
                ByteBuffer out = ByteBuffer.wrap("Hello world\n".getBytes());

                @Override
                public void react(Message msg) {

                }

                @Override
                public void start() {
                    try {
                        int numRead = channel.read(in);
                        in.flip();
                        byte[] bytes = new byte[in.remaining()];
                        in.get(bytes);
                        if (numRead >= 0) {
                            System.out.println("Read: " + numRead + " bytes:");
                            System.out.println(new String(bytes));
                            channel.write(out);
                        }
                    } catch (IOException e) {
                        e.printStackTrace(); // FIXME
                    } finally {
                        try {
                            channel.close();
                        } catch (IOException e) {
                            e.printStackTrace(); // FIXME
                        }
                    }

                    // FIXME disconnect from bus to clean up address space!
                }
            };
        }
    }

    public static void main (String[] args) throws Exception {
        Actor server = new TCPServerActor(3333, new Strategy());
        server.start();

        // Keep alive for 1 minute
        Thread.sleep(1000*60);
        System.out.println("Timed out");
    }

}
