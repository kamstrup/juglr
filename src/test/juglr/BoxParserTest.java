package juglr;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.*;

/**
 *
 */
public class BoxParserTest {

    @DataProvider(name="objects")
    public Iterator<Object[]> jsonDicts() {
        List<Object[]> data = new ArrayList<Object[]>();

        data.add(new Object[]{
                "{}",
                Box.newMap()
        });
        data.add(new Object[]{
                "{\"One\":1}",
                Box.newMap().put("One", new Box(1))
        });
        data.add(new Object[]{
                "{\"One\":1, \"bad\":false}",
                Box.newMap()
                        .put("One", new Box(1))
                        .put("bad", new Box(false))
        });
        data.add(new Object[]{
                "{\"bad\":false, \"list\":[27,68,true]}",
                Box.newMap()
                        .put("bad", new Box(false))
                        .put("list", Box.newList()
                                              .add(new Box(27))
                                              .add(new Box(68))
                                              .add(new Box(true)))
        });
        data.add(new Object[]{
                "{\"__store__\": [{\"__index__\": [\"firstname\"], \"__base__\": \"mybase\", \"__id__\": \"mke\", \"firstname\": \"Mikkel\", \"lastname\": \"kamstrup\"}]}",
                Box.newMap()
                    .put("__store__", Box.newList()
                        .add(Box.newMap()
                            .put("__index__", Box.newList().add("firstname"))
                            .put("__base__", "mybase")
                            .put("__id__", "mke")
                            .put("firstname", "Mikkel")
                            .put("lastname", "kamstrup"))
                     )
        });

        return data.iterator();
    }

    @DataProvider(name="arrays")
    public Iterator<Object[]> jsonArrays() {
        List<Object[]> data = new ArrayList<Object[]>();

        data.add(new Object[]{
                "[]",
                Box.newList()
        });
        data.add(new Object[]{
                "[1]",
                Box.newList().add(1)
        });
        data.add(new Object[]{
                "[1, 2]",
                Box.newList()
                        .add(1)
                        .add(2)
        });
        data.add(new Object[]{
                "[1, [2,3]]",
                Box.newList()
                        .add(1)
                        .add(Box.newList().add(2).add(3))
        });

        return data.iterator();
    }

    @Test(dataProvider="objects")
    public void parseDicts(String json, Message expected) {
        BoxParser parser = new JSonBoxParser();

        Box result = parser.parse(json);
        assertEquals(result,expected);
    }

    @Test(dataProvider="arrays")
    public void parseArrays(String json, Message expected) {
        BoxParser parser = new JSonBoxParser();

        Box result = parser.parse(json);
        assertEquals(result,expected);
    }

    @Test
    public void threadSafety() throws Exception {
        final String json = "{\"__store__\": [{\"__index__\": [\"firstname\"], \"__base__\": \"mybase\", \"__id__\": \"mke\", \"firstname\": \"Mikkel\", \"lastname\": \"kamstrup\"}]}";
        final BoxParser parser = new JSonBoxParser();
        final List<Throwable> errors =
                Collections.synchronizedList(new LinkedList<Throwable>());
        Thread[] threads = new Thread[20];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    try {
                        for (int i = 0; i < 10; i++) {
                            parser.parse(json);
                        }
                    } catch (Throwable t) {
                        errors.add(t);
                    }
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        if (errors.size() != 0) {
            for (Throwable t : errors) {
                t.printStackTrace();
            }
            fail();
        }
    }
}
