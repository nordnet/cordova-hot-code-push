/**
This hook is executed every time we build the project.
It will populate config.xml with plugin specific options and for iOS - it will activate Swift support.
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

var chcpBuildOptions = require('./lib/chcpBuildOptions.js'),
  chcpConfigXmlWriter = require('./lib/chcpConfigXmlWriter.js');

function logStart() {
  console.log('========CHCP plugin after prepare hook========');
}

function logEnd() {
  console.log('=====================END======================');
}

module.exports = function(ctx) {
  logStart();

  chcpBuildOptions.init(ctx);

  // find build options based on launch options
  var buildConfig = chcpBuildOptions.buildConfigurationBasedOnConsoleOptions();
  if (buildConfig == null && chcpBuildOptions.isBuildingForRelease()) {
    console.log('Building for release, not changing config.xml');
    logEnd();
    return;
  }

  // if no option is set - building for local development
  var isInLocalDevMode = false;
  if (buildConfig == null) {
    buildConfig = chcpBuildOptions.getLocalDevBuildOptions();
    isInLocalDevMode = true;
  }

  if (buildConfig == null) {
    console.warn('Unknown build configuration.');
    console.warn('You can ignore this if "config-file" is set in config.xml manually.');
    console.warn('Otherwise, please provide build configuration in chcpbuild.options. For local development please run:');
    console.warn('cordova-hcp server');
    console.warn('This will generate .chcpenv file with local server configuration.');
    logEnd();
    return;
  }

  console.log('Using config:');
  console.log(JSON.stringify(buildConfig, null, 2));

  // save options into config.xml
  chcpConfigXmlWriter.writeOptions(ctx, buildConfig);

  logEnd();
};
