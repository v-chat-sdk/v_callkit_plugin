import 'package:flutter/material.dart';
import 'package:v_callkit_plugin/v_callkit_plugin.dart';

class BatteryOptimizationWidget extends StatefulWidget {
  const BatteryOptimizationWidget({super.key});

  @override
  State<BatteryOptimizationWidget> createState() =>
      _BatteryOptimizationWidgetState();
}

class _BatteryOptimizationWidgetState extends State<BatteryOptimizationWidget> {
  final _plugin = VCallkitPlugin();
  bool _isOptimizationIgnored = true;
  bool _isCustomRom = false;
  String _manufacturer = 'unknown';

  @override
  void initState() {
    super.initState();
    _checkBatteryOptimization();
  }

  Future<void> _checkBatteryOptimization() async {
    try {
      final manufacturer = await _plugin.getDeviceManufacturer();
      final isCustomRom = await _plugin.isCustomChineseRom();
      final isIgnored = await _plugin.checkBatteryOptimization();

      if (mounted) {
        setState(() {
          _manufacturer = manufacturer;
          _isCustomRom = isCustomRom;
          _isOptimizationIgnored = isIgnored;
        });
      }
    } catch (e) {
      debugPrint('Error checking battery optimization: $e');
    }
  }

  Future<void> _requestBatteryOptimization() async {
    try {
      final result = await _plugin.requestBatteryOptimization();
      if (result) {
        // Re-check after user returns from settings
        await Future.delayed(const Duration(seconds: 1));
        await _checkBatteryOptimization();
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (!_isCustomRom) {
      return const SizedBox.shrink();
    }

    return Card(
      color: _isOptimizationIgnored ? Colors.green[50] : Colors.orange[50],
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(
                  _isOptimizationIgnored
                      ? Icons.battery_charging_full
                      : Icons.battery_alert,
                  color: _isOptimizationIgnored ? Colors.green : Colors.orange,
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    'Battery Optimization (${_manufacturer.toUpperCase()})',
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Text(
              _isOptimizationIgnored
                  ? 'Battery optimization is disabled ✓'
                  : 'Battery optimization is enabled - this may prevent incoming calls from ringing!',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
            if (!_isOptimizationIgnored) ...[
              const SizedBox(height: 16),
              Text(
                'Your device (${_manufacturer.toUpperCase()}) has aggressive battery optimization that can prevent call notifications from working properly. Please disable battery optimization for this app.',
                style: Theme.of(
                  context,
                ).textTheme.bodySmall?.copyWith(color: Colors.orange[800]),
              ),
              const SizedBox(height: 16),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton.icon(
                  onPressed: _requestBatteryOptimization,
                  icon: const Icon(Icons.settings),
                  label: const Text('Open Battery Settings'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.orange,
                    foregroundColor: Colors.white,
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
