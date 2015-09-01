

var exec = require('cordova/exec'),
  channel = require('cordova/channel'),
  PLUGIN_NAME = 'HotCodePush',
  pluginNativeMethod = {
    INITIALIZE: 'jsInitPlugin',
    FETCH_UPDATE: 'jsFetchUpdate',
    INSTALL_UPDATE: 'jsInstallUpdate',
    CONFIGURE: 'jsConfigure',
    REQUEST_APP_UPDATE: 'jsRequestAppUpdate'
  },
  pluginEvents = {
    'chcp_assetsInstallationError': true,
    'chcp_assetsInstalledOnExternalStorage': true,
    'chcp_updateLoadFailed': true,
    'chcp_nothingToUpdate': true,
    'chcp_updateIsReadyToInstall': true,
    'chcp_updateInstallFailed': true,
    'chcp_updateInstalled': true,
    'chcp_nothingToInstall': true
  };

channel.onCordovaReady.subscribe(function() {
  ensureCustomEventExists();
  exec(nativeCallback, null, PLUGIN_NAME, pluginNativeMethod.INITIALIZE, []);
});

function nativeCallback(msg) {
  var resultObj = processMessageFromNative(msg);
  if (resultObj.action == null) {
    console.log('Action is not provided, skipping');
    return;
  }

  var eventIsEnabled = pluginEvents[resultObj.action];
  if (eventIsEnabled == undefined || eventIsEnabled == null) {
    console.warn('Unsupported action: ' + resultObj.action);
    return;
  }

  if (eventIsEnabled) {
    processEventFromNative(resultObj);
  }
}

function processMessageFromNative(msg) {
  var errorContent = null;
  var dataContent = null;
  var actionId = null;

  try {
    var resultObj = JSON.parse(msg);
    if (resultObj.hasOwnProperty('error')) {
      errorContent = resultObj.error;
    }
    if (resultObj.hasOwnProperty('data')) {
      dataContent = resultObj.data;
    }
    if (resultObj.hasOwnProperty('action')) {
      actionId = resultObj.action;
    }
  } catch (err) {}

  return {
    action: actionId,
    error: errorContent,
    data: dataContent
  };
}

// region Update/Install events

/*
 * Polyfill for adding CustomEvent
 * see : https://developer.mozilla.org/fr/docs/Web/API/CustomEvent
 * It doesn't exist on older versions of Android.
 */
function ensureCustomEventExists() {
  // Create only if it doesn't exist
  if (window.CustomEvent) {
    return;
  }

  var CustomEvent = function(event, params) {
    params = params || {
      bubbles: false,
      cancelable: false,
      detail: undefined
    };
    var evt = document.createEvent('CustomEvent');
    evt.initCustomEvent(event, params.bubbles, params.cancelable, params.detail);
    return evt;
  };

  CustomEvent.prototype = window.Event.prototype;
  window.CustomEvent = CustomEvent;
}

function processEventFromNative(nativeMessage) {
  var params = {};
  if (nativeMessage.error != null) {
    params.error = nativeMessage.error;
  }

  var chcpEvent = new CustomEvent(nativeMessage.action, {
    'detail': params
  });
  document.dispatchEvent(chcpEvent);
}

// endregion

/*
pluginOptions = {
  config_url: 'some_url',
  allow_auto_install: true,
  allow_auto_download: true
}
*/

var chcp = {

  configure: function(options, callback) {
    if (options === undefined || options == null) {
      return;
    }

    var innerCallback = function(msg) {
      var resultObj = processMessageFromNative(msg);
      if (callback !== undefined && callback != null) {
        callback(resultObj.error);
      }
    };

    exec(innerCallback, null, PLUGIN_NAME, pluginNativeMethod.CONFIGURE, [options]);
  },

  requestApplicationUpdate: function(message, onStoreOpenCallback, onUserDismissedDialogCallback) {
    if (message == undefined || message.length == 0) {
      return;
    }

    var onSuccessInnerCallback = function(msg) {
      if (onStoreOpenCallback) {
        onStoreOpenCallback();
      }
    };

    var onFailureInnerCallback = function(msg) {
      if (onUserDismissedDialogCallback) {
        onUserDismissedDialogCallback();
      }
    };

    exec(onSuccessInnerCallback, onFailureInnerCallback, PLUGIN_NAME, pluginNativeMethod.REQUEST_APP_UPDATE, [message]);
  },

  fetchUpdate: function(callback) {
    var innerCallback = function(msg) {
      var resultObj = processMessageFromNative(msg);
      if (callback !== undefined && callback != null) {
        callback(resultObj.error, resultObj.data);
      }
    };

    exec(innerCallback, null, PLUGIN_NAME, pluginNativeMethod.FETCH_UPDATE, []);
  },

  installUpdate: function(callback) {
    var innerCallback = function(msg) {
      var resultObj = processMessageFromNative(msg);
      if (callback != undefined && callback != null) {
        callback(resultObj.error);
      }
    };

    exec(innerCallback, null, PLUGIN_NAME, pluginNativeMethod.INSTALL_UPDATE, []);
  }
};

module.exports = chcp;
