# Change Log

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
