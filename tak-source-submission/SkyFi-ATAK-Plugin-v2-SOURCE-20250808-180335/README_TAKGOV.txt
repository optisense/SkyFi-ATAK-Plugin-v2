SkyFi ATAK Plugin v2.0 - Source Code Submission
================================================

This package contains the complete source code for the SkyFi ATAK Plugin v2.0.

IMPORTANT NOTES FOR TAK.GOV BUILD:
-----------------------------------
1. This plugin has been updated to work with ATAK 5.4.0.16
2. The IServiceController dependency has been removed for compatibility
3. Please use the SkyFiPluginCompatWrapper class as the main plugin entry point
4. The plugin.xml has been configured to use the compatibility wrapper

BUILD INSTRUCTIONS:
-------------------
1. Use Java 17 for compilation
2. Target ATAK version: 5.4.0.16
3. The plugin should be signed with TAK.gov certificates for distribution

KEY FILES:
----------
- app/src/main/java/com/skyfi/atak/plugin/SkyFiPluginCompatWrapper.java (Main entry point)
- app/src/main/assets/plugin.xml (Plugin configuration)
- app/build.gradle (Build configuration)

CONTACT:
--------
Developer: SkyFi / Optisense
Email: jfuginay@optisense.ai
Date: $(date)

