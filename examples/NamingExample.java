/**
 * Example class demonstrating a named actor.
 * Compile with:
 *     javac -Xbootclasspath/p:../lib/jsr166.jar -classpath ../juglr-0.0.1.jar NamingExample.java
 *
 * Run with:
 *     java -Xbootclasspath/p:../lib/jsr166.jar -classpath ../juglr-0.0.1.jar:. NamingExample
 */
import juglr.Actor;
import juglr.Address;
import juglr.AddressAlreadyOwnedException;
import juglr.Message;
import juglr.StructuredMessage;

public class NamingExample {

    private static class NamedActor extends Actor {

        public NamedActor() {
            try {
                getBus().allocateNamedAddress(this, "CarmenSandiego");
            } catch (AddressAlreadyOwnedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void react(Message msg) {
            send(new StructuredMessage("You found me!"), msg.getSender());
        }
    }

    public static class DetectiveActor extends Actor {

        @Override
        public void start() {
            Address carmen = getBus().lookup("/CarmenSandiego");
            send(new StructuredMessage("Where are you?"), carmen);
        }

        @Override
        public void react(Message msg) {
            System.out.println("Carmen said: " + msg);
        }
    }

    public static void main (String[] args) throws Exception {
        Actor carmen = new NamedActor();
        Actor detective = new DetectiveActor();

        carmen.start();
        detective.start();

        Thread.sleep(1000);
    }

}
