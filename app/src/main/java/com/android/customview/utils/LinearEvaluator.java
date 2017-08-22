package com.android.customview.utils;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

/**
 * Created by Ben.li on 2017/8/17.
 */

public class LinearEvaluator implements TypeEvaluator<PointF> {

    private PointF point;

    public LinearEvaluator() {
        point = new PointF();
    }

    @Override
    public PointF evaluate(float t, PointF startValue, PointF endValue) {
        point.x = startValue.x + t * (endValue.x - startValue.x);
        point.y = startValue.y + t * (endValue.y - startValue.y);
        return point;
    }
}
