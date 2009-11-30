package juglr;

import java.io.Reader;
import java.io.File;
import java.io.IOException;

/**
 * Interface for parsers cabable of parsing {@link StructuredMessage} objects
 * from character streams.
 */
public interface MessageParser {

    public StructuredMessage parse(String in);

    public StructuredMessage parse(Reader in) throws IOException;

    public StructuredMessage parse(File in) throws IOException;
}
