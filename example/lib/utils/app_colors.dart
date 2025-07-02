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
}

class AppStrings {
  static const String appTitle = 'VCallKit Plugin Example';
  static const String callStatusSection = 'Call Status';
  static const String callSimulationSection = 'Call Simulation';
  static const String eventLogSection = 'Event Log';

  // Call messages
  static const String callAnswered = 'Call answered successfully!';
  static const String callRejected = 'Call rejected';
  static const String callEnded = 'Call ended';
  static const String callError = 'Error updating call status';
}
