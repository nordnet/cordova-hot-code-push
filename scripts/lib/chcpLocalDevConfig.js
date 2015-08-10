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
      config_url: chcpEnvConfig.config_url,
      local_dev_mode: localDevModeOptions
    };
  }

  function prepareLocalDevModeOptions(chcpEnvConfig) {
    return {
      enabled: true
    };
  }

})();
