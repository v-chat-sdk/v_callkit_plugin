import 'package:flutter/material.dart';
import 'package:v_callkit_plugin/v_callkit_plugin.dart';
import '../models/sample_caller.dart';
import '../widgets/error_dialog_widget.dart';

class CustomizationDemoScreen extends StatefulWidget {
  const CustomizationDemoScreen({super.key});

  @override
  State<CustomizationDemoScreen> createState() =>
      _CustomizationDemoScreenState();
}

class _CustomizationDemoScreenState extends State<CustomizationDemoScreen> {
  String _selectedTheme = 'Dark Green';
  String _selectedLanguage = 'English';
  bool _showCallerNumber = true;
  bool _enableVibration = true;
  bool _enableRingtone = true;
  bool _showCallDuration = true;
  int _callTimeoutSeconds = 60;

  // Create plugin instance
  final _vCallkitPlugin = VCallkitPlugin();

  // Predefined themes
  final Map<String, Map<String, dynamic>> _themes = {
    'Dark Green': {
      'backgroundColor': '#1C1C1E',
      'accentColor': '#34C759',
      'textColor': '#FFFFFF',
      'secondaryTextColor': '#8E8E93',
    },
    'Light Blue': {
      'backgroundColor': '#F2F2F7',
      'accentColor': '#007AFF',
      'textColor': '#000000',
      'secondaryTextColor': '#6D6D70',
    },
    'Purple': {
      'backgroundColor': '#1D1B20',
      'accentColor': '#D0BCFF',
      'textColor': '#E6E1E5',
      'secondaryTextColor': '#CAC4D0',
    },
    'Orange': {
      'backgroundColor': '#1C1B1F',
      'accentColor': '#FFB4AB',
      'textColor': '#E6E1E5',
      'secondaryTextColor': '#CAC4D0',
    },
  };

  // Predefined languages/translations
  final Map<String, Map<String, String>> _languages = {
    'English': {
      'answerButtonText': 'Answer',
      'declineButtonText': 'Decline',
      'hangupButtonText': 'Hangup',
      'incomingVoiceCallText': 'Incoming Voice Call',
      'incomingVideoCallText': 'Incoming Video Call',
      'callInProgressText': 'Call in Progress',
      'tapToReturnText': 'Tap to return to call',
      'incomingCallLabel': 'Incoming Call',
      'unknownCallerText': 'Unknown',
      'voiceCallText': 'Voice',
      'videoCallText': 'Video',
      'ongoingCallText': 'call',
    },
    'Spanish': {
      'answerButtonText': 'Contestar',
      'declineButtonText': 'Rechazar',
      'hangupButtonText': 'Colgar',
      'incomingVoiceCallText': 'Llamada de Voz Entrante',
      'incomingVideoCallText': 'Videollamada Entrante',
      'callInProgressText': 'Llamada en Progreso',
      'tapToReturnText': 'Toca para volver a la llamada',
      'incomingCallLabel': 'Llamada Entrante',
      'unknownCallerText': 'Desconocido',
      'voiceCallText': 'Voz',
      'videoCallText': 'Video',
      'ongoingCallText': 'llamada',
    },
    'French': {
      'answerButtonText': 'Répondre',
      'declineButtonText': 'Refuser',
      'hangupButtonText': 'Raccrocher',
      'incomingVoiceCallText': 'Appel Vocal Entrant',
      'incomingVideoCallText': 'Appel Vidéo Entrant',
      'callInProgressText': 'Appel en Cours',
      'tapToReturnText': 'Appuyez pour revenir à l\'appel',
      'incomingCallLabel': 'Appel Entrant',
      'unknownCallerText': 'Inconnu',
      'voiceCallText': 'Voix',
      'videoCallText': 'Vidéo',
      'ongoingCallText': 'appel',
    },
    'Arabic': {
      'answerButtonText': 'إجابة',
      'declineButtonText': 'رفض',
      'hangupButtonText': 'إنهاء',
      'incomingVoiceCallText': 'مكالمة صوتية واردة',
      'incomingVideoCallText': 'مكالمة فيديو واردة',
      'callInProgressText': 'مكالمة جارية',
      'tapToReturnText': 'اضغط للعودة إلى المكالمة',
      'incomingCallLabel': 'مكالمة واردة',
      'unknownCallerText': 'غير معروف',
      'voiceCallText': 'صوت',
      'videoCallText': 'فيديو',
      'ongoingCallText': 'مكالمة',
    },
  };

