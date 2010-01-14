package juglr;

/**
 *
 */
public class PingPong {

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
            /* This impl. is the "slow and blocking approach" */
            /*System.out.println("[" + this
                  + "] Initial ping from [" + msg.getSender() + "]");
            sendReply(msg);
            while ((msg = awaitMessage()) != null) {
                sendReply(msg);
            }*/

            /* This react() impl. would be the "fast and snappy" approach */
            sendReply(msg);
        }

        private void sendReply(Message msg) {
            pings++;
            System.out.println(
             "[" + this + "] replying to " + msg.getSender());
            send(new Message(), msg.getSender());
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
