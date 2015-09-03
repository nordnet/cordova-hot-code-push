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
  - [Listen for update events](#listen-for-update-events)
  - [Fetch update](#fetch-update)
  - [Install update](#install-update)
  - [Change plugin preferences on runtime](#change-plugin-preferences-on-runtime)
  - [Request application update through the store] (#request-application-update-through-the-store)
- [Error codes](#error-codes)

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

But what it you want to change it on the build phase using command line? For that purpose you can use `chcpbuild.options` file.

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
    "config-file": "https://5027caf9.ngrok.com/chcp.json",
    "local-development": {
      "enabled": "false"
    }
  },
  "production": {
    "config-file": "https://company_server.com/mobile/www/chcp.json",
  },
  "QA": {
    "config-file": "https://test.company_server.com/mobile/www/chcp.json"
  }
}
```

In order to build the app, configured to work with development server we can run command:
```sh
cordova build -- dev
```
As a result, platform-specific `config.xml` file (for example, `/Cordova/TestProject/platforms/android/res/xml/config.xml`) will have:
```xml
<chcp>
  <config-file url="https://5027caf9.ngrok.com/chcp.json"/>
  <local-development enabled="false"/>
</chcp>
```

As soon as application is ready for testing - we can build it, configured to work with test server:
```sh
cordova build -- QA
```
As a result, plugin-specific `config.xml` file will have:
```xml
<chcp>
  <config-file url="https://test.company_server.com/mobile/www/chcp.json"/>
</chcp>
```

As soon as we want to publish our app on the store (Google Play, App Store) we build, as usual, with command:
```sh
cordova build --release
```
In that case we are not touching `config.xml` in any way.

### Cordova Hot Code Push CLI client

Cordova Hot Code Push CLI client is a command line utility that will help you with development and deployment of your web project.

With it you can:
- generate both `chcp.json` and `chcp.manifest` files, so you wouldn't have to do it manually;
- run local server in order to detect any changes you make in your web project and instantly upload them on the devices;
- deploy your web project on the external server.

Of course, you can use Hot Code Push plugin without that utility. But it will make it much easier.

It's repository and documentation can be found [here](https://github.com/nordnet/cordova-hot-code-push-cli).

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
    document.getElementById("myFetchBtn").addEventListener("click", app.checkForUpdate);
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
    document.getElementById("myFetchBtn").addEventListener("click", app.checkForUpdate);
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

`auto-download` and `auto-install` can be used when you want to perform update download and installation manually. Let us extend previous example with `configure` method:

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

    // Add click event listener for our update button.
    // We do this here, because at this point Cordova modules are initialized.
    // Before that chcp is undefined.
    document.getElementById('myFetchBtn').addEventListener('click', app.checkForUpdate);
  },

  configurePlugin: function() {
    var options = {
      'auto-download': false,
      'auto-install': false
    };

    chcp.configure(options, configureCallback);
  },

  configureCallback: function(error) {
    if (error) {
      console.log('Error during the configuration process');
      console.log(error.description);
    } else {
      console.log('Plugin configured successfully');
      // from this moment only we control when to download and install new releases
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

#### Request application update through the store

As stated in [Application config](#application-config) section we can set minimum required version of the native side for our web releases (`min_native_interface` preference). When plugin loads new application config from the server and sees that current build version of the app is too low - it finishes with error code `-2`. By checking the error code on the JavaScript side we can understand that and request user to update the app through the store (Google Play or App Store).

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
    var error = eventData.details.error;
    if (error && error.code == -2) {
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

During the update download/installation process some errors can occur. To identify them you can use error codes that are listed below.

- `-1` - failed to download new application config from the server. Either file doesn't exist or some internet connection problems.
- `-2` - application's build version is too low for this update. New web release requires newer version of the app. User must update it through the store.
- `-3` - failed to download new content manifest file from the server. Check that `chcp.manifest` file is placed in the root of your `content_url`, specified in the application config.
- `-4` - failed to download updated/new files from the server. Check your `chcp.manifest` file: all listed files must be placed in the `content_url` from the application config. Also, check their hashes: they must match to the hashes in the `chcp.manifest`.
- `-5` - failed to move downloaded files to the installation folder. Can occur when there is no free space on the device.
- `-6` - update package is broken. Before installing anything plugin validates downloaded files once more by checking their hashes with the one that specified in the loaded `chcp.manifest` file. If they doesn't match or we are missing some file - this error is thrown.
- `-7` - could not create backup before the installation. Can occur if device is out of free space. We need a backup in the case if something will go wrong during the installation so we could rollback.
- `-8` - failed to copy new files to content directory. Can occur during the installation if there is not enough free space on device storage.
- `-9` - failed to load current application config from the local storage. Can occur if user manually deleted plugin working directories from the external storage. If so - folders will be restored on the next launch.
- `-10` - failed to load current manifest file from the local storage. Can occur if user manually deleted plugin working directories from the external storage. If so - folders will be restored on the next launch.
- `-11` - failed to load new version of the application config from download folder (local storage). Can occur on installation process if user deletes plugin working directories from the external storage. If so - folders will be restored on the next launch.
- `-12` - failed to load new version of the content manifest from download folder (local storage). Can occur on installation process if user deletes plugin working directories from the external storage. If so - folders will be restored on the next launch.
- `-13` - failed to copy web project files from application bundle into external storage. Can occur if there is not enough free space on the users device. Action is performed on the first launch of the application. If it fails - plugin can't do it's job.
