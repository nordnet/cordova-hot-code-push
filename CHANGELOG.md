# Change Log

## 1.1.2 (2015-12-2)

**Bug fixes:**

* Android. Fixed [issue #43](https://github.com/nordnet/cordova-hot-code-push/issues/43). Merged [pull request #44](https://github.com/nordnet/cordova-hot-code-push/pull/44). Thanks to [@andreialecu](https://github.com/andreialecu) for helping to fix that problem.
* iOS. Fixed [issue #53](https://github.com/nordnet/cordova-hot-code-push/issues/53).
* iOS. Fixed rollback procedure, if update installation has failed.
* iOS. Fixed issue, when update failed to install if it contained unexisting folders. Thanks to [@legege](https://github.com/legege) for providing [pull request #50](https://github.com/nordnet/cordova-hot-code-push/pull/50).
* Fixed [issue #49](https://github.com/nordnet/cordova-hot-code-push/issues/49) and [issue #48](https://github.com/nordnet/cordova-hot-code-push/issues/48). Added support for Cordova v5.4.
* Other fixes for both platforms, that aimed to make update procedure safer for the user.

**Enchancements:**

* Merged [pull request #40](https://github.com/nordnet/cordova-hot-code-push/pull/40). Preparation to add auth headers to the network requests. Thanks to [@davidovich](https://github.com/davidovich).
* Merged [pull request #52](https://github.com/nordnet/cordova-hot-code-push/pull/52). Thanks to [@legege](https://github.com/legege).

**Docs:**

* Documentation updates regarding `min_native_interface` for Android. Thanks to [@andreialecu](https://github.com/andreialecu) for providing [pull request #46](https://github.com/nordnet/cordova-hot-code-push/pull/46).

## 1.1.1 (2015-11-12)

Release holds only some documentation updates:

- Added section: `Migrating from previous version`.
- Small update to quick start guides.

## 1.1.0 (2015-11-10)

**Enchancements:**

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

**Enchancements:**

- Merged [pull request #27](https://github.com/nordnet/cordova-hot-code-push/pull/27). Error message now has an url of the file that we failed to load from the server. Thanks to [ptarjan](https://github.com/ptarjan).
- Merged [pull request #29](https://github.com/nordnet/cordova-hot-code-push/pull/29). Error message now has an url of the config file that we failed to load from the server. Thanks to [ptarjan](https://github.com/ptarjan).

## 1.0.3 (2015-10-02)

**Bug fixes:**

- Fixed [issue #8](https://github.com/nordnet/cordova-hot-code-push/issues/8). `ionic plugin add cordova-hot-code-push` will not hang. But from now on you will have to install CLI client manually.
- Fixed [issue #11](https://github.com/nordnet/cordova-hot-code-push/issues/11). `ionic state restore` will not hang any more.
- Fixed [issue #13](https://github.com/nordnet/cordova-hot-code-push/issues/13). Plugin is not gonna crash your application, if `content_url` is not set in the application config. Although, you should always set `content_url` in order for plugin to work.

**Enchancements:**

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
