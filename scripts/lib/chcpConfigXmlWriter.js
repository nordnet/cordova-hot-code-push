/**
Helper module to work with config.xml file.
We will use it to inject plugin-specific options.
*/


var path = require('path');
var xmlHelper = require('./xmlHelper.js');
var cordovaContext;
var projectRoot;
var platforms;

module.exports = {
  writeOptions: writeOptions
};

// region Public API

/**
 * Inject options into config.xml files of each platform.
 *
 * @param {Object} context - cordova context instance
 * @param {Object} options - plugin options to inject
 */
function writeOptions(context, options) {
  setup(context);
  injectOptions(options);
}

// endregion

// region Private API
/**
 * Initialize module.
 *
 * @param {Object} cordovaContext - cordova context instance
 */
function setup(context) {
  cordovaContext = context;
  platforms = context.opts.platforms;
  projectRoot = context.opts.projectRoot;
}

/**
 * Get name of the current project.
 *
 * @param {Object} ctx - cordova context instance
 * @param {String} projectRoot - current root of the project
 *
 * @return {String} name of the project
 */
function getProjectName(ctx, projectRoot) {
  var cordova_util = ctx.requireCordovaModule('cordova-lib/src/cordova/util');
  var xml = cordova_util.projectConfig(projectRoot);
  var ConfigParser;

  // If we are running Cordova 5.4 or abova - use parser from cordova-common.
  // Otherwise - from cordova-lib.
  try {
    ConfigParser = ctx.requireCordovaModule('cordova-common/src/ConfigParser/ConfigParser');
  } catch (e) {
    ConfigParser = ctx.requireCordovaModule('cordova-lib/src/configparser/ConfigParser')
  }

  return new ConfigParser(xml).name();
}

/**
 * Get path to config.xml inside iOS project.
 *
 * @return {String} absolute path to config.xml file
 */
function pathToIosConfigXml() {
  var projectName = getProjectName(cordovaContext, projectRoot);

  return path.join(projectRoot, 'platforms', 'ios', projectName, 'config.xml');
}

/**
 * Get path to config.xml inside Android project.
 *
 * @return {String} absolute path to config.xml file
 */
function pathToAndroidConfigXml() {
  return path.join(projectRoot, 'platforms', 'android', 'res', 'xml', 'config.xml');
}

/**
 * Get path to platform-specific config.xml file.
 *
 * @param {String} platform - for what platform we need config.xml
 * @return {String} absolute path to config.xml
 */
function getPlatformSpecificConfigXml(platform) {
  var configFilePath = null;
  switch (platform) {
    case 'ios':
      {
        configFilePath = pathToIosConfigXml();
        break;
      }
    case 'android':
      {
        configFilePath = pathToAndroidConfigXml();
        break;
      }
  }

  return configFilePath;
}

/**
 * Write provided options into config.xml file for each platform.
 *
 * @param {Object} options - plugin options
 */
function injectOptions(options) {
  platforms.forEach(function(platform) {
    var configXmlFilePath = getPlatformSpecificConfigXml(platform);
    if (configXmlFilePath == null) {
      return;
    }

    // read data from config.xml
    var configData = xmlHelper.readXmlAsJson(configXmlFilePath);
    if (configData == null) {
      console.warn('Configuration file ' + configXmlFilePath + ' not found');
      return;
    }

    // inject new options
    var chcpXmlConfig = {};
    for (var preferenceName in options) {
      injectPreference(chcpXmlConfig, preferenceName, options[preferenceName]);
    }

    // write them back to config.xml
    configData.widget['chcp'] = [];
    configData.widget.chcp.push(chcpXmlConfig);
    xmlHelper.writeJsonAsXml(configData, configXmlFilePath);
  });
}

/**
 * Inject preference into xml.
 *
 * @param {Object} xml - current xml preferences for the plugin
 * @param {String} preferenceName - preference name
 * @param {Object} preferenceAttributes - preference attributes
 */
function injectPreference(xml, preferenceName, preferenceAttributes) {
  xml[preferenceName] = [{
    '$': preferenceAttributes
  }];
}

// endregion
