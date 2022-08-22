package com.media.notabadplayer.View.Other;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.media.notabadplayer.R;
import com.media.notabadplayer.Utilities.ListAlphabet;
import com.media.notabadplayer.Utilities.UIAnimations;

public class GridSideIndexingView extends View {
    public static final int BOTTOM_PADDING = 10;
    
    private @NonNull Context _context;
    
    private @Nullable SectionIndexer _selectionIndexer = null;
    private Paint _paint;
    private String[] _sections = new String[1];

    private GridView _view;
    private TextView _indexingTextCharacter;
    private @NonNull ListAlphabet _alphabet = new ListAlphabet();
    
    public GridSideIndexingView(@NonNull Context context)
    {
        super(context);
        _context = context;
        initialize();
    }

    public GridSideIndexingView(@NonNull Context context, AttributeSet attrs)
    {
        super(context, attrs);
        _context = context;
        initialize();
    }

    public GridSideIndexingView(@NonNull Context context, AttributeSet attrs, int defStyle) 
    {
        super(context, attrs, defStyle);
        _context = context;
        initialize();
    }

    private void initialize()
    {
        _paint = new Paint();
        
        TypedValue outValue = new TypedValue();

        if (_context.getTheme().resolveAttribute(R.attr.gridSideIndexingTextColor, outValue, true))
        {
            _paint.setColor(_context.getResources().getColor(outValue.resourceId));
        }
        else
        {
            _paint.setColor(_context.getResources().getColor(R.color.gridSideIndexingText));
        }

        _paint.setTextSize(_context.getResources().getDimension(R.dimen.gridSideIndexingTextSize));
        _paint.setTextAlign(Paint.Align.LEFT);
    }
    
    public void start(@NonNull GridView list, @NonNull TextView textCharacter, @NonNull ArrayList<String> titles)
    {
        updateAlphabet(titles);
        
        _view = list;
        _indexingTextCharacter = textCharacter;
        _selectionIndexer = (SectionIndexer) list.getAdapter();
        
        if (_selectionIndexer == null || titles.size() == 0)
        {
            hide();
            return;
        }

        show();
        
        Object[] sectionsArr = _selectionIndexer.getSections();
        
        _sections = new String[sectionsArr.length];
        
        for (int i = 0; i < sectionsArr.length; i++) 
        {
            _sections[i] = sectionsArr[i].toString();
        }
        
        invalidate();
    }

    public void clear()
    {
        _alphabet.clear();
        hide();
    }
    
    public @NonNull List<Character> getAlphabet()
    {
        return _alphabet.getCharacters();
    }

    private void updateAlphabet(@NonNull ArrayList<String> titles)
    {
        _alphabet.updateAlphabet(titles);
    }
    
    protected void onDraw(Canvas canvas) 
    {
        if (_alphabet.isEmpty())
        {
            super.onDraw(canvas);
            return;
        }
        
        int viewHeight = getPaddedHeight();
        float charHeight = ((float) viewHeight) / (float) _sections.length;

        float widthCenter = getMeasuredWidth() / 2.0f;
        
        for (int i = 0; i < _sections.length; i++) 
        {
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

        if (_alphabet.isEmpty() || _selectionIndexer == null)
        {
            return true;
        }

        // What did we click on? Get the index of the label
        int y = (int) event.getY();
        int alphabetIndex = (int) (((float) y / (float) getPaddedHeight()) * getAlphabet().size());
        int alphabetSize = getAlphabet().size();

        if (alphabetIndex >= alphabetSize)
        {
            alphabetIndex = alphabetSize - 1;
        }

        if (alphabetIndex < 0)
        {
            alphabetIndex = 0;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE)
        {
            // Retrieve the first matching item for the index of the label we clicked on
            int exactItemIndex = _selectionIndexer.getPositionForSection(alphabetIndex);

            if (exactItemIndex != -1)
            {
                _view.setSelection(exactItemIndex);
                
                if (alphabetIndex < alphabetSize)
                {
                    displayTextCharacter(getAlphabet().get(alphabetIndex));
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

    private void show()
    {
        setVisibility(View.VISIBLE);
    }

    private void hide()
    {
        setVisibility(View.INVISIBLE);
        fadeOutTextCharacter();
    }
}