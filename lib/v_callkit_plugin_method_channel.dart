import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'dart:async';

import 'v_callkit_plugin_platform_interface.dart';

/// An implementation of [VCallkitPluginPlatform] that uses method channels.
class MethodChannelVCallkitPlugin extends VCallkitPluginPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('v_callkit_plugin');

  // Stream controllers for different call events
  final StreamController<Map<String, dynamic>> _callAnsweredController =
      StreamController<Map<String, dynamic>>.broadcast();
  final StreamController<Map<String, dynamic>> _callRejectedController =
      StreamController<Map<String, dynamic>>.broadcast();
  final StreamController<Map<String, dynamic>> _callEndedController =
      StreamController<Map<String, dynamic>>.broadcast();
  final StreamController<Map<String, dynamic>> _callHoldController =
      StreamController<Map<String, dynamic>>.broadcast();
  final StreamController<Map<String, dynamic>> _callMuteController =
      StreamController<Map<String, dynamic>>.broadcast();
  final StreamController<Map<String, dynamic>> _callStateChangedController =
      StreamController<Map<String, dynamic>>.broadcast();

  MethodChannelVCallkitPlugin() {
    // Set up method call handler for callbacks from native side
    methodChannel.setMethodCallHandler(_handleMethodCall);
  }

  /// Handles method calls from the native side (Android)
  Future<dynamic> _handleMethodCall(MethodCall call) async {
    final Map<String, dynamic> arguments = Map<String, dynamic>.from(
      call.arguments ?? <String, dynamic>{},
    );

    switch (call.method) {
      case 'onCallAnswered':
        _callAnsweredController.add(arguments);
        break;
      case 'onCallRejected':
        _callRejectedController.add(arguments);
        break;
      case 'onCallEnded':
        _callEndedController.add(arguments);
        break;
      case 'onCallHold':
        _callHoldController.add(arguments);
        break;
      case 'onCallMute':
        _callMuteController.add(arguments);
        break;
      case 'onCallStateChanged':
        _callStateChangedController.add(arguments);
        break;
      default:
        if (kDebugMode) {
          print('Unknown method call: ${call.method}');
        }
    }
  }

  // Event streams for listening to call events

  /// Stream of call answered events
  Stream<Map<String, dynamic>> get onCallAnswered =>
      _callAnsweredController.stream;

  /// Stream of call rejected events
  Stream<Map<String, dynamic>> get onCallRejected =>
      _callRejectedController.stream;

  /// Stream of call ended events
  Stream<Map<String, dynamic>> get onCallEnded => _callEndedController.stream;

  /// Stream of call hold/unhold events
  Stream<Map<String, dynamic>> get onCallHold => _callHoldController.stream;

  /// Stream of call mute/unmute events
  Stream<Map<String, dynamic>> get onCallMute => _callMuteController.stream;

  /// Stream of call state change events
  Stream<Map<String, dynamic>> get onCallStateChanged =>
      _callStateChangedController.stream;

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>(
      'getPlatformVersion',
    );
    return version;
  }

  @override
  Future<bool> hasPermissions() async {
    final hasPermissions = await methodChannel.invokeMethod<bool>(
      'hasPermissions',
    );
    return hasPermissions ?? false;
  }

  @override
  Future<bool> requestPermissions() async {
    final granted = await methodChannel.invokeMethod<bool>(
      'requestPermissions',
    );
    return granted ?? false;
  }

  @override
  Future<bool> showIncomingCall(Map<String, dynamic> callData) async {
    final success = await methodChannel.invokeMethod<bool>(
      'showIncomingCall',
      callData,
    );
    return success ?? false;
  }

  @override
  Future<bool> endCall([String? callId]) async {
    final success = await methodChannel.invokeMethod<bool>('endCall', callId);
    return success ?? false;
  }

  @override
  Future<bool> answerCall([String? callId]) async {
    final success = await methodChannel.invokeMethod<bool>(
      'answerCall',
      callId,
    );
    return success ?? false;
  }

  @override
  Future<bool> rejectCall([String? callId]) async {
    final success = await methodChannel.invokeMethod<bool>(
      'rejectCall',
      callId,
    );
    return success ?? false;
  }

  @override
  Future<bool> muteCall(bool isMuted, [String? callId]) async {
    final success = await methodChannel.invokeMethod<bool>('muteCall', {
      'isMuted': isMuted,
      'callId': callId,
    });
    return success ?? false;
  }

  @override
  Future<bool> holdCall(bool isOnHold, [String? callId]) async {
    final success = await methodChannel.invokeMethod<bool>('holdCall', {
      'isOnHold': isOnHold,
      'callId': callId,
    });
    return success ?? false;
  }

  @override
  Future<bool> isCallActive() async {
    final isActive = await methodChannel.invokeMethod<bool>('isCallActive');
    return isActive ?? false;
  }

  @override
  Future<Map<String, dynamic>?> getActiveCallData() async {
    final callData = await methodChannel.invokeMethod<Map<Object?, Object?>>(
      'getActiveCallData',
    );
    return callData != null ? _safeMapConvert(callData) : null;
  }

  @override
  Future<bool> setCustomRingtone(String? ringtoneUri) async {
    final result = await methodChannel.invokeMethod<bool>(
      'setCustomRingtone',
      ringtoneUri,
    );
    return result ?? false;
  }

  @override
  Future<List<Map<String, dynamic>>> getSystemRingtones() async {
    final result = await methodChannel.invokeMethod<List<Object?>>(
      'getSystemRingtones',
    );
    if (result != null) {
      return result
          .map((item) => Map<String, dynamic>.from(item as Map))
          .toList();
    }
    return [];
  }

  @override
  Future<bool> checkBatteryOptimization() async {
    final result = await methodChannel.invokeMethod<bool>(
      'checkBatteryOptimization',
    );
    return result ?? true;
  }

  @override
  Future<bool> requestBatteryOptimization() async {
    final result = await methodChannel.invokeMethod<bool>(
      'requestBatteryOptimization',
    );
    return result ?? false;
  }

  @override
  Future<String> getDeviceManufacturer() async {
    final result = await methodChannel.invokeMethod<String>(
      'getDeviceManufacturer',
    );
    return result ?? 'unknown';
  }

  @override
  Future<Map<String, dynamic>?> getLastCallActionLaunch() async {
    final result = await methodChannel.invokeMethod<Map<Object?, Object?>>(
      'getLastCallActionLaunch',
    );
    return result != null ? _safeMapConvert(result) : null;
  }

  @override
  Future<bool> hasCallActionLaunchData() async {
    final result = await methodChannel.invokeMethod<bool>(
      'hasCallActionLaunchData',
    );
    return result ?? false;
  }

  @override
  Future<bool> clearCallActionLaunchData() async {
    final result = await methodChannel.invokeMethod<bool>(
      'clearCallActionLaunchData',
    );
    return result ?? false;
  }

  @override
  Future<bool> startOutgoingCallNotification(
    Map<String, dynamic> callData,
  ) async {
    final result = await methodChannel.invokeMethod<bool>(
      'startOutgoingCallNotification',
      callData,
    );
    return result ?? false;
  }

  @override
  Future<bool> stopCallForegroundService() async {
    final result = await methodChannel.invokeMethod<bool>(
      'stopCallForegroundService',
    );
    return result ?? false;
  }

  /// Safely converts a map from platform channel to Map with String keys and dynamic values
  /// This handles nested maps and ensures proper type conversion
  static Map<String, dynamic> _safeMapConvert(dynamic value) {
    if (value == null) return {};

    if (value is Map<String, dynamic>) {
      return value;
    }

    if (value is Map) {
      final result = <String, dynamic>{};
      value.forEach((key, val) {
        final stringKey = key.toString();
        result[stringKey] = _convertValue(val);
      });
      return result;
    }

    return {};
  }

  /// Recursively converts values to appropriate types
  static dynamic _convertValue(dynamic value) {
    if (value == null) return null;

    if (value is Map) {
      return _safeMapConvert(value);
    }

    if (value is List) {
      return value.map((item) => _convertValue(item)).toList();
    }

    return value;
  }

  /// Dispose method to clean up resources
  void dispose() {
    _callAnsweredController.close();
    _callRejectedController.close();
    _callEndedController.close();
    _callHoldController.close();
    _callMuteController.close();
    _callStateChangedController.close();
  }
}
