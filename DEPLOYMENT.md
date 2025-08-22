# Aalay Android App - Deployment Guide 🚀

This guide provides step-by-step instructions for deploying the Aalay Android app to production environments.

## 📋 Pre-Deployment Checklist

### Code Quality
- [ ] All unit tests passing
- [ ] UI tests passing  
- [ ] Lint checks passing
- [ ] Security scans clear
- [ ] Code review completed
- [ ] Performance testing completed

### Configuration
- [ ] Production API endpoints configured
- [ ] Firebase production project setup
- [ ] Google Services JSON updated
- [ ] Mapbox production API keys
- [ ] Signing certificate ready
- [ ] ProGuard rules tested

### Security
- [ ] Certificate pinning configured
- [ ] API keys secured
- [ ] Sensitive data encrypted
- [ ] Network security config updated
- [ ] Permission requests reviewed

## 🏗️ Build Environments

### Development Environment
- **Purpose**: Local development and testing
- **API**: `https://dev-api.aalay.com/v1/`
- **Firebase**: Development project
- **Signing**: Debug keystore
- **Logs**: Verbose logging enabled

### Staging Environment  
- **Purpose**: Pre-production testing
- **API**: `https://staging-api.aalay.com/v1/`
- **Firebase**: Staging project
- **Signing**: Production keystore
- **Logs**: Limited logging

### Production Environment
- **Purpose**: Live app for users
- **API**: `https://api.aalay.com/v1/`
- **Firebase**: Production project
- **Signing**: Production keystore
- **Logs**: Error logging only

## 🔧 Local Build Setup

### 1. Environment Configuration

Create `local.properties` file:
```properties
# Android SDK
sdk.dir=/path/to/android/sdk

# Signing configuration
KEYSTORE_FILE=release.keystore
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=aalay_release
KEY_PASSWORD=your_key_password

# API Keys (for local builds)
MAPBOX_API_KEY=your_mapbox_key
GOOGLE_WEB_CLIENT_ID=your_google_client_id
```

### 2. Firebase Configuration

1. Download `google-services.json` from Firebase Console
2. Place in `app/` directory
3. Ensure all Firebase services are enabled:
   - Authentication
   - Firestore Database
   - Storage
   - Cloud Messaging
   - Analytics
   - Crashlytics

### 3. Signing Setup

Generate release keystore:
```bash
keytool -genkey -v -keystore aalay-release.jks \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -alias aalay_release
```

### 4. Build Commands

**Debug Build**:
```bash
./gradlew assembleDebug
```

**Release APK**:
```bash
./gradlew assembleRelease
```

**Release Bundle** (Recommended):
```bash
./gradlew bundleRelease
```

## 🔄 CI/CD Pipeline

### GitHub Actions Workflow

The CI/CD pipeline automatically:

1. **On Pull Request**:
   - Runs unit tests
   - Performs lint checks
   - Security scanning
   - Builds debug APK

2. **On Push to `develop`**:
   - All PR checks
   - Builds staging release
   - Deploys to Firebase App Distribution

3. **On Push to `main`**:
   - All previous checks
   - Builds production release
   - Uploads to Google Play Console (Internal Testing)

### Required Secrets

Configure these secrets in GitHub repository settings:

```
GOOGLE_SERVICES_JSON           # Firebase configuration
SIGNING_KEYSTORE              # Base64 encoded keystore
KEYSTORE_PASSWORD             # Keystore password
KEY_ALIAS                     # Key alias
KEY_PASSWORD                  # Key password
FIREBASE_APP_ID_STAGING       # Firebase App ID for staging
FIREBASE_SERVICE_ACCOUNT      # Firebase service account JSON
GOOGLE_PLAY_SERVICE_ACCOUNT   # Google Play service account JSON
SLACK_WEBHOOK                 # Slack notifications
```

## 🏪 Google Play Store Deployment

### 1. Initial Setup

