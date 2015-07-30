package com.nordnetab.chcp.js;

import android.text.TextUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nordnetab.chcp.updater.UpdatesInstaller;
import com.nordnetab.chcp.updater.UpdatesLoader;

import org.apache.cordova.PluginResult;

/**
 * Created by Nikolay Demyankov on 29.07.15.
 */
public class PluginResultHelper {

    public static PluginResult getResultForInstallationSuccess() {
        JsonNodeFactory factory = JsonNodeFactory.instance;

        ObjectNode dataContent = factory.objectNode();
        dataContent.set("status", factory.numberNode(1));

        return getResult("update_installed", dataContent, null);
    }

    public static PluginResult getResultForInstallationError(UpdatesInstaller.Error error) {
        JsonNode errorNode = createErrorNode(error.getErrorCode(), error.getErrorDescription());

        return getResult("installation_error", null, errorNode);
    }

    public static PluginResult getResultForNothingToInstall() {
        JsonNodeFactory factory = JsonNodeFactory.instance;

        ObjectNode dataContent = factory.objectNode();
        dataContent.set("status", factory.numberNode(0));

        return getResult("nothing_to_install", dataContent, null);
    }

    public static PluginResult getResultForUpdateLoadSuccess() {
        JsonNodeFactory factory = JsonNodeFactory.instance;

        ObjectNode dataContent = factory.objectNode();
        dataContent.set("status", factory.numberNode(1));

        return getResult("update_load_success", dataContent, null);
    }

    public static PluginResult getResultForUpdateLoadError(UpdatesLoader.ErrorType errorType) {
        JsonNode errorNode = createErrorNode(errorType.getErrorCode(), errorType.getErrorDescription());

        return getResult("update_load_error", null, errorNode);
    }

    public static PluginResult getResultForNothingToUpdate() {
        JsonNodeFactory factory = JsonNodeFactory.instance;

        ObjectNode dataContent = factory.objectNode();
        dataContent.set("status", factory.numberNode(0));

        return getResult("nothing_to_update", dataContent, null);
    }

    public static PluginResult getReloadPageAction(String url) {
        JsonNodeFactory factory = JsonNodeFactory.instance;

        ObjectNode dataNode = factory.objectNode();
        if (!TextUtils.isEmpty(url)) {
            dataNode.set("url", factory.textNode(url));
        }

        return getResult("reload_page", dataNode, null);
    }

    private static JsonNode createErrorNode(int errorCode, String errorDescription) {
        JsonNodeFactory factory = JsonNodeFactory.instance;

        ObjectNode errorData = factory.objectNode();
        errorData.set("code", factory.numberNode(errorCode));
        errorData.set("description", factory.textNode(errorDescription));

        return errorData;
    }

    private static PluginResult getResult(String action, JsonNode data, JsonNode error) {
        JsonNodeFactory factory = JsonNodeFactory.instance;

        ObjectNode resultObject = factory.objectNode();

        resultObject.set("action", factory.textNode(action));
        resultObject.set("data", data);
        resultObject.set("error", error);

        return new PluginResult(PluginResult.Status.OK, resultObject.toString());
    }

}
