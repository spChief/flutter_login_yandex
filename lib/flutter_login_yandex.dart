import 'flutter_login_yandex_platform_interface.dart';

class FlutterLoginYandex {
  Future<Map<Object?, Object?>?> signIn() {
    return FlutterLoginYandexPlatform.instance.signIn();
  }
}
