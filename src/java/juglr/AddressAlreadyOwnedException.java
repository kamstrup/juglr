package juglr;

/**
 * Thrown when trying to allocate an address that is already owned by another
 * actor on the bus.
 *
 * @see MessageBus#allocateNamedAddress(Actor, String) 
 */
public class AddressAlreadyOwnedException extends Exception {
    public AddressAlreadyOwnedException(String name) {
        super(name);
    }
}
