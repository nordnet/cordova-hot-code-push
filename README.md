# Cordova Hot Code Push Plugin
This plugin provides functionality to perform automatic updates of the web based content in your application. Basically, everything that is stored in `www` folder of your Cordova project can be updated using this plugin.

When you publish your application on the store - you pack in it all your web content: html files, JavaScript code, images and so on. There are two ways how you can update it:

1. Publish new version of the app on the store. But it takes time, especially with the App Store.
2. Sacrifice the offline feature and load all the pages online. But as soon as Internet connection goes down - application won't work.

This plugin is intended to fix all that. When user starts the app for the first time - it copies all the web files onto the external storage. From this moment all pages are loaded from the external folder and not from the packed bundle. On every launch plugin connects to your server and checks if the new version of web project is available for download. If so - it loads it on the device and installs on the next launch.

As a result, your application receives updates of the web content as soon as possible, and still can work in offline mode. Also, plugin allows you to specify dependency between the web release and the native version to make sure, that new release will work on the older versions of the application.

**Is it fine with App Store?** Yes, it is... as long as your content corresponds to what application is intended for. If your application should be a calculator, but after the update becomes an audio player - you will be banned.

## Supported platforms
- Android 4.0.0 or above.
- iOS 7.0 or above.

## Documentation
- [Installation](#installation)
- [Migrating from previous version](#migrating-from-previous-version)
- [Quick start guide for Cordova project](#quick-start-guide-for-cordova-project)
- [Quick start guide for Ionic project](#quick-start-guide-for-ionic-project)
- [Update workflow](#update-workflow)
- [How web project files are stored and updated](#how-web-project-files-are-stored-and-updated)
- [Cordova Hot Code Push CLI client](#cordova-hot-code-push-cli-client)
- [Local Development Add-on](#local-development-add-on)
- [Cordova config preferences](#cordova-config-preferences)
- [Configuration files](#configuration-files)
  - [Application config](#application-config)
  - [Content manifest](#content-manifest)
  - [Build options](#build-options)
- [JavaScript module](#javascript-module)
  - [Listen for update events](#listen-for-update-events)
  - [Fetch update](#fetch-update)
  - [Install update](#install-update)
  - [Change plugin preferences on runtime](#change-plugin-preferences-on-runtime)
  - [Request application update through the store](#request-application-update-through-the-store)
- [Error codes](#error-codes)

### Installation
This requires cordova 5.0+ (current stable 1.2.5)
```sh
cordova plugin add cordova-hot-code-push-plugin
```

It is also possible to install via repo url directly (__unstable__)
```sh
cordova plugin add https://github.com/nordnet/cordova-hot-code-push.git
```

At the end of the installation plugin will recommend you to install [Cordova Hot Code Push CLI client](https://github.com/nordnet/cordova-hot-code-push-cli). This client will help you to:
- easily generate necessary configuration files;
- launch local server to listen for any changes in the web project and deploy new version immediately on the app.

Of course, you can use this plugin without the CLI client, but it will make your life easier.

### Migrating from previous version

##### From v1.0.x to v1.1.x

In version 1.0.x local development mode was integrated in the plugin. Starting from v1.1.x it is moved to another [plugin](#local-development-add-on) as an add-on. Since v1.0 of hot code push plugin does some tweaks to the iOS project file to activate Swift support - after updating to v1.1.x you need to disable it.

The easiest way is to reinstall iOS platform:
```
cordova platform remove ios
cordova platform add ios
```
When platform is added - all project's plugins will be installed automatically.

Harder approach - remove Swift support manually. For that you need to open your iOS project in Xcode, and then do the following:

1. In the `Build Settings` set `Embedded Content Contains Swift Code` to `NO`.
2. In the project files find `<YOUR_PROJECT_NAME>-Prefix.pch` file, open it and remove `#import <YOUR_PROJECT_NAME>-Swift.h`. For example:

  ```
  #ifdef __OBJC__
      #import "TestProject-Swift.h"
  #endif
  ```
3. Build the project to check, if everything is fine.

### Quick start guide for Cordova project

In this guide we will show how quickly you can test this plugin and start using it for development. For that we will install [development add-on](#local-development-add-on) which requires **Xcode 7**, although hot code push plugin itself can work on the older versions of the Xcode.

1. Create new Cordova project using command line interface and add iOS/Android platforms:

  ```sh
  cordova create TestProject com.example.testproject TestProject
  cd ./TestProject
  cordova platform add android
  cordova platform add ios
  ```
  Or use the existing one.

2. Add plugin:

  ```sh
  cordova plugin add cordova-hot-code-push-plugin
  ```

3. Add plugin for local development:

  ```sh
  cordova plugin add cordova-hot-code-push-local-dev-addon
  ```

4. Install Cordova Hot Code Push CLI client:

  ```sh
  npm install -g cordova-hot-code-push-cli
  ```

5. Start local server by executing:

  ```sh
  cordova-hcp server
  ```

  As a result you will see something like this:
  ```
  Running server
  Checking:  /Cordova/TestProject/www
  local_url http://localhost:31284
  Warning: .chcpignore does not exist.
  Build 2015.09.02-10.17.48 created in /Cordova/TestProject/www
  cordova-hcp local server available at: http://localhost:31284
  cordova-hcp public server available at: https://5027caf9.ngrok.com
  ```

6. Open new console window, go to the project root and launch the app:

  ```sh
  cordova run
  ```

  Wait until application is launched for both platforms.

7. Now open your `index.html` page in `www` folder of the `TestProject`, change something in it and save. In a few seconds you will see updated page on the launched devices (emulators).

From this point you can do local development, where all the changes are uploaded on the devices without the need to restart applications on every change you made.

### Quick start guide for Ionic project

In this guide we will show how quickly you can test this plugin and start using it for development. For that we will install [development add-on](#local-development-add-on) which requires **Xcode 7**, although hot code push plugin itself can work on the older versions of the Xcode.

1. Create new Ionic project using command line interface and add iOS/Android platforms:

  ```sh
  ionic start TestProject blank
  cd ./TestProject
  ionic platform add android
  ionic platform add ios
  ```
  Or use the existing one.

2. Add plugin:

  ```sh
  ionic plugin add cordova-hot-code-push-plugin
  ```

3. Add plugin for local development:

  ```sh
  ionic plugin add cordova-hot-code-push-local-dev-addon
  ```

4. Install Cordova Hot Code Push CLI client:

  ```sh
  npm install -g cordova-hot-code-push-cli
  ```

5. Start local server by executing:

  ```sh
  cordova-hcp server
  ```

  As a result you will see something like this:
  ```
  Running server
  Checking:  /Cordova/TestProject/www
  local_url http://localhost:31284
  Warning: .chcpignore does not exist.
  Build 2015.09.02-10.17.48 created in /Cordova/TestProject/www
  cordova-hcp local server available at: http://localhost:31284
  cordova-hcp public server available at: https://5027caf9.ngrok.com
  ```

6. Open new console window, go to the project root and launch the app:

  ```sh
  ionic run
  ```

  Wait until application is launched for both platforms.

7. Now open your `index.html` page in `www` folder of the `TestProject`, change something in it and save. In a few seconds you will see updated page on the launched devices (emulators).

From this point you can do local development, where all the changes are uploaded on the devices without the need to restart applications on every change you made.

### Update workflow

Before overloading your head with all the configuration stuff - let us describe to you the update workflow of the plugin. In general, without any technical details.

![Update workflow](docs/images/update-workflow.png?raw=true)

1. User opens your application.
2. Plugin get's initialized and it launches update loader in the background thread.
3. Update loader takes `config-file` from the `config.xml` and loads JSON from the specified url. Then it compares `release` version of the loaded config to the currently installed one. If they are different - we go to the next step.
4. Update loader uses `content_url` from the application config to load manifest file. He uses it to find out, what has changed since the last release.
5. Update loader downloads all updated/new files from the `content_url`.
6. If everything went well - it sends notification, that update is ready for installation.
7. Update installed, and user is redirected to the index page of your application.

And that's it. Of course, there is a little more in it, but you get the general idea on how it works.

### How web project files are stored and updated

Every Cordova project has a `www` folder, where all your web files are stored. When `cordova build` is executed - `www` content is copied to the platform-specific `www` folder:

- For Android: `platforms/android/assets/www`.
- For iOS: `platforms/ios/www`.

And they are packed with the application. We can't update them, since they have a read-only access. For this reason on the first startup those files are copied to the external storage. Since we don't want to block user while content is copied - we display an index page from the bundled resources. But on every next launch/update - we will load an index page from the external storage.

If your update includes additional plugins or some native code - you need to publish new version of the app on the store. And for that - increase build version of the app (that is mandatory anyway for every new release on the App Store or Google Play). On the next launch plugin checks if build version has changed, and if so - it will reinstall `www` folder on the external folder.

When you are developing your app - you might get confused: done some changes, launched the app - but see the old stuff. Now you know the reason: plugin is using version of the web project from the external storage. To reset the cache you can do one of the following:

- Manually uninstall the app, and then execute `cordova run`.
- Increase build version of your app to force the plugin to reinstall the `www` folder. You can do it by setting `android-versionCode` and `ios-CFBundleVersion` in `config.xml`.
- Install [local development add-on](#local-development-add-on) and let him handle folder reset for you. It will increase the build version of the app on each build, so you don't have to do it manually.

That was a short intro, so you could get the general idea. Now lets dig into more details.

As you will read in [Configuration files](#configuration-files) section - there is an application config, called `chcp.json`. In it there is a `release` preference, which defines version of your web content. It is a required preference and should be unique for every release. It is constructed by the CLI client like so: `yyyy.MM.dd-HH.mm.ss` (i.e., `2015.09.01-13.30.35`).

For each release plugin creates a folder with this name on the external storage, and puts in it all your web files. It is a base url for your project. This approach helps to solve several problems:

- Files caching issue. For example, on iOS css files are cached by the UIWebView, and even if we reload the index page - new styles were not applied. You had to kill the app in the task manager to flush it, or do some hacks to change the url of the css file.
- Less chances that update will corrupt the existing content, since we are using totally different folders for each release.
- But if it is corrupted - we can rollback to the previous version.

For example, lets say that currently in the app we are running version `2015.12.01-12.01.33`. That means the following:
- All web files are stored in `/sdcard/some_path/2015.12.01-12.01.33/www/`. Including Cordova specific.
- Index page, that is displayed to the user is `/sdcard/some_path/2015.12.01-12.01.33/www/index.html`.

At some moment of time we release a new version: `2016.01.03-10.45.01`. At first, plugin need to load it on the device, and that's what happens:

1. A new folder with the release version name is created on the external storage: `/sdcard/some_path/2016.01.03-10.45.01/`.
2. Inside it - `update` folder is created: `/sdcard/some_path/2016.01.03-10.45.01/update/`.
3. All new/changed files from the `chcp.manifest` are loaded to this `update` folder.
4. New `chcp.manifest` and `chcp.json` files are placed in the `update` folder.
5. Saving internally, that particular release is loaded and prepared for installation.

When it's time to install the update:

1. Plugin copies `www` folder from the current version (the one, that is displayed to the user) to the new release folder. In the terms of our example: copy everything from `/sdcard/some_path/2015.12.01-12.01.33/www/` into `/sdcard/some_path/2016.01.03-10.45.01/www/`.
2. Copy new/updated files and configs from the `update` folder into `www` folder: `/sdcard/some_path/2016.01.03-10.45.01/update/` -> `/sdcard/some_path/2016.01.03-10.45.01/www/`.
3. Remove `/sdcard/some_path/2016.01.03-10.45.01/update/` folder since we don't need it anymore.
4. Load index page from the new release: `/sdcard/some_path/2016.01.03-10.45.01/www/index.html`.

From this moment forward plugin will load index page from the new release folder, and the previous one will stay as a backup just in case.

### Cordova Hot Code Push CLI client

[Cordova Hot Code Push CLI client](https://github.com/nordnet/cordova-hot-code-push-cli) is a command line utility that will help you with development and deployment of your web project.

With it you can:
- generate both `chcp.json` and `chcp.manifest` files, so you wouldn't have to do it manually;
- run local server in order to detect any changes you make in your web project and instantly upload them on the devices;
- deploy your web project on the external server.

Of course, you can use Hot Code Push plugin without that utility. But it will make it much easier.

### Local Development Add-on

When you develop your app locally - the general process looks like that:

1. Do some changes in the web project.
2. Execute `cordova run` to build and launch the app.
3. Wait for a while and see the result.

To see the results for even a smallest change you need to rebuild and restart the app. And that can take a while. And it is kind of boring.

In order to speed this up - you can use [Hot Code Push Local Development Add-on](https://github.com/nordnet/cordova-hot-code-push-local-dev-addon). Setup is pretty simple:

1. Add the plugin to the project.
2. Start local server by executing `cordova-hcp server`.
3. Add `<local-development enabled="true" />` to the `<chcp />` block of your project's `config.xml` file.
4. Launch the app.

From that moment, all the changes in the web project will be detected by the plugin, and immediately loaded into the app without the need to restart it.

You will have to restart the app only if you add some new plugin to the project.

**Important:** you should use this add-on for development purpose only. Consider deleting it before building the release version by executing `cordova plugin remove cordova-hot-code-push-local-dev-addon`.

### Cordova config preferences

As you probably know, Cordova uses `config.xml` file to set different project preferences: name, description, starting page and so on. Using this config file you can also set options for the plugin.

Those preferences are specified inside the `<chcp>` block. For example:

```xml
<chcp>
    <config-file url="https://5027caf9.ngrok.com/chcp.json"/>
</chcp>
```

##### config-file
Defines URL from which application config should be loaded. URL is declared in the `url` property. **It is a required property.**

In the case of the local development mode, if `config-file` is not defined - it is automatically set to the applications config path on the local server.

#####  auto-download
Defines if plugin is allowed to download updates. Originally update fetching is performed automatically, but you can disable it and do that manually through the JavaScript module.

To disable updates auto downloads add to `config.xml`:
```xml
<chcp>
  <auto-download enabled="false" />
</chcp>
```
By default preference is set to `true`.

##### auto-install
Defines if plugin is allowed to install updates. Originally update installation is performed automatically, but you can disable it and do that manually through the JavaScript module.

To disable updates auto installation add to `config.xml`:
```xml
<chcp>
  <auto-install enabled="false" />
</chcp>
```
By default preference is set to `true`.

### Configuration files

The plugin uses two main configuration files:
- [Application config](#application-config) - holds release related information: release version, required build version for the native side and so on.
- [Content manifest](#content-manifest) - holds information about project files: their names and hashes.

These two are essential for the plugin to work. They describe if any new release is available for download and what has changed compared to the version already packed in the application.

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

It should be placed in your `www` folder as `chcp.json` file. It is packed with the application and describes the version of the project that is installed on the device from the store.

You can either create it manually, or let `cordova-hcp` utility to do it for you. Just run `cordova-hcp init` in your project's root folder, and then on every new build execute `cordova-hcp build`. For more details [read the documentation](https://github.com/nordnet/cordova-hot-code-push-cli) for the CLI client.

##### content_url
URL on the server, where all your project files are located. Plugin will use it as a base url to download content manifest and all updated files. **This is a required option**.

##### release
Any string that describes your web project version. Should be unique for each release. Based on it plugin will detect if new content is available for download. **This is a required option**.

**Important:** plugin will compare release values as strings for equality, and if they are not equal - it will decide that new release is available.

##### min_native_interface
Minimum required version of the native application. This should be a build/code version of the app, not a version, that is displayed to the users on the App Store / Google Play. It should be a number.

In a `config.xml` you usually specify versions like so:
```xml
<widget id="io.cordova.hellocordova"
      version="1.0.1"
      android-versionCode="7"
      ios-CFBundleVersion="3">
```
- `version` - version of the app, that is visible on the store.
- `android-versionCode` - code version of the Android application. This value should be used for `min_native_interface`.
- `ios-CFBundleVersion` - code version of the iOS application. This value should be used for `min_native_interface`.

Preference creates dependency between the web and the native versions of the application.

**Important:** Due to [a quirk in cordova](https://issues.apache.org/jira/browse/CB-8976), the version code in your generated `.apk` will be multiplied by 10, resulting in an apk with a version code of 70, 72, or 74, depending on the platform (arm/x86/etc) for the previous example. In order to work around this, we recommend multiplying the iOS version code by `10` for every release, so that a `min_native_interface` of `70` can target both platforms, making your config.xml similar to:
```xml
<widget id="io.cordova.hellocordova"
      version="1.0.1"
      android-versionCode="7"
      ios-CFBundleVersion="70">
```

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

When plugin loads that new config from the server and sees, that it's `min_native_interface` is higher then the current build version of the app - it's not gonna load new release. Instead, it will send `chcp_updateLoadFailed` notification with error, stating that application update is required. In details this is described in [Request application update through the store](#request-application-update-through-the-store) section below.

**Note:** right now you can't specify different values for `min_native_interface` for different platforms. But this can be added later, if needed.

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
```json
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

As described in [Cordova config preferences](#cordova-config-preferences) section - you can change plugin options in the Cordova's `config.xml` file.

But what if you want to change it on the build phase through the command line? For that purpose you can use `chcpbuild.options` file.

It must be placed in the root directory of your Cordova project. In it you specify (in JSON) all the preferences you want to add/change in the resulting `config.xml` file. The original `config.xml` (in the projects root directory) is not touсhed, we modify the platform-specific one on `after_prepare` phase.

Lets say, that your Cordova project is located in the `/Cordova/TestProject` folder. Base `config.xml` file (`/Cordova/TestProject/config.xml`) has the following preferences:

```xml
<chcp>
  <config-file url="https://company_server.com/mobile/www/chcp.json" />
</chcp>
```

Now we create `chcpbuild.options` file inside `/Cordova/Testproject/` and put in it the following content:
```json
{
  "dev": {
    "config-file": "https://dev.company_server.com/mobile/www/chcp.json"
  },
  "production": {
    "config-file": "https://company_server.com/mobile/www/chcp.json"
  },
  "QA": {
    "config-file": "https://test.company_server.com/mobile/www/chcp.json"
  }
}
```

In order to build the app, configured to work with development server, we can run command:
```sh
cordova build -- chcp-dev
```

As a result, platform-specific `config.xml` file (for example, `/Cordova/TestProject/platforms/android/res/xml/config.xml`) will have:
```xml
<chcp>
  <config-file url="https://dev.company_server.com/mobile/www/chcp.json"/>
</chcp>
```

As you might notice - in console we prefixed build option name with the `chcp-`. This is required, so the plugin would know, that this option is for him. Also, it prevents conflicts between different plugins/hooks you already have.

When application is ready for testing - we can build it, configured to work with test server:
```sh
cordova build -- chcp-QA
```

And the plugin-specific `config.xml` will become:
```xml
<chcp>
  <config-file url="https://test.company_server.com/mobile/www/chcp.json"/>
</chcp>
```

When we are ready to release new version on the store (Google Play, App Store) - we build, as usual, with command:
```sh
cordova build --release
```
In that case `config.xml` is not modified.

If `chcpbuild.options` are not used - then plugin will use preferences from the project's main `config.xml`.

### JavaScript module

By default, all update checking->downloading->installation cycle is performed automatically by the plugins native side. No additional code on the web side is required. However, those processes can be controlled through the corresponding JavaScript module.

It allows you to:
- subscribe for update related events;
- check and download new releases from the server;
- install loaded updates;
- change plugin preferences;
- request user to download new version of the app from the store.

#### Listen for update events

Using JavaScript you can subscribe to different update related events. For example, you can get notified when update is loaded and ready for installation, or when something went wrong and we couldn't install new content.

You subscribe for events as you normally do like so:
```js
  document.addEventListener(eventName, eventCallback, false);

  function eventCallback(eventData) {
    // do something
  }
```

Error events have details on what went wrong. You can access them like so:
```js
function eventCallback(eventData) {
  var error = eventData.details.error;
  if (error) {
    console.log('Error with code: ' + error.code);
    console.log('Description: ' + error.description);
  }
}
```

Available events are:
- `chcp_updateIsReadyToInstall` - send when new release was successfully loaded and ready to be installed.
- `chcp_updateLoadFailed` - send when plugin couldn't load update from the server. Error details are attached to the event.
- `chcp_nothingToUpdate` - send when we successfully loaded application config from the server, but there is nothing new is available.
- `chcp_updateInstalled` - send when update was successfully installed.
- `chcp_updateInstallFailed` - send when update installation failed. Error details are attached to the event.
- `chcp_nothingToInstall` - send when there is nothing to install. Probably, nothing was loaded before that.
- `chcp_assetsInstalledOnExternalStorage` - send when plugin successfully copied web project files from bundle onto the external storage. Most likely you will use it for debug purpose only. Or even never.
- `chcp_assetsInstallationError` - send when plugin couldn't copy files from bundle onto the external storage. If this happens - plugin won't work. Can occur when there is not enough free space on the device. Error details are attached to the event.

Now it is time for small example. Lets say that you have an `index.js` file, which is included in the header of your `index.html` page.

```js
var app = {

  // Application Constructor
  initialize: function() {
    this.bindEvents();
  },

  // Bind any events that are required.
  // Usually you should subscribe on 'deviceready' event to know, when you can start calling cordova modules
  bindEvents: function() {
    document.addEventListener('deviceready', this.onDeviceReady, false);
  },

  // deviceready Event Handler
  onDeviceReady: function() {
    console.log('Device is ready for work');
  }
};

app.initialize();
```

It is very similar to the default `index.js` file which is generated by Cordova when you create your project. In order to get notified when plugin loads new release - you need to subscribe on `chcp_updateIsReadyToInstall` event like so:

```js
bindEvents: function() {
  // ...some other events subscription code...

  document.addEventListener('chcp_updateIsReadyToInstall', this.onUpdateReady, false);
},
```

and add event handler:
```js
// chcp_updateIsReadyToInstall Event Handler
onUpdateReady: function() {
  console.log('Update is ready for installation');
}
```

The resulting `index.js` will be:
```js
var app = {

  // Application Constructor
  initialize: function() {
    this.bindEvents();
  },

  // Bind any events that are required.
  // Usually you should subscribe on 'deviceready' event to know, when you can start calling cordova modules
  bindEvents: function() {
    document.addEventListener('deviceready', this.onDeviceReady, false);
    document.addEventListener('chcp_updateIsReadyToInstall', this.onUpdateReady, false);
  },

  // deviceready Event Handler
  onDeviceReady: function() {
    console.log('Device is ready for work');
  },

  // chcp_updateIsReadyToInstall Event Handler
  onUpdateReady: function() {
    console.log('Update is ready for installation');
  }
};

app.initialize();
```

From now on we will know, when update is loaded and ready for installation. By using JavaScript module we can force the plugin to install the update right now, even if it was meant to be installed on the next launch.

#### Fetch update

In order to force update check you can call from your web page:
```js
chcp.fetchUpdate(updateCallback);

function updateCallback(error, data) {
  // do some work
}
```

Callback function gets called with two parameters:
- `error` - error if any happened during the update check; `null` if everything went fine;
- `data` - additional data, sent from the native side. For now it can be ignored.

Let's assume that in `index.html` page we have some button, by clicking on which we want to fetch the update. In order to do that we need to:

1. Subscribe on `click` event.
2. Call `chcp.fetchUpdate()` when button is clicked.
3. Handle update result.

So, lets modify our `index.js` file:
```js
var app = {

  // Application Constructor
  initialize: function() {
    this.bindEvents();
  },

  // Bind any events that are required.
  // Usually you should subscribe on 'deviceready' event to know, when you can start calling cordova modules
  bindEvents: function() {
    document.addEventListener('deviceready', this.onDeviceReady, false);
  },

  // deviceready Event Handler
  onDeviceReady: function() {
    // Add click event listener for our update button.
    // We do this here, because at this point Cordova modules are initialized.
    // Before that chcp is undefined.
    document.getElementById('myFetchBtn').addEventListener('click', app.checkForUpdate);
  },

  checkForUpdate: function() {
    chcp.fetchUpdate(this.fetchUpdateCallback);
  },

  fetchUpdateCallback: function(error, data) {
    if (error) {
      console.log('Failed to load the update with error code: ' + error.code);
      console.log(error.description);
    } else {
      console.log('Update is loaded');
    }
  }
};

app.initialize();
```

**Be advised:** even if you call `fetchUpdate` method with a callback function - update related events are still broadcasted.

#### Install update

To install the update you can call:
```js
chcp.installUpdate(installationCallback);

function installationCallback(error) {
  // do some work
}
```
If installation fails - `error` parameter will have the details of what went wrong. Otherwise - it's `null`.

Lets extends previous example and perform the installation as soon as update is loaded.

```js
var app = {

  // Application Constructor
  initialize: function() {
    this.bindEvents();
  },

  // Bind any events that are required.
  // Usually you should subscribe on 'deviceready' event to know, when you can start calling cordova modules
  bindEvents: function() {
    document.addEventListener('deviceready', this.onDeviceReady, false);
  },

  // deviceready Event Handler
  onDeviceReady: function() {
    // Add click event listener for our update button.
    // We do this here, because at this point Cordova modules are initialized.
    // Before that chcp is undefined.
    document.getElementById('myFetchBtn').addEventListener('click', app.checkForUpdate);
  },

  checkForUpdate: function() {
    chcp.fetchUpdate(this.fetchUpdateCallback);
  },

  fetchUpdateCallback: function(error, data) {
    if (error) {
      console.log('Failed to load the update with error code: ' + error.code);
      console.log(error.description);
      return;
    }
    console.log('Update is loaded, running the installation');

    chcp.installUpdate(this.installationCallback);
  },

  installationCallback: function(error) {
    if (error) {
      console.log('Failed to install the update with error code: ' + error.code);
      console.log(error.description);
    } else {
      console.log('Update installed!');
    }
  }
};

app.initialize();
```

**Be advised:** even if you call `installUpdate` method with a callback function - installation related events are still broadcasted.

#### Change plugin preferences on runtime

Normally all plugin preferences are set through the Cordova's `config.xml`. But you can change some of them through the JavaScript module.

In order to do that you can call:
```js
chcp.configure(options, callback);

function callback(error) {
  // do some work
}
```

Supported options:
- `config-file` - url to the application config. If set - this value will be used to check for updates instead of the one in `config.xml`.
- `auto-download` - by setting to `false` you can disable automatic update checks and downloads.
- `auto-install` - by setting to `false` you can disable automatic installations.

Those options must be set on `deviceready` event. You should do that on every page load, because if application gets updated through the store - those options will be overridden with the corresponding values from the `config.xml`.

If you are planning manually call update download/installation - then you should disable auto preferences in the `config.xml`

```xml
<chcp>
  <auto-download enabled="false" />
  <auto-install enabled="false" />
</chcp>
```

instead of doing so on the JS side.

**Important:** You should change those two options to `false` on the runtime **only** if you can't update the app on the store and set them in the `config.xml`. In that case - use them.

Lets say, that we disabled `auto-download` and `auto-install` in the `config.xml`. And at some point `config-file` url has changed, but we don't want to update the app on the store. In that case, we need to do the following:

1. Release new version of the web content, which is available on the previous `config-file` url.
2. In the new release modify `index.js` like so:

  ```js
  var app = {

    // Application Constructor
    initialize: function() {
      this.bindEvents();
    },

    // Bind any events that are required.
    // Usually you should subscribe on 'deviceready' event to know, when you can start calling cordova modules
    bindEvents: function() {
      document.addEventListener('deviceready', this.onDeviceReady, false);
    },

    // deviceready Event Handler
    onDeviceReady: function() {
      // change plugin options
      app.configurePlugin();
    },

    configurePlugin: function() {
      var options = {
        'config-file': 'https://mynewdomain.com/some/path/mobile/chcp.json'
      };

      chcp.configure(options, configureCallback);
    },

    configureCallback: function(error) {
      if (error) {
        console.log('Error during the configuration process');
        console.log(error.description);
      } else {
        console.log('Plugin configured successfully');
        app.checkForUpdate();
      }
    },

    checkForUpdate: function() {
      chcp.fetchUpdate(this.fetchUpdateCallback);
    },

    fetchUpdateCallback: function(error, data) {
      if (error) {
        console.log('Failed to load the update with error code: ' + error.code);
        console.log(error.description);
        return;
      }
      console.log('Update is loaded, running the installation');

      chcp.installUpdate(this.installationCallback);
    },

    installationCallback: function(error) {
      if (error) {
        console.log('Failed to install the update with error code: ' + error.code);
        console.log(error.description);
      } else {
        console.log('Update installed!');
      }
    }
  };

  app.initialize();
  ```

By doing so we, at first, tell the plugin to work with the new `config-file`, and as soon as he is configured - fetch and install the update.

#### Request application update through the store

As stated in [Application config](#application-config) section we can set minimum required version of the native side for our web releases (`min_native_interface` preference). When plugin loads new application config from the server and sees that current build version of the app is too low - it finishes with error code `chcp.error.APPLICATION_BUILD_VERSION_TOO_LOW`. By checking the error code on the JavaScript side we can understand that and request user to update the app through the store (Google Play or App Store).

You can do that anyway you want. The most standard approach is to show dialog with update request message and two buttons: first redirects user to the store, and the second closes the dialog. Our plugin helps you do exactly this.

All you need to do is:

1. In your application config set `android_identifier` and `ios_identifier` preferences.
2. On JavaScript side capture corresponding update error and call `chcp.requestApplicationUpdate` method.

Time for some example. For simplicity we will subscribe on `chcp_updateLoadFailed` event.

```js
var app = {

  // Application Constructor
  initialize: function() {
    this.bindEvents();
  },

  // Bind any events that are required.
  // Usually you should subscribe on 'deviceready' event to know, when you can start calling cordova modules
  bindEvents: function() {
    document.addEventListener('deviceready', this.onDeviceReady, false);
    document.addEventListener('chcp_updateLoadFailed', this.onUpdateLoadError, false);
  },

  // deviceready Event Handler
  onDeviceReady: function() {
  },

  onUpdateLoadError: function(eventData) {
    var error = eventData.detail.error;
    if (error && error.code == chcp.error.APPLICATION_BUILD_VERSION_TOO_LOW) {
        console.log('Native side update required');
        var dialogMessage = 'New version of the application is available on the store. Please, update.';
        chcp.requestApplicationUpdate(dialogMessage, this.userWentToStoreCallback, this.userDeclinedRedirectCallback);
    }
  },

  userWentToStoreCallback: function() {
    // user went to the store from the dialog
  },

  userDeclinedRedirectCallback: function() {
    // User didn't want to leave the app.
    // Maybe he will update later.
  }
};

app.initialize();
```

#### Error codes

During the update download/installation process some errors can occur. You can match error code from the callback/event to the properties in `chcp.error` object.

Before v1.2.0 you had to use actual values. From now on, please, use named constants to make your code more readable and less dependent on the actual values. For example, instead of `if (error.code == -2)` use `if (error.code == chcp.error.APPLICATION_BUILD_VERSION_TOO_LOW)`.

List of errors:

- `NOTHING_TO_INSTALL` - installation request was sent to the plugin, but there is nothing to install. Error code value is `1`.
- `NOTHING_TO_UPDATE` - nothing new is available for download. Error code value is `2`.
- `FAILED_TO_DOWNLOAD_APPLICATION_CONFIG` - failed to download new application config from the server. Either file doesn't exist or some internet connection problems. Error code value is `-1`.
- `APPLICATION_BUILD_VERSION_TOO_LOW` - application's build version is too low for this update. New web release requires newer version of the app. User must update it through the store. Error code value is `-2`.
- `FAILED_TO_DOWNLOAD_CONTENT_MANIFEST` - failed to download new content manifest file from the server. Check that `chcp.manifest` file is placed in the root of your `content_url`, specified in the application config. Error code value is `-3`.
- `FAILED_TO_DOWNLOAD_UPDATE_FILES` - failed to download updated/new files from the server. Check your `chcp.manifest` file: all listed files must be placed in the `content_url` from the application config. Also, check their hashes: they must match to the hashes in the `chcp.manifest`. Error code value is `-4`.
- `FAILED_TO_MOVE_LOADED_FILES_TO_INSTALLATION_FOLDER` - failed to move downloaded files to the installation folder. Can occur when there is no free space on the device. Error code value is `-5`.
- `UPDATE_IS_INVALID` - update package is broken. Before installing anything plugin validates downloaded files once more by checking their hashes with the one that specified in the loaded `chcp.manifest` file. If they doesn't match or we are missing some file - this error is thrown. Error code value is `-6`.
- `FAILED_TO_COPY_FILES_FROM_PREVIOUS_RELEASE` - failed to copy `www` folder files from the previous release to the new release folder. Can occur if device is out of free space. Error code value is `-7`.
- `FAILED_TO_COPY_NEW_CONTENT_FILES` - failed to copy new files to content directory. Can occur during the installation if there is not enough free space on device storage. Error code value is `-8`.
- `LOCAL_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND` - failed to load current application config from the local storage. Can occur if user manually deleted plugin working directories from the external storage. If so - we will try to rollback to the previous/bundled version. Error code value is `-9`.
- `LOCAL_VERSION_OF_MANIFEST_NOT_FOUND` - failed to load current manifest file from the local storage. Can occur if user manually deleted plugin working directories from the external storage. If so - we will try to rollback to the previous/bundled version. Error code value is `-10`.
- `LOADED_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND` - failed to load new version of the application config from download folder (local storage). Can occur on installation process if user deletes plugin working directories from the external storage. If so - folders will be restored on the next launch. Error code value is `-11`.
- `LOADED_VERSION_OF_MANIFEST_NOT_FOUND` - failed to load new version of the content manifest from download folder (local storage). Can occur on installation process if user deletes plugin working directories from the external storage. If so - folders will be restored on the next launch. Error code value is `-12`.
- `FAILED_TO_INSTALL_ASSETS_ON_EXTERNAL_STORAGE` - failed to copy web project files from application bundle into external storage. Can occur if there is not enough free space on the users device. Action is performed on the first launch of the application. If it fails - plugin can't do it's job. Error code value is `-13`.
- `CANT_INSTALL_WHILE_DOWNLOAD_IN_PROGRESS` - error is thrown when we try to call `chcp.installUpdate` while update download is in progress. You will have to wait until download is done. Error code value is `-14`.
- `CANT_DOWNLOAD_UPDATE_WHILE_INSTALLATION_IN_PROGRESS` - error is thrown when we try to call `chcp.fetchUpdate` while installation is in progress. You will have to wait until installation is done. Error code value is `-15`.
- `INSTALLATION_ALREADY_IN_PROGRESS` - error is thrown when we try to call `chcp.installUpdate`, but installation is already in progress. Error code value is `-16`.
- `DOWNLOAD_ALREADY_IN_PROGRESS` - error is thrown when we try to call `chcp.fetchUpdate`, but download is already in progress. Error code value is `-17`.
- `ASSETS_FOLDER_IN_NOT_YET_INSTALLED` - error usually occur when we try to call `chcp` methods, while plugin is copying bundled sources on the external storage. This can happen only on the very first launch. Eventually this error will be removed. Error code value is `-18`.
