package juglr.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 *
 */
public class HTTPResponseReader extends HTTPReader {

    public HTTPResponseReader(SocketChannel channel,
                              ByteBuffer buf) {
        super(channel, buf);
    }

    public HTTPResponseReader(SocketChannel channel) {
        this(channel, ByteBuffer.allocate(1024));
    }

    @Override
    public HTTPResponseReader reset(SocketChannel channel) throws IOException {
        super.reset(channel);
        return this;
    }    

    @Override
    public HTTP.Version readVersion() throws IOException {
        // This is the first read, so fill the buffer
        channel.read(buf);
        buf.flip();

        HTTP.Version v = super.readVersion();
        if (readSpace()) {
            return v;
        }
        return HTTP.Version.ERROR;
    }

    @Override
    public HTTP.Status readStatus() throws IOException {
        HTTP.Status status = super.readStatus();

        // Skip past line end, to header section
        while (buf.get() != '\r');
        buf.get(); // \n

        return status;
    }

}
