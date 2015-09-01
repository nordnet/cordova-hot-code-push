(function() {

  module.exports = {
    init: init,
    buildConfigurationBasedOnConsoleOptions: buildConfigurationBasedOnConsoleOptions,
    isBuildingForRelease: isBuildingForRelease,
    getBuildOptionsFromConfig: getBuildOptionsFromConfig,
    getLocalDevBuildOptions: getLocalDevBuildOptions
  };

  var fs = require('fs'),
      path = require('path'),
      chcpLocalDevConfig = require('./chcpLocalDevConfig.js'),
      cordovaContext,
      chcpEnvFilePath,
      chcpBuildOptionsFilePath;

  // region Public API

  function init(context) {
    cordovaContext = context;
    chcpEnvFilePath = path.join(cordovaContext.opts.projectRoot,'.chcpenv'),
    chcpBuildOptionsFilePath = path.join(cordovaContext.opts.projectRoot,'chcpbuild.options');
  }

  function isBuildingForRelease() {
    var isRelease = false;
    cordovaContext.opts.options.some(function(value){
      if (value === '--release') {
        isRelease = true;
        return true;
      } else {
        return false;
      }
    });

    return isRelease;
  };

  // options from chcpbuild.options
  function getBuildOptionsFromConfig() {
    return readBuildConfig();
  };

  // options for localdev
  function getLocalDevBuildOptions() {
    var environmentConfig = readEnvironmentConfig();

    return chcpLocalDevConfig.load(environmentConfig);
  };

  function buildConfigurationBasedOnConsoleOptions() {
    var buildOption = null;

    var chcpBuildOptions = getBuildOptionsFromConfig();
    if (chcpBuildOptions == null) {
      return null;
    }

    console.log('Supported configurations are:');
    console.log(JSON.stringify(chcpBuildOptions, null, 2));

    // get build option depending on the args from console
    var consoleOpts = cordovaContext.opts.options;
    consoleOpts.some(function(value) {
      if (chcpBuildOptions.hasOwnProperty(value)) {
        buildOption = chcpBuildOptions[value];
        return true;
      } else {
        return false;
      }
    });

    return buildOption;
  }

  // endregion

  // region Private API

  function readObjectFromFile(filePath) {
    var objData = null;
    try {
      var data = fs.readFileSync(filePath);
      objData = JSON.parse(data, 'utf-8');
    } catch (err) {
    }

    return objData;
  }

  function readEnvironmentConfig() {
    return readObjectFromFile(chcpEnvFilePath);
  };

  function readBuildConfig() {
    return readObjectFromFile(chcpBuildOptionsFilePath);
  };

  // endregion

})();
