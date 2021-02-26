package com.media.notabadplayer.View.Player;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import com.google.common.base.Function;

public class PlayerLayout extends LinearLayout
{
    public enum SwipeAction {
        None, Left, Right, Down
    }
    
    private static float SWIPE_DOWN_GESTURE_X_DISTANCE_REQUIRED = 95;
    private static float SWIPE_DOWN_GESTURE_Y_DISTANCE_REQUIRED = 250;
    
    private static float SWIPE_HORIZONTAL_GESTURE_X_DISTANCE_REQUIRED = 200;
    private static float SWIPE_HORIZONTAL_GESTURE_Y_DISTANCE_REQUIRED = 90;
    
    private boolean _swipeEventFired = false;
    private float _layoutTouchMotionLastXPosition = -1;
    private float _layoutTouchMotionLastYPosition = -1;

    private Function<Void, Void> _swipeLeftCallback = null;
    private Function<Void, Void> _swipeRightCallback = null;
    private Function<Void, Void> _swipeDownCallback = null;

    public PlayerLayout(Context context) {
        super(context, null);
    }

    public PlayerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);
        setFocusable(true);
    }

    public void setSwipeLeftCallback(Function<Void, Void> callback)
    {
        _swipeLeftCallback = callback;
    }

    public void setSwipeRightCallback(Function<Void, Void> callback)
    {
        _swipeRightCallback = callback;
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

        boolean swipeEventFired = _swipeEventFired;

        SwipeAction result = updateMotionState(ev);

        if (swipeEventFired != _swipeEventFired)
        {
            switch (result)
            {
                case Down:
                    swipeDown();
                    break;
                case Left:
                    swipeLeft();
                    break;
                case Right:
                    swipeRight();
                    break;
            }
            
            return true;
        }

        return super.dispatchTouchEvent(ev);
    }
    
    public SwipeAction updateMotionState(MotionEvent event)
    {
        if (event.getAction() != MotionEvent.ACTION_MOVE)
        {
            return SwipeAction.None;
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

        if (diffY > 0 &&
                Math.abs(diffY) > SWIPE_DOWN_GESTURE_Y_DISTANCE_REQUIRED &&
                Math.abs(diffX) <= SWIPE_DOWN_GESTURE_X_DISTANCE_REQUIRED)
        {
            _swipeEventFired = true;
            return SwipeAction.Down;
        }

        if (diffX < 0 &&
                Math.abs(diffY) <= SWIPE_HORIZONTAL_GESTURE_Y_DISTANCE_REQUIRED &&
                Math.abs(diffX) > SWIPE_HORIZONTAL_GESTURE_X_DISTANCE_REQUIRED)
        {
            _swipeEventFired = true;
            return SwipeAction.Left;
        }

        if (diffX > 0 &&
                Math.abs(diffY) <= SWIPE_HORIZONTAL_GESTURE_Y_DISTANCE_REQUIRED &&
                Math.abs(diffX) > SWIPE_HORIZONTAL_GESTURE_X_DISTANCE_REQUIRED)
        {
            _swipeEventFired = true;
            return SwipeAction.Right;
        }

        return SwipeAction.None;
    }

    public void resetMotionState()
    {
        _swipeEventFired = false;
        _layoutTouchMotionLastXPosition = -1;
        _layoutTouchMotionLastYPosition = -1;
    }

    public void swipeLeft()
    {
        if (_swipeLeftCallback != null)
        {
            _swipeLeftCallback.apply(null);
        }
    }

    public void swipeRight()
    {
        if (_swipeRightCallback != null)
        {
            _swipeRightCallback.apply(null);
        }
    }

    public void swipeDown()
    {
        if (_swipeDownCallback != null)
        {
            _swipeDownCallback.apply(null);
        }
    }
}
