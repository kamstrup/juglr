package juglr;

import org.testng.annotations.DataProvider;

import java.util.*;

import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import static org.testng.Assert.*;

import java.util.Iterator;

/**
 * Test cases for the Message class
 */
public class StructuredMessageTest {

    @DataProvider(name="ints")
    public Iterator<Object[]> createInts() {
        List<Object[]> ints = new ArrayList<Object[]>();

        ints.add(new Object[]{-1});
        ints.add(new Object[]{0});
        ints.add(new Object[]{1});
        ints.add(new Object[]{Integer.MAX_VALUE});
        ints.add(new Object[]{Integer.MIN_VALUE});

        return ints.iterator();
    }

    @DataProvider(name="longs")
    public Iterator<Object[]> createLongs() {
        List<Object[]> ints = new ArrayList<Object[]>();

        ints.add(new Object[]{-1L});
        ints.add(new Object[]{0L});
        ints.add(new Object[]{1L});
        ints.add(new Object[]{Long.MAX_VALUE});
        ints.add(new Object[]{Long.MIN_VALUE});

        return ints.iterator();
    }

    @DataProvider(name="strings")
    public Iterator<Object[]> createStrings() {
        List<Object[]> ints = new ArrayList<Object[]>();

        ints.add(new Object[]{""});
        ints.add(new Object[]{"\n"});
        ints.add(new Object[]{" "});
        ints.add(new Object[]{"hello world"});
        ints.add(new Object[]{"æøå"});

        return ints.iterator();
    }

    @Test(dataProvider="ints")
    public void intTypes1 (int val) {
        StructuredMessage m = new StructuredMessage(val);
        assert m.getType() == StructuredMessage.Type.INT;
        assert m.getLong() == val;
    }

    @Test(dataProvider="longs")
    public void intTypes2 (long val) {
        StructuredMessage m = new StructuredMessage(val);
        assert m.getType() == StructuredMessage.Type.INT;
        assert m.getLong() == val;
    }

    @Test(dataProvider="strings")
    public void stringTypes1 (String val) {
        StructuredMessage m = new StructuredMessage(val);
        assert m.getType() == StructuredMessage.Type.STRING;
        assert m.getString().equals(val);
    }

    @Test
    public void stringConversion() {
        assertEquals(new StructuredMessage("foo").toString(), "\"foo\"");
        assertEquals(new StructuredMessage("").toString(), "\"\"");
        assertEquals(new StructuredMessage("# foo --* ").toString(), "\"# foo --* \"");

        assertEquals(new StructuredMessage(1).toString(), "1");
        assertEquals(new StructuredMessage(-1).toString(), "-1");
        assertEquals(new StructuredMessage(0).toString(), "0");

        assertEquals(new StructuredMessage(1L).toString(), "1");
        assertEquals(new StructuredMessage(-1L).toString(), "-1");
        assertEquals(new StructuredMessage(0L).toString(), "0");

        assertEquals(new StructuredMessage(1.1D).toString(), "1.1");
        assertEquals(new StructuredMessage(0D).toString(), "0.0");

        assertEquals(StructuredMessage.newList().toString(), "[]");
        assertEquals(StructuredMessage.newList()
                                      .add(new StructuredMessage(1))
                                      .toString(), "[1]");
        assertEquals(StructuredMessage.newList()
                                      .add(new StructuredMessage(1))
                                      .add(new StructuredMessage("foo"))
                                      .toString(), "[1,\"foo\"]");

        assertEquals(StructuredMessage.newMap().toString(), "{}");
        assertEquals(StructuredMessage.newMap()
                                      .put("one", new StructuredMessage(1))
                                      .toString(), "{\"one\":1}");
        assertEquals(StructuredMessage.newMap()
                                      .put("one", new StructuredMessage(1))
                                      .put("bar", new StructuredMessage("foo"))
                                      .toString(), "{\"one\":1,\"bar\":\"foo\"}");
    }
}
