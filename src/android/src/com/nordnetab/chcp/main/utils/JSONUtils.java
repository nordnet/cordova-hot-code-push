package com.nordnetab.chcp.main.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Nikolay Demyankov on 07.06.16.
 * <p/>
 * Helper class to work with JSON objects.
 */
public class JSONUtils {

    /**
     * Convert json object into simple string-string map.
     * Non-string values are ignored.
     *
     * @param object json object to convert
     * @return resulting map
     * @throws JSONException when json object is invalid
     */
    public static Map<String, String> toFlatStringMap(final JSONObject object) throws JSONException {
        final Map<String, String> map = new HashMap<String, String>();
        final Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            final String key = keysItr.next();
            final Object value = object.get(key);
            if (!(value instanceof String)) {
                continue;
            }

            map.put(key, (String) value);
        }

        return map;
    }

}
