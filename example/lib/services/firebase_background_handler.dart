import 'dart:math';
import 'dart:developer' as developer;
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:v_callkit_plugin/models/call_configuration.dart';
import 'package:v_callkit_plugin/v_callkit_plugin.dart';
import 'package:v_callkit_plugin/models/call_data.dart';

/// Firebase background message handler for showing fake calls
class FirebaseBackgroundHandler {
  static final VCallkitPlugin _plugin = VCallkitPlugin();

  /// List of fake caller data for simulation
  static const List<Map<String, String>> _fakeCallers = [
    {
      'name': 'Ahmed Hassan',
      'number': '+201234567890',
      'avatar': 'https://i.pravatar.cc/256?img=5',
    },
    {
      'name': 'Maria Garcia',
      'number': '+34987654321',
      'avatar': 'https://i.pravatar.cc/256?img=6',
    },
    {
      'name': 'John Smith',
      'number': '+1555123456',
      'avatar': 'https://i.pravatar.cc/256?img=1',
    },
    {
      'name': 'Sarah Wilson',
      'number': '+1555666777',
      'avatar': 'https://i.pravatar.cc/256?img=4',
    },
    {
      'name': 'Alex Turner',
      'number': '+447123456789',
      'avatar': '', // No avatar to test initials
    },
  ];

  /// Initialize the Firebase background handler
  static Future<void> initialize() async {
    try {
      // Initialize the plugin for background use
      _plugin.initialize();

      developer.log(
        'Firebase background handler initialized',
        name: 'FirebaseBackgroundHandler',
      );
    } catch (e) {
      developer.log(
        'Error initializing Firebase background handler: $e',
        name: 'FirebaseBackgroundHandler',
      );
    }
  }

  /// Handle background Firebase messages
  static Future<void> handleBackgroundMessage(RemoteMessage message) async {
    try {
      developer.log(
        'Background message received: ${message.messageId}',
        name: 'FirebaseBackgroundHandler',
      );

      // Show fake incoming call
      await _showFakeCall(message);

      developer.log(
        'Fake call triggered from background message',
        name: 'FirebaseBackgroundHandler',
      );
    } catch (e) {
      developer.log(
        'Error handling background message: $e',
        name: 'FirebaseBackgroundHandler',
      );
    }
  }

  /// Show a fake incoming call with random data
  static Future<void> _showFakeCall(RemoteMessage message) async {
    try {
      // Get random fake caller
      final random = Random();
      final fakeCaller = _fakeCallers[random.nextInt(_fakeCallers.length)];

      // Determine if it's a video call from message data or random
      final isVideoCall =
          message.data['isVideoCall'] == 'true' ||
          message.data['callType'] == 'video' ||
          random.nextBool();

      // Create call data
      final callData = CallData(
        id: 'firebase_call_${DateTime.now().millisecondsSinceEpoch}',
        callerName: message.data['callerName'] ?? fakeCaller['name']!,
        callerNumber: message.data['callerNumber'] ?? fakeCaller['number']!,
        callerAvatar: message.data['callerAvatar'] ?? fakeCaller['avatar']!,
        isVideoCall: isVideoCall,
        extra: {
          'source': 'firebase_background',
          'messageId': message.messageId ?? 'unknown',
          'timestamp': DateTime.now().toIso8601String(),
          'notification_title': message.notification?.title ?? '',
          'notification_body': message.notification?.body ?? '',
          ...message.data,
        },
      );

      developer.log(
        'Showing fake call: ${callData.callerName} (${isVideoCall ? 'Video' : 'Voice'})',
        name: 'FirebaseBackgroundHandler',
      );

      // Show the incoming call
      final success = await _plugin.showIncomingCall(
        callData: callData,
        configuration: VCallkitCallConfiguration(
          answerButtonText: "ANswer",
          enableVibration: true,
          showCallType: true,
        ),
      );

      if (success) {
        developer.log(
          'Fake call shown successfully',
          name: 'FirebaseBackgroundHandler',
        );
      } else {
        developer.log(
          'Failed to show fake call',
          name: 'FirebaseBackgroundHandler',
        );
      }
    } catch (e) {
      developer.log(
        'Error showing fake call: $e',
        name: 'FirebaseBackgroundHandler',
      );
    }
  }

  /// Get a random fake caller (for testing purposes)
  static Map<String, String> getRandomFakeCaller() {
    final random = Random();
    return _fakeCallers[random.nextInt(_fakeCallers.length)];
  }
}
