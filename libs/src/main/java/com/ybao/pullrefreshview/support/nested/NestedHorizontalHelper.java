package com.ybao.pullrefreshview.support.nested;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import com.ybao.pullrefreshview.layout.FlingLayout;

public class NestedHorizontalHelper extends NestedHelper {

    private static final int AXIS = ViewCompat.SCROLL_AXIS_HORIZONTAL;

    public NestedHorizontalHelper(FlingLayout flingLayout, FlingLayout.ScrollHelper scrollHelper) {
        super(flingLayout, scrollHelper);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes) {
        if (!isNestedScrollingEnabled()) {
            setNestedScrollingEnabled(true);//传递上去
        }
        return (axes & AXIS) == AXIS;
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        int[] offsetInWindow = new int[2];
        dispatchNestedScroll(dxConsumed, dyConsumed, dyConsumed, dyUnconsumed, offsetInWindow);
        mScrollHelper.offsetBy(-dxUnconsumed - offsetInWindow[0]);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int tdx, int tdy, @NonNull int[] consumed) {
        int offset = (int) mFlingLayout.getOffset();
        if (offset == 0) {
            //未滑动时，直接交给上级控件处理
            dispatchNestedPreScroll(tdx, tdy, consumed, null);
            //上级控件未消费完的交回下级处理，直接返回
        } else {
            //已经滑动时
            int mdx = -tdx;
            if ((offset < 0 && offset + mdx >= 0) || (offset > 0 && offset + mdx <= 0)) {
                //归位
                mScrollHelper.offsetTo(0);
                mdx = 0 - offset;
                int[] pConsumed = new int[2];
                dispatchNestedPreScroll(tdx + mdx, tdy, pConsumed, null);
                consumed[0] = pConsumed[0] - mdx;
                consumed[1] = pConsumed[1];
            } else if ((offset > 0 && mdx > 0) || (offset < 0 && mdx < 0)) {
                //是否超过最大距离
                int maxDistance = mFlingLayout.getMaxDistance();
                if (maxDistance > 0 && offset + mdx >= maxDistance) {
                    mScrollHelper.offsetTo(maxDistance);
                } else if (maxDistance > 0 && offset + mdx <= -maxDistance) {
                    mScrollHelper.offsetTo(-maxDistance);
                } else {
                    int ps = mdx * (Math.abs(offset) + maxDistance) / (2 * maxDistance);
                    mdx = mdx - ps;
                    mScrollHelper.offsetBy(mdx);
                }
                consumed[0] = tdx;
            } else {
                mScrollHelper.offsetBy(mdx);
                consumed[0] = tdx;
            }
        }
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        mScrollHelper.setVelocity(-velocityX);
        return super.onNestedPreFling(target, velocityX, velocityY);
    }
}
