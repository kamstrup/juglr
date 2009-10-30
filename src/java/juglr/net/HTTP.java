package juglr.net;

/**
 *
 */
public class HTTP {

    public enum Method {
        GET,
        POST,
        PUT,
        HEAD,
        DELETE,
        TRACE,
        CONNECT,
        UNKNOWN,
        ERROR
    }

    public enum Version {
        ONE_ZERO,
        ONE_ONE,
        UNKNOWN,
        ERROR
    }

}
