(function() {

  var fs = require('fs'),
      path = require('path'),
      xml2js = require('xml2js'),
      configXmlPlatformStoragePlaces = {},
      platforms;

  module.exports = {
    writeOptions: writeOptions
  };

  // region Public API

  function writeOptions(cordovaContext, options) {
    setup(cordovaContext);
    injectOptions(options);
  }

  // endregion

  // region Private API
  function setup(cordovaContext) {
    var projectRoot = cordovaContext.opts.projectRoot;
    platforms = cordovaContext.opts.platforms;

    configXmlPlatformStoragePlaces = {
      ios: path.join(projectRoot, 'platforms/ios/', path.basename(projectRoot), 'config.xml'),
      android: path.join(projectRoot,'platforms/android/res/xml/config.xml')
    }
  }

  function injectOptions(options) {
    platforms.forEach(function(platform) {
      var configXmlFilePath = configXmlPlatformStoragePlaces[platform];
      if (configXmlFilePath == null) {
        return;
      }

      var configData = readConfigXml(configXmlFilePath);
      if (configData == null) {
        return;
      }

      var chcpConfig = {};
      if (configData.widget.hasOwnProperty('chcp') && configData.widget.chcp.lenght > 0) {
        chcpConfig = configData.widget.chcp[0];
      }

      injectConfigUrl(chcpConfig, options);
      injectLocalDevOptions(chcpConfig, options);

      configData.widget.chcp[0] = chcpConfig;

      writeConfigXml(configXmlFilePath, configData);
    });
  }

  function injectConfigUrl(xml, options) {
    if (!options.hasOwnProperty('config-file')) {
      return;
    }

    xml['config-file'] = [
      {
        '$': {
          'url': options['config-file']
        }
      }
    ];
  }

  function injectLocalDevOptions(xml, options) {
    if (!options.hasOwnProperty('local-development')) {
      return;
    }

    var localDevBlock = {};
    localDevBlock['$'] = {
      enabled: options.['local-development'].enabled
    };

    xml['local-development'] = [localDevBlock];
  }

  function writeConfigXml(filePath, xmlData) {
    var xmlBuilder = new xml2js.Builder();
    var changedXmlData = xmlBuilder.buildObject(xmlData);
    fs.writeFileSync(filePath, changedXmlData);
  }

  function readConfigXml(filePath) {
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

  // endregion

})();
