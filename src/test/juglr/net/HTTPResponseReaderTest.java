package juglr.net;

import com.sun.xml.internal.bind.v2.util.ByteArrayOutputStreamEx;
import juglr.Box;
import juglr.BoxParser;
import juglr.JSonBoxParser;
import juglr.Message;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Test cases for HTTPReponseReader
 */
public class HTTPResponseReaderTest extends HTTPReaderTest {    

    /*
     * Generate (in_data, out_expected) pairs of HTTP responses
     */
    @DataProvider(name="response")
    public Iterator<Object[]> responseProvider() throws Exception {
        List<Object[]> data = new ArrayList<Object[]>();

        // A few headers, small ascii body
        ByteBuffer buf = ByteBuffer.allocate(1024);
        String msg =
                "HTTP/1.0 200 OK\r\n" +
                "Server: juglr\r\n"+
                "Content-Length: 10\r\n"+
                "\r\n" +
                "0123456789";
        buf.put(msg.getBytes());
        buf.flip();
        data.add(new Object[]{
                prepareSocket(buf),
                HTTP.Version.ONE_ZERO,
                HTTP.Status.OK,
                2,
                "0123456789"
        });

        // No headers, small unicode body
        buf = ByteBuffer.allocate(1024);
        msg =
                "HTTP/1.0 200 OK\r\n" +
                "\r\n" +
                "aoeaa-æøå";
        buf.put(msg.getBytes());
        buf.flip();
        data.add(new Object[]{
                prepareSocket(buf),
                HTTP.Version.ONE_ZERO,
                HTTP.Status.OK,
                0,
                "aoeaa-æøå"
        });

        // One header, no body
        buf = ByteBuffer.allocate(1024);
        msg =
                "HTTP/1.0 404 NotFound\r\n" +
                "Server: juglr\r\n" +
                "\r\n";
        buf.put(msg.getBytes());
        buf.flip();
        data.add(new Object[]{
                prepareSocket(buf),
                HTTP.Version.ONE_ZERO,
                HTTP.Status.NotFound,
                1,
                ""
        });

        // No header, no body
        buf = ByteBuffer.allocate(1024);
        msg =
                "HTTP/1.0 404 NotFound\r\n" +
                "\r\n";
        buf.put(msg.getBytes());
        buf.flip();
        data.add(new Object[]{
                prepareSocket(buf),
                HTTP.Version.ONE_ZERO,
                HTTP.Status.NotFound,
                0,
                ""
        });

        // One header, large odd-sized ascii body
        buf = ByteBuffer.allocate(206577);
        msg =
                "HTTP/1.0 200 OK\r\n" +
                "Server: juglr\r\n" +
                "\r\n";
        buf.put(msg.getBytes());
        byte[] body = new byte[buf.remaining()];
        for (int i = 0; i < buf.remaining(); i++) {
            body[i] = 'z';
        }
        buf.put(body);
        buf.flip();
        data.add(new Object[]{
                prepareSocket(buf),
                HTTP.Version.ONE_ZERO,
                HTTP.Status.OK,
                1,
                new String(body)
        });
        
        return data.iterator();
    }

    @Test(dataProvider="response")
    public void readHeader(SocketChannel channel, HTTP.Version version,
                           HTTP.Status status, int numHeaders,
                           String expectedBody)
                                                              throws Exception {
        HTTPResponseReader r = new HTTPResponseReader(channel);

        HTTP.Version v = r.readVersion();
        assertEquals(v, version);

        HTTP.Status s = r.readStatus();
        assertEquals(s, status);

        byte[] _buf = new byte[1024];
        int numRead;
        int _numHeaders = 0;
        while ((numRead = r.readHeaderField(_buf)) > 0) {
            _numHeaders++;
        }
        assertEquals(_numHeaders, numHeaders);

        InputStream bodyStream = r.streamBody();
        ByteArrayOutputStream _body = new ByteArrayOutputStream();
        while ((numRead = bodyStream.read(_buf)) != -1) {
            _body.write(_buf, 0, numRead);
        }

        String actualBody = new String(_body.toByteArray());
        assertEquals(actualBody.length(), expectedBody.length());
        assertEquals(actualBody, expectedBody);

        r.close();
    }

}
