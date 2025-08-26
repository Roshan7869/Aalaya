# Configuration Summary - Aalay Bhilai APK

This document provides a comprehensive summary of all configuration aspects for the Aalay Bhilai APK package.

## 📋 Environment Configuration

### Environment Files
- **Template**: `.env.template` - Contains all required environment variables with sample values
- **Local Config**: `.env` - Local development configuration (gitignored)
- **Loader**: `env-loader.gradle` - Gradle script for loading environment variables
- **Validator**: `validate-config.py` - Python script for validating configuration

### Required Environment Variables

#### API Endpoints (Bhilai-specific)
```bash
API_BASE_URL_DEV=https://dev-api.aalay.com/v1/bhilai
API_BASE_URL_STAGING=https://staging-api.aalay.com/v1/bhilai
API_BASE_URL_PROD=https://api.aalay.com/v1/bhilai
```

#### Mapbox Configuration (Optional)
```bash
MAPBOX_ACCESS_TOKEN_DEV=pk.sample_development_token
MAPBOX_ACCESS_TOKEN_STAGING=pk.sample_staging_token
MAPBOX_ACCESS_TOKEN_PROD=pk.sample_production_token
```

#### Firebase Configuration
```bash
FIREBASE_PROJECT_ID_DEV=aalaya-dev
FIREBASE_PROJECT_ID_STAGING=aalaya-staging
FIREBASE_PROJECT_ID_PROD=aalaya-prod
FIREBASE_APP_ID=1:123456789012:android:abcdef123456
```

#### Keystore Configuration (Release Builds)
```bash
KEYSTORE_FILE=release.keystore
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=your_key_alias
KEY_PASSWORD=your_key_password
```

#### Bhilai-Specific Settings
```bash
BHILAI_CENTER_LAT=21.2181
BHILAI_CENTER_LNG=81.3248
BHILAI_SEARCH_RADIUS_KM=10
GOOGLE_MAPS_URL_BASE=https://www.google.com/maps/search/?api=1&query=
```

## 🏗️ Build Configuration

### Build Variants

#### Debug Variant
- **Application ID**: `com.aalay.app.debug`
- **Version Suffix**: `-DEBUG`
- **API Endpoint**: Development Bhilai API
- **Logging**: Enabled
- **Obfuscation**: Disabled
- **Signing**: Debug keystore

#### Staging Variant  
- **Application ID**: `com.aalay.app.staging`
- **Version Suffix**: `-STAGING`
- **API Endpoint**: Staging Bhilai API
- **Logging**: Disabled
- **Obfuscation**: Enabled
- **Signing**: Release keystore (if available)

#### Release Variant
- **Application ID**: `com.aalay.app`
- **Version Suffix**: None
- **API Endpoint**: Production Bhilai API
- **Logging**: Disabled
- **Obfuscation**: Enabled
- **Signing**: Release keystore (required)
- **APK Splitting**: By ABI enabled

### Build Configuration Fields
```gradle
buildConfigField "String", "API_BASE_URL", "\"${api_endpoint}\""
buildConfigField "String", "BHILAI_API_ENDPOINT", "\"${api_endpoint}/bhilai\""
buildConfigField "String", "MAPBOX_API_KEY", "\"${mapbox_token}\""
buildConfigField "String", "ENVIRONMENT", "\"${environment}\""
buildConfigField "boolean", "ENABLE_LOGGING", "${logging_enabled}"
```

## 📱 APK Generation

### Build Commands

#### Debug APK
```bash
./gradlew assembleDebug -Penv=dev
```
**Output**: `app/build/outputs/apk/debug/app-debug.apk`

#### Staging APK
```bash
./gradlew assembleStaging -Penv=staging
```
**Output**: `app/build/outputs/apk/staging/app-staging.apk`

#### Release APK
```bash
./gradlew assembleRelease -Penv=prod
```
**Output**: `app/build/outputs/apk/release/app-release.apk`

### APK Signing Configuration

#### Debug Signing
- Uses default Android debug keystore
- No additional configuration required

#### Release Signing
- Requires custom keystore file
- Keystore details loaded from environment variables
- Fallback to `release.keystore` if environment variables not set

```gradle
signingConfigs {
    release {
        storeFile file(System.getenv("KEYSTORE_FILE") ?: "release.keystore")
        storePassword System.getenv("KEYSTORE_PASSWORD")
        keyAlias System.getenv("KEY_ALIAS")
        keyPassword System.getenv("KEY_PASSWORD")
    }
}
```

