package juglr;

import juglr.internal.org.json.JSONTokener;
import juglr.internal.org.json.JSONObject;
import juglr.internal.org.json.JSONException;
import juglr.internal.org.json.JSONArray;

import java.io.*;
import java.util.Iterator;

/**
 *
 */
public class JSonMessageParser implements MessageParser {

    public StructuredMessage parse (String in) {
        try {
            return parse(new StringReader(in));
        } catch (IOException e) {
            // This should never happen!
            throw new RuntimeException("Unexpected error reading string '"
                                       + in +"': " + e.getMessage(), e);
        }
    }

    public StructuredMessage parse (Reader in) throws IOException {
         try {
            return realParse(in);
        } catch (JSONException e) {
            throw new MessageFormatException("Syntax error reading JSON data "
                                             + "from stream:"
                                             + e.getMessage(), e);
        }
    }

    public StructuredMessage parse (File jsonFile) throws IOException {
        Reader r = new FileReader(jsonFile);
        try {
            return realParse(r);
        } catch (JSONException e) {
            throw new MessageFormatException("Syntax error reading JSON data "
                                             + "from " + jsonFile + ": "
                                             + e.getMessage(), e);
        }
    }

    private StructuredMessage realParse(Reader in) throws JSONException {
        JSONTokener t = new JSONTokener(in);
        JSONObject obj = new JSONObject(t);
        return parseObject(obj);
    }

    @SuppressWarnings("unchecked")
    private StructuredMessage parseObject (Object obj) throws JSONException {
        StructuredMessage msg;

        if (obj instanceof Integer) {
            return new StructuredMessage((Integer)obj);
        } else if (obj instanceof Long) {
            return new StructuredMessage((Long)obj);
        } else if (obj instanceof Double) {
            return new StructuredMessage((Double)obj);
        } else if (obj instanceof Boolean) {
            return new StructuredMessage((Boolean)obj);
        } else if (obj instanceof String) {
            return new StructuredMessage((String)obj);
        } else if (obj instanceof JSONArray) {
            JSONArray a = (JSONArray)obj;
            msg = StructuredMessage.newList();
            for (int i = 0; i < a.length(); i++) {
                msg.add(parseObject(a.get(i)));
            }
            return msg;
        } else if (obj instanceof JSONObject) {
            JSONObject jsObj = (JSONObject)obj;
            Iterator<String> iter = (Iterator<String>)jsObj.keys();
            msg = StructuredMessage.newMap();
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
