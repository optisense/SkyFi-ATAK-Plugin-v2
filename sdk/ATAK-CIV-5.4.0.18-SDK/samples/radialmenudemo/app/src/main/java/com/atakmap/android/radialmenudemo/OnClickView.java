package com.atakmap.android.radialmenudemo;

import android.content.Context;
import android.util.AttributeSet;

import com.atakmap.android.action.MapAction;
import com.atakmap.android.radialmenudemo.plugin.R;

public class OnClickView extends AssetSpinnerView<MapAction> {

    final protected MenuWidgetFactory factory;

    public OnClickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPathAndLabel("actions", R.string.onclickTitle);
        factory = new MenuWidgetFactory();
    }

    @Override
    protected MapAction resolve(String xmlResource) {
        return factory.resolveAction(xmlResource);
    }
}
