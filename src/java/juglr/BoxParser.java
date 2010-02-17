package juglr;

import java.io.InputStream;
import java.io.Reader;
import java.io.File;
import java.io.IOException;

/**
 * Interface for parsers cabable of parsing {@link Box} objects
 * from character streams. Note that all implementations of this interface
 * should be thread safe.
 *
 * @see Box
 * @see BoxReader
 * @see JSonBoxParser
 */
public interface BoxParser {

    public Box parse(String in);

    public Box parse(Reader in) throws IOException;

    public Box parse(File in) throws IOException;

    public Box parse(InputStream in) throws IOException;
}
