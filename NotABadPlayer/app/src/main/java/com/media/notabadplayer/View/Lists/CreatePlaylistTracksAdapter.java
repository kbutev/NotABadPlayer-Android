package com.media.notabadplayer.View.Lists;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Function;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.R;
import com.media.notabadplayer.Utilities.ArtImageFetcher;
import com.media.notabadplayer.Utilities.InternalAdapterView;
import com.media.notabadplayer.Utilities.InternalAdapterViews;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class CreatePlaylistTracksAdapter extends BaseAdapter
{
    private final @NonNull Context _context;
    private final @NonNull ArrayList<BaseAudioTrack> _tracks;
    private final @NonNull ArtImageFetcher _artImageFetcher;

    private final InternalAdapterViews _listViews = new InternalAdapterViews();

    private final Drawable _coverArtNone;

    public CreatePlaylistTracksAdapter(@NonNull Context context, @NonNull ArrayList<BaseAudioTrack> tracks)
    {
        this._context = context;
        this._tracks = new ArrayList<>(tracks);
        this._artImageFetcher = new ArtImageFetcher(context);

        this._coverArtNone = context.getResources().getDrawable(R.drawable.cover_art_none);
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

        // Views update
        InternalAdapterView itemView = _listViews.add(listItem);
        itemView.reset();

        // Item update
        BaseAudioTrack track = _tracks.get(position);

        final ImageView cover = listItem.findViewById(R.id.albumCover);

        Function<Bitmap, Void> callback = new Function<Bitmap, Void>() {
            @NullableDecl
            @Override
            public Void apply(@NullableDecl Bitmap imageBitmap) {
                if (imageBitmap != null) {
                    cover.setImageBitmap(imageBitmap);
                } else {
                    cover.setImageDrawable(_coverArtNone);
                }
                return null;
            }
        };

        itemView.token = _artImageFetcher.fetchAsync(track.getArtCover(), callback);

        TextView title = listItem.findViewById(R.id.title);
        title.setText(track.getTitle());

        TextView description = listItem.findViewById(R.id.description);
        description.setText(track.getDuration());

        return listItem;
    }
}
