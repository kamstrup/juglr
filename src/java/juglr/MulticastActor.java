package juglr;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Asynchronously forward incoming messages to a collection of delegates
 * based on a given strategy.
 */
public class MulticastActor extends Actor {

    public static interface Strategy {

        public Iterator<Address> recipients(Message msg);

        public void start();
    }

    private static class ForwardToAllStrategy implements Strategy {

        private List<Address> delegates;

        private ForwardToAllStrategy() {
            delegates = new LinkedList<Address>();
        }

        public ForwardToAllStrategy(Iterable<Address> delegates) {
            this.delegates = new LinkedList<Address>();
            for (Address delegate : delegates) {
                this.delegates.add(delegate);
            }
        }

        public ForwardToAllStrategy(Address... delegates) {
            this(Arrays.asList(delegates));
        }

        static ForwardToAllStrategy newForActors(Iterable<Actor> delegates) {
            ForwardToAllStrategy self = new ForwardToAllStrategy();
            for (Actor delegate : delegates) {
                self.delegates.add(delegate.getAddress());
            }
            return self;
        }

        static ForwardToAllStrategy newForActors(Actor... delegates) {
            return newForActors(Arrays.asList(delegates));
        }

        public Iterator<Address> recipients(Message msg) {
            return delegates.iterator();
        }

        public void start() {
            for (Address delegate : delegates) {
                delegate.getBus().start(delegate);
            }
        }
    }

    private Strategy strategy;

    private MulticastActor() {

    }

    /**
     * Create a new multicast actor forwarding all incoming messages to all
     * members of {@code delegates}.
     *
     * @param delegates the collection of delegates to forward all messages to
     */
    public MulticastActor (Address... delegates) {
        strategy = new ForwardToAllStrategy(delegates);
    }

    /**
     * Create a new multicast actor forwarding all incoming messages to all
     * members of {@code delegates}.
     *
     * @param delegates the collection of delegates to forward all messages to
     */
    public MulticastActor (Iterable<Address> delegates) {
        strategy = new ForwardToAllStrategy(delegates);
    }

    /**
     * Create a new multicast actor forwarding all incoming messages to all
     * delegates determined by a given {@link Strategy}.
     *
     * @param strategy the {@link Strategy} used to determine the message
     *                 recipients
     */
    public MulticastActor (Strategy strategy) {
        this.strategy = strategy;
    }

    public MulticastActor(Actor... delegates) {
        strategy = ForwardToAllStrategy.newForActors(delegates);
    }

    public static MulticastActor newForActors(Iterable<Actor> delegates) {
        MulticastActor self = new MulticastActor();
        self.strategy = ForwardToAllStrategy.newForActors(delegates);
        return self;
    }

    public static MulticastActor newForActors(Actor... delegates) {
        return newForActors(Arrays.asList(delegates));
    }

    @Override
    public void react(Message msg) {
        Iterator<Address> recipients = strategy.recipients(msg);
        while (recipients.hasNext()) {
            send(msg, recipients.next());
        }
    }

    @Override
    public void start() {
        strategy.start();
    }
}
