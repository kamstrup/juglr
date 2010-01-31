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
public class HTTPResponseWriter extends HTTPWriter {

    public HTTPResponseWriter(SocketChannel channel, ByteBuffer buf) {
        super(channel, buf);
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
        super.writeVersion(version);
        writeSpace();
    }

    public void writeStatus(Status status) throws IOException {
        writeBody(Integer.toString(status.httpOrdinal()));
        writeSpace();
        writeBody(status.toString());
        writeLF();
    }



}
