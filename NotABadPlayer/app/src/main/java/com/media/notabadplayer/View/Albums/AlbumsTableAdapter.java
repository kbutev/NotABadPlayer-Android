package com.media.notabadplayer.View.Albums;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import static com.google.common.base.Preconditions.checkNotNull;

import com.media.notabadplayer.Audio.AudioAlbum;
import com.media.notabadplayer.R;
import com.media.notabadplayer.View.Other.GridSideIndexingView;

class AlbumsTableAdapter extends BaseAdapter implements SectionIndexer
{
    private Context _context;
    private List<AudioAlbum> _albums;
    private GridSideIndexingView _sideSelector;
    
    public AlbumsTableAdapter(@NonNull Context context, List<AudioAlbum> albums, GridSideIndexingView sideSelector)
    {
        this._context = context;
        this._albums = albums;
        this._sideSelector = sideSelector;
    }
    
    public int getCount()
    {
        return _albums.size();
    }

    public Object getItem(int position)
    {
        return _albums.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = LayoutInflater.from(_context).inflate(R.layout.item_album, parent, false);
        }
        
        View listItem = convertView;
        
        // Item
        AudioAlbum item = (AudioAlbum) getItem(position);
        String dataTitle = item.albumTitle;
        String dataCover = item.albumCover;
        
        ImageView cover = checkNotNull((ImageView)listItem.findViewById(R.id.cover), "Base adapter is expecting a valid image view");
        
        if (!dataCover.isEmpty())
        {
            cover.setImageURI(Uri.parse(Uri.decode(dataCover)));
        }
        else
        {
            cover.setImageDrawable(parent.getResources().getDrawable(R.drawable.cover_art_none));
        }
        
        TextView title = checkNotNull((TextView)listItem.findViewById(R.id.title), "Base adapter is expecting a valid text view");
        
        if (dataTitle.length() == 0)
        {
            title.setText(R.string.title_unknown);
        }
        else
        {
            title.setText(dataTitle);
        }
        
        return listItem;
    }
    
    @Override
    public Object[] getSections() 
    {
        ArrayList<Character> alphabet = _sideSelector.getAlphabet();
        
        String[] chars = new String[alphabet.size()];
        
        for (int i = 0; i < alphabet.size(); i++)
        {
            chars[i] = String.valueOf(alphabet.get(i));
        }
        
        return chars;
    }
    
    @Override
    public int getPositionForSection(int sectionIndex) 
    {
        // Select the first album whose title first char matches the selected index char
        int position = 0;
        ArrayList<Character> alphabet = _sideSelector.getAlphabet();
        Character selectedCharacterIndex = alphabet.get(sectionIndex);
        
        for (int e = 0; e < _albums.size(); e++)
        {
            if (_albums.get(e).albumTitle.charAt(0) == selectedCharacterIndex)
            {
                position = e;
                break;
            }
        }
        
        return position;
    }
    
    @Override
    public int getSectionForPosition(int position) 
    {
        return 0;
    }
}
