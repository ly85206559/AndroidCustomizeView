package com.android.customview.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.customview.utils.BezierEvaluator;

import java.lang.ref.WeakReference;
import java.util.Random;

/**
 * Created by Ben.li on 2017/8/17.
 */

public class LoveIconView extends RelativeLayout {

    private int width, height;
    private int iconWidth, iconHeight;

    private Interpolator[] interpolators;
    private Random random = new Random();

    public LoveIconView(Context context) {
        this(context, null);
    }

    public LoveIconView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoveIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public LoveIconView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        initInterpolator();
    }

    private void initInterpolator() {
        interpolators = new Interpolator[]{
                new LinearInterpolator(),
                new AccelerateDecelerateInterpolator(),
                new AccelerateInterpolator(),
                new DecelerateInterpolator(),
        };
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
    }

    @Override
    protected void onDetachedFromWindow() {
        removeAllViews();
        super.onDetachedFromWindow();
    }

    private void startAnimator(ImageView view) {
        //曲线的两个顶点
        PointF pointF1 = new PointF(
                random.nextInt(width),
                random.nextInt(height / 2) + height / 2);
        PointF pointF2 = new PointF(
                random.nextInt(width),
                random.nextInt(height / 2));
        PointF pointStart = new PointF((width - iconWidth) / 2,
                height - iconHeight);
        PointF pointEnd = new PointF(random.nextInt(width), random.nextInt(height / 2));

        //贝塞尔估值器
        BezierEvaluator evaluator = new BezierEvaluator(pointF1, pointF2);
        ValueAnimator animator = ValueAnimator.ofObject(evaluator, pointStart, pointEnd);
        animator.setTarget(view);
        animator.setDuration(3000);
        animator.addUpdateListener(new UpdateListener(view));
        animator.addListener(new AnimatorListener(view, this));
        animator.setInterpolator(interpolators[random.nextInt(4)]);

        animator.start();
    }

    public void addLoveIcon(int resId) {
        ImageView view = new ImageView(getContext());
        view.setImageResource(resId);
        iconWidth = view.getDrawable().getIntrinsicWidth();
        iconHeight = view.getDrawable().getIntrinsicHeight();

        addView(view);
        startAnimator(view);
    }

    public static class UpdateListener implements ValueAnimator.AnimatorUpdateListener {

        private WeakReference<ImageView> iv;

        public UpdateListener(ImageView iv) {
            this.iv = new WeakReference<>(iv);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            PointF pointF = (PointF) animation.getAnimatedValue();
            ImageView view = iv.get();
            if (null != view) {
                view.setX(pointF.x);
                view.setY(pointF.y);
                view.setAlpha(1 - animation.getAnimatedFraction() + 0.1f);
            }
        }
    }

    public static class AnimatorListener extends AnimatorListenerAdapter {

        private WeakReference<ImageView> iv;
        private WeakReference<LoveIconView> parent;

        public AnimatorListener(ImageView iv, LoveIconView parent) {
            this.iv = new WeakReference<>(iv);
            this.parent = new WeakReference<>(parent);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            ImageView view = iv.get();
            LoveIconView parent = this.parent.get();
            if (null != view
                    && null != parent) {
                parent.removeView(view);
            }
        }
    }
}
