# flutter_login_yandex

Flutter plugin for authorization with Yandex LoginSDK for iOS and Android

## Getting Started
For first you need to register Yandex OAuth application, see [official docs](https://dev-id.docs-viewer.yandex.ru/ru/mobileauthsdk/ios/2.1.0/sdk-ios)

### Android
Add to your android/app/build.gradle default section this with replacement of yourClientId to Yandex OAuth app client id:
```
manifestPlaceholders = [YANDEX_CLIENT_ID:"yourClientId"]
```

It must looks like this:
```
defaultConfig {
    applicationId "com.example.flutter_login_yandex_example"
    minSdkVersion flutter.minSdkVersion
    targetSdkVersion flutter.targetSdkVersion
    versionCode flutterVersionCode.toInteger()
    versionName flutterVersionName
    manifestPlaceholders = [YANDEX_CLIENT_ID:"yourClientId"]
}
```

### iOS
Add this to your app Info.plist and replace "yourCientId" with Yandex client id from OAuth application
```xml
	<key>LSApplicationQueriesSchemes</key>
	<array>
		<string>yandexauth</string>
		<string>yandexauth2</string>
		<string>yandexauth4</string>
	</array>
	<key>YAClientId</key>
	<string>yourClientId</string>
	<key>CFBundleURLTypes</key>
	<array>
		<dict>
			<key>CFBundleURLName</key>
			<string>YandexLoginSDK</string>
			<key>CFBundleURLSchemes</key>
			<array>
				<string>yxyourClientId</string>
			</array>
		</dict>
	</array>
```

Add this to AppDelegate.swift:
```swift
    @available(iOS 8.0, *)
    func application(_ application: UIApplication, continue userActivity: NSUserActivity, restorationHandler: @escaping ([Any]?) -> Void) -> Bool {
       YXLSdk.shared.processUserActivity(userActivity)
       return true
    }

    // Application delegate
    override public func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        return YXLSdk.shared.handleOpen(url, sourceApplication: options[UIApplication.OpenURLOptionsKey.sourceApplication] as? String)
    }
```
If you already have OpenURL handler, you need to merge this method with your code

Also you need to set up Entitlements, add *Capability: Associated Domains* and enter domain with replaced yourClientId to your value:
```
applinks:yxyourClientId.oauth.yandex.ru
```


If you have some deprecation errors in compile time inside YandexLoginSDK, then you can use @MariyanskiDev fix. To do it, add to your Podfile target:
```
pod 'YandexLoginSDK', :git => 'https://github.com/MariyanskiDev/yandex-login-sdk-ios', :branch => 'depreciation_fix'
```



### Usage in application

Simply:

```dart
final flutterLoginYandexPlugin = FlutterLoginYandex();
final response = await _flutterLoginYandexPlugin.signIn();
saveToken(response['token'] as String);
```