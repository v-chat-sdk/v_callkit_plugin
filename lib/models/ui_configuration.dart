/// UI Configuration class for VCallkit Plugin
/// Handles themes, translations, audio settings, and behavior configurations
class VCallkitUIConfiguration {
  // Visual Theme Settings
  final String? backgroundColor;
  final String? accentColor;
  final String? textColor;
  final String? secondaryTextColor;
  final String? buttonColor;
  final String? declineButtonColor;
  final String? answerButtonColor;

  // Text Translations
  final String? answerButtonText;
  final String? declineButtonText;
  final String? hangupButtonText;
  final String? incomingVoiceCallText;
  final String? incomingVideoCallText;
  final String? callInProgressText;
  final String? tapToReturnText;
  final String? unknownCallerText;
  final String? videoCallText;
  final String? voiceCallText;
  final String? ongoingCallText;
  final String? outgoingCallText;

  // Call Display Settings
  final bool showCallerNumber;
  final bool showCallDuration;
  final int callTimeoutSeconds;

  // Audio Settings
  final bool enableVibration;
  final bool enableRingtone;

  // Behavior Settings
  final bool enableCallTimeout;
  final bool useFullScreenCallUI;
  final bool showCallType;
  final bool enableTapToReturnToCall;
  final int hangupNotificationPriority;
  final bool use24HourFormat;
  final String durationFormat;

  // Accessibility Settings
  final String answerButtonContentDescription;
  final String declineButtonContentDescription;
  final String hangupButtonContentDescription;

  const VCallkitUIConfiguration({
    // Visual Theme Settings
    this.backgroundColor,
    this.accentColor,
    this.textColor,
    this.secondaryTextColor,
    this.buttonColor,
    this.declineButtonColor,
    this.answerButtonColor,

    // Text Translations
    this.answerButtonText,
    this.declineButtonText,
    this.hangupButtonText,
    this.incomingVoiceCallText,
    this.incomingVideoCallText,
    this.callInProgressText,
    this.tapToReturnText,
    this.unknownCallerText,
    this.videoCallText,
    this.voiceCallText,
    this.ongoingCallText,
    this.outgoingCallText,

    // Call Display Settings
    this.showCallerNumber = true,
    this.showCallDuration = true,
    this.callTimeoutSeconds = 60,

    // Audio Settings
    this.enableVibration = true,
    this.enableRingtone = true,

    // Behavior Settings
    this.enableCallTimeout = true,
    this.useFullScreenCallUI = true,
    this.showCallType = true,
    this.enableTapToReturnToCall = true,
    this.hangupNotificationPriority = 0,
    this.use24HourFormat = true,
    this.durationFormat = 'mm:ss',

    // Accessibility Settings
    this.answerButtonContentDescription = 'Answer call button',
    this.declineButtonContentDescription = 'Decline call button',
    this.hangupButtonContentDescription = 'Hangup call button',
  });

