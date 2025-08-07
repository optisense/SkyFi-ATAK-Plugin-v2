package com.atakmap.android.videocollections.plugin.ui;

import android.content.Context;
import android.content.Intent;

import com.atakmap.android.action.MapAction;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapDataRef;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.assets.MapAssets;
import com.atakmap.android.menu.MapMenuButtonWidget;
import com.atakmap.android.menu.MapMenuFactory;
import com.atakmap.android.menu.MapMenuWidget;
import com.atakmap.android.menu.MenuMapAdapter;
import com.atakmap.android.menu.MenuResourceFactory;
import com.atakmap.android.menu.PluginMenuParser;
import com.atakmap.android.videocollections.plugin.data.VideoCollection;
import com.atakmap.android.videocollections.plugin.data.VideoCollectionList;
import com.atakmap.android.widgets.MapWidget;
import com.atakmap.android.widgets.WidgetIcon;

import java.io.IOException;

public class VideoMenuFactory implements MapMenuFactory {
    private final Context atakCtx, pluginCtx;
    private final VideoCollectionsInfoPane infoPane;
    private final MenuResourceFactory resourceFactory;
    private static final String videoIconUri = "asset://icons/video.png";

    public VideoMenuFactory(Context pluginCtx, VideoCollectionsInfoPane infoPane) {
        MapView mapView = MapView.getMapView();
        this.atakCtx = mapView.getContext();
        this.pluginCtx = pluginCtx;
        this.infoPane = infoPane;

        final MapAssets mapAssets = new MapAssets(atakCtx);
        final MenuMapAdapter adapter = new MenuMapAdapter();
        try { adapter.loadMenuFilters(mapAssets, "filters/menu_filters.xml"); }
        catch (IOException ignored) {}

        this.resourceFactory = new MenuResourceFactory(mapView, mapView.getMapData(), mapAssets, adapter);
    }

    @Override
    public MapMenuWidget create(MapItem item) {
        final MapMenuWidget menuWidget = resourceFactory.create(item);
        final MapMenuButtonWidget videoButton = getVideoButton(menuWidget);

        if (videoButton != null) {
            VideoCollectionList vcList = VideoCollectionList.fromMapItem(item);

            if (vcList == null)
                return null;

            videoButton.setSubmenuWidget(buildVideoSubmenu(item, vcList, videoButton.getOrientationRadius()));
            if (videoButton.isDisabled()) {
                videoButton.setOnClickAction(new MapAction() {
                    @Override
                    public void performAction(MapView mapView, MapItem item) {
                        if (vcList.isEmpty()) {
                            infoPane.show(item);
                            sendHideIntents();
                            return;
                        }

                        VideoCollection vc = vcList.getActive();
                        if (vc == null)
                            vc = vcList.get(0);

                        vc.display();
                        sendHideIntents();
                    }
                });
                videoButton.setDisabled(false);
            }
            return menuWidget;
        }
        return null;
    }

    private MapMenuButtonWidget getVideoButton(MapMenuWidget menuWidget) {
        if (menuWidget == null) return null;
        for (MapWidget child : menuWidget.getChildWidgets()) {
            if (child instanceof MapMenuButtonWidget) {
                MapMenuButtonWidget buttonWidget = (MapMenuButtonWidget) child;
                WidgetIcon icon = buttonWidget.getIcon();
                if (icon == null) continue;
                MapDataRef iconRef = icon.getIconRef(0);
                if (iconRef == null) continue;
                if (iconRef.toUri().contentEquals(videoIconUri))
                    return buttonWidget;
            }
        }
        return null;
    }

    private MapMenuWidget buildVideoSubmenu(MapItem item, VideoCollectionList vcList, float radius) {
        MapMenuWidget subMenu = new MapMenuWidget();
        MapAction showInfoAction = buildShowInfoAction(item);
        subMenu.addWidget(buildMenuButton("icons/info.png", showInfoAction, radius));
        for (int i = 0; i < vcList.size(); i++) {
            if (i == 5) {
                subMenu.addWidget(buildMenuButton("icons/more.png", showInfoAction, radius));
                break;
            }
            subMenu.addWidget(buildMenuButton(
                    "icons/video_" + (i + 1) + ".png",
                    buildDisplayVideoAction(vcList, i),
                    radius));
        }

        return subMenu;
    }

    private MapMenuButtonWidget buildMenuButton(String iconPath, MapAction action, float radius) {
        MapMenuButtonWidget button = new MapMenuButtonWidget(atakCtx);
        button.setOnClickAction(action);
        button.setIcon(buildWidgetIcon(iconPath));
        button.setOrientation(button.getOrientationAngle(), radius);
        return button;
    }

    private WidgetIcon buildWidgetIcon(String path) {
        String asset = PluginMenuParser.getItem(pluginCtx, path);
        if (asset.length() == 0) asset = "asset:///" + path;
        return new WidgetIcon.Builder()
                .setImageRef(0, MapDataRef.parseUri(asset))
                .setAnchor(16, 16)
                .setSize(32, 32)
                .build();
    }

    private MapAction buildShowInfoAction(MapItem item) {
        return new MapAction() {
            @Override
            public void performAction(MapView mapView, MapItem mapItem) {
                infoPane.show(item);
                sendHideIntents();
            }
        };
    }

    private MapAction buildDisplayVideoAction(VideoCollectionList vcList, int index) {
        return new MapAction() {
            @Override
            public void performAction(MapView mapView, MapItem mapItem) {
                vcList.get(index).display();
                sendHideIntents();
            }
        };
    }

    private void sendIntent(String action) {
        AtakBroadcast.getInstance().sendBroadcast(new Intent(action));
    }

    private void sendHideIntents() {
        sendIntent("com.atakmap.android.maps.UNFOCUS");
        sendIntent("com.atakmap.android.maps.HIDE_DETAILS");
        sendIntent("com.atakmap.android.maps.HIDE_MENU");
    }
}
