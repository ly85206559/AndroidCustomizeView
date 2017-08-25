package com.android.customview.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.android.customview.R;
import com.android.customview.utils.LinearEvaluator;

/**
 * Created by Ben.li on 2017/8/17.
 */
public class MessageBubble extends View {

    private Paint paint;
    //爆炸绘制画笔
    private Paint explosionPaint;
    //爆炸动画绘制区域
    private Rect explosionRect;

    //View宽高
    private int mWidth, mHeight;
    //爆炸动画宽高
    private int explosionWidth, explosionHeight;
    //固定和拖拽圆心点坐标
    private PointF fixPoint, dragPoint;
    //固定和拖拽圆半径
    private float fixRadius, dragRadius;

    //固定圆最大半径
    private float FIX_RADIUS_MAX;
    //固定圆最小半径
    private float FIX_RADIUS_MIN;
    //贝塞尔路径
    private Path bezierPath;

    //回弹动画
    private ValueAnimator rollBackAnimator;
    //爆炸动画
    private ValueAnimator explosionAnimator;
    private int[] explosionResIds = {
            R.drawable.explosion_1,
            R.drawable.explosion_2,
            R.drawable.explosion_3,
            R.drawable.explosion_4,
            R.drawable.explosion_5,
            -1
    };
    private Bitmap[] explosionBitmap;

    private boolean explosion = false;
    //当前爆炸动画的帧索引
    private int currentExplosionIndex = explosionResIds.length - 1;

    private Paint textPaint;
    private String content;

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

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(30);
        textPaint.setDither(true);
        textPaint.setAntiAlias(true);

        fixPoint = new PointF();
        dragPoint = new PointF();

        bezierPath = new Path();

        dragRadius = 30;
        FIX_RADIUS_MAX = dragRadius * 0.8f;
        FIX_RADIUS_MIN = 15;
        fixRadius = FIX_RADIUS_MAX;

        explosionPaint = new Paint();
        explosionRect = new Rect();
        explosionAnimator = ValueAnimator.ofInt(0, explosionResIds.length - 1);
        explosionAnimator.setInterpolator(new LinearInterpolator());
        explosionAnimator.setDuration(300);
        explosionAnimator.setRepeatCount(0);
        explosionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentExplosionIndex = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        explosionBitmap = new Bitmap[explosionResIds.length - 1];
        for (int i = 0; i < explosionBitmap.length; i++) {
            //将气泡爆炸的drawable转为bitmap
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), explosionResIds[i]);
            explosionBitmap[i] = bitmap;
        }
        explosionWidth = getResources().getDrawable(explosionResIds[0]).getIntrinsicWidth();
        explosionHeight = getResources().getDrawable(explosionResIds[0]).getIntrinsicHeight();
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
        if (explosion == true) {
            if (currentExplosionIndex != explosionResIds.length - 1) {
                explosionRect.set(
                        (int) dragPoint.x - explosionWidth / 2,
                        (int) dragPoint.y - explosionHeight / 2,
                        (int) dragPoint.x + explosionWidth / 2,
                        (int) dragPoint.y + explosionHeight / 2
                );
                canvas.drawBitmap(explosionBitmap[currentExplosionIndex], null, explosionRect, explosionPaint);
            }
            return;
        }
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

        if (!TextUtils.isEmpty(content)) {
            float textWidth = textPaint.measureText(content);
            float x = dragPoint.x - textWidth / 2;

            Paint.FontMetrics metrics = textPaint.getFontMetrics();
            //metrics.ascent为负数
            float dy = -(metrics.descent + metrics.ascent) / 2;
            float y = dragPoint.y + dy;
            canvas.drawText(content, x, y, textPaint);
        }
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
        double distance = Math.sqrt(
                (dragPoint.x - fixPoint.x) * (dragPoint.x - fixPoint.x)
                        + (dragPoint.y - fixPoint.y) * (dragPoint.y - fixPoint.y));
        if (Double.compare(FIX_RADIUS_MAX - distance / 15, FIX_RADIUS_MIN) < 0) {
            explosionAnimator.start();
            explosion = true;
            return;
        }
        PointF pointA = new PointF(
                dragPoint.x + (fixPoint.x - dragPoint.x) * 1.35f,
                dragPoint.y + (fixPoint.y - dragPoint.y) * 1.35f
        );
        PointF pointB = new PointF(
                dragPoint.x + (fixPoint.x - dragPoint.x) * 0.75f,
                dragPoint.y + (fixPoint.y - dragPoint.y) * 0.75f
        );
        rollBackAnimator = ValueAnimator.ofObject(new LinearEvaluator(),
                dragPoint, pointA, pointB, fixPoint);
        rollBackAnimator.setDuration(250);
        rollBackAnimator.setInterpolator(new AccelerateInterpolator());
        rollBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                PointF point = (PointF) animation.getAnimatedValue();
                dragPoint.x = point.x;
                dragPoint.y = point.y;
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

    @Override
    protected void onDetachedFromWindow() {
        if (explosionAnimator.isRunning()) {
            explosionAnimator.cancel();
        }
        super.onDetachedFromWindow();
    }

    public void reset() {
        dragPoint.x = mWidth / 2;
        dragPoint.y = mHeight / 2;
        explosion = false;
        invalidate();
    }

    public void setText(String text) {
        content = text;
    }
}
