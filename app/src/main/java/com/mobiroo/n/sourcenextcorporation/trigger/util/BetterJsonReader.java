package com.mobiroo.n.sourcenextcorporation.trigger.util;

import android.os.Build;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Reader;

public class BetterJsonReader {
    public static void readJSONObjectArray(Reader reader, JSONObjectArrayHandler handler) throws Exception {
        JsonReader jsonReader = new JsonReader(reader);
        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            handler.onArrayItem(readJSONObject(jsonReader));
        }
        jsonReader.endArray();
    }

    private static JSONObject readJSONObject(JsonReader reader) throws Exception {
        JSONObject object = new JSONObject();

        reader.beginObject();

        while (reader.hasNext()) {
            JsonToken token = reader.peek();
            if (token == JsonToken.END_OBJECT) {
                break;
            } else if (token == JsonToken.NAME) {
                String name = reader.nextName();
                object.put(name, readJSONValue(reader));
            } else {
                throw new Exception("Unexpected token: " + token);
            }
        }

        reader.endObject();

        return object;
    }

    public static JSONArray readJSONArray(JsonReader reader) throws Exception {
        JSONArray array = new JSONArray();

        reader.beginArray();
        while (reader.hasNext()) {
            if (reader.peek() == JsonToken.END_ARRAY) {
                break;
            } else {
                array.put(readJSONValue(reader));
            }
        }
        reader.endArray();

        return array;
    }

    public static Object readJSONValue(JsonReader reader) throws Exception {
        JsonToken token = reader.peek();
        if (token == JsonToken.BEGIN_OBJECT) {
            return readJSONObject(reader);

        } else if (token == JsonToken.BEGIN_ARRAY) {
            return readJSONArray(reader);

        } else if (token == JsonToken.BOOLEAN) {
            return reader.nextBoolean();

        } else if (token == JsonToken.NULL) {
            reader.nextNull();
            return JSONObject.NULL;

        } else if (token == JsonToken.NUMBER) {
            return reader.nextLong();

        } else if (token == JsonToken.STRING) {
            return reader.nextString();

        } else {
            throw new Exception("Unexpected token: " + token);
        }
    }

    public static boolean isSupported() {
        return (Build.VERSION.SDK_INT >= 11);
    }

    public abstract static class JSONObjectArrayHandler {
        public abstract void onArrayItem(JSONObject object) throws Exception;
    }
}