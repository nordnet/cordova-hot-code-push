(function() {

  var jsScripts = ['/socket.io/socket.io.js', '/connect/assets/liveupdate.js'];

  module.exports = {
    load: loadDefault
  };

  function loadDefault(chcpEnvConfig) {
    if (chcpEnvConfig == null) {
      return null;
    }

    var localDevModeOptions = prepareLocalDevModeOptions(chcpEnvConfig);

    return {
      config_url: chcpEnvConfig.config_url,
      local_dev_mode: localDevModeOptions
    };
  }

  function prepareLocalDevModeOptions(chcpEnvConfig) {
    var jsInlineCodeInjectionList = [
      {
        code: 'window.chcpDevServer="' + chcpEnvConfig.content_url + '";'
      }
    ];

    var jsScriptsInjectionList = [];

    jsScripts.forEach(function(scriptPath) {
      var fullPath = chcpEnvConfig.content_url + scriptPath;
      jsScriptsInjectionList.push({
        path: fullPath
      });
    });

    return {
      enabled: true,
      inject_js: {
        code: jsInlineCodeInjectionList,
        scripts: jsScriptsInjectionList
      }
    };
  }

})();