## 🗄️ Database Configuration

### Room Database Setup
- **Database Name**: `aalay_database`
- **Version**: 2 (upgraded to include LocationEntity)
- **Migration Strategy**: `fallbackToDestructiveMigration()` for development

### Entities
```kotlin
@Database(
    entities = [
        AccommodationEntity::class,
        StudentPreferencesEntity::class,
        TrafficCacheEntity::class,
        LocationEntity::class  // New Bhilai locations
    ],
    version = 2,
    exportSchema = false
)
```

### LocationEntity Schema
```kotlin
@Entity(tableName = "bhilai_locations")
data class LocationEntity(
    @PrimaryKey val id: String,
    val type: String,           // "room" or "mess"
    val name: String,
    val latitude: Double,       // Validated: 21.1 to 21.3
    val longitude: Double,      // Validated: 81.2 to 81.4
    val address: String,
    val pricePerMonth: Int?,
    val rating: Float,
    val isVerified: Boolean,
    val amenities: List<String>,
    // ... additional fields
)
```

## 🌐 API Configuration

### Bhilai Location API Endpoints
```kotlin
interface BhilaiLocationApiService {
    @GET("locations")
    suspend fun getAllLocations(): Response<List<LocationEntity>>
    
    @GET("locations")
    suspend fun getLocationsByType(@Query("type") type: String): Response<List<LocationEntity>>
    
    @GET("locations/nearby")
    suspend fun getNearbyLocations(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radiusKm: Double
    ): Response<List<LocationEntity>>
    
    // ... other endpoints
}
```

### Network Configuration
- **Base URL**: Environment-specific Bhilai API endpoints
- **Timeout**: Configurable via ConfigManager
- **Retry**: Connection retry enabled
- **Logging**: Environment-dependent HTTP logging
- **Headers**: Common JSON headers added automatically

## 🗺️ Maps Integration Configuration

### Google Maps URL Generation
```kotlin
object MapUtils {
    private const val GOOGLE_MAPS_BASE_URL = "https://www.google.com/maps/search/?api=1&query="
    
    fun generateMapsUrl(latitude: Double, longitude: Double): String {
        if (!isWithinBhilaiBounds(latitude, longitude)) {
            throw IllegalArgumentException("Coordinates outside Bhilai bounds")
        }
        return "$GOOGLE_MAPS_BASE_URL$latitude,$longitude"
    }
}
```

### Coordinate Validation
- **Latitude Range**: 21.1° to 21.3° N
- **Longitude Range**: 81.2° to 81.4° E
- **Center Point**: 21.2181° N, 81.3248° E
- **Validation**: All locations must be within Bhilai bounds

### Navigation URLs
- **Search**: `https://www.google.com/maps/search/?api=1&query={lat},{lng}`
- **Directions**: `https://www.google.com/maps/dir/?api=1&destination={lat},{lng}`
- **No API Key Required**: Uses public URL scheme

## 🎨 UI Configuration

### Doodle Theme Colors
```kotlin
object DoodleColors {
    val Primary = Color(0xFF1E88E5)      // Blue
    val Secondary = Color(0xFFFF6B6B)    // Coral
    val Accent = Color(0xFF4ECDC4)       // Teal
    val Success = Color(0xFF45B7D1)      // Light Blue
    val Warning = Color(0xFFFFD93D)      // Yellow
    val Background = Color(0xFFFFFBF7)   // Cream
    val Surface = Color(0xFFFFFFFF)      // White
}
```

### Typography Configuration
- **Font Family**: `FontFamily.Cursive` for doodle-style appearance
- **Text Decoration**: `TextDecoration.Underline` for navigation links
- **Font Weights**: Regular and Bold variants
- **Sizes**: 10sp to 24sp range

### UI Components
- **Cards**: `RoundedCornerShape(16.dp to 20.dp)`
- **Buttons**: `RoundedCornerShape(25.dp)` with elevation
- **Elevation**: 4dp to 8dp for cards and buttons
- **Icons**: Emoji-based (🏠🍽️🗺️📍) for visual appeal

## 🔧 Dependency Injection Configuration

### Hilt Modules

#### DatabaseModule
```kotlin
@Provides
@Singleton
fun provideAalayDatabase(@ApplicationContext context: Context): AalayDatabase

@Provides
fun provideLocationDao(database: AalayDatabase): LocationDao
```

