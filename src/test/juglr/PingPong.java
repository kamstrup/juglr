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
        public void react(Message msg) {
            pings++;
            System.out.println(
                    "[" + getAddress() + "] Got ping from " + msg.getSender());
            send(new Message(), msg.getSender());
        }
    }

    public static void main(String[] args) {
        Pong pong = new Pong();

        Actor[] pings = new Actor[10];
        for (int i = 0; i < pings.length; i++) {
            pings[i] = new Ping(pong.getAddress());
        }

        pong.start();
        for (Actor ping : pings) {
            ping.start();
        }
    }
}