1. **Create Google Play Console Account**
   - Go to [Google Play Console](https://play.google.com/console)
   - Pay $25 registration fee
   - Complete developer profile

2. **Create App Listing**
   - Upload app icon (512x512)
   - Add screenshots for different devices
   - Write app description
   - Set content rating
   - Configure pricing and distribution

### 2. Upload Process

**Manual Upload**:
1. Build release bundle: `./gradlew bundleRelease`
2. Go to Google Play Console → App releases
3. Create new release in Internal testing
4. Upload `.aab` file
5. Fill release notes
6. Review and publish

**Automated Upload** (via CI/CD):
- Automatic upload to Internal testing track
- Manual promotion to Alpha/Beta/Production

### 3. Release Tracks

1. **Internal Testing**
   - Up to 100 testers
   - Immediate publication
   - For team testing

2. **Alpha Testing**
   - Closed testing group
   - Up to 100 testers
   - For beta testers

3. **Beta Testing**
   - Open or closed testing
   - Up to 20,000 testers
   - For community feedback

4. **Production**
   - Live for all users
   - Gradual rollout recommended

## 📱 Firebase App Distribution

### Staging Deployment

Firebase App Distribution is used for staging builds:

1. **Setup**:
   ```bash
   npm install -g firebase-tools
   firebase login
   ```

2. **Manual Distribution**:
   ```bash
   firebase appdistribution:distribute app/build/outputs/apk/staging/app-staging-release.apk \
     --app 1:your-project:android:your-app-id \
     --groups staging-testers \
     --release-notes "Staging build with latest features"
   ```

3. **Automatic Distribution**:
   - Handled by GitHub Actions
   - Triggered on push to `develop` branch

## 🔒 Security Considerations

### Certificate Pinning
- Update certificate pins before expiry
- Test staging environment first
- Have backup pins ready

### API Key Rotation
- Rotate API keys regularly
- Update in all environments
- Monitor for unauthorized usage

### Signing Security
- Store keystores securely
- Use strong passwords
- Backup keystores safely
- Restrict access to signing keys

## 📊 Monitoring & Analytics

### Crash Monitoring
- Firebase Crashlytics enabled
- Real-time crash reporting
- Automatic crash grouping
- Performance monitoring

### Analytics
- Firebase Analytics events
- User behavior tracking
- Feature usage statistics
- Conversion funnel analysis

### Performance Monitoring
- App startup time
- Network request latency
- Screen rendering performance
- Memory usage patterns

## 🚨 Rollback Procedures

### Google Play Store
1. **Immediate Rollback**:
   - Go to Release management → App releases
   - Stop rollout of current release
   - Promote previous stable version

2. **Gradual Rollback**:
   - Reduce rollout percentage
   - Monitor crash reports
   - Full rollback if issues persist

### Firebase Crashlytics
- Monitor crash rate after release
- Set up alerts for crash spikes
- Automated rollback if crash rate > 1%

## 📈 Post-Deployment

### 1. Monitoring (First 24 hours)
- [ ] Crash reports
- [ ] User reviews
- [ ] Performance metrics
- [ ] Server load
- [ ] API error rates

### 2. User Feedback
- [ ] Monitor app store reviews
- [ ] Check support channels
- [ ] Social media mentions
- [ ] In-app feedback

### 3. Performance Analysis
- [ ] Analytics dashboard
- [ ] User engagement metrics
- [ ] Feature adoption rates
- [ ] Revenue impact (if applicable)

## 🆘 Emergency Procedures

### Critical Bug Found
1. **Immediate Actions**:
   - Stop current rollout
   - Assess impact severity
   - Communicate with team

2. **Hotfix Process**:
   - Create hotfix branch from `main`
   - Implement minimal fix
   - Fast-track testing
   - Emergency deployment

3. **Communication**:
   - Notify stakeholders
   - Update app store description if needed
   - Prepare user communication

### Server Issues
1. **Backend Problems**:
   - Check server status
   - Coordinate with backend team
   - Enable graceful degradation

2. **API Rate Limiting**:
   - Monitor API usage
   - Implement request throttling
   - Cache critical data locally

## 📋 Deployment Checklist

### Pre-Release
- [ ] Version number updated
- [ ] Release notes prepared
- [ ] All tests passing
- [ ] Security review completed
- [ ] Performance benchmarks met
- [ ] Staging deployment successful

### Release Day
- [ ] Final build verification
- [ ] Deployment executed
- [ ] Initial monitoring setup
- [ ] Team notifications sent
- [ ] Documentation updated

### Post-Release
- [ ] 24-hour monitoring
- [ ] User feedback collection
- [ ] Performance analysis
- [ ] Success metrics evaluation
- [ ] Lessons learned documented

## 📞 Support Contacts

- **DevOps Team**: devops@aalay.com
- **QA Team**: qa@aalay.com  
- **Product Team**: product@aalay.com
- **Emergency Hotline**: +91-XXXX-XXXX

---

**Remember**: Always test thoroughly in staging before production deployment!

*For questions or issues with this guide, contact the DevOps team.*