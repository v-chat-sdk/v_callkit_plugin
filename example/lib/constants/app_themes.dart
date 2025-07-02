/// Predefined themes for call customization
class AppThemes {
  static const Map<String, Map<String, dynamic>> themes = {
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

  static const String defaultTheme = 'Dark Green';

  static List<String> get themeNames => themes.keys.toList();

  static Map<String, dynamic>? getTheme(String name) => themes[name];

  static bool isValidTheme(String name) => themes.containsKey(name);
}
