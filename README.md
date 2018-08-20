# 변경점
1. config.xml을 통한 백업 삭제여부 설정가능  
 true를 주면 자동으로 백업을 삭제한다.
```xml
<chcp>
  <remove-backup enabled="true" />  <!-- default false --> 
</chcp>
````

# Cordova Hot Code Push Plugin

* google 번역기를 이용해 간단하게 번역하고 간단하게 의역해 알아보기 쉽게 변경함
공식문서의 [wiki](https://github.com/nordnet/cordova-hot-code-push/wiki)를 통해 보다 자세한정보를 얻는것이 좋다

이 플러그인은 응용 프로그램의 웹 기반 컨텐츠를 자동으로 업데이트하는 기능을 제공합니다. 
기본적으로 Cordova 프로젝트의`www` 폴더에 저장된 모든 것들은이 플러그인을 사용하여 업데이트 될 수 있습니다.

저장소에 응용 프로그램을 게시 할 때 - HTML 파일, JavaScript 코드, 이미지 등 모든 웹 컨텐츠를 패키지에 저장합니다. 
업데이트 방법에는 두 가지가 있습니다.

1. 마켓에 앱의 새 버전을 게시하십시오. 그러나 앱 스토어에서는 특히 시간이 걸립니다.
2. 오프라인 기능을 희생하고 모든 페이지를 온라인으로 로드하십시오. 그러나 인터넷 연결이 끊어지면 응용 프로그램이 작동하지 않습니다.

이 플러그인은 모든 문제를 해결하기위한 것입니다. 사용자가 앱을 처음 시작하면 모든 웹 파일이 외부 저장소에 복사됩니다. 
이 순간부터 모든 페이지는 패키징 된 번들이 아닌 외부 폴더에서 로드됩니다.
매 실행시 플러그인에서 서버(옵션 인증 사용, 아래 fetchUpdate () 참조)에 연결하고 새 버전의 웹 프로젝트를 다운로드 할 수 있는지 확인합니다.
다운로드가 가능하다면 장치에 로드하고 다음 실행에 설치합니다.

결과적으로 당신의 어플리케이션은 최대한 빠르게 웹 컨텐츠의 업데이트를 받아, 오프라인 모드로 작업 할 수 있습니다.
또한 플러그인을 사용하면 웹 Release와 네이티브 버전 간의 종속성을 지정하여 새 버전이 이전 버전의 응용 프로그램에서 작동하도록 할 수 있습니다.

**App Store에서 사용 가능한가요?** 
예, 어플리케이션이 의도 한 내용과 컨텐츠가 일치하고 사용자가 웹 컨텐츠를 업데이트하기 위해 일부 버튼을 클릭하도록 요청하지 않는 한 가능합니다.
자세한 내용은 [wiki 페이지](https://github.com/nordnet/cordova-hot-code-push/wiki/App-Store-FAQ)를 참조하십시오.

## 지원 플랫폼
- Android 4.0.0 또는 상위버전.
- iOS 7.0 또는 상위버전. Xcode 7 필요.

### 설치

이 플러그인은 cordova 5.0+ (current stable 1.5.3)을 사용합니다

```sh
cordova plugin add cordova-hot-code-push-plugin
```

repo url을 통해 직접 설치할 수도 있습니다 (__unstable__)
```sh
cordova plugin add https://github.com/nordnet/cordova-hot-code-push.git
```


설치가 끝나면 [Cordova Hot Code Push CLI 클라이언트](https://github.com/nordnet/cordova-hot-code-push-cli)를 설치하는 것이 좋습니다.
이 클라이언트는 다음과 같은 기능을 제공합니다:
- 필요한 구성 파일을 쉽게 생성 할 수 있습니다
- 로컬 서버를 시작하여 웹 프로젝트의 모든 변경 사항을 수신하고 새 버전을 앱에 즉시 배포합니다.

물론 CLI 클라이언트없이 이 플러그인을 사용할 수는 있지만, 사용하는 편이 훨씬 더 편리합니다.

### 빠른 시작 가이드

이 가이드에서는 이 플러그인을 테스트하고, 개발을 위해 얼마나 빨리 사용할 수 있는지 보여줍니다.
이를 위해 우리는 [Development add-on](https://github.com/nordnet/cordova-hot-code-push/wiki/Local-Development-Plugin)을 설치합니다.

1. 명령 줄 인터페이스를 사용하여 새로운 Cordova 프로젝트를 만들고 iOS / Android 플랫폼을 추가하십시오:

  ```sh
  cordova create TestProject com.example.testproject TestProject
  cd ./TestProject
  cordova platform add android
  cordova platform add ios
  ```
  또는 기존의 것을 사용하십시오.

2. 플러그인 추가:

  ```sh
  cordova plugin add cordova-hot-code-push-plugin
  ```

3. 로컬 개발용 플러그인 추가:

  ```sh
  cordova plugin add cordova-hot-code-push-local-dev-addon
  ```

4. Cordova Hot Code Push CLI 클라이언트 설치:

  ```sh
  npm install -g cordova-hot-code-push-cli
  ```

5. 로컬 서버 시작:

  ```sh
  cordova-hcp server
  ```

  실행하면 아래와 같은 내용을 볼 수 있습니다:
  ```
  Running server
  Checking:  /Cordova/TestProject/www
  local_url http://localhost:31284
  Warning: .chcpignore does not exist.
  Build 2015.09.02-10.17.48 created in /Cordova/TestProject/www
  cordova-hcp local server available at: http://localhost:31284
  cordova-hcp public server available at: https://5027caf9.ngrok.com
  ```

6. 프로젝트 루트로 이동하여 app을 시작합니다.:

  ```sh
  cordova run
  ```

  플랫폼에서 응용 프로그램이 시작될 때까지 기다립니다..

7. 이제`TestProject`의`www` 폴더에있는`index.html` 페이지를 열어 무언가를 변경하고 저장하십시오. 몇 초 안에 앱이 설치 된 장치(또는 에뮬레이터)에 업데이트 된 페이지가 표시됩니다.

이 시점부터 모든 변경 사항에 대해 어플리케이션을 다시 설치할 필요없이 모든 변경 사항을 장치에 업로드하는 로컬 개발 작업을 수행 할 수 있습니다.

production 빌드의 경우 필수 속성이므로 아래 내용을 `config.xml` 파일에 추가하는 것을 잊지 마십시오.
```xml
<chcp>
    <config-file url="https://5027caf9.ngrok.com/chcp.json"/>
</chcp>
```
자세한 내용은 [wiki](https://github.com/nordnet/cordova-hot-code-push/wiki/Cordova-config-preferences)를 확인하십시오.


### Documentation

모든 문서는 [Wiki on GitHub](https://github.com/nordnet/cordova-hot-code-push/wiki)에 자세히 나와 있습니다.

질문/문제/제안 사항이 있으시면 언제든지 [Issue](https://github.com/nordnet/cordova-hot-code-push/issues)를 게시하십시오.
실제 문제 인 경우 올바르게 수행하는 방법에 대해서는 [가이드](https://github.com/nordnet/cordova-hot-code-push/wiki/Issue-creation-guide)를 따르십시오.
