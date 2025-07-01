import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'v_callkit_plugin_method_channel.dart';

abstract class VCallkitPluginPlatform extends PlatformInterface {
  /// Constructs a VCallkitPluginPlatform.
  VCallkitPluginPlatform() : super(token: _token);

  static final Object _token = Object();

  static VCallkitPluginPlatform _instance = MethodChannelVCallkitPlugin();

  /// The default instance of [VCallkitPluginPlatform] to use.
  ///
  /// Defaults to [MethodChannelVCallkitPlugin].
  static VCallkitPluginPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [VCallkitPluginPlatform] when
  /// they register themselves.
  static set instance(VCallkitPluginPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  /// Checks if the app has required VoIP permissions
  Future<bool> hasPermissions() {
    throw UnimplementedError('hasPermissions() has not been implemented.');
  }

  /// Requests VoIP permissions (on Android)
  Future<bool> requestPermissions() {
    throw UnimplementedError('requestPermissions() has not been implemented.');
  }

  /// Shows an incoming call with the provided call data
  Future<bool> showIncomingCall(Map<String, dynamic> callData) {
    throw UnimplementedError('showIncomingCall() has not been implemented.');
  }

  /// Shows an incoming call with custom configuration
  Future<bool> showIncomingCallWithConfig(Map<String, dynamic> data) {
    throw UnimplementedError(
        'showIncomingCallWithConfig() has not been implemented.');
  }

  /// Sets global UI configuration for all call screens
  Future<bool> setUIConfiguration(Map<String, dynamic> config) {
    throw UnimplementedError('setUIConfiguration() has not been implemented.');
  }

  /// Forces display of hangup notification for testing
  Future<bool> forceShowHangupNotification(Map<String, dynamic> data) {
    throw UnimplementedError(
        'forceShowHangupNotification() has not been implemented.');
  }

  /// Gets debug information about the call manager
  Future<Map<String, dynamic>> getCallManagerDebugInfo() {
    throw UnimplementedError(
        'getCallManagerDebugInfo() has not been implemented.');
  }

  /// Ends the current call
  Future<bool> endCall([String? callId]) {
    throw UnimplementedError('endCall() has not been implemented.');
  }

  /// Answers the current call
  Future<bool> answerCall([String? callId]) {
    throw UnimplementedError('answerCall() has not been implemented.');
  }

  /// Rejects the current call
  Future<bool> rejectCall([String? callId]) {
    throw UnimplementedError('rejectCall() has not been implemented.');
  }

  /// Mutes or unmutes the current call
  Future<bool> muteCall(bool isMuted, [String? callId]) {
    throw UnimplementedError('muteCall() has not been implemented.');
  }

  /// Holds or unholds the current call
  Future<bool> holdCall(bool isOnHold, [String? callId]) {
    throw UnimplementedError('holdCall() has not been implemented.');
  }

  /// Checks if there's an active call
  Future<bool> isCallActive() {
    throw UnimplementedError('isCallActive() has not been implemented.');
  }

  /// Gets active call data
  Future<Map<String, dynamic>?> getActiveCallData() {
    throw UnimplementedError('getActiveCallData() has not been implemented.');
  }

  /// Sets a custom ringtone for incoming calls
  Future<bool> setCustomRingtone(String? ringtoneUri) {
    throw UnimplementedError('setCustomRingtone() has not been implemented.');
  }

  /// Gets available system ringtones
  Future<List<Map<String, dynamic>>> getSystemRingtones() {
    throw UnimplementedError('getSystemRingtones() has not been implemented.');
  }

  /// Checks if battery optimization is ignored
  Future<bool> checkBatteryOptimization() {
    throw UnimplementedError(
        'checkBatteryOptimization() has not been implemented.');
  }

  /// Requests to ignore battery optimization
  Future<bool> requestBatteryOptimization() {
    throw UnimplementedError(
        'requestBatteryOptimization() has not been implemented.');
  }

  /// Gets device manufacturer
  Future<String> getDeviceManufacturer() {
    throw UnimplementedError(
        'getDeviceManufacturer() has not been implemented.');
  }

  /// Gets the last call action that launched the app
  Future<Map<String, dynamic>?> getLastCallActionLaunch() {
    throw UnimplementedError(
        'getLastCallActionLaunch() has not been implemented.');
  }

  /// Checks if the app was launched from a call notification action
  Future<bool> hasCallActionLaunchData() {
    throw UnimplementedError(
        'hasCallActionLaunchData() has not been implemented.');
  }

  /// Clears any stored call action launch data
  Future<bool> clearCallActionLaunchData() {
    throw UnimplementedError(
        'clearCallActionLaunchData() has not been implemented.');
  }

  /// Starts persistent notification for outgoing calls
  Future<bool> startOutgoingCallNotification(Map<String, dynamic> callData) {
    throw UnimplementedError(
        'startOutgoingCallNotification() has not been implemented.');
  }

  /// Stops the call foreground service
  Future<bool> stopCallForegroundService() {
    throw UnimplementedError(
        'stopCallForegroundService() has not been implemented.');
  }
}
