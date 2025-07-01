import 'dart:async';

import 'v_callkit_plugin_platform_interface.dart';
import 'v_callkit_plugin_method_channel.dart';
import 'models/call_data.dart';
import 'models/call_event.dart';
import 'models/ui_configuration.dart';

/// Main plugin class for VCallkit functionality
class VCallkitPlugin {
  // Constants for better maintainability
  static const List<String> _customChineseRomManufacturers = [
    'xiaomi',
    'redmi',
    'poco',
    'huawei',
    'honor',
    'oppo',
    'vivo',
    'realme',
    'oneplus',
  ];

  // Singleton pattern
  static final VCallkitPlugin _instance = VCallkitPlugin._internal();
  factory VCallkitPlugin() => _instance;
  VCallkitPlugin._internal();

  final MethodChannelVCallkitPlugin _methodChannelPlugin =
      VCallkitPluginPlatform.instance as MethodChannelVCallkitPlugin;

  // Essential event streams only (removed onCallHold, onCallMute, onAllEvents)
  late final Stream<CallAnsweredEvent> _onCallAnswered;
  late final Stream<CallRejectedEvent> _onCallRejected;
  late final Stream<CallEndedEvent> _onCallEnded;
  late final Stream<CallStateChangedEvent> _onCallStateChanged;

  /// Stream of essential call events only
  late final Stream<CallEvent> _onCallEvent;

  bool _initialized = false;

  // Global UI configuration storage using the new class
  VCallkitUIConfiguration _globalUIConfiguration =
      const VCallkitUIConfiguration();

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

