/**
Helper module to work with config.xml file.
We will use it to inject plugin-specific options.
*/
(function() {

  var path = require('path'),
    xmlHelper = require('./xmlHelper.js'),
    cordovaContext,
    projectRoot,
    platforms;

  module.exports = {
    writeOptions: writeOptions
  };

  // region Public API

  /**
   * Inject options into config.xml files of each platform.
   *
   * @param {Object} context - cordova context instance
   * @param {Object} options - plugin options to inject
   */
  function writeOptions(context, options) {
    setup(context);
    injectOptions(options);
  }

  // endregion

  // region Private API
  /**
   * Initialize module.
   *
   * @param {Object} cordovaContext - cordova context instance
   */
  function setup(context) {
    cordovaContext = context;
    platforms = context.opts.platforms;
    projectRoot = context.opts.projectRoot;
  }

  /**
   * Get name of the current project.
   *
   * @param {Object} ctx - cordova context instance
   * @param {String} projectRoot - current root of the project
   *
   * @return {String} name of the project
   */
  function getProjectName(ctx, projectRoot) {
    var cordova_util = ctx.requireCordovaModule('cordova-lib/src/cordova/util'),
      ConfigParser = ctx.requireCordovaModule('cordova-lib/src/configparser/ConfigParser'),
      xml = cordova_util.projectConfig(projectRoot),
      cfg = new ConfigParser(xml);

    return cfg.name();
  }

  /**
   * Get path to config.xml inside iOS project.
   *
   * @return {String} absolute path to config.xml file
   */
  function pathToIosConfigXml() {
    var projectName = getProjectName(cordovaContext, projectRoot);

    return path.join(projectRoot, 'platforms', 'ios', projectName, 'config.xml');
  }

  /**
   * Get path to config.xml inside Android project.
   *
   * @return {String} absolute path to config.xml file
   */
  function pathToAndroidConfigXml() {
    return path.join(projectRoot, 'platforms', 'android', 'res', 'xml', 'config.xml');
  }

  /**
   * Get path to platform-specific config.xml file.
   *
   * @param {String} platform - for what platform we need config.xml
   * @return {String} absolute path to config.xml
   */
  function getPlatformSpecificConfigXml(platform) {
    var configFilePath = null;
    switch (platform) {
      case 'ios':
        {
          configFilePath = pathToIosConfigXml();
          break;
        }
      case 'android':
        {
          configFilePath = pathToAndroidConfigXml();
          break;
        }
    }

    return configFilePath;
  }

  /**
   * Write provided options into config.xml file for each platform.
   *
   * @param {Object} options - plugin options
   */
  function injectOptions(options) {
    platforms.forEach(function(platform) {
      var configXmlFilePath = getPlatformSpecificConfigXml(platform);
      if (configXmlFilePath == null) {
        return;
      }

      // read data from config.xml
      var configData = xmlHelper.readXmlAsJson(configXmlFilePath);
      if (configData == null) {
        console.warn('Configuration file ' + configXmlFilePath + ' not found');
        return;
      }

      // if config.xml already has chcp preferences - read them
      var chcpConfig = {};
      if (configData.widget.hasOwnProperty('chcp') && configData.widget.chcp.lenght > 0) {
        chcpConfig = configData.widget.chcp[0];
      } else {
        configData.widget['chcp'] = [];
      }

      // inject new options
      injectConfigUrl(chcpConfig, options);

      // write them back to config.xml
      configData.widget.chcp[0] = chcpConfig;
      xmlHelper.writeJsonAsXml(configData, configXmlFilePath);
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
  
  // endregion

})();
