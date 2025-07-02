import 'dart:math';

class SampleCaller {
  final String name;
  final String phoneNumber;
  final String avatar;

  const SampleCaller({
    required this.name,
    required this.phoneNumber,
    required this.avatar,
  });



  static const List<SampleCaller> sampleCallers = [
    SampleCaller(
      name: 'John Doe',
      phoneNumber: '+1234567890',
      avatar: 'https://i.pravatar.cc/256?img=1',
    ),
    SampleCaller(
      name: 'Jane Smith',
      phoneNumber: '+1987654321',
      avatar: 'https://i.pravatar.cc/256?img=2',
    ),
    SampleCaller(
      name: 'Mike Johnson',
      phoneNumber: '+1122334455',
      avatar: 'https://i.pravatar.cc/256?img=3',
    ),
    SampleCaller(
      name: 'Sarah Wilson',
      phoneNumber: '+1555666777',
      avatar: 'https://i.pravatar.cc/256?img=4',
    ),
    SampleCaller(
      name: 'Ahmed Hassan',
      phoneNumber: '+201234567890',
      avatar: 'https://i.pravatar.cc/256?img=5',
    ),
    SampleCaller(
      name: 'Maria Garcia',
      phoneNumber: '+34987654321',
      avatar: 'https://i.pravatar.cc/256?img=6',
    ),
    SampleCaller(
      name: 'Pierre Dubois',
      phoneNumber: '+33123456789',
      avatar: 'https://i.pravatar.cc/256?img=7',
    ),
    SampleCaller(
      name: 'Alex Turner',
      phoneNumber: '+447123456789',
      avatar: '', // Test case with no avatar - should show initials
    ),
  ];

  /// Get a random sample caller
  static SampleCaller getRandom() {
    final random = Random();
    return sampleCallers[random.nextInt(sampleCallers.length)];
  }

  /// For backward compatibility
  String get number => phoneNumber;

  SampleCaller copyWith({
    String? name,
    String? phoneNumber,
    String? avatar,
  }) {
    return SampleCaller(
      name: name ?? this.name,
      phoneNumber: phoneNumber ?? this.phoneNumber,
      avatar: avatar ?? this.avatar,
    );
  }
}
