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
 * 
 */
public class TCPServerActor extends Actor {

    private static class ConnectionListener implements Runnable {

        private ServerSocketChannel server;
        private TCPChannelActorFactory actorFactory;
        private MessageBus bus;

        public ConnectionListener (SocketAddress socketAddress,
                                   TCPChannelActorFactory actorFactory,
                                   MessageBus bus)
                                                            throws IOException {
            server = ServerSocketChannel.open();
            server.socket().bind(socketAddress);
            this.actorFactory = actorFactory;
            this.bus = bus;
        }

        public void run() {
            while (true) {
                try {
                    SocketChannel channel = server.accept();
                    TCPChannelActor actor = actorFactory.accept(channel);
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
                          TCPChannelActorFactory factory,
                          MessageBus bus)
                                                            throws IOException {
        super(bus);
        acceptThread = new Thread(
                new ConnectionListener(socketAddress, factory, bus),
                "ConnectionLister[" + socketAddress +"]"
        );
        acceptThread.setDaemon(true); // Allow JVM to exit
    }

    public TCPServerActor(int port, TCPChannelActorFactory factory)
                                                            throws IOException {
        this(new InetSocketAddress(port), factory, MessageBus.getDefault());
    }

    public TCPServerActor(
            String hostname, int port, TCPChannelActorFactory factory)
                                                            throws IOException {
        this(new InetSocketAddress(hostname, port),
             factory, MessageBus.getDefault());
    }

    public TCPServerActor(
            int port, TCPChannelActorFactory factory, MessageBus bus)
                                                            throws IOException {
        this(new InetSocketAddress(port), factory, bus);
    }

    public TCPServerActor(String hostname, int port,
                          TCPChannelActorFactory factory, MessageBus bus)
                                                            throws IOException {
        this(new InetSocketAddress(hostname, port), factory, bus);
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
