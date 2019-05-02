package com.media.notabadplayer.View.Player;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.google.common.base.Function;

public class QuickPlayerLayout extends LinearLayout
{
    private static float SWIPE_UP_GESTURE_X_DISTANCE_REQUIRED = 95;
    private static float SWIPE_UP_GESTURE_Y_DISTANCE_REQUIRED = 120;
    
    private boolean _onInterceptTouchEventResult = false;
    private float _layoutTouchMotionLastXPosition = -1;
    private float _layoutTouchMotionLastYPosition = -1;
    
    private Function<Void, Void> _swipeUpCallback = null;
    
    public QuickPlayerLayout(Context context) {
        super(context, null);
    }

    public QuickPlayerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);
        setFocusable(true);
    }
    
    public void setSwipeUpCallback(Function<Void, Void> callback)
    {
        _swipeUpCallback = callback;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // This method may be called either because swipe up condition is met or if an empty area
        // is tapped
        if (!_onInterceptTouchEventResult)
        {
            return false;
        }
        
        _onInterceptTouchEventResult = false;
        
        if (_swipeUpCallback != null)
        {
            _swipeUpCallback.apply(null);
        }
        
        return true;
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Need to override this method in order to pass the touch event to children if the swipe up
        // does not happen
        _onInterceptTouchEventResult = respondToSwipeUp(ev);
        return _onInterceptTouchEventResult;
    }
    
    public boolean respondToSwipeUp(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_MOVE)
        {
            float currentX = event.getX();
            float currentY = event.getY();

            if (_layoutTouchMotionLastYPosition == -1)
            {
                _layoutTouchMotionLastXPosition = currentX;
                _layoutTouchMotionLastYPosition = currentY;
                return false;
            }

            float diffX = currentX - _layoutTouchMotionLastXPosition;
            float diffY = currentY - _layoutTouchMotionLastYPosition;

            if (Math.abs(diffY) > SWIPE_UP_GESTURE_Y_DISTANCE_REQUIRED &&
                    diffY < 0 &&
                    Math.abs(diffX) <= SWIPE_UP_GESTURE_X_DISTANCE_REQUIRED)
            {
                _layoutTouchMotionLastXPosition = -1;
                _layoutTouchMotionLastYPosition = -1;
                return true;
            }
        }

        if (event.getAction() == MotionEvent.ACTION_UP)
        {
            _layoutTouchMotionLastXPosition = -1;
            _layoutTouchMotionLastYPosition = -1;
        }

        return false;
    }
}
