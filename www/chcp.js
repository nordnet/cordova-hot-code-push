

var exec = require('cordova/exec'),
  channel = require('cordova/channel'),

  // Reference name for the plugin
  PLUGIN_NAME = 'HotCodePush',

  // Plugin methods on the native side that can be called from JavaScript
  pluginNativeMethod = {
    INITIALIZE: 'jsInitPlugin',
    FETCH_UPDATE: 'jsFetchUpdate',
    INSTALL_UPDATE: 'jsInstallUpdate',
    CONFIGURE: 'jsConfigure',
    REQUEST_APP_UPDATE: 'jsRequestAppUpdate',
    IS_UPDATE_AVAILABLE_FOR_INSTALLATION: 'jsIsUpdateAvailableForInstallation',
    GET_INFO: 'jsGetVersionInfo'
  };

// Called when Cordova is ready for work.
// Here we will send default callback to the native side through which it will send to us different events.
channel.onCordovaReady.subscribe(function() {
  ensureCustomEventExists();
  exec(nativeCallback, null, PLUGIN_NAME, pluginNativeMethod.INITIALIZE, []);
});

/**
 * Method is called when native side sends us different events.
 * Those events can be about update download/installation process.
 *
 * @param {String} msg - JSON formatted string with call arguments
 */
function nativeCallback(msg) {
  // parse call arguments
  var resultObj = processMessageFromNative(msg);
  if (resultObj.action == null) {
    console.log('Action is not provided, skipping');
    return;
  }

  broadcastEventFromNative(resultObj);
}

/**
 * Parse arguments that were sent from the native side.
 * Arguments are a JSON string of type:
 * { action: "action identifier", error: {...error data...}, data: {...some additional data..} }
 * Some parameters may not exist, but the resulting object will have them all.
 *
 * @param {String} msg - arguments as JSON string
 * @return {Object} parsed string
 */
