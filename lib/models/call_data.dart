/// Represents the data for a VoIP call
class CallData {
  /// Unique identifier for the call
  final String id;

  /// Display name of the caller
  final String callerName;

  /// Phone number of the caller
  final String callerNumber;

  /// URL or path to the caller's avatar image
  final String? callerAvatar;

  /// Whether this is a video call
  final bool isVideoCall;

  /// Additional custom data for the call
  final Map<String, dynamic> extra;

  const CallData({
    required this.id,
    required this.callerName,
    required this.callerNumber,
    this.callerAvatar,
    this.isVideoCall = false,
    this.extra = const {},
  });

  /// Creates a CallData from a Map
  factory CallData.fromMap(Map<String, dynamic> map) {
    return CallData(
      id: map['id']?.toString() ?? '',
      callerName: map['callerName']?.toString() ?? '',
      callerNumber: map['callerNumber']?.toString() ?? '',
      callerAvatar: map['callerAvatar']?.toString(),
      isVideoCall: _safeBoolConvert(map['isVideoCall']) ?? false,
      extra: _safeMapConvert(map['extra']),
    );
  }

  /// Safely converts a value to bool
  static bool? _safeBoolConvert(dynamic value) {
    if (value == null) return null;
    if (value is bool) return value;
    if (value is String) {
      return value.toLowerCase() == 'true';
    }
    if (value is num) return value != 0;
    return null;
  }

  /// Safely converts a map from platform channel
  static Map<String, dynamic> _safeMapConvert(dynamic value) {
    if (value == null) return {};

    if (value is Map<String, dynamic>) {
      return value;
    }

    if (value is Map) {
      final result = <String, dynamic>{};
      value.forEach((key, val) {
        final stringKey = key.toString();
        result[stringKey] = val;
      });
      return result;
    }

    return {};
  }

  /// Converts CallData to a Map
  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'callerName': callerName,
      'callerNumber': callerNumber,
      'callerAvatar': callerAvatar,
      'isVideoCall': isVideoCall,
      'extra': extra,
    };
  }

  /// Creates a copy of this CallData with updated values
  CallData copyWith({
    String? id,
    String? callerName,
    String? callerNumber,
    String? callerAvatar,
    bool? isVideoCall,
    Map<String, dynamic>? extra,
  }) {
    return CallData(
      id: id ?? this.id,
      callerName: callerName ?? this.callerName,
      callerNumber: callerNumber ?? this.callerNumber,
      callerAvatar: callerAvatar ?? this.callerAvatar,
      isVideoCall: isVideoCall ?? this.isVideoCall,
      extra: extra ?? this.extra,
    );
  }

  @override
  String toString() {
    return 'CallData('
        'id: $id, '
        'callerName: $callerName, '
        'callerNumber: $callerNumber, '
        'callerAvatar: $callerAvatar, '
        'isVideoCall: $isVideoCall, '
        'extra: $extra'
        ')';
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;

    return other is CallData &&
        other.id == id &&
        other.callerName == callerName &&
        other.callerNumber == callerNumber &&
        other.callerAvatar == callerAvatar &&
        other.isVideoCall == isVideoCall &&
        _mapEquals(other.extra, extra);
  }

  @override
  int get hashCode {
    return id.hashCode ^
        callerName.hashCode ^
        callerNumber.hashCode ^
        callerAvatar.hashCode ^
        isVideoCall.hashCode ^
        extra.hashCode;
  }

  /// Helper method to compare maps
  bool _mapEquals(Map<String, dynamic> a, Map<String, dynamic> b) {
    if (a.length != b.length) return false;
    for (final key in a.keys) {
      if (!b.containsKey(key) || a[key] != b[key]) return false;
    }
    return true;
  }
}
