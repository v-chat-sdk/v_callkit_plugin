import 'package:flutter/material.dart';
import 'package:v_callkit_plugin/models/call_data.dart';
import '../utils/app_colors.dart';

class CallStatusWidget extends StatelessWidget {
  final bool isCallActive;
  final CallData? activeCallData;
  final String platformVersion;

  const CallStatusWidget({
    super.key,
    required this.isCallActive,
    required this.activeCallData,
    required this.platformVersion,
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
                Icon(
                  Icons.phone,
                  color: isCallActive
                      ? AppColors.activeCall
                      : AppColors.inactiveCall,
                ),
                const SizedBox(width: 8),
                Text(
                  AppStrings.callStatusSection,
                  style: Theme.of(context).textTheme.titleLarge,
                ),
              ],
            ),
            const SizedBox(height: 16),
            _buildStatusRow('Platform', platformVersion),
            _buildStatusRow(
              'Call Status',
              isCallActive ? 'Active' : 'No Active Call',
              isCallActive ? AppColors.activeCall : AppColors.inactiveCall,
            ),
            if (activeCallData != null) ...[
              const SizedBox(height: 8),
              _buildCallDetails(context, activeCallData!),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildStatusRow(String label, String value, [Color? valueColor]) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          SizedBox(
            width: 100,
            child: Text(
              '$label:',
              style: const TextStyle(fontWeight: FontWeight.w500),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: TextStyle(
                color: valueColor,
                fontWeight: valueColor != null ? FontWeight.w500 : null,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCallDetails(BuildContext context, CallData callData) {
    return Container(
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
            'Active Call Details',
            style: Theme.of(
              context,
            ).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 8),
          _buildStatusRow('Caller', callData.callerName),
          _buildStatusRow('Number', callData.callerNumber),
          _buildStatusRow('Type', callData.isVideoCall ? 'Video' : 'Voice'),
          _buildStatusRow('Call ID', callData.id),
        ],
      ),
    );
  }
}
