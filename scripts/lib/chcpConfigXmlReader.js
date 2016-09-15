/*
Helper class to read plugin-specific options from the config.xml.
*/

var fs = require('fs');
var path = require('path');
var xmlHelper = require('./xmlHelper.js');
var cordovaContext;
var projectRoot;

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
  var configFilePath = path.join(ctx.opts.projectRoot, 'config.xml');
  var configXmlContent = xmlHelper.readXmlAsJson(configFilePath, true);

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
  if (!configXmlContent.chcp) {
    return {};
  }

  return configXmlContent.chcp;
}

// endregion
