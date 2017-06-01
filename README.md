# Cordova Hot Code Push Plugin

This plugin provides functionality to perform automatic updates of the web based content in your application. Basically, everything that is stored in `www` folder of your Cordova project can be updated using this plugin.

When you publish your application on the store - you pack in it all your web content: html files, JavaScript code, images and so on. There are two ways how you can update it:

1. Publish new version of the app on the store. But it takes time, especially with the App Store.
2. Sacrifice the offline feature and load all the pages online. But as soon as Internet connection goes down - application won't work.

This plugin is intended to fix all that. When user starts the app for the first time - it copies all the web files onto the external storage. From this moment all pages are loaded from the external folder and not from the packed bundle. On every launch plugin connects to your server (with optional authentication, see fetchUpdate() below) and checks if the new version of web project is available for download. If so - it loads it on the device and installs on the next launch.

As a result, your application receives updates of the web content as soon as possible, and still can work in offline mode. Also, plugin allows you to specify dependency between the web release and the native version to make sure, that new release will work on the older versions of the application.

**Is it fine with App Store?** Yes, it is... as long as your content corresponds to what application is intended for and you don't ask user to click some button to update the web content. For more details please refer to [this wiki page](https://github.com/nordnet/cordova-hot-code-push/wiki/App-Store-FAQ).

## Supported platforms

- Android 4.0.0 or above.
- iOS 7.0 or above. Xcode 7 is required.

### Installation

This requires cordova 5.0+ (current stable 1.5.3)

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

### Quick start guide

In this guide we will show how quickly you can test this plugin and start using it for development. For that we will install [development add-on](https://github.com/nordnet/cordova-hot-code-push/wiki/Local-Development-Plugin).

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

For production build do not forget to add the following to your `config.xml` file as it is a required property. Checkout the [wiki](https://github.com/nordnet/cordova-hot-code-push/wiki/Cordova-config-preferences) for more information:

```xml
<chcp>
    <config-file url="https://5027caf9.ngrok.com/chcp.json"/>
</chcp>
```

### Documentation

All documentation can be found in details in our [Wiki on GitHub](https://github.com/nordnet/cordova-hot-code-push/wiki).

If you have some questions/problems/suggestions - don't hesitate to post a [thread](https://github.com/nordnet/cordova-hot-code-push/issues). If it's an actual issue - please, follow [this guide](https://github.com/nordnet/cordova-hot-code-push/wiki/Issue-creation-guide) on how to do that properly.
