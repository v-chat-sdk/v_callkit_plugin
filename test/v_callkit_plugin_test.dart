import 'package:flutter_test/flutter_test.dart';
import 'package:v_callkit_plugin/v_callkit_plugin_platform_interface.dart';
import 'package:v_callkit_plugin/v_callkit_plugin_method_channel.dart';
import 'package:v_callkit_plugin/models/call_data.dart';
import 'package:v_callkit_plugin/models/call_event.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockVCallkitPluginPlatform
    with MockPlatformInterfaceMixin
    implements VCallkitPluginPlatform {
  @override
  Future<bool> hasPermissions() => Future.value(true);

  @override
  Future<bool> requestPermissions() => Future.value(true);

  @override
  Future<bool> answerCall([String? callId]) => Future.value(true);

  @override
  Future<bool> isCallActive() => Future.value(false);

  @override
  Future<Map<String, dynamic>?> getActiveCallData() => Future.value(null);

  @override
  Future<bool> startOutgoingCallNotification(Map<String, dynamic> callData) =>
      Future.value(true);

  @override
  Future<bool> stopCallForegroundService() => Future.value(true);

  @override
  Future<bool> showIncomingCallWithConfig(Map<String, dynamic> data) =>
      Future.value(true);
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('VCallkitPluginPlatform', () {
    test('$MethodChannelVCallkitPlugin is the default instance', () {
      expect(
        VCallkitPluginPlatform.instance,
        isInstanceOf<MethodChannelVCallkitPlugin>(),
      );
    });

    test('Mock platform works correctly', () async {
      final mockPlatform = MockVCallkitPluginPlatform();

      expect(await mockPlatform.hasPermissions(), true);
      expect(await mockPlatform.requestPermissions(), true);
      expect(await mockPlatform.answerCall('test-call'), true);
      expect(await mockPlatform.isCallActive(), false);
      expect(await mockPlatform.getActiveCallData(), null);

      // Test foreground service methods
      expect(await mockPlatform.startOutgoingCallNotification({'test': 'data'}),
          true);
      expect(await mockPlatform.stopCallForegroundService(), true);

      // Test incoming call methods
      expect(await mockPlatform.showIncomingCallWithConfig({'test': 'config'}),
          true);
    });
  });

  group('CallData', () {
    test('creates CallData with required parameters', () {
      const callData = CallData(
        id: 'test-1',
        callerName: 'Test Caller',
        callerNumber: '+1234567890',
      );

      expect(callData.id, 'test-1');
      expect(callData.callerName, 'Test Caller');
      expect(callData.callerNumber, '+1234567890');
      expect(callData.callerAvatar, null);
      expect(callData.isVideoCall, false);
      expect(callData.extra, isEmpty);
    });

    test('creates CallData with all parameters', () {
      const callData = CallData(
        id: 'test-2',
        callerName: 'Video Caller',
        callerNumber: '+1987654321',
        callerAvatar: 'https://example.com/avatar.jpg',
        isVideoCall: true,
        extra: {'roomId': 'room-456', 'priority': 'high'},
      );

      expect(callData.isVideoCall, true);
      expect(callData.callerAvatar, 'https://example.com/avatar.jpg');
      expect(callData.extra['roomId'], 'room-456');
      expect(callData.extra['priority'], 'high');
    });

    test('fromMap creates CallData correctly', () {
      final map = {
        'id': 'map-test',
        'callerName': 'Map Caller',
        'callerNumber': '+1555666777',
        'callerAvatar': 'https://example.com/map-avatar.jpg',
        'isVideoCall': true,
        'extra': {'custom': 'data'},
      };

      final callData = CallData.fromMap(map);

      expect(callData.id, 'map-test');
      expect(callData.callerName, 'Map Caller');
      expect(callData.callerNumber, '+1555666777');
      expect(callData.callerAvatar, 'https://example.com/map-avatar.jpg');
      expect(callData.isVideoCall, true);
      expect(callData.extra['custom'], 'data');
    });

    test('toMap converts CallData correctly', () {
      const callData = CallData(
        id: 'to-map-test',
        callerName: 'To Map Caller',
        callerNumber: '+1888999000',
        callerAvatar: 'https://example.com/to-map-avatar.jpg',
        isVideoCall: false,
        extra: {'test': 'value'},
      );

      final map = callData.toMap();

      expect(map['id'], 'to-map-test');
      expect(map['callerName'], 'To Map Caller');
      expect(map['callerNumber'], '+1888999000');
      expect(map['callerAvatar'], 'https://example.com/to-map-avatar.jpg');
      expect(map['isVideoCall'], false);
      expect(map['extra']['test'], 'value');
    });
  });

  group('CallEvent Models', () {
    test('CallAnsweredEvent creation and parsing', () {
      final timestamp = DateTime.now();
      final event = CallAnsweredEvent(
        callId: 'answered-test',
        timestamp: timestamp,
        videoState: 1,
        data: const {'custom': 'data'},
      );

      expect(event.callId, 'answered-test');
      expect(event.action, CallAction.answered);
      expect(event.timestamp, timestamp);
      expect(event.videoState, 1);
      expect(event.data['custom'], 'data');
    });

    test('CallEndedEvent creation and parsing', () {
      final timestamp = DateTime.now();
      final event = CallEndedEvent(
        callId: 'ended-test',
        timestamp: timestamp,
        reason: 'disconnected',
      );

      expect(event.callId, 'ended-test');
      expect(event.action, CallAction.ended);
      expect(event.reason, 'disconnected');
    });

    test('CallStateChangedEvent creation and parsing', () {
      final timestamp = DateTime.now();
      final event = CallStateChangedEvent(
        callId: 'state-test',
        timestamp: timestamp,
        state: CallState.active,
      );

      expect(event.callId, 'state-test');
      expect(event.action, CallAction.stateChanged);
      expect(event.state, CallState.active);
    });

    test('Event parsing from Map', () {
      final map = {
        'callId': 'test-123',
        'action': 'answered',
        'timestamp': DateTime.now().millisecondsSinceEpoch,
        'videoState': 1,
      };

      final event = CallEvent.fromMap(map);
      expect(event, isA<CallAnsweredEvent>());
      expect(event.callId, 'test-123');
      expect(event.action, CallAction.answered);
    });
  });

  group('Data Serialization', () {
    test('CallData round-trip serialization', () {
      const original = CallData(
        id: 'round-trip-test',
        callerName: 'Round Trip Caller',
        callerNumber: '+1999888777',
        callerAvatar: 'https://example.com/round-trip.jpg',
        isVideoCall: true,
        extra: {'string': 'value', 'bool': true, 'number': 42},
      );

      final map = original.toMap();
      final restored = CallData.fromMap(map);

      expect(restored, equals(original));
      expect(restored.id, equals(original.id));
      expect(restored.callerName, equals(original.callerName));
      expect(restored.extra['string'], equals(original.extra['string']));
    });
  });
}
