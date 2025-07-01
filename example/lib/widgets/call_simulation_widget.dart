import 'package:flutter/material.dart';
import '../utils/app_colors.dart';

class CallSimulationWidget extends StatelessWidget {
  final bool hasPermissions;
  final VoidCallback onVoiceCallSimulation;
  final VoidCallback onVideoCallSimulation;

  const CallSimulationWidget({
    super.key,
    required this.hasPermissions,
    required this.onVoiceCallSimulation,
    required this.onVideoCallSimulation,
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
                const Icon(Icons.call, color: AppColors.primary),
                const SizedBox(width: 8),
                Text(
                  AppStrings.callSimulationSection,
                  style: Theme.of(context).textTheme.titleLarge,
                ),
              ],
            ),
            const SizedBox(height: 16),
            Text(
              'Simulate incoming calls with random sample callers:',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
            const SizedBox(height: 16),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: hasPermissions ? onVoiceCallSimulation : null,
                    icon: const Icon(Icons.call),
                    label: const Text('Voice Call'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.primary,
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 12),
                    ),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: hasPermissions ? onVideoCallSimulation : null,
                    icon: const Icon(Icons.videocam),
                    label: const Text('Video Call'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.secondary,
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 12),
                    ),
                  ),
                ),
              ],
            ),
            if (!hasPermissions)
              Padding(
                padding: const EdgeInsets.only(top: 8),
                child: Text(
                  'Permissions required to simulate calls',
                  style: TextStyle(color: AppColors.error, fontSize: 12),
                ),
              ),
          ],
        ),
      ),
    );
  }
}
