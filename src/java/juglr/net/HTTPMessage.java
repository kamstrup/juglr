package juglr.net;

import juglr.Box;
import juglr.Message;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * FIXME: Missing class docs for juglr.net.HTTPMessage
 *
 * @author Mikkel Kamstrup Erlandsen <mailto:mke@statsbiblioteket.dk>
 * @since Feb 16, 2010
 */
public class HTTPMessage extends Message {

    private CharSequence uri;
    private HTTP.Method method;
    private Box body;

    public HTTPMessage(CharSequence requestUri, HTTP.Method method, Box body)
                                                     throws URISyntaxException {
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
