package com.media.notabadplayer.View.Lists;

import java.util.List;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Function;
import com.media.notabadplayer.Audio.Model.AudioPlaylist;
import com.media.notabadplayer.R;

class ListAdapter extends BaseAdapter
{
    private List<AudioPlaylist> _playlists;

    private Context _context;
    
    private boolean _editMode = false;
    
    private @NonNull Function<Integer, Void> _onRemoveButton;

    public ListAdapter(@NonNull Context context,
                       @NonNull List<AudioPlaylist> playlists,
                       @NonNull Function<Integer, Void> onRemoveButton)
    {
        this._playlists = playlists;
        this._context = context;
        this._onRemoveButton = onRemoveButton;
    }

    public int getCount()
    {
        return _playlists.size();
    }

    public Object getItem(int position)
    {
        return _playlists.get(position);
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
            convertView = LayoutInflater.from(_context).inflate(R.layout.item_playlist, parent, false);
        }

        AudioPlaylist playlist = _playlists.get(position);
        String artCover = "";
        
        if (playlist.size() > 0)
        {
            artCover = playlist.getTrack(0).artCover;
        }
        
        View listItem = convertView;
        
        ImageView cover = listItem.findViewById(R.id.albumCover);
        
        if (!artCover.isEmpty())
        {
            String uri = Uri.decode(artCover);

            if (uri != null)
            {
                cover.setImageURI(Uri.parse(uri));
            }
            else
            {
                cover.setImageDrawable(_context.getResources().getDrawable(R.drawable.cover_art_none));
            }
        }
        else
        {
            cover.setImageDrawable(_context.getResources().getDrawable(R.drawable.cover_art_none));
        }
        
        TextView title = listItem.findViewById(R.id.title);
        title.setText(playlist.getName());
        
        TextView description = listItem.findViewById(R.id.description);
        String tracksText = _context.getResources().getString(R.string.albums_tracks);
        description.setText(String.valueOf(playlist.size()) + " " + tracksText);
        
        // Remove button is displayed only in edit mode and is never displayed for position one
        ImageButton removeButton = listItem.findViewById(R.id.removeButton);
        
        if (_editMode && position != 0)
        {
            removeButton.setVisibility(View.VISIBLE);
            
            final Integer index = position;
            
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _onRemoveButton.apply(index);
                }
            });
        }
        else
        {
            removeButton.setVisibility(View.GONE);
            
            removeButton.setOnClickListener(null);
        }
        
        return listItem;
    }
    
    public boolean isInEditMode()
    {
        return _editMode;
    }
    
    public void enterEditMode()
    {
        _editMode = true;
        
        notifyDataSetChanged();
    }

    public void leaveEditMode()
    {
        _editMode = false;

        notifyDataSetChanged();
    }
}
