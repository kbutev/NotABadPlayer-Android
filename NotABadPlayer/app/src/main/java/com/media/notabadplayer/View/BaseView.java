package com.media.notabadplayer.View;

import com.media.notabadplayer.Audio.MediaAlbum;
import com.media.notabadplayer.Audio.MediaInfo;

import java.util.ArrayList;

public interface BaseView {
    void setPresenter(BasePresenter presenter);
    
    void openAlbumScreen(MediaInfo mediaInfo, String albumID, String albumArtist, String albumTitle, String albumCover);
    
    void onMediaAlbumsLoad(ArrayList<MediaAlbum> albums);
    void onAlbumSongsLoad(ArrayList<com.media.notabadplayer.Audio.MediaTrack> songs);
    
    void openPlayerScreen(com.media.notabadplayer.Audio.MediaPlayerPlaylist playlist);
}
