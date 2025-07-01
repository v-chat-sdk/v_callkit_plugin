import 'dart:math';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:workmanager/workmanager.dart';

import '../models/sample_caller.dart';

/// Background task scheduler for simulating incoming calls
/// Uses WorkManager to execute tasks even when the app is terminated
class BackgroundCallScheduler {
  // Constants for better maintainability
  static const String _callTaskName = 'simulate_incoming_call';
  static const String _uniqueTaskTag = 'background_call_simulation';
  static const String _methodChannelName = 'v_callkit_plugin';
  static const String _showIncomingCallMethod = 'showIncomingCallNative';
  static const String _backgroundCallSource = 'background_workmanager_native';
  static const String _callIdPrefix = 'bg_call_';

  /// Initialize WorkManager for background task execution
  static Future<void> initialize() async {
    await Workmanager().initialize(
      _callbackDispatcher,
      isInDebugMode: kDebugMode,
    );
  }

  /// Schedule a background call after specified delay
  static Future<void> scheduleCall({
    required int delayInSeconds,
    required bool isVideoCall,
    SampleCaller? caller,
  }) async {
    await _cancelExistingScheduledCall();

    final selectedCaller = _selectCaller(caller);
    final inputData = _createTaskInputData(isVideoCall, selectedCaller);

    await _registerBackgroundTask(delayInSeconds, inputData);
    _logScheduledCall(selectedCaller, delayInSeconds);
  }

  /// Cancel any scheduled background call
  static Future<void> cancelScheduledCall() async {
    await _cancelExistingScheduledCall();
    _logCancelledCall();
  }

  /// Check if there's a scheduled call
  /// Note: WorkManager doesn't provide direct task status checking
  static Future<bool> hasScheduledCall() async {
    return false;
  }

  // Private helper methods for better code organization

  static Future<void> _cancelExistingScheduledCall() async {
    await Workmanager().cancelByUniqueName(_uniqueTaskTag);
  }

  static SampleCaller _selectCaller(SampleCaller? caller) {
    return caller ??
        SampleCaller.sampleCallers[Random().nextInt(
          SampleCaller.sampleCallers.length,
        )];
  }

  static Map<String, dynamic> _createTaskInputData(
    bool isVideoCall,
    SampleCaller caller,
  ) {
    return {
      'isVideoCall': isVideoCall,
      'callerName': caller.name,
      'callerNumber': caller.number,
      'callerAvatar': caller.avatar,
      'scheduledAt': DateTime.now().millisecondsSinceEpoch,
    };
  }

  static Future<void> _registerBackgroundTask(
    int delayInSeconds,
    Map<String, dynamic> inputData,
  ) async {
    await Workmanager().registerOneOffTask(
      _uniqueTaskTag,
      _callTaskName,
      inputData: inputData,
      initialDelay: Duration(seconds: delayInSeconds),
      constraints: Constraints(
        networkType: NetworkType.not_required,
        requiresBatteryNotLow: false,
        requiresCharging: false,
        requiresDeviceIdle: false,
        requiresStorageNotLow: false,
      ),
    );
  }

  static void _logScheduledCall(SampleCaller caller, int delayInSeconds) {
    if (kDebugMode) {
      print(
        'Background call scheduled for ${caller.name} in $delayInSeconds seconds',
      );
    }
  }

  static void _logCancelledCall() {
    if (kDebugMode) {
      print('Background call cancelled');
    }
  }
}

/// Handle the background call simulation using native method channel
/// This bypasses Flutter context limitations in background isolates
Future<bool> _executeBackgroundCallNative(
  Map<String, dynamic> inputData,
) async {
  try {
    final callParameters = _extractCallParametersFromInput(inputData);
    final callDataMap = _createCallDataMap(callParameters);

    return await _invokeNativeCallMethod(callDataMap, callParameters);
  } catch (e) {
    _logBackgroundCallError(e);
    return false;
  }
}

/// Extract and validate call parameters from input data
CallParameters _extractCallParametersFromInput(Map<String, dynamic> inputData) {
  return CallParameters(
    isVideoCall: inputData['isVideoCall'] as bool? ?? false,
    callerName: inputData['callerName'] as String? ?? 'Unknown Caller',
    callerNumber: inputData['callerNumber'] as String? ?? '+1234567890',
    callerAvatar: inputData['callerAvatar'] as String?,
    scheduledAt: inputData['scheduledAt'] as int? ?? 0,
  );
}

/// Create call data map for native method channel
Map<String, dynamic> _createCallDataMap(CallParameters parameters) {
  final callId =
      '${BackgroundCallScheduler._callIdPrefix}${DateTime.now().millisecondsSinceEpoch}';

  return {
    'id': callId,
    'callerName': parameters.callerName,
    'callerNumber': parameters.callerNumber,
    'callerAvatar': parameters.callerAvatar,
    'isVideoCall': parameters.isVideoCall,
    'extra': {
      'source': BackgroundCallScheduler._backgroundCallSource,
      'timestamp': DateTime.now().toIso8601String(),
    },
  };
}

/// Invoke native method to show incoming call
Future<bool> _invokeNativeCallMethod(
  Map<String, dynamic> callDataMap,
  CallParameters parameters,
) async {
  _logBackgroundCallStart(parameters.callerName);

  const methodChannel = MethodChannel(
    BackgroundCallScheduler._methodChannelName,
  );
  final result = await methodChannel.invokeMethod(
    BackgroundCallScheduler._showIncomingCallMethod,
    callDataMap,
  );

  _logBackgroundCallResult(parameters, result);
  return result == true;
}

/// Log background call execution details
void _logBackgroundCallStart(String callerName) {
  if (kDebugMode) {
    print('Background task: Creating native incoming call for $callerName');
  }
}

void _logBackgroundCallResult(CallParameters parameters, dynamic result) {
  if (kDebugMode) {
    final executionDelay =
        DateTime.now().millisecondsSinceEpoch - parameters.scheduledAt;
    print(
      'Background native call executed for ${parameters.callerName} '
      '(delay: ${executionDelay}ms), result: $result',
    );
    print('Background task: Native call UI shown, should now be ringing');
  }
}

void _logBackgroundCallError(dynamic error) {
  if (kDebugMode) {
    print('Error handling background native call: $error');
    print('Stack trace: ${StackTrace.current}');
  }
}

/// Background task callback dispatcher
/// This function runs in a separate isolate
@pragma('vm:entry-point')
void _callbackDispatcher() {
  Workmanager().executeTask((task, inputData) async {
    try {
      if (_isValidCallTask(task, inputData)) {
        return await _executeBackgroundCallNative(inputData!);
      }
      return Future.value(true);
    } catch (e) {
      _logTaskError(e);
      return Future.value(false);
    }
  });
}

bool _isValidCallTask(String task, Map<String, dynamic>? inputData) {
  return task == BackgroundCallScheduler._callTaskName && inputData != null;
}

void _logTaskError(dynamic error) {
  if (kDebugMode) {
    print('Background task error: $error');
  }
}

/// Data class to hold call parameters for better type safety
class CallParameters {
  final bool isVideoCall;
  final String callerName;
  final String callerNumber;
  final String? callerAvatar;
  final int scheduledAt;

  const CallParameters({
    required this.isVideoCall,
    required this.callerName,
    required this.callerNumber,
    required this.callerAvatar,
    required this.scheduledAt,
  });
}
