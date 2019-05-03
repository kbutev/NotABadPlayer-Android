package com.media.notabadplayer.View.Player;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.google.common.base.Function;

public class PlayerLayout extends LinearLayout
{
    private static float SWIPE_DOWN_GESTURE_X_DISTANCE_REQUIRED = 95;
    private static float SWIPE_DOWN_GESTURE_Y_DISTANCE_REQUIRED = 300;

    private boolean _swipeDownEventFired = false;
    private float _layoutTouchMotionLastXPosition = -1;
    private float _layoutTouchMotionLastYPosition = -1;

    private Function<Void, Void> _swipeDownCallback = null;

    public PlayerLayout(Context context) {
        super(context, null);
    }

    public PlayerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);
        setFocusable(true);
    }

    public void setSwipeDownCallback(Function<Void, Void> callback)
    {
        _swipeDownCallback = callback;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        if (ev.getAction() != MotionEvent.ACTION_MOVE)
        {
            resetMotionState();
            return super.dispatchTouchEvent(ev);
        }

        boolean swipeUpEventFired = _swipeDownEventFired;

        updateMotionState(ev);

        if (swipeUpEventFired != _swipeDownEventFired)
        {
            swipeDown();
            return true;
        }

        return super.dispatchTouchEvent(ev);
    }
    
    public void updateMotionState(MotionEvent event)
    {
        if (event.getAction() != MotionEvent.ACTION_MOVE)
        {
            return;
        }

        float currentX = event.getX();
        float currentY = event.getY();

        if (_layoutTouchMotionLastYPosition == -1)
        {
            _layoutTouchMotionLastXPosition = currentX;
            _layoutTouchMotionLastYPosition = currentY;
        }

        float diffX = currentX - _layoutTouchMotionLastXPosition;
        float diffY = currentY - _layoutTouchMotionLastYPosition;

        if (Math.abs(diffY) > SWIPE_DOWN_GESTURE_Y_DISTANCE_REQUIRED &&
                diffY > 0 &&
                Math.abs(diffX) <= SWIPE_DOWN_GESTURE_X_DISTANCE_REQUIRED)
        {
            _swipeDownEventFired = true;
        }
    }

    public void resetMotionState()
    {
        _swipeDownEventFired = false;
        _layoutTouchMotionLastXPosition = -1;
        _layoutTouchMotionLastYPosition = -1;
    }

    public void swipeDown()
    {
        if (_swipeDownCallback != null)
        {
            _swipeDownCallback.apply(null);
        }
    }
}
