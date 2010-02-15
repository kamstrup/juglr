package juglr.net;

import juglr.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A flexible light weight HTTP server
 *
 * @author Mikkel Kamstrup Erlandsen <mailto:mke@statsbiblioteket.dk>
 * @since Feb 15, 2010
 */
public class HTTPServer {

    private static class Handler {
        Matcher path;
        Address bottomHalf;
        HTTP.Method[] methods;
    }

    final private List<Handler> canonicalHandlers =
                                           new CopyOnWriteArrayList<Handler>();
    final ThreadLocal<List<Handler>> handlers =
                                              new ThreadLocal<List<Handler>>();
    TCPServerActor tcpServer;
    MessageBus bus;

    public HTTPServer(int port) throws IOException {
        this(port, MessageBus.getDefault());
    }

    public HTTPServer(int port, MessageBus messageBus) throws IOException {
        bus = messageBus;
        tcpServer = new TCPServerActor(port, new BottomHalvesStrategy(), bus);
    }

    public void registerHandler(
                    String pathRegex, Address handler, HTTP.Method... methods) {
        Handler h = new Handler();
        h.path = Pattern.compile(pathRegex).matcher("");
        h.bottomHalf = handler;
        h.methods = methods;
        canonicalHandlers.add(h);
    }

    public Address findHandler(HTTP.Method method, CharSequence path) {
        // We use a thread local copy of our handlers list for matching
        // to ensure lockless lookups of the right bottom half
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

        // Since _handlers is thread local, we don't have a critical region
        // around the java.util.regex.Matcher h.path
        for (Handler h : _handlers) {
            for (HTTP.Method m : h.methods) {
                if (m == method) {
                    h.path.reset(path);
                    if (h.path.matches()) {
                        return h.bottomHalf;
                    }
                }
            }
        }
        return null;
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
                    if (!(msg instanceof Box)) {
                        throw new MessageFormatException(
                                                  "Expected Box");
                    }

                    Box box = (Box)msg;
                    HTTP.Status status;
                    if (box.getType() == Box.Type.MAP &&
                        box.has("__httpStatusCode__")) {
                        status = HTTP.Status.fromHttpOrdinal(
                                                           (int)box.getLong());
                         box.getMap().remove("__httpStatusCode__");
                    } else {
                        status = HTTP.Status.OK;
                    }

                    try {
                        respond(status, box);
                    } catch (IOException e) {
                        // FIXME
                        e.printStackTrace();
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
                        CharSequence uri =
                              ByteBuffer.wrap(buf, 0, uriLength).asCharBuffer();
                        Address bottomHalf = findHandler(method, uri);

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
                            // Skip HTTP headers
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
                private void respond(HTTP.Status status, Box msg)
                                                            throws IOException {
                    try{
                        String msgString = msg.toString();

                        resp.writeVersion(HTTP.Version.ONE_ZERO);
                        resp.writeStatus(status);
                        resp.writeHeader("Content-Length", "" + msgString.length());
                        resp.writeHeader("Server", "juglr");
                        resp.startBody();
                        resp.writeBody(msgString);
                    } finally {
                        getBus().freeAddress(getAddress());
                        resp.close();                        
                    }
                }

                /* Send an error to the client and shut down the actor */
                private void respondError(HTTP.Status status, String msg)
                                                            throws IOException {
                    Box resp = Box.newMap();
                    respond(status, resp.put("error", msg));
                }

                private void dispatch(HTTP.Method method, Address bottomHalf,
                                      CharSequence uri, Reader msgBody)
                                                            throws IOException {
                    Box msg;
                    try {
                        msg = msgParser.parse(msgBody);
                    } catch (MessageFormatException e) {
                        respondError(HTTP.Status.BadRequest,
                                   "Illegal JSON POST data. " + e.getMessage());
                        return;
                    }

                    // BH should respond with a box to msg.getReplyTo()
                    // FIXME: We need a timeout to avoid leaking SocketChannels
                    send(msg, bottomHalf);
                }
            };
        }
    }
}
