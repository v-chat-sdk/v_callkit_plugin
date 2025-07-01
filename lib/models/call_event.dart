/// Represents the state of a call
enum CallState {
  initializing,
  newCall,
  ringing,
  dialing,
  active,
  holding,
  disconnected,
  unknown,
}

/// Represents different types of call actions (simplified - removed hold and mute)
enum CallAction {
  answered,
  rejected,
  ended,
  aborted,
  stateChanged,
}

/// Base class for all call events
abstract class CallEvent {
  /// The ID of the call this event relates to
  final String callId;

  /// The action that triggered this event
  final CallAction action;

  /// The timestamp when this event occurred
  final DateTime timestamp;

  /// Additional data related to this event
  final Map<String, dynamic> data;

  const CallEvent({
    required this.callId,
    required this.action,
    required this.timestamp,
    this.data = const {},
  });

  /// Converts the event to a Map
  Map<String, dynamic> toMap();

  /// Creates a CallEvent from a Map
  factory CallEvent.fromMap(Map<String, dynamic> map) {
    final actionString = map['action'] as String? ?? '';
    final action = _parseCallAction(actionString);

    // Create specific event types based on action
    switch (action) {
      case CallAction.answered:
        return CallAnsweredEvent.fromMap(map);
      case CallAction.rejected:
        return CallRejectedEvent.fromMap(map);
      case CallAction.ended:
      case CallAction.aborted:
        return CallEndedEvent.fromMap(map);
      case CallAction.stateChanged:
        return CallStateChangedEvent.fromMap(map);
    }
  }

  /// Parses call action from string
  static CallAction _parseCallAction(String actionString) {
    switch (actionString.toLowerCase()) {
      case 'answered':
        return CallAction.answered;
      case 'rejected':
        return CallAction.rejected;
      case 'ended':
        return CallAction.ended;
      case 'aborted':
        return CallAction.aborted;
      case 'state_changed':
        return CallAction.stateChanged;
      default:
        return CallAction.stateChanged;
    }
  }

  /// Converts call action to string
  static String _callActionToString(CallAction action) {
    switch (action) {
      case CallAction.answered:
        return 'answered';
      case CallAction.rejected:
        return 'rejected';
      case CallAction.ended:
        return 'ended';
      case CallAction.aborted:
        return 'aborted';
      case CallAction.stateChanged:
        return 'state_changed';
    }
  }
}

/// Event fired when a call is answered
class CallAnsweredEvent extends CallEvent {
  /// Video state if this is a video call
  final int? videoState;

  const CallAnsweredEvent({
    required super.callId,
    required super.timestamp,
    super.data = const {},
    this.videoState,
  }) : super(action: CallAction.answered);

  factory CallAnsweredEvent.fromMap(Map<String, dynamic> map) {
    return CallAnsweredEvent(
      callId: map['callId'] as String? ?? '',
      timestamp: DateTime.fromMillisecondsSinceEpoch(
        map['timestamp'] as int? ?? DateTime.now().millisecondsSinceEpoch,
      ),
      data: Map<String, dynamic>.from(map),
      videoState: map['videoState'] as int?,
    );
  }

  @override
  Map<String, dynamic> toMap() {
    return {
      'callId': callId,
      'action': CallEvent._callActionToString(action),
      'timestamp': timestamp.millisecondsSinceEpoch,
      'videoState': videoState,
      ...data,
    };
  }
}

/// Event fired when a call is rejected
class CallRejectedEvent extends CallEvent {
  const CallRejectedEvent({
    required super.callId,
    required super.timestamp,
    super.data = const {},
  }) : super(action: CallAction.rejected);

  factory CallRejectedEvent.fromMap(Map<String, dynamic> map) {
    return CallRejectedEvent(
      callId: map['callId'] as String? ?? '',
      timestamp: DateTime.fromMillisecondsSinceEpoch(
        map['timestamp'] as int? ?? DateTime.now().millisecondsSinceEpoch,
      ),
      data: Map<String, dynamic>.from(map),
    );
  }

