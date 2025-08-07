package com.atakmap.android.radialmenudemo;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.atakmap.android.menu.MapMenuWidget;

public class MenuDialogHandler implements DialogInterface.OnClickListener {

    public interface CompletionHandler {
        void OnComplete(AlertMenuView menuView,
                        MapMenuWidget menuWidget);
    }

    AlertMenuView menuView;
    MapMenuWidget menuWidget;
    CompletionHandler handler;

    public MenuDialogHandler(AlertMenuView menuView,
                             MapMenuWidget menuWidget,
                             CompletionHandler handler) {
        this.menuView = menuView;
        this.menuWidget = menuWidget;
        this.handler = handler;
    }

    public MenuDialogHandler(AlertMenuView menuView,
                             MapMenuWidget menuWidget) {
        this(menuView, menuWidget, null);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {

        if (which == AlertDialog.BUTTON_POSITIVE) {
            menuWidget.setStartAngle(menuView.getStartAngle());
            menuWidget.setButtonWidth(menuView.getButtonWidth());
            menuWidget.setInnerRadius(menuView.getInnerRadius());
            menuWidget.setButtonWidth(menuView.getButtonWidth());
            menuWidget.setClockwiseWinding(menuView.getButtonWinding());
        }

        if (null != handler)
            handler.OnComplete(menuView, menuWidget);
    }
}
