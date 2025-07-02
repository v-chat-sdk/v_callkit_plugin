import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:v_callkit_plugin/v_callkit_plugin_method_channel.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  MethodChannelVCallkitPlugin platform = MethodChannelVCallkitPlugin();
  const MethodChannel channel = MethodChannel('v_callkit_plugin');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(
      channel,
      (MethodCall methodCall) async {
        switch (methodCall.method) {
          case 'hasPermissions':
            return true;
          case 'requestPermissions':
            return true;
          case 'answerCall':
            return true;
          case 'isCallActive':
            return false;
          case 'getActiveCallData':
            return null;
          case 'showIncomingCallWithConfig':
            return true;
          case 'startOutgoingCallNotification':
            return true;
          case 'stopCallForegroundService':
            return true;
          default:
            return null;
        }
      },
    );
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });

  test('hasPermissions', () async {
    expect(await platform.hasPermissions(), true);
  });

  test('requestPermissions', () async {
    expect(await platform.requestPermissions(), true);
  });

  test('answerCall', () async {
    expect(await platform.answerCall('test-call'), true);
  });

  test('isCallActive', () async {
    expect(await platform.isCallActive(), false);
  });

  test('getActiveCallData', () async {
    expect(await platform.getActiveCallData(), null);
  });

  test('showIncomingCallWithConfig', () async {
    expect(await platform.showIncomingCallWithConfig({'test': 'data'}), true);
  });

  test('startOutgoingCallNotification', () async {
    expect(
        await platform.startOutgoingCallNotification({'test': 'data'}), true);
  });

  test('stopCallForegroundService', () async {
    expect(await platform.stopCallForegroundService(), true);
  });
}
