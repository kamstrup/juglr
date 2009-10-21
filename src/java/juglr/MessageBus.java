package juglr;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public class MessageBus {

    private static AtomicLong addressCounter = new AtomicLong(1);
    private static MessageBus defaultBus;

    public static synchronized MessageBus getDefault() {
        if (defaultBus == null) {
            defaultBus = new MessageBus();
        }

        return defaultBus;
    }

    private ForkJoinPool pool;

    public MessageBus() {
        pool = new ForkJoinPool();
    }

    ForkJoinPool getPool() {
        return pool;
    }

    public Address newAddress(final Actor actor) {
        return new Address() {

            String address = ":" + addressCounter.getAndIncrement();
            Actor resident = actor;

            @Override
            public String externalize() {
                return address;
            }

            @Override
            public Actor resolve() {
                return resident;
            }
        };
    }
}
