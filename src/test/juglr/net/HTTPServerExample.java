package juglr.net;

import juglr.*;

import static juglr.net.HTTP.*;
import static juglr.StructuredMessage.Type;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.channels.SocketChannel;

/**
 * A simple example that uses a HTTPMessageBus to expose a service that
 * calculates whether or not a given number is a prime.
 * <p/>
 * JSON messages should be <code>POST</code>ed to
 * <a href="http://localhost:4567/actor/calc">http://localhost:4567/calc</a>
 * looking like:
 * <pre>
 * {
 *    "isPrime" : 123
 * }
 * </pre>
 *
 * To invoke the 'list' or 'ping' handlers you can point your browser at
 * <a href="http://localhost:4567/list">http://localhost:4567/list</a> or
 * <a href="http://localhost:4567/ping/calc">http://localhost:4567/ping/calc</a>.
 * Or simply use a command line tool like <code>wget</code> to send
 * <code>GET</code> requests to those URLs.
 */
public class HTTPServerExample {

    static class CalcActor extends Actor {

        public void start() {
            try {
                getBus().allocateNamedAddress(this, "calc");
            } catch (AddressAlreadyOwnedException e) {
                e.printStackTrace();
            }
        }

        public void react(Message msg) {
            if (!(msg instanceof StructuredMessage)) {
                throw new MessageFormatException("Expected StructuredMessage");
            }

            StructuredMessage resp = new StructuredMessage(Type.MAP);
            StructuredMessage json = (StructuredMessage)msg;
            if (!json.has("isPrime")) {
                resp.put("error", "No 'isPrime' key in request");
                send(resp, msg.getSender());
                return;
            }
            String test = json.get("isPrime").toString();

            try {
                BigInteger bigInt = new BigInteger(test);

                // We guess right with 0.9990234375 probab
                if (bigInt.isProbablePrime(10)) {
                    resp.put("response", "true");
                } else {
                    resp.put("response", "false");
                }
                send(resp, msg.getSender());
            } catch (NumberFormatException e) {
                resp.put("error", "Not a valid integer");
                send(resp, msg.getSender());
                return;
            }
        }
    }

    public static void main (String[] args) throws Exception {
        System.setProperty("juglr.busclass", "juglr.net.HTTPMessageBus");
        Actor actor = new CalcActor();
        MessageBus.getDefault().start(actor.getAddress());

        synchronized (actor) {
            actor.wait(); // Indefinte non-busy block
        }
    }

}
