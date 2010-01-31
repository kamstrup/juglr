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
    @DataProvider(name="response")
    public Iterator<Object[]> responseProvider() throws Exception {
        List<Object[]> data = new ArrayList<Object[]>();

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

    @Test(dataProvider = "response")
    public void checkWrite(ByteBuffer buf, String expected) {
        assertEquals(new String(buf.array()), expected);
    }
}
