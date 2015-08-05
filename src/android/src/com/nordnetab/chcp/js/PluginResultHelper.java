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
 *
 *
 */
public class PluginResultHelper {

    private static class JsAction {
        public static final String UPDATE_INSTALLED = "update_installed";
        public static final String UPDATE_INSTALLATION_ERROR = "installation_error";
        public static final String NOTHING_TO_INSTALL = "nothing_to_install";
        public static final String UPDATE_IS_LOADED = "update_load_success";
        public static final String UPDATE_LOAD_ERROR = "update_load_error";
        public static final String NOTHING_TO_UPDATE = "nothing_to_update";
        public static final String LOCAL_DEV_INIT = "local_dev_init";
    }

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

    public static PluginResult getResultForInstallationSuccess() {
        JsonNodeFactory factory = JsonNodeFactory.instance;

        ObjectNode dataContent = factory.objectNode();
        dataContent.set("status", factory.numberNode(1));

        return getResult(JsAction.UPDATE_INSTALLED, dataContent, null);
    }

    public static PluginResult getResultForInstallationError(UpdatesInstaller.Error error) {
        JsonNode errorNode = createErrorNode(error.getErrorCode(), error.getErrorDescription());

        return getResult(JsAction.UPDATE_INSTALLATION_ERROR, null, errorNode);
    }

    public static PluginResult getResultForNothingToInstall() {
        JsonNodeFactory factory = JsonNodeFactory.instance;

        ObjectNode dataContent = factory.objectNode();
        dataContent.set("status", factory.numberNode(0));

        return getResult(JsAction.NOTHING_TO_INSTALL, dataContent, null);
    }

    public static PluginResult getResultForUpdateLoadSuccess() {
        JsonNodeFactory factory = JsonNodeFactory.instance;

        ObjectNode dataContent = factory.objectNode();
        dataContent.set("status", factory.numberNode(1));

        return getResult(JsAction.UPDATE_IS_LOADED, dataContent, null);
    }

    public static PluginResult getResultForUpdateLoadError(UpdatesLoader.ErrorType errorType) {
        JsonNode errorNode = createErrorNode(errorType.getErrorCode(), errorType.getErrorDescription());

        return getResult(JsAction.UPDATE_LOAD_ERROR, null, errorNode);
    }

    public static PluginResult getResultForNothingToUpdate() {
        JsonNodeFactory factory = JsonNodeFactory.instance;

        ObjectNode dataContent = factory.objectNode();
        dataContent.set("status", factory.numberNode(0));

        return getResult(JsAction.NOTHING_TO_UPDATE, dataContent, null);
    }

    public static PluginResult getLocalDevModeInitAction(String url) {
        JsonNodeFactory factory = JsonNodeFactory.instance;

        ObjectNode dataNode = factory.objectNode();
        dataNode.set("local_server_url", factory.textNode(url));

        return getResult(JsAction.LOCAL_DEV_INIT, dataNode, null);
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

        resultObject.set(JsParams.General.ACTION, factory.textNode(action));
        resultObject.set(JsParams.General.DATA, data);
        resultObject.set(JsParams.General.ERROR, error);

        return new PluginResult(PluginResult.Status.OK, resultObject.toString());
    }

}
