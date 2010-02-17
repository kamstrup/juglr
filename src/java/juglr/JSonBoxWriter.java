package juglr;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Map;

/**
 * FIXME: Missing class docs for juglr.JSonBoxWriter
 *
 * @author Mikkel Kamstrup Erlandsen <mailto:mke@statsbiblioteket.dk>
 * @since Feb 17, 2010
 */
public class JSonBoxWriter implements BoxWriter {
    
    public BoxWriter write(Box box, Appendable out) throws IOException {
        switch (box.getType()) {
            case INT:
                out.append(Long.toString(box.getLong()));
                break;
            case BOOLEAN:
                out.append(Boolean.toString(box.getBoolean()));
                break;
            case FLOAT:
                out.append(Double.toString(box.getFloat()));
                break;
            case LIST:
                out.append('[');
                boolean first = true;
                for (Box child : box.getList()) {
                    if (first) {
                        first = false;
                    } else {
                        out.append(',');
                    }
                    write(child, out);
                }
                out.append(']');
                break;
            case MAP:
                out.append('{');
                first = true;
                for (Map.Entry<String, Box> entry : box.getMap().entrySet()) {
                    if (first) {
                        first = false;
                    } else {
                        out.append(',');
                    }
                    out.append('"');
                    out.append(entry.getKey());
                    out.append("\":");
                    write(entry.getValue(), out);
                }
                out.append('}');
                break;
            case STRING:
                out.append('"');
                out.append(box.getString());
                out.append('"');
                break;
        }
        return this;
    }
}
