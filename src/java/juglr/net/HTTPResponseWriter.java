package juglr.net;

import static juglr.net.HTTP.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 *
 */
public class HTTPResponseWriter {

    private SocketChannel channel;
    private ByteBuffer buf;

    public HTTPResponseWriter(SocketChannel channel,
                             ByteBuffer buf) {
        this.channel = channel;
        this.buf = buf;
    }

    public HTTPResponseWriter(SocketChannel channel) {
        this(channel, ByteBuffer.allocate(1024));
    }

    public HTTPResponseWriter reset(SocketChannel channel) throws IOException {
        close();
        this.channel = channel;
        buf.clear();

        return this;
    }

    public void writeVersion(Version version) throws IOException {
        switch (version) {
            case ONE_ZERO:
                writeBody("HTTP/1.0 ".getBytes());
                break;
            case ONE_ONE:
                writeBody("HTTP/1.1 ".getBytes());
                break;
            case UNKNOWN:
                writeBody("HTTP/1.0 ".getBytes());
                break;
            case ERROR:
                writeBody("HTTP/1.0 ".getBytes());
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

    public void writeBody(byte[] bytes) throws IOException {
        writeBody(bytes, 0, bytes.length);
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
