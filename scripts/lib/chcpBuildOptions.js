/**
Module helps to generate building options for plugin.
Those options then injected into platform-specific config.xml.
*/
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

  /**
   * Initialize module.
   * Must be called before calling other methods.
   *
   * @param {Object} context - cordova context instance
   */
  function init(context) {
    cordovaContext = context;
    chcpEnvFilePath = path.join(cordovaContext.opts.projectRoot, '.chcpenv');
    chcpBuildOptionsFilePath = path.join(cordovaContext.opts.projectRoot, 'chcpbuild.options');
  }

  /**
   * Check if we are building for release.
   * We determine this by searching for --release options in console arguments.
   *
   * @return {boolean} true if we are building for release; false - otherwise
   */
  function isBuildingForRelease() {
    var isRelease = false;
    cordovaContext.opts.options.some(function(value) {
      if (value === '--release') {
        isRelease = true;
        return true;
      } else {
        return false;
      }
    });

    return isRelease;
  };

  /**
   * Read options, listed in chcpbuild.options file.
   *
   * @return {Object} options from chcpbuild.options file
   */
  function getBuildOptionsFromConfig() {
    return readBuildConfig();
  };

  // options for localdev
  /**
   * Construct local development options based on the .chcpenv file.
   *
   * @return {Object} local development options
   */
  function getLocalDevBuildOptions() {
    var environmentConfig = readEnvironmentConfig();

    return chcpLocalDevConfig.load(environmentConfig);
  };

  /**
   * Generate build options depending on the options, provided in console.
   *
   * @return {Object} build options; null - if none are found
   */
  function buildConfigurationBasedOnConsoleOptions() {
    var buildOption = null;

    // load options from the chcpbuild.options file
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
    } catch (err) {}

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
