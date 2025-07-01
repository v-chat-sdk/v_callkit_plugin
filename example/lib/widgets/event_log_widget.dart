import 'package:flutter/material.dart';
import '../utils/app_colors.dart';

class EventLogWidget extends StatelessWidget {
  final List<String> eventLog;
  final VoidCallback onClearLog;

  const EventLogWidget({
    super.key,
    required this.eventLog,
    required this.onClearLog,
  });

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
                const Icon(Icons.list_alt, color: AppColors.info),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    AppStrings.eventLogSection,
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                ),
                TextButton.icon(
                  onPressed: eventLog.isNotEmpty ? onClearLog : null,
                  icon: const Icon(Icons.clear_all, size: 18),
                  label: const Text('Clear'),
                  style: TextButton.styleFrom(foregroundColor: AppColors.error),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Container(
              height: 300,
              decoration: BoxDecoration(
                border: Border.all(color: Colors.grey[300]!),
                borderRadius: BorderRadius.circular(8),
              ),
              child: eventLog.isEmpty
                  ? _buildEmptyState(context)
                  : _buildEventList(),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildEmptyState(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.event_note, size: 48, color: Colors.grey[400]),
          const SizedBox(height: 8),
          Text(
            'No events yet',
            style: Theme.of(
              context,
            ).textTheme.bodyLarge?.copyWith(color: Colors.grey[600]),
          ),
          const SizedBox(height: 4),
          Text(
            'Call events will appear here',
            style: Theme.of(
              context,
            ).textTheme.bodySmall?.copyWith(color: Colors.grey[500]),
          ),
        ],
      ),
    );
  }

  Widget _buildEventList() {
    return ListView.builder(
      padding: const EdgeInsets.all(8),
      itemCount: eventLog.length,
      itemBuilder: (context, index) {
        final event = eventLog[index];
        final parts = event.split(': ');
        final timestamp = parts.isNotEmpty ? parts[0] : '';
        final message = parts.length > 1 ? parts.sublist(1).join(': ') : event;

        return Container(
          margin: const EdgeInsets.only(bottom: 8),
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: _getEventColor(message).withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(6),
            border: Border.all(
              color: _getEventColor(message).withValues(alpha: 0.3),
              width: 1,
            ),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Icon(
                    _getEventIcon(message),
                    size: 16,
                    color: _getEventColor(message),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      message,
                      style: TextStyle(
                        fontWeight: FontWeight.w500,
                        color: _getEventColor(message),
                      ),
                    ),
                  ),
                ],
              ),
              if (timestamp.isNotEmpty) ...[
                const SizedBox(height: 4),
                Text(
                  _formatTimestamp(timestamp),
                  style: TextStyle(fontSize: 11, color: Colors.grey[600]),
                ),
              ],
            ],
          ),
        );
      },
    );
  }

  Color _getEventColor(String message) {
    if (message.contains('✅') || message.contains('answered')) {
      return AppColors.success;
    } else if (message.contains('❌') || message.contains('rejected')) {
      return AppColors.error;
    } else if (message.contains('🔚') || message.contains('ended')) {
      return AppColors.warning;
    } else if (message.contains('⏸️') || message.contains('hold')) {
      return Colors.blue;
    } else if (message.contains('🔇') || message.contains('mute')) {
      return Colors.purple;
    }
    return AppColors.info;
  }

  IconData _getEventIcon(String message) {
    if (message.contains('✅') || message.contains('answered')) {
      return Icons.call;
    } else if (message.contains('❌') || message.contains('rejected')) {
      return Icons.call_end;
    } else if (message.contains('🔚') || message.contains('ended')) {
      return Icons.call_end;
    } else if (message.contains('⏸️') || message.contains('hold')) {
      return Icons.pause;
    } else if (message.contains('🔇') || message.contains('mute')) {
      return Icons.volume_off;
    }
    return Icons.event;
  }

  String _formatTimestamp(String timestamp) {
    try {
      final dateTime = DateTime.parse(timestamp);
      return '${dateTime.hour.toString().padLeft(2, '0')}:'
          '${dateTime.minute.toString().padLeft(2, '0')}:'
          '${dateTime.second.toString().padLeft(2, '0')}';
    } catch (e) {
      return timestamp;
    }
  }
}
