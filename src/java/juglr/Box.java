package juglr;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Recommended {@link Message} class for general purpose messaging.
 * A Box is a value container type that can hold one of:
 * <ul>
 *   <li>A simple data type; integer, float, boolean, and string</li>
 *   <li>A map of string keys to {@code Box}es</li>
 *   <li>or a list of {@code Box}es</li>
 * </ul>
 * Box instances serialize cleanly and efficiently over streams, and Juglr
 * bundles the classes {@link JSonBoxReader} and {@link JSonBoxParser}
 * for this purpose. 
 * <p/>
 *
 * @see JSonBoxReader
 * @see JSonBoxParser
 */
public class Box extends Message implements Serializable {

    /**
     * The allowed types for box values
     */
    public enum Type {
        INT,
        FLOAT,
        BOOLEAN,
        STRING,
        MAP,
        LIST
    }

    /**
     * Thrown when invoking methods on a box of a type that does not admit
     * the invoked method
     */
    public static class TypeException extends RuntimeException {
        public TypeException(String msg) {
            super(msg);
        }
    }

    private Type type;
    private Serializable val;

    /**
     * Create a Box to hold a value of type {@code type}
     * @param type the value type to store in the box
     */
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

    /**
     * Create a Box to hold a {@code long}
     * @param val the value to store in the box
     */
    public Box(long val) {
        type = Type.INT;
        this.val = val;
    }

    /**
     * Create a Box to hold a {@code double}
     * @param val the value to store in the box
     */
    public Box(double val) {
        type = Type.FLOAT;
        this.val = val;
    }

    /**
     * Create a Box to hold a {@code boolean}
     * @param val the value to store in the box
     */
    public Box(boolean val) {
        type = Type.BOOLEAN;
        this.val = val;
    }

    /**
     * Create a Box to hold a {@code String}
     * @param val the value to store in the box
     */
    public Box(String val) {
        type = Type.STRING;
        this.val = val;
    }

    /**
     * Create a Box to hold a list of boxes
     * @param val the value to store in the box
     */
    public Box(List<Box> val) {
        this.type = Type.LIST;
        this.val = new ArrayList<Box>(val);
    }

    /**
     * Create a Box to hold a map of string keys to Box values
     * @param val the value to store in the box
     */
    public Box(Map<String, Box> val) {
        this.type = Type.LIST;
        this.val = new HashMap<String, Box>(val);
    }

    /**
     * Create a Box that holds an empty list
     * @return a new box storing and empty list
     */
    public static Box newList() {
        return new Box(Type.LIST);
    }

    /**
     * Create a Box that holds an empty map of string keys to Box values
     * @return a box holding an empty map
     */
    public static Box newMap() {
        return new Box(Type.MAP);
    }

    /**
     * Get the type of the value stored in the box
     * @return the type of the value stored in the box
     */
    public Type getType() {
        return type;
    }

    /**
     * Get the value type of the box stored at position {@code index} of this
     * <i>list type</i> box.
     * @param index the index into the list stored by this box
     * @return the value type at position {@code index} of this box
     * @throws juglr.Box.TypeException if {@code this} is not of type {@code LIST}
     */
    public Type getType(int index) {
        return get(index).getType();
    }

    /**
     * Get the value type of the box stored with key {@code key} of this
     * <i>map type</i> box.
     * @param key check box with this key
     * @return the value type for the child box corresponding to {@code key}
     * @throws juglr.Box.TypeException if {@code this} is not of type {@code MAP}
     * @throws NullPointerException if there is no box for {@code key}
     */
    public Type getType(String key) {
        return get(key).getType();
    }

    /**
     * Check if this box of map type has a child for the key {@code key}
     * @param key the key to check
     * @return {@code true} if there is a box for the given key
     * @throws juglr.Box.TypeException if {@code this} is not of type {@code MAP}
     * @throws NullPointerException if there is no box for {@code key}
     */
    public boolean has(String key) {
        return get(key) != null;
    }

    /**
     * Return {@code true} if {@code index} is within the range of the
     * list contained in this box  of {@code LIST} type
     * @param index the number to check
     * @return {@code true} if and only if {@code index} is a valid index into
     *         the list
     */
    public boolean has(int index) {
        return index >= 0 && index < getList().size();
    }

    /**
     * Get the raw value contained in this box
     * @return the value within this box
     */
    public Serializable getVal() {
        return val;
    }

    /**
     * Add a child Box to a Box of LIST type. This method always returns
     * {@code this}.
     * @param box the Box to append
     * @return {@code this}
     * @throws TypeException if this box is not of type {@code LIST}
     */
    @SuppressWarnings("unchecked")
    public Box add(Box box) {
        checkType(Type.LIST);
        ((List<Box>)val).add(box);
        return this;
    }

    /**
     * Add a child to a Box of LIST type. This method always returns
     * {@code this}.
     * @param val the value to append
     * @return {@code this}
     * @throws TypeException if this box is not of type {@code LIST}
     */
    public Box add(long val) {
        return add(new Box(val));
    }

