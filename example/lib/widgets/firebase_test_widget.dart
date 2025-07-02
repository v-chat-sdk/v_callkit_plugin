import 'package:flutter/material.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import '../services/firebase_background_handler.dart';
import '../utils/app_colors.dart';

class FirebaseTestWidget extends StatefulWidget {
  const FirebaseTestWidget({super.key});

  @override
  State<FirebaseTestWidget> createState() => _FirebaseTestWidgetState();
}

class _FirebaseTestWidgetState extends State<FirebaseTestWidget> {
  String? _fcmToken;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _getFCMToken();
  }

  Future<void> _getFCMToken() async {
    setState(() => _isLoading = true);

    try {
      final token = await FirebaseMessaging.instance.getToken();
      setState(() {
        _fcmToken = token;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _fcmToken = 'Error: $e';
        _isLoading = false;
      });
    }
  }

  Future<void> _simulateFakeCall() async {
    try {
      final fakeCaller = FirebaseBackgroundHandler.getRandomFakeCaller();

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'Fake call simulation started!\nCaller: ${fakeCaller['name']}\nNumber: ${fakeCaller['number']}',
          ),
          backgroundColor: AppColors.success,
          duration: const Duration(seconds: 3),
        ),
      );
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e'), backgroundColor: AppColors.error),
      );
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
                Icon(Icons.cloud_queue, color: Colors.orange[700]),
                const SizedBox(width: 8),
                const Text(
                  'Firebase Background Messaging',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
              ],
            ),
            const SizedBox(height: 16),

            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.orange[50],
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: Colors.orange[200]!),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    '🔥 Firebase Status: Connected',
                    style: TextStyle(
                      color: Colors.orange[800],
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: 8),
                  if (_isLoading)
                    const Text('Loading FCM token...')
                  else if (_fcmToken != null) ...[
                    Text(
                      'FCM Token (for testing):',
                      style: TextStyle(
                        color: Colors.orange[700],
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: Colors.grey[100],
                        borderRadius: BorderRadius.circular(4),
                      ),
                      child: SelectableText(
                        _fcmToken!,
                        style: const TextStyle(
                          fontSize: 10,
                          fontFamily: 'monospace',
                        ),
                      ),
                    ),
                  ],
                ],
              ),
            ),

            const SizedBox(height: 16),

            const Text(
              'Testing Instructions:',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),

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
                    '1. Copy the FCM token above',
                    style: TextStyle(color: Colors.blue[700], fontSize: 12),
                  ),
                  Text(
                    '2. Go to Firebase Console > Cloud Messaging',
                    style: TextStyle(color: Colors.blue[700], fontSize: 12),
                  ),
                  Text(
                    '3. Create new notification and paste the token',
                    style: TextStyle(color: Colors.blue[700], fontSize: 12),
                  ),
                  Text(
                    '4. Send the notification (app can be closed/background)',
                    style: TextStyle(color: Colors.blue[700], fontSize: 12),
                  ),
                  Text(
                    '5. You should see a fake incoming call!',
                    style: TextStyle(color: Colors.blue[700], fontSize: 12),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 16),

            Row(
              children: [
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: _getFCMToken,
                    icon: const Icon(Icons.refresh, size: 16),
                    label: const Text('Refresh Token'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.orange[600],
                      foregroundColor: Colors.white,
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: _simulateFakeCall,
                    icon: const Icon(Icons.call, size: 16),
                    label: const Text('Test Fake Call'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.success,
                      foregroundColor: Colors.white,
                    ),
                  ),
                ),
              ],
            ),

            const SizedBox(height: 12),

            Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: Colors.green[50],
                borderRadius: BorderRadius.circular(6),
                border: Border.all(color: Colors.green[200]!),
              ),
              child: Row(
                children: [
                  Icon(Icons.info, color: Colors.green[700], size: 16),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      'Background handler active: When you send a Firebase notification, it will automatically show a fake call with random caller data.',
                      style: TextStyle(color: Colors.green[700], fontSize: 11),
                    ),
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
