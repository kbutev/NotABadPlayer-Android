package com.media.notabadplayer.View.Lists;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.common.base.Function;
import com.media.notabadplayer.Audio.Model.AudioArtCover;
import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Utilities.ArtImageFetcher;

class ListAdapter extends BaseAdapter
{
    private final Context _context;
    private final List<BaseAudioPlaylist> _playlists;

    private final @NonNull ArtImageFetcher _artImageFetcher;

    private boolean _editMode = false;
    
    private @NonNull Function<Integer, Void> _onRemoveButton;

    public ListAdapter(@NonNull Context context,
                       @NonNull List<BaseAudioPlaylist> playlists,
                       @NonNull Function<Integer, Void> onRemoveButton)
    {
        this._playlists = new ArrayList<>(playlists);
        this._context = context;
        this._onRemoveButton = onRemoveButton;
        this._artImageFetcher = new ArtImageFetcher(context);
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

        BaseAudioPlaylist playlist = _playlists.get(position);
        
        View listItem = convertView;
        
        ImageView cover = listItem.findViewById(R.id.albumCover);
        setCoverImage(cover, playlist);
        
        TextView title = listItem.findViewById(R.id.title);
        title.setText(playlist.getName());
        
        TextView description = listItem.findViewById(R.id.description);
        String tracksText = _context.getResources().getString(R.string.albums_tracks);
        description.setText(String.valueOf(playlist.size()) + " " + tracksText);
        
        // Remove button is never displayed for temporary playlists
        ImageButton removeButton = listItem.findViewById(R.id.removeButton);
        
        if (_editMode && !playlist.isTemporary())
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
    
    void setCoverImage(@NonNull ImageView cover, @NonNull BaseAudioPlaylist playlist) {
        // Temporary playlists may have a "fixed" image cover, try to use that
        if (playlist.isTemporary())
        {
            boolean success = setCoverImageCustom(cover, playlist);
            
            if (success) 
            {
                return;
            }
        }
        
        AudioArtCover artCover = null;

        if (playlist.size() > 0)
        {
            artCover = playlist.getTrack(0).getArtCover();
        }
        
        if (artCover != null)
        {
            setCover(cover, artCover);
        }
        else
        {
            setCoverImageNone(cover);
        }
    }
    
    void setCover(@NonNull ImageView cover, @Nullable AudioArtCover artCover)
    {
        Bitmap artCoverBitmap = _artImageFetcher.fetch(artCover);

        if (artCoverBitmap != null)
        {
            cover.setImageBitmap(artCoverBitmap);
            cover.setScaleType(ImageView.ScaleType.FIT_XY);
        }
        else
        {
            setCoverImageNone(cover);
        }
    }
    
    void setCoverImageNone(@NonNull ImageView cover) 
    {
        cover.setImageDrawable(_context.getResources().getDrawable(R.drawable.cover_art_none));
        cover.setScaleType(ImageView.ScaleType.FIT_XY);
    }
    
    boolean setCoverImageCustom(@NonNull ImageView cover, @NonNull BaseAudioPlaylist playlist)
    {
        // Favorites playlist has a specific image cover
        String favoritesName = cover.getResources().getString(R.string.playlist_name_favorites);
        if (playlist.getName().equals(favoritesName)) {
            cover.setImageDrawable(_context.getResources().getDrawable(R.drawable.shiny_star_small));
            cover.setScaleType(ImageView.ScaleType.CENTER);
            return true;
        }
        
        return false;
    }
}
