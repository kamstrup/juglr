package juglr.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * Base class for {@link HTTPResponseWriter} and {@link HTTPRequestWriter}
 */
public class HTTPWriter {

    protected ByteBuffer buf;
    protected SocketChannel channel;
    private CharsetEncoder encoder;

    HTTPWriter (SocketChannel channel, ByteBuffer buf) {
        this.buf = buf;
        this.channel = channel;
        encoder = Charset.defaultCharset().newEncoder();
    }

    public void writeSpace() throws IOException {
        if (buf.remaining() <= 1) {
            flush();
        }

        buf.put((byte)' ');
    }

    public void writeLF() throws IOException {
        if (buf.remaining() <= 2) {
            flush();
        }

        buf.put((byte)'\r').put((byte)'\n');
    }

    public void writeVersion(HTTP.Version version) throws IOException {
        switch (version) {
            case ONE_ZERO:
                writeBody("HTTP/1.0");
                break;
            case ONE_ONE:
                writeBody("HTTP/1.1");
                break;
            case UNKNOWN:
            case ERROR:
                writeBody("HTTP/1.0");
                break;
        }
    }

    public void writeHeader(String name, String value) throws IOException {
        writeBody(name);
        writeBody(": ");
        writeBody(value);
        writeLF();        
    }

    public void startBody() throws IOException {
        writeLF();
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
        if (buf.remaining() <= 1) {
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

        // If there still isn't room we need to write and flush in chunks,
        // if it fits entirely in the buffer, then it's easy
        if (len < buf.remaining()) {
            buf.put(bytes, offset, len);
        } else {
            int remainingBytes = len;
            int toWrite;
            while (remainingBytes > 0) {
                toWrite = Math.min(remainingBytes, buf.remaining());
                assert toWrite > 0;
                buf.put(bytes, offset + (len-remainingBytes), toWrite);
                remainingBytes -= toWrite;
                flush();
            }
        }
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
