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
    private Address replyTo;

    void setSender(Address sender) {
        this.sender = sender;
    }

    /**
     * Get the {@code Address} of the {@link Actor} responsible for sending
     * this message. Useful for responding to messages from otherwise unknown
     * parties. The sender address is guaranteed to be set when receiving a
     * message in {@link Actor#react} or {@link Actor#awaitMessage()}. Note
     * that it is generally advised to use {@link #getReplyTo()} instead
     * of this method, unless you understand what you are doing.
     *
     * @return the address of the sender or {@code null} if this message has
     *         not been send
     */
    public Address getSender() {
        return sender;
    }

    /**
     * Set the address that the message recipient should reply to in case
     * that is relevant. This is useful if you route a message through several
     * actors and want the last one to reply to the original sender.
     * <p/>
     * The {@link DelegatingActor} and {@link MulticastActor} will set the
     * reply-to address to that of the original sender when passing messages on
     * to delegates.
     * @param replyTo the address the message recipient should send replies to
     * @return always returns {@code this}
     */
    public Message setReplyTo(Address replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    /**
     * Get the address the message recipient should reply to in case
     * that is relevant. Note that this might not always be the same as
     * the message sender in case the message was routed through intermediate
     * actors like a {@link DelegatingActor} or {@link MulticastActor}.
     * @return the address that replies should be sent to
     */
    public Address getReplyTo() {
        return replyTo;
    }
}
