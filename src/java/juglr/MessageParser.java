package juglr;

import java.io.Reader;
import java.io.File;
import java.io.IOException;

/**
 * Interface for parsers cabable of parsing {@link Box} objects
 * from character streams.
 */
public interface MessageParser {

    public Box parse(String in);

    public Box parse(Reader in) throws IOException;

    public Box parse(File in) throws IOException;
}
