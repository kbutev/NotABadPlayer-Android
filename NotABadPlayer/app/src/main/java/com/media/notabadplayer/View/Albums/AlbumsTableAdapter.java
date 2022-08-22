package com.media.notabadplayer.View.Albums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.media.notabadplayer.Utilities.ListAlphabet;
import com.media.notabadplayer.View.Other.GridSideIndexingView;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

class AlbumsTableAdapter extends BaseAdapter implements SectionIndexer
{
    private final @NonNull Context _context;
    private final @NonNull List<AudioAlbum> _albums;
    private final @NonNull GridSideIndexingView _sideSelector;

    private @NonNull final ArtImageFetcher _artImageFetcher;

    private final InternalAdapterViews _listViews = new InternalAdapterViews();

    private final Drawable _coverArtNone;
    
    public AlbumsTableAdapter(@NonNull Context context, @NonNull List<AudioAlbum> albums, @NonNull GridSideIndexingView sideSelector)
    {
        _context = context;
        _albums = new ArrayList<>(albums);
        _sideSelector = sideSelector;
        _artImageFetcher = new ArtImageFetcher(_context);
        _coverArtNone = context.getResources().getDrawable(R.drawable.cover_art_none);

        sortAlbums();
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
        
        // Item
        AudioAlbum item = (AudioAlbum) getItem(position);
        String dataTitle = item.albumTitle;

        final ImageView cover = checkNotNull((ImageView)listItem.findViewById(R.id.cover), "Base adapter is expecting a valid image view");
        albumView.fetchArtCoverAsync(cover, _artImageFetcher, item.artCover, _coverArtNone);

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
        List<Character> alphabet = _sideSelector.getAlphabet();
        
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
        List<Character> alphabet = _sideSelector.getAlphabet();

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

    private void sortAlbums()
    {
        Collections.sort(_albums, new Comparator<AudioAlbum>() {
            @Override
            public int compare(AudioAlbum first, AudioAlbum second) {
                String a = first.albumTitle;
                String b = second.albumTitle;
                return ListAlphabet.compareStrings(a, b);
            }
        });
    }
}
