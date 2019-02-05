package com.media.notabadplayer.View.Albums;

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

import com.media.notabadplayer.Audio.MediaAlbum;
import com.media.notabadplayer.R;

import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkNotNull;

class AlbumsTableAdapter extends BaseAdapter implements SectionIndexer
{
    private Context _context;
    private ArrayList<MediaAlbum> _data;
    private GridSideIndexingView _sideSelector;
    
    public AlbumsTableAdapter(@NonNull Context context, ArrayList<MediaAlbum> albums, GridSideIndexingView sideSelector)
    {
        this._context = context;
        this._data = albums;
        this._sideSelector = sideSelector;
    }
    
    public int getCount()
    {
        return _data.size();
    }

    public Object getItem(int position)
    {
        return _data.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        // Init
        if (convertView == null)
        {
            LayoutInflater.from(_context).inflate(R.layout.item_table_album, parent, false);
        }
        
        // Item
        MediaAlbum item = (MediaAlbum) getItem(position);

        View listItem = LayoutInflater.from(_context).inflate(R.layout.item_table_album, parent, false);
        
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
        
        for (int e = 0; e < _data.size(); e++)
        {
            if (_data.get(e).albumTitle.charAt(0) == selectedCharacterIndex)
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
