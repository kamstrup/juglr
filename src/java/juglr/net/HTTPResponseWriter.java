package juglr.net;

import static juglr.net.HTTP.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * A {@link java.io.Writer}-like class for writing HTTP responses to a
 * {@link SocketChannel}.This class does not automatically respect the
 * HTTP protocol and you must manually call the the {@code write*}-methods
 * in the correct order to build a valid HTTP response. This is because
 * it must be possible to use this class in a fully optimized HTTP server.
 * <p/>
 * Writing a correct HTTP response looks something like:
 * <pre>
 * String msgString("Hello world of HTTP!");
 * resp.writeVersion(HTTP.Version.ONE_ZERO);
 * resp.writeStatus(HTTP.Status.OK);
 * resp.writeHeader("Content-Length", "" + msgString.length());
 * resp.writeHeader("Server", "juglr");
 * resp.startBody();
 * resp.writeBody(msgString);
 * resp.close();
 * </pre>
 */
public class HTTPResponseWriter {

    private SocketChannel channel;
    private ByteBuffer buf;
    private CharsetEncoder encoder;

    public HTTPResponseWriter(SocketChannel channel,
                             ByteBuffer buf) {
        this.channel = channel;
        this.buf = buf;
        encoder = Charset.defaultCharset().newEncoder();
    }

    public HTTPResponseWriter(SocketChannel channel) {
        this(channel, ByteBuffer.allocate(1024));
    }

    /**
     * Clear all state and reset the writer to start writing a new response
     * tp {@code channel}. If the previous channel is open it will be closed.
     * @param channel the new socket channel to write to
     * @return always returns {@code this}
     * @throws IOException if the previous channel was open and there was an
     *                     error when closing it
     */
    public HTTPResponseWriter reset(SocketChannel channel) throws IOException {
        close();
        this.channel = channel;
        buf.clear();

        return this;
    }

    public void writeVersion(Version version) throws IOException {
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
    }

    public void writeStatus(Status status) throws IOException {
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
