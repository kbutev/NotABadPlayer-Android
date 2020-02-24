package com.media.notabadplayer.View.Lists;

import java.util.ArrayList;
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

import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.R;

public class CreatePlaylistTracksAdapter extends BaseAdapter
{
    private final @NonNull Context _context;
    private final @NonNull ArrayList<BaseAudioTrack> _tracks;

    public CreatePlaylistTracksAdapter(@NonNull Context context, @NonNull ArrayList<BaseAudioTrack> tracks)
    {
        this._context = context;
        this._tracks = new ArrayList<>(tracks);
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
            convertView = LayoutInflater.from(_context).inflate(R.layout.item_playlist_track, parent, false);
        }

        View listItem = convertView;

        BaseAudioTrack track = _tracks.get(position);

        ImageView cover = listItem.findViewById(R.id.albumCover);

        if (!track.getArtCover().isEmpty())
        {
            String uri = Uri.decode(track.getArtCover());

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
        title.setText(track.getTitle());

        TextView description = listItem.findViewById(R.id.description);
        description.setText(track.getDuration());

        return listItem;
    }
}
