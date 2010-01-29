package juglr;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Asynchronously forward incoming messages to a collection of delegates
 * based on a given strategy. For cases where you want to forward messages
 * to a single actor out of a given set see {@link DelegatingActor}.
 *
 * @see DelegatingActor
 */
public class MulticastActor extends Actor {

    /**
     * Used by {@link MulticastActor} to determine which addresses
     * to relay a given message to
     */
    public static interface Strategy {

        /**
         * Get an iterator over the recipients of {@code msg}
         * @param msg the message to find the recipeints for
         * @return and iterator over the addresses to send {@code msg} to
         */
        public Iterator<Address> recipients(Message msg);

        /**
         * Make sure that all actors related to this strategy have their
         * {@link Actor#start} method invoked. Note that it is the
         * responsibility of the strategy to also start any actors that may
         * be created after this method call
         */
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

    /**
     * Asynchronously send {@code msg} to all addresses determined by calling
     * {@link Strategy#recipients}
     * @param msg the incoming message
     */
    @Override
    public void react(Message msg) {
        if (!validate(msg)) return;

        Iterator<Address> recipients = strategy.recipients(msg);
        while (recipients.hasNext()) {
            send(msg, recipients.next());
        }
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
