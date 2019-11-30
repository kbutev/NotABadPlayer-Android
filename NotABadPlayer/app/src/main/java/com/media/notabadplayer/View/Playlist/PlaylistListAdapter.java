package com.media.notabadplayer.View.Playlist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

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
import static com.google.common.base.Preconditions.checkNotNull;

import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Audio.Model.AudioPlaylist;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Utilities.StringUtilities;
import com.media.notabadplayer.Utilities.UIAnimations;

class PlaylistListAdapter extends BaseAdapter
{
    private Context _context;
    private String _playlistName;
    private ArrayList<BaseAudioTrack> _tracks;
    private boolean _isPlaylist;

    private HashSet<View> _listViews = new HashSet<>();

    private View _currentlySelectedView = null;
    private int _currentlySelectedViewListIndex = -1;
    
    public PlaylistListAdapter(@NonNull Context context, @NonNull AudioPlaylist playlist)
    {
        this._context = context;
        this._playlistName = playlist.getName();
        this._tracks = playlist.getTracks();
        this._isPlaylist = !playlist.isAlbumPlaylist();
    }
    
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
    
    public boolean isHeaderVisible(@NonNull GridView view)
    {
        return view.getFirstVisiblePosition() == 0;
    }
    
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        // Header (position = 0)
        if (position == 0)
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

        // Album track item
        // Never reuse convert view, because of header
        convertView = LayoutInflater.from(_context).inflate(R.layout.item_album_track, parent, false);

        View listItem = convertView;

        // Views set update
        _listViews.add(listItem);

        // Item (follow position > 0)
        position--;

        BaseAudioTrack item = (BaseAudioTrack) getItem(position);

        String dataTitle = item.getTitle();
        String dataTrackNum = String.valueOf(item.getTrackNum());
        String dataDuration = item.getDuration();
        
        TextView title = checkNotNull((TextView)listItem.findViewById(R.id.title), "Base adapter is expecting a valid text view for position " + String.valueOf(position) + "/" + String.valueOf(_tracks.size()));
        
        if (dataTitle.length() == 0)
        {
            title.setText(R.string.albums_title_unknown);
        }
        else
        {
            title.setText(dataTitle);
        }
        
        TextView trackNum = checkNotNull((TextView)listItem.findViewById(R.id.trackNum), "Base adapter is expecting a valid text view");
        
        if (!dataTrackNum.equals("0"))
        {
            trackNum.setText(dataTrackNum);
        }
        else
        {
            trackNum.setText(R.string.albums_zero_track_num_symbol);
        }
        
        TextView duration = checkNotNull((TextView)listItem.findViewById(R.id.duration), "Base adapter is expecting a valid text view");

        duration.setText(dataDuration);
        
        // Select playing track
        boolean isPlayingTrack = false;
        
        AudioPlaylist playlist = Player.getShared().getPlaylist();
        
        if (playlist != null)
        {
            BaseAudioTrack track = playlist.getPlayingTrack();

            if (track.equals(item))
            {
                isPlayingTrack = true;
            }
        }
        
        Resources resources = parent.getResources();
        
        if (!isPlayingTrack)
        {
            listItem.setBackgroundColor(resources.getColor(R.color.transparent));
        }
        else
        {
            _currentlySelectedView = listItem;
            _currentlySelectedViewListIndex = position;
            _currentlySelectedView.setBackgroundColor(resources.getColor(R.color.currentlyPlayingTrack));
        }
        
        return listItem;
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

        _currentlySelectedView.setBackgroundColor(_context.getResources().getColor(R.color.transparent));
        UIAnimations.getShared().listItemAnimations.animateTap(_context, _currentlySelectedView);
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
}
