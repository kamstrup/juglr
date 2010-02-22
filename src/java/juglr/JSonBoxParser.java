package juglr;

import juglr.internal.org.json.simple.parser.ContainerFactory;
import juglr.internal.org.json.simple.parser.JSONParser;
import juglr.internal.org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;

/**
 * Helper class for parsing strings, readers, or files into {@link Box}
 * instances. Note that all methods on this class are thread safe.
 *
 * @see BoxReader
 * @see JSonBoxReader
 * @see Box
 */
public class JSonBoxParser implements BoxParser {    

    /**
     * Parse a JSON formatted string into a {@code Box}
     * @param in the string to parse
     * @return returns a {@link Box} representing the JSON document, or
     *         {@code null} in case the string is empty
     */
    public Box parse (String in) {
        try {
            return parse(new StringReader(in));
        } catch (IOException e) {
            // This should never happen!
            throw new RuntimeException("Unexpected error reading string '"
                                       + in +"': " + e.getMessage(), e);
        }
    }

    /**
     * Parse a JSON formatted stream into a {@code Box}
     * @param in the stream to parse
     * @return returns a {@link Box} representing the JSON document, or
     *         {@code null} in case the stream is empty
     */
    public Box parse (Reader in) throws IOException {
         try {
            return realParse(in);
        } catch (ParseException e) {
            throw new MessageFormatException("Syntax error reading JSON data "
                                             + "from stream:"
                                             + e.getMessage(), e);
        }
    }

    /**
     * Parse a JSON formatted file into a {@code Box}
     * @param jsonFile the file to parse
     * @return returns a {@link Box} representing the JSON document, or
     *         {@code null} in case the file is empty
     */
    public Box parse (File jsonFile) throws IOException {
        Reader r = new FileReader(jsonFile);
        try {
            return realParse(r);
        } catch (ParseException e) {
            throw new MessageFormatException("Syntax error reading JSON data "
                                             + "from " + jsonFile + ": "
                                             + e.getMessage(), e);
        }
    }

    /**
     * Parse a JSON formatted stream into a {@code Box}
     * @param in the stream to parse
     * @return returns a {@link Box} representing the JSOn document, or
     *         {@code null} in case the stream is empty
     */
    public Box parse(InputStream in) throws IOException {
        return parse(new InputStreamReader(in));
    }

    private Box realParse(Reader in) throws ParseException, IOException {
        JSONParser parser = new JSONParser();
        Object o = parser.parse(in);//new SimpleJSONContainerFactory());
        return Box.parseObject(o);
    }    
}
