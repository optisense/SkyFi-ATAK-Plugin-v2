#\!/bin/bash

# Script to debug plugin loading issues on device
# Run this while ATAK is starting up to capture plugin initialization logs

echo "======================================"
echo "ATAK Plugin Loading Debug Monitor"
echo "======================================"
echo ""
echo "This script will monitor ATAK logs for plugin loading issues."
echo "Start ATAK after running this script."
echo ""

# Clear logcat
adb logcat -c

echo "Monitoring logs... (Press Ctrl+C to stop)"
echo ""
echo "Key things to look for:"
echo "- 'SkyFiPlugin' messages"
echo "- 'PluginLoader' errors"
echo "- 'ClassNotFoundException'"
echo "- 'VerifyError'"
echo "- 'UnsatisfiedLinkError'"
echo ""
echo "================================================"
echo ""

# Monitor with filters for plugin-related messages
adb logcat -v time | grep -E "(SkyFiPlugin|PluginLoader|PluginManager|ClassNotFoundException|VerifyError|NoClassDefFoundError|UnsatisfiedLinkError|Failed to load|plugin|Plugin|PLUGIN)" | while read line
do
    # Color code the output
    if echo "$line" | grep -q "SkyFiPlugin.*onStart"; then
        echo -e "\033[0;32m✓ $line\033[0m"  # Green for successful start
    elif echo "$line" | grep -q "Successfully"; then
        echo -e "\033[0;32m✓ $line\033[0m"  # Green for success
    elif echo "$line" | grep -q -E "(Exception|Error|Failed|failed|FATAL)"; then
        echo -e "\033[0;31m✗ $line\033[0m"  # Red for errors
    elif echo "$line" | grep -q "SkyFiPlugin"; then
        echo -e "\033[1;33m→ $line\033[0m"  # Yellow for plugin messages
    else
        echo "$line"
    fi
done
