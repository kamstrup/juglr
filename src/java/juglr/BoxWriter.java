package juglr;

import java.io.IOException;

/**
 * FIXME: Missing class docs for juglr.BoxWriter
 *
 * @author Mikkel Kamstrup Erlandsen <mailto:mke@statsbiblioteket.dk>
 * @since Feb 17, 2010
 */
public interface BoxWriter {

    public BoxWriter write(Box box, Appendable out) throws IOException;

}
