var exec = require('cordova/exec');

var chcp = {

    __javaMethod: {
        FETCH_UPDATE: 'fetchUpdate',
        INSTALL_UPDATE: 'installUpdate'
    },

    __PLUGIN_NAME: 'HotCodePush',

    fetchUpdate: function(callback) {
        var didGetUpdate = function(msg) {
            if (callback != null) {
                callback(null, "Some data");
            }
        };
        var didFailToGetUpdate = function(msg) {
            if (callback != null) {
                callback("Some error", null);    
            }
        };

        exec(didGetUpdate, didFailToGetUpdate, this.__PLUGIN_NAME, this.__javaMethod.FETCH_UPDATE, []);
    },

    installUpdate: function(callback) {
        var didInstall = function(msg) {
            if (callback != null) {
                callback(null);
            }
        };

       var didFailToInstall = function(msg) {
            if (callback != null) {
                callback("some error");
            }
       };

        exec(didInstall, didFailToInstall, this.__PLUGIN_NAME, this.__javaMethod.INSTALL_UPDATE, []);
    }
};

module.exports = chcp;