    /**
     * Add a child to a Box of LIST type. This method always returns
     * {@code this}.
     * @param val the value to append
     * @return {@code this}
     * @throws TypeException if this box is not of type {@code LIST}
     */
    public Box add(double val) {
        return add(new Box(val));
    }

    /**
     * Add a child to a Box of LIST type. This method always returns
     * {@code this}.
     * @param val the value to append
     * @return {@code this}
     * @throws TypeException if this box is not of type {@code LIST}
     */
    public Box add(boolean val) {
        return add(new Box(val));
    }

    /**
     * Add a child Box to a Box of LIST type. This method always returns
     * {@code this}.
     * @param val the value to append
     * @return {@code this}
     * @throws TypeException if this box is not of type {@code LIST}
     */
    public Box add(String val) {
        return add(new Box(val));
    }

    /**
     * Add a child Box to a Box of LIST type. This method always returns
     * {@code this}.
     * @param val the list to append
     * @return {@code this}
     * @throws TypeException if this box is not of type {@code LIST}
     */
    public Box add(List<Box> val) {
        return add(new Box(val));
    }

    /**
     * Add a child Box to a Box of LIST type. This method always returns
     * {@code this}.
     * @param val the map to append
     * @return {@code this}
     * @throws TypeException if this box is not of type {@code LIST}
     */
    public Box add(Map<String, Box> val) {
        return add(new Box(val));
    }

    /**
     * Add a collection of child boxes to a box of {@code LIST} type.
     * This method always returns {@code this}.
     * @param boxes the collection of Box instances to add
     * @return {@code this}
     * @throws TypeException if this box is not of type {@code LIST}
     */
    @SuppressWarnings("unchecked")
    public Box addAll(Collection<Box> boxes) {
        checkType(Type.LIST);
        ((List<Box>)val).addAll(boxes);
        return this;
    }

    /**
     * Associate a key String with a child Box inside a Box of
     * {@code LIST} type.
     * @param key the key to add {@code val} under
     * @param val the Box to associate with {@code key}
     * @return {@code this}
     * @throws TypeException if this box is not of type {@code MAP}
     */
    @SuppressWarnings("unchecked")
    public Box put(String key, Box val) {
        checkType(Type.MAP);
        ((Map<String, Box>)this.val).put(key,val);
        return this;
    }

    /**
     * Associate a value with {@code key} in box of {@code MAP} type.
     * @param key the key to associate {@code val} with
     * @param val the value to insert
     * @return {@code this}
     * @throws TypeException if this box is not of type {@code MAP}
     */
    public Box put(String key, long val) {
        return put(key, new Box(val));
    }

    /**
     * Associate a value with {@code key} in box of {@code MAP} type.
     * @param key the key to associate {@code val} with
     * @param val the value to insert
     * @return {@code this}
     * @throws TypeException if this box is not of type {@code MAP}
     */
    public Box put(String key, double val) {
        return put(key, new Box(val));
    }

    /**
     * Associate a value with {@code key} in box of {@code MAP} type.
     * @param key the key to associate {@code val} with
     * @param val the value to insert
     * @return {@code this}
     * @throws TypeException if this box is not of type {@code MAP}
     */
    public Box put(String key, boolean val) {
        return put(key, new Box(val));
    }

    /**
     * Associate a value with {@code key} in box of {@code MAP} type.
     * @param key the key to associate {@code val} with
     * @param val the value to insert
     * @return {@code this}
     * @throws TypeException if this box is not of type {@code MAP}
     */
    public Box put(String key, String val) {
        return put(key, new Box(val));
    }

    /**
     * Associate a value with {@code key} in box of {@code MAP} type.
     * @param key the key to associate {@code val} with
     * @param val the value to insert
     * @return {@code this}
     * @throws TypeException if this box is not of type {@code MAP}
     */
    public Box put(String key, List<Box> val) {
        return put(key, new Box(val));
    }

    /**
     * Associate a value with {@code key} in box of {@code MAP} type.
     * @param key the key to associate {@code val} with
     * @param val the value to insert
     * @return {@code this}
     * @throws TypeException if this box is not of type {@code MAP}
     */
    public Box put(String key, Map<String, Box> val) {
        return put(key, new Box(val));
    }

    /**
     * Import all key-value pairs from {@code map} into this Box (which
     * must be of the MAP type).
     * @param map a
     * @return {@code this}
     * @throws TypeException if this box is not of type {@code MAP}
     */
    @SuppressWarnings("unchecked")
    public Box putAll(Map<String, Box> map) {
        checkType(Type.MAP);
        ((Map<String, Box>)val).putAll(map);
        return this;
    }

    /**
     * Get the {@code long} which is contained in this Box of {@code INT} type
     * @return the value contained in the box
     * @throws TypeException if this box is not of type {@code INT}
     */
    @SuppressWarnings("unchecked")
    public long getLong() {
        checkType(Type.INT);
        return (Long)val;
    }

    public long getLong(int index) {
        return get(index).getLong();
    }

    public long getLong(String key) {
        return get(key).getLong();
    }

    public long getLong(String key, long defaultVal) {
        Box val = get(key);
        if (val != null) return val.getLong();
        return defaultVal;
    }

