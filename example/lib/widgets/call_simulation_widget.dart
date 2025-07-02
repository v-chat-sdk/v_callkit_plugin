import 'package:flutter/material.dart';
import '../utils/app_colors.dart';

class CallSimulationWidget extends StatelessWidget {
  final bool hasPermissions;
  final VoidCallback onVoiceCallSimulation;
  final VoidCallback onVideoCallSimulation;
  final VoidCallback onTestVibration;

  const CallSimulationWidget({
    super.key,
    required this.hasPermissions,
    required this.onVoiceCallSimulation,
    required this.onVideoCallSimulation,
    required this.onTestVibration,
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
            _buildHeader(context),
            const SizedBox(height: 16),
            _buildDescription(context),
            const SizedBox(height: 16),
            _buildCallButtons(),
            if (!hasPermissions) _buildPermissionWarning(),
            const SizedBox(height: 16),
            _buildVibrationTestButton(),
            const SizedBox(height: 8),
            _buildVibrationDescription(context),
          ],
        ),
      ),
    );
  }

  Widget _buildHeader(BuildContext context) {
    return Row(
      children: [
        const Icon(Icons.call, color: AppColors.primary),
        const SizedBox(width: 8),
        Text(
          AppStrings.callSimulationSection,
          style: Theme.of(context).textTheme.titleLarge,
        ),
      ],
    );
  }

  Widget _buildDescription(BuildContext context) {
    return Text(
      'Simulate incoming calls with random sample callers:',
      style: Theme.of(context).textTheme.bodyMedium,
    );
  }

  Widget _buildCallButtons() {
    return Row(
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
    );
  }

  Widget _buildPermissionWarning() {
    return Padding(
      padding: const EdgeInsets.only(top: 8),
      child: Text(
        'Permissions required to simulate calls',
        style: TextStyle(color: AppColors.error, fontSize: 12),
      ),
    );
  }

  Widget _buildVibrationTestButton() {
    return SizedBox(
      width: double.infinity,
      child: ElevatedButton.icon(
        onPressed: onTestVibration,
        icon: const Icon(Icons.vibration),
        label: const Text('Test Vibration'),
        style: ElevatedButton.styleFrom(
          backgroundColor: Colors.orange,
          foregroundColor: Colors.white,
          padding: const EdgeInsets.all(16),
        ),
      ),
    );
  }

  Widget _buildVibrationDescription(BuildContext context) {
    return Text(
      'Test if vibration works with current settings',
      style: Theme.of(context).textTheme.bodySmall,
      textAlign: TextAlign.center,
    );
  }
}
