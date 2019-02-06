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

import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioTrack;
import com.media.notabadplayer.R;

import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkNotNull;

class AlbumListAdapter extends BaseAdapter
{
    private Context _context;
    private ArrayList<AudioTrack> _tracks;
    
    public AlbumListAdapter(@NonNull Context context, ArrayList<AudioTrack> tracks)
    {
        this._context = context;
        this._tracks = tracks;
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

            AudioTrack firstTrack = _tracks.get(0);

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

        AudioTrack item = (AudioTrack) getItem(position);
        
        View listItem = LayoutInflater.from(_context).inflate(R.layout.item_album_song, parent, false);
        
        String dataTitle = item.title;
        String dataTrackNum = item.trackNum;
        String dataDuration = item.duration;
        
        TextView title = checkNotNull((TextView)listItem.findViewById(R.id.title), "Base adapter is expecting a valid text view for position " + String.valueOf(position) + "/" + String.valueOf(_tracks.size()));
        
        if (dataTitle == null || dataTitle.length() == 0)
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
        
        TextView duration = checkNotNull((TextView)listItem.findViewById(R.id.duration), "Base adapter is expecting a valid text view");

        duration.setText(dataDuration);
        
        // Color
        boolean isPlayingTrack = false;
        
        AudioPlaylist playlist = AudioPlayer.getShared().getPlaylist();
        
        if (playlist != null)
        {
            AudioTrack track = playlist.getPlayingTrack();
            
            if (track != null)
            {
                if (track.equals(item))
                {
                    isPlayingTrack = true;
                }
            }
        }
        
        Resources resources = parent.getResources();
        
        if (!isPlayingTrack)
        {
            listItem.setBackgroundColor(resources.getColor(R.color.colorTransparent));
        }
        else
        {
            listItem.setBackgroundColor(resources.getColor(R.color.colorAlbumItemPlayingBackground));
        }
        
        return listItem;
    }
}
