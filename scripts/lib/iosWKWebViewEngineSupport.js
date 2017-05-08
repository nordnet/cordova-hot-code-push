/*
Helper class to work with Swift.
Mainly, it has only two method: to activate and to deactivate swift support in the project.
*/

var path = require('path');
var fs = require('fs');
var strFormat = require('util').format;
var COMMENT_KEY = /_comment$/;
var WKWEBVIEW_PLUGIN_NAME = 'cordova-plugin-wkwebview-engine';
var WKWEBVIEW_MACRO = 'WK_WEBVIEW_ENGINE_IS_USED';
var isWkWebViewEngineUsed = 0;
var context;
var projectRoot;
var projectName;
var iosPlatformPath;

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

  var wkWebViewPluginPath = path.join(projectRoot, 'plugins', WKWEBVIEW_PLUGIN_NAME);
  isWkWebViewEngineUsed = isDirectoryExists(wkWebViewPluginPath) ? 1 : 0;
}

function isDirectoryExists(dir) {
  var exists = false;
  try {
    fs.accessSync(dir, fs.F_OK);
    exists = true;
  } catch(err) {
  }

  return exists;
}

/**
 * Load iOS project file from platform specific folder.
 *
 * @return {Object} projectFile - project file information
 */
function loadProjectFile() {
  var platform_ios;
  var projectFile;

  try {
    // try pre-5.0 cordova structure
    platform_ios = context.requireCordovaModule('cordova-lib/src/plugman/platforms')['ios'];
    projectFile = platform_ios.parseProjectFile(iosPlatformPath);
  } catch (e) {
    // let's try cordova 5.0 structure
    try {
      platform_ios = context.requireCordovaModule('cordova-lib/src/plugman/platforms/ios');
      projectFile = platform_ios.parseProjectFile(iosPlatformPath);
    } catch (e) {
      // cordova 7.0 due to https://issues.apache.org/jira/browse/CB-11242 / https://github.com/apache/cordova-lib/pull/526
      var project_files = context.requireCordovaModule('glob')
        .sync(path.join(iosPlatformPath, '*.xcodeproj', 'project.pbxproj'));

      if (project_files.length === 0) {
        throw new Error('does not appear to be an xcode project (no xcode project file)');
      }

      var pbxPath = project_files[0];

      var xcodeproj = context.requireCordovaModule('xcode').project(pbxPath);
      xcodeproj.parseSync();

      projectFile = {
        'xcode': xcodeproj,
        write: function () {
            var fs = context.requireCordovaModule('fs');

        var frameworks_file = path.join(iosPlatformPath, 'frameworks.json');
        var frameworks = {};
        try {
            frameworks = context.requireCordovaModule(frameworks_file);
        } catch (e) { }

        fs.writeFileSync(pbxPath, xcodeproj.writeSync());
          if (Object.keys(frameworks).length === 0) {
              // If there is no framework references remain in the project, just remove this file
              context.requireCordovaModule('shelljs').rm('-rf', frameworks_file);
              return;
          }
          fs.writeFileSync(frameworks_file, JSON.stringify(this.frameworks, null, 4));
        }
      };
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
 * Remove comments from the file.
 *
 * @param {Object} obj - file object
 * @return {Object} file object without comments
 */
function nonComments(obj) {
  var keys = Object.keys(obj);
  var newObj = {};

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
  var configurations = nonComments(xcodeProject.pbxXCBuildConfigurationSection());
  var config;
  var buildSettings;

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