  /// Create configuration from a Map (from platform channels)
  factory VCallkitUIConfiguration.fromMap(Map<String, dynamic> map) {
    return VCallkitUIConfiguration(
      // Visual Theme Settings
      backgroundColor: map['backgroundColor'] as String?,
      accentColor: map['accentColor'] as String?,
      textColor: map['textColor'] as String?,
      secondaryTextColor: map['secondaryTextColor'] as String?,
      buttonColor: map['buttonColor'] as String?,
      declineButtonColor: map['declineButtonColor'] as String?,
      answerButtonColor: map['answerButtonColor'] as String?,

      // Text Translations
      answerButtonText: map['answerButtonText'] as String?,
      declineButtonText: map['declineButtonText'] as String?,
      hangupButtonText: map['hangupButtonText'] as String?,
      incomingVoiceCallText: map['incomingVoiceCallText'] as String?,
      incomingVideoCallText: map['incomingVideoCallText'] as String?,
      callInProgressText: map['callInProgressText'] as String?,
      tapToReturnText: map['tapToReturnText'] as String?,
      unknownCallerText: map['unknownCallerText'] as String?,
      videoCallText: map['videoCallText'] as String?,
      voiceCallText: map['voiceCallText'] as String?,
      ongoingCallText: map['ongoingCallText'] as String?,
      outgoingCallText: map['outgoingCallText'] as String?,

      // Call Display Settings
      showCallerNumber: map['showCallerNumber'] as bool? ?? true,
      showCallDuration: map['showCallDuration'] as bool? ?? true,
      callTimeoutSeconds: map['callTimeoutSeconds'] as int? ?? 60,

      // Audio Settings
      enableVibration: map['enableVibration'] as bool? ?? true,
      enableRingtone: map['enableRingtone'] as bool? ?? true,

      // Behavior Settings
      enableCallTimeout: map['enableCallTimeout'] as bool? ?? true,
      useFullScreenCallUI: map['useFullScreenCallUI'] as bool? ?? true,
      showCallType: map['showCallType'] as bool? ?? true,
      enableTapToReturnToCall: map['enableTapToReturnToCall'] as bool? ?? true,
      hangupNotificationPriority:
          map['hangupNotificationPriority'] as int? ?? 0,
      use24HourFormat: map['use24HourFormat'] as bool? ?? true,
      durationFormat: map['durationFormat'] as String? ?? 'mm:ss',

      // Accessibility Settings
      answerButtonContentDescription:
          map['answerButtonContentDescription'] as String? ??
              'Answer call button',
      declineButtonContentDescription:
          map['declineButtonContentDescription'] as String? ??
              'Decline call button',
      hangupButtonContentDescription:
          map['hangupButtonContentDescription'] as String? ??
              'Hangup call button',
    );
  }

  /// Convert configuration to Map for platform channels
  Map<String, dynamic> toMap() {
    final map = <String, dynamic>{
      // Call Display Settings - always include these
      'showCallerNumber': showCallerNumber,
      'showCallDuration': showCallDuration,
      'callTimeoutSeconds': callTimeoutSeconds,

      // Audio Settings - always include these
      'enableVibration': enableVibration,
      'enableRingtone': enableRingtone,

      // Behavior Settings - always include these
      'enableCallTimeout': enableCallTimeout,
      'useFullScreenCallUI': useFullScreenCallUI,
      'showCallType': showCallType,
      'enableTapToReturnToCall': enableTapToReturnToCall,
      'hangupNotificationPriority': hangupNotificationPriority,
      'use24HourFormat': use24HourFormat,
      'durationFormat': durationFormat,

      // Accessibility Settings - always include these
      'answerButtonContentDescription': answerButtonContentDescription,
      'declineButtonContentDescription': declineButtonContentDescription,
      'hangupButtonContentDescription': hangupButtonContentDescription,
    };

    // Add optional visual theme settings only if they're not null
    if (backgroundColor != null) map['backgroundColor'] = backgroundColor;
    if (accentColor != null) map['accentColor'] = accentColor;
    if (textColor != null) map['textColor'] = textColor;
    if (secondaryTextColor != null)
      map['secondaryTextColor'] = secondaryTextColor;
    if (buttonColor != null) map['buttonColor'] = buttonColor;
    if (declineButtonColor != null)
      map['declineButtonColor'] = declineButtonColor;
    if (answerButtonColor != null) map['answerButtonColor'] = answerButtonColor;

    // Add optional text translations only if they're not null
    if (answerButtonText != null) map['answerButtonText'] = answerButtonText;
    if (declineButtonText != null) map['declineButtonText'] = declineButtonText;
    if (hangupButtonText != null) map['hangupButtonText'] = hangupButtonText;
    if (incomingVoiceCallText != null)
      map['incomingVoiceCallText'] = incomingVoiceCallText;
    if (incomingVideoCallText != null)
      map['incomingVideoCallText'] = incomingVideoCallText;
    if (callInProgressText != null)
      map['callInProgressText'] = callInProgressText;
    if (tapToReturnText != null) map['tapToReturnText'] = tapToReturnText;
    if (unknownCallerText != null) map['unknownCallerText'] = unknownCallerText;
    if (videoCallText != null) map['videoCallText'] = videoCallText;
    if (voiceCallText != null) map['voiceCallText'] = voiceCallText;
    if (ongoingCallText != null) map['ongoingCallText'] = ongoingCallText;
    if (outgoingCallText != null) map['outgoingCallText'] = outgoingCallText;

    return map;
  }

