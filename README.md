# Aalay - Student Housing Made Easy 🏠

[![Android CI](https://github.com/your-org/aalay-android/workflows/Android%20CI/badge.svg)](https://github.com/your-org/aalay-android/actions)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-blue.svg)](https://kotlinlang.org)

Aalay is a comprehensive student housing platform designed specifically for students in India. The app helps students find, book, and manage accommodations near their colleges with features like real-time traffic analysis, roommate matching, and student-verified listings.

## 🌟 Bhilai Edition Features

### 🏠 Bhilai-Specific Location Services
- **Local Room & Mess Discovery**: Dedicated services for Bhilai student accommodations
- **Coordinate Validation**: Ensures all listings are within Bhilai city bounds (21.1-21.3°N, 81.2-81.4°E)
- **Cost-Free Navigation**: Google Maps integration without API costs using URL schemes
- **Doodle-Style UI**: Playful, student-friendly interface with cursive fonts and emoji navigation
- **Walking Distance Calculator**: Special indicators for locations within walking distance from city center

### 🎨 Enhanced User Experience
- **Emoji Navigation**: 🏠 for rooms, 🍽️ for mess, 🗺️ for maps
- **Distance Display**: Real-time distance calculations from Bhilai center (21.2181°N, 81.3248°E)
- **Smart Filtering**: Quick filters for room types, verified listings, and availability
- **Offline Support**: Room database for offline browsing of cached locations

## 📱 Features

### Core Features
- **🏠 Accommodation Search**: Find PGs, hostels, and shared accommodations
- **📍 Location-Based Search**: Search near colleges with traffic-aware recommendations
- **⭐ Student Verification**: Verified student reviews and listings
- **💰 Budget Filtering**: Smart filtering based on student budgets
- **🚗 Traffic Analysis**: Real-time commute time calculations
- **👥 Roommate Matching**: Find compatible roommates based on preferences

### Advanced Features
- **🔒 Secure Authentication**: Firebase Auth with biometric support
- **💳 Integrated Booking**: Seamless booking with payment integration
- **📱 Push Notifications**: Real-time updates on bookings and matches
- **🌙 Dark Mode**: Complete dark theme support
- **🗣️ Multi-language**: Support for Hindi and English
- **📊 Analytics**: Detailed usage analytics and crash reporting

## 🏗️ Architecture

Aalay follows **Clean Architecture** principles with **MVVM** pattern:

```
├── app/                    # Android application module
├── data/                   # Data layer (repositories, API, database)
│   ├── local/             # Room database and DAOs
│   ├── remote/            # Retrofit API services
│   └── repository/        # Repository implementations
├── domain/                # Business logic layer
│   └── use_case/         # Use cases/interactors
├── ui/                    # Presentation layer
│   ├── activities/       # Activities
│   ├── fragments/        # Fragments (if any)
│   ├── screens/          # Jetpack Compose screens
│   ├── viewmodels/       # ViewModels
│   └── theme/            # UI theme and styling
├── di/                    # Dependency injection (Hilt)
├── utils/                 # Utility classes
└── services/              # Background services
```

## 🛠️ Tech Stack

### Core Technologies
- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt
- **Navigation**: Navigation Compose

### Data & Storage
- **Local Database**: Room
- **Remote API**: Retrofit + OkHttp
- **Image Loading**: Glide + Landscapist
- **Preferences**: Encrypted SharedPreferences

### Firebase Services
- **Authentication**: Firebase Auth
- **Database**: Firestore
- **Storage**: Firebase Storage
- **Messaging**: FCM
- **Analytics**: Firebase Analytics
- **Crashlytics**: Firebase Crashlytics

### External APIs
- **Maps**: Mapbox SDK
- **Directions**: Mapbox Directions API
- **Social Login**: Google Sign-In, Facebook Login

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or newer
- JDK 11 or higher
- Android SDK 34
- Git

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-org/aalay-android.git
   cd aalay-android
   ```

2. **Configure API Keys**
   ```bash
   # Copy the template file
   cp api_keys.env.template api_keys.env
   
   # Edit api_keys.env with your actual API keys
   nano api_keys.env
   ```

3. **Firebase Setup**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Download `google-services.json` and place it in `app/` directory
   - Enable Authentication, Firestore, Storage, and FCM services

4. **Mapbox Configuration**
   - Create account at [Mapbox](https://mapbox.com)
   - Add your Mapbox API key to `api_keys.env`

5. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

### Environment Configuration

The app supports three build variants:
- **debug**: Development environment
- **staging**: Testing environment  
- **release**: Production environment

## 📋 Build Variants

### Debug Build
- Includes debugging tools
- Uses development API endpoints
- Allows cleartext traffic for localhost

### Staging Build
- Mimics production environment
- Uses staging API endpoints
- Code obfuscation enabled

### Release Build
- Production-ready build
- Full code obfuscation
- Certificate pinning enabled
- Crash reporting enabled

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### UI Tests
```bash
./gradlew connectedAndroidTest
```

### Coverage Report
```bash
./gradlew jacocoTestReport
```

## 📦 Building for Production

### Building Bhilai APK

#### Quick APK Generation
```bash
# Debug APK (Development)
./gradlew assembleDebug -Penv=dev

# Staging APK (Testing)
./gradlew assembleStaging -Penv=staging

# Release APK (Production)
./gradlew assembleRelease -Penv=prod
```

#### Environment Configuration
Create `.env` file from template:
```bash
cp .env.template .env
# Edit with your Bhilai-specific API endpoints
```

**Bhilai API Endpoints:**
- Dev: `https://dev-api.aalay.com/v1/bhilai`
- Staging: `https://staging-api.aalay.com/v1/bhilai`
- Prod: `https://api.aalay.com/v1/bhilai`

#### APK Output Locations
- **Debug**: `app/build/outputs/apk/debug/app-debug.apk`
- **Staging**: `app/build/outputs/apk/staging/app-staging.apk`
- **Release**: `app/build/outputs/apk/release/app-release.apk`

📋 **Complete APK Guide**: See [BHILAI-APK-GUIDE.md](BHILAI-APK-GUIDE.md) for detailed implementation

### Signing Configuration
1. Create or obtain a signing keystore
2. Add keystore details to `local.properties`:
   ```properties
   KEYSTORE_FILE=path/to/keystore.jks
   KEYSTORE_PASSWORD=your_password
   KEY_ALIAS=your_alias
   KEY_PASSWORD=your_key_password
   ```

## 🔒 Security Features

- **Certificate Pinning**: Prevents man-in-the-middle attacks
- **Encrypted Storage**: All sensitive data encrypted locally
- **Biometric Authentication**: Fingerprint and face unlock support
- **API Request Signing**: All API requests signed for integrity
- **Network Security Config**: Strict network security policies
- **Code Obfuscation**: ProGuard rules for release builds

## 🌍 Localization

Currently supported languages:
- English (Default)
- Hindi (हिंदी)

To add new languages:
1. Create new `values-{language_code}/` directory
2. Add translated `strings.xml`
3. Update `LocaleHelper.kt` if needed

## 📊 Analytics & Monitoring

### Firebase Analytics Events
- User registration/login
- Accommodation searches
- Booking completions
- App crashes and errors

### Custom Events
- Search filters used
- Booking flow abandonment
- Feature usage patterns

## 🔧 Development Guidelines

### Code Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add documentation for public APIs
- Write unit tests for business logic

### Git Workflow
1. Create feature branch from `develop`
2. Make changes and commit with meaningful messages
3. Create pull request to `develop`
4. Code review and merge
5. Deploy from `main` branch

### Commit Message Format
```
type(scope): description

feat(auth): add biometric authentication
fix(search): resolve location permission issue
docs(readme): update setup instructions
```

## 🚨 Troubleshooting

### Common Issues

**Build fails with missing API keys**
- Ensure `api_keys.env` file exists with all required keys
- Check `google-services.json` is in the correct location

**Location not working**
- Check location permissions in device settings
- Ensure GPS is enabled
- Verify Mapbox API key is valid

**Firebase connection issues**
- Verify `google-services.json` configuration
- Check internet connectivity
- Ensure Firebase services are enabled

**Signing issues in release build**
- Verify keystore file exists and path is correct
- Check keystore passwords are correct
- Ensure `local.properties` has signing configuration

## 📈 Performance Optimization

### Image Loading
- WebP format for better compression
- Image caching with Glide
- Lazy loading for lists

### Database
- Room database with efficient queries
- Background thread operations
- Proper indexing for search queries

### Network
- Request/response caching
- Connection pooling
- Retry mechanisms with exponential backoff

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Development Setup
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

### Code of Conduct
Please read our [Code of Conduct](CODE_OF_CONDUCT.md) before contributing.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

- **Email**: support@aalay.com
- **Documentation**: [Wiki](https://github.com/your-org/aalay-android/wiki)
- **Issues**: [GitHub Issues](https://github.com/your-org/aalay-android/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-org/aalay-android/discussions)

## 🙏 Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose) for modern UI
- [Firebase](https://firebase.google.com) for backend services
- [Mapbox](https://mapbox.com) for mapping and navigation
- [Material Design](https://material.io) for design system
- [Hilt](https://dagger.dev/hilt/) for dependency injection

## 📝 Changelog

See [CHANGELOG.md](CHANGELOG.md) for a list of changes in each version.

---

**Made with ❤️ for Indian students**

*Aalay - Making student housing simple, secure, and affordable.*