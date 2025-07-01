import 'dart:async';

import 'v_callkit_plugin_platform_interface.dart';
import 'v_callkit_plugin_method_channel.dart';
import 'models/call_data.dart';
import 'models/call_event.dart';

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

  // Event streams with proper typing
  late final Stream<CallAnsweredEvent> _onCallAnswered;
  late final Stream<CallRejectedEvent> _onCallRejected;
  late final Stream<CallEndedEvent> _onCallEnded;
  late final Stream<CallHoldEvent> _onCallHold;
  late final Stream<CallMuteEvent> _onCallMute;
  late final Stream<CallStateChangedEvent> _onCallStateChanged;

  // Enhanced streams for customization
  late final Stream<Map<String, dynamic>> _onCallConfigurationChanged;
  late final Stream<Map<String, dynamic>> _onCallTimerUpdated;
  late final Stream<Map<String, dynamic>> _onCallAudioDeviceChanged;

  /// Stream of all call events
  late final Stream<CallEvent> _onCallEvent;

  /// Stream of all enhanced events (including configuration and timer updates)
  late final Stream<Map<String, dynamic>> _onAllEvents;

  bool _initialized = false;

  // Global UI configuration storage
  Map<String, dynamic> _globalUIConfiguration = {};

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

  // Public API - Event Streams

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

  /// Stream of call hold/unhold events
  Stream<CallHoldEvent> get onCallHold {
    _ensureInitialized();
    return _onCallHold;
  }

  /// Stream of call mute/unmute events
  Stream<CallMuteEvent> get onCallMute {
    _ensureInitialized();
    return _onCallMute;
  }

  /// Stream of call state change events
  Stream<CallStateChangedEvent> get onCallStateChanged {
    _ensureInitialized();
    return _onCallStateChanged;
  }

  /// Stream of call configuration change events
  Stream<Map<String, dynamic>> get onCallConfigurationChanged {
    _ensureInitialized();
    return _onCallConfigurationChanged;
  }

  /// Stream of call timer update events
  Stream<Map<String, dynamic>> get onCallTimerUpdated {
    _ensureInitialized();
    return _onCallTimerUpdated;
  }

  /// Stream of call audio device change events
  Stream<Map<String, dynamic>> get onCallAudioDeviceChanged {
    _ensureInitialized();
    return _onCallAudioDeviceChanged;
  }

  /// Stream of all call events
  Stream<CallEvent> get onCallEvent {
    _ensureInitialized();
    return _onCallEvent;
  }

  /// Stream of all enhanced events including configuration and timer updates
  Stream<Map<String, dynamic>> get onAllEvents {
    _ensureInitialized();
    return _onAllEvents;
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

  /// Set global UI configuration for all call screens
  /// This includes themes, translations, and behavior settings
  Future<bool> setUIConfiguration(Map<String, dynamic> config) {
    _globalUIConfiguration = Map<String, dynamic>.from(config);
    return VCallkitPluginPlatform.instance.setUIConfiguration(config);
  }

  /// Get the current global UI configuration
  Map<String, dynamic> get globalUIConfiguration =>
      Map<String, dynamic>.from(_globalUIConfiguration);

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

  /// Mute or unmute the current call
  Future<bool> muteCall(bool isMuted, [String? callId]) {
    return VCallkitPluginPlatform.instance.muteCall(isMuted, callId);
  }

  /// Hold or unhold the current call
  Future<bool> holdCall(bool isOnHold, [String? callId]) {
    return VCallkitPluginPlatform.instance.holdCall(isOnHold, callId);
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
    Map<String, dynamic>? customConfig,
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
    Map<String, dynamic>? customConfig,
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

    // Theme customization
    String? backgroundColor,
    String? accentColor,
    String? textColor,
    String? secondaryTextColor,

    // Text translations
    String? answerButtonText,
    String? declineButtonText,
    String? hangupButtonText,
    String? incomingCallText,
    String? callInProgressText,

    // Behavior settings
    bool? showCallerNumber,
    bool? enableVibration,
    bool? enableRingtone,
    bool? showCallDuration,
    int? callTimeoutSeconds,
  }) {
    final callData = CallData(
      id: callId ?? DateTime.now().millisecondsSinceEpoch.toString(),
      callerName: callerName,
      callerNumber: callerNumber,
      callerAvatar: callerAvatar,
      isVideoCall: isVideoCall,
      extra: extra,
    );

    final config = <String, dynamic>{
      // Merge global configuration
      ..._globalUIConfiguration,

      // Apply theme customization if provided
      if (backgroundColor != null) 'backgroundColor': backgroundColor,
      if (accentColor != null) 'accentColor': accentColor,
      if (textColor != null) 'textColor': textColor,
      if (secondaryTextColor != null) 'secondaryTextColor': secondaryTextColor,

      // Apply text translations if provided
      if (answerButtonText != null) 'answerButtonText': answerButtonText,
      if (declineButtonText != null) 'declineButtonText': declineButtonText,
      if (hangupButtonText != null) 'hangupButtonText': hangupButtonText,
      if (incomingCallText != null) 'incomingCallText': incomingCallText,
      if (callInProgressText != null) 'callInProgressText': callInProgressText,

      // Apply behavior settings if provided
      if (showCallerNumber != null) 'showCallerNumber': showCallerNumber,
      if (enableVibration != null) 'enableVibration': enableVibration,
      if (enableRingtone != null) 'enableRingtone': enableRingtone,
      if (showCallDuration != null) 'showCallDuration': showCallDuration,
      if (callTimeoutSeconds != null) 'callTimeoutSeconds': callTimeoutSeconds,
    };

    return showIncomingCallWithConfig({
      'callData': callData.toMap(),
      'config': config,
    });
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

  /// Listen to timer updates for a specific call
  Stream<Map<String, dynamic>> listenToCallTimer([String? callId]) {
    return onCallTimerUpdated
        .where((event) => callId == null || event['callId'] == callId);
  }

  /// Listen to configuration changes
  Stream<Map<String, dynamic>> get onConfigurationChanged =>
      onCallConfigurationChanged;

  // Public API - Advanced Configuration Helpers

  /// Create a theme configuration map
  static Map<String, dynamic> createTheme({
    required String backgroundColor,
    required String accentColor,
    required String textColor,
    required String secondaryTextColor,
  }) {
    return {
      'backgroundColor': backgroundColor,
      'accentColor': accentColor,
      'textColor': textColor,
      'secondaryTextColor': secondaryTextColor,
    };
  }

  /// Create a translation configuration map
  static Map<String, dynamic> createTranslation({
    required String answerButtonText,
    required String declineButtonText,
    required String hangupButtonText,
    required String incomingVoiceCallText,
    required String incomingVideoCallText,
    required String callInProgressText,
    String? tapToReturnText,
    String? unknownCallerText,
  }) {
    return {
      'answerButtonText': answerButtonText,
      'declineButtonText': declineButtonText,
      'hangupButtonText': hangupButtonText,
      'incomingVoiceCallText': incomingVoiceCallText,
      'incomingVideoCallText': incomingVideoCallText,
      'callInProgressText': callInProgressText,
      'tapToReturnText': tapToReturnText ?? 'Tap to return to call',
      'unknownCallerText': unknownCallerText ?? 'Unknown',
    };
  }

  /// Create a behavior configuration map
  static Map<String, dynamic> createBehaviorConfig({
    bool showCallerNumber = true,
    bool enableVibration = true,
    bool enableRingtone = true,
    bool showCallDuration = true,
    int callTimeoutSeconds = 60,
    bool enableCallTimeout = true,
    bool useFullScreenCallUI = true,
  }) {
    return {
      'showCallerNumber': showCallerNumber,
      'enableVibration': enableVibration,
      'enableRingtone': enableRingtone,
      'showCallDuration': showCallDuration,
      'callTimeoutSeconds': callTimeoutSeconds,
      'enableCallTimeout': enableCallTimeout,
      'useFullScreenCallUI': useFullScreenCallUI,
    };
  }

  // Private helper methods

  /// Set up all event streams during initialization
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

    _onCallHold = _methodChannelPlugin.onCallHold.map(
      (data) => CallHoldEvent.fromMap(data),
    );

    _onCallMute = _methodChannelPlugin.onCallMute.map(
      (data) => CallMuteEvent.fromMap(data),
    );

    _onCallStateChanged = _methodChannelPlugin.onCallStateChanged.map(
      (data) => CallStateChangedEvent.fromMap(data),
    );

    // Enhanced streams
    _onCallConfigurationChanged =
        _methodChannelPlugin.onCallConfigurationChanged;
    _onCallTimerUpdated = _methodChannelPlugin.onCallTimerUpdated;
    _onCallAudioDeviceChanged = _methodChannelPlugin.onCallAudioDeviceChanged;

    // Combined event stream
    _onCallEvent = StreamGroup.merge([
      _onCallAnswered,
      _onCallRejected,
      _onCallEnded,
      _onCallHold,
      _onCallMute,
      _onCallStateChanged,
    ]);

    // Combined enhanced events stream
    _onAllEvents = StreamGroup.merge([
      _onCallAnswered.map((e) => e.toMap()),
      _onCallRejected.map((e) => e.toMap()),
      _onCallEnded.map((e) => e.toMap()),
      _onCallHold.map((e) => e.toMap()),
      _onCallMute.map((e) => e.toMap()),
      _onCallStateChanged.map((e) => e.toMap()),
      _onCallConfigurationChanged,
      _onCallTimerUpdated,
      _onCallAudioDeviceChanged,
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
    Map<String, dynamic>? customConfig,
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
        'config': {
          ..._globalUIConfiguration,
          ...customConfig,
        },
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
    final controller = StreamController<T>.broadcast();

    final subscriptions = <StreamSubscription>[];

    for (final stream in streams) {
      final subscription = stream.listen(
        controller.add,
        onError: controller.addError,
      );
      subscriptions.add(subscription);
    }

    controller.onCancel = () {
      for (final subscription in subscriptions) {
        subscription.cancel();
      }
    };

    return controller.stream;
  }
}
