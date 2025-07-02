/// Comprehensive configuration model for VCallkit call presentation
class VCallkitCallConfiguration {
  // Visual Theme Properties
  final String? backgroundColor;
  final String? primaryColor;
  final String? accentColor;
  final String? textColor;
  final String? buttonBackgroundColor;
  final String? buttonTextColor;
  final String? iconColor;
  final double? borderRadius;
  final double? buttonSize;
  final String? fontFamily;
  final double? fontSize;
  final String? backgroundImage;
  final double? backgroundOpacity;

  // Text Translations
  final String? incomingCallTitle;
  final String? answerButtonText;
  final String? declineButtonText;
  final String? hangupButtonText;
  final String? audioCallText;
  final String? videoCallText;
  final String? callEndedText;
  final String? missedCallText;
  final String? callingText;
  final String? connectingText;

  // Call Settings
  final bool showCallerNumber;
  final bool showCallDuration;
  final int callTimeoutSeconds;

  // Audio Settings
  final bool enableVibration;
  final bool enableRingtone;

  // Additional Settings
  final bool enableCallTimeout;
  final bool useFullScreenCallUI;
  final bool showCallType;
  final bool enableTapToReturnToCall;
  final int hangupNotificationPriority;
  final bool use24HourFormat;
  final String durationFormat;

  // Accessibility
  final String answerButtonContentDescription;
  final String declineButtonContentDescription;
  final String hangupButtonContentDescription;

  const VCallkitCallConfiguration({
    // Visual Theme Properties
    this.backgroundColor,
    this.primaryColor,
    this.accentColor,
    this.textColor,
    this.buttonBackgroundColor,
    this.buttonTextColor,
    this.iconColor,
    this.borderRadius,
    this.buttonSize,
    this.fontFamily,
    this.fontSize,
    this.backgroundImage,
    this.backgroundOpacity,

    // Text Translations
    this.incomingCallTitle,
    this.answerButtonText,
    this.declineButtonText,
    this.hangupButtonText,
    this.audioCallText,
    this.videoCallText,
    this.callEndedText,
    this.missedCallText,
    this.callingText,
    this.connectingText,

    // Call Settings
    this.showCallerNumber = true,
    this.showCallDuration = true,
    this.callTimeoutSeconds = 30,

    // Audio Settings
    this.enableVibration = true,
    this.enableRingtone = true,

    // Additional Settings
    this.enableCallTimeout = true,
    this.useFullScreenCallUI = true,
    this.showCallType = true,
    this.enableTapToReturnToCall = true,
    this.hangupNotificationPriority = 0,
    this.use24HourFormat = true,
    this.durationFormat = 'mm:ss',

    // Accessibility
    this.answerButtonContentDescription = 'Answer call button',
    this.declineButtonContentDescription = 'Decline call button',
    this.hangupButtonContentDescription = 'Hangup call button',
  });

