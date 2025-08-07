# Testing COG Integration in SkyFi ATAK Plugin

## 1. Check Server is Running
Look for these log messages in `adb logcat`:
```bash
adb logcat | grep -E "SkyFi\.(COG|MapComponent)"
```

Expected logs when plugin starts:
- `SkyFi.MapComponent: COG Layer Manager initialized`
- `SkyFi.COGLayerManager: COG Layer Manager initialized`
- `SkyFi.COGTileServer: COG Tile Server started on port 8282`

## 2. Test Server Directly
From your computer while plugin is running on device:
```bash
# Forward port from device to computer
adb forward tcp:8282 tcp:8282

# Test if server responds (will return 400 but proves server is up)
curl -v http://localhost:8282/test
```

## 3. Add a Test COG Layer

### Option A: Use a Public COG
In ATAK, you need to trigger the AddCOGDialog. Since we don't have a menu item yet, add this test code to trigger it:

```java
// Add this temporary test in SkyFiDropDownReceiver.java onReceiveDropDownOpen()
if (intent.getAction().equals("com.skyfi.atak.SHOW_DROPDOWN")) {
    // Add test button to trigger COG dialog
    AddCOGDialog dialog = new AddCOGDialog(getMapView().getContext(), getMapView());
    dialog.showAddDialog();
}
```

Then use one of these public COG URLs for testing:
- Sentinel-2 imagery: `https://sentinel-cogs.s3.us-west-2.amazonaws.com/sentinel-s2-l2a-cogs/36/Q/WD/2020/7/S2A_36QWD_20200701_0_L2A/TCI.tif`
- Landsat: `https://landsatlook.usgs.gov/data/collection02/level-2/standard/oli-tirs/2021/026/027/LC08_L2SP_026027_20210708_20210713_02_T1/LC08_L2SP_026027_20210708_20210713_02_T1_SR_B4.TIF`

### Option B: Create Menu Item
Add a menu item to trigger the COG dialog:

In `app/src/main/assets/menus/skyfi_menu.xml`:
```xml
<entry class="com.skyfi.atak.plugin.cog.AddCOGMenuItem" 
       text="Add COG Layer" 
       icon="resource:/icons/skyfi_logo.png"/>
```

## 4. Monitor Tile Requests
Watch for tile fetch activity:
```bash
adb logcat | grep -E "COGTileServer|COGMetadata|COGLayerManager"
```

Expected logs when tiles are being served:
- `SkyFi.COGTileServer: Serving request: /cog_xxxxx/15/9876/5432.png`
- `SkyFi.COGTileServer: Serving cached tile: cog_xxxxx_15_9876_5432`
- `SkyFi.COGMetadata: Read COG metadata: 4 overview levels`

## 5. Check ATAK Layer Management
In ATAK's layer manager (usually in overlays or map settings), you should see:
- New layer named "SkyFi Order: [name]" or whatever you named it
- Layer should be toggleable on/off
- Map should pan/zoom to the layer bounds when added

## 6. Visual Confirmation
When working correctly, you'll see:
- Satellite imagery tiles loading on the map
- Tiles appearing as you pan/zoom
- Progressive loading (low res first, then higher res)

## 7. Troubleshooting Commands

```bash
# Check if port 8282 is listening on device
adb shell netstat -an | grep 8282

# Check memory usage (tile cache)
adb shell dumpsys meminfo com.skyfi.atak.plugin

# Full debug logs
adb logcat -c  # Clear logs
# Then add a COG layer and watch:
adb logcat | grep SkyFi

# Check for errors
adb logcat *:E | grep -i skyfi
```

## 8. Test COG File Requirements
For best results, COG files should be:
- JPEG-compressed internally (for Android compatibility)
- Have overview levels (pyramids)
- Be accessible via HTTP with range request support

You can verify a COG file with:
```bash
# Using GDAL (on your computer)
gdalinfo /vsicurl/https://your-cog-url.tif
```

## 9. Expected Issues
- **Blank tiles**: TIFF isn't JPEG-compressed internally (Android can't decode)
- **Server not starting**: Port 8282 already in use
- **No tiles loading**: URL not accessible or doesn't support range requests
- **Slow loading**: No overview levels in COG

## 10. Quick Test Procedure
1. Install the plugin APK
2. Start ATAK
3. Run: `adb logcat | grep "COG Tile Server started"`
4. If you see the message, server is running
5. Add a test COG layer using the dialog
6. Watch for "Serving request" messages in logcat
7. Check map for imagery tiles