import juglr.Box;
import juglr.BoxParser;
import juglr.JSonBoxParser;
import juglr.net.HTTP;
import juglr.net.HTTPRequestWriter;
import juglr.net.HTTPResponseReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Flood the HTTPServerExample with requests
 */
public class HTTPServerExampleClient {

    public static void main(String[] args) throws Exception {
        InetSocketAddress socketAddress =
                   new InetSocketAddress("localhost", 4567);
        for (int i = 1; i < 10000; i++) {
            ByteBuffer buf = ByteBuffer.allocate(1024);
            SocketChannel channel = SocketChannel.open(socketAddress);

            // Send request
            HTTPRequestWriter w = new HTTPRequestWriter(channel, buf);
            w.writeMethod(HTTP.Method.POST);
            w.writeUri("/actor/calc/");
            w.writeVersion(HTTP.Version.ONE_ZERO);
            w.writeHeader("User-Agent", "Juglr/0.2");
            w.startBody();
            w.writeBody("{\"isPrime\" : " + i + "}");
            w.flush();

            // Read response
            buf.clear();
            HTTPResponseReader r = new HTTPResponseReader(channel, buf);
            HTTP.Version v = r.readVersion();
            if (v == HTTP.Version.ERROR || v == HTTP.Version.UNKNOWN) {
                throw new IOException("Bad protocol version version");
            }
            int status = r.readStatus().httpOrdinal();
            if (status < 200 || status >= 300) {
                channel.close();
                throw new RuntimeException("Bad response code " + status);
            }

            byte[] bbuf = new byte[1024];
            while (r.readHeaderField(bbuf) > 0) {
                // Ignore headers
            }

            BoxParser parser = new JSonBoxParser();
            Box box = parser.parse(new InputStreamReader(r.streamBody()));
            System.out.println("Response: " + box.toString());

            w.close();
        }

    }

}
