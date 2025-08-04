# SkyFi ATAK Plugin Beta Testing Guide

## Uninstalling Old Version

### Method 1: Through ATAK
1. Open ATAK
2. Go to Settings → Tools
3. Find "SkyFi Plugin" in the list
4. Tap and select "Uninstall"
5. Confirm uninstallation

### Method 2: Through Android Settings
1. Go to Android Settings → Apps
2. Find "SkyFi ATAK Plugin" or "com.skyfi.atak.plugin"
3. Tap on it
4. Select "Uninstall"
5. Confirm

## Installing New Beta Version

### Prerequisites
1. Enable "Unknown Sources" or "Install unknown apps" for your file manager:
   - Settings → Security → Unknown Sources (older Android)
   - Settings → Apps & notifications → Special app access → Install unknown apps (newer Android)

### Installation Steps

1. **Download the APK**
   - Go to: https://github.com/optisense/SkyFi-ATAK-Plugin-v2
   - Navigate to: `/releases/v2.0-beta2/`
   - Download: `skyfi-atak-plugin-v2.0-beta2-civ-debug.apk`
   - Or direct link: https://github.com/optisense/SkyFi-ATAK-Plugin-v2/blob/main/releases/v2.0-beta2/skyfi-atak-plugin-v2.0-beta2-civ-debug.apk

2. **Install the APK**
   - Open your Downloads folder
   - Tap on the APK file
   - Review permissions
   - Tap "Install"
   - Wait for installation to complete

3. **Verify Installation**
   - Open ATAK 5.4.0
   - Look for the SkyFi icon in the toolbar
   - Go to Settings → Tools to confirm "SkyFi Plugin v2.0-beta2" is listed

## Testing Checklist

After installation, test these features:

- [ ] SkyFi toolbar icon opens the plugin
- [ ] API key configuration works
- [ ] Metrics display real data (satellite count, coverage, etc.)
- [ ] Draw AOI using ATAK's drawing tools
- [ ] Tap drawn shapes to see SkyFi icon in menu
- [ ] Save shape as AOI
- [ ] Task satellite with resolution bands
- [ ] Priority selection (WHEN_AVAILABLE/PRIORITY)

## Troubleshooting

### "App not installed" error
- Uninstall any previous version first
- Check available storage space
- Ensure APK downloaded completely

### Plugin doesn't appear in ATAK
- Restart ATAK
- Check ATAK version is 5.4.0
- Reinstall the plugin

### Can't draw shapes
- Use the polygon tool in ATAK's drawing toolbar
- Ensure you're in drawing mode
- Complete shape by double-tapping last point

## Reporting Issues

Please report any bugs or issues to:
https://github.com/optisense/SkyFi-ATAK-Plugin-v2/issues

Include:
- Android version
- ATAK version
- Steps to reproduce
- Screenshots if applicable