/**
Module helps to generate building options for plugin.
Those options then injected into platform-specific config.xml.
*/

var fs = require('fs');
var path = require('path');
var OPTIONS_FILE_NAME = 'chcpbuild.options';

module.exports = {
  getBuildConfigurationByName: getBuildConfigurationByName
};

// region Public API

/**
 * Generate build options depending on the options, provided in console.
 *
 * @param {String} buildName - build identifier
 * @return {Object} build options; null - if none are found
 */
function getBuildConfigurationByName(ctx, buildName) {
  // load options from the chcpbuild.options file
  var chcpBuildOptions = getBuildOptionsFromConfig(ctx);
  if (chcpBuildOptions == null) {
    return null;
  }

  var resultConfig = chcpBuildOptions[buildName];
  if (!resultConfig) {
    return null;
  }

  // backwards capability
  // TODO: remove this in the next versions
  if (resultConfig['config-file'] && !resultConfig['config-file']['url']) {
    var url = resultConfig['config-file'];
    resultConfig['config-file'] = {
      'url': url
    };
  }

  return resultConfig;
}

// endregion

// region Private API

/**
 * Read options, listed in chcpbuild.options file.
 *
 * @return {Object} options from chcpbuild.options file
 */
function getBuildOptionsFromConfig(ctx) {
  var chcpBuildOptionsFilePath = path.join(ctx.opts.projectRoot, OPTIONS_FILE_NAME);

  return readObjectFromFile(chcpBuildOptionsFilePath);
};

function readObjectFromFile(filePath) {
  var objData = null;
  try {
    var data = fs.readFileSync(filePath, 'utf-8');
    objData = JSON.parse(data, 'utf-8');
  } catch (err) {}

  return objData;
}

// endregion
