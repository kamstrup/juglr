package juglr.net;

import juglr.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A generic light weight HTTP server handling requests by forwarding them
 * to a set of actors and sending the reponse from the actor back to the
 * client.
 * <p/>
 * The "backend" actors functions much as <i>bottom halves</i> in system
 * level interrupt programming. The HTTPServer manages an actor per active
 * HTTP connection which is considered the <i>upper half</i>. The upper half
 * dispatches incoming request to the right bottom half. When
 * the bottom half responds the upper half sends back the reply to the
 * HTTP client.
 *
 * @author Mikkel Kamstrup Erlandsen <mailto:mke@statsbiblioteket.dk>
 * @since Feb 15, 2010
 */
public class HTTPServer {

    static class Handler {
        Matcher path;
        Address bottomHalf;
        HTTP.Method[] methods;
    }

    final private List<Handler> canonicalHandlers =
                                           new CopyOnWriteArrayList<Handler>();
    final ThreadLocal<List<Handler>> handlers =
                                              new ThreadLocal<List<Handler>>();
    Address tcpServer;
    MessageBus bus;
    boolean isStarted;

    /**
     * Create a new HTTP server listening on port {@code port}. To start
     * listening you must invoke the server's {@link #start()} method. Before
     * doing so you must install a set of handlers with the
     * {@link #registerHandler} method.
     * <p/>
     * The HTTP server will be configured to spawn, and talk to, actors on
     * the default message bus.
     *
     * @param port the port to listen on
     * @throws IOException if there is an error setting up the server socket
     * @see juglr.MessageBus#getDefault()
     */
    public HTTPServer(int port) throws IOException {
        this(port, MessageBus.getDefault());
    }

    /**
     * Create a new HTTP server listening on port {@code port}. To start
     * listening you must invoke the server's {@link #start()} method. Before
     * doing so you must install a set of handlers with the
     * {@link #registerHandler} method.
     * <p/>
     * The HTTP server will be configured to spawn, and talk to, actors on
     * the message bus {@code messageBus}.
     *
     * @param port the port to listen on
     * @param messageBus the MessageBus to use
     * @throws IOException if there is an error setting up the server socket 
     */
    public HTTPServer(int port, MessageBus messageBus) throws IOException {
        bus = messageBus;
        tcpServer = new TCPServerActor(
                            port, new BottomHalvesStrategy(), bus).getAddress();
        isStarted = false;
    }

    /**
     * Configure all requests with URLs matching {@code pathRegex} to be handled
     * by the actor living at the {@code handler} address.
     * <p/>
     * Messages sent from the HTTPServer to the handler are guaranteed to be
     * instances of {@link HTTPRequest}s.
     * <p/>
     * The {@code handler} <i>must</i> reply with a {@link HTTPResponse} or
     * {@link Box} to any incoming message, sending the reply to
     * {@code msg.getReplyTo()}. Sending a {@code Box} as response will
     * automatically result in a HTTP status code 200 OK. To alter the response
     * code you must wrap the response box in a {@code HTTPResponse}.
     * <p/>
     * It is strongly recommended that the handler replies in a {@code finally}
     * clause. Failing to reply will result in dangling connections, and empty
     * replies or timeouts for clients.
     * <p/>
     * When looking up a matching handler the first one with a matching path
     * will be used. The handlers are checked in the order they are registered.
     *
     * @param urlRegex A regular expression the request URL must match
     *        for the request to be forwarded to {@code handler}
     * @param handler the address of the actor that handles the request
     * @param methods the HTTP methods the handler can accept
     * @throws IllegalStateException if the server has had its {@link #start()}
     *                               method invoked
     */
    public void registerHandler(
                    String urlRegex, Address handler, HTTP.Method... methods) {
        if (isStarted) {
            throw new IllegalStateException(
                  "Handlers can not be registered after HTTPServer is started");
        }
        Handler h = new Handler();
        h.path = Pattern.compile(urlRegex).matcher("");
        h.bottomHalf = handler;
        h.methods = methods;
        canonicalHandlers.add(h);
    }

    /**
     * Start listening on the configured port. After invoking {@code start()}
     * the {@link #registerHandler} method should not be called again
     * @throws IllegalStateException if the server has already been started
     */
    public void start() {
        if (isStarted) {
            throw new IllegalStateException("HTTPServer already started");
        }
        isStarted = true;
        bus.start(tcpServer);
    }

    /**
     * Get a thread local list of handlers with all mutable state safe for use
     * by the calling thread
     * @return a list of handlers registered for the server
     */
    private List<Handler> threadLocalHandlers() {
        // We use a thread local copy of our handlers list for matching
        // to ensure lockless lookups of the right bottom half  - otherwise
        // we'd need a lock when using _h.path
        List<Handler> _handlers = handlers.get();
        if (_handlers == null) {
            _handlers = new ArrayList<Handler>(canonicalHandlers.size());
            for (Handler h : canonicalHandlers) {
                Handler _h = new Handler();
                _h.path = h.path.pattern().matcher("");
                _h.bottomHalf = h.bottomHalf;
                _h.methods = h.methods;
                _handlers.add(_h);
            }
            handlers.set(_handlers);
        }
        return _handlers;
    }

