# GitHub Actions CI/CD Pipeline

This repository contains a comprehensive GitHub Actions CI/CD pipeline for building and releasing the SkyFi ATAK Plugin v2.

## Available Workflows

### 1. Build and Release (`build-release.yml`) - **RECOMMENDED**
**Primary workflow for beta releases and production builds.**

- **Triggers**: Tag pushes (`v*`) and manual dispatch
- **Java Version**: 11 (matches build.gradle specification)
- **Build Matrix**: CIV and MIL flavors × Debug and Release builds
- **Features**:
  - Automated APK signing with debug keystore
  - Generates release notes from commits
  - Creates draft GitHub releases with APK artifacts
  - Comprehensive build information and checksums
  - Artifact retention for 30 days

### 2. PR Validation (`pr-validation.yml`) - **RECOMMENDED**
**Validates pull requests with fast feedback.**

- **Triggers**: Pull requests to main/develop branches
- **Features**:
  - Quick CIV debug build validation
  - Unit test execution
  - Lint checks
  - PR comment updates with build status
  - Artifact retention for 7 days

### 3. Legacy Workflows (Existing)
**Original workflows - consider migrating to new ones.**

- `build-atak-civ.yml` - Original CIV-only build (uses Java 17)
- `build-dev-bundle.yml` - Development bundle creation
- `build-unsigned-takgov.yml` - TAK.gov submission builds

## Migration Guide

### From Legacy to New Workflows

The new workflows (`build-release.yml` and `pr-validation.yml`) provide several advantages:

**Improvements:**
- ✅ Correct Java 11 usage (matches build.gradle)
- ✅ Multi-flavor support (CIV + MIL)
- ✅ Better error handling and debugging
- ✅ Comprehensive release automation
- ✅ Security best practices
- ✅ Matrix builds for efficiency

**Migration Steps:**
1. Test new workflows with manual dispatch
2. Verify APK compatibility with ATAK
3. Consider disabling legacy workflows
4. Update documentation and procedures

## Usage Instructions

### Creating a Beta Release

1. **Tag-based Release** (Recommended):
   ```bash
   git tag v2.0.0-beta.1
   git push origin v2.0.0-beta.1
   ```

2. **Manual Release** (For testing):
   - Go to Actions → "Build and Release SkyFi ATAK Plugin"
   - Click "Run workflow"
   - Select build type and options
   - Monitor the workflow progress

### Pull Request Testing

1. Create a pull request to main/develop
2. PR validation workflow runs automatically
3. Check workflow status and download test APKs
4. Verify functionality before merging

### Downloading APKs

**From Workflow Runs:**
1. Go to Actions tab
2. Select completed workflow run
3. Download artifact ZIP files
4. Extract APKs for testing

**From Releases:**
1. Go to Releases page
2. Download APKs from latest release
3. Verify SHA256 checksums (provided in release notes)

## Prerequisites

### Repository Files
- `atak-gradle-takdev.jar` - ATAK Gradle plugin ✅
- `android_keystore` - ATAK signing keystore ✅
- SDK dependencies in `/sdk/` directory ✅

### GitHub Secrets (Optional)
- `ANDROID_KEYSTORE_PASSWORD` - For production signing (future)
- `ANDROID_KEY_PASSWORD` - For production signing (future)

## Build Configuration

### Java Version
- **New Workflows**: Java 11 (matches build.gradle)
- **Legacy Workflows**: Java 17 (incorrect)

### Build Variants
- **CIV Debug/Release**: For civilian ATAK versions
- **MIL Debug/Release**: For military ATAK versions

### Signing Configuration
- **Debug Builds**: Use debug keystore for development
- **Release Builds**: Use debug keystore (production signing planned)

## Troubleshooting

### Common Issues

1. **Java Version Mismatch**:
   - Ensure using Java 11 (new workflows handle this)
   - Check build.gradle for correct version

2. **Missing Dependencies**:
   - Verify ATAK SDK in `/sdk/` directory
   - Check `atak-gradle-takdev.jar` exists

3. **Keystore Issues**:
   - Confirm `android_keystore` file is committed
   - Check keystore passwords in workflow

### Debug Tips
- Check workflow logs for detailed error messages
- Verify artifact uploads for successful builds
- Compare with local build process
- Test with manual workflow dispatch first

## Security Notes

- Debug keystore used for beta testing only
- Production signing certificates managed separately
- No classified information in public workflows
- Follow Space Force security protocols for deployment

---

For questions or issues, please create a GitHub issue or contact the development team.