  @override
  void initState() {
    super.initState();
    // Initialize the plugin
    _vCallkitPlugin.initialize();
  }

  @override
  void dispose() {
    _vCallkitPlugin.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[50],
      appBar: AppBar(
        title: const Text(
          'CallKit Customization Demo',
          style: TextStyle(fontWeight: FontWeight.w600, fontSize: 20),
        ),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black87,
        elevation: 0,
        centerTitle: true,
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(1),
          child: Container(height: 1, color: Colors.grey[200]),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header section
            _buildHeaderSection(),

            const SizedBox(height: 24),

            // Theme selection
            _buildSectionCard(
              title: '🎨 Visual Theme',
              child: _buildThemeSelector(),
            ),

            const SizedBox(height: 16),

            // Language selection
            _buildSectionCard(
              title: '🌍 Language / Translation',
              child: _buildLanguageSelector(),
            ),

            const SizedBox(height: 16),

            // Call settings
            _buildSectionCard(
              title: '⚙️ Call Settings',
              child: _buildCallSettings(),
            ),

            const SizedBox(height: 16),

            // Audio settings
            _buildSectionCard(
              title: '🔊 Audio & Vibration',
              child: _buildAudioSettings(),
            ),

            const SizedBox(height: 24),

            // Action buttons
            _buildActionButtons(),

            const SizedBox(height: 16),

            // Info section
            _buildInfoSection(),
          ],
        ),
      ),
    );
  }

  Widget _buildHeaderSection() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [Colors.blue[400]!, Colors.blue[600]!],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Icon(Icons.tune, color: Colors.white, size: 32),
          const SizedBox(height: 12),
          const Text(
            'Customize Your Call Experience',
            style: TextStyle(
              color: Colors.white,
              fontSize: 20,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Configure themes, languages, and call behavior to match your app\'s design and support multiple languages.',
            style: TextStyle(
              color: Colors.white.withValues(alpha: 0.9),
              fontSize: 14,
              height: 1.4,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSectionCard({required String title, required Widget child}) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withValues(alpha: 0.1),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 20, 20, 16),
            child: Text(
              title,
              style: const TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w600,
                color: Colors.black87,
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 0, 20, 20),
            child: child,
          ),
        ],
      ),
    );
  }

  Widget _buildThemeSelector() {
    return Column(
      children: [
        GridView.count(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          crossAxisCount: 2,
          mainAxisSpacing: 12,
          crossAxisSpacing: 12,
          childAspectRatio: 3,
          children: _themes.entries.map((entry) {
            final isSelected = _selectedTheme == entry.key;
            final theme = entry.value;

            return GestureDetector(
              onTap: () => setState(() => _selectedTheme = entry.key),
              child: Container(
                decoration: BoxDecoration(
                  color: Color(
                    int.parse(
                      theme['backgroundColor']!.replaceFirst('#', '0xff'),
                    ),
                  ),
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(
                    color: isSelected ? Colors.blue : Colors.transparent,
                    width: 2,
                  ),
                ),
                child: Row(
                  children: [
                    Container(
                      width: 8,
                      height: double.infinity,
                      decoration: BoxDecoration(
                        color: Color(
                          int.parse(
                            theme['accentColor']!.replaceFirst('#', '0xff'),
                          ),
                        ),
                        borderRadius: const BorderRadius.only(
                          topLeft: Radius.circular(6),
                          bottomLeft: Radius.circular(6),
                        ),
                      ),
                    ),
                    Expanded(
                      child: Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 8),
                        child: Text(
                          entry.key,
                          style: TextStyle(
                            color: Color(
                              int.parse(
                                theme['textColor']!.replaceFirst('#', '0xff'),
                              ),
                            ),
                            fontSize: 12,
                            fontWeight: FontWeight.w500,
                          ),
                          textAlign: TextAlign.center,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            );
          }).toList(),
        ),
      ],
    );
  }

  Widget _buildLanguageSelector() {
    return DropdownButtonFormField<String>(
      value: _selectedLanguage,
      decoration: InputDecoration(
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: BorderSide(color: Colors.grey[300]!),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: BorderSide(color: Colors.grey[300]!),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: Colors.blue),
        ),
        contentPadding: const EdgeInsets.symmetric(
          horizontal: 16,
          vertical: 12,
        ),
      ),
      items: _languages.keys.map((String language) {
        return DropdownMenuItem<String>(
          value: language,
          child: Row(
            children: [
              Text(_getLanguageFlag(language)),
              const SizedBox(width: 8),
              Text(language),
            ],
          ),
        );
      }).toList(),
      onChanged: (String? newValue) {
        if (newValue != null) {
          setState(() => _selectedLanguage = newValue);
        }
      },
    );
  }

  String _getLanguageFlag(String language) {
    switch (language) {
      case 'English':
        return '🇺🇸';
      case 'Spanish':
        return '🇪🇸';
      case 'French':
        return '🇫🇷';
      case 'Arabic':
        return '🇸🇦';
      default:
        return '🌍';
    }
  }

  Widget _buildCallSettings() {
    return Column(
      children: [
        _buildSwitchTile(
          title: 'Show Caller Number',
          subtitle: 'Display phone number in call UI',
          value: _showCallerNumber,
          onChanged: (value) => setState(() => _showCallerNumber = value),
        ),
        const Divider(height: 24),
        _buildSwitchTile(
          title: 'Show Call Duration',
          subtitle: 'Display timer in hangup notification',
          value: _showCallDuration,
          onChanged: (value) => setState(() => _showCallDuration = value),
        ),
        const Divider(height: 24),
        _buildSliderTile(
          title: 'Call Timeout',
          subtitle: 'Auto-dismiss incoming call after ${_callTimeoutSeconds}s',
          value: _callTimeoutSeconds.toDouble(),
          min: 30,
          max: 120,
          divisions: 9,
          onChanged: (value) =>
              setState(() => _callTimeoutSeconds = value.round()),
        ),
      ],
    );
  }

  Widget _buildAudioSettings() {
    return Column(
      children: [
        _buildSwitchTile(
          title: 'Enable Vibration',
          subtitle: 'Vibrate on incoming calls',
          value: _enableVibration,
          onChanged: (value) => setState(() => _enableVibration = value),
        ),
        const Divider(height: 24),
        _buildSwitchTile(
          title: 'Enable Ringtone',
          subtitle: 'Play sound for incoming calls',
          value: _enableRingtone,
          onChanged: (value) => setState(() => _enableRingtone = value),
        ),
      ],
    );
  }

  Widget _buildSwitchTile({
    required String title,
    required String subtitle,
    required bool value,
    required ValueChanged<bool> onChanged,
  }) {
    return Row(
      children: [
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                title,
                style: const TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w500,
                  color: Colors.black87,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                subtitle,
                style: TextStyle(fontSize: 12, color: Colors.grey[600]),
              ),
            ],
          ),
        ),
        Switch(value: value, onChanged: onChanged, activeColor: Colors.blue),
      ],
    );
  }

  Widget _buildSliderTile({
    required String title,
    required String subtitle,
    required double value,
    required double min,
    required double max,
    required int divisions,
    required ValueChanged<double> onChanged,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          title,
          style: const TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w500,
            color: Colors.black87,
          ),
        ),
        const SizedBox(height: 4),
        Text(subtitle, style: TextStyle(fontSize: 12, color: Colors.grey[600])),
        const SizedBox(height: 8),
        Slider(
          value: value,
          min: min,
          max: max,
          divisions: divisions,
          onChanged: onChanged,
          activeColor: Colors.blue,
        ),
      ],
    );
  }

  Widget _buildActionButtons() {
    return Column(
      children: [
        Row(
          children: [
            Expanded(
              child: _buildActionButton(
                label: 'Voice Call Demo',
                icon: Icons.call,
                color: Colors.green,
                onPressed: () => _showCall(false),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: _buildActionButton(
                label: 'Video Call Demo',
                icon: Icons.videocam,
                color: Colors.blue,
                onPressed: () => _showCall(true),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        SizedBox(
          width: double.infinity,
          child: _buildActionButton(
            label: 'Apply Global Configuration',
            icon: Icons.settings,
            color: Colors.orange,
            onPressed: _applyGlobalConfiguration,
          ),
        ),
        const SizedBox(height: 12),
        Row(
          children: [
            Expanded(
              child: _buildActionButton(
                label: 'Test Hangup Notification',
                icon: Icons.notifications,
                color: Colors.purple,
                onPressed: _testHangupNotification,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: _buildActionButton(
                label: 'Debug Info',
                icon: Icons.info,
                color: Colors.grey,
                onPressed: _showDebugInfo,
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildActionButton({
    required String label,
    required IconData icon,
    required Color color,
    required VoidCallback onPressed,
  }) {
    return ElevatedButton.icon(
      onPressed: onPressed,
      icon: Icon(icon, size: 18),
      label: Text(
        label,
        style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w500),
      ),
      style: ElevatedButton.styleFrom(
        backgroundColor: color,
        foregroundColor: Colors.white,
        elevation: 2,
        padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 16),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
    );
  }

  Widget _buildInfoSection() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.blue[50],
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.blue[200]!),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.info_outline, color: Colors.blue[700], size: 20),
              const SizedBox(width: 8),
              Text(
                'Enhanced Features',
                style: TextStyle(
                  color: Colors.blue[700],
                  fontWeight: FontWeight.w600,
                  fontSize: 14,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Text(
            '• 🎨 Themes change colors and visual appearance\n'
            '• 🌍 Languages translate all UI text elements\n'
            '• ⚙️ Settings control call behavior and features\n'
            '• ⏱️ Real-time duration counter in hangup notifications\n'
            '• 🔔 Non-dismissible notifications on all Android versions\n'
            '• 📱 Automatic duration tracking (MM:SS or HH:MM:SS format)\n'
            '• 👤 User avatars displayed in notifications (with initials fallback)\n'
            '• 🖼️ Automatic circular avatar cropping and loading from URLs\n'
            '• ✅ Works reliably on Android API 21+ (5.0+)',
            style: TextStyle(
              color: Colors.blue[700],
              fontSize: 12,
              height: 1.4,
            ),
          ),
          const SizedBox(height: 12),
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Colors.green[50],
              borderRadius: BorderRadius.circular(8),
              border: Border.all(color: Colors.green[200]!),
            ),
            child: Row(
              children: [
                Icon(Icons.timer, color: Colors.green[700], size: 16),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    'Avatar Display: Shows caller profile picture in notification with clean timer underneath. Falls back to colored initials if no avatar URL provided.',
                    style: TextStyle(
                      color: Colors.green[700],
                      fontSize: 11,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Map<String, dynamic> _getCurrentConfiguration() {
    final theme = _themes[_selectedTheme]!;
    final language = _languages[_selectedLanguage]!;

    return {
      // Visual customization
      ...theme,

      // Text translations
      ...language,

      // Call settings
      'showCallerNumber': _showCallerNumber,
      'showCallDuration': _showCallDuration,
      'callTimeoutSeconds': _callTimeoutSeconds,

      // Audio settings
      'enableVibration': _enableVibration,
      'enableRingtone': _enableRingtone,

      // Additional settings
      'enableCallTimeout': true,
      'useFullScreenCallUI': true,
      'showCallType': true,
      'enableTapToReturnToCall': true,
      'hangupNotificationPriority': 0,
      'use24HourFormat': true,
      'durationFormat': 'mm:ss',

      // Accessibility
      'answerButtonContentDescription': 'Answer call button',
      'declineButtonContentDescription': 'Decline call button',
      'hangupButtonContentDescription': 'Hangup call button',
    };
  }

  void _showCall(bool isVideoCall) async {
    try {
      final config = _getCurrentConfiguration();
      final sampleCaller = SampleCaller.getRandom();

      await _vCallkitPlugin.showIncomingCallWithConfig({
        'callData': {
          'id': DateTime.now().millisecondsSinceEpoch.toString(),
          'callerName': sampleCaller.name,
          'callerNumber': sampleCaller.phoneNumber,
          'isVideoCall': isVideoCall,
        },
        'config': config,
      });

      // Check if widget is still mounted before using context
      if (!mounted) return;

      // Show success message
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            '${isVideoCall ? 'Video' : 'Voice'} call demo started with $_selectedTheme theme and $_selectedLanguage language',
          ),
          backgroundColor: Colors.green,
          duration: const Duration(seconds: 3),
        ),
      );
    } catch (error) {
      if (!mounted) return;
      ErrorDialogWidget.show(context, 'Failed to show call', error.toString());
    }
  }

  void _applyGlobalConfiguration() async {
    try {
      final config = _getCurrentConfiguration();
      await _vCallkitPlugin.setUIConfiguration(config);

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'Global configuration applied: $_selectedTheme theme, $_selectedLanguage language',
          ),
          backgroundColor: Colors.green,
          duration: const Duration(seconds: 3),
        ),
      );
    } catch (error) {
      if (!mounted) return;
      ErrorDialogWidget.show(
        context,
        'Failed to apply configuration',
        error.toString(),
      );
    }
  }

  void _testHangupNotification() async {
    try {
      final config = _getCurrentConfiguration();
      final sampleCaller = SampleCaller.getRandom();

      await _vCallkitPlugin.forceShowHangupNotification({
        'callData': {
          'id': 'test_hangup_${DateTime.now().millisecondsSinceEpoch}',
          'callerName': sampleCaller.name,
          'callerNumber': sampleCaller.phoneNumber,
          'isVideoCall': false,
        },
        'config': config,
      });

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text(
            'Hangup notification shown! Check your notification panel - it should be non-dismissible.',
          ),
          backgroundColor: Colors.purple,
          duration: Duration(seconds: 4),
        ),
      );
    } catch (error) {
      if (!mounted) return;
      ErrorDialogWidget.show(
        context,
        'Failed to show hangup notification',
        error.toString(),
      );
    }
  }

  void _showDebugInfo() async {
    try {
      final debugInfo = await _vCallkitPlugin.getCallManagerDebugInfo();

      if (!mounted) return;

      showDialog(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text('Debug Information'),
          content: SingleChildScrollView(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                Text('Android Version: API ${debugInfo['androidVersion']}'),
                const SizedBox(height: 8),
                Text('Call Active: ${debugInfo['hasActiveCall']}'),
                const SizedBox(height: 8),
                Text(
                  'Supports Foreground Service: ${debugInfo['hasCallForegroundService']}',
                ),
                const SizedBox(height: 8),
                const Text(
                  'Current Configuration:',
                  style: TextStyle(fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 4),
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: Colors.grey[100],
                    borderRadius: BorderRadius.circular(4),
                  ),
                  child: Text(
                    'Theme: $_selectedTheme\n'
                    'Language: $_selectedLanguage\n'
                    'Show Number: $_showCallerNumber\n'
                    'Vibration: $_enableVibration\n'
                    'Ringtone: $_enableRingtone\n'
                    'Call Duration: $_showCallDuration\n'
                    'Timeout: ${_callTimeoutSeconds}s',
                    style: const TextStyle(
                      fontSize: 12,
                      fontFamily: 'monospace',
                    ),
                  ),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('Close'),
            ),
          ],
        ),
      );
    } catch (error) {
      if (!mounted) return;
      ErrorDialogWidget.show(
        context,
        'Failed to get debug info',
        error.toString(),
      );
    }
  }
}
