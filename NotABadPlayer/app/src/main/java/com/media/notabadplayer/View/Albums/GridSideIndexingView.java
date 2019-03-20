package com.media.notabadplayer.View.Albums;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.SectionIndexer;

import java.util.ArrayList;
import java.util.Collections;

public class GridSideIndexingView extends View {
    public static final int BOTTOM_PADDING = 10;
    
    private ArrayList<Character> _alphabet = new ArrayList<>();
    
    private SectionIndexer _selectionIndexer = null;
    private GridView _view;
    private Paint _paint;
    private String[] _sections;

    public GridSideIndexingView(@NonNull Context context)
    {
        super(context);
        init();
    }

    public GridSideIndexingView(@NonNull Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public GridSideIndexingView(@NonNull Context context, AttributeSet attrs, int defStyle) 
    {
        super(context, attrs, defStyle);
        init();
    }

    private void init()
    {
        setBackgroundColor(0x44FFFFFF);
        _paint = new Paint();
        _paint.setColor(0xFFA6A9AA);
        _paint.setTextSize(20);
        _paint.setTextAlign(Paint.Align.CENTER);
    }
    
    public ArrayList<Character> getAlphabet()
    {
        return _alphabet;
    }

    public void updateAlphabet(ArrayList<String> titles)
    {
        _alphabet = new ArrayList<>();
        
        for (int e = 0; e < titles.size(); e++)
        {
            char firstChar = titles.get(e).charAt(0);

            if (!_alphabet.contains(firstChar))
            {
                _alphabet.add(firstChar);
            }
        }

        Collections.sort(_alphabet);
        
        if (_alphabet.isEmpty())
        {
            _alphabet.add('a');
        }
    }

    public void setGridView(GridView _list) 
    {
        _view = _list;
        _selectionIndexer = (SectionIndexer) _list.getAdapter();

        Object[] sectionsArr = _selectionIndexer.getSections();
        _sections = new String[sectionsArr.length];
        for (int i = 0; i < sectionsArr.length; i++) {
            _sections[i] = sectionsArr[i].toString();
        }

    }

    public boolean onTouchEvent(MotionEvent event) 
    {
        super.onTouchEvent(event);
        
        int y = (int) event.getY();
        float selectedIndex = ((float) y / (float) getPaddedHeight()) * getAlphabet().size();
        
        if (selectedIndex >= getAlphabet().size())
        {
            selectedIndex = getAlphabet().size() - 1;
        }
        
        if (selectedIndex < 0)
        {
            selectedIndex = 0;
        }
        
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE)
        {
            if (_selectionIndexer == null)
            {
                _selectionIndexer = (SectionIndexer) _view.getAdapter();
            }
            
            int position = _selectionIndexer.getPositionForSection((int) selectedIndex);
            
            if (position != -1)
            {
                _view.setSelection(position);
            }
        }
        
        return true;
    }

    protected void onDraw(Canvas canvas) 
    {
        int viewHeight = getPaddedHeight();
        float charHeight = ((float) viewHeight) / (float) _sections.length;

        float widthCenter = getMeasuredWidth() / 2;
        for (int i = 0; i < _sections.length; i++) {
            canvas.drawText(String.valueOf(_sections[i]), widthCenter, charHeight + (i * charHeight), _paint);
        }
        super.onDraw(canvas);
    }

    private int getPaddedHeight()
    {
        return getHeight() - BOTTOM_PADDING;
    }
}