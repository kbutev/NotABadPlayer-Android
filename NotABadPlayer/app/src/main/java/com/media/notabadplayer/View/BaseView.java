package com.media.notabadplayer.View;

import com.media.notabadplayer.Audio.MediaTrack;

import java.util.ArrayList;

public interface BaseView {
    void setPresenter(BasePresenter presenter);
    
    void openAlbumScreen(com.media.notabadplayer.Audio.MediaInfo mediaInfo, String albumID, String albumTitle, String albumCover);
    
    void onMediaAlbumsLoad(ArrayList<com.media.notabadplayer.Audio.AlbumInfo> albums);
    void onAlbumSongsLoad(ArrayList<MediaTrack> songs);
    
    void openPlayerScreen(MediaTrack track);
}
