import 'dart:math';
import 'dart:developer' as developer;
import 'package:v_callkit_plugin/v_callkit_plugin.dart';
import 'package:v_callkit_plugin/models/call_data.dart';
import 'package:v_callkit_plugin/models/call_configuration.dart';
import '../models/sample_caller.dart';
import 'event_logger.dart';

/// Service class responsible for managing call operations
class CallService {
  static const int _defaultVibrationTestTimeout = 30;

  final VCallkitPlugin _plugin = VCallkitPlugin();
  final EventLogger _eventLogger;

  CallService(this._eventLogger);

  /// Initialize the call service
  Future<void> initialize() async {
    _plugin.initialize();
  }

  /// Dispose of resources
  void dispose() {
    _plugin.dispose();
  }

  /// Get platform version
  Future<String> getPlatformVersion() async {
    // Platform version method was removed from minimal API
    return 'Platform version not available in minimal API';
  }

  /// Check if there's an active call
  Future<bool> isCallActive() async {
    try {
      return await _plugin.isCallActive();
    } catch (e) {
      _eventLogger.addErrorEvent('Error checking call status: $e');
      return false;
    }
  }

  /// Get active call data
  Future<CallData?> getActiveCallData() async {
    try {
      return await _plugin.getActiveCallData();
    } catch (e) {
      _eventLogger.addErrorEvent('Error getting call data: $e');
      return null;
    }
  }

  /// Show incoming call with random sample caller
  Future<bool> showIncomingCall({required bool isVideo}) async {
    final caller = _getRandomCaller();

    try {
      final callData = _createCallData(
        caller: caller,
        isVideo: isVideo,
        source: 'call_simulation',
      );

      final success = await _plugin.showIncomingCall(callData: callData);

      if (success) {
        _eventLogger.addEvent(
          '📱 Incoming ${isVideo ? 'video' : 'voice'} call shown: ${caller.name}',
        );
      }

      return success;
    } catch (e) {
      developer.log('Incoming call error: $e', name: 'CallService');
      _eventLogger.addErrorEvent('Failed to show incoming call: $e');
      rethrow;
    }
  }

  /// Start persistent call notification
  Future<void> startPersistentNotification(CallData callData) async {
    try {
      await _plugin.startOutgoingCallNotification(callData);
      _eventLogger.addSuccessEvent(
        'Persistent call notification started for: ${callData.callerName}',
      );
    } catch (e) {
      _eventLogger.addErrorEvent('Error starting persistent notification: $e');
      rethrow;
    }
  }

  /// Stop persistent call notification
  Future<void> stopPersistentNotification() async {
    try {
      await _plugin.stopCallForegroundService();
      _eventLogger.addSuccessEvent('Persistent call notification stopped');
    } catch (e) {
      _eventLogger.addErrorEvent('Error stopping persistent notification: $e');
      rethrow;
    }
  }

  /// Test vibration functionality
  Future<bool> testVibration() async {
    try {
      _eventLogger.addEvent('🧪 Testing vibration functionality...');

      final testCallData = _createTestCallData();
      final config = _createVibrationTestConfig();

      _eventLogger.addEvent(
        '📳 Triggering test call with vibration enabled...',
      );

      final result = await _plugin.showIncomingCall(
        callData: testCallData,
        configuration: config,
      );

      if (result) {
        _eventLogger.addSuccessEvent(
          'Test vibration call triggered successfully',
        );

        // Debug info was removed from minimal API
        _eventLogger.addEvent('🔍 Debug info: not available in minimal API');
      } else {
        _eventLogger.addErrorEvent('Failed to trigger test vibration call');
      }

      return result;
    } catch (e, stackTrace) {
      _eventLogger.addErrorEvent('Error testing vibration: $e');
      developer.log(
        'Vibration test error: $e',
        name: 'CallService',
        error: e,
        stackTrace: stackTrace,
      );
      rethrow;
    }
  }

  /// Get the plugin instance for direct access if needed
  VCallkitPlugin get plugin => _plugin;

  // Private helper methods

  SampleCaller _getRandomCaller() {
    final random = Random();
    return SampleCaller.sampleCallers[random.nextInt(
      SampleCaller.sampleCallers.length,
    )];
  }

  CallData _createCallData({
    required SampleCaller caller,
    required bool isVideo,
    required String source,
  }) {
    return CallData(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      callerName: caller.name,
      callerNumber: caller.number,
      callerAvatar: caller.avatar,
      isVideoCall: isVideo,
      extra: {'source': source, 'timestamp': DateTime.now().toIso8601String()},
    );
  }

  CallData _createTestCallData() {
    return CallData(
      id: 'test_vibration_${DateTime.now().millisecondsSinceEpoch}',
      callerName: 'Vibration Test',
      callerNumber: '+1234567890',
      callerAvatar: null,
      isVideoCall: false,
      extra: {'test': true},
    );
  }

  VCallkitCallConfiguration _createVibrationTestConfig() {
    return VCallkitCallConfiguration.defaultConfig.copyWith(
      enableVibration: true,
      enableRingtone: false,
      primaryColor: '#FFD32F2F',
      backgroundColor: '#FF000000',
      answerButtonText: 'Test Answer',
      declineButtonText: 'Test Decline',
      callTimeoutSeconds: _defaultVibrationTestTimeout,
    );
  }
}
