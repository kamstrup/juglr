package juglr.net;

import juglr.Box;
import juglr.Message;

/**
 * Send as a reply to a {@link HTTPRequest}. The recipient of the reply should
 * be {@code httpRequest.getReplyTo()}.
 *
 * @author Mikkel Kamstrup Erlandsen <mailto:mke@statsbiblioteket.dk>
 * @since Feb 17, 2010
 */
public class HTTPResponse extends Message {

    private HTTP.Status status;
    private Box body;

    public HTTPResponse(HTTP.Status status, Box body) {
        this.status = status;
        this.body = body;
    }

    public HTTP.Status getStatus() {
        return status;
    }

    public Box getBody() {
        return body;
    }

}
