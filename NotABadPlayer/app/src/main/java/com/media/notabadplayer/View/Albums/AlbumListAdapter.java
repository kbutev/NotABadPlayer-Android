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
import android.widget.TextView;

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
        return _tracks.size();
    }
    
    public Object getItem(int position)
    {
        return null;
    }
    
    public long getItemId(int position)
    {
        return 0;
    }
    
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View listItem = convertView;

        if (listItem == null)
        {
            listItem = LayoutInflater.from(_context).inflate(R.layout.item_album_song, parent, false);
        }
        
        String dataTitle = _tracks.get(position).title;
        String dataTrackNum = _tracks.get(position).trackNum;
        String dataDuration = _tracks.get(position).duration;
        
        TextView title = checkNotNull((TextView)listItem.findViewById(R.id.title), "Base adapter is expecting a valid text view");

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
        
        return listItem;
    }
}
