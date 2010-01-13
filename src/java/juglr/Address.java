package juglr;

/**
 * Encapsulation of an address on the message bus. Each {@link Actor} is
 * assigned a <i>unique</i> address on the message bus upon construction.
 * Following that, actors may request <i>named addresses</i> on the bus.
 * Named addresses can be used to contact actors under well known names,
 * much like host names are used on the internet.
 * <p/>
 * Addresses are obtained by calling
 * {@link MessageBus#allocateNamedAddress(Actor, String)} and freed again by
 * calling {@link MessageBus#freeAddress(Address)}.
 *
 * @see Actor
 * @see MessageBus
 */
public abstract class Address {

    public String toString() {
        return externalize();
    }

    /**
     * Get the message bus on which this address is valid
     * @return
     */
    public abstract MessageBus getBus();

    /**
     * Return a string formatted version of the address, suitable for wire
     * protocols
     * @return and external representation of the address
     */
    public abstract String externalize();

}
