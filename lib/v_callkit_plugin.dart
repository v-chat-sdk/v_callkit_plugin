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
  late final Stream<CallDtmfEvent> _onDtmfTone;

  /// Stream of all call events
  late final Stream<CallEvent> _onCallEvent;

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

  /// Stream of DTMF tone events
  Stream<CallDtmfEvent> get onDtmfTone {
    _ensureInitialized();
    return _onDtmfTone;
  }

  /// Stream of all call events
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

  // Public API - Notifications

  /// Force show ongoing call notification
  Future<bool> forceShowOngoingNotification(CallData callData) {
    return VCallkitPluginPlatform.instance.forceShowOngoingNotification(
      callData.toMap(),
    );
  }

  /// Show persistent hangup notification
  ///
  /// This method shows a persistent notification that can only be dismissed
  /// via the hangup button. It appears after answering a call and provides
  /// a non-dismissible way to hang up the call from the notification area.
  ///
  /// The notification includes:
  /// - Call duration timer
  /// - Caller information
  /// - Single "Hangup" action button
  /// - Non-swipeable, persistent until hangup
  ///
  /// [callData] - The call information to display
  ///
  /// Returns true if the hangup notification was successfully shown
  Future<bool> showHangupNotification(CallData callData) {
    return VCallkitPluginPlatform.instance.showHangupNotification(
      callData.toMap(),
    );
  }

  /// Hide hangup notification
  ///
  /// This method removes the persistent hangup notification. Should be called
  /// when the call ends or when the hangup button is pressed.
  ///
  /// Returns true if the hangup notification was successfully hidden
  Future<bool> hideHangupNotification() {
    return VCallkitPluginPlatform.instance.hideHangupNotification();
  }

  /// Update hangup notification with new call data
  ///
  /// This method updates the existing hangup notification with new information
  /// while keeping the duration timer running. Useful for updating caller info
  /// or other call details during the call.
  ///
  /// [callData] - The updated call information to display
  ///
  /// Returns true if the hangup notification was successfully updated
  Future<bool> updateHangupNotification(CallData callData) {
    return VCallkitPluginPlatform.instance.updateHangupNotification(
      callData.toMap(),
    );
  }

  /// Launch hangup notification with the same foreground service pattern as accepting calls from notifications
  ///
  /// This method demonstrates that hangup notifications use the exact same foreground service
  /// robustness as when calls are accepted from notification actions. It provides:
  ///
  /// - Same foreground service pattern as CallForegroundService
  /// - Non-dismissible notification with proper Android flags
  /// - Live duration timer updates
  /// - App backgrounding and termination survival
  /// - Same service lifecycle management as call acceptance
  ///
  /// This is the recommended method to use when you want to ensure your hangup
  /// notification has the same reliability as the ongoing call notifications that
  /// appear when accepting calls from notification actions.
  ///
  /// [callData] - The call information to display
  ///
  /// Returns true if the hangup notification was successfully launched with foreground service
  Future<bool> launchHangupNotificationWithForegroundService(
    CallData callData,
  ) {
    return VCallkitPluginPlatform.instance
        .launchHangupNotificationWithForegroundService(callData.toMap());
  }

  // Public API - Advanced Configuration

  /// Show an incoming call with custom configuration
  Future<bool> showIncomingCallWithConfig(Map<String, dynamic> params) async {
    final callDataMap = _extractCallDataFromParams(params);
    final config = _extractConfigFromParams(params);

    final mergedData = _mergeCallDataWithConfig(callDataMap, config);
    return VCallkitPluginPlatform.instance.showIncomingCall(mergedData);
  }

  /// Set global UI configuration
  Future<bool> setUIConfiguration(Map<String, dynamic> config) async {
    return VCallkitPluginPlatform.instance.setUIConfiguration(config);
  }

  /// Force show hangup notification (alias for showHangupNotification)
  Future<bool> forceShowHangupNotification(Map<String, dynamic> params) async {
    final callDataMap = _extractCallDataFromParams(params);
    final config = _extractConfigFromParams(params);

    final mergedData = _mergeCallDataWithConfig(callDataMap, config);
    return VCallkitPluginPlatform.instance.showHangupNotification(mergedData);
  }

  /// Get call manager debug information
  Future<Map<String, dynamic>> getCallManagerDebugInfo() async {
    return VCallkitPluginPlatform.instance.getCallManagerDebugInfo();
  }

  // Public API - Foreground Service Control

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

  /// Start persistent notification for incoming calls
  ///
  /// This method provides explicit control over incoming call notifications
  /// using the foreground service pattern. Use this when you want to manually
  /// control when the persistent notification appears for incoming calls.
  ///
  /// [callData] - The call information to display
  ///
  /// Returns true if the incoming call notification was successfully started
  Future<bool> startIncomingCallNotification(CallData callData) {
    return VCallkitPluginPlatform.instance.startIncomingCallNotification(
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

  /// Update the call foreground service with new call data
  ///
  /// This method updates the persistent notification with new information
  /// while keeping the service running. Useful for updating caller info
  /// or other call details during the call.
  ///
  /// [callData] - The updated call information to display
  ///
  /// Returns true if the foreground service was successfully updated
  Future<bool> updateCallForegroundService(CallData callData) {
    return VCallkitPluginPlatform.instance.updateCallForegroundService(
      callData.toMap(),
    );
  }

  // Public API - Convenience Methods

  /// Show an incoming voice call with minimal setup
  Future<bool> showIncomingVoiceCall({
    String? callId,
    required String callerName,
    required String callerNumber,
    String? callerAvatar,
    Map<String, dynamic> extra = const {},
  }) {
    return _showIncomingCallWithType(
      callId: callId,
      callerName: callerName,
      callerNumber: callerNumber,
      callerAvatar: callerAvatar,
      isVideoCall: false,
      extra: extra,
    );
  }

  /// Show an incoming video call with minimal setup
  Future<bool> showIncomingVideoCall({
    String? callId,
    required String callerName,
    required String callerNumber,
    String? callerAvatar,
    Map<String, dynamic> extra = const {},
  }) {
    return _showIncomingCallWithType(
      callId: callId,
      callerName: callerName,
      callerNumber: callerNumber,
      callerAvatar: callerAvatar,
      isVideoCall: true,
      extra: extra,
    );
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

    _onDtmfTone = _methodChannelPlugin.onDtmfTone.map(
      (data) => CallDtmfEvent.fromMap(data),
    );

    // Combined event stream
    _onCallEvent = StreamGroup.merge([
      _onCallAnswered,
      _onCallRejected,
      _onCallEnded,
      _onCallHold,
      _onCallMute,
      _onCallStateChanged,
      _onDtmfTone,
    ]);
  }

  /// Ensure the plugin is initialized
  void _ensureInitialized() {
    if (!_initialized) {
      initialize();
    }
  }

  /// Helper method to reduce duplication in convenience methods
  Future<bool> _showIncomingCallWithType({
    String? callId,
    required String callerName,
    required String callerNumber,
    String? callerAvatar,
    required bool isVideoCall,
    Map<String, dynamic> extra = const {},
  }) {
    final callData = CallData(
      id: callId ?? DateTime.now().millisecondsSinceEpoch.toString(),
      callerName: callerName,
      callerNumber: callerNumber,
      callerAvatar: callerAvatar,
      isVideoCall: isVideoCall,
      extra: extra,
    );
    return showIncomingCall(callData);
  }

  /// Extract call data from configuration parameters
  Map<String, dynamic> _extractCallDataFromParams(Map<String, dynamic> params) {
    return params['callData'] as Map<String, dynamic>? ?? {};
  }

  /// Extract config from configuration parameters
  Map<String, dynamic> _extractConfigFromParams(Map<String, dynamic> params) {
    return params['config'] as Map<String, dynamic>? ?? {};
  }

  /// Merge call data with configuration
  Map<String, dynamic> _mergeCallDataWithConfig(
    Map<String, dynamic> callData,
    Map<String, dynamic> config,
  ) {
    return Map<String, dynamic>.from(callData)..addAll(config);
  }

  /// Filter stream by call ID if provided
  Stream<T> _filterStreamByCallId<T extends CallEvent>(
    Stream<T> stream,
    String? callId,
  ) {
    return callId != null
        ? stream.where((event) => event.callId == callId)
        : stream;
  }
}

/// Helper class to merge multiple streams
class StreamGroup<T> {
  static Stream<T> merge<T>(Iterable<Stream<T>> streams) {
    final controller = StreamController<T>.broadcast();
    final subscriptions = <StreamSubscription>[];

    for (final stream in streams) {
      subscriptions.add(
        stream.listen(controller.add, onError: controller.addError),
      );
    }

    controller.onCancel = () {
      for (final subscription in subscriptions) {
        subscription.cancel();
      }
    };

    return controller.stream;
  }
}
