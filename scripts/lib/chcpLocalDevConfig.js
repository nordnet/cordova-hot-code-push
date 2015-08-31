(function() {

  module.exports = {
    load: loadDefault
  };

  function loadDefault(chcpEnvConfig) {
    if (chcpEnvConfig == null) {
      return null;
    }

    var localDevModeOptions = prepareLocalDevModeOptions(chcpEnvConfig);

    return {
      'config-file': chcpEnvConfig.config_url,
      'local-development': localDevModeOptions
    };
  }

  function prepareLocalDevModeOptions(chcpEnvConfig) {
    return {
      enabled: true
    };
  }

})();
