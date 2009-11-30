package juglr.net;

import juglr.*;

import java.io.*;
import java.nio.channels.SocketChannel;

/**
 * FIXME: This class is incomplete
 * TODO:
 *   * Expose all message bus functionality remotely:
 *     - Remote address allocations (named and unique)
 *     - Remote lookup()
 *     - Remote start()
 *   * Implement a HTTPMessageBusProxy which is a MessageBus that talks
 *     to a remote HTTPMessageBus
 */
public class HTTPMessageBus extends MessageBus {

    private Actor server;

    public HTTPMessageBus(int port) throws IOException {
        server = new TCPServerActor(port, new ConnectionActorFactory(), this);
        server.start();
    }

    public HTTPMessageBus() throws IOException {
        this(getBusPort());
    }

    private static int getBusPort() {
        String busPort = System.getProperty("juglr.busport", "4567");
        try {
            return Integer.parseInt(busPort);
        } catch (NumberFormatException e) {
            throw new EnvironmentError(
                            "Unable to detemine message bus port from '"
                            + busPort + "': " + e.getMessage(), e);
        }
    }

    /**
     * This class is really just a factory for closures dispatching
     * HTTP messages to the recipient actor.
     */
    class ConnectionActorFactory implements TCPChannelActorFactory {

        public TCPChannelActor accept(final SocketChannel channel) {
            return new TCPChannelActor() {
                // FIXME: Use ThreadLocals here to save memory
                JSonMessageParser msgParser = new JSonMessageParser();
                HTTPRequestReader req = new HTTPRequestReader(channel);
                HTTPResponseWriter resp = new HTTPResponseWriter(channel);

                @Override
                public void react(Message msg) {

                }

                @Override
                public void start() {
                    try {
                        // FIXME: Allocation should be thread local
                        byte[] buf = new byte[1024];
                        HTTP.Method method = req.readMethod();

                        if (method != HTTP.Method.POST) {
                            respond(HTTP.Status.MethodNotAllowed,
                                    "Only POST allowed, got " + method);
                            return;
                        }

                        int uriLength = req.readURI(buf);
                        String uri = new String(buf, 0, uriLength);
                        HTTP.Version ver = req.readVersion();

                        while (req.readHeaderField(buf) > 0) {
                            // Skip HTTP headers
                        }

                        int bodyLength = req.readBody(buf);
                        InputStream bodyStream = new ByteArrayInputStream(
                                                            buf, 0, bodyLength);
                        Reader bodyReader = new InputStreamReader(bodyStream);

                        StructuredMessage msg;
                        try {
                            msg = msgParser.parse(bodyReader);
                        } catch (MessageFormatException e) {
                            respond(HTTP.Status.BadRequest,
                                    "Illegal JSON POST data");
                            return;
                        }

                        Address recipient = lookup(uri);
                        if (recipient == null) {
                            respond(
                                  HTTP.Status.NotFound, "No such actor " + uri);
                            return;
                        }

                        super.send(msg, recipient);
                        respond(HTTP.Status.Accepted,
                                "Message to " + uri + " accepted");
                    } catch (IOException e) {
                        // Error writing response
                        // FIXME: Handle this gracefully
                        System.err.println("Error writing response");
                        e.printStackTrace();
                    } finally {
                        try {
                            resp.close();
                        } catch (IOException e) {
                            e.printStackTrace(); // FIXME
                        }
                    }
                }

                private void respond(HTTP.Status status, String msg)
                                                            throws IOException {
                    resp.writeVersion(HTTP.Version.ONE_ZERO);
                    resp.writeStatus(status);
                    resp.writeHeader("Content-Length", "" + msg.length());
                    resp.writeHeader("Server", "juglr");
                    resp.startBody();

                    // FIXME: Unneeded memory duplication
                    resp.writeBody(msg.getBytes());
                }
            };
        }
    }

    public static void main(String[] args) {
        System.setProperty("juglr.busclass", "juglr.net.HTTPMessageBus");

        if (args.length > 0) {
            System.setProperty("juglr.busport", args[0]);
        }

        MessageBus bus = null;
        try {
            bus = MessageBus.getDefault();
        } catch (EnvironmentError e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            synchronized (bus) {
                bus.wait(); // Indefinite non-busy block
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupted");
            System.exit(-1);
        }
    }
}
