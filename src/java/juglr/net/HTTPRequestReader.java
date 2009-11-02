package juglr.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static juglr.net.HTTP.*;

/**
 *
 */
public class HTTPRequestReader {

    // FIXME: Document MAX_URI_LENGTH and make it tweakable
    public static final int MAX_URI_LENGTH = 1024;

    // FIXME: Document MAX_HEADER_LENGTH and make it tweakable
    public static final int MAX_HEADER_LENGTH = 1024;

    private SocketChannel channel;
    private ByteBuffer buf;   

    public HTTPRequestReader(SocketChannel channel,
                             ByteBuffer buf) {
        this.channel = channel;
        this.buf = buf;
    }

    public HTTPRequestReader(SocketChannel channel) {
        this(channel, ByteBuffer.allocate(1024));
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

    public int readURI(byte[] target) {
        int uriEnd = buf.position();
        while (buf.get(uriEnd) != ' ' &&
               uriEnd < MAX_URI_LENGTH &&
               uriEnd < buf.remaining()) {
            uriEnd++;
        }
        int numRead = uriEnd - buf.position();
        buf.get(target, 0, numRead);
        buf.get(); // Skip whitespace
        return numRead;
    }

    public Version readVersion() {
        if (buf.get() == 'H' &&
            buf.get() == 'T' &&
            buf.get() == 'T' &&
            buf.get() == 'P' &&
            buf.get() == '/' &&
            buf.get() == '1' &&
            buf.get() == '.')
            if (buf.get() == '0' &&
                buf.get() == '\r' &&
                buf.get() == '\n') {
                return Version.ONE_ZERO;
            } else if (buf.get() == '1' &&
                       buf.get() == '\r' &&
                       buf.get() == '\n') {
                return Version.ONE_ONE;
            } else {
                return Version.UNKNOWN;
            }
        return Version.ERROR;
    }

    public int readHeaderField(byte[] target) {
        int fieldEnd = buf.position();

        // The empty line just before the body
        if (fieldEnd + 2 < buf.limit() &&
            buf.get(fieldEnd) == '\r' && buf.get(fieldEnd + 1) == '\n') {

            // Move position to after fieldEnd + '\r' + '\n'
            buf.position(fieldEnd + 2);
            return 0;
        }

        while (fieldEnd < MAX_HEADER_LENGTH &&
               fieldEnd + 2 < buf.limit()) {

            // Success on line end
            if (buf.get(fieldEnd + 1) == '\r' && buf.get(fieldEnd +2) == '\n') {
                int numRead = Math.min(target.length,
                                       fieldEnd - buf.position() + 1);
                buf.get(target, 0, numRead);

                // Move position to after fieldEnd + '\r' + '\n', even though
                // we might have read less than the entire line
                buf.position(fieldEnd + 3);
                return numRead;
            }
            fieldEnd++;
        }

        // We did not meet a line end
        return -1;
    }

    public int readBody(byte[] target) {
        int numRead = Math.min(buf.remaining(), target.length);
        buf.get(target, 0, numRead);
        return numRead;
    }


}
