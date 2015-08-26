
  var exec = require('cordova/exec');
  var channel = require('cordova/channel');

  channel.onCordovaReady.subscribe(function() {
    console.log('Sending callback to native side');
    exec(nativeCallback, null, 'HotCodePush', 'jsInitPlugin', []);
  });

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
    } catch (err) {
    }

    return {action: actionId, error: errorContent, data: dataContent};
  }

  function nativeCallback(msg) {
    var resultObj = processMessageFromNative(msg);
    if (resultObj.action == null) {
      console.log('Action is not provided, skipping');
      return;
    }

    switch (resultObj.action) {
      case 'chcp_updateLoadFailed':
      case 'chcp_nothingToUpdate':
      case 'chcp_updateIsReadyToInstall':
      case 'chcp_updateInstallFailed':
      case 'chcp_updateInstalled':
      case 'chcp_nothingToInstall': {
        processEventFromNative(resultObj);
        break;
      }

      default:
        console.warn('Unsupported action: '  + resultObj.action);
    }
  }

  // region Update/Install events

  function processEventFromNative(nativeMessage) {
    var params = {};
    if (nativeMessage.error != null) {
      params.error = nativeMessage.error;
    }

    var chcpEvent = new CustomEvent(nativeMessage.action, {'detail': params});
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

    __nativeMethod: {
      FETCH_UPDATE: 'jsFetchUpdate',
      INSTALL_UPDATE: 'jsInstallUpdate',
      CONFIGURE: 'jsConfigure',
      REQUEST_APP_UPDATE: 'jsRequestAppUpdate'
    },

    __PLUGIN_NAME: 'HotCodePush',

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

      exec(innerCallback, null, this.__PLUGIN_NAME, this.__nativeMethod.CONFIGURE, [options]);
    },

    requestApplicationUpdate: function(message, onStoreOpenCallback, onUserDismissedDialogCallback) {
      if (message == undefined || message.length == 0) {
        return;
      }

      var onSuccessInnerCallback = function(msg) {
        onStoreOpenCallback();
      };

      var onFailureInnerCallback = function(msg) {
        onUserDismissedDialogCallback();
      };

      exec(onSuccessInnerCallback, onFailureInnerCallback, this.__PLUGIN_NAME, this.__nativeMethod.REQUEST_APP_UPDATE, [message]);
    },

    fetchUpdate: function(callback) {
      var innerCallback = function(msg) {
        var resultObj = processMessageFromNative(msg);
        if (callback !== undefined && callback != null) {
          callback(resultObj.error, resultObj.data);
        }
      };

      exec(innerCallback, null, this.__PLUGIN_NAME, this.__nativeMethod.FETCH_UPDATE, []);
    },

    installUpdate: function(callback) {
      var innerCallback = function(msg) {
        var resultObj = processMessageFromNative(msg);
        if (callback != undefined && callback != null) {
          callback(resultObj.error);
        }
      };

      exec(innerCallback, null, this.__PLUGIN_NAME, this.__nativeMethod.INSTALL_UPDATE, []);
    }
  };

  module.exports = chcp;
