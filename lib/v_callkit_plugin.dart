import 'dart:async';

import 'v_callkit_plugin_platform_interface.dart';
import 'v_callkit_plugin_method_channel.dart';
import 'models/call_data.dart';
import 'models/call_event.dart';
import 'models/call_configuration.dart';

/// Main plugin class for VCallkit functionality
class VCallkitPlugin {
  // Singleton pattern
  static final VCallkitPlugin _instance = VCallkitPlugin._internal();
  factory VCallkitPlugin() => _instance;
  VCallkitPlugin._internal();

  final MethodChannelVCallkitPlugin _methodChannelPlugin =
      VCallkitPluginPlatform.instance as MethodChannelVCallkitPlugin;

  // Essential event streams only
  late final Stream<CallAnsweredEvent> _onCallAnswered;
  late final Stream<CallEndedEvent> _onCallEnded;
  late final Stream<CallStateChangedEvent> _onCallStateChanged;

  bool _initialized = false;

  // Public API - Initialization and Lifecycle

  /// Initialize the plugin and set up event streams
  void initialize() {
    if (_initialized) return;

    _setupEventStreams();
    _initialized = true;
  }

  /// Dispose the plugin and clean up resources
  void dispose() {
    _methodChannelPlugin.dispose();
    _initialized = false;
  }

  // Public API - Essential Event Streams Only

  /// Stream of call answered events
  Stream<CallAnsweredEvent> get onCallAnswered {
    _ensureInitialized();
    return _onCallAnswered;
  }

  /// Stream of call ended events
  Stream<CallEndedEvent> get onCallEnded {
    _ensureInitialized();
    return _onCallEnded;
  }

  /// Stream of call state change events
  Stream<CallStateChangedEvent> get onCallStateChanged {
    _ensureInitialized();
    return _onCallStateChanged;
  }

  // Public API - Permissions

  /// Check if the app has required VoIP permissions
  Future<bool> hasPermissions() {
    return VCallkitPluginPlatform.instance.hasPermissions();
  }

  /// Request VoIP permissions (Android only)
  /// Note: On Android, permissions need to be requested through system settings
  Future<bool> requestPermissions() {
    return VCallkitPluginPlatform.instance.requestPermissions();
  }

  // Public API - Call Display and Control

  /// Show an incoming call with optional configuration
  Future<bool> showIncomingCall({
    required CallData callData,
    VCallkitCallConfiguration? configuration,
  }) {
    final data = <String, dynamic>{
      'callData': callData.toMap(),
    };

    if (configuration != null) {
      data['config'] = configuration.toMap();
    }

    return VCallkitPluginPlatform.instance.showIncomingCallWithConfig(data);
  }

  /// Answer the current call
  Future<bool> answerCall([String? callId]) {
    return VCallkitPluginPlatform.instance.answerCall(callId);
  }

  /// Check if there's an active call
  Future<bool> isCallActive() {
    return VCallkitPluginPlatform.instance.isCallActive();
  }

  /// Get active call data
  Future<CallData?> getActiveCallData() async {
    final data = await VCallkitPluginPlatform.instance.getActiveCallData();
    return data != null ? CallData.fromMap(data) : null;
  }

  // Public API - Foreground Service Control (Essential for Android 14+)

  /// Start persistent notification for outgoing calls
  Future<bool> startOutgoingCallNotification(CallData callData) {
    return VCallkitPluginPlatform.instance.startOutgoingCallNotification(
      callData.toMap(),
    );
  }

  /// Stop the call foreground service
  Future<bool> stopCallForegroundService() {
    return VCallkitPluginPlatform.instance.stopCallForegroundService();
  }

  // Private helper methods

  /// Set up essential event streams only during initialization
  void _setupEventStreams() {
    _onCallAnswered = _methodChannelPlugin.onCallAnswered.map(
      (data) => CallAnsweredEvent.fromMap(data),
    );

    _onCallEnded = _methodChannelPlugin.onCallEnded.map(
      (data) => CallEndedEvent.fromMap(data),
    );

    _onCallStateChanged = _methodChannelPlugin.onCallStateChanged.map(
      (data) => CallStateChangedEvent.fromMap(data),
    );
  }

  /// Ensure the plugin is initialized
  void _ensureInitialized() {
    if (!_initialized) {
      initialize();
    }
  }
}
