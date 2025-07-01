import 'package:flutter/material.dart';
import 'package:v_callkit_plugin/v_callkit_plugin.dart';
import 'package:v_callkit_plugin/models/call_data.dart';

class OutgoingCallDemoWidget extends StatefulWidget {
  const OutgoingCallDemoWidget({super.key});

  @override
  State<OutgoingCallDemoWidget> createState() => _OutgoingCallDemoWidgetState();
}

class _OutgoingCallDemoWidgetState extends State<OutgoingCallDemoWidget> {
  final VCallkitPlugin _plugin = VCallkitPlugin();
  bool _isOutgoingCallActive = false;
  bool _isLoading = false;
  String _statusMessage = '';

  // Sample outgoing call data
  final CallData _outgoingCallData = CallData(
    id: 'outgoing_call_${DateTime.now().millisecondsSinceEpoch}',
    callerName: 'John Smith',
    callerNumber: '+1 (555) 123-4567',
    callerAvatar: null,
    isVideoCall: false,
    extra: {
      'callDirection': 'outgoing',
      'timestamp': DateTime.now().toIso8601String(),
    },
  );

  @override
  void initState() {
    super.initState();
    _plugin.initialize();
    _statusMessage = 'Ready to demonstrate outgoing call notifications';
  }

  @override
  void dispose() {
    // Clean up when widget is disposed
    if (_isOutgoingCallActive) {
      _stopOutgoingCall();
    }
    super.dispose();
  }

  Future<void> _startOutgoingCall() async {
    setState(() {
      _isLoading = true;
      _statusMessage = 'Starting outgoing call notification...';
    });

    try {
      // Start the outgoing call notification
      final success = await _plugin.startOutgoingCallNotification(
        _outgoingCallData,
      );

      if (success) {
        setState(() {
          _isOutgoingCallActive = true;
          _statusMessage =
              'Outgoing call notification active! Check your notification panel.';
        });

        _showSnackBar('Outgoing call notification started', Colors.green);
      } else {
        setState(() {
          _statusMessage = 'Failed to start outgoing call notification';
        });
        _showSnackBar('Failed to start notification', Colors.red);
      }
    } catch (e) {
      setState(() {
        _statusMessage = 'Error: $e';
      });
      _showSnackBar('Error: $e', Colors.red);
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  Future<void> _stopOutgoingCall() async {
    setState(() {
      _isLoading = true;
      _statusMessage = 'Stopping outgoing call notification...';
    });

    try {
      final success = await _plugin.stopCallForegroundService();

      if (success) {
        setState(() {
          _isOutgoingCallActive = false;
          _statusMessage = 'Outgoing call notification stopped';
        });

        _showSnackBar('Outgoing call notification stopped', Colors.orange);
      } else {
        setState(() {
          _statusMessage = 'Failed to stop outgoing call notification';
        });
        _showSnackBar('Failed to stop notification', Colors.red);
      }
    } catch (e) {
      setState(() {
        _statusMessage = 'Error: $e';
      });
      _showSnackBar('Error: $e', Colors.red);
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  void _showSnackBar(String message, Color color) {
    if (!mounted) return;

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: color,
        duration: const Duration(seconds: 3),
        behavior: SnackBarBehavior.floating,
        margin: const EdgeInsets.all(16),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
    );
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
                Icon(Icons.call_made, color: Colors.green[600], size: 24),
                const SizedBox(width: 8),
                const Text(
                  'Outgoing Call Notifications',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Text(
              'Simulate persistent notifications for outgoing calls using CallForegroundService',
              style: TextStyle(fontSize: 14, color: Colors.grey[600]),
            ),
            const SizedBox(height: 16),

            // Call Info Card
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.grey[50],
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: Colors.grey[300]!),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Sample Outgoing Call:',
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                      color: Colors.grey[700],
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text('📞 ${_outgoingCallData.callerName}'),
                  Text('📱 ${_outgoingCallData.callerNumber}'),
                  Text(
                    '🎥 ${_outgoingCallData.isVideoCall ? 'Video Call' : 'Voice Call'}',
                  ),
                ],
              ),
            ),

            const SizedBox(height: 16),

            // Status
            Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: _isOutgoingCallActive
                    ? Colors.green[50]
                    : Colors.grey[50],
                borderRadius: BorderRadius.circular(6),
                border: Border.all(
                  color: _isOutgoingCallActive
                      ? Colors.green[200]!
                      : Colors.grey[300]!,
                ),
              ),
              child: Row(
                children: [
                  Icon(
                    _isOutgoingCallActive ? Icons.check_circle : Icons.info,
                    size: 16,
                    color: _isOutgoingCallActive
                        ? Colors.green[600]
                        : Colors.grey[600],
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      _statusMessage,
                      style: TextStyle(
                        fontSize: 12,
                        color: _isOutgoingCallActive
                            ? Colors.green[700]
                            : Colors.grey[700],
                      ),
                    ),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 16),

            // Action Buttons
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                ElevatedButton.icon(
                  onPressed: _isLoading || _isOutgoingCallActive
                      ? null
                      : _startOutgoingCall,
                  icon: _isLoading
                      ? const SizedBox(
                          width: 16,
                          height: 16,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Icon(Icons.call_made, size: 16),
                  label: const Text('Start Outgoing Call'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.green[600],
                    foregroundColor: Colors.white,
                  ),
                ),
                ElevatedButton.icon(
                  onPressed: _isLoading || !_isOutgoingCallActive
                      ? null
                      : _stopOutgoingCall,
                  icon: const Icon(Icons.call_end, size: 16),
                  label: const Text('End Call'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.red[600],
                    foregroundColor: Colors.white,
                  ),
                ),
              ],
            ),

            const SizedBox(height: 16),

            // Instructions
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.blue[50],
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: Colors.blue[200]!),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'How to use:',
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                      color: Colors.blue[800],
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '1. Tap "Start Outgoing Call" to create a persistent notification\n'
                    '2. Check your notification panel - it should be non-dismissible\n'
                    '3. Try backgrounding the app - notification persists\n'
                    '4. Tap "End Call" to remove the notification',
                    style: TextStyle(fontSize: 11, color: Colors.blue[700]),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
