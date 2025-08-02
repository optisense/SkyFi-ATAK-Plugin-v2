# SkyFi ATAK Plugin - Preview Functionality Implementation

## Overview
This document describes the implementation of the minimum preview functionality for point selection in the SkyFi ATAK Plugin v2, as requested in the TAK v1 feedback.

## Features Implemented

### 1. Point Selection Preview
- **Map Click Interception**: Added map click event handling to show imagery previews when users tap on the map
- **Toggle Mode**: Users can enable/disable preview mode through the main plugin menu
- **Non-Intrusive Design**: Small popup overlay that doesn't interfere with normal ATAK operations

### 2. Quick Archive Search
- **Automatic AOI Creation**: Creates a 5km radius search area around the clicked point
- **Fast API Query**: Uses basic filters (low cloud coverage, exclude open data) for faster response
- **Limited Results**: Shows up to 6 most recent thumbnails for quick preview

### 3. Preview UI Components
- **Minimal Popup**: Compact 300dp wide popup with elevation for visibility
- **Thumbnail Grid**: 3-column grid showing small 72dp square thumbnails
- **Archive Details**: Provider name and capture date for each thumbnail
- **Action Buttons**: "Full Search" and "Place Order" buttons for workflow integration

### 4. Integration with Existing Workflow
- **Full Search**: Seamlessly launches the existing ArchiveSearch.java with pre-populated AOI
- **Order Placement**: Direct ordering from preview using existing ArchivesBrowser.java patterns
- **Thumbnail Details**: Click on thumbnails to see additional archive information

## Files Created/Modified

### New Files Created:
1. **`ImageryPreviewManager.java`** - Main preview functionality manager
   - Handles map click events
   - Manages preview popup display
   - Performs quick archive searches
   - Integrates with existing order workflow

2. **`PreviewThumbnailAdapter.java`** - RecyclerView adapter for thumbnail grid
   - Displays archive thumbnails in compact format
   - Uses same image loading pattern as existing code
   - Handles thumbnail click events

3. **`image_preview_popup.xml`** - Layout for preview popup
   - Loading indicator
   - Location display
   - Thumbnail grid
   - Action buttons

4. **`preview_thumbnail_item.xml`** - Layout for individual thumbnail items
   - 72dp square image
   - Provider and date text
   - Compact design

### Modified Files:
1. **`SkyFiPlugin.java`** - Main plugin file
   - Added ImageryPreviewManager initialization
   - Added preview mode toggle menu item
   - Added cleanup on plugin stop

2. **`strings.xml`** - Added new string resources
   - Preview mode UI text
   - Toggle button labels
   - Status messages

## User Workflow

### Enabling Preview Mode:
1. Open SkyFi plugin menu
2. Tap "Enable Preview Mode"
3. Plugin shows confirmation toast

### Using Preview:
1. Tap anywhere on the map while preview mode is enabled
2. Small popup appears with loading indicator
3. After search completes, popup shows:
   - Location coordinates
   - Up to 6 thumbnail images with provider/date
   - "Full Search" button to open detailed search
   - "Place Order" button (if imagery available)

### From Preview to Full Workflow:
- **Full Search**: Opens ArchiveSearch.java with pre-populated 5km AOI around clicked point
- **Place Order**: Directly orders the most recent available archive
- **Thumbnail Click**: Shows detailed archive information with order option

## Technical Implementation Details

### Map Event Handling:
- Uses ATAK's MapEventDispatcher.MapEventDispatchListener
- Listens for MapEvent.MAP_CLICK events
- Converts screen coordinates to geo coordinates using mapView.inverse()

### Archive Search:
- Creates square AOI using JTS geometry library (same as existing code)
- Uses ArchivesRequest with basic filters for speed
- Sorts results by capture date (newest first)
- Limits to 6 results for compact display

### Image Loading:
- Uses same pattern as ArchivesBrowserRecyclerViewAdapter
- Background thread loading with URLConnection and BitmapFactory
- Placeholder images for failed loads
- Proper memory management

### UI Design:
- PopupWindow for non-modal display
- Proper positioning to avoid screen edges
- Dark theme matching existing plugin style
- Elevation for visual separation from map

## Integration Points

### With Existing Archive Search:
- Passes AOI string to ArchiveSearch.ACTION intent
- Maintains same search parameters format
- Allows users to refine search criteria

### With Existing Order System:
- Uses same ArchiveOrder API calls
- Same error handling patterns
- Consistent user feedback messages

### With Existing UI Patterns:
- Same button styles and colors
- Consistent typography and spacing
- Same loading indicators and progress patterns

## Performance Considerations

1. **Limited Search Scope**: 5km radius prevents overly broad searches
2. **Basic Filters**: Only essential filters applied for speed
3. **Result Limiting**: Maximum 6 thumbnails to keep popup compact
4. **Background Loading**: Image loading doesn't block UI thread
5. **Proper Cleanup**: Event listeners removed when preview mode disabled

## Error Handling

- Network failures show "No imagery available" message
- Image loading failures show placeholder images
- API errors display user-friendly error messages
- Popup automatically closes on errors

## Future Enhancement Opportunities

1. **Caching**: Add thumbnail caching for faster repeated views
2. **Filter Options**: Add quick filter toggles in preview popup
3. **Preview Size**: Allow user to adjust preview search radius
4. **Animation**: Add smooth transitions for popup appearance
5. **Gesture Support**: Add long-press for preview mode toggle

## Testing Recommendations

1. Test map clicking in various zoom levels
2. Verify popup positioning at screen edges
3. Test with slow network connections
4. Verify proper cleanup when plugin stops
5. Test integration with existing search and order workflows