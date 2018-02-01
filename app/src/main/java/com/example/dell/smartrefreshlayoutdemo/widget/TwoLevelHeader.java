package com.example.dell.smartrefreshlayoutdemo.widget;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshKernel;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.impl.RefreshHeaderWrapper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by dell on 2018/2/1.
 */
@SuppressLint("RestrictedApi")
public class TwoLevelHeader extends FrameLayout implements RefreshHeader,InvocationHandler {

    private RefreshState mState;


    /**
     * 二级刷新监听器
     */
    public interface OnTwoLevelListener {
        /**
         * 二级刷新触发
         * @param refreshLayout 刷新布局
         * @return true 将会展开二楼状态 false 关闭刷新
         */
        boolean onTwoLevel(RefreshLayout refreshLayout);
    }


    //<editor-fold desc="属性字段">
    protected int mSpinner;
    protected float mPercent = 0;
    protected float mMaxRage = 2.5f;
    protected float mFloorRage = 1.9f;
    protected float mRefreshRage = 1f;
    protected boolean mEnableTwoLevel = true;
    protected boolean mEnablePullToCloseTwoLevel = true;
    protected int mFloorDuration = 1000;
    protected int mHeaderHeight;
    protected int mPaintAlpha;
    protected Paint mPaint;
    protected RefreshHeader mRefreshHeader;
    protected RefreshKernel mRefreshKernel;
    protected OnTwoLevelListener mTwoLevelListener;
    protected SpinnerStyle mSpinnerStle = SpinnerStyle.FixedBehind;
    protected Method mRrequestDrawBackgoundForHeaderMethod;
    //</editor-fold>


    //<editor-fold desc="构造方法">
    public TwoLevelHeader(@NonNull Context context) {
        super(context);
        this.initView(context, null);
    }

    public TwoLevelHeader(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initView(context, attrs);
    }

