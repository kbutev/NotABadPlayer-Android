package com.media.notabadplayer.View.Playlist;

import java.util.ArrayList;
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

import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Utilities.UIAnimations;

class PlaylistListAdapter extends BaseAdapter
{
    private Context _context;
    private String _playlistName;
    private ArrayList<AudioTrack> _tracks;
    private boolean _isPlaylist;
    
    private View _currentlySelectedView = null;
    
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
            
            AudioTrack track = _tracks.get(0);

            if (!track.artCover.isEmpty() && !_isPlaylist)
            {
                String uri = Uri.decode(track.artCover);
                
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
            
            if (!track.artist.isEmpty() && !_isPlaylist)
            {
                albumArtist.setVisibility(View.VISIBLE);
                albumArtist.setText(track.artist);
            }
            else
            {
                albumArtist.setVisibility(View.GONE);
            }
            
            albumDescription.setText(getAlbumDescription());
            
            return header;
        }
        
        // Item (follow position > 0)
        position--;

        AudioTrack item = (AudioTrack) getItem(position);
        
        View listItem = LayoutInflater.from(_context).inflate(R.layout.item_album_track, parent, false);
        
        String dataTitle = item.title;
        String dataTrackNum = item.trackNum;
        String dataDuration = item.duration;
        
        TextView title = checkNotNull((TextView)listItem.findViewById(R.id.title), "Base adapter is expecting a valid text view for position " + String.valueOf(position) + "/" + String.valueOf(_tracks.size()));
        
        if (dataTitle.length() == 0)
        {
            title.setText(R.string.title_unknown);
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
            trackNum.setText(R.string.zero_track_num_symbol);
        }
        
        TextView duration = checkNotNull((TextView)listItem.findViewById(R.id.duration), "Base adapter is expecting a valid text view");

        duration.setText(dataDuration);
        
        // Select playing track
        boolean isPlayingTrack = false;
        
        AudioPlaylist playlist = AudioPlayer.getShared().getPlaylist();
        
        if (playlist != null)
        {
            AudioTrack track = playlist.getPlayingTrack();
            
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
            listItem.setBackgroundColor(resources.getColor(R.color.currentlyPlayingTrack));
            _currentlySelectedView = listItem;
        }
        
        return listItem;
    }
    
    private String getAlbumDescription()
    {
        double totalDuration = 0;
        
        for (AudioTrack track: _tracks) 
        {
            totalDuration += track.durationInSeconds;
        }
        
        String tracks = _context.getResources().getString(R.string.tracks);
        String total_duration = _context.getResources().getString(R.string.total_duration);
        
        return String.valueOf(_tracks.size()) + " " + tracks + ", " + total_duration + " " + AudioTrack.secondsToString(totalDuration);
    }
    
    public void selectItem(@NonNull View view)
    {
        deselectCurrentItem();
        
        view.setBackgroundColor(_context.getResources().getColor(R.color.transparent));
        
        _currentlySelectedView = view;
        
        UIAnimations.getShared().listItemAnimations.animateTap(_context, _currentlySelectedView);
    }
    
    public void deselectCurrentItem()
    {
        if (_currentlySelectedView != null)
        {
            UIAnimations.getShared().listItemAnimations.endAll();
            _currentlySelectedView.setBackgroundColor(_context.getResources().getColor(R.color.transparent));
        }

        _currentlySelectedView = null;
    }
}
