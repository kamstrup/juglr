package juglr;

/**
 * Abstract base class for all message types. It is recommended to use the
 * {@link Box}  class to transfer messages between actors since
 * it guarantees "shared nothing" which is important in highly parallel
 * computation.
 * <p/>
 * If you need to send specialized messages that can not be seriliazed to
 * simple Java types, or serialization is too expensive you can subclass
 * this class to get what you need.
 *
 * @see Actor
 * @see Box
 * @see MessageBus
 */
public class Message {

    private Address sender;

    void setSender(Address sender) {
        this.sender = sender;
    }

    /**
     * Get the {@code Address} of the {@link Actor} responsible for sending
     * this message. Useful for responding to messages from otherwise unknown
     * parties. The sender address is guaranteed to be set when receiving a
     * message in {@link Actor#react} or {@link Actor#awaitMessage()}.
     *
     * @return the address of the sender or {@code null} if this message has
     *         not been send
     */
    public Address getSender() {
        return sender;
    }
}
