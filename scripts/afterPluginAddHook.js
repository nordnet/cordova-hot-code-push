/**
Hook is executed when plugin is added to the project.
It will check all necessary module dependencies and install the missing ones locally.
Also, it will suggest to user to install CLI client for that plugin.
It can be found in https://github.com/nordnet/cordova-hot-code-push-cli
*/

var exec = require('child_process').exec,
  path = require('path'),
  fs = require('fs'),
  modules = ['read-package-json'],
  chcpCliPackageName = 'cordova-hot-code-push-cli',
  INSTALLATION_FLAG_FILE_NAME = '.installed',
  packageJsonFilePath;

// region NPM specific

/**
 * Discovers module dependencies in plugin's package.json and installs them.
 */
function installModulesFromPackageJson() {
  var readJson = require('read-package-json');
  readJson(packageJsonFilePath, console.error, false, function(err, data) {
    if (err) {
      printLog('Can\'t read package.json file: ' + err);
      return;
    }

    var dependencies = data['dependencies'];
    if (dependencies) {
      for (var module in dependencies) {
        modules.push(module);
      }
      installRequiredNodeModules(function() {
        printLog('All dependency modules are installed.');
        checkCliDependency();
      });
    }
  });
}

/**
 * Check if node package is installed.
 *
 * @param {String} moduleName
 * @return {Boolean} true if package already installed
 */
function isNodeModuleInstalled(moduleName) {
  var installed = true;
  try {
    var module = require(moduleName);
  } catch (err) {
    installed = false;
  }

  return installed;
}

/**
 * Install node module locally.
 * Basically, it runs 'npm install module_name'.
 *
 * @param {String} moduleName
 * @param {Callback(error)} callback
 */
function installNodeModule(moduleName, callback) {
  if (isNodeModuleInstalled(moduleName)) {
    printLog('Node module ' + moduleName + ' is found');
    callback(null);
    return;
  }
  printLog('Can\'t find module ' + moduleName + ', running npm install');

  var cmd = 'npm install -D ' + moduleName;
  exec(cmd, function(err, stdout, stderr) {
    callback(err);
  });
}

/**
 * Install all required node packages.
 */
function installRequiredNodeModules(callback) {
  if (modules.length == 0) {
    callback();
    return;
  }

  var moduleName = modules.shift();
  installNodeModule(moduleName, function(err) {
    if (err) {
      printLog('Failed to install module ' + moduleName + ':' + err);
      return;
    }

    printLog('Module ' + moduleName + ' is installed');
    installRequiredNodeModules(callback);
  });
}

// endregion

// region CLI specific

/**
 * Check if cordova-hcp utility is installed. If not - suggest user to install it.
 */
function checkCliDependency() {
  checkIfChcpInstalled(function(err) {
    if (err) {
      suggestCliInstallation();
    }
  });
}

/**
 * Check if cordova-hcp utility is installed.
 *
 * @param {Callback(error)} callback
 */
function checkIfChcpInstalled(callback) {
  var cmd = 'npm -g list ' + chcpCliPackageName;
  exec(cmd, function(err, stdout, stderr) {
    callback(err);
  });
}

/**
 * Show message, that developer should install CLI client for the plugin, so it would be easier to use.
 */
function suggestCliInstallation() {
  printLog('');
  printLog('To make the development process easier for you - we developed a CLI client for our plugin.');
  printLog('To install it, please, use command:');
  printLog('npm install -g ' + chcpCliPackageName);
  printLog('For more information please visit https://github.com/nordnet/cordova-hot-code-push-cli');
}

// endregion

// region Logging

function logStart() {
  console.log('CHCP checking dependencies:');
}

function printLog(msg) {
  var formattedMsg = '    ' + msg;
  console.log(formattedMsg);
}

// endregion

// region Private API

/**
 * Perform initialization before any execution.
 *
 * @param {Object} ctx - cordova context object
 */
function init(ctx) {
  packageJsonFilePath = path.join(ctx.opts.projectRoot, 'plugins', ctx.opts.plugin.id, 'package.json');
}

/**
 * Check if we already executed this hook.
 *
 * @param {Object} ctx - cordova context
 * @return {Boolean} true if already executed; otherwise - false
 */
function isInstallationAlreadyPerformed(ctx) {
  var pathToInstallFlag = path.join(ctx.opts.projectRoot, 'plugins', ctx.opts.plugin.id, INSTALLATION_FLAG_FILE_NAME),
    isInstalled = false;
  try {
    var content = fs.readFileSync(pathToInstallFlag);
    isInstalled = true;
  } catch (err) {
  }

  return isInstalled;
}

/**
 * Create empty file - indicator, that we tried to install dependency modules after installation.
 * We have to do that, or this hook is gonna be called on any plugin installation.
 */
function createPluginInstalledFlag(ctx) {
  var pathToInstallFlag = path.join(ctx.opts.projectRoot, 'plugins', ctx.opts.plugin.id, INSTALLATION_FLAG_FILE_NAME);

  fs.closeSync(fs.openSync(pathToInstallFlag, 'w'));
}

// endregion

module.exports = function(ctx) {
  // exit if we already executed this hook once
  if (isInstallationAlreadyPerformed(ctx)) {
    return;
  }

  logStart();

  init(ctx);
  installRequiredNodeModules(installModulesFromPackageJson);

  createPluginInstalledFlag(ctx);
};
