package juglr;

import java.util.*;
import java.io.Serializable;

/**
 * A StructuredMessage is a (possible nested) map of String keys to simple data types,
 * other StructuredMessages, or ordered lists of any of these.
 * <p/>
 * A StructuredMessage is the main atom of communication in PutIt.
 *
 */
public class StructuredMessage extends Message implements Serializable {

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

    public StructuredMessage(Type type) {
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
                val = new ArrayList<StructuredMessage>();
                break;
            case MAP:
                val = new HashMap<String,StructuredMessage>();
                break;
            case STRING:
                val = "";
                break;
        }
    }

    public StructuredMessage(long val) {
        type = Type.INT;
        this.val = val;
    }

    public StructuredMessage(double val) {
        type = Type.FLOAT;
        this.val = val;
    }

    public StructuredMessage(boolean val) {
        type = Type.BOOLEAN;
        this.val = val;
    }

    public StructuredMessage(String val) {
        type = Type.STRING;
        this.val = val;
    }

    public StructuredMessage(List<StructuredMessage> val) {
        this.type = Type.LIST;
        this.val = new ArrayList<StructuredMessage>(val);
    }

    public StructuredMessage(Map<String,StructuredMessage> val) {
        this.type = Type.LIST;
        this.val = new HashMap<String,StructuredMessage>(val);
    }

    public static StructuredMessage newList() {
        return new StructuredMessage(Type.LIST);
    }

    public static StructuredMessage newMap() {
        return new StructuredMessage(Type.MAP);
    }

    public Type getType() {
        return type;
    }

    public Serializable getVal() {
        return val;
    }

    /**
     * Add a child StructuredMessage to a StructuredMessage of LIST type. This method always returns
     * {@code this}.
     * @param msg the StructuredMessage to append
     * @return {@code this}
     */
    @SuppressWarnings("unchecked")
    public StructuredMessage add(StructuredMessage msg) {
        checkType(Type.LIST);
        ((List<StructuredMessage>)val).add(msg);
        return this;
    }

    public StructuredMessage add(long val) {
        return add(new StructuredMessage(val));
    }

    public StructuredMessage add(double val) {
        return add(new StructuredMessage(val));
    }

    public StructuredMessage add(boolean val) {
        return add(new StructuredMessage(val));
    }

    public StructuredMessage add(String val) {
        return add(new StructuredMessage(val));
    }

    public StructuredMessage add(List<StructuredMessage> val) {
        return add(new StructuredMessage(val));
    }

    public StructuredMessage add(Map<String,StructuredMessage> val) {
        return add(new StructuredMessage(val));
    }

    /**
     * Add a collection of child StructuredMessages to a StructuredMessage of LIST type.
     * This method always returns {@code this}.
     * @param msgList the collection of StructuredMessages to append
     * @return {@code this}
     */
    @SuppressWarnings("unchecked")
    public StructuredMessage addAll(Collection<StructuredMessage> msgList) {
        checkType(Type.LIST);
        ((List<StructuredMessage>)val).addAll(msgList);
        return this;
    }

    /**
     * Associate a key String with a child StructuredMessage inside a StructuredMessage of
     * LIST type.
     * This method always returns {@code this}.
     * @param key the key to add {@code val} under
     * @param val the StructuredMessage to associate with {@code key}
     * @return {@code this}
     */
    @SuppressWarnings("unchecked")
    public StructuredMessage put(String key, StructuredMessage val) {
        checkType(Type.MAP);
        ((Map<String,StructuredMessage>)this.val).put(key,val);
        return this;
    }

    public StructuredMessage put(String key, long val) {
        return put(key, new StructuredMessage(val));
    }

    public StructuredMessage put(String key, double val) {
        return put(key, new StructuredMessage(val));
    }

    public StructuredMessage put(String key, boolean val) {
        return put(key, new StructuredMessage(val));
    }

    public StructuredMessage put(String key, String val) {
        return put(key, new StructuredMessage(val));
    }

    public StructuredMessage put(String key, List<StructuredMessage> val) {
        return put(key, new StructuredMessage(val));
    }

    public StructuredMessage put(String key, Map<String,StructuredMessage> val) {
        return put(key, new StructuredMessage(val));
    }

    /**
     * Import all key-value pairs from {@code map} into this StructuredMessage (which
     * must be of the MAP type).
     * This method always returns {@code this}.
     * @param map a
     * @return {@code this}
     */
    @SuppressWarnings("unchecked")
    public StructuredMessage putAll(Map<String,StructuredMessage> map) {
        checkType(Type.MAP);
        ((Map<String,StructuredMessage>)val).putAll(map);
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
    public List<StructuredMessage> getList() {
        checkType(Type.LIST);
        return (List<StructuredMessage>)val;
    }

    @SuppressWarnings("unchecked")
    public Map<String,StructuredMessage> getMap() {
        checkType(Type.MAP);
        return (Map<String,StructuredMessage>)val;
    }

    /**
     * Get the StructuredMessage at {@code index} from a StructuredMessage of LIST type.
     * @param index the index into the list from which to retrieve the child
     *              StructuredMessage
     * @return the StructuredMessage at {@code index}
     */
    public StructuredMessage get(int index) {
        return getList().get(index);
    }

    /**
     * Retrieve a child StructuredMessage with key {@code key} from a StructuredMessage of
     * MAP type.
     * @param key the key for the child StructuredMessage to look up
     * @return the child StructuredMessage or {@code null}
     */
    public StructuredMessage get(String key) {
        return getMap().get(key);
    }

    /**
     * Return the number of immediate child StructuredMessages of this StructuredMessage
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
     * Assert that this StructuredMessage is of type {@code t} and throw a
     * {@link MessageTypeException} if it is not.
     * @param t the type that this StructuredMessage must be
     */
    public void checkType(Type t) {
        if (t != type) {
            throw new MessageTypeException(
                   "StructuredMessage of type " + type + ", but expected " + t);
        }
    }

    /**
     * Assert that the StructuredMessage {@code m} is of type {@code t} and throw a
     * {@link MessageTypeException} if it is not.
     * @param m the StructuredMessage to check the type of
     * @param t the type that this StructuredMessage must be
     */
    public static void checkType(StructuredMessage m, Type t) {
        if (t != m.getType()) {
            throw new MessageTypeException(
                    "StructuredMessage of type " + m.getType()
                            + ", but expected " + t);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StructuredMessage)) {
            return false;
        }

        StructuredMessage m = (StructuredMessage)o;

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
                List<StructuredMessage> us = getList();
                List<StructuredMessage> them = m.getList();
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
                Map<String,StructuredMessage> usM = getMap();
                Map<String,StructuredMessage> themM = m.getMap();
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
        String s = null;
        switch (getType()) {
            case BOOLEAN:
                return Boolean.toString(getBoolean());
            case FLOAT:
                return Double.toString(getFloat());
            case INT:
                return Long.toString(getLong());
            case LIST:
                s = "[";
                for (int i = 0; i < size(); i++) {
                    if (s.length() == 1) {
                        s += get(i);
                    } else {
                        s += ", " + get(i);
                    }
                }
                return s + "]";
            case MAP:
                Map<String,StructuredMessage> map = getMap();
                s = "{";
                for (Map.Entry<String,StructuredMessage> entry: map.entrySet()){
                    if (s.length() == 1) {
                        s += "'" + entry.getKey() +"' : " + entry.getValue();
                    } else {
                        s += ", '" + entry.getKey()
                                 + "' : " + entry.getValue();
                    }

                }
                return s + "}";
            case STRING:
                return "'" + getString() + "'";
        }
        return "ERROR";
    }
}
