# Cordova Hot Code Push Plugin
This plugin provides functionality to perform automatic updates of the web based content. Basically, everything that is stored in `www` folder of your project can be updated using this plugin.

## Supported Platforms
- Android 4.0.0 or above
- iOS 7.0 or above

## Documentation
- [Installation](#installation)
- [Description](#description)
- [Cordova config preferences](#cordova-config-preferences)
- [Configuration files](#configuration-files)
  - [Application config](#application-config)
  - [Content manifest](#content-manifest)
  - [Build options](#build-options)
- [Cordova Hot Code Push CLI client](#cordova-hot-code-push-cli-client)
- [JavaScript module](#javascript-module)
  - [Usage](#javascript-module-usage)
  - [Events](#javascript-module-events)
- [Examples](#examples)̨

### Installation
This requires cordova 5.0+ (current stable 0.1)
```sh
    cordova plugin add cordova-hot-code-push-plugin
```

It is also possible to install via repo url directly (__unstable__)
```sh
    cordova plugin add https://github.com/nordnet/cordova-hot-code-push.git
```

### Description

### Cordova config preferences



### Configuration files

Plugin uses two main configuration files for his work:
- [Application config](#application-config) - holds release related information: release version, required build version for the native side and so on.
- [Content manifest](#content-manifest) - holds information about project files: their names and hashes.

Those two are essential for plugin to work because their describe if any new release available for download and what has changed compared to the version, packed in the application.

There is also a [build options](#build-options) file which allow you to specify the plugin options in the command line when you build with `cordova build` command.

#### Application config

Application config holds information about current release of the web project. Simplest example is:
```json
{
  "content_url": "https://5027caf9.ngrok.com",
  "release": "2015.09.01-13.30.35"
}
```
Application config should be placed in your `www` folder as `chcp.json` file. It is packed with the application and describes version of the project that is installed on the device from the store.

##### content_url
URL on the server, where all your project files are located. Plugin will use it as a base url to download content manifest and all updated files.

**Option is mandatory**

##### release
Any string that describes your web project version. Based on it plugin will detect if new content is available for download.

**Be advised:** plugin will compare release values as strings for equality, and if they are not equal - it will decide that new release is available.

**Option is mandatory**

##### min_native_interface
Minimum required version of the native application. This should be a build version of the app. Can be used to add dependency between the web and the native versions of the application.

For example, if you add new plugin to the project - most likely it will require native version to update. In order to prevent user from downloading web content that he can't use right now - you increase the `min_native_interface` value.



##### update
When to perform update.

##### android_identifier

##### ios_identifier







#### Content manifest

#### Build options

### Cordova Hot Code Push CLI client

### JavaScript module

#### Usage

#### Events
