package juglr;

import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

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

}
