/**
This is an iOS specific hook. It enables Swift support in the project.
Plugin itself is written in Objective-C, but it uses Socket.IO library to connect to local server when we in
local development mode. And that library is written in Swift.
*/

var path = require('path'),
  fs = require('fs'),
  IOS_DEPLOYMENT_TARGET = '7.0',
  COMMENT_KEY = /_comment$/,
  context,
  projectRoot,
  projectName,
  projectModuleName,
  iosPlatformPath;

module.exports = function(ctx) {
  enableSwiftSupport(ctx);
};

/**
 * Enables Swift support.
 *
 * @param {Object} ctx - cordova context instance
 */
function enableSwiftSupport(ctx) {
  init(ctx);

  // injecting options in project file
  var projectFile = loadProjectFile();
  injectOptionsInProjectConfig(projectFile.xcode);
  projectFile.write();

  // injecting inclusion of Swift header
  injectSwiftHeader();
}

/**
 * Initialize before execution.
 *
 * @param {Object} ctx - cordova context instance
 */
function init(ctx) {
  context = ctx;
  projectRoot = ctx.opts.projectRoot;
  projectName = getProjectName(ctx, projectRoot);
  iosPlatformPath = path.join(projectRoot, 'platforms', 'ios')
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
    ConfigParser = ctx.requireCordovaModule('cordova-lib/src/configparser/ConfigParser'),
    xml = cordova_util.projectConfig(projectRoot),
    cfg = new ConfigParser(xml);

  return cfg.name();
}

/**
 * Inject Swift options into project configuration file.
 *
 * @param {Object} xcodeProject - xcode project file instance
 */
function injectOptionsInProjectConfig(xcodeProject) {
  var configurations = nonComments(xcodeProject.pbxXCBuildConfigurationSection()),
    config,
    buildSettings;

  for (config in configurations) {
    buildSettings = configurations[config].buildSettings;
    buildSettings['IPHONEOS_DEPLOYMENT_TARGET'] = IOS_DEPLOYMENT_TARGET;
    buildSettings['EMBEDDED_CONTENT_CONTAINS_SWIFT'] = "YES";
    buildSettings['LD_RUNPATH_SEARCH_PATHS'] = '"@executable_path/Frameworks"';

    // if project module name is not defined - set it with value from build settings
    if ((!projectModuleName || projectModuleName.length == 0) && buildSettings['PRODUCT_NAME']) {
      setProjectModuleName(buildSettings['PRODUCT_NAME']);
    }
  }
  console.log('IOS project now has deployment target set as:[' + IOS_DEPLOYMENT_TARGET + '] ...');
  console.log('IOS project option EMBEDDED_CONTENT_CONTAINS_SWIFT set as:[YES] ...');
  console.log('IOS project Runpath Search Paths set to: @executable_path/Frameworks ...');
}

/**
 * Set project module name from the build settings.
 * Will be used to generate name of the Swift header.
 *
 * @param {String} nameFromBuildSettings - name of the product from build settings
 */
function setProjectModuleName(nameFromBuildSettings) {
  projectModuleName = nameFromBuildSettings.trim().replace(/"/g, '');
}

/**
 * Inject Swift inclusion header into ProjectName-Prefix.pch.
 * This way we ensure that Swift libraries are accessible in all project classes.
 */
function injectSwiftHeader() {
  // path to Prefix file and the name of included header
  var prefixFilePath = path.join(iosPlatformPath, projectName, projectName + '-Prefix.pch'),
    swiftImportHeader = generateSwiftHeaderFromProjectName(projectModuleName),
    prefixFileContent;

  try {
    prefixFileContent = fs.readFileSync(prefixFilePath, {
      encoding: 'utf8'
    });
  } catch (err) {
    console.log(err);
    return;
  }

  // don't import if it is already there
  if (prefixFileContent.indexOf(swiftImportHeader) > -1) {
    return;
  }

  prefixFileContent += '\n#ifdef __OBJC__\n' +
    '    #import "' + swiftImportHeader + '"\n' +
    '#endif\n';

  fs.writeFileSync(prefixFilePath, prefixFileContent, {
    encoding: 'utf8'
  });

  console.log('IOS project ' + swiftImportHeader + ' now contains import for Swift ');
}

/**
 * Generate name of the header file for Swift support.
 * Details on Swift header name could be found here: https://developer.apple.com/library/ios/documentation/Swift/Conceptual/BuildingCocoaApps/MixandMatch.html
 *
 * @param {String} projectModuleName - projects module name from build configuration
 * @return {String} Swift header name
 */
function generateSwiftHeaderFromProjectName(projectModuleName) {
  var normalizedName = projectModuleName.replace(/([^a-z0-9]+)/gi, '_');

  return normalizedName + '-Swift.h'
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
