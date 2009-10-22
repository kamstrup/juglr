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
public class MessageParserTest {

    @DataProvider(name="json")
    public Iterator<Object[]> flatJSon() {
        List<Object[]> data = new ArrayList<Object[]>();

        data.add(new Object[]{
                "{}",
                StructuredMessage.newMap()
        });
        data.add(new Object[]{
                "{\"One\":1}",
                StructuredMessage.newMap().put("One", new StructuredMessage(1))
        });
        data.add(new Object[]{
                "{\"One\":1, \"bad\":false}",
                StructuredMessage.newMap()
                        .put("One", new StructuredMessage(1))
                        .put("bad", new StructuredMessage(false))
        });
        data.add(new Object[]{
                "{\"bad\":false, \"list\":[27,68,true]}",
                StructuredMessage.newMap()
                        .put("bad", new StructuredMessage(false))
                        .put("list", StructuredMessage.newList()
                                              .add(new StructuredMessage(27))
                                              .add(new StructuredMessage(68))
                                              .add(new StructuredMessage(true)))
        });

        return data.iterator();
    }

    @Test(dataProvider="json")
    public void parseJSon(String json, Message expected) {
        MessageParser parser = new JSonMessageParser();

        StructuredMessage result = parser.parse(json);
        assertEquals(result,expected);

    }
}
