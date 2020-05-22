package com.media.notabadplayer.View.Other;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Model.OpenPlaylistOptions;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Utilities.StringUtilities;
import com.media.notabadplayer.Utilities.UIAnimations;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public abstract class TrackListAdapter extends BaseAdapter {
    protected Context _context;
    private String _playlistName;
    private List<BaseAudioTrack> _tracks;
    private final boolean _isPlaylist;
    private final boolean _highlightAnimation;
    
    private @NonNull OpenPlaylistOptions _options;

    private @NonNull TrackListHighlightedChecker _highlightedChecker;
    private @NonNull TrackListFavoritesChecker _favoritesChecker;

    private HashSet<View> _listViews = new HashSet<>();

    private View _currentlySelectedView = null;
    private int _currentlySelectedViewListIndex = -1;

    public TrackListAdapter(@NonNull Context context,
                            @NonNull BaseAudioPlaylist playlist,
                            @NonNull OpenPlaylistOptions options,
                            @Nullable TrackListHighlightedChecker highlightedChecker,
                            @Nullable TrackListFavoritesChecker favoritesChecker,
                            boolean highlightAnimation)
    {
        this._context = context;
        this._playlistName = playlist.getName();
        this._tracks = playlist.getTracks();
        this._isPlaylist = !playlist.isAlbum();
        this._highlightAnimation = highlightAnimation;
        this._options = options;
        this._highlightedChecker = highlightedChecker != null ? highlightedChecker : new TrackListAdapter.DummyHighlightedChecker();
        this._favoritesChecker = favoritesChecker != null ? favoritesChecker : new TrackListAdapter.DummyFavoritesChecker();
    }
    
    // Inflate views - can be overriden

    public abstract @NonNull View getLayout(@NonNull ViewGroup parent);

    public abstract @NonNull TextView getTitleView(@NonNull View listItem);

    public abstract @NonNull TextView getTrackNumView(@NonNull View listItem);

    public abstract @NonNull TextView getDescriptionView(@NonNull View listItem);

    public abstract @NonNull ImageView getFavoriteIcon(@NonNull View listItem);
    
    // Properties
    
    public int getCount()
    {
        return _tracks.size() + 1;
    }

    public Object getItem(int position)
    {
        return _tracks.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public boolean displaysHeader() 
    {
        return _options.displayHeader;
    }
    
    public boolean isHeaderVisible(@NonNull GridView view)
    {
        return view.getFirstVisiblePosition() == 0;
    }
    
    // Adapter

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        // Header (only for position = 0)
        if (displaysHeader() && position == 0)
        {
            return getViewHeader(position, convertView, parent);
        }

        // Album track item
        // Never reuse convert view, because of header
        convertView = getLayout(parent);

        View listItem = convertView;

        // Views set update
        _listViews.add(listItem);

        // Items
        position--;

        BaseAudioTrack item = (BaseAudioTrack) getItem(position);

        String dataTitle = item.getTitle();
        String dataTrackNum = String.valueOf(item.getTrackNum());
        String dataDescription = "";
        String durationText = item.getDuration();
        String albumTitle = item.getAlbumTitle();
        
        if (_options.displayDescriptionDuration) {
            
            if (_options.displayDescriptionAlbumTitle) {
                dataDescription = String.format("%s - %s", durationText, albumTitle);
            } else {
                dataDescription = durationText;
            }
        } else {
            if (_options.displayDescriptionAlbumTitle) {
                dataDescription = albumTitle;
            }
        }

        TextView title = getTitleView(listItem);
        if (dataTitle.length() == 0)
        {
            title.setText(R.string.albums_title_unknown);
        }
        else
        {
            title.setText(dataTitle);
        }

        TextView trackNumText = getTrackNumView(listItem);
        
        if (_options.displayTrackNumber) 
        {
            if (!dataTrackNum.equals("0"))
            {
                trackNumText.setText(dataTrackNum);
            }
            else
            {
                trackNumText.setText(R.string.albums_zero_track_num_symbol);
            }
        } 
        else 
        {
            trackNumText.setVisibility(View.GONE);
        }

        TextView descriptionText = getDescriptionView(listItem);

        descriptionText.setText(dataDescription);

        ImageView favoriteIcon = getFavoriteIcon(listItem);
        
        if (_options.displayFavoriteIcon && _favoritesChecker.isMarkedFavorite(item)) {
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

    public View getViewHeader(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View header = LayoutInflater.from(_context).inflate(R.layout.item_album_header, parent, false);

        ImageView albumCover = header.findViewById(R.id.albumCover);
        TextView albumTitle = header.findViewById(R.id.albumTitle);
        TextView albumArtist = header.findViewById(R.id.albumArtist);
        TextView albumDescription = header.findViewById(R.id.albumDescription);

        BaseAudioTrack track = _tracks.get(0);

        if (!track.getArtCover().isEmpty() && !_isPlaylist)
        {
            String uri = Uri.decode(track.getArtCover());

            if (uri != null)
            {
                albumCover.setImageURI(Uri.parse(uri));
                albumCover.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            albumCover.setVisibility(View.GONE);
        }

        albumTitle.setText(_playlistName);

        if (!track.getArtist().isEmpty() && !_isPlaylist)
        {
            albumArtist.setVisibility(View.VISIBLE);
            albumArtist.setText(track.getArtist());
        }
        else
        {
            albumArtist.setVisibility(View.GONE);
        }

        albumDescription.setText(getAlbumDescription());
        
        return header;
    }

    private String getAlbumDescription()
    {
        double totalDuration = 0;

        for (BaseAudioTrack track: _tracks)
        {
            totalDuration += track.getDurationInSeconds();
        }

        String tracks = _context.getResources().getString(R.string.albums_tracks);
        String total_duration = _context.getResources().getString(R.string.albums_total_duration);

        return String.valueOf(_tracks.size()) + " " + tracks + ", " + total_duration + " " + StringUtilities.secondsToString(totalDuration);
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
        public boolean shouldBeHighlighted(@NonNull BaseAudioTrack track) {
            return false;
        }
    }

    class DummyFavoritesChecker implements TrackListFavoritesChecker {
        public boolean isMarkedFavorite(@NonNull BaseAudioTrack track) {
            return false;
        }
    }
}
