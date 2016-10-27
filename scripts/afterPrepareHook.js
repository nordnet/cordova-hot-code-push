/**
This hook is executed every time we build the project.
It will populate config.xml with plugin specific options.
If you want to specify for which server to build the project - you can create chcpbuild.options and put your servers like so:
  {
    "build_name_1": {
      "config-file": "https://some/path/to/chcp.json"
    },
    "build_name_2": {
      "config-file": "https://some/other/path/to/chcp.json",
      "local-development": {
        "enabled": true
      }
    },
    ...
  }
File contains list of build options in JSON format.
After it is set you can run build command like that:
  cordova build -- build_name_1

If no option is provided - hook will use .chcpenv file to build for local development.
More information can be found on https://github.com/nordnet/cordova-hot-code-push.
*/

var chcpBuildOptions = require('./lib/chcpBuildOptions.js');
var chcpConfigXmlReader = require('./lib/chcpConfigXmlReader.js');
var chcpConfigXmlWriter = require('./lib/chcpConfigXmlWriter.js');
var iosWKWebViewEngineSupport = require('./lib/iosWKWebViewEngineSupport.js');
var BUILD_OPTION_PREFIX = 'chcp-';
var RELEASE_BUILD_FLAG = '--release';

function logStart() {
  console.log('CHCP plugin after prepare hook:');
}

function printLog(msg) {
  var formattedMsg = '    ' + msg;
  console.log(formattedMsg);
}

/**
 * Read arguments from console.
 * We are reading only plugin-related preferences.
 *
 * @param {Object} ctx - cordova context object
 * @return {Object} parsed arguments; if none were provided - default options are returned
 */
function processConsoleOptions(ctx) {
  var consoleOptions = ctx.opts.options;

  // If we are using Cordova 5.3.3 or lower - arguments are array of strings.
  // Will be removed after some time.
  if (consoleOptions instanceof Array) {
    return processConsoleOptions_cordova_53(consoleOptions);
  }

  // for newer version of Cordova - they are an object of properties
  return processConsoleOptions_cordova_54(consoleOptions);
}

function processConsoleOptions_cordova_53(consoleOptions) {
  var parsedOptions = {
    isRelease: false,
    buildOption: ''
  };

  // Search for release flag, or plugin-specific build options.
  for (var idx in consoleOptions) {
    var opt = consoleOptions[idx];
    if (opt === RELEASE_BUILD_FLAG) {
      parsedOptions.isRelease = true;
      break;
    } else if (opt.indexOf(BUILD_OPTION_PREFIX) == 0) {
      parsedOptions.buildOption = opt.replace(BUILD_OPTION_PREFIX, '');
      break;
    }
  }

  return parsedOptions;
}

function isString(s) {
  return typeof(s) === 'string' || s instanceof String;
}

function processConsoleOptions_cordova_54(consoleOptions) {
  // For now it's like this for backwards capability.
  // Will be simplified later, when Cordova 5.4.x will be used more wide.
  var parsedOptions = {
    isRelease: false,
    buildOption: ''
  };

  // if building for release - save that and exit
  if (consoleOptions.hasOwnProperty('release')) {
    parsedOptions.isRelease = consoleOptions.release;
    return parsedOptions;
  }

  // search for plugin specific build options
  var args = consoleOptions.argv;
  for (var idx in args) {
    var opt = args[idx];
    if (!isString(opt)) {
      continue;
    }

    if (opt.indexOf(BUILD_OPTION_PREFIX) === -1) {
      continue;
    }

    parsedOptions.buildOption = opt.replace(BUILD_OPTION_PREFIX, '');

    break;
  }

  return parsedOptions;
}

/**
 * Try to inject build options according to the arguments from the console.
 *
 * @param {Object} ctx - cordova context object
 * @param {String} optionName - build option name from console; will be mapped to configuration from chcpbuild.options file
 * @return {boolean} true - if build option is found and we successfully injected it into config.xml; otherwise - false
 */
function prepareWithCustomBuildOption(ctx, optionName, chcpXmlOptions) {
  if (optionName.length == 0) {
    return false;
  }

  var buildConfig = chcpBuildOptions.getBuildConfigurationByName(ctx, optionName);
  if (buildConfig == null) {
    console.warn('Build configuration for "' + optionName + '" not found in chcp.options. Ignoring it.');
    return false;
  }

  console.log('Using config from chcp.options:');
  console.log(JSON.stringify(buildConfig, null, 2));

  mergeBuildOptions(chcpXmlOptions, buildConfig);

  console.log('Resulting config will contain the following preferences:');
  console.log(JSON.stringify(chcpXmlOptions, null, 2));

  chcpConfigXmlWriter.writeOptions(ctx, chcpXmlOptions);

  return true;
}

/**
 * Merge build options into current config.xml preferences.
 *
 * @param {Object} currentXmlOptions - current plugin preferences from config.xml
 * @param {Object} buildConfig - build config preferences
 */
function mergeBuildOptions(currentXmlOptions, buildConfig) {
  for (var key in buildConfig) {
    currentXmlOptions[key] = buildConfig[key];
  }
}

module.exports = function(ctx) {
  var buildConfig,
    chcpXmlOptions;

  logStart();

  // apply iOS-specific stuff
  if (ctx.opts.platforms.indexOf('ios') !== -1) {
    iosWKWebViewEngineSupport.setWKWebViewEngineMacro(ctx);
  }

  // if we are running build with --release option - do nothing
  var consoleOptions = processConsoleOptions(ctx);
  if (consoleOptions.isRelease) {
    printLog('Building for release, not changing config.xml');
    return;
  }

  // read plugin preferences from config.xml
  chcpXmlOptions = chcpConfigXmlReader.readOptions(ctx);

  // if any build option is provided in console - try to map it with chcpbuild.options
  if (prepareWithCustomBuildOption(ctx, consoleOptions.buildOption, chcpXmlOptions)) {
    return;
  }

  // if none of the above
  if (!chcpXmlOptions['config-file']) {
    printLog('config-file preference is not set.');
  } else {
    printLog('config-file set to ' + chcpXmlOptions['config-file']['url']);
  }
};
