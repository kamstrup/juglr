package juglr;

import java.util.*;
import java.io.Serializable;

/**
 * A Box is a (possible nested) map of String keys to simple data types,
 * other StructuredMessages, or ordered lists of any of these.
 * <p/>
 * A Box is the main atom of communication in PutIt.
 *
 */
public class Box extends Message implements Serializable {

    public enum Type {
        INT,
        FLOAT,
        BOOLEAN,
        STRING,
        MAP,
        LIST
    }

    public static class MessageTypeException extends RuntimeException {
        public MessageTypeException(String msg) {
            super(msg);
        }
    }

    private Type type;
    private Serializable val;

    public Box(Type type) {
        this.type = type;

        switch (type) {
            case INT:
                val = 0;
                break;
            case BOOLEAN:
                val = false;
                break;
            case FLOAT:
                val = 0D;
                break;
            case LIST:
                val = new ArrayList<Box>();
                break;
            case MAP:
                val = new HashMap<String, Box>();
                break;
            case STRING:
                val = "";
                break;
        }
    }

    public Box(long val) {
        type = Type.INT;
        this.val = val;
    }

    public Box(double val) {
        type = Type.FLOAT;
        this.val = val;
    }

    public Box(boolean val) {
        type = Type.BOOLEAN;
        this.val = val;
    }

    public Box(String val) {
        type = Type.STRING;
        this.val = val;
    }

    public Box(List<Box> val) {
        this.type = Type.LIST;
        this.val = new ArrayList<Box>(val);
    }

    public Box(Map<String, Box> val) {
        this.type = Type.LIST;
        this.val = new HashMap<String, Box>(val);
    }

    public static Box newList() {
        return new Box(Type.LIST);
    }

    public static Box newMap() {
        return new Box(Type.MAP);
    }

    public Type getType() {
        return type;
    }

    public Type getType(int index) {
        return get(index).getType();
    }

    public Type getType(String key) {
        return get(key).getType();
    }

    public boolean has(String key) {
        return get(key) != null;
    }

    public boolean has(int index) {
        return index >= 0 && index < getList().size();
    }

    public Serializable getVal() {
        return val;
    }

    /**
     * Add a child Box to a Box of LIST type. This method always returns
     * {@code this}.
     * @param msg the Box to append
     * @return {@code this}
     */
    @SuppressWarnings("unchecked")
    public Box add(Box msg) {
        checkType(Type.LIST);
        ((List<Box>)val).add(msg);
        return this;
    }

    public Box add(long val) {
        return add(new Box(val));
    }

    public Box add(double val) {
        return add(new Box(val));
    }

    public Box add(boolean val) {
        return add(new Box(val));
    }

    public Box add(String val) {
        return add(new Box(val));
    }

    public Box add(List<Box> val) {
        return add(new Box(val));
    }

    public Box add(Map<String, Box> val) {
        return add(new Box(val));
    }

    /**
     * Add a collection of child StructuredMessages to a Box of LIST type.
     * This method always returns {@code this}.
     * @param msgList the collection of StructuredMessages to append
     * @return {@code this}
     */
    @SuppressWarnings("unchecked")
    public Box addAll(Collection<Box> msgList) {
        checkType(Type.LIST);
        ((List<Box>)val).addAll(msgList);
        return this;
    }

    /**
     * Associate a key String with a child Box inside a Box of
     * LIST type.
     * This method always returns {@code this}.
     * @param key the key to add {@code val} under
     * @param val the Box to associate with {@code key}
     * @return {@code this}
     */
    @SuppressWarnings("unchecked")
    public Box put(String key, Box val) {
        checkType(Type.MAP);
        ((Map<String, Box>)this.val).put(key,val);
        return this;
    }

    public Box put(String key, long val) {
        return put(key, new Box(val));
    }

    public Box put(String key, double val) {
        return put(key, new Box(val));
    }

    public Box put(String key, boolean val) {
        return put(key, new Box(val));
    }

    public Box put(String key, String val) {
        return put(key, new Box(val));
    }

    public Box put(String key, List<Box> val) {
        return put(key, new Box(val));
    }

    public Box put(String key, Map<String, Box> val) {
        return put(key, new Box(val));
    }

