/**
 * A simple example that uses a HTTPMessageBus to expose a service that
 * calculates whether or not a given number is a prime. Workload is spead
 * over three parallel calculators.
 * <p/>
 * Compile with:
 *     javac -Xbootclasspath/p:../lib/jsr166.jar -classpath ../juglr-0.3.0.jar HTTPServerExample.java
 *
 * Run with:
 *     java -Xbootclasspath/p:../lib/jsr166.jar -classpath ../juglr-0.3.0.jar:. HTTPServerExample
 * <p/>
 * JSON messages should be send to
 * <a href="http://localhost:4567/calc">http://localhost:4567/calc</a>
 * looking like:
 * <pre>
 * {
 *    "isPrime" : 123
 * }
 * </pre>
 *
 * To send an isPrime request using <code>curl</code> use the following command
 * in a Unix terminal:
 * <pre>
 *     curl -XGET http://localhost:4567/calc --data '{ "isPrime" : 982451653 }'
 * </pre>
 */

import juglr.*;
import juglr.net.HTTP;
import juglr.net.HTTPRequest;
import juglr.net.HTTPResponse;
import juglr.net.HTTPServer;

import java.math.BigInteger;

public class HTTPServerExample {

    static class CalcActor extends Actor {

        public void react(Message msg) {
            if (!(msg instanceof HTTPRequest)) {
                throw new MessageFormatException("Expected HTTPRequest");
            }

            Box resp = Box.newMap();
            Box box = ((HTTPRequest)msg).getBody();
            if (!box.has("isPrime")) {
                // We need to set an error state, so we have to wrap the
                // response to the upper half in a HTTPResponse
                resp.put("error", "No 'isPrime' key in request");
                send(new HTTPResponse(HTTP.Status.BadRequest, resp),
                     msg.getReplyTo());
                return;
            }

            Box test = box.get("isPrime");
            try {
                BigInteger bigInt = new BigInteger(test.toString());
                // We guess right with 0.9990234375 probability
                if (bigInt.isProbablePrime(10)) {
                    resp.put("response", "true");
                } else {
                    resp.put("response", "false");
                }

                // Sending a plain old Box back to the upper half
                // results in a 200 OK
                send(resp, msg.getReplyTo());
            } catch (NumberFormatException e) {
                resp.put("error", "Not a valid integer: " + test.toString());
                send(new HTTPResponse(HTTP.Status.BadRequest, resp),
                     msg.getReplyTo());
            }
        }
    }

    public static void main (String[] args) throws Exception {
        // Delegate work to three CalcActors in a round-robin manner
        Actor actor = new DelegatingActor(
                new CalcActor(), new CalcActor(), new CalcActor());
        MessageBus.getDefault().allocateNamedAddress(actor, "/calc");
        MessageBus.getDefault().start(actor.getAddress());

        HTTPServer server = new HTTPServer(4567);
        server.registerHandler("^/calc/?$", actor.getAddress(), HTTP.Method.GET);
        server.start();

        // Indefinite non-busy block
        synchronized (actor) {
            actor.wait();
        }
    }

}
