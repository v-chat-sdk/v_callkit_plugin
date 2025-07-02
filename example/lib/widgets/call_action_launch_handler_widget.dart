import 'package:flutter/material.dart';
import 'dart:developer' as developer;

import 'package:v_callkit_plugin/models/call_data.dart';
import '../utils/app_colors.dart';

/// Utility to safely convert dynamic data from native platforms
class _TypeUtils {
  /// Safely converts a map from platform channel to Map with String keys and dynamic values
  static Map<String, dynamic> safeMapConvert(dynamic value) {
    if (value == null) return {};

    if (value is Map<String, dynamic>) {
      return value;
    }

    if (value is Map) {
      final result = <String, dynamic>{};
      value.forEach((key, val) {
        final stringKey = key.toString();
        result[stringKey] = _convertValue(val);
      });
      return result;
    }

    return {};
  }

  /// Recursively converts values to appropriate types
  static dynamic _convertValue(dynamic value) {
    if (value == null) return null;

    if (value is Map) {
      return safeMapConvert(value);
    }

    if (value is List) {
      return value.map((item) => _convertValue(item)).toList();
    }

    return value;
  }
}

/// Widget that detects and handles app launch from call notification actions
class CallActionLaunchHandlerWidget extends StatefulWidget {
  final Function(String action, CallData callData) onCallActionLaunch;
  final Function(String message, Color color) onShowSnackBar;
  final Function(String message) onAddToEventLog;

  const CallActionLaunchHandlerWidget({
    super.key,
    required this.onCallActionLaunch,
    required this.onShowSnackBar,
    required this.onAddToEventLog,
  });

  @override
  State<CallActionLaunchHandlerWidget> createState() =>
      _CallActionLaunchHandlerWidgetState();
}

class _CallActionLaunchHandlerWidgetState
    extends State<CallActionLaunchHandlerWidget> {
  bool _hasCheckedLaunchData = false;
  Map<String, dynamic>? _lastLaunchData;

  @override
  void initState() {
    super.initState();
    // Check for launch data when widget initializes
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _checkForCallActionLaunch();
    });
  }

  Future<void> _checkForCallActionLaunch() async {
    if (_hasCheckedLaunchData) return;

    try {
      // Call action launch detection was removed from minimal API
      developer.log(
        'Call action launch detection not available in minimal API',
        name: 'CallActionLaunch',
      );

      widget.onAddToEventLog(
        '⚠️ Call action launch detection not available in minimal API',
      );

      _hasCheckedLaunchData = true;
    } catch (e) {
      developer.log(
        'Error checking call action launch: $e',
        name: 'CallActionLaunch',
      );
    }
  }

  Future<void> _manualCheck() async {
    setState(() {
      _hasCheckedLaunchData = false;
    });
    await _checkForCallActionLaunch();
  }

  @override
  Widget build(BuildContext context) {
    if (_lastLaunchData == null) {
      return const SizedBox.shrink();
    }

    final action = _lastLaunchData!['action']?.toString() ?? '';
    final callDataMap = _TypeUtils.safeMapConvert(_lastLaunchData!['callData']);
    final callData = CallData.fromMap(callDataMap);
    final timestamp = (_lastLaunchData!['timestamp'] as num?)?.toInt() ?? 0;
    final launchTime = DateTime.fromMillisecondsSinceEpoch(timestamp);

    return Card(
      margin: const EdgeInsets.all(16),
      color: action == 'ANSWER'
          ? AppColors.success.withValues(alpha: 0.1)
          : AppColors.warning.withValues(alpha: 0.1),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(
                  action == 'ANSWER' ? Icons.call : Icons.call_end,
                  color: action == 'ANSWER'
                      ? AppColors.success
                      : AppColors.warning,
                ),
                const SizedBox(width: 8),
                Text(
                  'App Launched from Call Action',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: action == 'ANSWER'
                        ? AppColors.success
                        : AppColors.warning,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: Colors.grey.withValues(alpha: 0.3)),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _buildInfoRow(
                    'Action:',
                    action,
                    color: action == 'ANSWER'
                        ? AppColors.success
                        : AppColors.warning,
                  ),
                  const SizedBox(height: 8),
                  _buildInfoRow('Caller:', callData.callerName),
                  const SizedBox(height: 8),
                  _buildInfoRow('Number:', callData.callerNumber),
                  const SizedBox(height: 8),
                  _buildInfoRow(
                    'Call Type:',
                    callData.isVideoCall ? 'Video' : 'Voice',
                  ),
                  const SizedBox(height: 8),
                  _buildInfoRow(
                    'Launch Time:',
                    '${launchTime.hour.toString().padLeft(2, '0')}:${launchTime.minute.toString().padLeft(2, '0')}:${launchTime.second.toString().padLeft(2, '0')}',
                  ),
                ],
              ),
            ),
            const SizedBox(height: 12),
            const Text(
              '💡 This is where you can:\n'
              '• Make HTTP requests to your server\n'
              '• Join the call in your app\n'
              '• Update user status\n'
              '• Handle call-specific logic',
              style: TextStyle(fontSize: 13, fontStyle: FontStyle.italic),
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: () {
                      setState(() {
                        _lastLaunchData = null;
                      });
                    },
                    icon: const Icon(Icons.clear),
                    label: const Text('Dismiss'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.grey[600],
                      foregroundColor: Colors.white,
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: _manualCheck,
                    icon: const Icon(Icons.refresh),
                    label: const Text('Check Again'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.primary,
                      foregroundColor: Colors.white,
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildInfoRow(String label, String value, {Color? color}) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SizedBox(
          width: 80,
          child: Text(
            label,
            style: const TextStyle(
              fontWeight: FontWeight.w500,
              color: Colors.grey,
            ),
          ),
        ),
        Expanded(
          child: Text(
            value,
            style: TextStyle(
              fontWeight: FontWeight.w500,
              color: color ?? Colors.black87,
            ),
          ),
        ),
      ],
    );
  }
}
