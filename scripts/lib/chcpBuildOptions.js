/**
Module helps to generate building options for plugin.
Those options then injected into platform-specific config.xml.
*/
(function() {
  var fs = require('fs'),
    path = require('path'),
    chcpLocalDevConfig = require('./chcpLocalDevConfig.js');

  module.exports = {
    getBuildConfigurationByName: getBuildConfigurationByName,
    getLocalDevBuildOptions: getLocalDevBuildOptions
  };

  // region Public API

  // options for localdev
  /**
   * Construct local development options based on the .chcpenv file.
   *
   * @return {Object} local development options
   */
  function getLocalDevBuildOptions(ctx) {
    var environmentConfig = readEnvironmentConfig(ctx);

    return chcpLocalDevConfig.load(environmentConfig);
  };

  /**
   * Generate build options depending on the options, provided in console.
   *
   * @param {String} buildName - build identifier
   * @return {Object} build options; null - if none are found
   */
  function getBuildConfigurationByName(ctx, buildName) {
    // load options from the chcpbuild.options file
    var chcpBuildOptions = getBuildOptionsFromConfig(ctx);
    if (chcpBuildOptions == null) {
      return null;
    }

    return chcpBuildOptions[buildName];
  }

  // endregion

  // region Private API

  /**
   * Read options, listed in chcpbuild.options file.
   *
   * @return {Object} options from chcpbuild.options file
   */
  function getBuildOptionsFromConfig(ctx) {
    var chcpBuildOptionsFilePath = path.join(ctx.opts.projectRoot, 'chcpbuild.options');

    return readObjectFromFile(chcpBuildOptionsFilePath);
  };

  function readObjectFromFile(filePath) {
    var objData = null;
    try {
      var data = fs.readFileSync(filePath, 'utf-8');
      objData = JSON.parse(data, 'utf-8');
    } catch (err) {
    }

    return objData;
  }

  function readEnvironmentConfig(ctx) {
    var chcpEnvFilePath = path.join(ctx.opts.projectRoot, '.chcpenv');

    return readObjectFromFile(chcpEnvFilePath);
  };

  // endregion

})();
