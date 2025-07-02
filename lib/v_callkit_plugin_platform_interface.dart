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

  /// Checks if the app has required VoIP permissions
  Future<bool> hasPermissions() {
    throw UnimplementedError('hasPermissions() has not been implemented.');
  }

  /// Requests VoIP permissions (on Android)
  Future<bool> requestPermissions() {
    throw UnimplementedError('requestPermissions() has not been implemented.');
  }

  /// Shows an incoming call with custom configuration
  Future<bool> showIncomingCallWithConfig(Map<String, dynamic> data) {
    throw UnimplementedError(
        'showIncomingCallWithConfig() has not been implemented.');
  }

  /// Answers the current call
  Future<bool> answerCall([String? callId]) {
    throw UnimplementedError('answerCall() has not been implemented.');
  }

  /// Checks if there's an active call
  Future<bool> isCallActive() {
    throw UnimplementedError('isCallActive() has not been implemented.');
  }

  /// Gets active call data
  Future<Map<String, dynamic>?> getActiveCallData() {
    throw UnimplementedError('getActiveCallData() has not been implemented.');
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
