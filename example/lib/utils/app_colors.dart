import 'package:flutter/material.dart';

class AppColors {
  static const Color primary = Colors.blue;
  static const Color secondary = Colors.blueAccent;
  static const Color success = Colors.green;
  static const Color error = Colors.red;
  static const Color warning = Colors.orange;
  static const Color info = Colors.blue;

  // Status colors
  static const Color activeCall = Colors.green;
  static const Color inactiveCall = Colors.grey;
  static const Color permissionGranted = Colors.green;
  static const Color permissionDenied = Colors.red;
}

class AppStrings {
  static const String appTitle = 'VCallKit Plugin Example';
  static const String permissionsSection = 'Permissions';
  static const String callStatusSection = 'Call Status';
  static const String callSimulationSection = 'Call Simulation';
  static const String backgroundCallSection = 'Background Call Scheduler';
  static const String eventLogSection = 'Event Log';

  // Permission messages
  static const String permissionsGranted = 'Permissions granted!';
  static const String permissionsRequired =
      'Permissions required to show incoming calls';
  static const String permissionsNotGranted =
      'Permissions not granted. Please enable in settings.';
  static const String permissionsError = 'Error requesting permissions';

  // Call messages
  static const String callAnswered = 'Call answered successfully!';
  static const String callRejected = 'Call rejected';
  static const String callEnded = 'Call ended';
  static const String callError = 'Error updating call status';

  // Background call messages
  static const String scheduleBackgroundCall = 'Schedule Background Call';
  static const String cancelScheduledCall = 'Cancel Scheduled Call';
  static const String backgroundCallScheduled = 'Background call scheduled!';
  static const String backgroundCallCancelled = 'Scheduled call cancelled';
  static const String backgroundCallError = 'Error scheduling background call';
  static const String terminateAppMessage =
      'Minimize the app to test background call';
}
