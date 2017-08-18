package com.android.customview.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by Ben.li on 2017/8/17.
 */
public class MessageBubble extends View {

    private Paint paint;
    private int mWidth, mHeight;
    private PointF fixPoint, dragPoint;
    private float fixRadius, dragRadius;

    private float FIX_RADIUS_MAX;
    private float FIX_RADIUS_MIN;
    private Path bezierPath;

    private ValueAnimator rollBackAnimator;
    private float slope;

    public MessageBubble(Context context) {
        this(context, null);
    }

    public MessageBubble(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageBubble(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MessageBubble(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setDither(true);
        paint.setAntiAlias(true);

        fixPoint = new PointF();
        dragPoint = new PointF();

        bezierPath = new Path();

        dragRadius = 30;
        FIX_RADIUS_MAX = dragRadius * 0.8f;
        FIX_RADIUS_MIN = 15;
        fixRadius = FIX_RADIUS_MAX;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        dragPoint.x = fixPoint.x = mWidth / 2;
        dragPoint.y = fixPoint.y = mHeight / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        double distance = Math.sqrt(
                (dragPoint.x - fixPoint.x) * (dragPoint.x - fixPoint.x)
                        + (dragPoint.y - fixPoint.y) * (dragPoint.y - fixPoint.y));
        fixRadius = (float) (FIX_RADIUS_MAX - distance / 15);
        if (fixRadius > FIX_RADIUS_MIN) {
            canvas.drawCircle(fixPoint.x, fixPoint.y, fixRadius, paint);
            setBezierPath();
            canvas.drawPath(bezierPath, paint);
        }
        canvas.drawCircle(dragPoint.x, dragPoint.y, dragRadius, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {

        } else if (MotionEvent.ACTION_MOVE == event.getAction()) {
            updateDragPoint(event.getX(), event.getY());
        } else if (MotionEvent.ACTION_UP == event.getAction()) {
            updateView();
        }

        invalidate();
        return true;
    }

    private void updateView() {
        slope = (dragPoint.y - fixPoint.y) / (dragPoint.x - fixPoint.x);
        rollBackAnimator = ValueAnimator.ofFloat(dragPoint.x, fixPoint.x);
        rollBackAnimator.setDuration(200);
        rollBackAnimator.setInterpolator(new LinearInterpolator());
        rollBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                dragPoint.x = (float) animation.getAnimatedValue();
                dragPoint.y = dragPoint.x * slope;
                invalidate();
            }
        });
        rollBackAnimator.start();
    }

    private void updateDragPoint(float x, float y) {
        dragPoint.x = x;
        dragPoint.y = y;
    }

    private void setBezierPath() {
        float dx = fixPoint.x - dragPoint.x;
        float dy = fixPoint.y - dragPoint.y;
        if (Float.compare(dx, 0) == 0) {
            dx = 0.001f;
        }

        float arcTanA = (float) Math.atan(dy / dx);

        float P0X = (float) (fixPoint.x + fixRadius * Math.sin(arcTanA));
        float P0Y = (float) (fixPoint.y - fixRadius * Math.cos(arcTanA));

        float P1X = (float) (dragPoint.x + dragRadius * Math.sin(arcTanA));
        float P1Y = (float) (dragPoint.y - dragRadius * Math.cos(arcTanA));

        float P2X = (float) (dragPoint.x - dragRadius * Math.sin(arcTanA));
        float P2Y = (float) (dragPoint.y + dragRadius * Math.cos(arcTanA));

        float P3X = (float) (fixPoint.x - fixRadius * Math.sin(arcTanA));
        float P3Y = (float) (fixPoint.y + fixRadius * Math.cos(arcTanA));

        PointF controlPoint = new PointF(
                (fixPoint.x + dragPoint.x) / 2,
                (fixPoint.y + dragPoint.y) / 2);

        bezierPath.reset();
        bezierPath.moveTo(P0X, P0Y);
        bezierPath.quadTo(controlPoint.x, controlPoint.y, P1X, P1Y);
        bezierPath.lineTo(P2X, P2Y);
        bezierPath.quadTo(controlPoint.x, controlPoint.y, P3X, P3Y);
        bezierPath.close();
    }
}
