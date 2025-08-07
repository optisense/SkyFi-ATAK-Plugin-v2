# "From Existing Shape" Feature Implementation

## Overview

This implementation adds a powerful "From Existing Shape" feature to the SkyFi ATAK plugin, allowing users to tap on any existing shape on the map and convert it into an AOI (Area of Interest) for satellite tasking.

## Architecture

The feature consists of four main components:

### 1. ShapeSelectionTool.java
**Purpose**: Main tool for entering shape selection mode and handling user interactions
- Implements ATAK's Tool interface for proper integration
- Listens for map clicks and item clicks
- Validates selected shapes and extracts geometry
- Provides callbacks for shape selection and cancellation

### 2. ShapeGeometryExtractor.java  
**Purpose**: Handles extraction of coordinates from various ATAK shape types
- Supports DrawingCircle, DrawingRectangle, DrawingShape (polygons/freehand)
- Supports generic Shape objects and Polylines
- Converts circles to polygon approximations
- Calculates accurate areas using shoelace formula
- Handles edge cases and validation

### 3. ShapeSelectionFeedback.java
**Purpose**: Provides visual feedback and user guidance
- Shows instruction overlays during selection mode
- Highlights shapes when hovered/selected
- Displays progress messages and warnings
- Provides area validation feedback
- Handles cleanup of visual elements

### 4. Integration with Existing Classes
**SkyFiPlugin.java**: Main integration point
- Initializes ShapeSelectionTool in onStart()
- Adds "From Existing Shape" option to AOI creation menu
- Handles shape selection workflow and AOI creation dialogs
- Cleanup in onStop()

**SkyFiRadialMenuReceiver.java**: Enhanced radial menu
- Adds "Convert to AOI (Advanced)" option to shape context menus
- Uses ShapeGeometryExtractor for advanced shape processing
- Provides alternative path for shape conversion

## Supported Shape Types

### Primary Support (Full Feature Set)
- **DrawingCircle**: Converted to polygon approximation with accurate area calculation
- **DrawingRectangle**: Direct coordinate extraction 
- **DrawingShape**: Polygons, freehand drawings, and other drawing shapes
- **Generic Shape**: Standard ATAK shape objects

### Secondary Support (Basic Conversion)
- **Polyline**: Converted to polygon if closed (first/last points within 50m)
- **Generic MapItem**: Fallback to point-based AOI around center

## User Workflows

### Method 1: "From Existing Shape" Menu Option
1. User opens SkyFi plugin dashboard
2. Clicks "Create New AOI" → "From Existing Shape"
3. Plugin closes dashboard and enters shape selection mode
4. Visual instructions appear: "Tap any shape to convert to AOI"
5. User taps on desired existing shape
6. Plugin extracts geometry and validates area
7. AOI naming dialog appears with shape information
8. User creates AOI and optionally creates tasking order

### Method 2: Shape Context Menu (Radial Menu)
1. User right-clicks on any existing shape
2. SkyFi options menu appears
3. User selects "Convert to AOI (Advanced)"
4. Plugin uses advanced geometry extraction
5. Area validation and AOI creation dialog
6. Direct integration with tasking workflow

## Technical Implementation Details

### Shape Detection and Validation
```java
// Validates if a MapItem is a supported shape type
private boolean isValidShape(MapItem item) {
    if (item instanceof Shape) return true;
    
    String itemType = item.getType();
    return itemType != null && (
        itemType.contains("polygon") ||
        itemType.contains("circle") ||
        itemType.contains("rectangle") ||
        itemType.contains("drawing") ||
        itemType.contains("freehand")
    );
}
```

### Geometry Extraction Process
1. **Type Identification**: Determines specific shape type
2. **Coordinate Extraction**: Gets points using appropriate method for each type
3. **Circle Conversion**: Creates 32-sided polygon approximation for circles
4. **Area Calculation**: Uses shoelace formula for accurate area calculation
5. **Validation**: Checks minimum area requirements and point count

### Visual Feedback System
- **Selection Mode Instructions**: Persistent overlay showing available actions
- **Shape Highlighting**: Temporarily changes stroke color/width when hovering
- **Selection Confirmation**: Brief flash effect when shape is selected
- **Error Messages**: Clear feedback for unsupported shapes or errors
- **Progress Updates**: Status messages during processing

