package juglr;

import java.io.IOException;
import java.util.Map;

/**
 * Serialize a {@link Message} object to JSON. To save system resources you can
 * resuse the same JSonMessageReader by calling
 * {@link #reset(StructuredMessage)} when starting the serialization of a
 * new message.
 */
public class JSonMessageReader extends MessageReader {

    private StructuredMessage next;
    private StringBuilder buf;
    private int cursor;

    public JSonMessageReader(StructuredMessage msg) {
        super(msg);
        buf = new StringBuilder();
        reset(msg);
    }

    @Override
    public void reset(StructuredMessage msg) {
        if (msg == null) {
            throw new NullPointerException();
        }

        this.msg = msg;
        next = null;
        cursor = 0;
        buf.setLength(0);

        appendStructuredMessage(msg);
    }

    public String asString () {
        return buf.toString();
    }

    @Override
    public int read(char[] chars, int offset, int count) throws IOException {
        int len = buf.length();
        int start = cursor;

        while(cursor < len
              && cursor-start < count
              && cursor-start+offset < chars.length) {

            chars[cursor-start+offset] = buf.charAt(cursor);
            cursor++;
        }

        if (start == cursor) {
            return -1;
        }

        return cursor - start;
    }

    @Override
    public int read() {
        if (cursor == buf.length()) {
            return -1;
        }

        char next = buf.charAt(cursor);
        cursor++;
        return (int)next;
    }

    @Override
    public void close() throws IOException {
        // Allow to free the memory of the CharSequence
        buf = null;
    }

    private void appendStructuredMessage(StructuredMessage m) {
        switch (m.getType()) {
            case INT:
                buf.append(m.getLong());
                break;
            case BOOLEAN:
                buf.append(m.getBoolean());
                break;
            case FLOAT:
                buf.append(m.getFloat());
                break;
            case LIST:
                buf.append('[');
                boolean first = true;
                for (StructuredMessage child : m.getList()) {
                    if (first) {
                        first = false;
                    } else {
                        buf.append(',');
                    }
                    appendStructuredMessage(child);
                }
                buf.append(']');
                break;
            case MAP:
                buf.append('{');
                first = true;
                for (Map.Entry<String,StructuredMessage> entry : m.getMap().entrySet()) {
                    if (first) {
                        first = false;
                    } else {
                        buf.append(',');
                    }
                    buf.append('"');
                    buf.append(entry.getKey());
                    buf.append("\":");
                    appendStructuredMessage(entry.getValue());
                }
                buf.append('}');
                break;
            case STRING:
                buf.append('"');
                buf.append(m.getString());
                buf.append('"');
                break;
        }
    }
}
