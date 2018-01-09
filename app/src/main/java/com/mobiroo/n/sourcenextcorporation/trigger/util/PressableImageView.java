package com.mobiroo.n.sourcenextcorporation.trigger.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

public class PressableImageView extends ImageView {
    private Paint mPressedPaint;

    public PressableImageView(Context context) {
        this(context, null);
    }

    public PressableImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PressableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setClickable(true);

        mPressedPaint = new Paint();
        mPressedPaint.setColor(Color.BLACK);
        mPressedPaint.setAlpha(30);
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isPressed()) {
            canvas.drawRect(canvas.getClipBounds(), mPressedPaint);
        }
    }
}
