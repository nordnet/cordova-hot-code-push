package com.nordnetab.chcp.main.js;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nordnetab.chcp.main.events.IPluginEvent;
import com.nordnetab.chcp.main.model.ChcpError;

import org.apache.cordova.PluginResult;

import java.util.Map;
import java.util.Set;

/**
 * Created by Nikolay Demyankov on 29.07.15.
 *
 * Helper class to generate proper instance of PluginResult, which is send to the web side.
 *
 * @see PluginResult
 */
public class PluginResultHelper {

    // TODO: migrate to JSONObject

    // keywords for JSON string, that is send to JavaScript side
    private static class JsParams {
        private static class General {
            public static final String ACTION = "action";
            public static final String ERROR = "error";
            public static final String DATA = "data";
        }

        private static class Error {
            public static final String CODE = "code";
            public static final String DESCRIPTION = "description";
        }
    }

    /**
     * Create PluginResult instance from event.
     *
     * @param event hot-code-push plugin event
     *
     * @return cordova's plugin result
     * @see PluginResult
     * @see IPluginEvent
     */
    public static PluginResult pluginResultFromEvent(IPluginEvent event) {
        final String actionName = event.name();
        final Map<String, Object> data = event.data();
        final ChcpError error = event.error();

        return createPluginResult(actionName, data, error);
    }

    public static PluginResult createPluginResult(String actionName, Map<String, Object> data, ChcpError error) {
        JsonNode errorNode = null;
        JsonNode dataNode = null;

        if (error != null) {
            errorNode = createErrorNode(error.getErrorCode(), error.getErrorDescription());
        }

        if (data != null && data.size() > 0) {
            dataNode = createDataNode(data);
        }

        return getResult(actionName, dataNode, errorNode);
    }

    // region Private API

    private static JsonNode createDataNode(Map<String, Object> data) {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode dataNode = factory.objectNode();

        Set<Map.Entry<String, Object>> dataSet = data.entrySet();
        for (Map.Entry<String, Object> entry : dataSet) {
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }

            dataNode.set(entry.getKey(), factory.textNode(value.toString()));
        }

        return dataNode;
    }

    private static JsonNode createErrorNode(int errorCode, String errorDescription) {
        JsonNodeFactory factory = JsonNodeFactory.instance;

        ObjectNode errorData = factory.objectNode();
        errorData.set(JsParams.Error.CODE, factory.numberNode(errorCode));
        errorData.set(JsParams.Error.DESCRIPTION, factory.textNode(errorDescription));

        return errorData;
    }

    private static PluginResult getResult(String action, JsonNode data, JsonNode error) {
        JsonNodeFactory factory = JsonNodeFactory.instance;

        ObjectNode resultObject = factory.objectNode();
        if (action != null) {
            resultObject.set(JsParams.General.ACTION, factory.textNode(action));
        }

        if (data != null) {
            resultObject.set(JsParams.General.DATA, data);
        }

        if (error != null) {
            resultObject.set(JsParams.General.ERROR, error);
        }

        return new PluginResult(PluginResult.Status.OK, resultObject.toString());
    }

    // endregion

}
