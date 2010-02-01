package juglr;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 *
 */
public class BoxParserTest {

    @DataProvider(name="json")
    public Iterator<Object[]> flatJSon() {
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

    @Test(dataProvider="json")
    public void parseJSon(String json, Message expected) {
        BoxParser parser = new JSonBoxParser();

        Box result = parser.parse(json);
        assertEquals(result,expected);

    }
}