function processMessageFromNative(msg) {
  var errorContent = null,
    dataContent = null,
    actionId = null;

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

function callNativeMethod(methodName, options, callback) {
  var innerCallback = function(msg) {
    var resultObj = processMessageFromNative(msg);
    if (callback !== undefined && callback != null) {
      callback(resultObj.error, resultObj.data);
    }
  };

  var sendArgs = [];
  if (options !== null && options !== undefined) {
    sendArgs.push(options);
  }

  exec(innerCallback, null, PLUGIN_NAME, methodName, sendArgs);
}

// region Update/Install events

/*
 * Polyfill for adding CustomEvent which may not exist on older versions of Android.
 * See https://developer.mozilla.org/fr/docs/Web/API/CustomEvent for more details.
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

/**
 * Broadcast event that was received from the native side.
 *
 * @param {Object} arguments, received from the native side
 */
function broadcastEventFromNative(nativeMessage) {
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

/**
 * Public module of the plugin.
 * May be used by developers to send commands to the plugin.
 */
var chcp = {

  // Plugin error codes
  error: {
    NOTHING_TO_INSTALL: 1,
    NOTHING_TO_UPDATE: 2,

    FAILED_TO_DOWNLOAD_APPLICATION_CONFIG: -1,
    APPLICATION_BUILD_VERSION_TOO_LOW: -2,
    FAILED_TO_DOWNLOAD_CONTENT_MANIFEST: -3,
    FAILED_TO_DOWNLOAD_UPDATE_FILES: -4,
    FAILED_TO_MOVE_LOADED_FILES_TO_INSTALLATION_FOLDER: -5,
    UPDATE_IS_INVALID: -6,
    FAILED_TO_COPY_FILES_FROM_PREVIOUS_RELEASE: -7,
    FAILED_TO_COPY_NEW_CONTENT_FILES: -8,
    LOCAL_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND: -9,
    LOCAL_VERSION_OF_MANIFEST_NOT_FOUND: -10,
    LOADED_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND: -11,
    LOADED_VERSION_OF_MANIFEST_NOT_FOUND: -12,
    FAILED_TO_INSTALL_ASSETS_ON_EXTERNAL_STORAGE: -13,
    CANT_INSTALL_WHILE_DOWNLOAD_IN_PROGRESS: -14,
    CANT_DOWNLOAD_UPDATE_WHILE_INSTALLATION_IN_PROGRESS: -15,
    INSTALLATION_ALREADY_IN_PROGRESS: -16,
    DOWNLOAD_ALREADY_IN_PROGRESS: -17,
    ASSETS_FOLDER_IN_NOT_YET_INSTALLED: -18,
    NEW_APPLICATION_CONFIG_IS_INVALID: -19
  },

  // Plugin events
  event: {
    BEFORE_ASSETS_INSTALLATION: 'chcp_beforeAssetsInstalledOnExternalStorage',
    ASSETS_INSTALLATION_FAILED: 'chcp_assetsInstallationError',
    ASSETS_INSTALLED: 'chcp_assetsInstalledOnExternalStorage',

    NOTHING_TO_UPDATE: 'chcp_nothingToUpdate',
    UPDATE_LOAD_FAILED: 'chcp_updateLoadFailed',
    UPDATE_IS_READY_TO_INSTALL: 'chcp_updateIsReadyToInstall',

    BEFORE_UPDATE_INSTALLATION: 'chcp_beforeInstall',
    UPDATE_INSTALLATION_FAILED: 'chcp_updateInstallFailed',
    UPDATE_INSTALLED: 'chcp_updateInstalled',
    NOTHING_TO_INSTALL: 'chcp_nothingToInstall'
  },

  /**
   * DEPRECATED! WILL BE REMOVED EVENTUALLY!
   *
   * If you want to set config-url - use chcp.fetchUpdate(callback, options).
   * If you want to set auto-download/auto-install preference - do it in config.xml instead of this method.
   *
   * Set plugin options.
   * Options are send to the native side.
   * As soon as they are processed - callback is called.
   *
   * @param {Object} options - options to set
   * @param {Callback(error)} callback - callback to call when options are set
   */
  configure: function(options, callback) {
    if (options === undefined || options == null) {
      return;
    }

    callNativeMethod(pluginNativeMethod.CONFIGURE, options, callback);
  },

  /**
   * Show dialog with the request to update application through the Store (App Store or Google Play).
   * For that purpose you can use any other cordova library, this is just a small helper method.
   *
   * @param {String} message - message to show in the dialog
   * @param {Callback()} onStoreOpenCallback - called when user redirects to the Store
   * @param {Callback()} onUserDismissedDialogCallback - called when user declines to go to the Store
   */
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

  /**
   * Check if any new content is available on the server and download it.
   * Usually this is done automatically by the plugin, but can be triggered at any time from the web page.
   *
   * @param {Callback(error, data)} callback - called when native side finished update process
   * @param {Object} options - additional options, such as "config-url" and additional http headers.
   */
  fetchUpdate: function(callback, options) {
    callNativeMethod(pluginNativeMethod.FETCH_UPDATE, options, callback);
  },

  /**
   * Install update if there is anything to install.
   *
   * @param {Callback(error)} callback - called when native side finishes installation process
   */
  installUpdate: function(callback) {
    callNativeMethod(pluginNativeMethod.INSTALL_UPDATE, null, callback);
  },

  /**
   * Check if update was loaded and ready to be installed.
   * If update was loaded and can be installed - "data" property of the callback will contain the name of the current version and the name of the new
   * version.
   * If not - "error" will contain code chcp.error.NOTHING_TO_INSTALL.
   *
   * @param {Callback(error, data)} callback - called, when information is retrieved from the native side.
   */
  isUpdateAvailableForInstallation: function(callback) {
    callNativeMethod(pluginNativeMethod.IS_UPDATE_AVAILABLE_FOR_INSTALLATION, null, callback);
  },

  /**
   * Get information about the current version like current release version, app build version and so on.
   * The "data" property of the callback will contain all the information.
   *
   * @param {Callback(error, data)} callback - called, when information is retrieved from the native side.
   */
  getVersionInfo: function(callback) {
    callNativeMethod(pluginNativeMethod.GET_INFO, null, callback);
  }
};

module.exports = chcp;