  /// Create a copy with updated values
  VCallkitUIConfiguration copyWith({
    // Visual Theme Settings
    String? backgroundColor,
    String? accentColor,
    String? textColor,
    String? secondaryTextColor,
    String? buttonColor,
    String? declineButtonColor,
    String? answerButtonColor,

    // Text Translations
    String? answerButtonText,
    String? declineButtonText,
    String? hangupButtonText,
    String? incomingVoiceCallText,
    String? incomingVideoCallText,
    String? callInProgressText,
    String? tapToReturnText,
    String? unknownCallerText,
    String? videoCallText,
    String? voiceCallText,
    String? ongoingCallText,
    String? outgoingCallText,

    // Call Display Settings
    bool? showCallerNumber,
    bool? showCallDuration,
    int? callTimeoutSeconds,

    // Audio Settings
    bool? enableVibration,
    bool? enableRingtone,

    // Behavior Settings
    bool? enableCallTimeout,
    bool? useFullScreenCallUI,
    bool? showCallType,
    bool? enableTapToReturnToCall,
    int? hangupNotificationPriority,
    bool? use24HourFormat,
    String? durationFormat,

    // Accessibility Settings
    String? answerButtonContentDescription,
    String? declineButtonContentDescription,
    String? hangupButtonContentDescription,
  }) {
    return VCallkitUIConfiguration(
      // Visual Theme Settings
      backgroundColor: backgroundColor ?? this.backgroundColor,
      accentColor: accentColor ?? this.accentColor,
      textColor: textColor ?? this.textColor,
      secondaryTextColor: secondaryTextColor ?? this.secondaryTextColor,
      buttonColor: buttonColor ?? this.buttonColor,
      declineButtonColor: declineButtonColor ?? this.declineButtonColor,
      answerButtonColor: answerButtonColor ?? this.answerButtonColor,

      // Text Translations
      answerButtonText: answerButtonText ?? this.answerButtonText,
      declineButtonText: declineButtonText ?? this.declineButtonText,
      hangupButtonText: hangupButtonText ?? this.hangupButtonText,
      incomingVoiceCallText:
          incomingVoiceCallText ?? this.incomingVoiceCallText,
      incomingVideoCallText:
          incomingVideoCallText ?? this.incomingVideoCallText,
      callInProgressText: callInProgressText ?? this.callInProgressText,
      tapToReturnText: tapToReturnText ?? this.tapToReturnText,
      unknownCallerText: unknownCallerText ?? this.unknownCallerText,
      videoCallText: videoCallText ?? this.videoCallText,
      voiceCallText: voiceCallText ?? this.voiceCallText,
      ongoingCallText: ongoingCallText ?? this.ongoingCallText,
      outgoingCallText: outgoingCallText ?? this.outgoingCallText,

      // Call Display Settings
      showCallerNumber: showCallerNumber ?? this.showCallerNumber,
      showCallDuration: showCallDuration ?? this.showCallDuration,
      callTimeoutSeconds: callTimeoutSeconds ?? this.callTimeoutSeconds,

      // Audio Settings
      enableVibration: enableVibration ?? this.enableVibration,
      enableRingtone: enableRingtone ?? this.enableRingtone,

      // Behavior Settings
      enableCallTimeout: enableCallTimeout ?? this.enableCallTimeout,
      useFullScreenCallUI: useFullScreenCallUI ?? this.useFullScreenCallUI,
      showCallType: showCallType ?? this.showCallType,
      enableTapToReturnToCall:
          enableTapToReturnToCall ?? this.enableTapToReturnToCall,
      hangupNotificationPriority:
          hangupNotificationPriority ?? this.hangupNotificationPriority,
      use24HourFormat: use24HourFormat ?? this.use24HourFormat,
      durationFormat: durationFormat ?? this.durationFormat,

      // Accessibility Settings
      answerButtonContentDescription:
          answerButtonContentDescription ?? this.answerButtonContentDescription,
      declineButtonContentDescription: declineButtonContentDescription ??
          this.declineButtonContentDescription,
      hangupButtonContentDescription:
          hangupButtonContentDescription ?? this.hangupButtonContentDescription,
    );
  }

