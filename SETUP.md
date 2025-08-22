# Aalay Development Setup Guide 🚀

This guide helps you set up the Aalay Android project with proper environment configuration.

## 📋 Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 11 or higher
- Git
- Python 3.x (for validation scripts)

## 🛠️ Quick Setup

### 1. Clone and Configure Environment

```bash
# Clone the repository
git clone https://github.com/your-org/aalay-android.git
cd aalay-android

# Copy environment template
cp .env.template .env

# Edit .env with your actual API keys and configuration
nano .env  # or use your preferred editor
```

### 2. Firebase Setup

```bash
# Copy Firebase template
cp app/google-services.json.template app/google-services.json

# Replace with your actual Firebase configuration
# Download from Firebase Console → Project Settings → General → Your Apps
```

### 3. Validate Configuration

```bash
# Run configuration validator
python validate-config.py

# This will check for:
# - Required environment variables
# - Firebase configuration
# - Build files
# - Security configurations
```

### 4. Build and Run

```bash
# Build debug version
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

## 🔧 Environment Configuration

### Required API Keys

The `.env` file must contain these critical values:

```env
# API Configuration
API_BASE_URL_DEV=https://dev-api.aalay.com/v1/
API_BASE_URL_STAGING=https://staging-api.aalay.com/v1/
API_BASE_URL_PROD=https://api.aalay.com/v1/

# Mapbox (Get from https://mapbox.com)
MAPBOX_ACCESS_TOKEN_DEV=pk.eyJ1...your_dev_token
MAPBOX_ACCESS_TOKEN_STAGING=pk.eyJ1...your_staging_token
MAPBOX_ACCESS_TOKEN_PROD=pk.eyJ1...your_prod_token

# Firebase
FIREBASE_PROJECT_ID_DEV=your-dev-project
FIREBASE_PROJECT_ID_STAGING=your-staging-project
FIREBASE_PROJECT_ID_PROD=your-prod-project

# Google Services
GOOGLE_WEB_CLIENT_ID=your_client_id.apps.googleusercontent.com

# Facebook Login
FACEBOOK_APP_ID=your_facebook_app_id
FACEBOOK_CLIENT_TOKEN=your_facebook_client_token
```

### How Configuration Works

1. **Environment Loading**: The `env-loader.gradle` script reads `.env` file
2. **Build Config**: Values are injected into `BuildConfig` during build
3. **Runtime Access**: `ConfigManager` provides centralized access to all settings
4. **Security**: Sensitive values are encrypted and stored securely

### Build Variants

The app has three build variants, each with different configurations:

- **debug**: Development environment with verbose logging
- **staging**: Pre-production testing environment
- **release**: Production environment with optimizations

## 🔒 Security Features

### Automatic Security Configuration

- **Certificate Pinning**: Prevents man-in-the-middle attacks
- **Encrypted Storage**: All sensitive data encrypted locally
- **API Request Signing**: All API requests signed for integrity
- **Network Security**: Strict HTTPS-only communication

### Feature Flags

Enable/disable features via environment variables:

```env
FEATURE_BIOMETRIC_AUTH=true
FEATURE_DARK_MODE=true
FEATURE_OFFLINE_MODE=true
FEATURE_PUSH_NOTIFICATIONS=true
FEATURE_PAYMENT_GATEWAY=true
```

## 🧪 Testing Configuration

### Unit Tests

```bash
# Run unit tests
./gradlew test

# Run with coverage
./gradlew testDebugUnitTestCoverage
```

### Configuration Validation

```bash
# Validate environment setup
python validate-config.py

# Check specific configuration
python -c "
from validate_config import ConfigValidator
validator = ConfigValidator('.')
validator.validate_env_file()
"
```

## 🚀 Deployment

### Development Deployment

```bash
# Build and install debug version
./gradlew installDebug
```

### Staging Deployment

```bash
# Build staging APK
./gradlew assembleStagingRelease

# Or deploy to Firebase App Distribution (via CI/CD)
```

### Production Deployment

```bash
# Build production bundle
./gradlew bundleRelease

# Upload to Google Play Console (via CI/CD)
```

## 🐛 Troubleshooting

### Common Issues

**Build fails with "Missing API key"**
```bash
# Check .env file exists and has required values
cat .env | grep -E "(API_BASE_URL|MAPBOX_ACCESS_TOKEN)"

# Validate configuration
python validate-config.py
```

**Firebase authentication not working**
```bash
# Check google-services.json is properly configured
ls -la app/google-services.json

# Validate JSON structure
python -m json.tool app/google-services.json > /dev/null && echo "Valid JSON" || echo "Invalid JSON"
```

**Network requests failing**
```bash
# Check network security configuration
cat app/src/main/res/xml/network_security_config.xml

# Enable network logging in .env
echo "ENABLE_NETWORK_LOGGING=true" >> .env
```

### Debug Mode

Enable detailed logging:

```env
# In .env file
DEBUG_MODE=true
ENABLE_LOGGING=true
ENABLE_NETWORK_LOGGING=true
LOG_LEVEL=DEBUG
```

### Reset Configuration

```bash
# Reset to default development configuration
cp .env.template .env
cp app/google-services.json.template app/google-services.json

# Clean and rebuild
./gradlew clean assembleDebug
```

## 📞 Support

- **Documentation**: Check `README.md` and `DEPLOYMENT.md`
- **Issues**: Create GitHub issue with configuration validation output
- **Team Chat**: Contact development team

## 📝 Configuration Checklist

Before submitting code, ensure:

- [ ] `.env` file configured with your API keys
- [ ] `google-services.json` contains your Firebase project
- [ ] `python validate-config.py` passes without errors
- [ ] `./gradlew assembleDebug` builds successfully
- [ ] App runs on device/emulator
- [ ] Network requests work correctly
- [ ] Authentication flows work

---

**Happy coding! 🎉**

*Need help? Run `python validate-config.py` to check your setup.*