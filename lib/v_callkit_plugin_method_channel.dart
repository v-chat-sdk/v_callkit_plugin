import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'dart:async';

import 'v_callkit_plugin_platform_interface.dart';

/// An implementation of [VCallkitPluginPlatform] that uses method channels.
class MethodChannelVCallkitPlugin extends VCallkitPluginPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('v_callkit_plugin');

  // Essential stream controllers only
  final StreamController<Map<String, dynamic>> _callAnsweredController =
      StreamController<Map<String, dynamic>>.broadcast();
  final StreamController<Map<String, dynamic>> _callEndedController =
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
      case 'onCallEnded':
        _callEndedController.add(arguments);
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

  // Essential event streams only

  /// Stream of call answered events
  Stream<Map<String, dynamic>> get onCallAnswered =>
      _callAnsweredController.stream;

  /// Stream of call ended events
  Stream<Map<String, dynamic>> get onCallEnded => _callEndedController.stream;

  /// Stream of call state change events
  Stream<Map<String, dynamic>> get onCallStateChanged =>
      _callStateChangedController.stream;

  /// Dispose method to clean up resources
  void dispose() {
    _callAnsweredController.close();
    _callEndedController.close();
    _callStateChangedController.close();
  }

  @override
  Future<bool> hasPermissions() async {
    final hasPerms = await methodChannel.invokeMethod<bool>('hasPermissions');
    return hasPerms ?? false;
  }

  @override
  Future<bool> requestPermissions() async {
    final granted =
        await methodChannel.invokeMethod<bool>('requestPermissions');
    return granted ?? false;
  }

  @override
  Future<bool> showIncomingCallWithConfig(Map<String, dynamic> data) async {
    final success = await methodChannel.invokeMethod<bool>(
      'showIncomingCallWithConfig',
      data,
    );
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
  Future<bool> startOutgoingCallNotification(
      Map<String, dynamic> callData) async {
    final success = await methodChannel.invokeMethod<bool>(
      'startOutgoingCallNotification',
      callData,
    );
    return success ?? false;
  }

  @override
  Future<bool> stopCallForegroundService() async {
    final success = await methodChannel.invokeMethod<bool>(
      'stopCallForegroundService',
    );
    return success ?? false;
  }

  /// Safely converts a map from platform channel
  Map<String, dynamic> _safeMapConvert(Map<Object?, Object?> map) {
    final result = <String, dynamic>{};
    map.forEach((key, value) {
      final stringKey = key.toString();
      result[stringKey] = value;
    });
    return result;
  }
}