#### NetworkModule
```kotlin
@Provides
@Singleton
fun provideBhilaiLocationApiService(@Named("aalay_retrofit") retrofit: Retrofit): BhilaiLocationApiService
```

#### Repository Dependencies
- `BhilaiLocationRepository` - Singleton
- Repository dependencies auto-injected via constructor

### Component Hierarchy
- **SingletonComponent**: Database, Network, Repository singletons
- **ViewModelComponent**: ViewModels with repository dependencies
- **ActivityComponent**: Activity-scoped dependencies

## 🚀 CI/CD Configuration

### GitHub Actions Workflow
- **Trigger**: Push to main/develop, PR to main, manual dispatch
- **Jobs**: validate-config, build, deploy-firebase, test
- **Environments**: Debug, Staging, Release
- **Artifacts**: APK files with retention policies

### Deployment Configuration
- **Firebase App Distribution**: Automated for release builds
- **Google Play**: Configured for production deployment
- **Artifact Storage**: 30 days (debug/staging), 90 days (release)

### Secrets Required
```
MAPBOX_ACCESS_TOKEN_PROD
FIREBASE_PROJECT_ID_PROD
GOOGLE_SERVICES_JSON
KEYSTORE_BASE64
KEYSTORE_PASSWORD
KEY_ALIAS
KEY_PASSWORD
FIREBASE_TOKEN
FIREBASE_APP_ID
```

## 📊 Performance Configuration

### Database Optimization
- **Indexing**: Efficient queries for location search
- **Caching**: Flow-based reactive updates
- **Background Operations**: All database operations on background threads

### Network Optimization
- **Connection Pooling**: OkHttp connection management
- **Caching**: HTTP response caching
- **Retry Logic**: Exponential backoff for failed requests

### UI Performance
- **Compose Optimization**: State management best practices
- **Image Loading**: Glide with caching
- **List Performance**: LazyColumn for efficient scrolling

## 🔒 Security Configuration

### Network Security
- **Certificate Pinning**: Enabled in release builds
- **Network Security Config**: XML configuration for allowed domains
- **HTTPS Only**: All API endpoints use HTTPS

### Data Security
- **Encrypted Storage**: Room database with encryption
- **Secure Preferences**: EncryptedSharedPreferences for sensitive data
- **Input Validation**: All user inputs validated

### Code Protection
- **ProGuard**: Code obfuscation in release builds
- **API Key Protection**: Keys stored in environment variables
- **Signing**: APK signing for integrity verification

## 📝 Configuration Validation

### Validation Script (`validate-config.py`)
```python
def validate_config():
    required_keys = ['API_BASE_URL_PROD', 'MAPBOX_ACCESS_TOKEN_PROD']
    # Validates presence and format of required configurations
    # Warns about placeholder values
    # Checks file existence and JSON validity
```

### Build-time Validation
- Environment variable loading with fallbacks
- Coordinate validation during data insertion
- API endpoint validation during network setup

## 🐛 Troubleshooting Configuration

### Common Issues

**Missing Environment Variables**
```bash
# Solution: Copy template and configure
cp .env.template .env
nano .env
```

**Invalid Coordinates**
```kotlin
// Error: Coordinates outside Bhilai bounds
// Solution: Verify latitude (21.1-21.3) and longitude (81.2-81.4)
```

**Build Failures**
```bash
# Check configuration
python validate-config.py

# Clean and rebuild
./gradlew clean assembleDebug
```

**Signing Issues**
```
# Verify keystore configuration in environment variables
# Ensure keystore file exists and passwords are correct
```

## 📈 Configuration Best Practices

### Development
1. Use `.env` file for local development
2. Never commit sensitive keys to version control
3. Use debug build for local testing
4. Validate configuration before building

### Staging
1. Use staging environment for integration testing
2. Test with production-like data
3. Verify all API endpoints work correctly
4. Test APK installation and functionality

### Production
1. Use production API endpoints
2. Enable code obfuscation and signing
3. Test thoroughly before release
4. Monitor crash reports and analytics

---

## ✅ Configuration Checklist

- [ ] `.env` file created and configured
- [ ] API endpoints updated for Bhilai
- [ ] Firebase project configured
- [ ] Keystore created for signing
- [ ] Configuration validated with script
- [ ] Build variants tested (debug/staging/release)
- [ ] APK generated and installed successfully
- [ ] Navigation functionality verified
- [ ] UI elements displaying correctly

---

**Configuration Status**: ✅ Complete and Ready for Production