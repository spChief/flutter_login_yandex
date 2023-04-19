import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_login_yandex_method_channel.dart';

abstract class FlutterLoginYandexPlatform extends PlatformInterface {
  /// Constructs a FlutterLoginYandexPlatform.
  FlutterLoginYandexPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterLoginYandexPlatform _instance = MethodChannelFlutterLoginYandex();

  /// The default instance of [FlutterLoginYandexPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterLoginYandex].
  static FlutterLoginYandexPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterLoginYandexPlatform] when
  /// they register themselves.
  static set instance(FlutterLoginYandexPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<Map<Object?, Object?>?> signIn() {
    throw UnimplementedError('signIn() has not been implemented.');
  }

  Future<Map<Object?, Object?>?> signOut() {
    throw UnimplementedError('signOut() has not been implemented.');
  }
}
