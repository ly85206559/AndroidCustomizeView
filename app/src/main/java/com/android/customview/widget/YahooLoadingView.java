package com.android.customview.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.android.customview.R;

/**
 * Created by Ben.li on 2017/8/11.
 */

public class YahooLoadingView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder surfaceHolder;

    //View width/2 height/2 center point
    private int width, height;

    //Draw small circle
    private Paint paint;

    //Draw expanded circle
    private Paint expandPaint;
    //Small color list
    private int[] colors;
    private ValueAnimator circleAnimator;
    private ValueAnimator expandAnimator;
    private ValueAnimator collapsingAnimator;
    private ValueAnimator filterAnimator;

    //Current rotate angle
    private float rotateAngle;
    private int innerRadius, outerRadius, expandRadius = -1;
    //The angle between each circle
    private float gapAngle;
    //The factor for collapsing and expand
    private float factor = 1.0f;
    //Status
    private Status status = Status.IDLE;
    //Expand animator set
    private AnimatorSet animatorSet;
    //Circle animator set
    private AnimatorSet circleAnimatorSet;

    private PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);

    public enum Status {
        IDLE,
        LOADING,
        COMPLETE,
    }

    public YahooLoadingView(Context context) {
        this(context, null);
    }

    public YahooLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YahooLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public YahooLoadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.YahooLoading);
        innerRadius = a.getDimensionPixelSize(R.styleable.YahooLoading_innerRadius, 15);
        outerRadius = a.getDimensionPixelSize(R.styleable.YahooLoading_outerRadius, 160);
        a.recycle();

        surfaceHolder = getHolder();

        initData();
        initAnimator();

        setLayerType(LAYER_TYPE_HARDWARE, null);
        setBackgroundColor(Color.WHITE);

        setZOrderOnTop(true);
        surfaceHolder.addCallback(this);
    }

    private void initData() {
        colors = new int[]{Color.RED, Color.BLUE, Color.GREEN, Color.CYAN, Color.DKGRAY, Color.GRAY};
        gapAngle = (float) Math.PI * 2 / colors.length;

        paint = new Paint();
        paint.setAntiAlias(true);

        expandPaint = new Paint();
        expandPaint.setAntiAlias(true);
    }

    private void initAnimator() {
        circleAnimator = ValueAnimator.ofFloat(0, (float) Math.PI * 2);
        circleAnimator.setDuration(1600);
        circleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        circleAnimator.setInterpolator(new LinearInterpolator());
        circleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                expand();
            }
        });
        circleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (Status.COMPLETE == status) {
                    animation.cancel();
                    return;
                }
                rotateAngle = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        expandAnimator = ValueAnimator.ofFloat(1, 1.5f);
        expandAnimator.setDuration(200);
        expandAnimator.setRepeatCount(0);
        expandAnimator.setInterpolator(new DecelerateInterpolator());
        expandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                factor = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        collapsingAnimator = ValueAnimator.ofFloat(1.5f, 0f);
        collapsingAnimator.setDuration(300);
        collapsingAnimator.setRepeatCount(0);
        collapsingAnimator.setInterpolator(new AccelerateInterpolator());
        collapsingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                factor = (float) animation.getAnimatedValue();
                invalidate();
            }
        });


        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        int width = point.x / 2;
        int height = point.y / 2;
        filterAnimator = ValueAnimator.ofInt(0, (int) Math.sqrt(width * width + height * height));
        filterAnimator.setDuration(2000);
        filterAnimator.setRepeatCount(0);
        filterAnimator.setInterpolator(new LinearInterpolator());
        filterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                expandRadius = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        filterAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                resetData();
                setVisibility(GONE);
            }
        });

        animatorSet = new AnimatorSet();
        animatorSet.playSequentially(expandAnimator, collapsingAnimator, filterAnimator);

        circleAnimatorSet = new AnimatorSet();
        circleAnimatorSet.playTogether(circleAnimator);
    }

    private void resetData() {
        status = Status.IDLE;
        expandRadius = -1;
        rotateAngle = 0;
        factor = 1;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        status = Status.LOADING;
        circleAnimatorSet.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        circleAnimatorSet.cancel();
        animatorSet.cancel();
    }

    public void setColors(int[] colors) {
        if (null == colors) {
            return;
        }
        this.colors = colors;
        gapAngle = (float) Math.PI * 2 / this.colors.length;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth() / 2;
        height = getMeasuredHeight() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (expandRadius > -1) {
            expandPaint.setColor(Color.WHITE);
            expandPaint.setXfermode(mode);
            canvas.drawCircle(width, height, expandRadius, expandPaint);
        }

        for (int i = 0; i < colors.length; i++) {
            paint.setColor(colors[i]);
            float radius = (outerRadius - innerRadius) * factor;
            float cx = (float) (radius * Math.sin((double) (rotateAngle + i * gapAngle)) + width);
            float cy = (float) (height - radius * Math.cos((double) (rotateAngle + i * gapAngle)));
            if (radius > innerRadius) {
                canvas.drawCircle(cx, cy, innerRadius, paint);
            } else {
                canvas.drawCircle(cx, cy, radius, paint);
            }
        }
    }

    private void expand() {
        animatorSet.start();
    }

    public void startLoading() {
        if (Status.LOADING == status) {
            return;
        }
        status = Status.LOADING;
        setVisibility(VISIBLE);
    }

    public void loadingComplete() {
        if (Status.IDLE == status
                || Status.COMPLETE == status) {
            return;
        }
        status = Status.COMPLETE;
    }
}