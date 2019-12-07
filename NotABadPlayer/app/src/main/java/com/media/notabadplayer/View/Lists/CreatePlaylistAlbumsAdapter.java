package com.media.notabadplayer.View.Lists;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Function;
import com.media.notabadplayer.Audio.Model.AudioAlbum;
import com.media.notabadplayer.Audio.AudioInfo;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.R;

public class CreatePlaylistAlbumsAdapter extends BaseAdapter
{
    private @NonNull List<AudioAlbum> _albums;
    
    private @NonNull Context _context;
    
    private @NonNull AudioInfo _audioInfo;
    
    private int _selectedAlbumPosition;
    private LinearLayout _selectedAlbum;
    private CreatePlaylistAlbumsTracksAdapter _selectedAlbumAdapter;
    private List<BaseAudioTrack> _selectedTracks = new ArrayList<>();
    
    private @NonNull Function<BaseAudioTrack, Void> _onItemClick;
    
    public CreatePlaylistAlbumsAdapter(@NonNull Context context,
                                       @NonNull AudioInfo audioInfo,
                                       @NonNull List<AudioAlbum> albums,
                                       @NonNull Function<BaseAudioTrack, Void> onItemClick)
    {
        this._albums = albums;
        this._context = context;
        this._audioInfo = audioInfo;
        this._selectedAlbumPosition = -1;
        this._selectedAlbum = null;
        this._selectedAlbumAdapter = null;
        this._onItemClick = onItemClick;
    }

    public int getCount()
    {
        return _albums.size();
    }

    public Object getItem(int position)
    {
        return _albums.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public @NonNull View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        if (_selectedAlbumPosition == position)
        {
            return getViewSelected(position, convertView, parent);
        }
        
        return getViewUnselected(position, convertView, parent);
    }
    
    private View getViewUnselected(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = LayoutInflater.from(_context).inflate(R.layout.item_playlist_album_expanded, parent, false);
        }
        
        View listItem = convertView;
        
        AudioAlbum album = _albums.get(position);

        ImageView cover = listItem.findViewById(R.id.albumCover);

        if (!album.albumCover.isEmpty())
        {
            String uri = Uri.decode(album.albumCover);

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
        title.setText(album.albumTitle);
        
        LinearLayout tracks = listItem.findViewById(R.id.tracks);
        tracks.setVisibility(View.GONE);
        tracks.removeAllViews();
        
        return listItem;
    }
    
    private View getViewSelected(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = LayoutInflater.from(_context).inflate(R.layout.item_playlist_album_expanded, parent, false);
        }
        
        View listItem = convertView;
        
        AudioAlbum album = _albums.get(position);

        ImageView cover = listItem.findViewById(R.id.albumCover);

        if (!album.albumCover.isEmpty())
        {
            String uri = Uri.decode(album.albumCover);

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
        title.setText(album.albumTitle);

        final List<BaseAudioTrack> tracks = _audioInfo.getAlbumTracks(album);
        
        if (tracks.size() > 0)
        {
            _selectedAlbum = listItem.findViewById(R.id.tracks);
            _selectedAlbum.setVisibility(View.VISIBLE);
            _selectedAlbum.removeAllViews();
            
            _selectedAlbumAdapter = new CreatePlaylistAlbumsTracksAdapter(_context, tracks, _selectedTracks);
            
            for (int i = 0; i < _selectedAlbumAdapter.getCount(); i++)
            {
                View view = _selectedAlbumAdapter.getView(i, null, _selectedAlbum);
                _selectedAlbum.addView(view);
                
                final int index = i;
                
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        _onItemClick.apply(tracks.get(index));
                    }
                });
            }
        }
        else
        {
            listItem.findViewById(R.id.tracks).setVisibility(View.GONE);
            ((LinearLayout)listItem.findViewById(R.id.tracks)).removeAllViews();
        }

        return listItem;
    }
    
    public int getSelectedAlbumPosition()
    {
        return _selectedAlbumPosition;
    }
    
    public void selectAlbum(int position)
    {
        _selectedAlbumPosition = position;
        
        notifyDataSetChanged();
    }

    public void deselectAlbum()
    {
        _selectedAlbumPosition = -1;

        if (_selectedAlbum != null)
        {
            _selectedAlbum.setVisibility(View.GONE);
            _selectedAlbum.removeAllViews();
            _selectedAlbumAdapter = null;
        }

        notifyDataSetChanged();
    }

    public void selectTrack(@NonNull BaseAudioTrack track)
    {
        if (!_selectedTracks.contains(track))
        {
            _selectedTracks.add(track);
            _selectedAlbumAdapter.selectTrack(track);
        }
    }
    
    public void deselectTrack(@NonNull BaseAudioTrack track)
    {
        _selectedTracks.remove(track);
    }

    public class CreatePlaylistAlbumsTracksAdapter extends BaseAdapter
    {
        private @NonNull List<BaseAudioTrack> _tracks;
        private @NonNull List<BaseAudioTrack> _selectedTracks;

        private Context _context;

        public CreatePlaylistAlbumsTracksAdapter(@NonNull Context context,
                                                 @NonNull List<BaseAudioTrack> tracks,
                                                 @NonNull List<BaseAudioTrack> selectedTracks)
        {
            this._tracks = tracks;
            this._selectedTracks = selectedTracks;
            this._context = context;
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
            if (convertView == null)
            {
                convertView = LayoutInflater.from(_context).inflate(R.layout.item_playlist_album_expanded_track, parent, false);
            }

            View listItem = convertView;

            BaseAudioTrack track = _tracks.get(position);
            boolean selected = hasSelectedItem(track);
            
            TextView title = listItem.findViewById(R.id.title);
            title.setText(track.getTitle());
            
            if (!selected)
            {
                title.setTextAppearance(_context, R.style.ListItemText);
            }
            else
            {
                title.setTextAppearance(_context, R.style.ListItemTextSelected);
            }
            
            TextView description = listItem.findViewById(R.id.description);
            description.setText(track.getDuration());

            if (!selected)
            {
                description.setTextAppearance(_context, R.style.ListItemSubtext);
            }
            else
            {
                description.setTextAppearance(_context, R.style.ListItemSubtextSelected);
            }

            return listItem;
        }
        
        public boolean hasSelectedItem(@NonNull BaseAudioTrack track)
        {
            return _selectedTracks.contains(track);
        }
        
        public void selectTrack(@NonNull BaseAudioTrack track)
        {
            if (!hasSelectedItem(track))
            {
                _selectedTracks.add(track);
            }

            notifyDataSetChanged();
        }
    }
}
