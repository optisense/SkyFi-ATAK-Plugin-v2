package com.atakmap.android.radialmenudemo;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.atakmap.android.menu.MapMenuWidget;
import com.atakmap.android.radialmenudemo.plugin.R;

public class AlertMenuView extends LinearLayout {

    public AlertMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void populateLayout(MapMenuWidget menuWidget) {

        EditText startAngleEntry = (EditText) findViewById(R.id.start_angle);
        startAngleEntry.setText(String.valueOf(menuWidget.getStartAngle()));

        EditText coveredAngleEntry = (EditText) findViewById(R.id.covered_angle);
        coveredAngleEntry.setText(String.valueOf(menuWidget.getCoveredAngle()));

        EditText innerRadiusEntry = (EditText) findViewById(R.id.inner_radius);
        innerRadiusEntry.setText(String.valueOf(menuWidget.getInnerRadius()));

        EditText buttonWidthEntry = (EditText) findViewById(R.id.button_width);
        buttonWidthEntry.setText(String.valueOf(menuWidget.getButtonWidth()));

        Switch buttonWinding = (Switch) findViewById(R.id.button_winding);
        buttonWinding.setChecked(menuWidget.isClockwiseWinding());
    }
    public boolean getButtonWinding() {
        Switch buttonWinding = (Switch) findViewById(R.id.button_winding);
        return buttonWinding.isChecked();
    }

    protected float getNumericValue(int viewId) {
        EditText valueText = (EditText) findViewById(viewId);
        return Float.parseFloat(valueText.getText().toString());
    }

    public float getStartAngle() {
        return getNumericValue(R.id.start_angle);
    }

    public float getCoveredAngle() {
        return getNumericValue(R.id.covered_angle);
    }

    public float getInnerRadius() {
        return getNumericValue(R.id.inner_radius);
    }

    public float getButtonWidth() {
        return getNumericValue(R.id.button_width);
    }

}
