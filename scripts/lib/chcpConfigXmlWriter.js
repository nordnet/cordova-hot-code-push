/**
Helper module to work with config.xml file.
We will use it to inject plugin-specific options.
*/
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

  /**
   * Inject options into config.xml files of each platform.
   *
   * @param {Object} cordovaContext - cordova context instance
   * @param {Object} options - plugin options to inject
   */
  function writeOptions(cordovaContext, options) {
    setup(cordovaContext);
    injectOptions(options);
  }

  // endregion

  // region Private API
  /**
   * Initialize module.
   *
   * @param {Object} cordovaContext - cordova context instance
   */
  function setup(cordovaContext) {
    var projectRoot = cordovaContext.opts.projectRoot;
    platforms = cordovaContext.opts.platforms;

    configXmlPlatformStoragePlaces = {
      ios: path.join(projectRoot, 'platforms/ios/', path.basename(projectRoot), 'config.xml'),
      android: path.join(projectRoot, 'platforms/android/res/xml/config.xml')
    }
  }

  /**
   * Write provided options into config.xml file for each platform.
   *
   * @param {Object} options - plugin options
   */
  function injectOptions(options) {
    platforms.forEach(function(platform) {
      var configXmlFilePath = configXmlPlatformStoragePlaces[platform];
      if (configXmlFilePath == null) {
        return;
      }

      // read data from config.xml
      var configData = readConfigXml(configXmlFilePath);
      if (configData == null) {
        return;
      }

      // if config.xml already has chcp preferences - read them
      var chcpConfig = {};
      if (configData.widget.hasOwnProperty('chcp') && configData.widget.chcp.lenght > 0) {
        chcpConfig = configData.widget.chcp[0];
      }

      // inject new options
      injectConfigUrl(chcpConfig, options);
      injectLocalDevOptions(chcpConfig, options);

      // write them back to config.xml
      configData.widget.chcp[0] = chcpConfig;
      writeConfigXml(configXmlFilePath, configData);
    });
  }

  /**
   * Inject config-file preference if any is set in provided options.
   *
   * @param {Object} xml - config.xml data
   * @param {Object} options - plugin options to inject
   */
  function injectConfigUrl(xml, options) {
    if (!options.hasOwnProperty('config-file')) {
      return;
    }

    xml['config-file'] = [{
      '$': {
        'url': options['config-file']
      }
    }];
  }

  /**
   * Inject local-development options if any is set in provided options.
   *
   * @param {Object} xml - config.xml data
   * @param {Object} options - plugin options to inject
   */
  function injectLocalDevOptions(xml, options) {
    if (!options.hasOwnProperty('local-development')) {
      return;
    }

    var localDevBlock = {};
    localDevBlock['$'] = {
      enabled: options['local-development'].enabled
    };

    xml['local-development'] = [localDevBlock];
  }

  /**
   * Write xml data into the given file.
   *
   * @param {String} filePath - path to the file where to write provided data
   * @param {Object} xmlData - xml data to write
   */
  function writeConfigXml(filePath, xmlData) {
    var xmlBuilder = new xml2js.Builder();
    var changedXmlData = xmlBuilder.buildObject(xmlData);
    fs.writeFileSync(filePath, changedXmlData);
  }

  /**
   * Read xml data from the specified file.
   *
   * @param {String} filePath - path to the xml file
   * @return {Object} xml data from the file
   */
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
