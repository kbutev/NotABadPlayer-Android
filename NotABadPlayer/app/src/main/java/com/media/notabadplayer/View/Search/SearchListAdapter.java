package com.media.notabadplayer.View.Search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Utilities.UIAnimations;
import com.media.notabadplayer.View.Other.TrackListFavoritesChecker;
import com.media.notabadplayer.View.Other.TrackListHighlightedChecker;

public class SearchListAdapter extends BaseAdapter
{
    private Context _context;
    private List<BaseAudioTrack> _tracks;
    private final boolean _highlightAnimation;
    
    private @NonNull TrackListHighlightedChecker _highlightedChecker;
    private @NonNull TrackListFavoritesChecker _favoritesChecker;
    
    private HashSet<View> _listViews = new HashSet<>();

    private View _currentlySelectedView = null;
    private int _currentlySelectedViewListIndex = -1;

    public SearchListAdapter(@NonNull Context context, 
                             @NonNull List<BaseAudioTrack> tracks, 
                             @Nullable TrackListHighlightedChecker highlightedChecker,
                             @Nullable TrackListFavoritesChecker favoriteChecker)
    {
        this(context, tracks, highlightedChecker, favoriteChecker, true);
    }

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

        // Views set update
        _listViews.add(convertView);
        
        // Item update
        BaseAudioTrack item = (BaseAudioTrack) getItem(position);
        
        ImageView cover = listItem.findViewById(R.id.albumCover);
        
        if (!item.getArtCover().isEmpty())
        {
            cover.setImageURI(Uri.parse(Uri.decode(item.getArtCover())));
        }
        else
        {
            cover.setImageDrawable(parent.getResources().getDrawable(R.drawable.cover_art_none));
        }
        
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

        Resources resources = parent.getResources();

        if (!isHighlighted)
        {
            listItem.setBackgroundColor(resources.getColor(R.color.transparent));
        }
        else
        {
            _currentlySelectedView = listItem;
            _currentlySelectedViewListIndex = position;
            _currentlySelectedView.setBackgroundColor(resources.getColor(R.color.currentHighlightedTrack));
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
            _currentlySelectedView.setBackgroundColor(_context.getResources().getColor(R.color.transparent));
            UIAnimations.getShared().listItemAnimations.animateTap(_context, _currentlySelectedView);
        }
        else
        {
            _currentlySelectedView.setBackgroundColor(_context.getResources().getColor(R.color.currentHighlightedTrack));
        }
    }

    public void deselectCurrentItem()
    {
        UIAnimations.getShared().listItemAnimations.endAll();

        Iterator<View> iterator = _listViews.iterator();

        while (iterator.hasNext())
        {
            View child = iterator.next();
            child.setBackgroundColor(_context.getResources().getColor(R.color.transparent));
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
