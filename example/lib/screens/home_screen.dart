import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:async';

import 'package:v_callkit_plugin/models/call_data.dart';

import '../widgets/call_status_widget.dart';
import '../widgets/call_simulation_widget.dart';
import '../widgets/call_action_launch_handler_widget.dart';
import '../widgets/event_log_widget.dart';
import '../widgets/error_dialog_widget.dart';
import '../widgets/outgoing_call_demo_widget.dart';
import '../widgets/customization_demo_navigation_widget.dart';
import '../widgets/firebase_test_widget.dart';
import '../services/event_logger.dart';
import '../services/call_service.dart';
import '../utils/app_colors.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  // Services
  late final EventLogger _eventLogger;
  late final CallService _callService;

  // State
  String _platformVersion = 'Unknown';
  bool _isCallActive = false;
  CallData? _activeCallData;

  // Controllers
  final ScrollController _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    _initializeServices();
    _initializePlatformState();
    _setupCallListeners();
  }

  @override
  void dispose() {
    _callService.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  void _initializeServices() {
    _eventLogger = EventLogger();
    _callService = CallService(_eventLogger);
  }

  Future<void> _initializePlatformState() async {
    try {
      await _callService.initialize();
      _platformVersion = await _callService.getPlatformVersion();
      await _updateCallStatus();
    } catch (e) {
      _eventLogger.addErrorEvent('Failed to initialize: $e');
    }

    if (mounted) {
      setState(() {});
    }
  }

  void _setupCallListeners() {
    final plugin = _callService.plugin;

    // Listen to all call events
    plugin.onCallEvent.listen((event) {
      _eventLogger.addCallEvent(event.action.name, event.callId);
      _updateCallStatus();
    });

    // Listen to specific events
    plugin.onCallAnswered.listen(_handleCallAnswered);
    plugin.onCallRejected.listen(_handleCallRejected);
    plugin.onCallEnded.listen(_handleCallEnded);
    plugin.onCallStateChanged.listen(_handleCallStateChanged);
  }

  Future<void> _updateCallStatus() async {
    if (!mounted) return;

    final isActive = await _callService.isCallActive();
    final callData = await _callService.getActiveCallData();

    setState(() {
      _isCallActive = isActive;
      _activeCallData = callData;
    });
  }

  // Call event handlers

  Future<void> _handleCallAnswered(dynamic event) async {
    _eventLogger.addSuccessEvent('Call Answered: ${event.callId}');
    _showSnackBar(AppStrings.callAnswered, AppColors.success);
    await _startPersistentNotification(event.callId);
  }

  void _handleCallRejected(dynamic event) {
    _eventLogger.addErrorEvent('Call Rejected: ${event.callId}');
    _showSnackBar(AppStrings.callRejected, AppColors.error);
  }

  Future<void> _handleCallEnded(dynamic event) async {
    _eventLogger.addEvent('🔚 Call Ended: ${event.callId} (${event.reason})');
    _showSnackBar(AppStrings.callEnded, AppColors.warning);
    await _stopPersistentNotification(event.callId);
  }

  void _handleCallStateChanged(dynamic event) {
    _eventLogger.addCallStateEvent(event.callId, event.state.name);
  }

  // Call management methods

  Future<void> _startPersistentNotification(String callId) async {
    try {
      _eventLogger.addInfoEvent(
        'Starting persistent call notification for call: $callId',
      );

      final callData = await _callService.getActiveCallData();
      if (callData != null) {
        await _callService.startPersistentNotification(callData);
        _showSuccessMessage('Persistent call notification started!');
      } else {
        _eventLogger.addErrorEvent(
          'No call data found for answered call: $callId',
        );
        _showSnackBar('No call data found for answered call', AppColors.error);
      }
    } catch (e) {
      _showSnackBar(
        'Error starting persistent notification: $e',
        AppColors.error,
      );
    }
  }

  Future<void> _stopPersistentNotification(String callId) async {
    try {
      _eventLogger.addEvent(
        '🛑 Stopping persistent call notification for call: $callId',
      );
      await _callService.stopPersistentNotification();
      _showInfoMessage('Persistent call notification stopped!');
    } catch (e) {
      _showSnackBar(
        'Error stopping persistent notification: $e',
        AppColors.error,
      );
    }
  }

  // Call simulation methods

  Future<void> _simulateIncomingCall({required bool isVideo}) async {
    try {
      final success = await _callService.showIncomingCall(isVideo: isVideo);
      if (success) {
        _showSnackBar(
          'Incoming ${isVideo ? 'video' : 'voice'} call simulation started',
          AppColors.info,
        );
      } else {
        _showSnackBar('Failed to show incoming call', AppColors.error);
      }
    } catch (e) {
      if (mounted &&
          e is PlatformException &&
          e.code == 'NOTIFICATIONS_DISABLED') {
        WidgetsBinding.instance.addPostFrameCallback((_) {
          if (mounted) {
            ErrorDialogWidget.showPhoneAccountError(context);
          }
        });
      } else {
        _showSnackBar('Error showing incoming call: $e', AppColors.error);
      }
    }
  }

  Future<void> _testVibration() async {
    try {
      final success = await _callService.testVibration();
      if (success) {
        _showVibrationTestMessage();
      } else {
        _showSnackBar('Failed to trigger test vibration call', AppColors.error);
      }
    } catch (e) {
      _showSnackBar('Error testing vibration: $e', AppColors.error);
    }
  }

  // Call action handling

  void _handleCallActionLaunch(String action, CallData callData) {
    _eventLogger.addInfoEvent(
      'Call action handler triggered: $action for ${callData.callerName}',
    );

    final message = action == 'ANSWER'
        ? _getAnswerActionMessage()
        : _getDeclineActionMessage();

    final color = action == 'ANSWER' ? AppColors.success : AppColors.warning;
    _showSnackBar(message, color);
  }

  // UI helper methods

  void _showSnackBar(String message, Color color) {
    if (!mounted) return;

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: color,
        duration: const Duration(seconds: 2),
        behavior: SnackBarBehavior.floating,
        margin: const EdgeInsets.all(16),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
    );
  }

  void _showSuccessMessage(String title) {
    _showSnackBar(
      '$title\n• Non-dismissible notification shown\n• Works on Android 9+ and below\n• Hangup button available',
      AppColors.success,
    );
  }

  void _showInfoMessage(String title) {
    _showSnackBar(
      '$title\n• Foreground service stopped\n• Notification dismissed',
      AppColors.info,
    );
  }

  void _showVibrationTestMessage() {
    _showSnackBar(
      'Vibration test call shown!\n• Check if device vibrates\n• Answer or decline to dismiss\n• Check logs for vibration debug info',
      AppColors.success,
    );
  }

  String _getAnswerActionMessage() {
    return 'Call answered from notification! You can now:\n• Make HTTP requests\n• Join the call\n• Update user status';
  }

  String _getDeclineActionMessage() {
    return 'Call declined from notification! You can now:\n• Update call status\n• Send decline notification\n• Log the decline';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(appBar: _buildAppBar(), body: _buildBody());
  }

  PreferredSizeWidget _buildAppBar() {
    return AppBar(
      title: const Text(
        AppStrings.appTitle,
        style: TextStyle(fontWeight: FontWeight.bold),
      ),
      backgroundColor: AppColors.primary,
      foregroundColor: Colors.white,
      elevation: 2,
      actions: [
        IconButton(
          onPressed: _updateCallStatus,
          icon: const Icon(Icons.refresh),
          tooltip: 'Refresh Status',
        ),
      ],
    );
  }

  Widget _buildBody() {
    return SingleChildScrollView(
      controller: _scrollController,
      child: Column(
        children: [
          CallActionLaunchHandlerWidget(
            onCallActionLaunch: _handleCallActionLaunch,
            onShowSnackBar: _showSnackBar,
            onAddToEventLog: _eventLogger.addEvent,
          ),
          CallStatusWidget(
            isCallActive: _isCallActive,
            activeCallData: _activeCallData,
            platformVersion: _platformVersion,
          ),
          const FirebaseTestWidget(),
          const CustomizationDemoNavigationWidget(),
          CallSimulationWidget(
            hasPermissions: true,
            onVoiceCallSimulation: () => _simulateIncomingCall(isVideo: false),
            onVideoCallSimulation: () => _simulateIncomingCall(isVideo: true),
            onTestVibration: _testVibration,
          ),
          const OutgoingCallDemoWidget(),
          EventLogWidget(
            eventLog: _eventLogger.events.toList(),
            onClearLog: _eventLogger.clearEvents,
          ),
          const SizedBox(height: 20),
        ],
      ),
    );
  }
}
