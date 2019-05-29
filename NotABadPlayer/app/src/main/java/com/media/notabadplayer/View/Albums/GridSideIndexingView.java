package com.media.notabadplayer.View.Albums;

import java.util.ArrayList;
import java.util.Collections;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.media.notabadplayer.R;
import com.media.notabadplayer.Utilities.UIAnimations;

public class GridSideIndexingView extends View {
    public static final int BOTTOM_PADDING = 10;
    
    private Context _context;
    private SectionIndexer _selectionIndexer = null;
    private Paint _paint;
    private String[] _sections;

    private GridView _view;
    private TextView _indexingTextCharacter;
    private ArrayList<Character> _alphabet = new ArrayList<>();
    
    public GridSideIndexingView(@NonNull Context context)
    {
        super(context);
        _context = context;
        init(_context);
    }

    public GridSideIndexingView(@NonNull Context context, AttributeSet attrs)
    {
        super(context, attrs);
        _context = context;
        init(_context);
    }

    public GridSideIndexingView(@NonNull Context context, AttributeSet attrs, int defStyle) 
    {
        super(context, attrs, defStyle);
        _context = context;
        init(_context);
    }

    private void init(@NonNull Context context)
    {
        _paint = new Paint();
        
        TypedValue outValue = new TypedValue();
        
        if (context.getTheme().resolveAttribute(R.attr.gridSideIndexingTextColor, outValue, true))
        {
            _paint.setColor(context.getResources().getColor(outValue.resourceId));
        }
        else
        {
            _paint.setColor(context.getResources().getColor(R.color.gridSideIndexingText));
        }
        
        _paint.setTextSize(context.getResources().getDimension(R.dimen.gridSideIndexingTextSize));
        _paint.setTextAlign(Paint.Align.LEFT);
    }

    public void start(@NonNull GridView list, @NonNull TextView textCharacter)
    {
        _view = list;
        _indexingTextCharacter = textCharacter;
        _selectionIndexer = (SectionIndexer) list.getAdapter();

        Object[] sectionsArr = _selectionIndexer.getSections();
        _sections = new String[sectionsArr.length];
        for (int i = 0; i < sectionsArr.length; i++) {
            _sections[i] = sectionsArr[i].toString();
        }

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
            String title = titles.get(e);
            
            if (title.length() > 0)
            {
                char firstChar = title.charAt(0);

                if (!_alphabet.contains(firstChar))
                {
                    _alphabet.add(firstChar);
                }
            }
        }

        Collections.sort(_alphabet);
        
        if (_alphabet.isEmpty())
        {
            _alphabet.add('a');
        }
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

    public boolean onTouchEvent(MotionEvent event)
    {
        super.onTouchEvent(event);

        int y = (int) event.getY();
        int selectedIndex = (int) (((float) y / (float) getPaddedHeight()) * getAlphabet().size());
        int alphabetSize = getAlphabet().size();

        if (selectedIndex >= alphabetSize)
        {
            selectedIndex = alphabetSize - 1;
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

            int position = _selectionIndexer.getPositionForSection(selectedIndex);

            if (position != -1)
            {
                _view.setSelection(position);
                
                if (selectedIndex < alphabetSize)
                {
                    displayTextCharacter(getAlphabet().get(selectedIndex));
                }
            }
        }

        if (event.getAction() == MotionEvent.ACTION_UP)
        {
            fadeOutTextCharacter();
        }

        return true;
    }
    
    public void displayTextCharacter(char c)
    {
        CharSequence text = _indexingTextCharacter.getText();
        String character = String.valueOf(c);
        
        if (!text.equals(character))
        {
            _indexingTextCharacter.setText(character);
        }

        _indexingTextCharacter.setAlpha(1);
    }
    
    public void fadeOutTextCharacter()
    {
        UIAnimations.getShared().animateFadeOut(_context, _indexingTextCharacter);
    }
}