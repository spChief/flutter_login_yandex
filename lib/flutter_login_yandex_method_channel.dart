import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_login_yandex_platform_interface.dart';

/// An implementation of [FlutterLoginYandexPlatform] that uses method channels.
class MethodChannelFlutterLoginYandex extends FlutterLoginYandexPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_login_yandex');

  @override
  Future<Map<Object?, Object?>?> signIn() async {
    final result =
        await methodChannel.invokeMethod<Map<Object?, Object?>>('signIn');
    return result;
  }

  @override
  Future<Map<Object?, Object?>?> signOut() async {
    final result =
        await methodChannel.invokeMethod<Map<Object?, Object?>>('signOut');
    return result;
  }
}
