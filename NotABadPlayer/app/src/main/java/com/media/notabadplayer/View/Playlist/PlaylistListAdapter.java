package com.media.notabadplayer.View.Playlist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.media.notabadplayer.Audio.Model.BaseAudioPlaylist;
import com.media.notabadplayer.Audio.Model.OpenPlaylistOptions;
import com.media.notabadplayer.R;
import com.media.notabadplayer.View.Other.TrackListAdapter;
import com.media.notabadplayer.View.Other.TrackListFavoritesChecker;
import com.media.notabadplayer.View.Other.TrackListHighlightedChecker;

class PlaylistListAdapter extends TrackListAdapter
{
    public PlaylistListAdapter(@NonNull Context context, 
                               @NonNull BaseAudioPlaylist playlist, 
                               @NonNull OpenPlaylistOptions options,
                               @Nullable TrackListHighlightedChecker highlightedChecker, 
                               @Nullable TrackListFavoritesChecker favoritesChecker)
    {
        super(context, playlist, options, highlightedChecker, favoritesChecker);
    }

    public @NonNull View getLayout(@NonNull ViewGroup parent) {
        return LayoutInflater.from(_context).inflate(R.layout.item_album_track, parent, false);
    }

    public @NonNull TextView getTitleView(@NonNull View listItem) {
        return (TextView)listItem.findViewById(R.id.titleText);
    }

    public @NonNull TextView getTrackNumView(@NonNull View listItem) {
        return (TextView)listItem.findViewById(R.id.trackNumText);
    }

    public @NonNull TextView getDescriptionView(@NonNull View listItem) {
        return (TextView)listItem.findViewById(R.id.descriptionText);
    }

    public @NonNull ImageView getFavoriteIcon(@NonNull View listItem) {
        return (ImageView)listItem.findViewById(R.id.favoriteIcon);
    }
}
