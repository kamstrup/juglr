package juglr.net;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static juglr.net.HTTP.*;

/**
 * base class for {@link HTTPRequestReader} and {@link HTTPResponseReader}
 */
public class HTTPReader {

    // FIXME: Document MAX_HEADER_LENGTH and make it tweakable
    public static final int MAX_HEADER_LENGTH = 1024;

    protected SocketChannel channel;
    protected ByteBuffer buf;

    public HTTPReader(SocketChannel channel,
                      ByteBuffer buf) {
        this.channel = channel;
        this.buf = buf;
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

    public Status readStatus() throws IOException {
        if (buf.remaining() < 3) {
            throw new BufferUnderflowException();
        }

        int d0 = buf.get() - 48;
        int d1 = buf.get() - 48;
        int d2 = buf.get() - 48;

        switch (d0) {
            case 2:
                if (d1 == 0) {
                    switch (d2) {
                        case 0: return Status.OK;
                        case 1: return Status.Created;
                        case 2: return Status.Accepted;
                        case 4: return Status.NoContent;
                    }
                }
                throw new UnsupportedStatusException(d0, d1, d2);
            case 3:
                if (d1 == 0 && d2 == 2) return Status.Found;
                throw new UnsupportedStatusException(d0, d1, d2);
            case 4:
                if (d1 == 0) {
                    switch (d2) {
                        case 0: return Status.BadRequest;
                        case 1: return Status.Unauthorized;
                        case 3: return Status.Forbidden;
                        case 4: return Status.NotFound;
                        case 5: return Status.MethodNotAllowed;
                        case 6: return Status.NotAcceptable;
                        case 8: return Status.RequestTimeout;
                        case 9: return Status.Conflict;
                    }
                }
                throw new UnsupportedStatusException(d0, d1, d2);
            case 5:
                if (d1 == 0 && d2 == 0) return Status.InternalError;
                throw new UnsupportedStatusException(d0, d1, d2);
            default:
                throw new UnsupportedStatusException(d0, d1, d2);

        }
    }

    /**
     * Parse a HTTP protocol version declaration as used in the HTTP
     * protocol. The buffer will be positioned just after the last parsed
     * character upon method return
     * @return the HTTP version or {@link Version#ERROR} on errors
     */
    public HTTP.Version readVersion() throws IOException {
        if (buf.get() == 'H' &&
                buf.get() == 'T' &&
                buf.get() == 'T' &&
                buf.get() == 'P' &&
                buf.get() == '/' &&
                buf.get() == '1' &&
                buf.get() == '.')
            if (buf.get() == '0') {
                return Version.ONE_ZERO;
            } else if (buf.get() == '1') {
                return Version.ONE_ONE;
            } else {
                return Version.UNKNOWN;
            }
        return Version.ERROR;
    }

    public boolean readSpace() throws IOException {
        if (buf.remaining() == 0) {
            channel.read(buf);
            buf.flip();
        }
        return buf.get() == ' ';
    }

    public boolean readLF() throws IOException {
        if (buf.remaining() == 0) {
            channel.read(buf);
            buf.flip();
        }
        if (buf.get() != '\r') {
            return false;
        }
        if (buf.remaining() == 0) {
            channel.read(buf);
            buf.flip();
        }
        return buf.get() == '\n';

    }

    /**
     * Close the underlying channel if it's open
     * @throws IOException if the underlying channel is open and there is an
     *                     error closing the channel
     */
    public void close() throws IOException {
        if (this.channel.isOpen()) {
            this.channel.close();
        }
    }

    /**
     * Reset all state in the reader, preparing it for reading {@code channel}.
     * @param channel the channel to start reading from. If the reader refers
     * a channel then this channel will be closed.
     *
     * @return always returns {@code this}
     * @throws IOException if the reader already refers an open channel and
     *                     there is an error closing it
     */
    public HTTPReader reset(SocketChannel channel) throws IOException {
        close();
        this.channel = channel;
        buf.clear();

        return this;
    }
}
