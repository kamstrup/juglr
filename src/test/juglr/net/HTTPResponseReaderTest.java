package juglr.net;

import juglr.Box;
import juglr.BoxParser;
import juglr.JSonBoxParser;
import juglr.Message;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Test cases for HTTPReponseReader
 */
public class HTTPResponseReaderTest {

    /*
     * Generate (in_data, out_expected) pairs of HTTP responses
     */
    @DataProvider(name="response")
    public Iterator<Object[]> responseProvider() {
        List<Object[]> data = new ArrayList<Object[]>();

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
                buf,
                HTTP.Version.ONE_ZERO,
                HTTP.Status.OK,
                2,
                "0123456789"
        });

        buf = ByteBuffer.allocate(1024);
        msg =
                "HTTP/1.0 200 OK\r\n" +
                "\r\n" +
                "aoeaa-æøå";
        buf.put(msg.getBytes());
        buf.flip();
        data.add(new Object[]{
                buf,
                HTTP.Version.ONE_ZERO,
                HTTP.Status.OK,
                0,
                "aoeaa-æøå"
        });

        return data.iterator();
    }

    @Test(dataProvider="response")
    public void readHeader(ByteBuffer buf, HTTP.Version version,
                           HTTP.Status status, int numHeaders, String body) {
        HTTPResponseReader r = new HTTPResponseReader(null, buf);

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

        numRead = r.readBody(_buf);
        String _body = new String(_buf, 0, numRead);
        assertEquals(_body, body);
    }

}
