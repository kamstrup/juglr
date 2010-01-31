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
public class HTTPRequestWriter extends HTTPWriter {

    public HTTPRequestWriter(SocketChannel channel,
                             ByteBuffer buf) {
        super(channel, buf);
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
        writeSpace();
    }

    public void writeUri(CharSequence uri) throws IOException {
        writeBody(uri);
        writeSpace();
    }

    public void writeVersion(HTTP.Version version) throws IOException {
        super.writeVersion(version);
        writeBody("\r\n");
    }

}
