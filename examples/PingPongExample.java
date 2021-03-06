/**
 * A simple example that sends a massive number of pings to a pong server
 * that is backed by three identical actors.
 * <p/>
 * Compile with:
 *     javac -Xbootclasspath/p:../lib/jsr166.jar -classpath ../juglr-0.3.2.jar PingPongExample.java
 *
 * Run with:
 *     java -Xbootclasspath/p:../lib/jsr166.jar -classpath ../juglr-0.3.2.jar:. PingPongExample
 */

import juglr.*;

public class PingPongExample {

    static class Ping extends Actor {

        Address pong;

        Ping(Address pong) {
            this.pong = pong;
        }

        @Override
        public void start() {
            send(new Message(), pong);
        }

        @Override
        public void react(Message msg) {
            System.out.println(
                    "[" + getAddress() + "] Got pong from " + msg.getSender());
        }
    }

    static class Pong extends Actor {

        int pings = 0;

        @Override
        public void start() {
            System.out.println("[" + this + "] Started");
        }

        @Override
        public void react(Message msg) {
            pings++;
            System.out.println(
             "[" + this + "] replying to " + msg.getReplyTo());
            send(new Message(), msg.getReplyTo());
        }
    }

    public static void main(String[] args) throws Exception {
        /* Three Pong actors handle incoming requests in a round-robin manner */
        Actor pong = new DelegatingActor(new Pong(), new Pong(), new Pong());

        Actor[] pings = new Actor[50000];
        for (int i = 0; i < pings.length; i++) {
            pings[i] = new Ping(pong.getAddress());
        }

        pong.start();
        for (Actor ping : pings) {
            ping.start();
        }

        Thread.sleep(1000000);
    }
}
