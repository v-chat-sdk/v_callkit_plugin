// This is a basic Flutter integration test.
//
// Since integration tests run in a full Flutter application, they can interact
// with the host side of a plugin implementation, unlike Dart unit tests.
//
// For more information about Flutter integration tests, please see
// https://flutter.dev/to/integration-testing

import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';

import 'package:v_callkit_plugin/v_callkit_plugin.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('hasPermissions test', (WidgetTester tester) async {
    final VCallkitPlugin plugin = VCallkitPlugin();
    plugin.initialize();
    final bool hasPermissions = await plugin.hasPermissions();
    // hasPermissions should return a boolean value
    expect(hasPermissions, isA<bool>());
  });
}