  @override
  String toString() {
    return 'VCallkitUIConfiguration('
        'showCallerNumber: $showCallerNumber, '
        'enableVibration: $enableVibration, '
        'enableRingtone: $enableRingtone, '
        'callTimeoutSeconds: $callTimeoutSeconds, '
        'backgroundColor: $backgroundColor, '
        'answerButtonText: $answerButtonText'
        ')';
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;

    return other is VCallkitUIConfiguration &&
        other.backgroundColor == backgroundColor &&
        other.accentColor == accentColor &&
        other.textColor == textColor &&
        other.secondaryTextColor == secondaryTextColor &&
        other.showCallerNumber == showCallerNumber &&
        other.showCallDuration == showCallDuration &&
        other.callTimeoutSeconds == callTimeoutSeconds &&
        other.enableVibration == enableVibration &&
        other.enableRingtone == enableRingtone &&
        other.answerButtonText == answerButtonText &&
        other.declineButtonText == declineButtonText &&
        other.hangupButtonText == hangupButtonText;
  }

  @override
  int get hashCode {
    return backgroundColor.hashCode ^
        accentColor.hashCode ^
        textColor.hashCode ^
        secondaryTextColor.hashCode ^
        showCallerNumber.hashCode ^
        showCallDuration.hashCode ^
        callTimeoutSeconds.hashCode ^
        enableVibration.hashCode ^
        enableRingtone.hashCode ^
        answerButtonText.hashCode ^
        declineButtonText.hashCode ^
        hangupButtonText.hashCode;
  }

  /// Predefined Dark Theme
  static VCallkitUIConfiguration get darkTheme => const VCallkitUIConfiguration(
        backgroundColor: '#1a1a1a',
        accentColor: '#4CAF50',
        textColor: '#ffffff',
        secondaryTextColor: '#b0b0b0',
        answerButtonColor: '#4CAF50',
        declineButtonColor: '#f44336',
        answerButtonText: 'Answer',
        declineButtonText: 'Decline',
        hangupButtonText: 'Hang Up',
        incomingVoiceCallText: 'Incoming voice call',
        incomingVideoCallText: 'Incoming video call',
        tapToReturnText: 'Tap to return to call',
      );

  /// Predefined Light Theme
  static VCallkitUIConfiguration get lightTheme =>
      const VCallkitUIConfiguration(
        backgroundColor: '#ffffff',
        accentColor: '#2196F3',
        textColor: '#000000',
        secondaryTextColor: '#666666',
        answerButtonColor: '#4CAF50',
        declineButtonColor: '#f44336',
        answerButtonText: 'Answer',
        declineButtonText: 'Decline',
        hangupButtonText: 'Hang Up',
        incomingVoiceCallText: 'Incoming voice call',
        incomingVideoCallText: 'Incoming video call',
        tapToReturnText: 'Tap to return to call',
      );

  /// Minimal configuration with only essential settings
  static VCallkitUIConfiguration get minimal => const VCallkitUIConfiguration();
}
