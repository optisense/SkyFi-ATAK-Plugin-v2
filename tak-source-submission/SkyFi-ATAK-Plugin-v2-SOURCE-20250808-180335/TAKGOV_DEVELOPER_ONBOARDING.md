# TAK.gov Developer Onboarding Guide

## Overview
This guide helps new SkyFi developers get access to TAK.gov resources and set up their development environment for ATAK plugin development.

## Prerequisites
- U.S. Citizenship or U.S. Person status (required for TAK.gov access)
- Company email address (@skyfi.com or @optisense.com)
- Government sponsor (if working on government contracts)

## Step 1: Register for TAK.gov Access

### 1.1 Create TAK.gov Account
1. Navigate to https://tak.gov
2. Click "Register" in the top right
3. Fill out the registration form:
   - Use your company email address
   - Select appropriate user type:
     - **Industry Developer** - For commercial development
     - **Government Contractor** - If working on government contracts
   - Provide organization details (SkyFi/Optisense)
   - Include justification for access (e.g., "Developing ATAK plugin for satellite imagery integration")

### 1.2 Account Verification
- Verification typically takes 1-3 business days
- You'll receive an email when approved
- Some accounts may require additional verification

### 1.3 Two-Factor Authentication
Once approved:
1. Log into TAK.gov
2. Navigate to Account Settings
3. Enable 2FA (required for SDK downloads)

## Step 2: Download ATAK SDK

### 2.1 Access Developer Resources
1. Log into TAK.gov with your verified account
2. Navigate to **Products** → **ATAK**
3. Select **Developer Resources**

### 2.2 Download Required SDKs
Download the following based on your needs:

#### Civilian Development (Recommended for initial development)
- **ATAK-CIV SDK** (Latest stable version)
  - Current version: 5.3.0 or later
  - File: `ATAK-CIV-X.X.X-SDK.zip`
  
#### Military Development (Requires additional clearance)
- **ATAK-MIL SDK** (if authorized)
  - Requires government sponsor approval
  - Additional ITAR compliance required

### 2.3 SDK Contents
The SDK package includes:
```
ATAK-CIV-X.X.X-SDK/
├── atak-gradle-takdev.jar     # TAK development Gradle plugin
├── atak-civ.apk              # ATAK civilian APK for testing
├── docs/                      # API documentation
├── examples/                  # Example plugins
└── libs/                      # Required libraries
```

## Step 3: Local Development Setup

### 3.1 SDK Installation

#### Option A: Use SkyFi Private SDK Repository (Internal Developers)
For quick setup, SkyFi developers can access our private SDK repository:

1. Request access to the private `skyfi-atak-sdk` repository from your team lead
2. Clone the repository:
   ```bash
   git clone git@github.com:optisense/skyfi-atak-sdk.git
   ```
3. Copy SDK files to your plugin directory:
   ```bash
   cp -r skyfi-atak-sdk/ATAK-CIV-5.3.0-SDK/* SkyFi-ATAK-Plugin-v2/sdk/
   ```

#### Option B: Manual SDK Setup (External Contributors)
1. Extract the downloaded SDK:
   ```bash
   unzip ATAK-CIV-X.X.X-SDK.zip
   ```

2. Copy required files to the plugin project:
   ```bash
   # Create SDK directory in plugin project
   mkdir -p SkyFi-ATAK-Plugin-v2/sdk
   
   # Copy SDK contents
   cp -r ATAK-CIV-X.X.X-SDK/* SkyFi-ATAK-Plugin-v2/sdk/
   
   # Copy the TAK Gradle plugin to project root
   cp ATAK-CIV-X.X.X-SDK/atak-gradle-takdev.jar SkyFi-ATAK-Plugin-v2/
   ```

### 3.2 Configure Local Properties
Create `local.properties` in the project root:
```properties
# Path to Android SDK
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk

# Path to ATAK SDK
atak.sdk.path=./sdk/ATAK-CIV-5.3.0-SDK

# Path to ATAK APK for testing
atak.apk.path=./sdk/atak-civ.apk

# Signing configuration (for local debug builds only)
key.store=android_keystore
key.store.password=android
key.alias=androiddebugkey
key.password=android
```

### 3.3 Verify Setup
Test your setup:
```bash
cd SkyFi-ATAK-Plugin-v2
./gradlew clean assembleDebug
```

## Step 4: ATAK Installation for Testing

### 4.1 Install ATAK on Device
1. Enable Developer Mode on your Android device
2. Enable USB Debugging
3. Install ATAK:
   ```bash
   adb install sdk/atak-civ.apk
   ```

### 4.2 Install Plugin for Testing
```bash
# Build and install plugin
./build-plugin-quick.sh

# Or manually:
./gradlew clean assembleCivDebug
adb install -r app/build/outputs/apk/civ/debug/app-civ-debug.apk
```

## Step 5: TAK.gov Submission Process

### 5.1 Understanding the Process
- We cannot sign plugins ourselves for production use
- All production plugins must be submitted to TAK.gov for signing
- TAK.gov builds and signs the plugin in their secure environment

### 5.2 Preparing for Submission
1. Ensure code meets TAK.gov requirements:
   - No external network dependencies during build
   - All dependencies included in submission
   - Clean, documented code
   - No hardcoded credentials

2. Create submission package:
   ```bash
   ./create-takgov-submission.sh
   ```

### 5.3 Submission Checklist
- [ ] Code builds successfully with included `atak-gradle-takdev.jar`
- [ ] No external Maven repositories required
- [ ] All proprietary libraries included in `app/libs/`
- [ ] `build.gradle` configured for TAK.gov environment
- [ ] Documentation included (README, BUILD_INSTRUCTIONS)
- [ ] Version information updated

## Important Notes

### Security Considerations
- **NEVER** commit the SDK files to public repositories
- Keep SDK files in `.gitignore`
- Use private repositories for SDK distribution within the team
- Follow ITAR compliance for military versions

### SDK Version Compatibility
- Always use SDK version matching your target ATAK version
- Check compatibility matrix on TAK.gov
- Test with multiple ATAK versions if supporting range

### Getting Help

#### Internal Resources
- SkyFi ATAK Development Slack: #atak-development
- Team Lead: Contact for private SDK repository access
- Internal Wiki: [URL to internal documentation]

#### External Resources
- TAK.gov Forums: https://tak.gov/forums
- TAK.gov Documentation: Available after login
- ATAK Discord: Community support (link available on TAK.gov)

## Troubleshooting

### Common Issues

#### "SDK not found" Error
- Verify `atak-gradle-takdev.jar` is in project root
- Check `local.properties` paths are correct
- Ensure SDK files are properly extracted

#### Build Failures
- Verify Java version (use Java 17 for TAK.gov compatibility)
- Check Android SDK is properly installed
- Ensure all dependencies in `app/libs/` are present

#### Plugin Not Loading in ATAK
- Check plugin signature matches ATAK signature (dev builds)
- Verify minimum SDK version compatibility
- Check logcat for detailed error messages:
  ```bash
  adb logcat | grep -i skyfi
  ```

## Next Steps
1. Complete TAK.gov registration
2. Download and set up SDK
3. Build and test the plugin locally
4. Join the development Slack channel
5. Review existing plugin code and documentation

---

*Last Updated: December 2024*
*Version: 1.0*