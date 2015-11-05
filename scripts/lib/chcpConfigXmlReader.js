/*
Helper class to read plugin-specific options from the config.xml.
*/

(function() {
  var fs = require('fs'),
    path = require('path'),
    xmlHelper = require('./xmlHelper.js'),
    cordovaContext,
    projectRoot;

  module.exports = {
    readOptions: readOptions
  };

  // region Public API

  /**
   * Read plugin options from config.xml.
   * If none is specified - default options are returned.
   *
   * @param {Object} ctx - cordova context object
   * @return {Object} plugin prefereces
   */
  function readOptions(ctx) {
    var configFilePath = path.join(ctx.opts.projectRoot, 'config.xml'),
      configXmlContent = xmlHelper.readXmlAsJson(configFilePath);

    return parseConfig(configXmlContent);
  }

  // endregion

  // region Private API

  /**
   * Retrieve plugin preferences from the config.xml content.
   *
   * @param {Object} configXmlContent - config.xml content as JSON object
   * @return {Object} plugin preferences
   */
  function parseConfig(configXmlContent) {
    var rootContent = configXmlContent['widget'],
      parsedData = {
        'config-file': ''
      };

    // if no <chcp> tag is found - return empty preferences
    if (rootContent['chcp'] == null) {
      return parsedData;
    }

    var chcpContent = rootContent.chcp[0];
    if (chcpContent['config-file']) {
      parsedData['config-file'] = chcpContent['config-file'][0]['$']['url'];
    }

    return parsedData;
  }

  // endregion

})();
