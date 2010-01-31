package juglr.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static juglr.net.HTTP.*;

/**
 * A {@link java.io.Reader}-like API for parsing HTTP requests from a
 * {@link SocketChannel}. This class does
 * not automatically parse the HTTP header and body. You must manually do this,
 * by calling the appropriate {@code read*}-methods in the order the
 * corresponding elements occur in the HTTP request.
 * The reason for this is to allow for absolutely optimized parsing of the
 * HTTP request.
 *
 * <p/>
 * To parse a HTTP request you would do something like:
 * <pre>
 * byte[] buf = new byte[1024];
 * HTTPRequestReader reader = new HTTPRequestReader(chan);
 * HTTP.Method method = reader.readMethod();
 * int uriLen = reader.readURI(buf);
 * HTTP.Version version = reader.readVersion();
 *
 * System.out.println("Request URI: " + new String(buf, 0, uriLen));
 *
 * while ((int headerLen = reader.readHeaderField(buf)) &gt; 0) {
 *     System.out.println("Header field: " + new String(buf, 0, headerLen));
 * }
 *
 * System.out.println("Body:");
 * while ((int len = reader.readBody(buf)) > 0) {
 *     System.out.print(new String(buf, 0, len));
 * }
 * System.out.println();
 * </pre>
 */
public class HTTPRequestReader extends HTTPReader {

    // FIXME: Document MAX_URI_LENGTH and make it tweakable
    public static final int MAX_URI_LENGTH = 1024;    

    public HTTPRequestReader(SocketChannel channel,
                             ByteBuffer buf) {
        super(channel, buf);
    }

    public HTTPRequestReader(SocketChannel channel) {
        this(channel, ByteBuffer.allocate(1024));
    }

    @Override
    public HTTPRequestReader reset(SocketChannel channel) throws IOException {
        super.reset(channel);
        return this;
    }

    public Method readMethod() {
        int numRead;
        try {
            numRead = channel.read(buf);
            buf.flip();
        } catch (IOException e) {
            return Method.ERROR;
        }

        if (numRead <= 5) {
            /* Connection has been truncated or closed, we need at least
             * 5 bytes for GET with a white space and the next byte */
            return Method.ERROR;
        }

        byte b0 = buf.get(0), b1 = buf.get(1), b2 = buf.get(2), b3 = buf.get(3);
        switch (b0) {
            case 'P':
                if (b1 == 'O' && b2 == 'S' && b3 == 'T' &&
                    buf.get(4) == ' ' && numRead >= 6) {
                    buf.position(5);
                    return Method.POST;
                } else if (b1 == 'U' && b2 == 'T' && b3 == ' ') {
                    buf.position(4);
                    return Method.PUT;
                }
            case 'G':
                if (b1 == 'E' && b2 == 'T' && b3 == ' ') {
                    buf.position(4);
                    return Method.GET;
                }
            case 'H':
                if (numRead >= 6 && b1 == 'E' && b2 == 'A' &&
                    b3 == 'D' && buf.get(4) == ' ') {
                    buf.position(5);
                    return Method.HEAD;
                }
            case 'D':
                if (numRead >= 8 && b1 == 'E' && b2 == 'L' && b3 == 'E' &&
                    buf.get(4) == 'T' && buf.get(5) == 'E' &&
                    buf.get(6) == ' ') {
                    buf.position(7);
                    return Method.DELETE;
                }
            case 'T':
                if (numRead >= 7 && b1 == 'R' && b2 == 'A' && b3 == 'C' &&
                    buf.get(4) == 'E' && buf.get(5) == ' ') {
                    buf.position(6);
                    return Method.TRACE;
                }
            case 'C':
                if (numRead >= 9 && b1 == 'O' && b2 == 'N' && b3 == 'N' &&
                    buf.get(4) == 'E' && buf.get(5) == 'C' &&
                    buf.get(6) == 'T' && buf.get(7) == ' ') {
                    buf.position(8);
                    return Method.CONNECT;
                }
            default:
                return Method.UNKNOWN;
        }
    }

    public int readURI(byte[] target) throws IOException {
        int uriEnd = buf.position();
        while (buf.get(uriEnd) != ' ' &&
               uriEnd < MAX_URI_LENGTH &&
               uriEnd < buf.remaining()) {
            uriEnd++;
        }
        int numRead = uriEnd - buf.position();
        buf.get(target, 0, numRead);
        readSpace(); // Skip whitespace
        return numRead;
    }

    public HTTP.Version readVersion() throws IOException {
        Version v = super.readVersion();
        if (readLF()) return v;
        return Version.ERROR;
    }

    


}