  /// Stream of call rejected events
  Stream<CallRejectedEvent> get onCallRejected {
    _ensureInitialized();
    return _onCallRejected;
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

  /// Stream of essential call events
  Stream<CallEvent> get onCallEvent {
    _ensureInitialized();
    return _onCallEvent;
  }

  // Public API - Basic Call Operations

  /// Get platform version
  Future<String?> getPlatformVersion() {
    return VCallkitPluginPlatform.instance.getPlatformVersion();
  }

  /// Check if the app has required VoIP permissions
  Future<bool> hasPermissions() {
    return VCallkitPluginPlatform.instance.hasPermissions();
  }

  /// Request VoIP permissions (Android only)
  /// Note: On Android, permissions need to be requested through system settings
  Future<bool> requestPermissions() {
    return VCallkitPluginPlatform.instance.requestPermissions();
  }

  /// Show an incoming call with the provided call data
  Future<bool> showIncomingCall(CallData callData) {
    return VCallkitPluginPlatform.instance.showIncomingCall(callData.toMap());
  }

  /// Show an incoming call with custom configuration
  /// This allows per-call customization overriding global settings
  Future<bool> showIncomingCallWithConfig(Map<String, dynamic> data) {
    return VCallkitPluginPlatform.instance.showIncomingCallWithConfig(data);
  }

  /// Set global UI configuration using the VCallkitUIConfiguration class
  /// This includes themes, translations, and behavior settings
  Future<bool> setUIConfiguration(VCallkitUIConfiguration config) {
    _globalUIConfiguration = config;
    return VCallkitPluginPlatform.instance.setUIConfiguration(config.toMap());
  }

  /// Overloaded method to accept Map for backward compatibility
  Future<bool> setUIConfigurationFromMap(Map<String, dynamic> config) {
    final uiConfig = VCallkitUIConfiguration.fromMap(config);
    return setUIConfiguration(uiConfig);
  }

  /// Get the current global UI configuration
  VCallkitUIConfiguration get globalUIConfiguration => _globalUIConfiguration;

  /// Get the current global UI configuration as Map for backward compatibility
  Map<String, dynamic> get globalUIConfigurationMap =>
      _globalUIConfiguration.toMap();

  /// Force show hangup notification for testing purposes
  Future<bool> forceShowHangupNotification(Map<String, dynamic> data) {
    return VCallkitPluginPlatform.instance.forceShowHangupNotification(data);
  }

  /// Get debug information about the call manager state
  Future<Map<String, dynamic>> getCallManagerDebugInfo() {
    return VCallkitPluginPlatform.instance.getCallManagerDebugInfo();
  }

  /// End the current call
  Future<bool> endCall([String? callId]) {
    return VCallkitPluginPlatform.instance.endCall(callId);
  }

  /// Answer the current call
  Future<bool> answerCall([String? callId]) {
    return VCallkitPluginPlatform.instance.answerCall(callId);
  }

  /// Reject the current call
  Future<bool> rejectCall([String? callId]) {
    return VCallkitPluginPlatform.instance.rejectCall(callId);
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

  // Public API - Audio and Customization

  /// Sets a custom ringtone for incoming calls
  Future<bool> setCustomRingtone(String? ringtoneUri) {
    return VCallkitPluginPlatform.instance.setCustomRingtone(ringtoneUri);
  }

  /// Gets available system ringtones
  Future<List<Map<String, dynamic>>> getSystemRingtones() {
    return VCallkitPluginPlatform.instance.getSystemRingtones();
  }

  // Public API - System Integration

  /// Check if battery optimization is ignored (important for Xiaomi/Huawei)
  Future<bool> checkBatteryOptimization() {
    return VCallkitPluginPlatform.instance.checkBatteryOptimization();
  }

  /// Request to ignore battery optimization (important for Xiaomi/Huawei)
  Future<bool> requestBatteryOptimization() {
    return VCallkitPluginPlatform.instance.requestBatteryOptimization();
  }

  /// Get device manufacturer
  Future<String> getDeviceManufacturer() {
    return VCallkitPluginPlatform.instance.getDeviceManufacturer();
  }

  /// Check if device is from a manufacturer with custom OS that might affect calls
  Future<bool> isCustomChineseRom() async {
    final manufacturer = (await getDeviceManufacturer()).toLowerCase();
    return _customChineseRomManufacturers.contains(manufacturer);
  }

  // Public API - Call Action Launch Detection

  /// Get the last call action that launched the app (if any)
  Future<Map<String, dynamic>?> getLastCallActionLaunch() {
    return VCallkitPluginPlatform.instance.getLastCallActionLaunch();
  }

  /// Check if the app was launched from a call notification action
  Future<bool> hasCallActionLaunchData() {
    return VCallkitPluginPlatform.instance.hasCallActionLaunchData();
  }

  /// Clear any stored call action launch data
  Future<bool> clearCallActionLaunchData() {
    return VCallkitPluginPlatform.instance.clearCallActionLaunchData();
  }

  // Public API - Foreground Service Control (Essential for Android 14+)

  /// Start persistent notification for outgoing calls
  ///
  /// This method creates a non-dismissible notification for outgoing calls once they are accepted.
  /// Perfect for when you initiate a call from your device and want both caller and callee
  /// to see a persistent notification during the call.
  ///
  /// Features:
  /// - Non-dismissible notification with hangup button
  /// - Live duration timer
  /// - Survives app backgrounding and termination
  /// - Works on Android 14+ with proper foreground service
  ///
  /// [callData] - The call information to display
  ///
  /// Returns true if the outgoing call notification was successfully started
  Future<bool> startOutgoingCallNotification(CallData callData) {
    return VCallkitPluginPlatform.instance.startOutgoingCallNotification(
      callData.toMap(),
    );
  }

  /// Stop the call foreground service
  ///
  /// This method stops any active foreground service and removes the persistent notification.
  /// Call this when the call ends to clean up the notification.
  ///
  /// Returns true if the foreground service was successfully stopped
  Future<bool> stopCallForegroundService() {
    return VCallkitPluginPlatform.instance.stopCallForegroundService();
  }

  // Public API - Enhanced Convenience Methods

  /// Show an incoming voice call with minimal setup
  Future<bool> showIncomingVoiceCall({
    String? callId,
    required String callerName,
    required String callerNumber,
    String? callerAvatar,
    Map<String, dynamic> extra = const {},
    VCallkitUIConfiguration? customConfig,
  }) {
    return _showIncomingCallWithType(
      callId: callId,
      callerName: callerName,
      callerNumber: callerNumber,
      callerAvatar: callerAvatar,
      isVideoCall: false,
      extra: extra,
      customConfig: customConfig,
    );
  }

  /// Show an incoming video call with minimal setup
  Future<bool> showIncomingVideoCall({
    String? callId,
    required String callerName,
    required String callerNumber,
    String? callerAvatar,
    Map<String, dynamic> extra = const {},
    VCallkitUIConfiguration? customConfig,
  }) {
    return _showIncomingCallWithType(
      callId: callId,
      callerName: callerName,
      callerNumber: callerNumber,
      callerAvatar: callerAvatar,
      isVideoCall: true,
      extra: extra,
      customConfig: customConfig,
    );
  }

  /// Show an incoming call with comprehensive configuration
  Future<bool> showIncomingCallWithFullConfig({
    String? callId,
    required String callerName,
    required String callerNumber,
    String? callerAvatar,
    required bool isVideoCall,
    Map<String, dynamic> extra = const {},
    VCallkitUIConfiguration? customConfig,
  }) {
    final callData = CallData(
      id: callId ?? DateTime.now().millisecondsSinceEpoch.toString(),
      callerName: callerName,
      callerNumber: callerNumber,
      callerAvatar: callerAvatar,
      isVideoCall: isVideoCall,
      extra: extra,
    );

    if (customConfig != null) {
      return showIncomingCallWithConfig({
        'callData': callData.toMap(),
        'config': customConfig.toMap(),
      });
    }

    return showIncomingCall(callData);
  }

  // Public API - Event Filtering

  /// Listen to specific call events for a particular call ID
  Stream<CallEvent> listenToCall(String callId) {
    return onCallEvent.where((event) => event.callId == callId);
  }

  /// Listen to call answered events for a specific call
  Stream<CallAnsweredEvent> listenToCallAnswered([String? callId]) {
    return _filterStreamByCallId(onCallAnswered, callId);
  }

  /// Listen to call ended events for a specific call
  Stream<CallEndedEvent> listenToCallEnded([String? callId]) {
    return _filterStreamByCallId(onCallEnded, callId);
  }

  // Private helper methods

  /// Set up essential event streams only during initialization
  void _setupEventStreams() {
    _onCallAnswered = _methodChannelPlugin.onCallAnswered.map(
      (data) => CallAnsweredEvent.fromMap(data),
    );

    _onCallRejected = _methodChannelPlugin.onCallRejected.map(
      (data) => CallRejectedEvent.fromMap(data),
    );

    _onCallEnded = _methodChannelPlugin.onCallEnded.map(
      (data) => CallEndedEvent.fromMap(data),
    );

    _onCallStateChanged = _methodChannelPlugin.onCallStateChanged.map(
      (data) => CallStateChangedEvent.fromMap(data),
    );

    // Combined essential event stream only
    _onCallEvent = StreamGroup.merge([
      _onCallAnswered,
      _onCallRejected,
      _onCallEnded,
      _onCallStateChanged,
    ]);
  }

  /// Ensure the plugin is initialized
  void _ensureInitialized() {
    if (!_initialized) {
      initialize();
    }
  }

  /// Helper method to create call data and show incoming call with optional custom config
  Future<bool> _showIncomingCallWithType({
    String? callId,
    required String callerName,
    required String callerNumber,
    String? callerAvatar,
    required bool isVideoCall,
    Map<String, dynamic> extra = const {},
    VCallkitUIConfiguration? customConfig,
  }) {
    final callData = CallData(
      id: callId ?? DateTime.now().millisecondsSinceEpoch.toString(),
      callerName: callerName,
      callerNumber: callerNumber,
      callerAvatar: callerAvatar,
      isVideoCall: isVideoCall,
      extra: extra,
    );

    if (customConfig != null) {
      return showIncomingCallWithConfig({
        'callData': callData.toMap(),
        'config': customConfig.toMap(),
      });
    }

    return showIncomingCall(callData);
  }

  /// Filter stream by call ID
  Stream<T> _filterStreamByCallId<T extends CallEvent>(
    Stream<T> stream,
    String? callId,
  ) {
    if (callId == null) return stream;
    return stream.where((event) => event.callId == callId);
  }
}

/// Helper class for merging streams
class StreamGroup {
  static Stream<T> merge<T>(List<Stream<T>> streams) {
    late StreamController<T> controller;
    List<StreamSubscription<T>> subscriptions = [];

    controller = StreamController<T>(
      onListen: () {
        for (var stream in streams) {
          subscriptions.add(stream.listen(
            (data) => controller.add(data),
            onError: (error) => controller.addError(error),
          ));
        }
      },
      onCancel: () {
        for (var subscription in subscriptions) {
          subscription.cancel();
        }
        subscriptions.clear();
      },
    );

    return controller.stream;
  }
}
