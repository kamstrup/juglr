package juglr;

import java.util.concurrent.ForkJoinPool;
import static java.util.concurrent.ForkJoinPool.ManagedBlocker;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
public abstract class Actor {    

    private MessageBus bus;
    private Address address;
    private ManagedBlocker blocker;
    private final Lock dispatchLock = new ReentrantLock();
    private final Condition publicPostFlag  = dispatchLock.newCondition();
    private final Condition privatePostFlag = dispatchLock.newCondition();
    private Message waitingMessage;
    private boolean waitingForPrivatePostFlag;

    public Actor() {
        this(MessageBus.getDefault());
    }

    public Actor(MessageBus bus) {
        this.bus = bus;
        address = bus.newAddress(this);
        waitingMessage = null;
    }

    public final Address getAddress() {
        return address;
    }

    public MessageBus getBus() {
        return bus;
    }

    public String toString() {
        return address.externalize();
    }

    public final void send(Message msg, Address receiver) {
        msg.setSender(this.getAddress());
        bus.send(msg, receiver);
    }

    /**
     * Returns {@code null} if interrupted
     * @return
     */
    public final Message awaitMessage () {
        if (blocker == null) {
            blocker = new ManagedBlocker() {

                public boolean block() throws InterruptedException {
                    // Release dispatchLock
                    privatePostFlag.await();

                    return isReleasable();
                }

                public boolean isReleasable() {
                    return waitingMessage != null;
                }
            };
        }

        try {
            waitingMessage = null;
            waitingForPrivatePostFlag = true;
            ForkJoinPool.managedBlock(blocker, true);
            Message incoming = waitingMessage;
            waitingMessage = null;
            waitingForPrivatePostFlag = false;
            publicPostFlag.signal();
            return incoming;
        } catch (InterruptedException e) {
            // Ignore interrupts, we *likely* have waitingMessage == null,
            // but this is OK by our contract
            return null;
        }
    }

    /**
     * This method ensures that access to react() is always synchronized
     * @param msg the message to invoke react() on
     */
    void dispatchReact(Message msg) {
        /* If actor is stuck in a awaitMessage() then pass the message
         * into this context, notify the waiting thread, and return
         */
        dispatchLock.lock();
        try {
            if (waitingMessage != null || waitingForPrivatePostFlag) {
                while (waitingMessage != null) {
                    try {
                        publicPostFlag.await();
                    } catch (InterruptedException e) {
                        // Ignore
                    }                    
                }
                waitingMessage = msg;
                privatePostFlag.signal();
                return;
            } else {
                /* All is well */
                react(msg);
            }
        } finally {
            dispatchLock.unlock();
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