  /// Creates a VCallkitCallConfiguration from a Map
  factory VCallkitCallConfiguration.fromMap(Map<String, dynamic> map) {
    return VCallkitCallConfiguration(
      // Visual Theme Properties
      backgroundColor: map['backgroundColor']?.toString(),
      primaryColor: map['primaryColor']?.toString(),
      accentColor: map['accentColor']?.toString(),
      textColor: map['textColor']?.toString(),
      buttonBackgroundColor: map['buttonBackgroundColor']?.toString(),
      buttonTextColor: map['buttonTextColor']?.toString(),
      iconColor: map['iconColor']?.toString(),
      borderRadius: _safeDoubleConvert(map['borderRadius']),
      buttonSize: _safeDoubleConvert(map['buttonSize']),
      fontFamily: map['fontFamily']?.toString(),
      fontSize: _safeDoubleConvert(map['fontSize']),
      backgroundImage: map['backgroundImage']?.toString(),
      backgroundOpacity: _safeDoubleConvert(map['backgroundOpacity']),

      // Text Translations
      incomingCallTitle: map['incomingCallTitle']?.toString(),
      answerButtonText: map['answerButtonText']?.toString(),
      declineButtonText: map['declineButtonText']?.toString(),
      hangupButtonText: map['hangupButtonText']?.toString(),
      audioCallText: map['audioCallText']?.toString(),
      videoCallText: map['videoCallText']?.toString(),
      callEndedText: map['callEndedText']?.toString(),
      missedCallText: map['missedCallText']?.toString(),
      callingText: map['callingText']?.toString(),
      connectingText: map['connectingText']?.toString(),

      // Call Settings
      showCallerNumber: _safeBoolConvert(map['showCallerNumber']) ?? true,
      showCallDuration: _safeBoolConvert(map['showCallDuration']) ?? true,
      callTimeoutSeconds: _safeIntConvert(map['callTimeoutSeconds']) ?? 30,

      // Audio Settings
      enableVibration: _safeBoolConvert(map['enableVibration']) ?? true,
      enableRingtone: _safeBoolConvert(map['enableRingtone']) ?? true,

      // Additional Settings
      enableCallTimeout: _safeBoolConvert(map['enableCallTimeout']) ?? true,
      useFullScreenCallUI: _safeBoolConvert(map['useFullScreenCallUI']) ?? true,
      showCallType: _safeBoolConvert(map['showCallType']) ?? true,
      enableTapToReturnToCall:
          _safeBoolConvert(map['enableTapToReturnToCall']) ?? true,
      hangupNotificationPriority:
          _safeIntConvert(map['hangupNotificationPriority']) ?? 0,
      use24HourFormat: _safeBoolConvert(map['use24HourFormat']) ?? true,
      durationFormat: map['durationFormat']?.toString() ?? 'mm:ss',

      // Accessibility
      answerButtonContentDescription:
          map['answerButtonContentDescription']?.toString() ??
              'Answer call button',
      declineButtonContentDescription:
          map['declineButtonContentDescription']?.toString() ??
              'Decline call button',
      hangupButtonContentDescription:
          map['hangupButtonContentDescription']?.toString() ??
              'Hangup call button',
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

  /// Safely converts a value to int
  static int? _safeIntConvert(dynamic value) {
    if (value == null) return null;
    if (value is int) return value;
    if (value is double) return value.toInt();
    if (value is String) return int.tryParse(value);
    return null;
  }

  /// Safely converts a value to double
  static double? _safeDoubleConvert(dynamic value) {
    if (value == null) return null;
    if (value is double) return value;
    if (value is int) return value.toDouble();
    if (value is String) return double.tryParse(value);
    return null;
  }

  /// Converts VCallkitCallConfiguration to a Map
  Map<String, dynamic> toMap() {
    final map = <String, dynamic>{};

    // Visual Theme Properties
    if (backgroundColor != null) map['backgroundColor'] = backgroundColor;
    if (primaryColor != null) map['primaryColor'] = primaryColor;
    if (accentColor != null) map['accentColor'] = accentColor;
    if (textColor != null) map['textColor'] = textColor;
    if (buttonBackgroundColor != null)
      map['buttonBackgroundColor'] = buttonBackgroundColor;
    if (buttonTextColor != null) map['buttonTextColor'] = buttonTextColor;
    if (iconColor != null) map['iconColor'] = iconColor;
    if (borderRadius != null) map['borderRadius'] = borderRadius;
    if (buttonSize != null) map['buttonSize'] = buttonSize;
    if (fontFamily != null) map['fontFamily'] = fontFamily;
    if (fontSize != null) map['fontSize'] = fontSize;
    if (backgroundImage != null) map['backgroundImage'] = backgroundImage;
    if (backgroundOpacity != null) map['backgroundOpacity'] = backgroundOpacity;

    // Text Translations
    if (incomingCallTitle != null) map['incomingCallTitle'] = incomingCallTitle;
    if (answerButtonText != null) map['answerButtonText'] = answerButtonText;
    if (declineButtonText != null) map['declineButtonText'] = declineButtonText;
    if (hangupButtonText != null) map['hangupButtonText'] = hangupButtonText;
    if (audioCallText != null) map['audioCallText'] = audioCallText;
    if (videoCallText != null) map['videoCallText'] = videoCallText;
    if (callEndedText != null) map['callEndedText'] = callEndedText;
    if (missedCallText != null) map['missedCallText'] = missedCallText;
    if (callingText != null) map['callingText'] = callingText;
    if (connectingText != null) map['connectingText'] = connectingText;

    // Call Settings
    map['showCallerNumber'] = showCallerNumber;
    map['showCallDuration'] = showCallDuration;
    map['callTimeoutSeconds'] = callTimeoutSeconds;

    // Audio Settings
    map['enableVibration'] = enableVibration;
    map['enableRingtone'] = enableRingtone;

    // Additional Settings
    map['enableCallTimeout'] = enableCallTimeout;
    map['useFullScreenCallUI'] = useFullScreenCallUI;
    map['showCallType'] = showCallType;
    map['enableTapToReturnToCall'] = enableTapToReturnToCall;
    map['hangupNotificationPriority'] = hangupNotificationPriority;
    map['use24HourFormat'] = use24HourFormat;
    map['durationFormat'] = durationFormat;

    // Accessibility
    map['answerButtonContentDescription'] = answerButtonContentDescription;
    map['declineButtonContentDescription'] = declineButtonContentDescription;
    map['hangupButtonContentDescription'] = hangupButtonContentDescription;

    return map;
  }

  /// Creates a copy of this configuration with updated values
  VCallkitCallConfiguration copyWith({
    // Visual Theme Properties
    String? backgroundColor,
    String? primaryColor,
    String? accentColor,
    String? textColor,
    String? buttonBackgroundColor,
    String? buttonTextColor,
    String? iconColor,
    double? borderRadius,
    double? buttonSize,
    String? fontFamily,
    double? fontSize,
    String? backgroundImage,
    double? backgroundOpacity,

    // Text Translations
    String? incomingCallTitle,
    String? answerButtonText,
    String? declineButtonText,
    String? hangupButtonText,
    String? audioCallText,
    String? videoCallText,
    String? callEndedText,
    String? missedCallText,
    String? callingText,
    String? connectingText,

    // Call Settings
    bool? showCallerNumber,
    bool? showCallDuration,
    int? callTimeoutSeconds,

    // Audio Settings
    bool? enableVibration,
    bool? enableRingtone,

    // Additional Settings
    bool? enableCallTimeout,
    bool? useFullScreenCallUI,
    bool? showCallType,
    bool? enableTapToReturnToCall,
    int? hangupNotificationPriority,
    bool? use24HourFormat,
    String? durationFormat,

    // Accessibility
    String? answerButtonContentDescription,
    String? declineButtonContentDescription,
    String? hangupButtonContentDescription,
  }) {
    return VCallkitCallConfiguration(
      // Visual Theme Properties
      backgroundColor: backgroundColor ?? this.backgroundColor,
      primaryColor: primaryColor ?? this.primaryColor,
      accentColor: accentColor ?? this.accentColor,
      textColor: textColor ?? this.textColor,
      buttonBackgroundColor:
          buttonBackgroundColor ?? this.buttonBackgroundColor,
      buttonTextColor: buttonTextColor ?? this.buttonTextColor,
      iconColor: iconColor ?? this.iconColor,
      borderRadius: borderRadius ?? this.borderRadius,
      buttonSize: buttonSize ?? this.buttonSize,
      fontFamily: fontFamily ?? this.fontFamily,
      fontSize: fontSize ?? this.fontSize,
      backgroundImage: backgroundImage ?? this.backgroundImage,
      backgroundOpacity: backgroundOpacity ?? this.backgroundOpacity,

      // Text Translations
      incomingCallTitle: incomingCallTitle ?? this.incomingCallTitle,
      answerButtonText: answerButtonText ?? this.answerButtonText,
      declineButtonText: declineButtonText ?? this.declineButtonText,
      hangupButtonText: hangupButtonText ?? this.hangupButtonText,
      audioCallText: audioCallText ?? this.audioCallText,
      videoCallText: videoCallText ?? this.videoCallText,
      callEndedText: callEndedText ?? this.callEndedText,
      missedCallText: missedCallText ?? this.missedCallText,
      callingText: callingText ?? this.callingText,
      connectingText: connectingText ?? this.connectingText,

      // Call Settings
      showCallerNumber: showCallerNumber ?? this.showCallerNumber,
      showCallDuration: showCallDuration ?? this.showCallDuration,
      callTimeoutSeconds: callTimeoutSeconds ?? this.callTimeoutSeconds,

      // Audio Settings
      enableVibration: enableVibration ?? this.enableVibration,
      enableRingtone: enableRingtone ?? this.enableRingtone,

      // Additional Settings
      enableCallTimeout: enableCallTimeout ?? this.enableCallTimeout,
      useFullScreenCallUI: useFullScreenCallUI ?? this.useFullScreenCallUI,
      showCallType: showCallType ?? this.showCallType,
      enableTapToReturnToCall:
          enableTapToReturnToCall ?? this.enableTapToReturnToCall,
      hangupNotificationPriority:
          hangupNotificationPriority ?? this.hangupNotificationPriority,
      use24HourFormat: use24HourFormat ?? this.use24HourFormat,
      durationFormat: durationFormat ?? this.durationFormat,

      // Accessibility
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
    return 'VCallkitCallConfiguration('
        'backgroundColor: $backgroundColor, '
        'primaryColor: $primaryColor, '
        'showCallerNumber: $showCallerNumber, '
        'showCallDuration: $showCallDuration, '
        'callTimeoutSeconds: $callTimeoutSeconds, '
        'enableVibration: $enableVibration, '
        'enableRingtone: $enableRingtone'
        ')';
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;

    return other is VCallkitCallConfiguration &&
        other.backgroundColor == backgroundColor &&
        other.primaryColor == primaryColor &&
        other.accentColor == accentColor &&
        other.textColor == textColor &&
        other.buttonBackgroundColor == buttonBackgroundColor &&
        other.buttonTextColor == buttonTextColor &&
        other.iconColor == iconColor &&
        other.borderRadius == borderRadius &&
        other.buttonSize == buttonSize &&
        other.fontFamily == fontFamily &&
        other.fontSize == fontSize &&
        other.backgroundImage == backgroundImage &&
        other.backgroundOpacity == backgroundOpacity &&
        other.incomingCallTitle == incomingCallTitle &&
        other.answerButtonText == answerButtonText &&
        other.declineButtonText == declineButtonText &&
        other.hangupButtonText == hangupButtonText &&
        other.audioCallText == audioCallText &&
        other.videoCallText == videoCallText &&
        other.callEndedText == callEndedText &&
        other.missedCallText == missedCallText &&
        other.callingText == callingText &&
        other.connectingText == connectingText &&
        other.showCallerNumber == showCallerNumber &&
        other.showCallDuration == showCallDuration &&
        other.callTimeoutSeconds == callTimeoutSeconds &&
        other.enableVibration == enableVibration &&
        other.enableRingtone == enableRingtone &&
        other.enableCallTimeout == enableCallTimeout &&
        other.useFullScreenCallUI == useFullScreenCallUI &&
        other.showCallType == showCallType &&
        other.enableTapToReturnToCall == enableTapToReturnToCall &&
        other.hangupNotificationPriority == hangupNotificationPriority &&
        other.use24HourFormat == use24HourFormat &&
        other.durationFormat == durationFormat &&
        other.answerButtonContentDescription ==
            answerButtonContentDescription &&
        other.declineButtonContentDescription ==
            declineButtonContentDescription &&
        other.hangupButtonContentDescription == hangupButtonContentDescription;
  }

  @override
  int get hashCode {
    return Object.hashAll([
      backgroundColor,
      primaryColor,
      accentColor,
      textColor,
      buttonBackgroundColor,
      buttonTextColor,
      iconColor,
      borderRadius,
      buttonSize,
      fontFamily,
      fontSize,
      backgroundImage,
      backgroundOpacity,
      incomingCallTitle,
      answerButtonText,
      declineButtonText,
      hangupButtonText,
      audioCallText,
      videoCallText,
      callEndedText,
      missedCallText,
      callingText,
      connectingText,
      showCallerNumber,
      showCallDuration,
      callTimeoutSeconds,
      enableVibration,
      enableRingtone,
      enableCallTimeout,
      useFullScreenCallUI,
      showCallType,
      enableTapToReturnToCall,
      hangupNotificationPriority,
      use24HourFormat,
      durationFormat,
      answerButtonContentDescription,
      declineButtonContentDescription,
      hangupButtonContentDescription,
    ]);
  }

  /// Default configuration for testing
  static const VCallkitCallConfiguration defaultConfig =
      VCallkitCallConfiguration();

  /// Dark theme configuration
  static const VCallkitCallConfiguration darkTheme = VCallkitCallConfiguration(
    backgroundColor: '#1a1a1a',
    primaryColor: '#007AFF',
    accentColor: '#34C759',
    textColor: '#FFFFFF',
    buttonBackgroundColor: '#2C2C2E',
    buttonTextColor: '#FFFFFF',
    iconColor: '#FFFFFF',
  );

  /// Light theme configuration
  static const VCallkitCallConfiguration lightTheme = VCallkitCallConfiguration(
    backgroundColor: '#FFFFFF',
    primaryColor: '#007AFF',
    accentColor: '#34C759',
    textColor: '#000000',
    buttonBackgroundColor: '#F2F2F7',
    buttonTextColor: '#000000',
    iconColor: '#000000',
  );
}
