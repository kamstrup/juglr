package juglr.net;

import juglr.Box;
import juglr.Message;

/**
 * Send by a {@link HTTPServer} to one of its bottom half actors according
 * to the rules created via {@link HTTPServer#registerHandler}.
 *
 * @author Mikkel Kamstrup Erlandsen <mailto:mke@statsbiblioteket.dk>
 * @since Feb 16, 2010
 */
public class HTTPRequest extends Message {

    private CharSequence uri;
    private HTTP.Method method;
    private Box body;

    public HTTPRequest(CharSequence requestUri, HTTP.Method method, Box body) {
        this.uri = requestUri;
        this.method = method;
        this.body = body;
    }

    public HTTP.Method getMethod() {
        return method;
    }

    public CharSequence getUri() {
        return uri;
    }

    public Box getBody() {
        return body;
    }


}
