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
        ObjectNode data = factory.objectNode();

        ObjectNode dataContent = factory.objectNode();
        dataContent.set("status", factory.numberNode(1));

        data.set("data", dataContent);

        return getResult("update_installed", data);
    }

    public static PluginResult getResultForInstallationError(UpdatesInstaller.Error error) {
        return getResult("installation_error", createErrorNode(error.getErrorCode(), error.getErrorDescription()));
    }

    public static PluginResult getResultForNothingToInstall() {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode data = factory.objectNode();

        ObjectNode dataContent = factory.objectNode();
        dataContent.set("status", factory.numberNode(0));

        data.set("data", dataContent);

        return getResult("nothing_to_install", data);
    }

    public static PluginResult getResultForUpdateLoadSuccess() {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode data = factory.objectNode();

        ObjectNode dataContent = factory.objectNode();
        dataContent.set("status", factory.numberNode(1));

        data.set("data", dataContent);

        return getResult("update_load_success", data);
    }

    public static PluginResult getResultForUpdateLoadError(UpdatesLoader.ErrorType errorType) {
        return getResult("update_load_error", createErrorNode(errorType.getErrorCode(), errorType.getErrorDescription()));
    }

    public static PluginResult getResultForNothingToUpdate() {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode data = factory.objectNode();

        ObjectNode dataContent = factory.objectNode();
        dataContent.set("status", factory.numberNode(0));

        data.set("data", dataContent);

        return getResult("nothing_to_update", data);
    }

    public static PluginResult getReloadPageAction(String url) {
        JsonNodeFactory factory = JsonNodeFactory.instance;

        ObjectNode dataNode = factory.objectNode();
        if (!TextUtils.isEmpty(url)) {
            dataNode.set("url", factory.textNode(url));
        }

        return getResult("reload_page", dataNode);
    }

    private static JsonNode createErrorNode(int errorCode, String errorDescription) {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode error = factory.objectNode();

        ObjectNode errorData = factory.objectNode();
        errorData.set("code", factory.numberNode(errorCode));
        errorData.set("description", factory.textNode(errorDescription));

        error.set("error", errorData);

        return error;
    }

    private static PluginResult getResult(String action, JsonNode params) {
        JsonNodeFactory factory = JsonNodeFactory.instance;

        ObjectNode resultObject = factory.objectNode();
        resultObject.set("action", factory.textNode(action));
        resultObject.set("params", params);

        return new PluginResult(PluginResult.Status.OK, resultObject.toString());
    }

}
