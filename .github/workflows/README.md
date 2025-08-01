# GitHub Actions Build Instructions

## ATAK CIV Build Workflow

The `build-atak-civ.yml` workflow builds the SkyFi ATAK Plugin v2 specifically for ATAK CIV 5.4 (Play Store version).

### Prerequisites

Before the workflow can run successfully, ensure these files are in your repository:

1. **atak-gradle-takdev.jar** - ATAK Gradle plugin (already in repo)
2. **android_keystore** - ATAK signing keystore (already in repo)

### Workflow Triggers

- **Push to main/develop branches** - Automatic build
- **Pull requests to main** - Build for verification
- **Manual trigger** - Use "Run workflow" button in Actions tab

### Build Output

The workflow produces:
- **APK artifact** - Available for 30 days in workflow run
- **Release APK** - Automatically created when pushing tags (e.g., `v2.0.0`)

### Usage

1. **Manual Build**:
   - Go to Actions tab
   - Select "Build ATAK CIV Release"
   - Click "Run workflow"

2. **Download APK**:
   - Go to workflow run
   - Download "SkyFi-ATAK-CIV-APK" artifact
   - Extract ZIP to get APK

3. **Create Release**:
   ```bash
   git tag v2.0.0
   git push origin v2.0.0
   ```

The build uses debug signing configuration for ATAK compatibility, matching the v1 plugin signing approach.