  @override
  Map<String, dynamic> toMap() {
    return {
      'callId': callId,
      'action': CallEvent._callActionToString(action),
      'timestamp': timestamp.millisecondsSinceEpoch,
      ...data,
    };
  }
}

/// Event fired when a call ends
class CallEndedEvent extends CallEvent {
  /// The reason the call ended
  final String reason;

  const CallEndedEvent({
    required super.callId,
    required super.timestamp,
    super.data = const {},
    this.reason = 'ended',
  }) : super(action: CallAction.ended);

  factory CallEndedEvent.fromMap(Map<String, dynamic> map) {
    return CallEndedEvent(
      callId: map['callId'] as String? ?? '',
      timestamp: DateTime.fromMillisecondsSinceEpoch(
        map['timestamp'] as int? ?? DateTime.now().millisecondsSinceEpoch,
      ),
      data: Map<String, dynamic>.from(map),
      reason: map['action'] as String? ?? 'ended',
    );
  }

  @override
  Map<String, dynamic> toMap() {
    return {
      'callId': callId,
      'action': CallEvent._callActionToString(action),
      'timestamp': timestamp.millisecondsSinceEpoch,
      'reason': reason,
      ...data,
    };
  }
}

/// Event fired when call state changes
class CallStateChangedEvent extends CallEvent {
  /// The new state of the call
  final CallState state;

  const CallStateChangedEvent({
    required super.callId,
    required super.timestamp,
    super.data = const {},
    required this.state,
  }) : super(action: CallAction.stateChanged);

  factory CallStateChangedEvent.fromMap(Map<String, dynamic> map) {
    return CallStateChangedEvent(
      callId: map['callId'] as String? ?? '',
      timestamp: DateTime.fromMillisecondsSinceEpoch(
        map['timestamp'] as int? ?? DateTime.now().millisecondsSinceEpoch,
      ),
      data: Map<String, dynamic>.from(map),
      state: _parseCallState(map['state'] as String? ?? 'unknown'),
    );
  }

  @override
  Map<String, dynamic> toMap() {
    return {
      'callId': callId,
      'action': CallEvent._callActionToString(action),
      'timestamp': timestamp.millisecondsSinceEpoch,
      'state': _callStateToString(state),
      ...data,
    };
  }

  /// Parses call state from string
  static CallState _parseCallState(String stateString) {
    switch (stateString.toLowerCase()) {
      case 'initializing':
        return CallState.initializing;
      case 'new':
        return CallState.newCall;
      case 'ringing':
        return CallState.ringing;
      case 'dialing':
        return CallState.dialing;
      case 'active':
        return CallState.active;
      case 'holding':
        return CallState.holding;
      case 'disconnected':
        return CallState.disconnected;
      default:
        return CallState.unknown;
    }
  }

  /// Converts call state to string
  static String _callStateToString(CallState state) {
    switch (state) {
      case CallState.initializing:
        return 'initializing';
      case CallState.newCall:
        return 'new';
      case CallState.ringing:
        return 'ringing';
      case CallState.dialing:
        return 'dialing';
      case CallState.active:
        return 'active';
      case CallState.holding:
        return 'holding';
      case CallState.disconnected:
        return 'disconnected';
      case CallState.unknown:
        return 'unknown';
    }
  }
}

/// Generic event for unknown or custom events
class CallGenericEvent extends CallEvent {
  const CallGenericEvent({
    required super.callId,
    required super.action,
    required super.timestamp,
    super.data = const {},
  });

  factory CallGenericEvent.fromMap(Map<String, dynamic> map) {
    return CallGenericEvent(
      callId: map['callId'] as String? ?? '',
      action: CallEvent._parseCallAction(map['action'] as String? ?? ''),
      timestamp: DateTime.fromMillisecondsSinceEpoch(
        map['timestamp'] as int? ?? DateTime.now().millisecondsSinceEpoch,
      ),
      data: Map<String, dynamic>.from(map),
    );
  }

  @override
  Map<String, dynamic> toMap() {
    return {
      'callId': callId,
      'action': CallEvent._callActionToString(action),
      'timestamp': timestamp.millisecondsSinceEpoch,
      ...data,
    };
  }
}
