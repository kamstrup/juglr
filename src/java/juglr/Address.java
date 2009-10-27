package juglr;

/**
 *
 */
public abstract class Address {

    public String toString() {
        return externalize();
    }

    public abstract MessageBus getBus();

    public abstract String externalize();

}
