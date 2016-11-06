# Change Log

## 1.5.2 (2016-11-06)

**Bug fixes:**

- [Issue #213](https://github.com/nordnet/cordova-hot-code-push/issues/213). Large sync lead to timeout on iOS. Merged [pull request #217](https://github.com/nordnet/cordova-hot-code-push/pull/217), thanks to [@bbreijer](https://github.com/bbreijer). Now iOS version should be more memory efficient when loading large set of files. It also can now download large files.
- Merged [pull request #218](https://github.com/nordnet/cordova-hot-code-push/pull/218). Fixes build failure on the after_prepare hook. Thanks to [@warent](https://github.com/warent).
- [Issue #228](https://github.com/nordnet/cordova-hot-code-push/issues/228). Added missing error code `NEW_APPLICATION_CONFIG_IS_INVALID` to JS API.
- [Issue #229](https://github.com/nordnet/cordova-hot-code-push/issues/229). Thanks to [@zwwhappy](https://github.com/zwwhappy) for [pull request #225](https://github.com/nordnet/cordova-hot-code-push/pull/225), and [@sfnt](https://github.com/sfnt) for [pull request #231](https://github.com/nordnet/cordova-hot-code-push/pull/231).

## 1.5.1 (2016-09-06)

**Bug fixes:**

- Merged [pull request #205](https://github.com/nordnet/cordova-hot-code-push/pull/205). Fixes Android app crashes, when `content_url` is missing in server's `chcp.json` config. Thanks to [@Mikey1982](https://github.com/Mikey1982).
- [Issue #208](https://github.com/nordnet/cordova-hot-code-push/issues/208). In some cases `plugins/ios.json` file couldn't be found by the hook, that checks, if `cordova-plugin-wkwebview-engine` plugin is installed in the project. Now hook will check for the plugin's folder directly.

## 1.5.0 (2016-08-30)

**Bug fixes:**

- [Issue #197](https://github.com/nordnet/cordova-hot-code-push/issues/197). Android can now update files with spaces in their names.

**Improvements:**

- Merged [pull request #204](https://github.com/nordnet/cordova-hot-code-push/pull/204). Initial installation of the assets on the external storage on Android is now much faster. Thanks to [@Mikey1982](https://github.com/Mikey1982).
- Added new JS method: `getVersionInfo`. Based on [pull request #170](https://github.com/nordnet/cordova-hot-code-push/pull/170) from [@Manduro](https://github.com/Manduro) - thank you. For documentation, please, refer to [wikki page](https://github.com/nordnet/cordova-hot-code-push/wiki/Get-version-information).

## 1.4.0 (2016-06-21)

**Bug fixes:**

- [Issue #155](https://github.com/nordnet/cordova-hot-code-push/issues/155). Android app should not crash if server has a bad `chcp.json` file.

**Improvements:**

- [Issue #153](https://github.com/nordnet/cordova-hot-code-push/issues/153). You can now pass `chcp.json` file url into `fetchUpdate` method on JS side. Also, you can provide additional HTTP headers to the request. For example, authorization info. These headers will be used for loading configuration files and updated/changed files from your server. Thanks to [@davidovich](https://github.com/davidovich) for [pull request #150](https://github.com/nordnet/cordova-hot-code-push/pull/150).
- [Issue #99](https://github.com/nordnet/cordova-hot-code-push/issues/99). iOS build version can now be a string. So, changing it from 1.0.0 to 1.0.1 will trigger reinstallation of `www` folder. Before that it had to be an integer.
- [Issue #160](https://github.com/nordnet/cordova-hot-code-push/issues/160). Old releases are now gets removed after update installation. Previously cleanup was performed only on application restart.

## 1.3.2 (2016-04-27)

**Bug fixes:**

- [Issue #137](https://github.com/nordnet/cordova-hot-code-push/issues/137). Fixed installation failure on Windows. Thanks to [@oxiao](https://github.com/oxiao) for providing the fix.

## 1.3.1 (2016-04-25)

**Bug fixes:**

- [Issue #139](https://github.com/nordnet/cordova-hot-code-push/issues/139). If you don't have `cordova-hcp` client - installation will not fail.
- Fixed typo in `before_plugin_install` hook. If installation failed - it will now throw a correct error.

**Enhancements:**

- iOS. Switched from custom API version check macro to the Cordova's one: `__CORDOVA_4_0_0`. Less JS code in the hooks, more proper API switching in the code.

**Docs:**

- Minified readme file. Moved documentation into [Wiki](https://github.com/nordnet/cordova-hot-code-push/wiki).

## 1.3.0 (2016-04-22)

**Enhancements:**

- Merged [pull request #101](https://github.com/nordnet/cordova-hot-code-push/pull/101). Plugin now dispatches `chcp_beforeInstall` event before installing new update. Thanks to [@toostn](https://github.com/toostn).
- Merged [pull request #107](https://github.com/nordnet/cordova-hot-code-push/pull/107). In Android we now set connection timeout for configs/files download, so the process would not hang. Thanks to [@kenvuz](https://github.com/kenvunz).
- [Issue #105](https://github.com/nordnet/cordova-hot-code-push/issues/105). Plugin now dispatches `chcp_beforeAssetsInstalledOnExternalStorage` event, when it's about to install assets on external storage.
- [Issue #102](https://github.com/nordnet/cordova-hot-code-push/issues/102). You can now check from the JS side, if update was loaded on the device and ready to be installed. Later on will be added method to check update availability on the server side.
- [Issue #45](https://github.com/nordnet/cordova-hot-code-push/issues/45). Added `<native-interface version="" />` tag to define application's native interface version. Plugin will now use it instead of the application's code version.
- [Issue #85](https://github.com/nordnet/cordova-hot-code-push/issues/85). Adde support for `cordova-plugin-wkwebview-engine` plugin.
- Dependency node modules are now installed in the plugin's folder, instead of the project's root folder.

**Docs:**

- Added info about migrating to v1.3.0 in `Migrating from previous version` section.
- Added `Check if update was loaded and ready to be installed` section.
- Updated `Cordova config preferences` section.
- Updated `JavaScript module` section.

## 1.2.6 (2016-04-11)

**Bug fixes:**

- Fixed [issue #129](https://github.com/nordnet/cordova-hot-code-push/issues/129). `plugin.xml` was missing scheme for android. Thanks to [@Christianuchermannuc](https://github.com/Christianuchermannuc).
- Fixed [issue #132](https://github.com/nordnet/cordova-hot-code-push/issues/132). Problem was with the EventBus on Android: package name has changed.
- Fixed [issue #109](https://github.com/nordnet/cordova-hot-code-push/issues/109). In the plugin's header file iOS Cordova platform version is now set to `4` by default. Previously it was `3`.

## 1.2.5 (2016-02-05)

**Bug fixes:**

- Fixed [issue #91](https://github.com/nordnet/cordova-hot-code-push/issues/91). Build options now will be merged into existing preferences from the `config.xml` instead of changing them all.
- Fixed [issue #93](https://github.com/nordnet/cordova-hot-code-push/issues/93). Plugin will not reload Android application to index page, when app is resumed from the background state.
- Fixed [issue #97](https://github.com/nordnet/cordova-hot-code-push/issues/97).
- Merged [PR #94](https://github.com/nordnet/cordova-hot-code-push/pull/94). Android version of the plugin should now be compatible with other plugins, that are using EventBus. Thanks to [@Steffaan](https://github.com/Steffaan).

## 1.2.4 (2016-01-17)

**Bug fixes:**

- Fixed [issue #84](https://github.com/nordnet/cordova-hot-code-push/issues/84).
- Fixed [issue #88](https://github.com/nordnet/cordova-hot-code-push/issues/88).
- Fixed [issue #90](https://github.com/nordnet/cordova-hot-code-push/issues/90).

## 1.2.3 (2016-01-12)

**Bug fixes:**

- Fixed [issue #83](https://github.com/nordnet/cordova-hot-code-push/issues/83).
- Fixed Cordova 5.4 warnings in the hooks.

## 1.2.2 (2016-01-11)

**Bug fixes:**

- Fixed the issue, where plugin failed to update, when there was a `#` or `?` in the index page. For example `<content src="index.html#/tabs/dash/" />`
- Fixed deletion of the old releases.

## 1.2.1 (2016-01-07)

**Bug fixes:**

- Fixed application crash when we install new app with v1.2.0 on the device with the previous version of the plugin.

## 1.2.0 (2016-01-07)

You can find full list of changes below. But in short, this update brings two main things.

First of all, plugin now supports Cordova iOS platform v4, and can still be used on v3. This is achieved by the hook, that is executed when you build your project. It reads `platforms/package.json` and depending on the `ios` version - set's native macro in the plugin sources, which will switch the used API.

Second one is aimed to make updates more stable and more bulletproof, plus fix files caching on iOS. This was achieved by changing the way on how project files are stored on the external storage. From now on each release has it's own `www` folder. You can read about this in more details in the `How web project files are stored and updated` section of the readme.

**Bug fixes:**

- Fixed [issue #47](https://github.com/nordnet/cordova-hot-code-push/issues/47). Changed the way the files are stored on the external storage. As a result, webview cache is now flushed automatically.
- Fixed [issue #64](https://github.com/nordnet/cordova-hot-code-push/issues/64). Plugin now supports Cordova iOS platform v4, and also backwards compatible.
- Fixed [issue #70](https://github.com/nordnet/cordova-hot-code-push/issues/70). Empty `<chcp />` tag in `plugin.xml` could sometimes lead to the build failures. Thanks to [@alexbuijs](https://github.com/alexbuijs) for the pull request.
- Fixed [issue #72](https://github.com/nordnet/cordova-hot-code-push/issues/72). On Android `auto-download` preference from the `config.xml` is not ignored any more.
- Fixed [issue #73](https://github.com/nordnet/cordova-hot-code-push/issues/73). `chcp.configure` callback is now gets called.
- Fixed [issue #77](https://github.com/nordnet/cordova-hot-code-push/issues/77). On iOS `chcp.configure` now works the same way, as on Android: it accepts an object as an options, not a string.
- Fixed potential rushing conditions on update download/installations. Right now there can be only one running update task.

**Enhancements:**

- Changed the way on how web files are stored on the external storage. That made updates more stable, plus fixes the caching issue.
- Merged [pull request #67](https://github.com/nordnet/cordova-hot-code-push/pull/67). If code version of the application has changed (incresed or decreased) - it is considered as updated and `www` folder is reinstalled on the external storage. Thanks to [@hassellof](https://github.com/hassellof).
- Merged [pull request #71](https://github.com/nordnet/cordova-hot-code-push/pull/71). Removed empty `<chcp />` tag from the `plugin.xml` config. Thanks to [@alexbuijs](https://github.com/alexbuijs).
- Added `chcp.error` object in JS module of the plugin, so you could use it to match the errors by names instead of codes.

**Docs:**

- Updated `Ionic quick start guide` section.
- Updated `How web project files are stored and updated` section.
- Updated `Change plugin preferences on runtime` section.
- Updated `Error codes` section.

## 1.1.2 (2015-12-2)

**Bug fixes:**

- Android. Fixed [issue #43](https://github.com/nordnet/cordova-hot-code-push/issues/43). Merged [pull request #44](https://github.com/nordnet/cordova-hot-code-push/pull/44). Thanks to [@andreialecu](https://github.com/andreialecu) for helping to fix that problem.
- iOS. Fixed [issue #53](https://github.com/nordnet/cordova-hot-code-push/issues/53).
- iOS. Fixed rollback procedure, if update installation has failed.
- iOS. Fixed issue, when update failed to install if it contained unexisting folders. Thanks to [@legege](https://github.com/legege) for providing [pull request #50](https://github.com/nordnet/cordova-hot-code-push/pull/50).
- Fixed [issue #49](https://github.com/nordnet/cordova-hot-code-push/issues/49) and [issue #48](https://github.com/nordnet/cordova-hot-code-push/issues/48). Added support for Cordova v5.4.
- Other fixes for both platforms, that aimed to make update procedure safer for the user.

**Enhancements:**

- Merged [pull request #40](https://github.com/nordnet/cordova-hot-code-push/pull/40). Preparation to add auth headers to the network requests. Thanks to [@davidovich](https://github.com/davidovich).
- Merged [pull request #52](https://github.com/nordnet/cordova-hot-code-push/pull/52). Thanks to [@legege](https://github.com/legege).

**Docs:**

- Documentation updates regarding `min_native_interface` for Android. Thanks to [@andreialecu](https://github.com/andreialecu) for providing [pull request #46](https://github.com/nordnet/cordova-hot-code-push/pull/46).

## 1.1.1 (2015-11-12)

Release holds only some documentation updates:

- Added section: `Migrating from previous version`.
- Small update to quick start guides.

## 1.1.0 (2015-11-10)

**Enhancements:**

- Moved local development mode to the separate plugin: https://github.com/nordnet/cordova-hot-code-push-local-dev-addon
- Node modules installed only once after plugin is added. Installation of the new plugin is not gonna trigger it, as before.
- You can now build your project on Xcode 6.4, as long as you don't use local development add-on. [Issue #17](https://github.com/nordnet/cordova-hot-code-push/issues/17).
- Minor cleanup and fixes.

**Bug fixes:**

Since local development mode was moved to the separate plugin - that should fix the [issue #26](https://github.com/nordnet/cordova-hot-code-push/issues/26) with the Swift on iOS, when it lead for the app to be rejected by the Apple. When development is done - just remove development plugin and that's it. For more information please visit [documentation page](https://github.com/nordnet/cordova-hot-code-push-local-dev-addon) of the local development add-on.

## 1.0.5 (2015-11-03)

**Bug fixes:**

- Fixed [issue #31](https://github.com/nordnet/cordova-hot-code-push/issues/31). Additional checking that `www` folder was installed on the external storage.

## 1.0.4 (2015-10-23)

**Bug fixes:**

- iOS. Fixed [issue #2](https://github.com/nordnet/cordova-hot-code-push/issues/2). Ionic application should refresh it's content and ignore UIWebView cache.

**Enhancements:**

- Merged [pull request #27](https://github.com/nordnet/cordova-hot-code-push/pull/27). Error message now has an url of the file that we failed to load from the server. Thanks to [ptarjan](https://github.com/ptarjan).
- Merged [pull request #29](https://github.com/nordnet/cordova-hot-code-push/pull/29). Error message now has an url of the config file that we failed to load from the server. Thanks to [ptarjan](https://github.com/ptarjan).

## 1.0.3 (2015-10-02)

**Bug fixes:**

- Fixed [issue #8](https://github.com/nordnet/cordova-hot-code-push/issues/8). `ionic plugin add cordova-hot-code-push` will not hang. But from now on you will have to install CLI client manually.
- Fixed [issue #11](https://github.com/nordnet/cordova-hot-code-push/issues/11). `ionic state restore` will not hang any more.
- Fixed [issue #13](https://github.com/nordnet/cordova-hot-code-push/issues/13). Plugin is not gonna crash your application, if `content_url` is not set in the application config. Although, you should always set `content_url` in order for plugin to work.

**Enhancements:**

- As asked in [issue #12](https://github.com/nordnet/cordova-hot-code-push/issues/12) - plugin is not gonna show unnecessary messages in the console.
- Changed workflow on `chcpbuild.options`. From now on you should add `chcp-` prefix to the build options, when you use them with the `build` command: `cordova build -- chcp-buildOptionName`. More info can be found in the Readme.
- Plugin installation script now takes node dependencies from the package.json.
- Removed unused node dependencies.

**Docs:**

- Updated `Installation` section.
- Updated `Quick start guides`.
- Updated `Build options` section.
- Added `Update workflow` section.
- Some gramma updates thanks to [@ptarjan](https://github.com/ptarjan).

## 1.0.2 (2015-09-21)

**Bug fixes:**
- Fixed [issue #3](https://github.com/nordnet/cordova-hot-code-push/issues/3). Updated socket.io client: now it builds with Xcode 7.

## 1.0.1 (2015-09-08)

- Added change log file.
- Updated readme file.

**Bug fixes:**

- iOS. Fixed issue with local development mode. Now it is disabled by default.
- iOS. Fixed application crashes when plugin tries to connect to local server, but `config-file` preference is not set in `config.xml`.
- iOS. Fixed Swift header duplicates issue which occur when you change your project name.
- iOS. Fixed Swift header name generation: now based on project module name as it should be.
- Now it works with Ionic framework. Although, some minor issues still remains.
