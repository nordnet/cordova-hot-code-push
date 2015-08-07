(function() {

  var fs = require('fs'),
      path = require('path'),
      xml2js = require('xml2js'),
      configXmlPlatformStoragePlaces;

  module.exports = {
    writeOptions: writeOptions
  };

  // region Public API

  function writeOptions(projectRoot, options) {
    setup(projectRoot);
    injectOptions(options);
  }

  // endregion

  // region Private API
  function setup(projectRoot) {
    configXmlPlatformStoragePlaces = [
      path.join(projectRoot,'platforms/android/res/xml/config.xml'),
      path.join(projectRoot, 'platforms/ios/', path.basename(projectRoot), 'config.xml')
    ];
  }

  function injectOptions(options) {
    configXmlPlatformStoragePlaces.forEach(function(configXmlFilePath) {
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
    if (!options.hasOwnProperty('config_url')) {
      return;
    }

    xml['config-file'] = [
      {
        '$': {
          'url': options.config_url
        }
      }
    ];
  }

  function injectLocalDevOptions(xml, options) {
    if (!options.hasOwnProperty('local_dev_mode')) {
      return;
    }

    var localDevBlock = {};
    localDevBlock['$'] = {
      enabled: options.local_dev_mode.enabled
    };

    var jsCode = prepareJsCodeInjectionBlock(options);
    if (jsCode) {
      localDevBlock['inject-js-code'] = jsCode;
    }

    var jsScript = prepareJsScriptsInjectionBlock(options);
    if (jsScript) {
      localDevBlock['inject-js-script'] = jsScript;
    }

    xml['local-development'] = [localDevBlock];
  }

  function prepareJsCodeInjectionBlock(options) {
    if (!options.local_dev_mode.hasOwnProperty('inject_js') || !options.local_dev_mode.inject_js.hasOwnProperty('code')) {
      return null;
    }

    var injectedCode = options.local_dev_mode.inject_js.code;
    if (injectedCode.length == 0) {
      return null;
    }

    var block = [];
    injectedCode.forEach(function(jsCode) {
      block.push(jsCode.code);
    });

    return block;
  }

  function prepareJsScriptsInjectionBlock(options) {
    if (!options.local_dev_mode.hasOwnProperty('inject_js') || !options.local_dev_mode.inject_js.hasOwnProperty('scripts')) {
      return null;
    }

    var injectedScripts = options.local_dev_mode.inject_js.scripts;
    if (injectedScripts.length == 0) {
      return null;
    }

    var block = [];
    injectedScripts.forEach(function(jsScript) {
      block.push({
        '$': {'path': jsScript.path}
      });
    });

    return block;
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
