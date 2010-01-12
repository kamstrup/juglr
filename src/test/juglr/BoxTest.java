package juglr;

import org.testng.annotations.DataProvider;

import java.util.*;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

import java.util.Iterator;

/**
 * Test cases for the Message class
 */
public class BoxTest {

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
        Box m = new Box(val);
        assert m.getType() == Box.Type.INT;
        assert m.getLong() == val;
    }

    @Test(dataProvider="longs")
    public void intTypes2 (long val) {
        Box m = new Box(val);
        assert m.getType() == Box.Type.INT;
        assert m.getLong() == val;
    }

    @Test(dataProvider="strings")
    public void stringTypes1 (String val) {
        Box m = new Box(val);
        assert m.getType() == Box.Type.STRING;
        assert m.getString().equals(val);
    }

    @Test
    public void stringConversion() {
        assertEquals(new Box("foo").toString(), "\"foo\"");
        assertEquals(new Box("").toString(), "\"\"");
        assertEquals(new Box("# foo --* ").toString(), "\"# foo --* \"");

        assertEquals(new Box(1).toString(), "1");
        assertEquals(new Box(-1).toString(), "-1");
        assertEquals(new Box(0).toString(), "0");

        assertEquals(new Box(1L).toString(), "1");
        assertEquals(new Box(-1L).toString(), "-1");
        assertEquals(new Box(0L).toString(), "0");

        assertEquals(new Box(1.1D).toString(), "1.1");
        assertEquals(new Box(0D).toString(), "0.0");

        assertEquals(Box.newList().toString(), "[]");
        assertEquals(Box.newList()
                                      .add(new Box(1))
                                      .toString(), "[1]");
        assertEquals(Box.newList()
                                      .add(new Box(1))
                                      .add(new Box("foo"))
                                      .toString(), "[1,\"foo\"]");

        assertEquals(Box.newMap().toString(), "{}");
        assertEquals(Box.newMap()
                                      .put("one", new Box(1))
                                      .toString(), "{\"one\":1}");
        assertEquals(Box.newMap()
                                      .put("one", new Box(1))
                                      .put("bar", new Box("foo"))
                                      .toString(), "{\"one\":1,\"bar\":\"foo\"}");
    }
}
