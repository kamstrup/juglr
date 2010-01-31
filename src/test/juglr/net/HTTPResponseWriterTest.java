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
 * Tests for {@link HTTPResponseWriter}
 */
public class HTTPResponseWriterTest {

    /*
     * Generate (in_data, out_expected) pairs of HTTP responses
     */
    @DataProvider(name="fullResponse")
    public Iterator<Object[]> fullResponseProvider() throws Exception {
        List<Object[]> data = new ArrayList<Object[]>();

        // A full write with all data written via writeBody(String)
        ByteBuffer buf = ByteBuffer.allocate(1024);
        HTTPResponseWriter w = new HTTPResponseWriter(null, buf);
        String msg =
                "HTTP/1.0 200 OK\r\n" +
                "Server: juglr\r\n"+
                "Content-Length: 10\r\n"+
                "\r\n" +
                "0123456789";
        w.writeBody(msg);
        buf.flip(); // Prepare buf for reading
        data.add(new Object[]{
                buf,
                msg
        });

        // A full write with all data written via writeBody(String)
        // this time with some non-ascii chars and no headers
        buf = ByteBuffer.allocate(1024);
        w = new HTTPResponseWriter(null, buf);
        msg = "HTTP/1.0 200 OK\r\n" +
              "\r\n" +
              "aoeaa-æøå";
        w.writeBody(msg);
        buf.flip();
        data.add(new Object[]{
                buf,
                msg
        });

        return data.iterator();
    }

    @Test(dataProvider = "fullResponse")
    public void checkFullWrite(ByteBuffer buf, String expected) {
        assertEquals(new String(buf.array()), expected);
    }

    /*
     * Generate (in_data, out_expected) pairs of HTTP responses
     */
    @DataProvider(name="response")
    public Iterator<Object[]> responseProvider() throws Exception {
        List<Object[]> data = new ArrayList<Object[]>();

        // A full write with all data written via writeBody(String)
        ByteBuffer buf = ByteBuffer.allocate(1024);
        HTTPResponseWriter w = new HTTPResponseWriter(null, buf);
        String msg =
                "HTTP/1.0 200 OK\r\n" +
                "Server: juglr\r\n"+
                "Content-Length: 10\r\n"+
                "\r\n" +
                "0123456789";
        w.writeVersion(HTTP.Version.ONE_ZERO);
        w.writeStatus(HTTP.Status.OK);
        w.writeHeader("Server", "juglr");
        w.writeHeader("Content-Length", "10");
        w.startBody();
        w.writeBody("0123456789");
        buf.flip(); // Prepare buf for reading
        data.add(new Object[]{
                buf,
                msg
        });

        // A full write with all data written via writeBody(String)
        // this time with some non-ascii chars and no headers
        buf = ByteBuffer.allocate(1024);
        w = new HTTPResponseWriter(null, buf);
        msg = "HTTP/1.0 202 Accepted\r\n" +
              "\r\n" +
              "aoeaa-æøå";
        w.writeVersion(HTTP.Version.ONE_ZERO);
        w.writeStatus(HTTP.Status.Accepted);
        w.startBody();
        w.writeBody("aoeaa-æøå");
        buf.flip();
        data.add(new Object[]{
                buf,
                msg
        });

        return data.iterator();
    }

    @Test(dataProvider = "response")
    public void checkWrite(ByteBuffer buf, String expected) {
        assertEquals(new String(buf.array()), expected);
    }
}
