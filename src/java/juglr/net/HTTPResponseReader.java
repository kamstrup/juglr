package juglr.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 *
 */
public class HTTPResponseReader {

    // FIXME: Document MAX_URI_LENGTH and make it tweakable
    public static final int MAX_URI_LENGTH = 1024;

    // FIXME: Document MAX_HEADER_LENGTH and make it tweakable
    public static final int MAX_HEADER_LENGTH = 1024;

    private SocketChannel channel;
    private ByteBuffer buf;

    public HTTPResponseReader(SocketChannel channel,
                              ByteBuffer buf) {
        this.channel = channel;
        this.buf = buf;
    }

    public HTTPResponseReader(SocketChannel channel) {
        this(channel, ByteBuffer.allocate(1024));
    }

    /**
     * Reset all state in the reader, preparing it for reading {@code channel}.
     * @param channel the channel to start reading from. If the reader refers
     * a channel then this channel will be closed.
     *
     * @return always returns {@code this}
     * @throws java.io.IOException if the reader already refers an open channel and
     *                     there is an error closing it
     */
    public HTTPResponseReader reset(SocketChannel channel) throws IOException {
        close();
        this.channel = channel;
        buf.clear();

        return this;
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

    public HTTP.Version readVersion() {
        HTTP.Version v = HTTP.Version.read(buf);
        if (buf.get() != ' ') {
            return HTTP.Version.ERROR;
        }
        return v;
    }

    public HTTP.Status readStatus() {
        HTTP.Status status = HTTP.Status.read(buf);

        // Skip past line end, to header section
        while (buf.get() != '\r');
        buf.get(); // \n

        return status;
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
