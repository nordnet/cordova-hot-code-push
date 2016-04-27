/**
Hook is executed when plugin is added to the project.
It will check all necessary module dependencies and install the missing ones locally.
Also, it will suggest to user to install CLI client for that plugin.
It can be found in https://github.com/nordnet/cordova-hot-code-push-cli
*/

var path = require('path');
var fs = require('fs');
var spawnSync = require('child_process').spawnSync;
var pluginNpmDependencies = require('../package.json').dependencies;
var INSTALLATION_FLAG_FILE_NAME = '.npmInstalled';

// region CLI specific

/**
 * Check if cordova-hcp utility is installed. If not - suggest user to install it.
 */
function checkCliDependency(ctx) {
  var result = spawnSync('cordova-hcp', [], { cwd: './plugins/' + ctx.opts.plugin.id });
  if (!result.error) {
    return;
  }

  suggestCliInstallation();
}

/**
 * Show message, that developer should install CLI client for the plugin, so it would be easier to use.
 */
function suggestCliInstallation() {
  console.log('---------CHCP-------------');
  console.log('To make the development process easier for you - we developed a CLI client for our plugin.');
  console.log('To install it, please, use command:');
  console.log('npm install -g cordova-hot-code-push-cli');
  console.log('For more information please visit https://github.com/nordnet/cordova-hot-code-push-cli');
  console.log('--------------------------');
}

// endregion

// region mark that we installed npm packages
/**
 * Check if we already executed this hook.
 *
 * @param {Object} ctx - cordova context
 * @return {Boolean} true if already executed; otherwise - false
 */
function isInstallationAlreadyPerformed(ctx) {
  var pathToInstallFlag = path.join(ctx.opts.projectRoot, 'plugins', ctx.opts.plugin.id, INSTALLATION_FLAG_FILE_NAME);
  try {
    fs.accessSync(pathToInstallFlag, fs.F_OK);
    return true;
  } catch (err) {
    return false;
  }
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
  if (isInstallationAlreadyPerformed(ctx)) {
    return;
  }

  console.log('Installing dependency packages: ');
  console.log(JSON.stringify(pluginNpmDependencies, null, 2));

  var npm = (process.platform === "win32" ? "npm.cmd" : "npm");
  var result = spawnSync(npm, ['install', '--production'], { cwd: './plugins/' + ctx.opts.plugin.id });
  if (result.error) {
    throw result.error;
  }

  createPluginInstalledFlag(ctx);

  console.log('Checking cordova-hcp CLI client...');

  checkCliDependency(ctx);
};
