/*
Helper class to work with Swift.
Mainly, it has only two method: to activate and to deactivate swift support in the project.
*/

(function() {
  var path = require('path'),
    fs = require('fs'),
    strFormat = require('util').format,
    COMMENT_KEY = /_comment$/,
    WKWEBVIEW_PLUGIN_NAME = 'cordova-plugin-wkwebview-engine',
    WKWEBVIEW_MACRO = 'WK_WEBVIEW_ENGINE_IS_USED',
    isWkWebViewEngineUsed = 0,
    context,
    projectRoot,
    projectName,
    iosPlatformPath;

  module.exports = {
    setWKWebViewEngineMacro: setWKWebViewEngineMacro
  };

  /**
   * Define preprocessor macro for WKWebViewEngine.
   *
   * @param {Object} cordovaContext - cordova context
   */
  function setWKWebViewEngineMacro(cordovaContext) {
    init(cordovaContext);

    // injecting options in project file
    var projectFile = loadProjectFile();
    setMacro(projectFile.xcode);
    projectFile.write();
  }

  // region General private methods

  /**
   * Initialize before execution.
   *
   * @param {Object} ctx - cordova context instance
   */
  function init(ctx) {
    context = ctx;
    projectRoot = ctx.opts.projectRoot;
    projectName = getProjectName(ctx, projectRoot);
    iosPlatformPath = path.join(projectRoot, 'platforms', 'ios');

    var iosPlatform = require(path.join(projectRoot, 'plugins', 'ios.json'));
    if (iosPlatform.installed_plugins.hasOwnProperty(WKWEBVIEW_PLUGIN_NAME)) {
      isWkWebViewEngineUsed = 1;
    }
  }

  /**
   * Load iOS project file from platform specific folder.
   *
   * @return {Object} projectFile - project file information
   */
  function loadProjectFile() {
    var platform_ios,
      projectFile;

    try {
      // try pre-5.0 cordova structure
      platform_ios = context.requireCordovaModule('cordova-lib/src/plugman/platforms')['ios'];
      projectFile = platform_ios.parseProjectFile(iosPlatformPath);
    } catch (e) {
      // let's try cordova 5.0 structure
      platform_ios = context.requireCordovaModule('cordova-lib/src/plugman/platforms/ios');
      projectFile = platform_ios.parseProjectFile(iosPlatformPath);
    }

    return projectFile;
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

  /**
   * Remove comments from the file.
   *
   * @param {Object} obj - file object
   * @return {Object} file object without comments
   */
  function nonComments(obj) {
    var keys = Object.keys(obj),
      newObj = {};

    for (var i = 0, len = keys.length; i < len; i++) {
      if (!COMMENT_KEY.test(keys[i])) {
        newObj[keys[i]] = obj[keys[i]];
      }
    }

    return newObj;
  }

  // endregion

  // region Macros injection

  /**
   * Inject WKWebView macro into project configuration file.
   *
   * @param {Object} xcodeProject - xcode project file instance
   */
  function setMacro(xcodeProject) {
    var configurations = nonComments(xcodeProject.pbxXCBuildConfigurationSection()),
      config,
      buildSettings;

    for (config in configurations) {
      buildSettings = configurations[config].buildSettings;
      var preprocessorDefs = buildSettings['GCC_PREPROCESSOR_DEFINITIONS'] ? buildSettings['GCC_PREPROCESSOR_DEFINITIONS'] : [];
      if (!preprocessorDefs.length && !isWkWebViewEngineUsed) {
        continue;
      }

      if (!Array.isArray(preprocessorDefs)) {
        preprocessorDefs = [preprocessorDefs];
      }

      var isModified = false;
      var injectedDefinition = strFormat('"%s=%d"', WKWEBVIEW_MACRO, isWkWebViewEngineUsed);
      preprocessorDefs.forEach(function(item, idx) {
        if (item.indexOf(WKWEBVIEW_MACRO) !== -1) {
          preprocessorDefs[idx] = injectedDefinition;
          isModified = true;
        }
      });

      if (!isModified) {
        preprocessorDefs.push(injectedDefinition);
      }

      if (preprocessorDefs.length === 1) {
        buildSettings['GCC_PREPROCESSOR_DEFINITIONS'] = preprocessorDefs[0];
      } else {
        buildSettings['GCC_PREPROCESSOR_DEFINITIONS'] = preprocessorDefs;
      }
    }
  }

  // endregion

})();
