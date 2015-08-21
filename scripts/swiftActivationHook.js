

  var path = require('path'),
    fs = require('fs'),
    IOS_DEPLOYMENT_TARGET = '7.0',
    COMMENT_KEY = /_comment$/,
    context,
    projectRoot,
    projectName,
    iosPlatformPath;

  module.exports = function(ctx) {
    enableSwiftSupport(ctx);
  };

  function enableSwiftSupport(ctx) {
    init(ctx);

    // injecting options in project file
    var projectFile = loadProjectFile();
    injectOptionsInProjectConfig(projectFile.xcode);
    projectFile.write();

    // injecting inclusion of Swift header
    injectSwiftHeader();
  }

  function init(ctx) {
    context = ctx;
    projectRoot = ctx.opts.projectRoot;
    projectName = getProjectName(ctx, projectRoot);
    iosPlatformPath = path.join(projectRoot, 'platforms', 'ios')
  }

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

  function getProjectName(ctx, projectRoot) {
    var cordova_util = ctx.requireCordovaModule('cordova-lib/src/cordova/util'),
      ConfigParser = ctx.requireCordovaModule('cordova-lib/src/configparser/ConfigParser'),
      xml = cordova_util.projectConfig(projectRoot),
      cfg = new ConfigParser(xml);

    return cfg.name();
  }

  function injectOptionsInProjectConfig(xcodeProject) {
    var configurations = nonComments(xcodeProject.pbxXCBuildConfigurationSection()),
      config,
      buildSettings;

    for (config in configurations) {
      buildSettings = configurations[config].buildSettings;
      buildSettings['IPHONEOS_DEPLOYMENT_TARGET'] = IOS_DEPLOYMENT_TARGET;
      buildSettings['EMBEDDED_CONTENT_CONTAINS_SWIFT'] = "YES";
      buildSettings['LD_RUNPATH_SEARCH_PATHS'] = '"@executable_path/Frameworks"'
    }
    console.log('IOS project now has deployment target set as:[' + IOS_DEPLOYMENT_TARGET + '] ...');
    console.log('IOS project option EMBEDDED_CONTENT_CONTAINS_SWIFT set as:[YES] ...');
    console.log('IOS project Runpath Search Paths set to: @executable_path/Frameworks ...');
  }

  function injectSwiftHeader() {
    var prefixFilePath = path.join(iosPlatformPath, projectName, projectName + '-Prefix.pch'),
      swiftImportHeader = projectName + '-Swift.h',
      prefixFileContent;

    try {
      prefixFileContent = fs.readFileSync(prefixFilePath, {encoding: 'utf8'});
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

    fs.writeFileSync(prefixFilePath, prefixFileContent, {encoding: 'utf8'});

    console.log('IOS project ' + swiftImportHeader + ' now contains import for Swift ');
  }

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
