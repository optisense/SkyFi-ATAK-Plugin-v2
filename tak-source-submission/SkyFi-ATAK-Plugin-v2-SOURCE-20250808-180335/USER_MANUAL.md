# SkyFi ATAK Plugin User Manual
## Version 2.0-beta3

---

## Table of Contents
1. [Introduction](#introduction)
2. [Installation](#installation)
3. [Getting Started](#getting-started)
4. [Core Features](#core-features)
5. [Ordering Satellite Imagery](#ordering-satellite-imagery)
6. [Managing Orders](#managing-orders)
7. [Working with AOIs](#working-with-aois)
8. [Cloud Optimized GeoTIFF Support](#cloud-optimized-geotiff-support)
9. [Collaboration Features](#collaboration-features)
10. [Troubleshooting](#troubleshooting)
11. [FAQ](#faq)

---

## Introduction

### What is SkyFi?
SkyFi is a satellite imagery platform that provides on-demand access to Earth observation data. The SkyFi ATAK Plugin integrates this capability directly into the Android Team Awareness Kit (ATAK), enabling tactical users to order, view, and share satellite imagery in the field.

### Key Capabilities
- **On-Demand Satellite Tasking**: Order new satellite imagery for any location
- **Archive Access**: Search and download historical satellite imagery
- **Real-Time Collaboration**: Share imagery and Areas of Interest (AOIs) with team members
- **Offline Support**: Cache imagery for use without internet connectivity
- **COG Support**: Stream large satellite images without downloading entire files

### Requirements
- ATAK version 5.4.0 or higher (CIV or MIL)
- Android device running Android 7.0 (API 24) or higher
- Active SkyFi account with API credentials
- Internet connection for ordering and downloading imagery

---

## Installation

### Step 1: Obtain the Plugin
1. Download the SkyFi ATAK Plugin APK file:
   - **TAK.gov Users**: Download from official TAK.gov plugin repository
   - **Direct Users**: Obtain from SkyFi support team
   - **File name**: `ATAK-Plugin-SkyFi-*.apk`

### Step 2: Install the Plugin

#### Method A: Using ATAK's Plugin Manager
1. Open ATAK
2. Tap the menu button (three horizontal lines)
3. Navigate to **Settings** → **Tool Preferences** → **Plugin Management**
4. Tap **Load Plugin**
5. Navigate to the downloaded APK file
6. Select the file and tap **Install**
7. Restart ATAK when prompted

#### Method B: Side-loading via ADB
```bash
adb install -r ATAK-Plugin-SkyFi-*.apk
```

### Step 3: Verify Installation
1. After ATAK restarts, open the menu
2. Look for **SkyFi** in the tools list
3. If present, the plugin is successfully installed

---

## Getting Started

### Initial Configuration

#### 1. API Credentials Setup
1. Open ATAK and navigate to **SkyFi** from the menu
2. On first launch, you'll see the authentication screen
3. Enter your SkyFi credentials:
   - **API URL**: `https://api.skyfi.com` (or your organization's endpoint)
   - **API Key**: Your SkyFi API key
   - **Organization ID**: Your organization identifier
4. Tap **Save & Connect**
5. A green checkmark indicates successful connection

#### 2. User Profile Configuration
1. Navigate to **SkyFi** → **Profile**
2. Configure your preferences:
   - **Default Sensor Type**: Optical or SAR
   - **Default Resolution**: 30cm, 50cm, 1m, etc.
   - **Auto-cache Imagery**: Enable/disable automatic caching
   - **Cache Size Limit**: Set maximum cache size (GB)

### Understanding the Interface

#### Main Toolbar
When you open the SkyFi plugin, you'll see three main tabs:
- **Orders**: View and manage your satellite imagery orders
- **Archive**: Search historical imagery
- **Profile**: Manage settings and preferences

#### Map Integration
The plugin integrates directly with ATAK's map view:
- **Purple Shapes**: Areas of Interest (AOIs) for ordering
- **Blue Overlays**: Available archived imagery
- **Green Overlays**: Completed orders ready for viewing

---

## Core Features

### Drawing Areas of Interest (AOIs)

#### Creating an AOI
1. On the ATAK map, use the drawing tools to create a shape:
   - Tap the **drawing toolbar** (pencil icon)
   - Select **Polygon** or **Rectangle** tool
   - Draw your area of interest on the map
   - Double-tap to complete the shape

2. Converting to SkyFi AOI:
   - Long-press on the shape you drew
   - Select **SkyFi** → **Convert to AOI** from the radial menu
   - The shape will turn purple, indicating it's now a SkyFi AOI

#### AOI Information
Each AOI automatically calculates:
- **Area**: Square kilometers covered
- **Center Point**: Lat/lon coordinates
- **Estimated Cost**: Based on current pricing
- **Sensor Compatibility**: Which satellites can image this area

### Sharing AOIs with Team Members

#### Via CoT (Cursor on Target)
1. Select an AOI on the map
2. Tap **Share** in the radial menu
3. Choose recipients from your team list
4. The AOI will appear on teammates' maps in real-time

#### Metadata Included
- AOI boundaries
- Area calculation
- Order status (if applicable)
- Time of interest window

---

## Ordering Satellite Imagery

### Tasking Orders (New Imagery)

#### Step 1: Define Your AOI
1. Draw your area of interest on the map (see [Drawing AOIs](#drawing-areas-of-interest-aois))
2. Ensure the area is within limits:
   - **Minimum**: 25 km²
   - **Maximum**: 500 km² (varies by sensor)

#### Step 2: Configure Order Parameters
1. With the AOI selected, tap **Order Imagery** from the radial menu
2. Configure your order:

**Sensor Type:**
- **Optical**: Visual imagery, requires clear weather
  - Best for: Detailed analysis, change detection
  - Resolution: 30cm - 3m
- **SAR (Radar)**: All-weather, day/night capability
  - Best for: Maritime, flooding, persistent surveillance
  - Resolution: 50cm - 3m

**Priority:**
- **Standard**: 7-14 days typical delivery
- **Priority**: 3-7 days typical delivery
- **Urgent**: 24-72 hours (subject to availability)

**Time Window:**
- **Start Date**: Earliest acceptable image date
- **End Date**: Latest acceptable image date
- **Time of Day**: Morning/Afternoon/Anytime

**Cloud Cover** (Optical only):
- 0-10%: Clear imagery required
- 10-20%: Mostly clear acceptable
- 20%+: Partial clouds acceptable

#### Step 3: Review and Submit
1. Review the order summary:
   - Total area coverage
   - Estimated cost
   - Delivery timeframe
2. Add any special instructions
3. Tap **Submit Order**
4. You'll receive a confirmation with order ID

### Archive Orders (Historical Imagery)

#### Step 1: Search Archive
1. Navigate to **SkyFi** → **Archive**
2. Define search parameters:
   - Draw search area on map OR
   - Enter coordinates manually
3. Set date range:
   - **From**: Earliest date of interest
   - **To**: Latest date of interest
4. Filter options:
   - **Sensor Type**: Optical/SAR/Both
   - **Resolution**: Minimum acceptable resolution
   - **Cloud Cover**: Maximum acceptable (optical only)
   - **Off-nadir Angle**: Maximum viewing angle

#### Step 2: Review Results
Results display as overlays on the map:
- **Blue borders**: Available imagery
- Tap an overlay to see:
  - Capture date and time
  - Sensor/satellite name
  - Resolution
  - Cloud cover percentage
  - Preview thumbnail

#### Step 3: Order Archive Imagery
1. Select desired image(s)
2. Tap **Add to Cart**
3. Review cart and total cost
4. Tap **Process Order**

---

## Managing Orders

### Order Status Tracking

Navigate to **SkyFi** → **Orders** to view all orders:

#### Order States
- **Submitted**: Order received, awaiting scheduling
- **Scheduled**: Satellite tasked, awaiting capture
- **Capturing**: Satellite actively imaging
- **Processing**: Raw data being processed
- **Available**: Ready for download
- **Completed**: Downloaded to device
- **Failed**: Unable to complete (weather, technical issues)

### Downloading Imagery

#### Automatic Download
When enabled in settings, completed orders download automatically when on WiFi.

#### Manual Download
1. Go to **Orders** tab
2. Find orders marked **Available**
3. Tap the download icon
4. Choose quality:
   - **Full Resolution**: Best quality, larger file
   - **Optimized**: Balanced quality and size
   - **Preview**: Quick download, lower quality

### Viewing Downloaded Imagery

#### As Map Overlay
1. Downloaded imagery automatically appears as a map layer
2. Adjust opacity using the slider in the order details
3. Toggle visibility with the eye icon

#### Layer Management
1. Access ATAK's **Layer Manager**
2. Find **SkyFi Imagery** section
3. Toggle individual images on/off
4. Adjust drawing order (stack priority)

---

## Working with AOIs

### AOI Management

#### Saving AOIs for Reuse
1. After drawing an AOI, long-press it
2. Select **Save as Template**
3. Name your template (e.g., "Base Perimeter", "Northern Sector")
4. Access saved templates from **SkyFi** → **AOI Templates**

#### Batch Operations
1. Select multiple AOIs by:
   - Holding Shift and tapping each AOI
   - Using the lasso tool to select a group
2. Available batch actions:
   - Order imagery for all selected
   - Share with team
   - Delete
   - Export to KML

### AOI Analytics

Each AOI provides useful metrics:
- **Revisit Rate**: How often satellites pass over
- **Best Imaging Times**: Optimal sun angles for optical
- **Weather Statistics**: Historical cloud cover patterns
- **Cost Estimates**: Based on area and sensor type

---

## Cloud Optimized GeoTIFF Support

### What are COGs?
Cloud Optimized GeoTIFFs allow streaming of large satellite images without downloading the entire file. Only the visible portion at the current zoom level is fetched.

### Adding COG Layers

#### From SkyFi Orders
Completed orders can be accessed as COGs:
1. In the **Orders** tab, find a completed order
2. Tap the menu (three dots) → **View as COG**
3. The imagery streams directly without full download

#### From External Sources
1. Tap **Test COG** button (or **SkyFi** → **Add COG Layer**)
2. Enter COG details:
   - **Name**: Display name for the layer
   - **URL**: Direct HTTPS link to the COG file
   - **Bounds**: Geographic extent (auto-detected if possible)
3. Tap **Add**

### COG Performance Tips
- Works best on WiFi or 4G/5G connections
- Tiles are cached locally for offline use
- Zoom in gradually for faster loading
- Clear cache periodically in settings

---

## Collaboration Features

### Team Sharing

#### Real-time AOI Sharing
AOIs are automatically shared with team members via CoT when:
1. Team sharing is enabled in settings
2. Team members are connected to the same network/server
3. Multicast or TAK Server is configured

#### Order Status Broadcasting
Your team can see:
- When you submit an order
- Order status updates
- When imagery becomes available

### Export Options

#### KML/KMZ Export
1. Select one or more orders/AOIs
2. Tap **Export** → **KML**
3. Choose inclusion options:
   - Imagery (increases file size)
   - Metadata only
   - Style information
4. Share via ATAK's share mechanism

#### GeoTIFF Export
1. From a completed order, tap **Export**
2. Select **GeoTIFF**
3. Choose resolution and compression
4. File saves to `ATAK/exports/skyfi/`

---

## Troubleshooting

### Common Issues and Solutions

#### Cannot Connect to SkyFi API
**Symptoms**: Red X on connection status, "Connection failed" message

**Solutions**:
1. Verify internet connectivity
2. Check API credentials are correct
3. Ensure API URL is properly formatted (https://...)
4. Try disconnecting and reconnecting
5. Check firewall/proxy settings if on military network

#### Orders Not Appearing
**Symptoms**: Submitted orders don't show in Orders tab

**Solutions**:
1. Pull down to refresh the orders list
2. Check filter settings (may be hiding certain order types)
3. Verify you're logged into correct account
4. Check network connectivity

#### Imagery Not Loading
**Symptoms**: Downloaded imagery doesn't appear on map

**Solutions**:
1. Check ATAK's Layer Manager - ensure SkyFi layers are enabled
2. Verify download completed successfully
3. Check available storage space
4. Clear app cache and re-download
5. Restart ATAK

#### COG Tiles Not Loading
**Symptoms**: COG layers show blank/transparent tiles

**Solutions**:
1. Verify COG URL is accessible (test in browser)
2. Check internet connectivity
3. Ensure COG is properly formatted (use gdalinfo to verify)
4. Try a known working COG URL for testing
5. Check logs: `adb logcat | grep COG`

### Performance Optimization

#### Reduce Memory Usage
1. Limit cached imagery to essential areas
2. Use lower resolution for overview planning
3. Clear cache regularly: **Settings** → **Clear Cache**
4. Disable auto-download for large orders

#### Improve Load Times
1. Pre-download imagery while on WiFi
2. Use COG streaming instead of full downloads
3. Reduce overlay opacity for faster rendering
4. Disable unnecessary map layers

### Error Messages

#### "Area too large for selected sensor"
- Maximum area varies by sensor type
- Split large areas into multiple smaller AOIs
- Consider using lower resolution sensors for large areas

#### "Insufficient credits"
- Check account balance in Profile tab
- Contact SkyFi support to purchase credits
- Consider using archive imagery (often cheaper)

#### "No imagery available for selected criteria"
- Expand date range
- Increase acceptable cloud cover
- Try different sensor types
- Check if area has imaging restrictions

---

## FAQ

### General Questions

**Q: How much does satellite imagery cost?**
A: Pricing varies based on:
- Area size (km²)
- Sensor type and resolution
- Priority level
- Archive vs. new tasking
Typical range: $50-500 per order

**Q: How long does it take to receive imagery?**
A: 
- Archive imagery: Usually within hours
- Standard tasking: 7-14 days
- Priority tasking: 3-7 days
- Urgent tasking: 24-72 hours (if available)

**Q: What's the minimum area I can order?**
A: Minimum order size is typically 25 km² for tasking orders. Archive imagery may have different minimums.

**Q: Can I order imagery for restricted areas?**
A: Some areas have legal restrictions. The system will notify you if your AOI includes restricted zones.

### Technical Questions

**Q: How much storage do I need?**
A: 
- Single optical image (full res): 100-500 MB
- Single SAR image: 200-800 MB
- Recommended free space: 5+ GB

**Q: Does the plugin work offline?**
A: Yes, for viewing cached/downloaded imagery. Internet required for:
- Ordering new imagery
- Downloading completed orders
- Streaming COG tiles

**Q: Can I use my own satellite imagery?**
A: Yes, if formatted as:
- GeoTIFF with proper georeferencing
- Cloud Optimized GeoTIFF (COG)
- KML/KMZ with embedded imagery

**Q: How do I update the plugin?**
A: 
1. Download new version
2. Install over existing (settings preserved)
3. Restart ATAK

### Account Questions

**Q: How do I get a SkyFi account?**
A: Visit https://skyfi.com or contact sales@skyfi.com

**Q: Can I share my account with team members?**
A: Each user needs their own account for tracking and billing. Team/organization accounts available.

**Q: What happens if my credits run out?**
A: You can still view previously downloaded imagery but cannot place new orders until credits are added.

---

## Support

### Getting Help

#### In-App Support
- Tap **Profile** → **Help & Support**
- Access documentation and video tutorials
- Submit support tickets

#### Contact Information
- **Email**: support@skyfi.com
- **Phone**: +1-555-SKYFI-00
- **TAK.gov Forums**: Search "SkyFi" for community support

#### Logs for Support
When reporting issues, provide:
1. Order ID (if applicable)
2. Screenshot of error
3. Device logs:
```bash
adb logcat -d > skyfi_logs.txt
```
4. ATAK version and plugin version
5. Device model and Android version

### Feature Requests
Submit feature requests via:
- In-app feedback form
- GitHub issues: https://github.com/skyfi/atak-plugin/issues
- Email: features@skyfi.com

---

## Appendix

### Glossary

**AOI**: Area of Interest - geographic region for imagery collection

**CoT**: Cursor on Target - military messaging protocol for real-time information sharing

**COG**: Cloud Optimized GeoTIFF - streamable imagery format

**Off-nadir**: Viewing angle of satellite (0° = straight down)

**Orthorectified**: Imagery corrected for terrain displacement

**Pan-sharpened**: Combined panchromatic and multispectral for color and detail

**SAR**: Synthetic Aperture Radar - radar-based imaging system

**Tasking**: Scheduling a satellite to capture new imagery

### Satellite Specifications

#### Optical Satellites
- **WorldView-3**: 30cm resolution, 8-band multispectral
- **WorldView-2**: 46cm resolution, 8-band multispectral  
- **Pleiades**: 50cm resolution, 4-band multispectral
- **SkySat**: 50cm resolution, video capability
- **PlanetScope**: 3m resolution, daily revisit

#### SAR Satellites
- **TerraSAR-X**: 25cm resolution, X-band
- **RADARSAT-2**: 1m resolution, C-band
- **Sentinel-1**: 5m resolution, C-band, free data
- **ICEYE**: 50cm resolution, X-band, constellation

### File Formats

**Supported Import Formats:**
- GeoTIFF (.tif, .tiff)
- Cloud Optimized GeoTIFF (.tif)
- JPEG2000 (.jp2)
- KML/KMZ with imagery
- MrSID (.sid)

**Export Formats:**
- GeoTIFF (georeferenced)
- KML/KMZ (Google Earth compatible)
- GeoPackage (.gpkg)
- PNG (with world file)

---

*SkyFi ATAK Plugin User Manual v2.0-beta3*
*Last Updated: August 2024*
*© 2024 SkyFi (Optisense Inc.)*