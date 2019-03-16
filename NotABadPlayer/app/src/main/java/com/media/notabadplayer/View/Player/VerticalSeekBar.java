package com.media.notabadplayer.View.Player;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.support.v7.widget.AppCompatSeekBar;

public class VerticalSeekBar extends AppCompatSeekBar 
{
    private OnSeekBarChangeListener _onSeekListener;
    
    public VerticalSeekBar(Context context) {
        super(context);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setProgress(int progress)
    {
        super.setProgress(progress);
        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }
    
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(h, w, oldh, oldw);
    }
    
    @Override
    protected void onDraw(Canvas c) {
        c.rotate(-90);
        c.translate(-getHeight(),0);
        
        super.onDraw(c);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        
        int i = getMax() - (int) (getMax() * event.getY() / getHeight());
        
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                super.setProgress(i);
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                _onSeekListener.onProgressChanged(this, getProgress(), true);
                break;
            case MotionEvent.ACTION_UP:
                super.setProgress(i);
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                _onSeekListener.onProgressChanged(this, getProgress(), true);
                break;

            case MotionEvent.ACTION_CANCEL:
                break;
        }
        
        return true;
    }
    
    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        _onSeekListener = l;
    }
}
