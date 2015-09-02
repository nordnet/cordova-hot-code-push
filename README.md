# Cordova Hot Code Push Plugin
This plugin provides functionality to perform automatic updates of the web based content. Basically, everything that is stored in `www` folder of your project can be updated using this plugin.

## Supported Platforms
- Android 4.0.0 or above
- iOS 7.0 or above

## Documentation
- [Quick start guide](#quick-start-guide)
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

### Quick start guide

1) Create new Cordova project using command line interface and add iOS/Android platforms:
```sh
cordova create TestProject com.example.testproject TestProject
cd ./TestProject
cordova platform add android
cordova platform add ios
```
Or use the existing one.

2) Add plugin:
```sh
cordova plugin add cordova-hot-code-push-plugin
```

At the end of the installation you will be prompted to install Cordova Hot Code Push CLI client.
```
To make the development process more easy for you - we developed CLI client for our plugin.
For more information please visit https://github.com/nordnet/cordova-hot-code-push-cli
Would you like to install CLI client for the plugin?(y/n):
```
Say `y` and let it be installed.

3) Run initialization command for deployment:
```sh
cordova-hcp init
```
Fill in all the required fields, other fields leave empty. For now even for required fields you can enter any random value. For example:
```
Please provide: Enter project name (required):  TestProject
Please provide: Amazon S3 Bucket name (required for cordova-hcp deploy):
Please provide: Amazon S3 region (required for chcp deploy):  (us-east-1)
Please provide: IOS app identifier:
Please provide: Android app identifier:
Please provide: Update method (required): (resume)
Please provide: Enter full URL to directory where chcp build result will be uploaded: https://random/url
```
As a result `cordova-hcp.json` file will be created in you projects root directory.

4) Start local server by executing:
```sh
cordova-hcp server
```

As a result you will see something like this:
```
Running server
Checking:  /Cordova/TestProject/www
local_url http://localhost:31284
Config { name: 'TestProject',
  ios_identifier: '',
  android_identifier: '',
  update: 'resume',
  content_url: 'https://5027caf9.ngrok.com',
  release: '2015.09.02-10.17.48' }
Warning: .chcpignore does not exist.
Build 2015.09.02-10.17.48 created in /Cordova/TestProject/www
cordova-hcp local server available at: http://localhost:31284
cordova-hcp public server available at: https://5027caf9.ngrok.com
Connect your app using QR code at: https://5027caf9.ngrok.com/connect
Connect route for scanner application
```

5) Open new console window, go to the project root and launch the app:
```sh
cordova run
```

Wait until application is launched for both platforms.

6) Now open your `index.html` page in `www` folder of the `TestProject`, change something in it and save. In a few seconds you will see updated page on the launched devices (emulators).

From this point you can do local development, where all the changes are uploaded on the devices without the need to restart applications on every change you made.

### Installation
This requires cordova 5.0+ (current stable 0.1)
```sh
    cordova plugin add cordova-hot-code-push-plugin
```

It is also possible to install via repo url directly (__unstable__)
```sh
    cordova plugin add https://github.com/nordnet/cordova-hot-code-push.git
```

At the end of the installation plugin will ask you to install [Cordova Hot Code Push CLI client](https://github.com/nordnet/cordova-hot-code-push-cli). This client will help you to:
- easily generate necessary configuration files;
- launch local server to listen for any changes in the web project and deploy new version immediately on the app.

Of course, you can use this plugin without the CLI client, but it will make your life easier. Either way, you can always install it later manually.

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
