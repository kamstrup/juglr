package juglr;

/**
 *
 */
public abstract class Address {

    public String toString() {
        return externalize();
    }

    public abstract String externalize();

    abstract Actor resolve();

}
