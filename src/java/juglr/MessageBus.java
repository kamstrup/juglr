package juglr;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public class MessageBus {

    /**
     *
     */
    static class ForkJoinMessageClosure extends RecursiveAction {

        private MessageBus bus;
        private Message msg;
        private Address receiver;

        public ForkJoinMessageClosure(
                                MessageBus bus, Message msg, Address receiver) {
            this.bus = bus;
            this.msg = msg;
            this.receiver = receiver;
        }

        @Override
        public void compute() {
            Actor actor = bus.lookup(receiver);
            actor.dispatchReact(msg);
        }
    }

    /**
     *
     */
    static class ForkJoinStartClosure extends RecursiveAction {

        private MessageBus bus;
        private Address receiver;

        public ForkJoinStartClosure(
                                MessageBus bus, Address receiver) {
            this.bus = bus;
            this.receiver = receiver;
        }

        @Override
        public void compute() {
            Actor actor = bus.lookup(receiver);
            actor.start();
        }
    }

    private static AtomicLong addressCounter = new AtomicLong(1);
    private static MessageBus defaultBus;

    public static synchronized MessageBus getDefault() {
        if (defaultBus == null) {
            defaultBus = new MessageBus();
        }

        return defaultBus;
    }

    private ForkJoinPool pool;
    private Map<String,Actor> addressSpace;

    public MessageBus() {
        pool = new ForkJoinPool();
        addressSpace = new HashMap<String,Actor>();

        pool.setAsyncMode(true);
    }

    public Address allocateUniqueAddress(final Actor actor) {
        Address address =
                new LocalAddress(
                        "+" + addressCounter.getAndIncrement(), actor, this);
        addressSpace.put(address.externalize(), actor);

        return address;
    }

    public synchronized Address allocateNamedAddress(Actor actor, String name)
                                           throws AddressAlreadyOwnedException {
        if (addressSpace.containsKey(name)) {
            throw new AddressAlreadyOwnedException(name);
        }

        if (name.startsWith("+")) {
            throw new IllegalAddressException(
                    "Address must not start with ':' : " + name);
        }

        // FIXME: More strict naming scheme, throw IllegalAddressException

        Address address =
                new LocalAddress(
                        name, actor, this);
        addressSpace.put(address.externalize(), actor);

        return address;
    }

    public boolean freeAddress(Address address) {
        return addressSpace.remove(address.externalize()) != null;
    }

    public Iterator<Address> list() {
        /* We delegate work to this iter to be able to resolve addresses
         * most efficiently without looking them up. Basically this approach
         * allows us to use the fast path enabled by LocalAddress when
         * resolving the actor for the address */
        final Iterator<Actor> iter = addressSpace.values().iterator();

        return new Iterator<Address>() {

            public boolean hasNext() {
                return iter.hasNext();
            }

            public Address next() {
                return iter.next().getAddress();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public void send(Message msg, Address recipient) {
        pool.submit(new ForkJoinMessageClosure(this, msg, recipient));
    }

    public void start(Address recipient) {
        pool.submit(new ForkJoinStartClosure(this, recipient));
    }

    private Actor lookup(Address address) {
        // Fast path lookups for local addresses
        if (address instanceof LocalAddress) {
            return ((LocalAddress) address).resolve();
        }

        return addressSpace.get(address.externalize());
    }

    public Address lookup(String address) {
        Actor actor = addressSpace.get(address);
        return actor == null ? null : actor.getAddress();
    }

    private static class LocalAddress extends Address {

        String address;
        Actor resident;
        MessageBus bus;

        public LocalAddress(String address, Actor resident, MessageBus bus) {
            this.address = address;
            this.resident = resident;
            this.bus = bus;
        }

        @Override
        public String externalize() {
            return address;
        }

        @Override
        public MessageBus getBus() {
            return bus;
        }

        public Actor resolve() {
            return resident;
        }
    }
}
