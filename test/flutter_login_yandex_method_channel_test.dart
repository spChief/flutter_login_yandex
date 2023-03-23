import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_login_yandex/flutter_login_yandex_method_channel.dart';

void main() {
  MethodChannelFlutterLoginYandex platform = MethodChannelFlutterLoginYandex();
  const MethodChannel channel = MethodChannel('flutter_login_yandex');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.signIn(), '42');
  });
}
