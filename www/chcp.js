
var exec = require('cordova/exec');
var channel = require('cordova/channel');

channel.onCordovaReady.subscribe(function() {
    exec(nativeCallback, null, 'HotCodePush', 'init', []);
});

function processMessageFromNative(msg) {
    try {
        return JSON.parse(msg);
    } catch (err) {
        console.log(err);
        return null;
    }
}

function nativeCallback(msg) {
    console.log(msg);

    var resultObj = processMessageFromNative(msg);
    if (resultObj == null) {
        return;
    }

    switch (resultObj.action) {
        case 'update_load_error':
            processUpdateLoadErrorAction(resultObj.params);
            break;
        case 'nothing_to_update':
            processNothingToUpdateAction(resultObj.params);
            break;
        case 'update_load_success':
            processUpdateIsReadyForInstallationAction(resultObj.params);
            break;

        case 'installation_error':
            processInstallationErrorAction(resultObj.params);
            break;
        case 'nothing_to_install':
            processNothingToInstallAction(resultObj.params);
            break;
        case 'update_installed':
            processUpdateInstalledAction(resultObj.params);
            break;
        case 'reload_page':
            processPageReloadAction(resultObj.params);
            break;

        default:
            console.log("Unsupported action: " + resultObj.action);
    }
}

function processPageReloadAction(actionParams) {
    location.replace(actionParams.url);
}

function processUpdateInstalledAction(actionParams) {
}

function processUpdateIsReadyForInstallationAction(actionParams) {

}

function processNothingToUpdateAction(actionParams) {

}

function processUpdateLoadErrorAction(actionParams) {

}

function processNothingToInstallAction(actionParams) {

}

function processInstallationErrorAction(actionParams) {

}

var chcp = {

    __javaMethod: {
        FETCH_UPDATE: 'fetchUpdate',
        INSTALL_UPDATE: 'installUpdate',
    },

    __PLUGIN_NAME: 'HotCodePush',

    fetchUpdate: function(callback) {
        var innerCallback = function(msg) {
            var resultObj = processMessageFromNative(msg);
            var error = null;
            var data = null;
            if (resultObj.params.hasOwnProperty('error')) {
                error = resultObj.params.error;
            } else {
                data = resultObj.params.data;
            }

            if (callback != null) {
                callback(error, data);
            }
        };

        exec(innerCallback, null, this.__PLUGIN_NAME, this.__javaMethod.FETCH_UPDATE, []);
    },

    installUpdate: function(callback) {
        var innerCallback = function(msg) {
            var resultObj = processMessageFromNative(msg);
            var error = null;
            var data = null;
            if (resultObj.params.hasOwnProperty('error')) {
                error = resultObj.params.error;
            } else {
                data = resultObj.params.data;
            }

            if (callback != null) {
                callback(error, data);
            }
        };

        exec(innerCallback, null, this.__PLUGIN_NAME, this.__javaMethod.INSTALL_UPDATE, []);
    }
};

module.exports = chcp;

