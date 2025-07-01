import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:async';
import 'dart:math';
import 'dart:developer' as developer;

import 'package:v_callkit_plugin/v_callkit_plugin.dart';
import 'package:v_callkit_plugin/models/call_data.dart';

import '../widgets/permissions_widget.dart';
import '../widgets/call_status_widget.dart';
import '../widgets/call_simulation_widget.dart';
import '../widgets/background_call_scheduler_widget.dart';
import '../widgets/call_action_launch_handler_widget.dart';
import '../widgets/event_log_widget.dart';
import '../widgets/error_dialog_widget.dart';
import '../widgets/ringtone_selection_widget.dart';
import '../widgets/battery_optimization_widget.dart';
import '../widgets/outgoing_call_demo_widget.dart';
import '../widgets/customization_demo_navigation_widget.dart';
import '../models/sample_caller.dart';
import '../utils/app_colors.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  String _platformVersion = 'Unknown';
  bool _hasPermissions = false;
  bool _isCallActive = false;
  CallData? _activeCallData;
  final _vCallkitPlugin = VCallkitPlugin();

  final List<String> _eventLog = [];
  final ScrollController _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    _initializePlatformState();
    _setupCallListeners();
  }

  @override
  void dispose() {
    _vCallkitPlugin.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  Future<void> _initializePlatformState() async {
    String platformVersion;
    bool hasPermissions = false;

    try {
      // Initialize the plugin
      _vCallkitPlugin.initialize();

      // Get platform version
      platformVersion =
          await _vCallkitPlugin.getPlatformVersion() ??
          'Unknown platform version';

      // Check permissions
      hasPermissions = await _vCallkitPlugin.hasPermissions();
    } on PlatformException catch (e) {
      platformVersion = 'Failed to get platform version: ${e.message}';
    }

    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
      _hasPermissions = hasPermissions;
    });

    // Update call status
    await _updateCallStatus();
  }

  void _setupCallListeners() {
    // Listen to all call events
    _vCallkitPlugin.onCallEvent.listen((event) {
      _addToEventLog('📞 Call Event: ${event.action.name} for ${event.callId}');
      _updateCallStatus();
    });

    // Listen to specific events
    _vCallkitPlugin.onCallAnswered.listen((event) async {
      _addToEventLog('✅ Call Answered: ${event.callId}');
      _showSnackBar(AppStrings.callAnswered, AppColors.success);

      // 🎯 EXAMPLE: Start outgoing call notification after call is answered
      // This is where you control when to show the persistent call notification
      await _handleCallAnswered(event.callId);
    });

    _vCallkitPlugin.onCallRejected.listen((event) {
      _addToEventLog('❌ Call Rejected: ${event.callId}');
      _showSnackBar(AppStrings.callRejected, AppColors.error);
    });

    _vCallkitPlugin.onCallEnded.listen((event) async {
      _addToEventLog('🔚 Call Ended: ${event.callId} (${event.reason})');
      _showSnackBar(AppStrings.callEnded, AppColors.warning);

      // 🎯 EXAMPLE: Stop persistent notification when call ends
      await _handleCallEnded(event.callId);
    });

    _vCallkitPlugin.onCallStateChanged.listen((event) {
      _addToEventLog('🔄 Call State: ${event.callId} -> ${event.state.name}');
    });
  }

  void _addToEventLog(String message) {
    setState(() {
      _eventLog.insert(0, '${DateTime.now().toIso8601String()}: $message');
      if (_eventLog.length > 50) {
        _eventLog.removeRange(50, _eventLog.length);
      }
    });
  }

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

  Future<void> _updateCallStatus() async {
    try {
      final isActive = await _vCallkitPlugin.isCallActive();
      final callData = await _vCallkitPlugin.getActiveCallData();

      if (mounted) {
        setState(() {
          _isCallActive = isActive;
          _activeCallData = callData;
        });
      }
    } catch (e) {
      _addToEventLog('❌ ${AppStrings.callError}: $e');
    }
  }

  Future<void> _requestPermissions() async {
    try {
      final granted = await _vCallkitPlugin.requestPermissions();
      setState(() {
        _hasPermissions = granted;
      });

      if (granted) {
        _showSnackBar(AppStrings.permissionsGranted, AppColors.success);
      } else {
        _showSnackBar(AppStrings.permissionsNotGranted, AppColors.error);
      }
    } on PlatformException catch (e) {
      developer.log(
        'Permission error: ${e.code} - ${e.message}',
        name: 'VCallkit',
      );

      if (mounted) {
        if (e.code == 'NOTIFICATIONS_DISABLED') {
          // Use addPostFrameCallback to ensure context is still valid
          WidgetsBinding.instance.addPostFrameCallback((_) {
            if (mounted) {
              ErrorDialogWidget.showPhoneAccountError(context);
            }
          });
        } else {
          _showSnackBar(
            '${AppStrings.permissionsError}: ${e.message}',
            AppColors.error,
          );
        }
      }
    } catch (e) {
      developer.log('Unexpected permission error: $e', name: 'VCallkit');
      _showSnackBar('${AppStrings.permissionsError}: $e', AppColors.error);
    }
  }

  Future<void> _showIncomingCall({bool isVideo = false}) async {
    if (!_hasPermissions) {
      _showSnackBar(AppStrings.permissionsRequired, AppColors.error);
      return;
    }

    final random = Random();
    final caller = SampleCaller
        .sampleCallers[random.nextInt(SampleCaller.sampleCallers.length)];

    try {
      final callData = CallData(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        callerName: caller.name,
        callerNumber: caller.number,
        callerAvatar: caller.avatar,
        isVideoCall: isVideo,
        extra: {
          'source': 'example_app',
          'timestamp': DateTime.now().toIso8601String(),
        },
      );

      final success = await _vCallkitPlugin.showIncomingCall(callData);

      if (success) {
        _addToEventLog(
          '📱 Incoming ${isVideo ? 'video' : 'voice'} call shown: ${caller.name}',
        );
        _showSnackBar(
          'Incoming ${isVideo ? 'video' : 'voice'} call from ${caller.name}',
          AppColors.info,
        );
      } else {
        _showSnackBar('Failed to show incoming call', AppColors.error);
      }
    } catch (e) {
      developer.log('Incoming call error: $e', name: 'VCallkit');

      // Handle specific platform exceptions
      if (mounted) {
        if (e is PlatformException) {
          if (e.code == 'NOTIFICATIONS_DISABLED') {
            // Use addPostFrameCallback to ensure context is still valid
            WidgetsBinding.instance.addPostFrameCallback((_) {
              if (mounted) {
                ErrorDialogWidget.showPhoneAccountError(context);
              }
            });
          } else {
            _showSnackBar('Error: ${e.message}', AppColors.error);
          }
        } else {
          _showSnackBar('Error showing incoming call: $e', AppColors.error);
        }
      }
    }
  }

  void _clearEventLog() {
    setState(() {
      _eventLog.clear();
    });
  }

  /// 🎯 EXAMPLE: Handle call answered event and start persistent notification
  /// This is called when a call is answered from the notification
  Future<void> _handleCallAnswered(String callId) async {
    try {
      _addToEventLog(
        '🔥 Starting persistent call notification for call: $callId',
      );

      // Get the call data for the answered call
      final callData = await _vCallkitPlugin.getActiveCallData();

      if (callData != null) {
        // 🎯 THIS IS THE KEY: Start the outgoing call notification
        // This creates a persistent, non-dismissible notification with hangup button
        await _vCallkitPlugin.startOutgoingCallNotification(callData);

        _addToEventLog(
          '✅ Persistent call notification started for: ${callData.callerName}',
        );
        _showSnackBar(
          'Persistent call notification started!\n'
          '• Non-dismissible notification shown\n'
          '• Works on Android 9+ and below\n'
          '• Hangup button available',
          AppColors.success,
        );

        // TODO: Add your call logic here
        // Example:
        // await _joinCall(callData);
        // await _updateUserStatus('in_call');
        // await _startCallTimer();
      } else {
        _addToEventLog('❌ No call data found for answered call: $callId');
        _showSnackBar('No call data found for answered call', AppColors.error);
      }
    } catch (e) {
      _addToEventLog('❌ Error starting persistent notification: $e');
      _showSnackBar(
        'Error starting persistent notification: $e',
        AppColors.error,
      );
    }
  }

  /// 🎯 EXAMPLE: Handle call ended event and stop persistent notification
  /// This is called when a call ends (hangup, decline, etc.)
  Future<void> _handleCallEnded(String callId) async {
    try {
      _addToEventLog(
        '🛑 Stopping persistent call notification for call: $callId',
      );

      // 🎯 THIS IS THE KEY: Stop the foreground service and notification
      await _vCallkitPlugin.stopCallForegroundService();

      _addToEventLog('✅ Persistent call notification stopped');
      _showSnackBar(
        'Persistent call notification stopped!\n'
        '• Foreground service stopped\n'
        '• Notification dismissed',
        AppColors.info,
      );

      // TODO: Add your cleanup logic here
      // Example:
      // await _leaveCall();
      // await _updateUserStatus('offline');
      // await _stopCallTimer();
    } catch (e) {
      _addToEventLog('❌ Error stopping persistent notification: $e');
      _showSnackBar(
        'Error stopping persistent notification: $e',
        AppColors.error,
      );
    }
  }

  void _handleCallActionLaunch(String action, CallData callData) {
    // This is where you can make HTTP requests and handle call logic
    _addToEventLog(
      '🔥 Call action handler triggered: $action for ${callData.callerName}',
    );

    if (action == 'ANSWER') {
      // Example: Join the call, make HTTP requests, etc.
      _showSnackBar(
        'Call answered from notification! You can now:\n'
        '• Make HTTP requests\n'
        '• Join the call\n'
        '• Update user status',
        AppColors.success,
      );

      // TODO: Add your HTTP requests here
      // Example:
      // await _joinCall(callData);
      // await _updateUserStatus('in_call');
    } else if (action == 'DECLINE') {
      // Example: Update call status, notify server, etc.
      _showSnackBar(
        'Call declined from notification! You can now:\n'
        '• Update call status\n'
        '• Send decline notification\n'
        '• Log the decline',
        AppColors.warning,
      );

      // TODO: Add your HTTP requests here
      // Example:
      // await _declineCall(callData);
      // await _notifyServer(callData.id, 'declined');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
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
      ),
      body: SingleChildScrollView(
        controller: _scrollController,
        child: Column(
          children: [
            CallActionLaunchHandlerWidget(
              onCallActionLaunch: _handleCallActionLaunch,
              onShowSnackBar: _showSnackBar,
              onAddToEventLog: _addToEventLog,
            ),
            PermissionsWidget(
              hasPermissions: _hasPermissions,
              onPermissionsRequested: _requestPermissions,
            ),
            const BatteryOptimizationWidget(),
            CallStatusWidget(
              isCallActive: _isCallActive,
              activeCallData: _activeCallData,
              platformVersion: _platformVersion,
            ),
            const CustomizationDemoNavigationWidget(),
            CallSimulationWidget(
              hasPermissions: _hasPermissions,
              onVoiceCallSimulation: () => _showIncomingCall(isVideo: false),
              onVideoCallSimulation: () => _showIncomingCall(isVideo: true),
            ),
            BackgroundCallSchedulerWidget(
              hasPermissions: _hasPermissions,
              onShowSnackBar: _showSnackBar,
              onAddToEventLog: _addToEventLog,
            ),
            const OutgoingCallDemoWidget(),
            const RingtoneSelectionWidget(),
            EventLogWidget(eventLog: _eventLog, onClearLog: _clearEventLog),
            const SizedBox(height: 20),
          ],
        ),
      ),
    );
  }
}
