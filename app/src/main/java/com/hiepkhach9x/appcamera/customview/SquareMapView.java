package com.hiepkhach9x.appcamera.customview;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.gms.maps.MapView;

/**
 * Created by hungh on 3/28/2017.
 */

public class SquareMapView extends MapView {
    public SquareMapView(Context context) {
        super(context);
    }

    public SquareMapView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public SquareMapView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);
    }
}
