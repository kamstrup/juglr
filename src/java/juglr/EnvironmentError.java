package juglr;

/**
 * Fatal error for the Juglr runtime. The Juglr components, message bus, actors,
 * etc. can not be expected to work in a predictable manner.
 * <p/>
 * If the receiving application relies on Juglr for its core tasks it is
 * advised that the application shut down the JVM.
 * <p/>
 */
public class EnvironmentError extends Error {

    public EnvironmentError(String msg) {
        super(msg);
    }

    public EnvironmentError(String msg, Throwable cause) {
        super(msg, cause);
    }

    public EnvironmentError(Throwable cause) {
        super(cause);
    }
}
