package juglr;

/**
 * Throw when trying to allocate a named address on the bus is not legal
 * according to the bus naming rules.
 *
 * @see MessageBus#allocateNamedAddress(Actor, String) 
 */
public class IllegalAddressException extends RuntimeException {
    public IllegalAddressException(String name) {
    }
}
