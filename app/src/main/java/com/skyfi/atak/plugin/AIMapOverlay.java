package com.skyfi.atak.plugin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;

import com.atakmap.android.hierarchy.HierarchyListFilter;
import com.atakmap.android.hierarchy.HierarchyListItem;
import com.atakmap.android.hierarchy.action.Visibility;
import com.atakmap.android.hierarchy.items.AbstractHierarchyListItem2;
import com.atakmap.android.maps.DeepMapItemQuery;
import com.atakmap.android.maps.DefaultMapGroup;
import com.atakmap.android.maps.MapGroup;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.overlay.AbstractMapOverlay2;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoBounds;
import com.atakmap.coremap.maps.coords.GeoPoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * AI-powered map overlay that integrates with ATAK's overlay manager
 * Provides toggleable AI analysis layers with proper ATAK integration
 */
public class AIMapOverlay extends AbstractMapOverlay2 {
    
    private static final String TAG = "AIMapOverlay";
    
    private final MapView mapView;
    private final Context pluginContext;
    private final DefaultMapGroup aiGroup;
    private final AIOverlayDeepMapItemQuery query;
    private AIListModel listModel;
    private AIOverlaySystem aiOverlaySystem;
    
    public AIMapOverlay(MapView mapView, Context pluginContext) {
        this.mapView = mapView;
        this.pluginContext = pluginContext;
        this.query = new AIOverlayDeepMapItemQuery();
        this.aiGroup = new DefaultMapGroup("AI Analysis");
        this.aiGroup.setMetaBoolean("addToObjList", false);
        
        // Initialize the AI overlay system
        this.aiOverlaySystem = new AIOverlaySystem(pluginContext, mapView);
        
        Log.d(TAG, "AI Map Overlay initialized");
    }
    
    @Override
    public String getIdentifier() {
        return TAG;
    }
    
    @Override
    public String getName() {
        return "AI Analysis";
    }
    
    @Override
    public MapGroup getRootGroup() {
        return aiGroup;
    }
    
    @Override
    public DeepMapItemQuery getQueryFunction() {
        return query;
    }
    
    @Override
    public HierarchyListItem getListModel(BaseAdapter adapter, long capabilities, HierarchyListFilter prefFilter) {
        if (listModel == null) {
            listModel = new AIListModel();
        }
        listModel.refresh(adapter, prefFilter);
        return listModel;
    }
    
    public AIOverlaySystem getAIOverlaySystem() {
        return aiOverlaySystem;
    }
    
    /**
     * AI Overlay List Model for the Overlay Manager
     */
    public class AIListModel extends AbstractHierarchyListItem2 implements Visibility {
        
        private View headerView;
        
        public AIListModel() {
            this.asyncRefresh = true;
        }
        
        @Override
        public String getTitle() {
            return AIMapOverlay.this.getName();
        }
        
        @Override
        public String getIconUri() {
            return "android.resource://" + pluginContext.getPackageName() + "/" + R.drawable.ic_ai_brain;
        }
        
        @Override
        public int getPreferredListIndex() {
            return 3; // Show after main overlays
        }
        
        @Override
        public int getDescendantCount() {
            return AIOverlaySystem.AILayerType.values().length;
        }
        
        @Override
        public Object getUserObject() {
            return this;
        }
        
        @Override
        public View getExtraView() {
            return null;
        }
        
        @Override
        public View getHeaderView() {
            if (headerView == null) {
                headerView = LayoutInflater.from(pluginContext)
                    .inflate(android.R.layout.simple_list_item_1, mapView, false);
            }
            return headerView;
        }
        
        @Override
        public View getFooterView() {
            return null;
        }
        
        @Override
        public void refreshImpl() {
            List<HierarchyListItem> filtered = new ArrayList<>();
            
            // Add each AI layer type as a separate item
            for (AIOverlaySystem.AILayerType layerType : AIOverlaySystem.AILayerType.values()) {
                AILayerHierarchyListItem item = new AILayerHierarchyListItem(layerType);
                if (this.filter.accept(item)) {
                    filtered.add(item);
                }
            }
            
            // Sort items
            sortItems(filtered);
            
            // Update children
            updateChildren(filtered);
        }
        
        @Override
        public void dispose() {
            disposeChildren();
        }
        
        @Override
        public boolean hideIfEmpty() {
            return false;
        }
        
        @Override
        public boolean isMultiSelectSupported() {
            return true;
        }
        
