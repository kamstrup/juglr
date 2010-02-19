package juglr.net;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * Tests for {@link juglr.net.HTTPRequestReader}
 *
 * @author Mikkel Kamstrup Erlandsen <mailto:mke@statsbiblioteket.dk>
 * @since Feb 19, 2010
 */
public class HTTPRequestReaderTest extends HTTPReaderTest {    

    /*
     * Generate (in_data, out_expected) pairs of HTTP responses
     */
    @DataProvider(name="response")
    public Iterator<Object[]> responseProvider() throws Exception {
        List<Object[]> data = new ArrayList<Object[]>();

        // Two headers, small ascii body
        ByteBuffer buf = ByteBuffer.allocate(1024);
        String msg =
                "GET /foo HTTP/1.0\r\n" +
                "User-Agent: Juglr/0.3\r\n"+
                "Content-Length: 10\r\n"+
                "\r\n" +
                "0123456789";
        buf.put(msg.getBytes());
        buf.flip();
        data.add(new Object[]{
                prepareSocket(buf),
                HTTP.Method.GET,
                "/foo",
                HTTP.Version.ONE_ZERO,
                2,
                "0123456789"
        });

        // One header, small unicode body
        buf = ByteBuffer.allocate(1024);
        msg =
                "POST / HTTP/1.0\r\n" +
                "User-Agent: Juglr/0.3\r\n"+
                "\r\n" +
                "æøå";
        buf.put(msg.getBytes());
        buf.flip();
        data.add(new Object[]{
                prepareSocket(buf),
                HTTP.Method.POST,
                "/",
                HTTP.Version.ONE_ZERO,
                1,
                "æøå"
        });

        return data.iterator();
    }

    @Test(dataProvider="response")
    public void readHeader(SocketChannel channel, HTTP.Method method,
                           String uri, HTTP.Version version, int numHeaders,
                           String expectedBody)
                                                              throws Exception {
        byte[] buf = new byte[1024];
        HTTPRequestReader r = new HTTPRequestReader(channel);
        HTTP.Method m = r.readMethod();
        int numRead = r.readURI(buf);
        String u = new String(buf, 0, numRead);
        HTTP.Version v = r.readVersion();

        assertEquals(m, method);
        assertEquals(u, uri);
        assertEquals(v, version);


        int _numHeaders = 0;
        while ((numRead = r.readHeaderField(buf)) > 0) {
            _numHeaders++;
        }
        assertEquals(_numHeaders, numHeaders);

        InputStream bodyStream = r.streamBody();
        ByteArrayOutputStream _body = new ByteArrayOutputStream();
        while ((numRead = bodyStream.read(buf)) != -1) {
            _body.write(buf, 0, numRead);
        }

        String actualBody = new String(_body.toByteArray());
        assertEquals(actualBody.length(), expectedBody.length());
        assertEquals(actualBody, expectedBody);

        r.close();
    }
}
