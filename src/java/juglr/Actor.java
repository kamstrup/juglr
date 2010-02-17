package juglr;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import static java.util.concurrent.ForkJoinPool.ManagedBlocker;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Base class for all actors. An Actor in the Juglr framework sends and receives
 * {@link Message}s over a {@link MessageBus}. All communications between actors
 * are fully asynchronous.
 * <p/>
 * A very important rule when coding with actors is the idea of
 * <i>"shared nothing"</i>. This is enforced by routing all inter-actor
 * communuications through the message bus. It must be emphasized
 * that actors should not have direct references to each
 * other. Instead they simply store the {@link Address}es of the actors they
 * need to send messages to.
 * <p/>
 * Although the standard {@link Message} class can indeed hold shared state it
 * is strongly advised to avoid this. One way to assert that there is no shared
 * state is to only use {@link Box} messages which can only store
 * simple data types (and also have the added benefit of mapping cleanly to
 * JSON).
 * <p/>
 * There are two central callback methods actors can override, namely
 * {@link #react} and {@link #start}. As a rule of thumb these methods should
 * never block in order not to starvate the underlying threadpool of the
 * message bus. There are a few legal ways for an actor to block, notably
 * {@link #await(Callable)} and {@link #awaitTimeout(long)}.
 * <p/>
 * <h3>Parallelizing Work</h3>
 * Each actor is guaranteed to only be handling one message at a time.
 * In technical terms this means that {@link #react(Message)} and
 * {@link #start()}is guaranteed to be called from a context synchronized on
 * the actor. This has the nice effect of making actors automatically thread
 * safe - actor implementations need not take any precautions to remain thread
 * safe.
 * <p/>
 * The only tricky part in writing highly parallel actors is to keep in mind
 * that it might not be the same thread that invokes the actor methods from time
 * to time. This makes constructs like {@code ThreadLocal}s more tricky to use
 * correctly, but it can still be done. 
 * <p/>
 * Juglr provides some
 * helper classes for parallelizing work, namely {@link DelegatingActor} and
 * {@link MulticastActor}.
 *
 * @see Message
 * @see MessageBus
 */
public abstract class Actor {    

    private MessageBus bus;
    private Address address;

    /**
     * Create an actor connected to the default message bus
     *
     * @see MessageBus#getDefault()
     */
    public Actor() {
        this(MessageBus.getDefault());
    }

    /**
     * Create an actor connected to the {@link MessageBus} {@code bus}
     * @param bus the message bus the actor should connect to
     */
    public Actor(MessageBus bus) {
        this.bus = bus;
        address = bus.allocateUniqueAddress(this);
    }

    /**
     * Get the unique address of this actor assigned by the message bus
     * upon connection time
     * @return the unique bus name for this actor
     */
    public final Address getAddress() {
        return address;
    }

    /**
     * Get the message bus this actor is connected to
     * @return
     */
    public MessageBus getBus() {
        return bus;
    }

    /**
     * Returns the externalized form of this actor's {@link Address}
     * @return
     */
    public String toString() {
        return address.externalize();
    }

    /**
     * Send a message to another actor. To send a reply to an incoming message
     * do {@code send(myMsg, msg.getSender()}.
     * <p/>
     * If the reply-to field of {@code msg} is not set, this method will set
     * it to the address of {@code this}.
     *
     * @param msg the message to send
     * @param receiver the address of the actor to send to
     * @see Message#getSender
     */
    public final void send(Message msg, Address receiver) {
        msg.setSender(this.getAddress());
        if (msg.getReplyTo() == null) {
            msg.setReplyTo(this.getAddress());
        }
        bus.send(msg, receiver);
    }

    /**
     * Do a blocking call and return its value. This is useful for doing
     * IO or other blocking operations. The blocking will be done in a manner
     * such that the thread pool of the message bus will not be affected.
     * <p/>
     * This method must only be called within the {@link #react} and
     * {@link #start} methods of the actor.
     * <p/>
     * If you need to do a lot of blocking operations consider batching them
     * into one call to this method. Ie. don't read 128 bit blocks from a file
     * in sequential calls to this method, but read big chunks or even the whole
     * file in one go.
     *
     * @param closure the callable to execute
     * @return the return value of {@code closure.call()}
     * @throws InterruptedException if interrupted while processing the
     *     blocking call
     * @throws InvocationTargetException if {@code closure.call()} throws an
     *     exception. In this case the cause of the
     *     {@code InvocationTargetException} is guaranteed to be set to the
     *     original exception from {@code closure.call()}.
     *
     */
    public final <T> T await(final Callable<T> closure)
                        throws InvocationTargetException, InterruptedException {
        BlockingClosure<T> closureBlocker = new BlockingClosure<T>(closure);

        ForkJoinPool.managedBlock(closureBlocker, true);
        if (closureBlocker.getError() != null) {
            throw new InvocationTargetException(closureBlocker.getError());
        } else {
            return closureBlocker.getResult();
        }
    }

    /**
     * Sleep for {@code millis} milliseconds and resume operation. The blocking
     * is done in a cooperative manner and the thread pool of the message bus
     * will not be starved because of threads blocking on {@code awaitTimeout}.
     *
     * @param millis number of milliseconds to sleep
     * @throws InterruptedException if interruped while sleeping
     */
    public void awaitTimeout(final long millis) throws InterruptedException {
        ManagedBlocker blocker = new ManagedBlocker() {
            private boolean hasSlept = false;
            public boolean block() throws InterruptedException {
                hasSlept = true; // First set this in case of interrupts
                Thread.sleep(millis);
                return isReleasable();
            }

            public boolean isReleasable() {
                return hasSlept;
            }
        };

        ForkJoinPool.managedBlock(blocker, true);
    }

    /**
     * This method ensures that access to react() is always synchronized
     * @param msg the message to invoke react() on
     */
    void dispatchReact(Message msg) {
        try {
            synchronized (this) {
                react(msg);
            }
        } catch (Throwable t) {
            /* Catch anything, since we can't trust react() and we are running
             * in a thread, so exceptions will silently vanish if uncaught */
            System.err.println(String.format(
             "Error caught from actor '%s' while processing the message %s: %s",
             this, msg, t));
            t.printStackTrace();
        }
    }

    /**
     * This method ensures that access to start() is always synchronized
     */
    void dispatchStart() {
        try {
            synchronized (this) {
                start();
            }
        } catch (Throwable t) {
            /* Catch anything, since we can't trust start() and we are running
             * in a thread, so exceptions will silently vanish if uncaught */
            System.err.println(String.format(
             "Error caught from actor '%s' while starting it: %s", this, t));
            t.printStackTrace();
        }
    }

    /**
     * Initiate the actor life cycle, you may start sending messages from
     * within this method. This method is guaranteed to be run
     * in a calling context synchronized on this actor.
     */
    public void start() {
        // Default impl does nothing
    }

    /**
     * Primary method for handling incoming messages, override it with
     * your message handling logic. This method is guaranteed to be run
     * in a calling context synchronized on this actor. In effect this means
     * that actors only handle one message at a time. For a discussion on how
     * to parallelize message processing see the section in the class
     * documentation.
     * <p/>
     * Blocking operations, such as IO, should be done within an
     * {@link #await(Callable)} call.
     *
     * @param msg the incoming message
     */
    public abstract void react (Message msg);

    private static class BlockingClosure<T> implements ManagedBlocker {
        private T result;
        private Exception exception;
        private Callable<T> closure;

        public BlockingClosure(Callable<T> closure) {
            this.closure = closure;
        }

        public boolean block() throws InterruptedException {
            try {
                result = closure.call();
            } catch (Exception e) {
                exception = e;
            } finally {
                closure = null;
            }
            return isReleasable();
        }

        public boolean isReleasable() {
            return closure == null;
        }

        public Exception getError() {
            return exception;
        }

        public T getResult() {
            return result;
        }
    }

}
