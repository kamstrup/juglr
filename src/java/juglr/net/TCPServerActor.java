package juglr.net;

import juglr.Actor;
import juglr.Message;
import juglr.MessageBus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * A TCP server that accepts connections in a designated thread and dispatches
 * control to a {@link TCPChannelActor} obtained from a
 * {@link TCPChannelStrategy}. The {@code accept}-loop is very tight and is able
 * to handle a massive number of concurrent connections (mainly limited by the
 * multiplexing capabilities of Java's {@link ServerSocketChannel}).
 * <p/>
 * To stop the server send it the {@link #SHUTDOWN} message.
 *
 * @seealso {@link HTTPMessageBus}
 */
public class TCPServerActor extends Actor {

    private static class ConnectionListener implements Runnable {

        private ServerSocketChannel server;
        private TCPChannelStrategy strategy;
        private MessageBus bus;

        public ConnectionListener (SocketAddress socketAddress,
                                   TCPChannelStrategy strategy,
                                   MessageBus bus)
                                                            throws IOException {
            server = ServerSocketChannel.open();
            server.socket().bind(socketAddress);
            this.strategy = strategy;
            this.bus = bus;
        }

        public void run() {
            while (true) {
                try {
                    SocketChannel channel = server.accept();
                    TCPChannelActor actor = strategy.accept(channel);
                    bus.start(actor.getAddress());
                } catch (ClosedChannelException e) {
                    // Handles interrupts etc.
                    System.out.println("Shutdown"); // FIXME
                    break;
                } catch (IOException e) {
                    e.printStackTrace();  //FIXME!!!
                    break;
                }
            }
        }
    }

    public static final Message SHUTDOWN = new Message();

    private ServerSocketChannel server;
    private Thread acceptThread;

    public TCPServerActor(SocketAddress socketAddress,
                          TCPChannelStrategy strategy,
                          MessageBus bus)
                                                            throws IOException {
        super(bus);
        acceptThread = new Thread(
                new ConnectionListener(socketAddress, strategy, bus),
                "ConnectionLister[" + socketAddress +"]"
        );
        acceptThread.setDaemon(true); // Allow JVM to exit
    }

    /**
     * Create a server listening on {@code port} handling connection with
     * actor obtained from {@code strategy}
     * @param port
     * @param strategy
     * @throws IOException
     */
    public TCPServerActor(int port, TCPChannelStrategy strategy)
                                                            throws IOException {
        this(new InetSocketAddress(port), strategy, MessageBus.getDefault());
    }

    public TCPServerActor(
            String hostname, int port, TCPChannelStrategy strategy)
                                                            throws IOException {
        this(new InetSocketAddress(hostname, port),
             strategy, MessageBus.getDefault());
    }

    public TCPServerActor(
            int port, TCPChannelStrategy strategy, MessageBus bus)
                                                            throws IOException {
        this(new InetSocketAddress(port), strategy, bus);
    }

    public TCPServerActor(String hostname, int port,
                          TCPChannelStrategy strategy, MessageBus bus)
                                                            throws IOException {
        this(new InetSocketAddress(hostname, port), strategy, bus);
    }

    @Override
    public void react(Message msg) {
        if (msg == SHUTDOWN) {
            acceptThread.interrupt();
        }
    }

    @Override
    public void start() {
        acceptThread.start();
    }

}
