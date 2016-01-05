/**
This script is iOS-only hook. It set's backwards capability macro in the plugin header.
It depends on the current iOS cordova platform version, because of the differences of the API.
*/
(function() {

  var path = require('path'),
    fs = require('fs'),
    platformsJsonRelativePath = path.join('platforms', 'platforms.json'),
    PLUGIN_NAME = 'cordova-hot-code-push-plugin',
    PLUGIN_HEADER = 'HCPPlugin.h';

  module.exports = {
    setCordovaVersionMacro: setCordovaVersionMacro
  };

  /**
   * Set correct cordova version macro in the plugin header.
   *
   * @param {Object} ctx cordova context
   */
  function setCordovaVersionMacro(ctx) {
    var projectRoot = ctx.opts.projectRoot,
      platformsJson = require(path.join(projectRoot, platformsJsonRelativePath)),
      projectName = getProjectName(ctx, projectRoot);
      pathToPluginHeader = path.join(projectRoot, 'platforms', 'ios', projectName, 'Plugins', PLUGIN_NAME, PLUGIN_HEADER);

    // if there is no ios key in the platforms.json - exit
    if (!platformsJson.hasOwnProperty('ios')) {
      return;
    }

    // read ios platform version
    var iosPlatformVersion = parseInt(platformsJson.ios);

    // read plugin header
    var headerContent = fs.readFileSync(pathToPluginHeader, 'utf8');

    // set correct macro
    headerContent = headerContent.replace(/#define HCP_CORDOVA_VERSION [0-9]+/ig, '#define HCP_CORDOVA_VERSION ' + iosPlatformVersion);

    // rewrite the plugins header
    fs.writeFileSync(pathToPluginHeader, headerContent, 'utf8');
  }

  function getProjectName(ctx, projectRoot) {
    var cordova_util = ctx.requireCordovaModule('cordova-lib/src/cordova/util'),
      xml = cordova_util.projectConfig(projectRoot),
      ConfigParser;

    // If we are running Cordova 5.4 or abova - use parser from cordova-common.
    // Otherwise - from cordova-lib.
    try {
      ConfigParser = ctx.requireCordovaModule('cordova-common/src/ConfigParser/ConfigParser');
    } catch (e) {
      ConfigParser = ctx.requireCordovaModule('cordova-lib/src/configparser/ConfigParser')
    }

    return new ConfigParser(xml).name();
  }

})();
