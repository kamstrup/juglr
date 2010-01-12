package juglr;

import java.io.Reader;

/**
 * A Reader class that outputs a message as a character stream. You can save
 * system resources by invoking {@link #reset(Box)} instead of
 * creating new MessageReaders.
 */
public abstract class MessageReader extends Reader {

    /**
     * The message being serialized.
     */
    protected Box msg;

    public MessageReader (Box msg) {
        this.msg = msg;
    }

    /**
     * Prepare the reader for serializing another message.
     * @param msg the message to serialize
     */
    public abstract void reset(Box msg);

    /**
     * Read the entire message an return it as a string
     * @return string representation of the message
     */
    public abstract String asString();
}