    /**
     * Get the {@code double} which is contained in this Box of {@code FLOAT}
     * type
     * @return the value contained in the box
     * @throws TypeException if this box is not of type {@code FLOAT}
     */
    @SuppressWarnings("unchecked")
    public double getFloat() {
        checkType(Type.FLOAT);
        return (Double)val;
    }

    public double getFloat(int index) {
        return get(index).getFloat();
    }

    public double getFloat(String key) {
        return get(key).getFloat();
    }

    public double getFloat(String key, double defaultVal) {
        Box val = get(key);
        if (val != null) return val.getFloat();
        return defaultVal;
    }

    /**
     * Get the {@code boolean} which is contained in this Box of {@code BOOLEAN}
     * type
     * @return the value contained in the box
     * @throws TypeException if this box is not of type {@code BOOLEAN}
     */
    @SuppressWarnings("unchecked")
    public boolean getBoolean() {
        checkType(Type.BOOLEAN);
        return (Boolean)val;
    }

    public boolean getBoolean(int index) {
        return get(index).getBoolean();
    }

    public boolean getBoolean(String key) {
        return get(key).getBoolean();
    }

    public boolean getBoolean(String key, boolean defaultVal) {
        Box val = get(key);
        if (val != null) return val.getBoolean();
        return defaultVal;
    }

    /**
     * Get the {@link String} which is contained in this Box of {@code STRING}
     * type
     * @return the value contained in the box
     * @throws TypeException if this box is not of type {@code STRING}
     */
    @SuppressWarnings("unchecked")
    public String getString() {
        checkType(Type.STRING);
        return (String)val;
    }

    public String getString(int index) {
        return get(index).getString();
    }

    public String getString(String key) {
        return get(key).getString();
    }

    public String getString(String key, String defaultVal) {
        Box val = get(key);
        if (val != null) return val.getString();
        return defaultVal;
    }

    /**
     * Get the {@link List} which is contained in this Box of {@code LIST} type
     * @return the value contained in the box
     * @throws TypeException if this box is not of type {@code LIST}
     */
    @SuppressWarnings("unchecked")
    public List<Box> getList() {
        checkType(Type.LIST);
        return (List<Box>)val;
    }

    public List<Box> getList(int index) {
        return get(index).getList();
    }

    public List<Box> getList(String key) {
        return get(key).getList();
    }

    public List<Box> getList(String key, List<Box> defaultVal) {
        Box val = get(key);
        if (val != null) return val.getList();
        return defaultVal;
    }

    /**
     * Get the {@link Map} which is contained in this Box of {@code MAP} type
     * @return the value contained in the box
     * @throws TypeException if this box is not of type {@code MAP}
     */
    @SuppressWarnings("unchecked")
    public Map<String, Box> getMap() {
        checkType(Type.MAP);
        return (Map<String, Box>)val;
    }

    public Map<String,Box> getMap(int index) {
        return get(index).getMap();
    }

    public Map<String,Box> getMap(String key) {
        return get(key).getMap();
    }

    public Map<String,Box> getMap(String key, Map<String,Box> defaultVal) {
        Box val = get(key);
        if (val != null) return val.getMap();
        return defaultVal;
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
     * @return The number of child boxes. This will be 0 for all
     *         Boxes that are not of the types {@code MAP} or {@code LIST}
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
     * {@link juglr.Box.TypeException} if it is not.
     * @param t the type that this Box must be
     */
    public void checkType(Type t) {
        if (t != type) {
            throw new TypeException(
                   "Box of type " + type + ", but expected " + t);
        }
    }

    /**
     * Assert that the Box {@code m} is of type {@code t} and throw a
     * {@link juglr.Box.TypeException} if it is not.
     * @param m the Box to check the type of
     * @param t the type that this Box must be
     */
    public static void checkType(Box m, Type t) {
        if (t != m.getType()) {
            throw new TypeException(
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

    /**
     * String format the value contained in this box
     * @return human readable and serialization-friendly string format
     *         of the box's value
     */
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
                return new JSonBoxReader(this).asString();
            default:
                // This should never happen
                throw new RuntimeException("Unexpected message type " + type);
        }

    }

    /**
     * Return a newly allocated byte array containing this box as JSON encoded
     * in the default system encoding.
     * <p/>
     * This method is slightly faster than {@code toString().getBytes()} in that
     * it avoids the intermediate string representation of the box.
     * @return a JSON data encoded in the default system encoding
     */
    public byte[] toBytes() {
        switch (type) {
            case INT:
            case FLOAT:
                return val.toString().getBytes();
            case BOOLEAN:
                if ((Boolean)val) {
                    return new byte[]{'t', 'r', 'u', 'e'};
                } else {
                    return new byte[]{'f', 'a', 'l', 's', 'e'};
                }
            case STRING:
            case MAP:
            case LIST:
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                Writer writer = new OutputStreamWriter(bytes);
                try {
                    new JSonBoxWriter().write(this, writer);
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException(
                            "I/O Exception from in-memory work. " +
                            "This should never happen");
                }
                return bytes.toByteArray();
            default:
                // This should never happen
                throw new RuntimeException("Unexpected message type " + type);
        }

        
    }
}
