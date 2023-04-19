import Flutter
import UIKit
import YandexLoginSDK

/// Plugin methods.
enum PluginMethod: String {
    case signIn
    case signOut
}

enum InitSdkArg: String {
    case clientId
}

public class SwiftFlutterLoginYandexPlugin: NSObject, FlutterPlugin {

    private lazy var _loginDelegate = LogInDelegate();

    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "flutter_login_yandex", binaryMessenger: registrar.messenger())
        let instance = SwiftFlutterLoginYandexPlugin()
        let clientId = Bundle.main.object(forInfoDictionaryKey: "YAClientId") as? String

        do {
            try YXLSdk.shared.activate(withAppId: clientId!)
        } catch {
            // todo
        }

        registrar.addMethodCallDelegate(instance, channel: channel)
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        guard let method = PluginMethod(rawValue: call.method) else {
            result(FlutterMethodNotImplemented)
            return
        }

        switch method {
        case .signIn:
            logIn(result: result)
        case .signOut:
            logOut(result: result)
        }
    }

    @available(iOS 8.0, *)
    func application(_ application: UIApplication, continue userActivity: NSUserActivity, restorationHandler: @escaping ([Any]?) -> Void) -> Bool {
       YXLSdk.shared.processUserActivity(userActivity)
       return true
    }

    // Application delegate
    public func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        return YXLSdk.shared.handleOpen(url, sourceApplication: options[UIApplication.OpenURLOptionsKey.sourceApplication] as? String)
    }

    private func logIn(result: @escaping FlutterResult) {
        _loginDelegate.startLogin(result: result)
        YXLSdk.shared.authorize()
    }
    
    private func logOut(result: @escaping FlutterResult) {
        YXLSdk.shared.logout()
        result(nil)
    }
}

class LogInDelegate : NSObject, YXLObserver {

    private var _pendingLoginResult: FlutterResult?

    func startLogin(result: @escaping FlutterResult) {
        if let prevResult = _pendingLoginResult {
            prevResult(["error": "Interrupted by another login call"])
        }

        YXLSdk.shared.add(observer: self)
        _pendingLoginResult = result
    }

    func loginDidFinish(with result: YXLLoginResult) {
        YXLSdk.shared.remove(observer: self)
        if let pendingResult = _pendingLoginResult {
            _pendingLoginResult = nil
            let token = result.token
            pendingResult([
                "token": token,
            ])
        }

    }

    func loginDidFinishWithError(_ error: Error) {
        YXLSdk.shared.remove(observer: self)
        if let pendingResult = _pendingLoginResult {
            _pendingLoginResult = nil
            pendingResult([
                "error": error.localizedDescription
            ])
        }
    }
}
