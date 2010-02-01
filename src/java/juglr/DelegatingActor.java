package juglr;

import java.util.*;

/**
 * Asynchronously forward messages to a delegate according to
 * a given strategy. The DelegatingActor will always forward a message to
 * exactly one delegate. If you need to forward the same message to multiple
 * delegates use a {@link MulticastActor}.
 *
 * @see MulticastActor
 */
public class DelegatingActor extends Actor {

    public static interface Strategy {

        /**
         * Select the recipient for {@code msg}
         * @param msg the message to look up a recipient for
         * @return the address to send {@code msg} to. If {@code null}
         *         is returned the message will be silently dropped
         */
        public Address recipient(Message msg);

        /**
         * Make sure actors in this strategy have their {@link Actor#start}
         * methods invoked. Note that it is the responsibility of the strategy
         * to start any actors that are created after this method has been
         * invoked
         */
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

        public Address recipient(Message msg) {
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

    protected Strategy strategy;

    private DelegatingActor() {

    }

    /**
     * Create a new DelegatingActor forwarding messages to {@code delegates}
     * using a round-robin strategy
     * @param delegates the collection of delegates to forward messages to
     */
    public DelegatingActor(Address... delegates) {
        strategy = new RoundRobinStrategy(delegates);
    }

    /**
     * Create a new DelegatingActor forwarding messages to {@code delegates}
     * using a round-robin strategy
     * @param delegates the collection of delegates to forward messages to
     */
    public DelegatingActor(Iterable<Address> delegates) {
        strategy = new RoundRobinStrategy(delegates);
    }

    public DelegatingActor(Actor... delegates) {
        strategy = RoundRobinStrategy.newForActors(delegates);
    }

    public static DelegatingActor newForActors(Iterable<Actor> delegates) {
        DelegatingActor self = new DelegatingActor();
        self.strategy = RoundRobinStrategy.newForActors(delegates);
        return self;
    }

    public static DelegatingActor newForActors(Actor... delegates) {
        return newForActors(Arrays.asList(delegates));
    }

    /**
     * Create a new DelegatingActor forwarding messages to {@code delegates}
     * selected using the given {@link Strategy}.
     * @param strategy the Strategy used for selecting the next address to
     *                 forward an incoming message to
     */
    public DelegatingActor(Strategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Asynchronously relay the incoming message to the address determined
     * by calling {@link Strategy#recipient(Message)}. The delegate can
     * obtain the address of the original sender by calling
     * {@link Message#getReplyTo()}.
     * @param msg the incoming message
     */
    @Override
    public void react(Message msg) {
        if (!validate(msg)) return;
        
        Address delegate = strategy.recipient(msg);

        if (delegate == null) {
            return;
        }

        if (msg.getReplyTo() == null) {
            msg.setReplyTo(msg.getSender());
        }

        /* Note that send() rewrites the sender,
         * but keeps the replyTo intact if it's set */
        send(msg, delegate);
    }

    /**
     * Invoke {@link Strategy#start}
     */
    @Override
    public void start() {
        strategy.start();
    }

    /**
     * If this method returns {@code false} {@code msg} will not be sent
     * along to the delegates. The default implementation always return
     * {@code true} - subclasses should override this method with their own
     * validation logic.
     * @param msg the message to validate
     * @return {@code true} if the message is good and should be forwarded to
     *         the delegates and {@code false} if the message should be blocked
     */
    public boolean validate(Message msg) {
        return true;
    }
}
