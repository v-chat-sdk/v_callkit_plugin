/// Predefined languages and translations for call customization
class AppLanguages {
  static const Map<String, Map<String, String>> languages = {
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

  static const String defaultLanguage = 'English';

  static List<String> get languageNames => languages.keys.toList();

  static Map<String, String>? getLanguage(String name) => languages[name];

  static bool isValidLanguage(String name) => languages.containsKey(name);

  static String getLanguageFlag(String language) {
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
}
