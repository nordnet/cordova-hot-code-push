# Cordova Hot Code Push Plugin
This plugin provides functionality to perform automatic updates of the web based content. Basically, everything that is stored in `www` folder of your project can be updated using this plugin.

## Supported Platforms
- Android 4.0.0 or above
- iOS 7.0 or above

## Documentation
- [Installation](#installation)
- [Quick start guide](#quick-start-guide)
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

At the end of the installation plugin will ask you to install [Cordova Hot Code Push CLI client](https://github.com/nordnet/cordova-hot-code-push-cli). This client will help you to:
- easily generate necessary configuration files;
- launch local server to listen for any changes in the web project and deploy new version immediately on the app.

Of course, you can use this plugin without the CLI client, but it will make your life easier. Either way, you can always install it later manually.

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

### Cordova config preferences

As you probably know, Cordova uses `config.xml` file to set different project preferences: name, description, starting page and so on. Using this config file you can also set options for the plugin.

Those preferences are specified inside the `<chcp>` block. For example:

```xml
<chcp>
    <config-file url="https://5027caf9.ngrok.com/chcp.json"/>
    <local-development enabled="true"/>
</chcp>
```

##### config-file
Defines URL from which application config should be loaded. URL is declared in the `url` property.

In the case of the local development this value is automatically set to the applications config path on the local server.

##### local-development
Defines if local development mode is activated. If `enabled` is set to `true` - plugin will try to connect to local server via socket and listen for any changes that you make in the `www` folder. For production releases this must be set to `false`.

When you execute
```sh
cordova run
```
it is automatically set to `true` since we are running in debug mode.

### Configuration files

Plugin uses two main configuration files for his work:
- [Application config](#application-config) - holds release related information: release version, required build version for the native side and so on.
- [Content manifest](#content-manifest) - holds information about project files: their names and hashes.

Those two are essential for plugin to work because their describe if any new release available for download and what has changed compared to the version, packed in the application.

There is also a [build options](#build-options) file which allow you to specify the plugin options in the command line when you build with `cordova build` command.

#### Application config

Application config holds information about the current release of the web project.

Simplest example is:
```json
{
  "content_url": "https://5027caf9.ngrok.com",
  "release": "2015.09.01-13.30.35"
}
```

It should be placed in your `www` folder as `chcp.json` file. It is packed with the application and describes version of the project that is installed on the device from the store.

In the case of the local development this file is created automatically by the `cordova-hcp` utility.

##### content_url
URL on the server, where all your project files are located. Plugin will use it as a base url to download content manifest and all updated files. **This is a required option**.

##### release
Any string that describes your web project version. Based on it plugin will detect if new content is available for download. **This is a required option**.

**Be advised:** plugin will compare release values as strings for equality, and if they are not equal - it will decide that new release is available.

##### min_native_interface
Minimum required version of the native application. This should be a build version of the app. Can be used to add dependency between the web and the native versions of the application.

For example, if you add new plugin to the project - most likely it will require native version to update. In order to prevent user from downloading web content that he can't use right now - you increase the `min_native_interface` value.

Lets say, that inside our app we have the following application config:
```json
{
  "content_url": "https://5027caf9.ngrok.com",
  "release": "2015.09.01-13.30.35",
  "min_native_interface": 10
}
```
And the build version of our app is `13`.

At some point we release a new version and publish it on the server with the config:
```json
{
  "content_url": "https://5027caf9.ngrok.com",
  "release": "2015.09.05-12.20.15",
  "min_native_interface": 15
}
```

As a result, when plugin loads that new config from the server and sees, that it's `min_native_interface` is higher then current build version of the app - it's not gonna load new release; instead it will send notification that application update is required.

##### update
Defines when to perform the update. Supported values are:
- `start` - install update when application is launched. Used by default.
- `resume` - install the update when application is resumed from background state.
- `now` - install update as soon as it has been downloaded.

You can disable automatic installation through the JavaScript. How to do that - read in [JavaScript module](#javascript-module) section.

##### android_identifier
Package name of the Android version of the application. If defined - used to redirect user to the applications page on the Google Play Store.

##### ios_identifier
Identification number of the application, for example: `id345038631`. If defined - used to redirect user to the applications page on the App Store.

#### Content manifest

Content manifest describes the state of the files inside your web project.
```
[
  {
    "file": "index.html",
    "hash": "5540bd44cbcb967efef932bc8381f886"
  },
  {
    "file": "css/index.css",
    "hash": "e46d9a1c456a9c913ca10f3c16d50000"
  },
  {
    "file": "img/logo.png",
    "hash": "7e34c95ac701f8cd9f793586b9df2156"
  },
  {
    "file": "js/index.js",
    "hash": "0ba83df8459288fd1fa1576465163ff5"
  }
]
```
Based on it plugin detects which files were removed from the project, which has changed or added. As a result:
- at the update phase it will load from the server new/updated files;
- at the installation phase it will remove deleted files.

It should be placed in your `www` folder as `chcp.manifest` file. It is packed with the application and describes project files that are installed with the app from the store.

Also, it should be placed in the root of your `content_url` from application config. For example, if your `content_url` is `https://somedomain.com/www`, then url to the manifest file will be `https://somedomain.com/www/chcp.manifest`.

To generate `chcp.manifest` file execute `build` command of plugins CLI client inside your projects root directory:
```sh
cordova-hcp build
```

##### file
Relative path to the file inside the `www` folder (where your web content is placed).

Lets say, that your web project is located at:
``
/Workspace/Cordova/TestProject/www.
``
Then `file` value should be set relative to this folder as shown in the example above.

##### hash
MD5 hash of the file. Used to detect if file has been changed since last release. Also, used as a checksum to validate that loaded file is not corrupted.

**Be advised:** always update your `chcp.manifest` file after every change in the files of the web project. Otherwise plugin is not gonna detect any changes and won't update the app.

#### Build options

### Cordova Hot Code Push CLI client

### JavaScript module

#### Usage

#### Events
