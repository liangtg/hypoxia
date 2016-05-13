package com.syber.base.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by liangtg on 16-5-10.
 */
public class OvalView extends FrameLayout {
    private Paint paint = new Paint();

    public OvalView(Context context) {
        super(context);
        ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, paint);
    }

    public OvalView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }
}
