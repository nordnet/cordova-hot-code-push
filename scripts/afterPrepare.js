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

var chcpBuildReader = require('./lib/chcpBuildReader.js');
    chcpConfigXmlWriter = require('./lib/chcpConfigXmlWriter.js');

function logStart() {
  console.log('========CHCP plugin after prepare hook========');
}

function logEnd() {
  console.log('=====================END======================');
}

module.exports = function(ctx) {
  logStart();

  chcpBuildReader.init(ctx);

  var buildConfig = chcpBuildReader.buildConfigurationBasedOnConsoleOptions();
  if (buildConfig == null && chcpBuildReader.isBuildingForRelease()) {
    console.log('Building for release, not changing config.xml');
    logEnd();
    return;
  }

  // building for local development
  if (buildConfig == null) {
    buildConfig = chcpBuildReader.getLocalDevBuildOptions();
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

  console.log('Using config:');
  console.log(JSON.stringify(buildConfig, null, 2));

  chcpConfigXmlWriter.writeOptions(ctx.opts.projectRoot, buildConfig);

  logEnd();
};
