package com.media.notabadplayer.View.Albums;

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

import com.media.notabadplayer.Audio.MediaPlayer;
import com.media.notabadplayer.Audio.MediaPlayerPlaylist;
import com.media.notabadplayer.Audio.MediaTrack;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Utilities.TrackSorting;

import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkNotNull;

class AlbumListAdapter extends BaseAdapter
{
    private Context _context;
    private ArrayList<MediaTrack> _tracks;
    
    public AlbumListAdapter(@NonNull Context context, ArrayList<MediaTrack> tracks)
    {
        this._context = context;
        this._tracks = TrackSorting.sortByTrackNumber(tracks);
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
    
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        // Init
        if (convertView == null)
        {
            LayoutInflater.from(_context).inflate(R.layout.item_table_album, parent, false);
        }
        
        // Header (position = 0)
        if (position == 0)
        {
            View header = LayoutInflater.from(_context).inflate(R.layout.header_album, parent, false);

            ImageView albumCover = header.findViewById(R.id.albumCover);
            TextView albumTitle = header.findViewById(R.id.albumTitle);

            MediaTrack firstTrack = _tracks.get(0);

            if (!firstTrack.artCover.isEmpty())
            {
                String uri = Uri.decode(firstTrack.artCover);
                
                if (uri != null)
                {
                    albumCover.setImageURI(Uri.parse(uri));
                    albumCover.setVisibility(View.VISIBLE);
                }
            }

            albumTitle.setText(firstTrack.albumTitle);

            return header;
        }
        
        // Item (follow position > 0)
        position--;

        MediaTrack item = (MediaTrack) getItem(position);
        
        View listItem = LayoutInflater.from(_context).inflate(R.layout.item_album_song, parent, false);
        
        String dataTitle = item.title;
        String dataTrackNum = item.trackNum;
        String dataDuration = item.duration;
        
        TextView title = checkNotNull((TextView)listItem.findViewById(R.id.title), "Base adapter is expecting a valid text view for position " + String.valueOf(position) + "/" + String.valueOf(_tracks.size()));
        
        if (dataTitle == null || dataTitle.length() == 0)
        {
            title.setText("Unknown");
        }
        else
        {
            title.setText(dataTitle);
        }
        
        TextView trackNum = checkNotNull((TextView)listItem.findViewById(R.id.trackNum), "Base adapter is expecting a valid text view");
        
        if (dataTrackNum != null && !dataTrackNum.equals("0"))
        {
            trackNum.setText(dataTrackNum);
        }
        
        TextView duration = checkNotNull((TextView)listItem.findViewById(R.id.duration), "Base adapter is expecting a valid text view");

        duration.setText(dataDuration);
        
        // Color
        boolean isPlayingTrack = false;
        
        MediaPlayerPlaylist playlist = MediaPlayer.getShared().getPlaylist();
        
        if (playlist != null)
        {
            MediaTrack track = playlist.getPlayingTrack();
            
            if (track != null)
            {
                if (track.title.equals(dataTitle) && track.albumTitle.equals(item.albumTitle))
                {
                    isPlayingTrack = true;
                }
            }
        }
        
        Resources resources = parent.getResources();
        
        if (!isPlayingTrack)
        {
            title.setTextColor(resources.getColor(R.color.colorAlbumItemText));
            duration.setTextColor(resources.getColor(R.color.colorAlbumItemTextSub));
        }
        else
        {
            title.setTextColor(resources.getColor(R.color.colorAlbumItemTextPlaying));
            duration.setTextColor(resources.getColor(R.color.colorAlbumItemTextSubPlaying));
        }
        
        return listItem;
    }
}
