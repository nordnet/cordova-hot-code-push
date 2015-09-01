/**
Hook is executed when plugin is added to the project.
It will check all necessary module dependencies and install the missing ones locally.
Also, it will suggest to user to install CLI client for that plugin.
It can be found in https://github.com/nordnet/cordova-hot-code-push-cli.git
*/

var exec = require('child_process').exec,
  isWindows = /^win/.test(process.platform),
  chcpCliGitRepository = 'https://github.com/nordnet/cordova-hot-code-push-cli.git',
  chcpCliPackageName = 'chcp-cli',
  modules = ['prompt', 'xml2js'];

// region CLI specific

/**
 * Install cordova-hcp utility if needed.
 */
function runCliInstall() {
  checkIfChcpInstalled(function(err) {
    if (err) {
      promptCliInstallation();
    } else {
      logEnd();
    }
  });
}

/**
 * Check if cordova-hcp utility is installed.
 * If not - we will prompt user to install it.
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
 * Install CLI client for the plugin.
 */
function installChcpCLI() {
  console.log('Installing CHCP CLI client...');

  // installation command depends on the platform
  var cmd;
  if (isWindows) {
    cmd = 'npm -g install ' + chcpCliGitRepository;
  } else {
    cmd = 'sudo npm -g install ' + chcpCliGitRepository;
  }

  exec(cmd, function(err, stdout, stderr) {
    if (err) {
      console.log('Failed to install.');
      console.log(stderr);
      logEnd();
    } else {
      console.log('CLI for plugin is installed. You are good to go. For more information use "cordova-hcp --help"');
      logEnd();
    }
  });
}

/**
 * Ask user if he want to install CLI client for this plugin.
 */
function promptCliInstallation() {
  console.log('');
  console.log('To make the development process more easy for you - we developed CLI client for our plugin.');
  console.log('For more information please visit https://github.com/nordnet/cordova-hot-code-push-cli');

  var prompt = require('prompt');

  var schema = {
    properties: {
      install: {
        description: '(y/n):',
        pattern: /^[yn]{1}$/,
        message: '(y/n)',
        required: true
      }
    }
  };
  prompt.message = 'Would you like to install CLI client for the plugin?';
  prompt.delimiter = '';
  prompt.start();

  prompt.get(schema, function(err, result) {
    if (result.install === 'y') {
      installChcpCLI();
    } else {
      console.log('Next time, then. You can do it yourself any time you want by running: ');
      console.log('npm -g install ' + chcpCliGitRepository);
      logEnd();
    }
  });
}

// endregion

// region NPM specific

/**
 * Check if node package is installed.
 *
 * @param {String} moduleName
 * @return {Boolean} true if package already installed
 */
function isNodeModuleInstalled(moduleName) {
  var installed = true;
  try {
    require.resolve(moduleName);
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
    console.log('Node module ' + moduleName + ' is found');
    callback(null);
    return;
  }
  console.log('Can\'t find module ' + moduleName + ', running npm install');

  var cmd = 'npm install ' + moduleName;
  exec(cmd, function(err, stdout, stderr) {
    callback(err);
  });
}

/**
 * Install all required node packages. For now we have to do it manually.
 * Once we make plugin as a node package - we can specify dependencies in package.json.
 */
function installRequiredNodeModules() {
  if (modules.length == 0) {
    console.log('All dependency modules are installed.');
    runCliInstall();
    return;
  }

  var moduleName = modules.shift();
  installNodeModule(moduleName, function(err) {
    if (err) {
      console.log('Failed to install module ' + moduleName);
      console.log(err);
      return;
    } else {
      console.log('Package ' + moduleName + ' is installed');
      installRequiredNodeModules();
    }
  });
}

// endregion

function logStart() {
  console.log("======== CHCP after plugin add process ========");
}

function logEnd() {
  console.log("=================================================");
}

module.exports = function(ctx) {
  logStart();
  installRequiredNodeModules();
};
