package juglr;

/**
 * Generic exception raised when someone is trying to parse a {@link Message}
 * from a stream that contains syntax errors.
 */
public class MessageFormatException extends RuntimeException {

    public MessageFormatException(String msg) {
        super(msg);
    }

    public MessageFormatException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
