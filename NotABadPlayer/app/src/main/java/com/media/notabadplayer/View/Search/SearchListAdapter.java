package com.media.notabadplayer.View.Search;

import java.util.ArrayList;
import java.util.Iterator;
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
import android.widget.TextView;

import com.google.common.base.Function;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Utilities.ArtImageFetcher;
import com.media.notabadplayer.Utilities.InternalAdapterView;
import com.media.notabadplayer.Utilities.InternalAdapterViews;
import com.media.notabadplayer.Utilities.UIAnimations;
import com.media.notabadplayer.View.Other.TrackListFavoritesChecker;
import com.media.notabadplayer.View.Other.TrackListHighlightedChecker;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class SearchListAdapter extends BaseAdapter
{
    private final Context _context;
    private final List<BaseAudioTrack> _tracks;
    private final boolean _highlightAnimation;
    
    private final @NonNull TrackListHighlightedChecker _highlightedChecker;
    private final @NonNull TrackListFavoritesChecker _favoritesChecker;

    private final @NonNull ArtImageFetcher _artImageFetcher;

    private final InternalAdapterViews _listViews = new InternalAdapterViews();

    private View _currentlySelectedView = null;
    private int _currentlySelectedViewListIndex = -1;

    private final Drawable _coverArtNone;
    private final int _transparentColor;
    private final int _highlightedColor;

    public SearchListAdapter(@NonNull Context context,
                             @NonNull List<BaseAudioTrack> tracks,
                             @Nullable TrackListHighlightedChecker highlightedChecker,
                             @Nullable TrackListFavoritesChecker favoriteChecker,
                             boolean highlightAnimation)
    {
        this._context = context;
        this._tracks = new ArrayList<>(tracks);
        this._highlightAnimation = highlightAnimation;
        this._highlightedChecker = highlightedChecker != null ? highlightedChecker : new SearchListAdapter.DummyHighlightedChecker();
        this._favoritesChecker = favoriteChecker != null ? favoriteChecker : new SearchListAdapter.DummyFavoritesChecker();
        this._artImageFetcher = new ArtImageFetcher(context);

        this._coverArtNone = context.getResources().getDrawable(R.drawable.cover_art_none);
        this._transparentColor = context.getResources().getColor(R.color.transparent);
        this._highlightedColor = context.getResources().getColor(R.color.currentHighlightedTrack);
    }
    
    public int getCount()
    {
        return _tracks.size();
    }

    public Object getItem(int position)
    {
        return _tracks.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) 
    {
        // Album track item
        if (convertView == null)
        {
            convertView = LayoutInflater.from(_context).inflate(R.layout.item_search_result, parent, false);
        }

        View listItem = convertView;

        // Views update
        InternalAdapterView searchView = _listViews.add(listItem);
        searchView.reset();
        
        // Item update
        BaseAudioTrack item = (BaseAudioTrack) getItem(position);
        
        final ImageView cover = listItem.findViewById(R.id.albumCover);

        Function<Bitmap, Void> callback = new Function<Bitmap, Void>() {
            @NullableDecl
            @Override
            public Void apply(@NullableDecl Bitmap imageBitmap) {
                if (imageBitmap != null) {
                    cover.setImageBitmap(imageBitmap);
                } else {
                    cover.setImageDrawable(_coverArtNone);
                }
                return null;
            }
        };

        cover.setImageDrawable(_coverArtNone);

        searchView.token = _artImageFetcher.fetchAsync(item.getArtCover(), callback);

        TextView titleText = listItem.findViewById(R.id.titleText);
        titleText.setText(item.getTitle());

        TextView albumTitleText = listItem.findViewById(R.id.albumTitleText);
        albumTitleText.setText(item.getAlbumTitle());

        TextView descriptionText = listItem.findViewById(R.id.descriptionText);
        descriptionText.setText(item.getDuration());

        ImageView favoriteIcon = listItem.findViewById(R.id.favoriteIcon);

        if (_favoritesChecker.isMarkedFavorite(item)) {
            favoriteIcon.setVisibility(View.VISIBLE);
        } else {
            favoriteIcon.setVisibility(View.GONE);
        }

        // Highlighted
        boolean isHighlighted = _highlightedChecker.shouldBeHighlighted(item);

        if (!isHighlighted)
        {
            listItem.setBackgroundColor(_transparentColor);
        }
        else
        {
            _currentlySelectedView = listItem;
            _currentlySelectedViewListIndex = position;
            _currentlySelectedView.setBackgroundColor(_highlightedColor);
        }
        
        return listItem;
    }

    public boolean isItemSelectedForTrack(@NonNull BaseAudioTrack track)
    {
        int position = -1;

        for (int e = 0; e < _tracks.size(); e++)
        {
            BaseAudioTrack listTrack = _tracks.get(e);

            if (listTrack.equals(track))
            {
                position = e;
                break;
            }
        }

        if (position == -1)
        {
            return false;
        }

        return position == _currentlySelectedViewListIndex;
    }

    public void selectItem(@NonNull View view, int position)
    {
        deselectCurrentItem();

        _currentlySelectedView = view;
        _currentlySelectedViewListIndex = position;

        if (_highlightAnimation)
        {
            _currentlySelectedView.setBackgroundColor(_transparentColor);
            UIAnimations.getShared().listItemAnimations.animateTap(_context, _currentlySelectedView);
        }
        else
        {
            _currentlySelectedView.setBackgroundColor(_highlightedColor);
        }
    }

    public void deselectCurrentItem()
    {
        UIAnimations.getShared().listItemAnimations.endAll();

        Iterator<InternalAdapterView> iterator = _listViews.iterator();

        while (iterator.hasNext())
        {
            View child = iterator.next().view;
            child.setBackgroundColor(_transparentColor);
        }
    }
    
    class DummyHighlightedChecker implements TrackListHighlightedChecker {
        public boolean shouldBeHighlighted(@NonNull BaseAudioTrack track) { return false; }
    }

    class DummyFavoritesChecker implements TrackListFavoritesChecker {
        public boolean isMarkedFavorite(@NonNull BaseAudioTrack track) {
            return false;
        }
    }
}
