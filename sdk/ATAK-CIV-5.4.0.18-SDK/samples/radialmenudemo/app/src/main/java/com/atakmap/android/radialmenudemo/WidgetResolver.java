package com.atakmap.android.radialmenudemo;

import com.atakmap.android.menu.MapMenuButtonWidget;
import com.atakmap.android.menu.MapMenuReceiver;
import com.atakmap.android.menu.MapMenuWidget;
import com.atakmap.android.widgets.AbstractParentWidget;
import com.atakmap.android.widgets.MapWidget;

// collection of brute force searches,
// but good enough for infrequent queries over small hierarchies
public class WidgetResolver {

    public static MapMenuButtonWidget
    resolveButtonWidget(final int buttonHash, final AbstractParentWidget rootWidget) {
        for (MapWidget childWidget : rootWidget.getChildWidgets()) {
            if (childWidget instanceof MapMenuWidget) {
                MapMenuWidget menuWidget = (MapMenuWidget) childWidget;
                MapMenuButtonWidget buttonWidget =
                        resolveButtonWidget(buttonHash, menuWidget);
                if (null != buttonWidget)
                    return buttonWidget;
            } else if (childWidget instanceof MapMenuButtonWidget) {
                MapMenuButtonWidget buttonWidget = (MapMenuButtonWidget) childWidget;
                if (buttonHash == buttonWidget.hashCode())
                    return buttonWidget;
                MapMenuWidget menuWidget = buttonWidget.getSubmenuWidget();
                if (null != menuWidget) {
                    buttonWidget = resolveButtonWidget(buttonHash, menuWidget);
                    if (null != buttonWidget)
                        return buttonWidget;
                }
            }
        }
        return null;
    }

    public static MapMenuButtonWidget resolveButtonWidget(final int buttonHash) {
        return resolveButtonWidget(buttonHash, MapMenuReceiver.getMenuWidget());
    }

    public static MapMenuWidget
    resolveMenuWidget(final int menuHash, final AbstractParentWidget rootWidget) {
        for (MapWidget childWidget : rootWidget.getChildWidgets()) {
            if (childWidget instanceof MapMenuWidget) {
                MapMenuWidget menuWidget = (MapMenuWidget) childWidget;
                if (menuHash == menuWidget.hashCode())
                    return menuWidget;
                menuWidget = resolveMenuWidget(menuHash, menuWidget);
                if (null != menuWidget)
                    return menuWidget;
            } else if (childWidget instanceof MapMenuButtonWidget) {
                MapMenuButtonWidget buttonWidget = (MapMenuButtonWidget) childWidget;
                MapMenuWidget menuWidget = buttonWidget.getSubmenuWidget();
                if (null != menuWidget) {
                    if (menuHash == menuWidget.hashCode()) {
                        return menuWidget;
                    } else {
                        menuWidget = resolveMenuWidget(menuHash, menuWidget);
                        if (null != menuWidget)
                            return menuWidget;
                    }
                }
            }
        }
        return null;
    }
    public static MapMenuWidget resolveMenuWidget(final int menuHash) {
        return resolveMenuWidget(menuHash, MapMenuReceiver.getMenuWidget());
    }

    public static MapMenuWidget
    resolveButtonParentWidget(final int buttonHash, final AbstractParentWidget rootWidget) {
        for (MapWidget childWidget : rootWidget.getChildWidgets()) {
            if (childWidget instanceof MapMenuWidget) {
                MapMenuWidget menuWidget = (MapMenuWidget) childWidget;
                MapMenuWidget parentWidget =
                        resolveButtonParentWidget(buttonHash, menuWidget);
                if (null != parentWidget)
                    return parentWidget;
            } else if (childWidget instanceof MapMenuButtonWidget) {
                MapMenuButtonWidget buttonWidget = (MapMenuButtonWidget) childWidget;
                if (buttonHash == buttonWidget.hashCode())
                    return (MapMenuWidget) rootWidget;
                MapMenuWidget menuWidget = buttonWidget.getSubmenuWidget();
                if (null != menuWidget) {
                    MapMenuWidget parentWidget =
                            resolveButtonParentWidget(buttonHash, menuWidget);
                    if (null != parentWidget)
                        return parentWidget;
                }
            }
        }
        return null;
    }

    public static MapMenuWidget resolveButtonParentWidget(final int buttonHash) {
        return resolveButtonParentWidget(buttonHash, MapMenuReceiver.getMenuWidget());
    }
}
