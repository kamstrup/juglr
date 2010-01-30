package juglr.net;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Constants used for the HTTP implementation
 */
public class HTTP {

    /**
     * Enumeration of HTTP methods
     */
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

    /**
     * HTTP protocol version
     */
    public enum Version {
        ONE_ZERO,
        ONE_ONE,
        UNKNOWN,
        ERROR;

        /**
         * Parse a HTTP protocol version declaration as used in the HTTP
         * protocol. The buffer will be positioned just after the last parsed
         * character upon method return
         * @param buf the buffer to parse from
         * @return the HTTP version or {@link #ERROR} on errors
         */
        public static Version read(ByteBuffer buf) {
            if (buf.get() == 'H' &&
                    buf.get() == 'T' &&
                    buf.get() == 'T' &&
                    buf.get() == 'P' &&
                    buf.get() == '/' &&
                    buf.get() == '1' &&
                    buf.get() == '.')
                if (buf.get() == '0' &&
                        buf.get() == '\r' &&
                        buf.get() == '\n') {
                    return Version.ONE_ZERO;
                } else if (buf.get() == '1' &&
                        buf.get() == '\r' &&
                        buf.get() == '\n') {
                    return Version.ONE_ONE;
                } else {
                    return Version.UNKNOWN;
                }
            return Version.ERROR;
        }
    }

    /**
     * HTTP Status Codes - see
     * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">RFC 2616</a>.
     * This is an incomplete list but it covers the most used status codes.
     */
    public enum Status {
        OK,
        Created,
        Accepted,
        NoContent,
        Found,
        BadRequest,
        Unauthorized,
        Forbidden,
        NotFound,
        MethodNotAllowed,
        NotAcceptable,
        RequestTimeout,
        Conflict,
        InternalError;

        /**
         * Return the numrical HTTP status code for a {@code Status} instance.
         * The code
         * {@code fromHttpOrdinal(status.httpOrdinal()) == status} is guaranteed
         * to evaluate to {@code true}.
         * @return the numrical HTTP status code for the instance
         */
        public int httpOrdinal() {
            switch (this) {
                case OK:
                    return 200;
                case Created:
                    return 201;
                case Accepted:
                    return 202;
                case NoContent:
                    return 204;
                case Found:
                    return 302;
                case BadRequest:
                    return 400;
                case Unauthorized:
                    return 401;
                case Forbidden:
                    return 403;
                case NotFound:
                    return 404;
                case MethodNotAllowed:
                    return 405;
                case NotAcceptable:
                    return 406;
                case RequestTimeout:
                    return 408;
                case Conflict:
                    return 409;
                case InternalError:
                    return 500;
                default:
                    return 500;
            }
        }

        /**
         * Return a {@code Status} instance matching a given numrical HTTP
         * status code. The code
         * {@code fromHttpOrdinal(status.httpOrdinal()) == status} is guaranteed
         * to evaluate to {@code true}.
         * @param ord the numerical status code
         * @return the {@code Status} instance matching {@code ord}
         */
        public static Status fromHttpOrdinal(int ord) {
            switch (ord) {
                case 200:
                    return OK;
                case 201:
                    return Created;
                case 202:
                    return Accepted;
                case 204:
                    return NoContent;
                case 302:
                    return Found;
                case 400:
                    return BadRequest;
                case 401:
                    return Unauthorized;
                case 403:
                    return Forbidden;
                case 404:
                    return NotFound;
                case 405:
                    return MethodNotAllowed;
                case 406:
                    return NotAcceptable;
                case 408:
                    return RequestTimeout;
                case 409:
                    return Conflict;
                case 500:
                    return InternalError;
                default:
                    return InternalError;
            }
        }

        public static Status read(ByteBuffer buf) {
            if (buf.remaining() < 3) {
                throw new BufferUnderflowException();
            }

            int d0 = buf.get() - 48;
            int d1 = buf.get() - 48;
            int d2 = buf.get() - 48;

            switch (d0) {
                case 2:
                    if (d1 == 0) {
                        switch (d2) {
                            case 0: return OK;
                            case 1: return Created;
                            case 2: return Accepted;
                            case 4: return NoContent;
                        }
                    }
                    throw new UnsupportedStatusException(d0, d1, d2);
                case 3:
                    if (d1 == 0 && d2 == 2) return Found;
                    throw new UnsupportedStatusException(d0, d1, d2);
                case 4:
                    if (d1 == 0) {
                        switch (d2) {
                            case 0: return BadRequest;
                            case 1: return Unauthorized;
                            case 3: return Forbidden;
                            case 4: return NotFound;
                            case 5: return MethodNotAllowed;
                            case 6: return NotAcceptable;
                            case 8: return RequestTimeout;
                            case 9: return Conflict;
                        }
                    }
                    throw new UnsupportedStatusException(d0, d1, d2);
                case 5:
                    if (d1 == 0 && d2 == 0) return InternalError;
                    throw new UnsupportedStatusException(d0, d1, d2);
                default:
                    throw new UnsupportedStatusException(d0, d1, d2);

            }
        }


    }

    public static class UnsupportedStatusException extends RuntimeException {
        public UnsupportedStatusException(int d0, int d1, int d2) {
            super(String.format("HTTP status code %s%s%s", d0, d1, d2));
        }

    }

}