## Integration Points

### ATAK Tool System
```java
// Registers as official ATAK tool
Intent startToolIntent = new Intent(ToolManagerBroadcastReceiver.BEGIN_TOOL);
startToolIntent.putExtra("tool", TOOL_IDENTIFIER);
AtakBroadcast.getInstance().sendBroadcast(startToolIntent);
```

### Map Event Handling
```java
// Listens for both item clicks and empty map clicks
mapView.getMapEventDispatcher().addMapEventListener(MapEvent.MAP_CLICK, this);
mapView.getMapEventDispatcher().addMapEventListener(MapEvent.ITEM_CLICK, this);
```

### AOI Management Integration
```java
// Creates AOI using existing AOIManager
AOIManager.AOI aoi = aoiManager.createAOI(aoiName, points, areaSqKm, "from_shape");
```

## Error Handling

### Validation Checks
- **Shape Type Validation**: Ensures selected item is a supported shape
- **Geometry Extraction**: Handles cases where coordinates cannot be extracted
- **Area Validation**: Warns about shapes below minimum area requirements
- **Point Count**: Ensures minimum of 3 points for polygon AOIs

### User Feedback
- **Unsupported Shapes**: Clear message explaining why shape cannot be used
- **Extraction Failures**: Specific error messages for geometry issues
- **Area Warnings**: Option to proceed with small areas or cancel
- **System Errors**: Graceful handling with user-friendly error messages

## Performance Considerations

### Efficient Shape Processing
- **Lazy Initialization**: ShapeGeometryExtractor created only when needed
- **Optimized Calculations**: Shoelace formula for O(n) area calculation
- **Memory Management**: Proper cleanup of visual feedback elements
- **Event Listener Cleanup**: Removes map event listeners when tool ends

### Visual Performance
- **Minimal UI Updates**: Only necessary visual changes during selection
- **Toast Management**: Prevents toast overflow with proper cancellation
- **Highlighting Optimization**: Reuses original stroke colors when possible

## Testing Recommendations

### Shape Type Testing
1. **Drawing Shapes**: Test with ATAK's built-in polygon tool
2. **Circles**: Test with circle drawing tool 
3. **Rectangles**: Test with rectangle drawing tool
4. **Freehand**: Test with freehand drawing tool
5. **Mixed Types**: Test selection among multiple shape types

### Workflow Testing
1. **Normal Flow**: Complete AOI creation from various shape types
2. **Cancellation**: Test canceling at various points
3. **Error Cases**: Test with invalid or unsupported items
4. **Area Warnings**: Test with very small shapes
5. **Multiple Selections**: Test rapid selection/cancellation cycles

### Integration Testing
1. **Plugin Lifecycle**: Test during plugin start/stop cycles
2. **Memory Leaks**: Verify proper cleanup of listeners and visuals
3. **ATAK Integration**: Ensure proper tool registration/unregistration
4. **Concurrent Usage**: Test with other ATAK tools active

## Usage Tips for Users

### Best Practices
- **Shape Visibility**: Ensure target shapes are clearly visible on map
- **Zoom Level**: Use appropriate zoom level for accurate selection
- **Shape Quality**: Better defined shapes produce more accurate AOIs
- **Area Awareness**: Check that shapes meet minimum area requirements

### Troubleshooting
- **Selection Issues**: Try tapping directly on shape outline/fill
- **Small Shapes**: Zoom in for better selection accuracy
- **Cancellation**: Tap empty map area to cancel selection mode
- **Visual Feedback**: Look for instruction overlays and shape highlighting

## Future Enhancement Opportunities

### Advanced Features
- **Multi-Shape Selection**: Select multiple shapes to create combined AOI
- **Shape Editing**: Allow geometry modification before AOI creation
- **Batch Processing**: Convert multiple shapes to AOIs simultaneously
- **Smart Filtering**: Filter visible shapes by type or properties

### UI Enhancements
- **Shape Preview**: Show shape outline during selection mode
- **Area Visualization**: Real-time area display during hover
- **Selection History**: Remember recently selected shapes
- **Keyboard Shortcuts**: Hotkeys for common operations

This implementation provides a robust, user-friendly system for converting existing ATAK shapes into SkyFi AOIs while maintaining full integration with the existing plugin architecture and ATAK's native systems.