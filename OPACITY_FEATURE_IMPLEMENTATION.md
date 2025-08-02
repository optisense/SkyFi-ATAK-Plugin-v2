# SAR Imagery Opacity Control Implementation

## Overview
This implementation adds opacity slider controls for SAR imagery overlays in the SkyFi ATAK Plugin. The feature allows users to adjust the transparency of SAR imagery to better see underlying map features, which is especially useful for orientation and terrain feature identification.

## Implementation Details

### 1. UI Components Added

#### Order Row Layout (`order_row.xml`)
- Added an opacity control section that appears only for SAR imagery orders
- Includes:
  - Inline SeekBar for quick opacity adjustment (0-100%)
  - Percentage display showing current opacity value
  - Advanced settings button (⚙) for opening detailed opacity dialog

#### Opacity Control Dialog (`opacity_control_dialog.xml`)
- Already existed - provides advanced opacity control with visual feedback
- Shows descriptive text (e.g., "Semi-Transparent", "Nearly Opaque")
- Allows fine-grained opacity adjustment

### 2. Backend Components

#### Preferences Enhancement (`Preferences.java`)
- Added layer-specific opacity storage: `PREF_LAYER_OPACITY_PREFIX + layerName`
- Default opacity set to 80% for optimal SAR imagery visibility
- Methods:
  - `getLayerOpacity(String layerName)` - retrieve saved opacity
  - `setLayerOpacity(String layerName, int opacity)` - save opacity setting

#### Adapter Updates (`OrdersRecyclerViewAdapter.java`)
- Added SAR imagery detection via `isSarImagery(Order order)`
- Shows opacity controls only for SAR imagery with active layers
- Real-time opacity adjustment with immediate feedback
- Integration with advanced opacity dialog

#### Layer Management (`Orders.java`)
- Implements `OpacityChangeListener` interface
- Applies opacity changes via ATAK broadcast system: `com.atakmap.android.layers.SET_LAYER_ALPHA`
- Stores layer URI mappings for opacity control
- Automatically applies saved opacity when layers are loaded

### 3. SAR Imagery Detection

The system detects SAR imagery by checking if `order.getSarProductTypes()` is not null and contains elements. This ensures opacity controls only appear for relevant imagery types.

### 4. ATAK Integration

#### Layer Opacity Control
```java
Intent opacityIntent = new Intent();
opacityIntent.setAction("com.atakmap.android.layers.SET_LAYER_ALPHA");
opacityIntent.putExtra(EXTRA_LAYER_NAME, layerName);
opacityIntent.putExtra("alpha", opacity / 100.0f);
AtakBroadcast.getInstance().sendBroadcast(opacityIntent);
```

#### Timing Considerations
- Layer selection happens 5 seconds after import to avoid race conditions
- Opacity application happens 2 seconds after layer selection to ensure layer is fully loaded
- This prevents timing issues that could cause opacity settings to be ignored

### 5. User Experience Features

#### Visual Feedback
- Real-time percentage display during slider adjustment
- Immediate opacity changes in the map view
- Advanced dialog with descriptive opacity levels

#### Persistence
- Opacity settings are saved per layer in ATAK preferences
- Settings persist across app sessions and restarts
- Each SkyFi layer maintains independent opacity settings

## Usage Flow

1. **Order Display**: User views completed SAR imagery orders in the Orders list
2. **Opacity Control**: For SAR imagery, an opacity slider appears below the order details
3. **Quick Adjustment**: User can drag the slider for immediate opacity changes (0-100%)
4. **Advanced Settings**: Clicking the ⚙ button opens the advanced opacity dialog
5. **Persistence**: All opacity changes are automatically saved and restored when the layer is reloaded

## Technical Benefits

- **Performance**: Minimal UI overhead - controls only appear for SAR imagery
- **Integration**: Uses ATAK's native layer management system
- **Reliability**: Proper timing mechanisms prevent race conditions
- **Extensibility**: Framework can be extended to other imagery types
- **User-Friendly**: Intuitive controls with immediate visual feedback

## Files Modified

1. `/app/src/main/res/layout/order_row.xml` - Added opacity control UI
2. `/app/src/main/java/com/skyfi/atak/plugin/Preferences.java` - Added opacity preference management
3. `/app/src/main/java/com/skyfi/atak/plugin/OrdersRecyclerViewAdapter.java` - Added opacity control logic
4. `/app/src/main/java/com/skyfi/atak/plugin/Orders.java` - Added layer opacity management

## Testing Recommendations

1. **SAR Imagery Verification**: Test with known SAR orders to ensure controls appear
2. **Opacity Application**: Verify opacity changes are immediately visible on the map
3. **Persistence Testing**: Restart app and confirm opacity settings are restored
4. **Non-SAR Imagery**: Confirm opacity controls don't appear for optical imagery
5. **Advanced Dialog**: Test the advanced opacity dialog functionality

This implementation provides a robust, user-friendly opacity control system specifically designed for SAR imagery in ATAK, improving situational awareness and terrain feature identification capabilities.