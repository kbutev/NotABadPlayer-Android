package com.media.notabadplayer.View.Albums;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Utilities.ArtImageFetcher;
import com.media.notabadplayer.Utilities.InternalAdapterView;
import com.media.notabadplayer.Utilities.InternalAdapterViews;
import com.media.notabadplayer.View.Other.GridSideIndexingView;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

class AlbumsTableAdapter extends BaseAdapter implements SectionIndexer
{
    private final @NonNull Context _context;
    private final @NonNull List<AudioAlbum> _albums;
    private final @NonNull GridSideIndexingView _sideSelector;

    private @NonNull final ArtImageFetcher _fetcher;

    private final InternalAdapterViews _listViews = new InternalAdapterViews();

    private final Drawable _artCoverNoneDrawable;
    
    public AlbumsTableAdapter(@NonNull Context context, @NonNull List<AudioAlbum> albums, @NonNull GridSideIndexingView sideSelector)
    {
        _context = context;
        _albums = new ArrayList<>(albums);
        _sideSelector = sideSelector;
        _fetcher = new ArtImageFetcher(_context);
        _artCoverNoneDrawable = context.getResources().getDrawable(R.drawable.cover_art_none);
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

        // Views update
        InternalAdapterView albumView = _listViews.add(listItem);
        albumView.reset();
        
        // Item
        AudioAlbum item = (AudioAlbum) getItem(position);
        String dataTitle = item.albumTitle;

        final ImageView cover = checkNotNull((ImageView)listItem.findViewById(R.id.cover), "Base adapter is expecting a valid image view");

        cover.setImageDrawable(_artCoverNoneDrawable);

        if (item.artCover.isValid())
        {
            albumView.token = _fetcher.fetchAsync(item.artCover, new Function<Bitmap, Void>() {
                @NullableDecl
                @Override
                public Void apply(@NullableDecl Bitmap input) {
                    if (input != null) {
                        cover.setImageBitmap(input);
                    } else {
                        cover.setImageDrawable(_artCoverNoneDrawable);
                    }

                    return null;
                }
            });
        }

        TextView title = checkNotNull((TextView)listItem.findViewById(R.id.title), "Base adapter is expecting a valid text view");
        
        if (dataTitle.length() == 0)
        {
            title.setText(R.string.albums_title_unknown);
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
    public int getPositionForSection(int selectedIndex)
    {
        ArrayList<Character> alphabet = _sideSelector.getAlphabet();

        // Check for invalid index
        if (selectedIndex >= alphabet.size()) {
            return 0;
        }

        // Select the first album whose title first char matches the selected index char
        int position = 0;
        Character selectedCharacter = alphabet.get(selectedIndex);
        
        for (int e = 0; e < _albums.size(); e++)
        {
            if (_albums.get(e).albumTitle.charAt(0) == selectedCharacter)
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