    /**
     * Import all key-value pairs from {@code map} into this Box (which
     * must be of the MAP type).
     * This method always returns {@code this}.
     * @param map a
     * @return {@code this}
     */
    @SuppressWarnings("unchecked")
    public Box putAll(Map<String, Box> map) {
        checkType(Type.MAP);
        ((Map<String, Box>)val).putAll(map);
        return this;
    }

    @SuppressWarnings("unchecked")
    public long getLong() {
        checkType(Type.INT);
        return (Long)val;
    }

    @SuppressWarnings("unchecked")
    public double getFloat() {
        checkType(Type.FLOAT);
        return (Double)val;
    }

    @SuppressWarnings("unchecked")
    public boolean getBoolean() {
        checkType(Type.BOOLEAN);
        return (Boolean)val;
    }

    @SuppressWarnings("unchecked")
    public String getString() {
        checkType(Type.STRING);
        return (String)val;
    }

    @SuppressWarnings("unchecked")
    public List<Box> getList() {
        checkType(Type.LIST);
        return (List<Box>)val;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Box> getMap() {
        checkType(Type.MAP);
        return (Map<String, Box>)val;
    }

    /**
     * Get the Box at {@code index} from a Box of LIST type.
     * @param index the index into the list from which to retrieve the child
     *              Box
     * @return the Box at {@code index}
     */
    public Box get(int index) {
        return getList().get(index);
    }

    /**
     * Retrieve a child Box with key {@code key} from a Box of
     * MAP type.
     * @param key the key for the child Box to look up
     * @return the child Box or {@code null}
     */
    public Box get(String key) {
        return getMap().get(key);
    }

    /**
     * Return the number of immediate child StructuredMessages of this Box
     * @return The number of child StructuredMessages. This will be 0 for all StructuredMessages
     *         that are not of the types MAP or LIST
     */
    public int size() {
        switch (type) {
            case LIST:
                return getList().size();
            case MAP:
                return getMap().size();
            default:
                return 0;
        }
    }

    /**
     * Assert that this Box is of type {@code t} and throw a
     * {@link MessageTypeException} if it is not.
     * @param t the type that this Box must be
     */
    public void checkType(Type t) {
        if (t != type) {
            throw new MessageTypeException(
                   "Box of type " + type + ", but expected " + t);
        }
    }

    /**
     * Assert that the Box {@code m} is of type {@code t} and throw a
     * {@link MessageTypeException} if it is not.
     * @param m the Box to check the type of
     * @param t the type that this Box must be
     */
    public static void checkType(Box m, Type t) {
        if (t != m.getType()) {
            throw new MessageTypeException(
                    "Box of type " + m.getType()
                            + ", but expected " + t);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Box)) {
            return false;
        }

        Box m = (Box)o;

        if (type != m.getType()) {
            return false;
        }

        switch (m.getType()) {
            case BOOLEAN:
                return getBoolean() == m.getBoolean();
            case FLOAT:
                return getFloat() == m.getFloat();
            case INT:
                return getLong() == m.getLong();
            case LIST:
                List<Box> us = getList();
                List<Box> them = m.getList();
                if (us.size() != them.size()) {
                    return false;
                }
                for (int i = 0; i < us.size(); i++) {
                    if (!us.get(i).equals(them.get(i))) {
                        return false;
                    }
                }
                return true;
            case MAP:
                Map<String, Box> usM = getMap();
                Map<String, Box> themM = m.getMap();
                if (usM.size() != themM.size()) {
                    return false;
                }
                for (String key : usM.keySet()){
                    if (!usM.get(key).equals(themM.get(key))) {
                        return false;
                    }
                }
                return true;
            case STRING:
                return getString().equals(m.getString());
        }
        return true;
    }

    @Override
    public String toString() {
        switch (type) {
            case INT:
            case FLOAT:
            case BOOLEAN:
                return val.toString();
            case STRING:
                return "\"" + val.toString() + "\"";
            case MAP:
            case LIST:
                return new JSonMessageReader(this).asString();
            default:
                // This should never happen
                throw new RuntimeException("Unexpected message type " + type);
        }

    }
}