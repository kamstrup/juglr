package juglr;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.Reader;
import java.io.IOException;

/**
 *
 */
public class JSonMessageReaderTest {

    @DataProvider(name="simple")
    public Iterator<Object[]> createSimpleStructuredMessages() {
        List<Object[]> messages = new ArrayList<Object[]>();

        /* Strings */
        messages.add(new Object[]{
                         new StructuredMessage(""),
                         "\"\""});
        messages.add(new Object[]{
                         new StructuredMessage("Hello world "),
                         "\"Hello world \""});
        messages.add(new Object[]{
                         new StructuredMessage("åæø"),
                         "\"åæø\""});

        /* Ints/Longs */
        messages.add(new Object[]{
                         new StructuredMessage(-1),
                         "-1"});
        messages.add(new Object[]{
                         new StructuredMessage(0),
                         "0"});
        messages.add(new Object[]{
                         new StructuredMessage(1),
                         "1"});
        messages.add(new Object[]{
                         new StructuredMessage(Integer.MAX_VALUE),
                         "" + Integer.MAX_VALUE});
        messages.add(new Object[]{
                         new StructuredMessage(Integer.MIN_VALUE),
                         "" + Integer.MIN_VALUE});
        messages.add(new Object[]{
                         new StructuredMessage(Long.MAX_VALUE),
                         "" + Long.MAX_VALUE});
        messages.add(new Object[]{
                         new StructuredMessage(Long.MIN_VALUE),
                         "" + Long.MIN_VALUE});

        /* Booleans */
        messages.add(new Object[]{
                         new StructuredMessage(true),
                         "true"});
        messages.add(new Object[]{
                         new StructuredMessage(false),
                         "false"});

        /* Float/Double */
        messages.add(new Object[]{
                         new StructuredMessage(0D),
                         "" + 0D});
        messages.add(new Object[]{
                         new StructuredMessage(-1D),
                         "" + -1D});
        messages.add(new Object[]{
                         new StructuredMessage(1D),
                         "" + 1D});

        return messages.iterator();
    }

    @Test(dataProvider="simple")
    public void cmpSimpleMsg2JS(StructuredMessage msg, String json) throws IOException {
        Reader r = new JSonMessageReader(msg);
        assertEquals(readFully(r), json);
    }

    @DataProvider(name="list")
    public Iterator<Object[]> createListmessages() {
        List<Object[]> messages = new ArrayList<Object[]>();

        messages.add(new Object[]{
                         new StructuredMessage(StructuredMessage.Type.LIST),
                         "[]"});
        messages.add(new Object[]{
                         new StructuredMessage(StructuredMessage.Type.LIST).add(new StructuredMessage("")),
                         "[\"\"]"});
        messages.add(new Object[]{
                         new StructuredMessage(StructuredMessage.Type.LIST)
                                 .add(new StructuredMessage("Hello world")),
                         "[\"Hello world\"]"});
        messages.add(new Object[]{
                         new StructuredMessage(StructuredMessage.Type.LIST)
                                 .add(new StructuredMessage("Hello"))
                                 .add(new StructuredMessage("world")),
                         "[\"Hello\",\"world\"]"});
        messages.add(new Object[]{
                         new StructuredMessage(StructuredMessage.Type.LIST)
                                 .add(new StructuredMessage(StructuredMessage.Type.LIST)),
                         "[[]]"});
        messages.add(new Object[]{
                         new StructuredMessage(StructuredMessage.Type.LIST)
                                 .add(new StructuredMessage("One"))
                                 .add(new StructuredMessage(1)),
                         "[\"One\",1]"});
        messages.add(new Object[]{
                         new StructuredMessage(StructuredMessage.Type.LIST)
                                 .add(new StructuredMessage(true)),
                         "[true]"});
        messages.add(new Object[]{
                         new StructuredMessage(StructuredMessage.Type.LIST)
                                 .add(new StructuredMessage(1.1D))
                                 .add(new StructuredMessage(StructuredMessage.Type.LIST)),
                         "["+1.1D+",[]]"});

        return messages.iterator();
    }

    @Test(dataProvider="list")
    public void cmpListMsg2JS(StructuredMessage msg, String json) throws IOException {
        Reader r = new JSonMessageReader(msg);
        assertEquals(readFully(r), json);
    }

    @DataProvider(name="map")
    public Iterator<Object[]> createMapmessages() {
        List<Object[]> messages = new ArrayList<Object[]>();

        messages.add(new Object[]{
                         new StructuredMessage(StructuredMessage.Type.MAP),
                         "{}"});
        messages.add(new Object[]{
                         new StructuredMessage(StructuredMessage.Type.MAP).put("",new StructuredMessage("")),
                         "{\"\":\"\"}"});
        messages.add(new Object[]{
                         new StructuredMessage(StructuredMessage.Type.MAP)
                                 .put("Hello", new StructuredMessage("world")),
                         "{\"Hello\":\"world\"}"});
        messages.add(new Object[]{
                         new StructuredMessage(StructuredMessage.Type.MAP)
                                 .put("One", new StructuredMessage(1)),
                         "{\"One\":1}"});
        messages.add(new Object[]{
                         new StructuredMessage(StructuredMessage.Type.MAP)
                                 .put("One", new StructuredMessage(1))
                                 .put("Two", new StructuredMessage(2))
                                 .put("Three", new StructuredMessage(3)),
                         "{\"Three\":3,\"One\":1,\"Two\":2}"});
        messages.add(new Object[]{
                         new StructuredMessage(StructuredMessage.Type.MAP)
                                 .put("map", new StructuredMessage(StructuredMessage.Type.MAP))
                                 .put("bool", new StructuredMessage(false)),
                         "{\"map\":{},\"bool\":false}"});
        return messages.iterator();
    }

    @Test(dataProvider="map")
    public void cmpMapMsg2JS(StructuredMessage msg, String json) throws IOException {
        // FIXME: These comparisons depend on the internal Hash impl of Java
        Reader r = new JSonMessageReader(msg);
        assertEquals(readFully(r), json);
    }

    public static String readFully(Reader r) throws IOException {
        StringBuilder s = new StringBuilder();

        int c;
        while ((c = r.read()) != -1) {
            s.append((char)c);
        }

        return s.toString();
    }

}