    /**
     * This class dispatches incoming SocketChannels to bottom half actors that
     * handles the request as a Box. The bottom half then replies to
     * box.getReplyTo() which is forwarded into the waiting SocketChannel
     * after which this actor retracts from the bus
     */
    class BottomHalvesStrategy implements TCPChannelStrategy {

        public TCPChannelActor accept(SocketChannel channel) {
            return new TCPChannelActor(channel) {
                // FIXME: Use ThreadLocals here to save memory
                JSonBoxParser msgParser = new JSonBoxParser();
                HTTPRequestReader req = new HTTPRequestReader(channel);
                HTTPResponseWriter resp = new HTTPResponseWriter(channel);

                /* The bottom half actor should send a Box back to us */
                @Override
                public void react(Message msg) {
                    if (msg instanceof HTTPResponse) {
                        HTTPResponse resp = (HTTPResponse)msg;
                        respond(resp.getStatus(), resp.getBody());
                    } else if (msg instanceof Box) {
                        Box body = (Box)msg;
                        respond(HTTP.Status.OK, body);
                    } else {
                        respondError(HTTP.Status.InternalError, String.format(
                                "Error in internal message routing. Got '%s'," +
                                 " but expected Box or HTTPResponse",
                                msg.getClass().getSimpleName()));
                    }
                }

                @Override
                public void start() {
                    try {
                        // BEWARE: This buf array contains mutable state that is
                        //         propagated down to the bottom half
                        byte[] buf = new byte[1024];
                        HTTP.Method method = req.readMethod();

                        int uriLength = req.readURI(buf);
                        String uri = new String(buf, 0, uriLength);
                        Address bottomHalf = findBottomHalf(method, uri);

                        if (bottomHalf == null) {
                            respondError(HTTP.Status.NotFound,
                                         "No handler for path "
                                         + new String(buf, 0, uriLength));
                            return;
                        }

                        HTTP.Version ver = req.readVersion();
                        if (ver == HTTP.Version.ERROR ||
                            ver == HTTP.Version.UNKNOWN) {
                            respondError(HTTP.Status.BadRequest,
                                   "Illegal HTTP protocol version declaration");
                                return;
                        }


                        while (req.readHeaderField(buf) > 0) {
                            // FIXME: We ignore HTTP headers
                        }

                        Reader bodyReader =
                                     new InputStreamReader(req.streamBody());
                        dispatch(method, bottomHalf, uri, bodyReader);
                    } catch (IOException e) {
                        // Error writing response
                        // FIXME: Handle this gracefully
                        System.err.println("Error writing response");
                        e.printStackTrace();
                        bus.freeAddress(getAddress());
                    }
                }

                /* Send a response msg to the client and shut down the actor */
                private void respond(HTTP.Status status, Box body) {
                    try{
                        byte[] _body = body.toBytes();

                        resp.writeVersion(HTTP.Version.ONE_ZERO);
                        resp.writeStatus(status);
                        resp.writeHeader("Content-Length", "" + _body.length);
                        resp.writeHeader("Server", "juglr");
                        resp.startBody();
                        resp.writeBody(_body);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.err.println(
                              "Error sending HTTP response: " + e.getMessage());
                    } finally {
                        getBus().freeAddress(getAddress());
                        try {
                            resp.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println("Error closing HTTP connection: "
                                               + e.getMessage());
                        }
                    }
                }

                /* Send an error to the client and shut down the actor */
                private void respondError(HTTP.Status status, String msg) {
                    Box resp = Box.newMap();
                    respond(status, resp.put("error", msg));
                }

                private void dispatch(HTTP.Method method, Address bottomHalf,
                                      CharSequence uri, Reader msgBody)
                                                            throws IOException {
                    Box box;
                    try {
                        box = msgParser.parse(msgBody);
                    } catch (MessageFormatException e) {
                        respondError(HTTP.Status.BadRequest,
                                   "Illegal JSON POST data. " + e.getMessage());
                        return;
                    }

                    // BH should respond with a box to msg.getReplyTo()
                    // FIXME: We need a timeout to avoid leaking SocketChannels
                    send(new HTTPRequest(uri, method, box), bottomHalf);
                }

                private Address findBottomHalf(
                                         HTTP.Method method, CharSequence url) {
                    // Since _handlers is thread local, we don't have a critical
                    // region around the java.util.regex.Matcher h.path
                    List<Handler> _handlers = threadLocalHandlers();
                    for (Handler h : _handlers) {
                        for (HTTP.Method m : h.methods) {
                            if (m == method) {
                                h.path.reset(url);
                                if (h.path.matches()) {
                                    return h.bottomHalf;
                                }
                            }
                        }
                    }
                    return null;
                }
            };
        }
    }
}
