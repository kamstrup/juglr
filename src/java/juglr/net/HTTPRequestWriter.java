package juglr.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * Writer-like interface for constructing HTTP requests
 */
public class HTTPRequestWriter {

    private SocketChannel channel;
    private ByteBuffer buf;
    private CharsetEncoder encoder;

    public HTTPRequestWriter(SocketChannel channel,
                             ByteBuffer buf) {
        this.channel = channel;
        this.buf = buf;
        encoder = Charset.defaultCharset().newEncoder();
    }

    public HTTPRequestWriter(SocketChannel channel) {
        this(channel, ByteBuffer.allocate(1024));
    }

    /**
     * Clear all state and reset the writer to start writing a new response
     * tp {@code channel}. If the previous channel is open it will be closed.
     * @param channel the new socket channel to write to
     * @return always returns {@code this}
     * @throws java.io.IOException if the previous channel was open and there was an
     *                     error when closing it
     */
    public HTTPRequestWriter reset(SocketChannel channel) throws IOException {
        close();
        this.channel = channel;
        buf.clear();

        return this;
    }

    public void writeMethod(HTTP.Method method) throws IOException {
        writeBody(method.toString());
        writeBody((byte)' ');
    }

    public void writeUri(String uri) throws IOException {
        writeBody(uri);
        writeBody((byte)' ');
    }

    public void writeVersion(HTTP.Version version) throws IOException {
        switch (version) {
            case ONE_ZERO:
                writeBody("HTTP/1.0 ");
                break;
            case ONE_ONE:
                writeBody("HTTP/1.1 ");
                break;
            case UNKNOWN:
                writeBody("HTTP/1.0 ");
                break;
            case ERROR:
                writeBody("HTTP/1.0 ");
                break;
        }
        writeBody("\r\n");
    }

    public void writeStatus(HTTP.Status status) throws IOException {
        //FIXME : This is not particularly optimized, we allocate a lot of strings
        writeBody(Integer.toString(status.httpOrdinal()).getBytes());
        writeBody((" " + status + "\r\n").getBytes());
    }

    public void writeHeader(String name, String value) throws IOException {
        // FIXME: To many string/byte[] allocations
        writeBody((name + ": " + value + "\r\n").getBytes());
    }

    public void startBody() throws IOException {
        writeBody(new byte[]{'\r', '\n'});
    }

    public void writeBody(CharSequence s) throws IOException {
        CharBuffer cbuf = CharBuffer.wrap(s);
        while (encoder.encode(cbuf, buf, true) == CoderResult.OVERFLOW) {
            flush();
        }
    }

    public void writeBody(byte[] bytes) throws IOException {
        writeBody(bytes, 0, bytes.length);
    }

    public void writeBody (byte b) throws IOException {
        // Try and make room, if we are filled up. The flush() guarantees
        // that buf is cleared. Thus we should have space for at least one byte
        if (buf.remaining() == 1) {
            flush();
        }

        buf.put(b);
    }

    public void writeBody(byte[] bytes, int offset, int len) throws IOException {
        if (offset + len > bytes.length) {
            throw new ArrayIndexOutOfBoundsException(
                            "End position past buffer end: "
                            + offset + len + " > " + bytes.length);
        }

        // Try and make room, if we are filled up
        if (len > buf.remaining()) {
            flush();
        }

        // If there still isn't room we need to write and flush in chunks
        if (len > buf.remaining()) {
            throw new UnsupportedOperationException("FIXME");
        }

        buf.put(bytes, offset, len);
    }

    public void flush() throws IOException {
        buf.flip();
        channel.write(buf);
        buf.clear();
    }

    public void close() throws IOException {
        if (channel.isOpen()) {
            flush();
            channel.close();
        }
    }

}