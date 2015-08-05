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

var fs = require('fs'),
    path = require('path'),
    xml2js = require('xml2js');

var ChcpBuildHook = function(context) {
  this.cordova = context,
  this.chcpEnvFilePath = path.join(this.cordova.opts.projectRoot,'.chcpenv'),
  this.chcpBuildOptionsFilePath = path.join(this.cordova.opts.projectRoot,'chcpbuild.options'),
  this.configXmlPlatformStoragePlaces = [
    path.join(this.cordova.opts.projectRoot,'platforms/android/res/xml/config.xml')
  ];
};

ChcpBuildHook.prototype.__readObjectFromFile = function(filePath) {
  var objData = null;
  try {
    var data = fs.readFileSync(filePath);
    objData = JSON.parse(data, 'utf-8');
  } catch (err) {
  }

  return objData;
}

// read file .chcpenv
ChcpBuildHook.prototype.__readEnvironmentConfig = function() {
  return this.__readObjectFromFile(this.chcpEnvFilePath);
};

ChcpBuildHook.prototype.__readBuildConfig = function() {
  return this.__readObjectFromFile(this.chcpBuildOptionsFilePath);
};

ChcpBuildHook.prototype.__readConfigXml = function(filePath) {
  var xmlData = null;
  var parsedData = null;

  try {
    xmlData = fs.readFileSync(filePath);
    var xmlParser = new xml2js.Parser();
    xmlParser.parseString(xmlData, function(err, data) {
      if (data) {
        parsedData = data;
      }
    });
  } catch (err) {
    console.log(err);
  }

  return parsedData;
};

ChcpBuildHook.prototype.__writeConfigXml = function(filePath, xmlData) {
  var xmlBuilder = new xml2js.Builder();
  var changedXmlData = xmlBuilder.buildObject(xmlData);
  fs.writeFileSync(filePath, changedXmlData);
}

ChcpBuildHook.prototype.__injectConfigUrlPreference = function(configData, configUrl) {
  var isPrefFound = false;
  configData.widget.preference.some(function(prefValue) {
    if (prefValue['$'].name === 'chcp_config_url') {
      prefValue['$'].value = configUrl;
      isPrefFound = true;
      return true;
    } else {
      isPrefFound = false;
      return false;
    }
  });
  if (isPrefFound) {
    //return configData;
    return;
  }

  // inject preference if none exist
  var chcpPref = {
    '$': {
      'name': 'chcp_config_url',
      'value': configUrl
    }
  };
  configData.widget.preference.push(chcpPref);

  //return configData;
};

ChcpBuildHook.prototype.__injectLocalDevFlagPreference = function(configData, value) {
  var pref = {
    '$': {
      'name': 'chcp_local_dev_mode',
      'value': value
    }
  };
  configData.widget.preference.push(pref);
}

ChcpBuildHook.prototype.isBuildingForRelease = function() {
  var isRelease = false;
  this.cordova.opts.options.some(function(value){
    if (value === '--release') {
      isRelease = true;
      return true;
    } else {
      return false;
    }
  });

  return isRelease;
};

// options from chcpbuild.options
ChcpBuildHook.prototype.getBuildOptionsFromConfig = function() {
  return this.__readBuildConfig();
};

// options for localdev
ChcpBuildHook.prototype.getLocalDevBuildOptions = function() {
  var options = null;
  var chcpEnvConfig = this.__readEnvironmentConfig();
  if (chcpEnvConfig) {
    options = {
      config_url: chcpEnvConfig.config_url,
      local_dev_mode: true
    };
  }

  return options;
};

// inject config_url in config.xml files
ChcpBuildHook.prototype.injectOptions = function(buildOptions) {
  var __this = this;
  this.configXmlPlatformStoragePlaces.forEach(function(configXmlFilePath) {
    var configData = __this.__readConfigXml(configXmlFilePath);
    if (configData == null) {
      return;
    }

    // TODO: move to switch with the list of allowed options
    // Or just use Object.keys(buildOptions) and insert all the options from there
    if (buildOptions.hasOwnProperty('config_url')) {
      //configData = __this.__injectConfigUrlPreference(configData, buildOptions.config_url);
      __this.__injectConfigUrlPreference(configData, buildOptions.config_url);
    }

    if (buildOptions.hasOwnProperty('local_dev_mode')) {
      __this.__injectLocalDevFlagPreference(configData, buildOptions.local_dev_mode);
    }

    __this.__writeConfigXml(configXmlFilePath, configData);
    console.log('Injected options into: ' + configXmlFilePath);
  });
};

ChcpBuildHook.prototype.buildConfigurationBasedOnConsoleOptions = function() {
  var buildOption = null;

  var chcpBuildOptions = this.getBuildOptionsFromConfig();
  if (chcpBuildOptions == null) {
    return null;
  }

  console.log('Supported configurations are:');
  console.log(chcpBuildOptions);

  // get build option depending on the args from console
  var consoleOpts = this.cordova.opts.options;
  consoleOpts.some(function(value) {
    if (chcpBuildOptions.hasOwnProperty(value)) {
      buildOption = chcpBuildOptions[value];
      return true;
    } else {
      return false;
    }
  });

  return buildOption;
}

function logStart() {
  console.log('========CHCP plugin after prepare hook========');
}

function logEnd() {
  console.log('=====================END======================');
}

module.exports = function(ctx) {
  logStart();

  var chcp = new ChcpBuildHook(ctx);
  var buildConfig = chcp.buildConfigurationBasedOnConsoleOptions();
  if (buildConfig == null && chcp.isBuildingForRelease()) {
    console.log('Building for release, not changing config.xml');
    logEnd();
    return;
  }

  // building for local development
  if (buildConfig == null) {
    buildConfig = chcp.getLocalDevBuildOptions();
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
  console.log(buildConfig);

  chcp.injectOptions(buildConfig);

  logEnd();
};
