/**
  This hook is executed every time we build the project.
  It will populate config.xml with plugin specific options and for iOS - it will activate Swift support.
  If you want to specify for which server to build the project - you can create chcpbuild.options and put your servers like so:
    {
      "production": {
        "config-file": "https://some/production/server/chcp.json"
      },
      "dev": {
        "config-file": "https://some/dev/server/chcp.json"
        "local-development": {
          "enabled": "true"
        }
      },
      "qa": {
        "config-file": "https://some/qa/server/chcp.json"
      }
    }

  File contains list of build options in JSON format.
  After it is set you can run build command like that:
    cordova build -- dev


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
    console.warn('Unknown build configuration.');
    console.warn('You can ignore this if "config_url" is set in config.xml manually.');
    console.warn('Otherwise, please provide build configuration in chcpbuild.options. For local development please run:');
    console.warn('cordova-hcp server');
    console.warn('This will generate .chcpenv file with local server configuration.');
    logEnd();
    return;
  }

  console.log('Using config:');
  console.log(JSON.stringify(buildConfig, null, 2));
  chcpConfigXmlWriter.writeOptions(ctx, buildConfig);

  logEnd();
};
