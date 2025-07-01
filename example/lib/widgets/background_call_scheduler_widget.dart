import 'package:flutter/material.dart';
import 'dart:async';
import '../models/sample_caller.dart';
import '../utils/app_colors.dart';
import '../services/background_call_scheduler.dart';

class BackgroundCallSchedulerWidget extends StatefulWidget {
  final bool hasPermissions;
  final Function(String, Color) onShowSnackBar;
  final Function(String) onAddToEventLog;

  const BackgroundCallSchedulerWidget({
    super.key,
    required this.hasPermissions,
    required this.onShowSnackBar,
    required this.onAddToEventLog,
  });

  @override
  State<BackgroundCallSchedulerWidget> createState() =>
      _BackgroundCallSchedulerWidgetState();
}

class _BackgroundCallSchedulerWidgetState
    extends State<BackgroundCallSchedulerWidget> {
  int _selectedDelay = 10; // Default 10 seconds
  bool _isVideoCall = false;
  bool _isScheduled = false;
  String? _scheduledCallerName;

  final List<int> _delayOptions = [5, 10, 15, 30, 60, 120]; // seconds

  @override
  void initState() {
    super.initState();
    _initializeWorkManager();
  }

  Future<void> _initializeWorkManager() async {
    try {
      await BackgroundCallScheduler.initialize();
      widget.onAddToEventLog('🔧 WorkManager initialized for background calls');
    } catch (e) {
      widget.onAddToEventLog('❌ WorkManager initialization failed: $e');
    }
  }

  Future<void> _scheduleBackgroundCall() async {
    if (!widget.hasPermissions) {
      widget.onShowSnackBar(
        'Permissions required for background calls',
        AppColors.error,
      );
      return;
    }

    try {
      // Select random caller for display
      final caller =
          SampleCaller.sampleCallers[DateTime.now().millisecondsSinceEpoch %
              SampleCaller.sampleCallers.length];

      await BackgroundCallScheduler.scheduleCall(
        delayInSeconds: _selectedDelay,
        isVideoCall: _isVideoCall,
        caller: caller,
      );

      setState(() {
        _isScheduled = true;
        _scheduledCallerName = caller.name;
      });

      widget.onAddToEventLog(
        '⏰ Background call scheduled: ${caller.name} in $_selectedDelay seconds',
      );

      widget.onShowSnackBar(
        'Background call scheduled for ${caller.name} in $_selectedDelay seconds\n'
        'Close the app to test background functionality!',
        AppColors.success,
      );
    } catch (e) {
      widget.onAddToEventLog('❌ Failed to schedule background call: $e');
      widget.onShowSnackBar(
        'Failed to schedule background call: $e',
        AppColors.error,
      );
    }
  }

  Future<void> _cancelScheduledCall() async {
    try {
      await BackgroundCallScheduler.cancelScheduledCall();

      setState(() {
        _isScheduled = false;
        _scheduledCallerName = null;
      });

      widget.onAddToEventLog('🚫 Background call cancelled');
      widget.onShowSnackBar('Background call cancelled', AppColors.warning);
    } catch (e) {
      widget.onAddToEventLog('❌ Failed to cancel background call: $e');
      widget.onShowSnackBar(
        'Failed to cancel background call: $e',
        AppColors.error,
      );
    }
  }

  String _getDelayText(int seconds) {
    if (seconds < 60) {
      return '${seconds}s';
    } else {
      return '${seconds ~/ 60}m';
    }
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.all(16),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(
                  Icons.schedule,
                  color: _isScheduled ? AppColors.success : AppColors.primary,
                ),
                const SizedBox(width: 8),
                Text(
                  AppStrings.backgroundCallSection,
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                if (_isScheduled) ...[
                  const Spacer(),
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 8,
                      vertical: 4,
                    ),
                    decoration: BoxDecoration(
                      color: AppColors.success,
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: const Text(
                      'SCHEDULED',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 10,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ],
              ],
            ),
            const SizedBox(height: 16),
            Text(
              'Schedule calls using WorkManager for true background execution.',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
            const SizedBox(height: 8),
            Text(
              'Works even when the app is completely closed!',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: AppColors.success,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 16),

            // Delay selection
            Row(
              children: [
                const Icon(Icons.timer, size: 20),
                const SizedBox(width: 8),
                const Text('Delay:'),
                const SizedBox(width: 16),
                Expanded(
                  child: DropdownButton<int>(
                    value: _selectedDelay,
                    isExpanded: true,
                    items: _delayOptions.map((delay) {
                      return DropdownMenuItem<int>(
                        value: delay,
                        child: Text(_getDelayText(delay)),
                      );
                    }).toList(),
                    onChanged: _isScheduled
                        ? null
                        : (value) {
                            setState(() {
                              _selectedDelay = value!;
                            });
                          },
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),

            // Call type selection
            Row(
              children: [
                const Icon(Icons.call_split, size: 20),
                const SizedBox(width: 8),
                const Text('Type:'),
                const SizedBox(width: 16),
                Expanded(
                  child: SegmentedButton<bool>(
                    segments: const [
                      ButtonSegment<bool>(
                        value: false,
                        label: Text('Voice'),
                        icon: Icon(Icons.call),
                      ),
                      ButtonSegment<bool>(
                        value: true,
                        label: Text('Video'),
                        icon: Icon(Icons.videocam),
                      ),
                    ],
                    selected: {_isVideoCall},
                    onSelectionChanged: _isScheduled
                        ? null
                        : (Set<bool> newSelection) {
                            setState(() {
                              _isVideoCall = newSelection.first;
                            });
                          },
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),

            // Schedule/Cancel button
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: widget.hasPermissions
                    ? (_isScheduled
                          ? _cancelScheduledCall
                          : _scheduleBackgroundCall)
                    : null,
                icon: Icon(_isScheduled ? Icons.cancel : Icons.schedule_send),
                label: Text(
                  _isScheduled
                      ? 'Cancel Scheduled Call'
                      : 'Schedule Background Call',
                ),
                style: ElevatedButton.styleFrom(
                  backgroundColor: _isScheduled
                      ? AppColors.error
                      : AppColors.primary,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 12),
                ),
              ),
            ),

            if (_isScheduled && _scheduledCallerName != null) ...[
              const SizedBox(height: 12),
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: AppColors.success.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(
                    color: AppColors.success.withValues(alpha: 0.3),
                  ),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(Icons.person, size: 16, color: AppColors.success),
                        const SizedBox(width: 4),
                        Text(
                          'Scheduled for: $_scheduledCallerName',
                          style: TextStyle(
                            color: AppColors.success,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      '${_isVideoCall ? 'Video' : 'Voice'} call in ${_getDelayText(_selectedDelay)}',
                      style: TextStyle(color: AppColors.success, fontSize: 12),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      '💡 Close the app completely to test background execution!',
                      style: TextStyle(
                        color: AppColors.primary,
                        fontSize: 11,
                        fontStyle: FontStyle.italic,
                      ),
                    ),
                  ],
                ),
              ),
            ],

            if (!widget.hasPermissions) ...[
              const SizedBox(height: 12),
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: AppColors.error.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(
                    color: AppColors.error.withValues(alpha: 0.3),
                  ),
                ),
                child: Row(
                  children: [
                    Icon(Icons.warning, color: AppColors.error, size: 16),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        'Permissions required for background calls',
                        style: TextStyle(color: AppColors.error, fontSize: 12),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
