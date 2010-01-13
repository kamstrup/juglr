package juglr;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Central message hub all actors are connected to. Actors created without
 * a specific message bus will be using the bus obtained from calling
 * {@link juglr.MessageBus#getDefault()}.
 *
 * @see juglr.net.HTTPMessageBus
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

    /**
     * Get a reference to the default {@code MessageBus} instance for the
     * runtime. If there is no instance yet a new one will be created.
     * By a {@link MessageBus} will be created, but you may specify another
     * {@code MessageBus} class in the system property {@code juglr.busclass}.
     * <p/>
     * {@link Actor}s created without a reference to a message bus will use the
     * message bus obtained from calling this method.
     *  
     * @return a reference to the system wide default message bus
     */
    public static synchronized MessageBus getDefault() {
        if (defaultBus == null) {
            String busClassName = System.getProperty(
                                    "juglr.busclass", "juglr.MessageBus");
            try {
                Class busClass = Class.forName(busClassName);
                defaultBus = (MessageBus)busClass.newInstance();
            } catch (Throwable t) {
                throw new EnvironmentError(
                        "Unable to load default message bus class, "
                        + busClassName + ": " + t.getMessage(), t);
            }
        }

        return defaultBus;
    }

    private ForkJoinPool pool;
    private Map<String,Actor> addressSpace;

    /**
     * Create a new MessageBus
     */
    public MessageBus() {
        pool = new ForkJoinPool();
        addressSpace = new HashMap<String,Actor>();

        pool.setAsyncMode(true);
    }

    public Address allocateUniqueAddress(final Actor actor) {
        Address address =
                new LocalAddress(
                        "" + addressCounter.getAndIncrement(), actor, this);
        addressSpace.put(address.externalize(), actor);

        return address;
    }

    public synchronized Address allocateNamedAddress(Actor actor, String name)
                                           throws AddressAlreadyOwnedException {
        if (addressSpace.containsKey(name)) {
            throw new AddressAlreadyOwnedException(name);
        }

        if (name.startsWith("/")) {
            throw new IllegalAddressException(
                    "Address must not start with '/' : " + name);
        }

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
        /* We delegate work to this iter to be able to resolve the returned
         * addresses most efficiently without looking them up.
         * Basically this approach allows us to use the fast path enabled by
         * LocalAddress when resolving the actor for the address via addr.resident */
        final Iterator<String> iter = addressSpace.keySet().iterator();
        final MessageBus dummy = this;

        return new Iterator<Address>() {

            public boolean hasNext() {
                return iter.hasNext();
            }

            public Address next() {
                String ext = iter.next();
                return new LocalAddress(ext, addressSpace.get(ext), dummy);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public void send(Message msg, Address recipient) {
        if (recipient == null) {
            throw new NullPointerException("Recipient address is null");
        }
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
            if (!address.startsWith("/")) {
                this.address = "/" + address;
            } else {
                this.address = address;
            }
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

    public static void main(String[] args) {
        if (System.getProperty("juglr.busclass") == null) {
            System.setProperty("juglr.busclass", "juglr.net.HTTPMessageBus");
        }

        if (args.length > 0) {
            System.setProperty("juglr.busport", args[0]);
        }

        MessageBus bus = null;
        try {
            bus = MessageBus.getDefault();
        } catch (EnvironmentError e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            synchronized (bus) {
                bus.wait(); // Indefinite non-busy block
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupted");
            System.exit(-1);
        }
    }
}