        @Override
        public boolean setVisible(boolean visible) {
            // Toggle all AI layers
            List<Visibility> actions = getChildActions(Visibility.class);
            boolean success = true;
            for (Visibility vis : actions) {
                success &= vis.setVisible(visible);
            }
            return success;
        }
        
        @Override
        public boolean isVisible() {
            // Return true if any AI layer is visible
            for (AIOverlaySystem.AILayerType layerType : AIOverlaySystem.AILayerType.values()) {
                if (aiOverlaySystem.isLayerVisible(layerType)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    /**
     * Individual AI Layer hierarchy item
     */
    private class AILayerHierarchyListItem extends AbstractHierarchyListItem2 implements Visibility {
        
        private final AIOverlaySystem.AILayerType layerType;
        
        AILayerHierarchyListItem(AIOverlaySystem.AILayerType layerType) {
            this.layerType = layerType;
        }
        
        @Override
        public String getTitle() {
            return layerType.displayName;
        }
        
        @Override
        public String getDescription() {
            return layerType.description + " (" + aiOverlaySystem.getLayerItemCount(layerType) + " items)";
        }
        
        @Override
        public String getIconUri() {
            return "android.resource://" + pluginContext.getPackageName() + "/" + R.drawable.ic_layers;
        }
        
        @Override
        public Object getUserObject() {
            return layerType;
        }
        
        @Override
        public boolean isChildSupported() {
            return false;
        }
        
        @Override
        public int getDescendantCount() {
            return 0;
        }
        
        @Override
        public void refreshImpl() {
            // No children to refresh
        }
        
        @Override
        public boolean hideIfEmpty() {
            return false;
        }
        
        @Override
        public boolean setVisible(boolean visible) {
            aiOverlaySystem.setLayerVisible(layerType, visible);
            return true;
        }
        
        @Override
        public boolean isVisible() {
            return aiOverlaySystem.isLayerVisible(layerType);
        }
    }
    
    /**
     * Deep map item query implementation for AI overlay
     */
    private class AIOverlayDeepMapItemQuery implements DeepMapItemQuery {
        
        @Override
        public MapItem deepFindItem(Map<String, String> metadata) {
            return null;
        }
        
        @Override
        public List<MapItem> deepFindItems(Map<String, String> metadata) {
            return new ArrayList<>();
        }
        
        @Override
        public MapItem deepFindClosestItem(GeoPoint location, double threshold, Map<String, String> metadata) {
            return null;
        }
        
        @Override
        public Collection<MapItem> deepFindItems(GeoPoint location, double radius, Map<String, String> metadata) {
            List<MapItem> items = new ArrayList<>();
            
            // Search through AI overlay items
            for (MapItem item : aiGroup.getItems()) {
                if (item instanceof Marker) {
                    Marker marker = (Marker) item;
                    if (marker.getPoint().distanceTo(location) <= radius) {
                        items.add(marker);
                    }
                }
            }
            
            return items;
        }
        
        @Override
        public MapItem deepHitTest(int xpos, int ypos, GeoPoint point, MapView view) {
            // Check if any AI overlay items are at this location
            for (MapItem item : aiGroup.getItems()) {
                if (item instanceof Marker) {
                    Marker marker = (Marker) item;
                    if (marker.getPoint().distanceTo(point) < 100) { // 100m tolerance
                        return marker;
                    }
                }
            }
            return null;
        }
        
        @Override
        public SortedSet<MapItem> deepHitTestItems(int xpos, int ypos, GeoPoint point, MapView view) {
            SortedSet<MapItem> items = new TreeSet<>(MapItem.ZORDER_HITTEST_COMPARATOR);
            
            for (MapItem item : aiGroup.getItems()) {
                if (item instanceof Marker) {
                    Marker marker = (Marker) item;
                    if (marker.getPoint().distanceTo(point) < 100) { // 100m tolerance
                        items.add(marker);
                    }
                }
            }
            
            return items;
        }
        
        @Override
        public Collection<MapItem> deepFindItems(GeoBounds bounds, Map<String, String> metadata) {
            List<MapItem> items = new ArrayList<>();
            
            for (MapItem item : aiGroup.getItems()) {
                if (item instanceof Marker) {
                    Marker marker = (Marker) item;
                    if (bounds.contains(marker.getPoint())) {
                        items.add(marker);
                    }
                }
            }
            
            return items;
        }
    }
    
    public void dispose() {
        if (aiOverlaySystem != null) {
            aiOverlaySystem.dispose();
        }
    }
}