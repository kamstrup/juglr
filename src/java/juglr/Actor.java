package juglr;

import java.util.concurrent.ForkJoinPool;
import static java.util.concurrent.ForkJoinPool.ManagedBlocker;
import java.util.concurrent.RecursiveAction;

/**
 *
 */
public abstract class Actor {

    /**
     *
     */
    static class ForkJoinMessageClosure extends RecursiveAction {

        private Message msg;
        private Address receiver;

        public ForkJoinMessageClosure(Message msg, Address receiver) {
            this.msg = msg;
            this.receiver = receiver;
        }

        @Override
        public void compute() {
            receiver.resolve().dispatchReact(msg);
        }
    }

    private MessageBus bus;
    private Address address;
    private ManagedBlocker blocker;
    private Message waitingMessage;

    /* Concurrency note: These two members must only be accessed
     *                   while holding the monitor on postFlag */
    private boolean waitingForPostFlag;
    private final Object postFlag = new Object();

    public Actor() {
        this(MessageBus.getDefault());
    }

    public Actor(MessageBus bus) {
        this.bus = bus;
        address = bus.newAddress(this);
        waitingForPostFlag = false;
    }

    public final Address getAddress() {
        return address;
    }

    public String toString() {
        return address.externalize();
    }

    public final void send(Message msg, Address receiver) {
        msg.setSender(this.getAddress());
        bus.getPool().submit(new ForkJoinMessageClosure(msg, receiver));
    }

    public final synchronized Message awaitMessage ()
                                                   throws InterruptedException {
        if (blocker == null) {
            blocker = new ManagedBlocker() {

                public boolean block() throws InterruptedException {
                    if (!isReleasable()) {
                        synchronized (postFlag) {
                            waitingForPostFlag = true;
                            postFlag.wait();
                        }
                        return isReleasable();
                    }
                    return isReleasable();
                }

                public boolean isReleasable() {
                    return waitingMessage != null;
                }
            };
        }

        ForkJoinPool.managedBlock(blocker, true);

        /* We are now guaranteed that waitingMessage is != null */
        synchronized (postFlag) {
            Message incoming = waitingMessage;
            waitingForPostFlag = false;
            waitingMessage = null;
            return incoming;
        }
    }

    /**
     * This method ensures that access to react() is always synchronized
     * @param msg the message to invoke react() on
     */
    private void dispatchReact(Message msg) {
        /* If actor is stuck in a awaitMessage() then pass the message
         * into this context, notify the waiting thread, and return
         */
        synchronized (postFlag) {
            if (waitingForPostFlag) {
                waitingMessage = msg;
                postFlag.notify();
                return;
            }
        }

        /* All is well */
        synchronized (this) {
            react(msg);
        }
    }

    /**
     * Initiate the actor life cycle, you may start sending messages from
     * within this method
     */
    public void start() {
        // Default impl does nothing
    }

    /**
     * Primary method for handling incoming messages, override it with
     * your message handling logic. This method is guaranteed to be run
     * in a calling context synchronized on this actor.
     * <p/>
     * You can await messages from withing this method by calling
     * {@link #awaitMessage()}. To prepare for handling the next message
     * in a clean context simply return from this method call.
     *
     * @param msg the incoming message
     */
    public abstract void react (Message msg);

}
