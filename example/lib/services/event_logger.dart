import 'dart:collection';

/// Service class responsible for managing event logs
class EventLogger {
  static const int _maxLogEntries = 50;

  final List<String> _events = [];

  /// Get all events as an unmodifiable list
  UnmodifiableListView<String> get events => UnmodifiableListView(_events);

  /// Check if there are any events
  bool get hasEvents => _events.isNotEmpty;

  /// Add a new event to the log
  void addEvent(String message) {
    final timestamp = DateTime.now().toIso8601String();
    _events.insert(0, '$timestamp: $message');

    // Keep only the most recent entries
    if (_events.length > _maxLogEntries) {
      _events.removeRange(_maxLogEntries, _events.length);
    }
  }

  /// Clear all events
  void clearEvents() {
    _events.clear();
  }

  /// Add a call event with specific formatting
  void addCallEvent(String action, String callId) {
    addEvent('📞 Call Event: $action for $callId');
  }

  /// Add a call state change event
  void addCallStateEvent(String callId, String state) {
    addEvent('🔄 Call State: $callId -> $state');
  }

  /// Add a success event
  void addSuccessEvent(String message) {
    addEvent('✅ $message');
  }

  /// Add an error event
  void addErrorEvent(String message) {
    addEvent('❌ $message');
  }

  /// Add an info event
  void addInfoEvent(String message) {
    addEvent('🔥 $message');
  }
}
