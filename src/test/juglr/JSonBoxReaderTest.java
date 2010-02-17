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
public class JSonBoxReaderTest {

    @DataProvider(name="simple")
    public Iterator<Object[]> createSimpleStructuredMessages() {
        List<Object[]> messages = new ArrayList<Object[]>();

        /* Strings */
        messages.add(new Object[]{
                         new Box(""),
                         "\"\""});
        messages.add(new Object[]{
                         new Box("Hello world "),
                         "\"Hello world \""});
        messages.add(new Object[]{
                         new Box("åæø"),
                         "\"åæø\""});

        /* Ints/Longs */
        messages.add(new Object[]{
                         new Box(-1),
                         "-1"});
        messages.add(new Object[]{
                         new Box(0),
                         "0"});
        messages.add(new Object[]{
                         new Box(1),
                         "1"});
        messages.add(new Object[]{
                         new Box(Integer.MAX_VALUE),
                         "" + Integer.MAX_VALUE});
        messages.add(new Object[]{
                         new Box(Integer.MIN_VALUE),
                         "" + Integer.MIN_VALUE});
        messages.add(new Object[]{
                         new Box(Long.MAX_VALUE),
                         "" + Long.MAX_VALUE});
        messages.add(new Object[]{
                         new Box(Long.MIN_VALUE),
                         "" + Long.MIN_VALUE});

        /* Booleans */
        messages.add(new Object[]{
                         new Box(true),
                         "true"});
        messages.add(new Object[]{
                         new Box(false),
                         "false"});

        /* Float/Double */
        messages.add(new Object[]{
                         new Box(0D),
                         "" + 0D});
        messages.add(new Object[]{
                         new Box(-1D),
                         "" + -1D});
        messages.add(new Object[]{
                         new Box(1D),
                         "" + 1D});

        return messages.iterator();
    }

    @Test(dataProvider="simple")
    public void cmpSimpleMsg2JS(Box msg, String json) throws IOException {
        Reader r = new JSonBoxReader(msg);
        assertEquals(readFully(r), json);
    }

    @DataProvider(name="list")
    public Iterator<Object[]> createListmessages() {
        List<Object[]> messages = new ArrayList<Object[]>();

        messages.add(new Object[]{
                         new Box(Box.Type.LIST),
                         "[]"});
        messages.add(new Object[]{
                         new Box(Box.Type.LIST).add(new Box("")),
                         "[\"\"]"});
        messages.add(new Object[]{
                         new Box(Box.Type.LIST)
                                 .add(new Box("Hello world")),
                         "[\"Hello world\"]"});
        messages.add(new Object[]{
                         new Box(Box.Type.LIST)
                                 .add(new Box("Hello"))
                                 .add(new Box("world")),
                         "[\"Hello\",\"world\"]"});
        messages.add(new Object[]{
                         new Box(Box.Type.LIST)
                                 .add(new Box(Box.Type.LIST)),
                         "[[]]"});
        messages.add(new Object[]{
                         new Box(Box.Type.LIST)
                                 .add(new Box("One"))
                                 .add(new Box(1)),
                         "[\"One\",1]"});
        messages.add(new Object[]{
                         new Box(Box.Type.LIST)
                                 .add(new Box(true)),
                         "[true]"});
        messages.add(new Object[]{
                         new Box(Box.Type.LIST)
                                 .add(new Box(1.1D))
                                 .add(new Box(Box.Type.LIST)),
                         "["+1.1D+",[]]"});

        return messages.iterator();
    }

    @Test(dataProvider="list")
    public void cmpListMsg2JS(Box msg, String json) throws IOException {
        Reader r = new JSonBoxReader(msg);
        assertEquals(readFully(r), json);
    }

    @DataProvider(name="map")
    public Iterator<Object[]> createMapmessages() {
        List<Object[]> messages = new ArrayList<Object[]>();

        messages.add(new Object[]{
                         new Box(Box.Type.MAP),
                         "{}"});
        messages.add(new Object[]{
                         new Box(Box.Type.MAP).put("",new Box("")),
                         "{\"\":\"\"}"});
        messages.add(new Object[]{
                         new Box(Box.Type.MAP)
                                 .put("Hello", new Box("world")),
                         "{\"Hello\":\"world\"}"});
        messages.add(new Object[]{
                         new Box(Box.Type.MAP)
                                 .put("One", new Box(1)),
                         "{\"One\":1}"});
        messages.add(new Object[]{
                         new Box(Box.Type.MAP)
                                 .put("One", new Box(1))
                                 .put("Two", new Box(2))
                                 .put("Three", new Box(3)),
                         "{\"Three\":3,\"One\":1,\"Two\":2}"});
        messages.add(new Object[]{
                         new Box(Box.Type.MAP)
                                 .put("map", new Box(Box.Type.MAP))
                                 .put("bool", new Box(false)),
                         "{\"map\":{},\"bool\":false}"});
        messages.add(new Object[]{
                         Box.newMap()
                                 .put("foo", Box.newList()
                                    .add(Box.newMap().put("bar", "quiz"))
                                    .add(Box.newMap().put("bar", "quiz"))),
                         "{\"foo\":[{\"bar\":\"quiz\"},{\"bar\":\"quiz\"}]}"});
        return messages.iterator();
    }

    @Test(dataProvider="map")
    public void cmpMapMsg2JS(Box msg, String json) throws IOException {
        // FIXME: These comparisons depend on the internal Hash impl of Java
        Reader r = new JSonBoxReader(msg);
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
