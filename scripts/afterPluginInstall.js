var exec = require('child_process').exec,
    isWindows = /^win/.test(process.platform),
    chcpCliGitRepository = 'https://github.com/nordnet/cordova-hot-code-push-cli.git',
    chcpCliPackageName = 'chcp-cli',
    modules = ['prompt', 'xml2js'];

function checkIfChcpInstalled(callback) {
  var cmd = 'npm -g list ' + chcpCliPackageName;
  exec(cmd, function(err, stdout, stderr) {
    callback(err);
  });
}

function installChcpCLI() {
  console.log('Installing CHCP CLI client...');

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

function promptCliInstallation() {
  console.log('');
  console.log('To make the development process more easy for you - we developed CLI client for our plugin.');
  console.log('For more information please visit https://github.com/nordnet/cordova-hot-code-push-cli');

  var prompt = require('prompt');

  var schema = {
    properties: {
      install: {
        description:'(y/n):',
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

function isNodeModuleInstalled(moduleName) {
  var installed = true;
  try {
    require.resolve(moduleName);
  } catch (err) {
    installed = false;
  }

  return installed;
}

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

var isModulesInstalled = false;

function runCliInstall() {
  checkIfChcpInstalled(function(err) {
    if (err) {
      promptCliInstallation();
    } else {
      logEnd();
    }
  });
}

function logStart() {
  console.log("======== CHCP after installation process ========");
}

function logEnd() {
  console.log("=================================================");
}

module.exports = function(ctx) {
  logStart();
  installRequiredNodeModules();
};
