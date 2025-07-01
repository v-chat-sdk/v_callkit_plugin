import 'package:flutter/material.dart';
import '../utils/app_colors.dart';

class PermissionsWidget extends StatelessWidget {
  final bool hasPermissions;
  final VoidCallback onPermissionsRequested;

  const PermissionsWidget({
    super.key,
    required this.hasPermissions,
    required this.onPermissionsRequested,
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
                  Icons.security,
                  color: hasPermissions ? AppColors.success : AppColors.error,
                ),
                const SizedBox(width: 8),
                Text(
                  AppStrings.permissionsSection,
                  style: Theme.of(context).textTheme.titleLarge,
                ),
              ],
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: Text(
                    'VoIP Permissions: ${hasPermissions ? "Granted" : "Not Granted"}',
                    style: TextStyle(
                      color: hasPermissions
                          ? AppColors.success
                          : AppColors.error,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
                if (!hasPermissions)
                  ElevatedButton.icon(
                    onPressed: onPermissionsRequested,
                    icon: const Icon(Icons.settings),
                    label: const Text('Request'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.primary,
                      foregroundColor: Colors.white,
                    ),
                  ),
              ],
            ),
            if (!hasPermissions) ...[
              const SizedBox(height: 8),
              Text(
                'VoIP permissions are required to display native incoming call UI. '
                'On Android, this requires MANAGE_OWN_CALLS permission.',
                style: Theme.of(
                  context,
                ).textTheme.bodySmall?.copyWith(color: Colors.grey[600]),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
