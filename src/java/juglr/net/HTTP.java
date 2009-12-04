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

    /**
     * HTTP Status Codes - see
     * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">RFC 2616</a> 
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
    }

}
