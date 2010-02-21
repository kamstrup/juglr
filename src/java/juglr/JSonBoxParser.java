package juglr;

import juglr.internal.org.json.JSONTokener;
import juglr.internal.org.json.JSONObject;
import juglr.internal.org.json.JSONException;
import juglr.internal.org.json.JSONArray;

import java.io.*;
import java.util.Iterator;

/**
 * Helper class for parsing strings, readers, or files into {@link Box}
 * instanses. Note that all methods on this class are thread safe.
 *
 * @see BoxReader
 * @see JSonBoxReader
 * @see Box
 */
public class JSonBoxParser implements BoxParser {

    public Box parse (String in) {
        try {
            return parse(new StringReader(in));
        } catch (IOException e) {
            // This should never happen!
            throw new RuntimeException("Unexpected error reading string '"
                                       + in +"': " + e.getMessage(), e);
        }
    }

    public Box parse (Reader in) throws IOException {
         try {
            return realParse(in);
        } catch (JSONException e) {
            throw new MessageFormatException("Syntax error reading JSON data "
                                             + "from stream:"
                                             + e.getMessage(), e);
        }
    }

    public Box parse (File jsonFile) throws IOException {
        Reader r = new FileReader(jsonFile);
        try {
            return realParse(r);
        } catch (JSONException e) {
            throw new MessageFormatException("Syntax error reading JSON data "
                                             + "from " + jsonFile + ": "
                                             + e.getMessage(), e);
        }
    }

    public Box parse(InputStream in) throws IOException {
        return parse(new InputStreamReader(in));
    }

    private Box realParse(Reader in) throws JSONException {
        JSONTokener t = new JSONTokener(in);
        Object obj = t.nextValue();
        return parseObject(obj);
    }

    @SuppressWarnings("unchecked")
    private Box parseObject (Object obj) throws JSONException {
        Box msg;

        if (obj instanceof Integer) {
            return new Box((Integer)obj);
        } else if (obj instanceof Long) {
            return new Box((Long)obj);
        } else if (obj instanceof Double) {
            return new Box((Double)obj);
        } else if (obj instanceof Boolean) {
            return new Box((Boolean)obj);
        } else if (obj instanceof String) {
            return new Box((String)obj);
        } else if (obj instanceof JSONArray) {
            JSONArray a = (JSONArray)obj;
            msg = Box.newList();
            for (int i = 0; i < a.length(); i++) {
                msg.add(parseObject(a.get(i)));
            }
            return msg;
        } else if (obj instanceof JSONObject) {
            JSONObject jsObj = (JSONObject)obj;
            Iterator<String> iter = (Iterator<String>)jsObj.keys();
            msg = Box.newMap();
            while (iter.hasNext()) {
                String key = iter.next();
                msg.put(key, parseObject(jsObj.get(key)));
            }
            return msg;
        } else {
            throw new RuntimeException("Unexpected object type from JSON" +
                                       "stream " + obj.getClass());
        }
    }



}