    public TwoLevelHeader(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initView(context, attrs);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public TwoLevelHeader(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initView(context, attrs);
    }

    public void initView(Context context, AttributeSet attrs) {
    }
    //</editor-fold>


    //<editor-fold desc="生命周期">
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        for (int i = 0, len = getChildCount(); i < len; i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof RefreshHeader) {
                mRefreshHeader = (RefreshHeader) childAt;
                bringChildToFront(childAt);
                break;
            }
        }
        if (mRefreshHeader == null) {
            mRefreshHeader = new RefreshHeaderWrapper(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mSpinnerStle = SpinnerStyle.MatchLayout;
        if (mRefreshHeader == null) {
            mRefreshHeader = new RefreshHeaderWrapper(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mSpinnerStle = SpinnerStyle.FixedBehind;
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mRefreshHeader.getView() != this) {
            int mode = MeasureSpec.getMode(heightMeasureSpec);
            if (mode == MeasureSpec.AT_MOST) {
                mRefreshHeader.getView().measure(widthMeasureSpec, heightMeasureSpec);
                int height = mRefreshHeader.getView().getMeasuredHeight();
                setMeasuredDimension(resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec), height);
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean isInEditMode = isInEditMode();
        if (child == mRefreshHeader.getView() && mPaint != null) {
            canvas.drawRect(0, 0, getWidth(), ((isInEditMode) ? mHeaderHeight : mSpinner) + child.getTop(), mPaint);
        }
        return super.drawChild(canvas, child, drawingTime);
    }

    //</editor-fold>

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object returnValue = null;
        if (mRefreshKernel != null) {
            if (method.equals(mRrequestDrawBackgoundForHeaderMethod)) {
                int backgroundColor = (int) args[0];
                if (backgroundColor == 0) {
                    mPaint = null;
                } else {
                    if (mPaint == null) {
                        mPaint = new Paint();
                    }
                    mPaint.setColor(backgroundColor);
                    mPaintAlpha = ((backgroundColor & 0xFF000000) >> 24);
                }
                returnValue = proxy;
            } else {
                returnValue = method.invoke(mRefreshKernel, args);
            }
        }
        if (method.getReturnType().equals(RefreshKernel.class)) {
            if (mRefreshKernel == null && RefreshKernel.class.equals(method.getDeclaringClass())) {
                if (mRrequestDrawBackgoundForHeaderMethod == null) {
                    mRrequestDrawBackgoundForHeaderMethod = method;
                }
            }
            return proxy;
        }
        return returnValue;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onInitialized(@NonNull RefreshKernel kernel, int height, int extendHeight) {
        if (1f * (extendHeight + height) / height != mMaxRage && mHeaderHeight == 0) {
            mHeaderHeight = height;
            kernel.getRefreshLayout().setHeaderMaxDragRate(mMaxRage);
            return;
        }
        if (!isInEditMode() && mRefreshHeader.getSpinnerStyle() == SpinnerStyle.Translate
                && mRefreshKernel == null) {
            MarginLayoutParams params = (MarginLayoutParams) mRefreshHeader.getView().getLayoutParams();
            params.topMargin -= height;
            mRefreshHeader.getView().setLayoutParams(params);
        }

        RefreshKernel proxy = (RefreshKernel) Proxy.newProxyInstance(RefreshKernel.class.getClassLoader(), new Class[]{RefreshKernel.class}, this);
        proxy.requestDrawBackgoundForHeader(0);
        proxy.requestHeaderNeedTouchEventWhenRefreshing(false);

        mHeaderHeight = height;
        mRefreshKernel = kernel;
        mRefreshKernel.requestFloorDuration(mFloorDuration);
        mRefreshHeader.onInitialized(proxy, height, extendHeight);
        mRefreshKernel.requestHeaderNeedTouchEventWhenRefreshing(!mEnablePullToCloseTwoLevel);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && mPaint != null) {
            mRefreshHeader.getView().animate().setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation.getAnimatedValue() instanceof Float) {
                        mPaint.setAlpha(((int)((mRefreshHeader.getView().getAlpha())*mPaintAlpha)));
                    }
                }
            });
        }

    }




    @SuppressLint("RestrictedApi")
    @Override
    public void onPullingDown(float percent, int offset, int headerHeight, int extendHeight) {
        moveSpinner(offset);
        mRefreshHeader.onPullingDown(percent, offset, headerHeight, extendHeight);
        if (mPercent < mFloorRage && percent >= mFloorRage && mEnableTwoLevel) {
            mRefreshKernel.setState(RefreshState.ReleaseToTwoLevel);
        } else if (mPercent >= mFloorRage && percent < mRefreshRage) {
            mRefreshKernel.setState(RefreshState.PullDownToRefresh);
        } else if (mPercent >= mFloorRage && percent < mFloorRage) {
            mRefreshKernel.setState(RefreshState.ReleaseToRefresh);
        }
        mPercent = percent;

    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onReleasing(float percent, int offset, int headerHeight, int extendHeight) {
        moveSpinner(offset);
        mRefreshHeader.onReleasing(percent, offset, headerHeight, extendHeight);

    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onRefreshReleased(RefreshLayout layout, int headerHeight, int extendHeight) {
        mRefreshHeader.onRefreshReleased(layout, headerHeight, extendHeight);

    }

    @SuppressLint("RestrictedApi")
    protected void moveSpinner(int spinner) {
        if (mSpinner != spinner && mRefreshHeader.getView() != this) {
            mSpinner = spinner;
            switch (mRefreshHeader.getSpinnerStyle()) {
                case Translate:
                    mRefreshHeader.getView().setTranslationY(spinner);
                    break;
                case Scale:{
                    View view = mRefreshHeader.getView();
                    view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getTop() + Math.max(0, spinner));
                    break;
                }
            }
            if (mPaint != null) {
                invalidate();
            }
        }
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @NonNull
    @Override
    public SpinnerStyle getSpinnerStyle() {
        return mSpinnerStle;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setPrimaryColors(int... colors) {
        mRefreshHeader.setPrimaryColors(colors);
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) {
        mRefreshHeader.onHorizontalDrag(percentX, offsetX, offsetMax);

    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onStartAnimator(@NonNull RefreshLayout layout, int height, int extendHeight) {
        mRefreshHeader.onStartAnimator(layout, height, extendHeight);

    }

    @SuppressLint("RestrictedApi")
    @Override
    public int onFinish(@NonNull RefreshLayout layout, boolean success) {
        return mRefreshHeader.onFinish(layout, success);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean isSupportHorizontalDrag() {
        return mRefreshHeader.isSupportHorizontalDrag();
    }



    @SuppressLint("RestrictedApi")
    @Override
    public void onStateChanged(RefreshLayout refreshLayout, RefreshState oldState, RefreshState newState) {
        mRefreshHeader.onStateChanged(refreshLayout, oldState, newState);
        switch (mState = newState) {
            case TwoLevelReleased:
                if (mRefreshHeader.getView() != this) {
                    mRefreshHeader.getView().animate().alpha(0).setDuration(mFloorDuration / 2);
                }
                if (mPaint != null) {
                    invalidate();
                }
                mRefreshKernel.startTwoLevel(mTwoLevelListener == null || mTwoLevelListener.onTwoLevel(refreshLayout));
                break;
            case TwoLevel:
                break;
            case TwoLevelFinish:
                if (mRefreshHeader.getView() != this) {
                    mRefreshHeader.getView().animate().alpha(1).setDuration(mFloorDuration / 2);
                }
                break;
            case PullDownToRefresh:
                if (mRefreshHeader.getView().getAlpha() == 0 && mRefreshHeader.getView() != this) {
                    mRefreshHeader.getView().setAlpha(1);
                }
                break;
        }

    }


    //<editor-fold desc="开放API">

    /**
     * 设置指定的Header
     */
    public TwoLevelHeader setRefreshHeader(RefreshHeader header) {
        return setRefreshHeader(header, MATCH_PARENT, WRAP_CONTENT);
    }

    /**
     * 设置指定的Header
     */
    public TwoLevelHeader setRefreshHeader(RefreshHeader header, int width, int height) {
        if (header != null) {
            if (mRefreshHeader != null) {
                removeView(mRefreshHeader.getView());
            }
            this.mRefreshHeader = header;
            if (header.getSpinnerStyle() == SpinnerStyle.FixedBehind) {
                this.addView(mRefreshHeader.getView(), 0, new LayoutParams(width, height));
            } else {
                this.addView(mRefreshHeader.getView(), width, height);
            }
        }
        return this;
    }

    /**
     * 设置下拉Header的最大高度比值
     *
     * @param rate MaxDragHeight/HeaderHeight
     */
    public TwoLevelHeader setMaxRage(float rate) {
        if (this.mMaxRage != rate) {
            this.mMaxRage = rate;
            if (this.mRefreshKernel != null) {
                this.mHeaderHeight = 0;
                this.mRefreshKernel.getRefreshLayout().setHeaderMaxDragRate(mMaxRage);
            }
        }
        return this;
    }

    /**
     * 是否禁止在二极状态是上滑关闭状态回到初态
     * @param disable 是否禁止
     */
    public TwoLevelHeader setDisablePullToCloseTwoLevel(boolean disable) {
        this.mEnablePullToCloseTwoLevel = !disable;
        if (this.mRefreshKernel != null) {
            this.mRefreshKernel.requestHeaderNeedTouchEventWhenRefreshing(disable);
        }
        return this;
    }

    public TwoLevelHeader setFloorRage(float rate) {
        this.mFloorRage = rate;
        return this;
    }

    public TwoLevelHeader setRefreshRage(float rate) {
        this.mRefreshRage = rate;
        return this;
    }

    public TwoLevelHeader setEnableTwoLevel(boolean enable) {
        this.mEnableTwoLevel = enable;
        return this;
    }

    public TwoLevelHeader setOnTwoLevelListener(OnTwoLevelListener listener) {
        this.mTwoLevelListener = listener;
        return this;
    }

    public TwoLevelHeader finishTwoLevel() {
        this.mRefreshKernel.finishTwoLevel();
        return this;
    }

    //</editor-fold>














}
