/*
Small helper class to read/write from/to xml file.
*/


var fs = require('fs');
var xml2jsProcessors = require('xml2js/lib/processors');
var xml2js = require('xml2js');

module.exports = {
  readXmlAsJson: readXmlAsJson,
  writeJsonAsXml: writeJsonAsXml
};

/**
 * Read data from the xml file as JSON object.
 *
 * @param {String} filePath - absolute path to xml file
 * @param {Boolean} simplify - if set to true - cleanup resulting json
 * @return {Object} JSON object with the contents of the xml file
 */
function readXmlAsJson(filePath, simplify) {
  var xmlData;
  var parsedData;
  var parserOptions = {};

  if (simplify) {
    parserOptions = {
      attrValueProcessors: [xml2jsProcessors.parseNumbers, xml2jsProcessors.parseBooleans],
      explicitArray: false,
      mergeAttrs: true,
      explicitRoot: false
    };
  }

  var xmlParser = new xml2js.Parser(parserOptions);
  try {
    xmlData = fs.readFileSync(filePath);
    xmlParser.parseString(xmlData, function(err, data) {
      if (data) {
        parsedData = data;
      }
    });
  } catch (err) {}

  return parsedData;
}

/**
 * Write JSON object as xml into the specified file.
 *
 * @param {Object} jsData - JSON object to write
 * @param {String} filePath - path to the xml file where data should be saved
 * @param {Object} options - xml options
 * @return {boolean} true - if data saved to file; false - otherwise
 */
function writeJsonAsXml(jsData, filePath, options) {
  var xmlBuilder = new xml2js.Builder(options);
  var changedXmlData = xmlBuilder.buildObject(jsData);
  var isSaved = true;

  try {
    fs.writeFileSync(filePath, changedXmlData);
  } catch (err) {
    console.log(err);
    isSaved = false;
  }

  return isSaved;
}
