import 'package:flutter/material.dart';
import 'package:v_callkit_plugin/v_callkit_plugin.dart';
import '../utils/app_colors.dart';

class RingtoneSelectionWidget extends StatefulWidget {
  const RingtoneSelectionWidget({super.key});

  @override
  State<RingtoneSelectionWidget> createState() =>
      _RingtoneSelectionWidgetState();
}

class _RingtoneSelectionWidgetState extends State<RingtoneSelectionWidget> {
  final _vCallkitPlugin = VCallkitPlugin();
  List<Map<String, dynamic>> _ringtones = [];
  String? _selectedRingtoneUri;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _loadSystemRingtones();
  }

  Future<void> _loadSystemRingtones() async {
    setState(() {
      _isLoading = true;
    });

    try {
      final ringtones = await _vCallkitPlugin.getSystemRingtones();
      setState(() {
        _ringtones = ringtones;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _isLoading = false;
      });
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error loading ringtones: $e'),
            backgroundColor: AppColors.error,
          ),
        );
      }
    }
  }

  Future<void> _selectRingtone(String? uri) async {
    try {
      final success = await _vCallkitPlugin.setCustomRingtone(uri);
      if (success) {
        setState(() {
          _selectedRingtoneUri = uri;
        });
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(
                'Ringtone ${uri == null ? 'reset to default' : 'changed successfully'}',
              ),
              backgroundColor: AppColors.success,
            ),
          );
        }
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error setting ringtone: $e'),
            backgroundColor: AppColors.error,
          ),
        );
      }
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
                const Icon(Icons.music_note, color: AppColors.primary),
                const SizedBox(width: 8),
                Text(
                  'Call Ringtone',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
              ],
            ),
            const SizedBox(height: 16),
            Text(
              'Choose a ringtone for incoming VoIP calls:',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
            const SizedBox(height: 16),
            if (_isLoading)
              const Center(
                child: CircularProgressIndicator(color: AppColors.primary),
              )
            else
              Container(
                height: 200,
                decoration: BoxDecoration(
                  border: Border.all(color: Colors.grey.shade300),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: ListView.builder(
                  itemCount: _ringtones.length,
                  itemBuilder: (context, index) {
                    final ringtone = _ringtones[index];
                    final title = ringtone['title'] as String;
                    final uri = ringtone['uri'] as String;
                    final isDefault = ringtone['isDefault'] == 'true';
                    final isSelected =
                        _selectedRingtoneUri == uri ||
                        (_selectedRingtoneUri == null && isDefault);

                    return ListTile(
                      title: Text(title),
                      subtitle: isDefault ? const Text('System Default') : null,
                      leading: Radio<String>(
                        value: uri,
                        groupValue: _selectedRingtoneUri,
                        onChanged: (value) => _selectRingtone(value),
                        activeColor: AppColors.primary,
                      ),
                      trailing: isDefault
                          ? const Icon(
                              Icons.star,
                              color: AppColors.warning,
                              size: 20,
                            )
                          : null,
                      selected: isSelected,
                      selectedTileColor: AppColors.primary.withValues(
                        alpha: 0.1,
                      ),
                      onTap: () => _selectRingtone(uri),
                    );
                  },
                ),
              ),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: _loadSystemRingtones,
                    icon: const Icon(Icons.refresh),
                    label: const Text('Refresh'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.secondary,
                      foregroundColor: Colors.white,
                    ),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: () => _selectRingtone(null),
                    icon: const Icon(Icons.restore),
                    label: const Text('Reset Default'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.warning,
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
}
