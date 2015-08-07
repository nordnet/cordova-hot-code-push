// chcpbuild.options
// File contains list of build options in key-value manner like so:
// {
//   "build_type": {
//     "preference_1": "value_1",
//     ...
//   }
// }
// Example:
// {
//   "production": {
//     "config_url": "https://some_production/server/chcp.json"
//   },
//   "dev": {
//     "config_url": "https://some_dev/server/chcp.json"
//   },
//   "qa": {
//     "config_url": "https://some_qa/server/chcp.json"
//   }
// }
// Usage:
// cordova build -- dev

var chcpBuildOptions = require('./lib/chcpBuildOptions.js'),
    chcpConfigXmlWriter = require('./lib/chcpConfigXmlWriter.js'),
    fs = require('fs'),
    path = require('path');

function logStart() {
  console.log('========CHCP plugin after prepare hook========');
}

function logEnd() {
  console.log('=====================END======================');
}

// For local development we need to modify .release and .update preferences in chcp.json.
// This way we will force the update when the app is launched.
function modifyChcpJsonForLocalDev(wwwPaths) {
  wwwPaths.forEach(function(wwwFolderPath) {
    var filePath = path.join(wwwFolderPath, 'chcp.json');
    var chcpJson = JSON.parse(fs.readFileSync(filePath));
    chcpJson.release = "";
    chcpJson.update = "now";

    fs.writeFileSync(filePath, JSON.stringify(chcpJson));
  });
}

module.exports = function(ctx) {
  logStart();

  chcpBuildOptions.init(ctx);

  var buildConfig = chcpBuildOptions.buildConfigurationBasedOnConsoleOptions();
  if (buildConfig == null && chcpBuildOptions.isBuildingForRelease()) {
    console.log('Building for release, not changing config.xml');
    logEnd();
    return;
  }

  // building for local development
  var isInLocalDevMode = false;
  if (buildConfig == null) {
    buildConfig = chcpBuildOptions.getLocalDevBuildOptions();
    isInLocalDevMode = true;
  }

  if (buildConfig == null) {
    console.log('WARNING! Unknown build configuration.');
    console.log('You can ignore this if "hot_code_push_config_url" preference is set in config.xml manually.');
    console.log('Otherwise, please provide build configuration in chcpbuild.options. For local development please run:');
    console.log('cordova-hcp server');
    console.log('This will generate .chcpenv file with local server configuration.');
    logEnd();
    return;
  }

  if (isInLocalDevMode) {
    modifyChcpJsonForLocalDev(ctx.opts.paths);
  }

  console.log('Using config:');
  console.log(JSON.stringify(buildConfig, null, 2));

  chcpConfigXmlWriter.writeOptions(ctx.opts.projectRoot, buildConfig);

  logEnd();
};
