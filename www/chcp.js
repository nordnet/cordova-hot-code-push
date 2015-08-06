
// TODO: config.xml should be something like this:
// <chcp>
//   <config-file url="" />
//   <local-development enabled="true">
//     <inject-js-code>some inline js code</inject-js-code>
//     <inject-js-script path="/socket.io/socket.io.js" />
//     <inject-js-script path="/connect/assets/liveupdate.js" />
//   </local-development>
// </chcp>

var exec = require('cordova/exec');
var channel = require('cordova/channel');

channel.onCordovaReady.subscribe(function() {
  console.log('Sending callback to native side');
  exec(nativeCallback, null, 'HotCodePush', 'init', []);
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
  console.log(msg);

  var resultObj = processMessageFromNative(msg);
  if (resultObj.action == null) {
    console.log('Action is not provided, skipping');
    return;
  }

  switch (resultObj.action) {
    case 'update_load_error':
      processUpdateLoadErrorAction(resultObj);
      break;
    case 'nothing_to_update':
      processNothingToUpdateAction(resultObj);
      break;
    case 'update_load_success':
      processUpdateIsReadyForInstallationAction(resultObj);
      break;

    case 'installation_error':
      processInstallationErrorAction(resultObj);
      break;
    case 'nothing_to_install':
      processNothingToInstallAction(resultObj);
      break;
    case 'update_installed':
      processUpdateInstalledAction(resultObj);
      break;

    case 'local_dev_init':
      initForLocalDev(resultObj);
      break;

    default:
      console.log('Unsupported action: '  + resultObj.action);
  }
}

// region Update/Install events

function processUpdateInstalledAction(nativeMessage) {
  var event = new CustomEvent('chcp_updateInstalled', {});
  document.dispatchEvent(event);
}

function processUpdateIsReadyForInstallationAction(nativeMessage) {
  var event = new CustomEvent('chcp_updateIsReadyToInstall', {});
  document.dispatchEvent(event);
}

function processNothingToUpdateAction(nativeMessage) {
  var event = new CustomEvent('chcp_nothingToUpdate', {});
  document.dispatchEvent(event);
}

function processUpdateLoadErrorAction(nativeMessage) {
  var event = new CustomEvent('chcp_updateLoadFailed', {'error': nativeMessage.error});
  document.dispatchEvent(event);
}

function processNothingToInstallAction(nativeMessage) {
  var event = new CustomEvent('chcp_nothingToUpdate', {});
  document.dispatchEvent(event);
}

function processInstallationErrorAction(nativeMessage) {
  var event = new CustomEvent('chcp_updateInstallFailed', {'error': nativeMessage.error});
  document.dispatchEvent(event);
}

// endregion

// region Hooks for local development

function injectScript(scriptFilePath, callback) {
  var script   = document.createElement('script');
  script.type  = 'text/javascript';
  script.src   = scriptFilePath;
  script.onload = function() {
    script.onload = null;
    if (callback) {
      callback();
    }
  };
  document.body.appendChild(script);
}

function injectRowScript(scriptData) {
  var script = document.createElement('script');
  script.type = 'text/javascript';
  script.text = scriptData;
  document.body.appendChild(script);
}

var localDevScripts = ['/socket.io/socket.io.js', '/connect/assets/liveupdate.js'];

function injectLocalDevScripts(localServerUrl) {
  if (localDevScripts.length == 0) {
    return;
  }

  var scriptPath = localServerUrl + localDevScripts.shift();
  injectScript(scriptPath, function() {
    injectLocalDevScripts(localServerUrl);
  });
}

function initForLocalDev(nativeMessage) {
  var localServerUrl = nativeMessage.data.local_server_url;

  injectRowScript('window.chcpDevServer="' + localServerUrl + '";');

  // var socketScript = localServerUrl + '/socket.io/socket.io.js';
  // injectScript(socketScript, function() {
  //   var liveUpdateScript = localServerUrl + '/connect/assets/liveupdate.js';
  //   injectScript(liveUpdateScript);
  // });

  injectLocalDevScripts(localServerUrl);
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

  __javaMethod: {
    FETCH_UPDATE: 'fetchUpdate',
    INSTALL_UPDATE: 'installUpdate',
    CONFIGURE: 'configure'
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

    exec(innerCallback, null, this.__PLUGIN_NAME, this.__javaMethod.CONFIGURE, [options]);
  },

  // TODO: add support for chcp.json as parameter
  fetchUpdate: function(config, callback) {
    var innerCallback = function(msg) {
      var resultObj = processMessageFromNative(msg);
      if (callback !== undefined && callback != null) {
        callback(resultObj.error, resultObj.data);
      }
    };

    exec(innerCallback, null, this.__PLUGIN_NAME, this.__javaMethod.FETCH_UPDATE, []);
  },

  installUpdate: function(callback) {
    var innerCallback = function(msg) {
      var resultObj = processMessageFromNative(msg);
      if (callback != undefined && callback != null) {
        callback(resultObj.error);
      }
    };

    exec(innerCallback, null, this.__PLUGIN_NAME, this.__javaMethod.INSTALL_UPDATE, []);
  }
};

module.exports = chcp;
