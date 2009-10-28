package juglr;

import java.util.*;

/**
 * Asynchronously forward messages to a set of delegates according to
 * a given strategy. The SwarmActor will always forward a message to
 * exactly one delegate. If you need to forward the same message to multiple
 * delegates use a {@link MulticastActor}.
 */
public class SwarmActor extends Actor {

    public static interface Strategy {

        public Address next(Message msg);

        public void start();

    }

    private static class RoundRobinStrategy implements Strategy {
        private Set<Address> delegates;
        private Iterator<Address> iter;

        private RoundRobinStrategy() {
            delegates = new HashSet<Address>();
        }

         public RoundRobinStrategy (Iterable<Address> delegates) {
            this.delegates = new HashSet<Address>();
            for (Address delegate : delegates) {
                this.delegates.add(delegate);
            }
        }

        public RoundRobinStrategy (Address... delegates) {
            this(Arrays.asList(delegates));
        }

        static RoundRobinStrategy newForActors(Iterable<Actor> delegates) {
            RoundRobinStrategy self = new RoundRobinStrategy();
            for (Actor delegate : delegates) {
                self.delegates.add(delegate.getAddress());
            }
            return self;
        }

        static RoundRobinStrategy newForActors(Actor... delegates) {
            return newForActors(Arrays.asList(delegates));
        }

        public Address next(Message msg) {
            if (iter == null || !iter.hasNext()) {
                iter = delegates.iterator();
            }

            return iter.next();
        }

        public void start() {
            for (Address delegate : delegates) {
                delegate.getBus().start(delegate);
            }
        }
    }

    private Strategy strategy;

    private SwarmActor() {

    }

    /**
     * Create a new SwarmActor forwarding messages to {@code delegates}
     * using a round-robin strategy
     * @param delegates the collection of delegates to forward messages to
     */
    public SwarmActor(Address... delegates) {
        strategy = new RoundRobinStrategy(delegates);
    }

    /**
     * Create a new SwarmActor forwarding messages to {@code delegates}
     * using a round-robin strategy
     * @param delegates the collection of delegates to forward messages to
     */
    public SwarmActor(Iterable<Address> delegates) {
        strategy = new RoundRobinStrategy(delegates);
    }

    public SwarmActor(Actor... delegates) {
        strategy = RoundRobinStrategy.newForActors(delegates);
    }

    public static SwarmActor newForActors(Iterable<Actor> delegates) {
        SwarmActor self = new SwarmActor();
        self.strategy = RoundRobinStrategy.newForActors(delegates);
        return self;
    }

    public static SwarmActor newForActors(Actor... delegates) {
        return newForActors(Arrays.asList(delegates));
    }

    /**
     * Create a new SwarmActor forwarding messages to {@code delegates}
     * selected using the given {@link Strategy}.
     * @param strategy the Strategy used for selecting the next address to
     *                 forward an incoming message to
     */
    public SwarmActor(Strategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void react(Message msg) {
        Address delegate = strategy.next(msg);

        /* Send via the bus instead of this.send()
         * to avoid rewriting the sender address */
        getBus().send(msg, delegate);
    }

    @Override
    public void start() {
        strategy.start();
    }
}
