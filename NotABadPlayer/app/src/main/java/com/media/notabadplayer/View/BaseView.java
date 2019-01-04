package com.media.notabadplayer.View;

import java.util.ArrayList;

public interface BaseView {
    void setPresenter(BasePresenter presenter);
    
    void openAlbumScreen(com.media.notabadplayer.Audio.MediaInfo mediaInfo, String albumID, String albumTitle, String albumCover);
    
    void onMediaAlbumsLoad(ArrayList<com.media.notabadplayer.Audio.AlbumInfo> albums);
    void onAlbumSongsLoad(ArrayList<com.media.notabadplayer.Audio.AudioTrack> songs);
    
    void openPlayerScreen();
    void openPlayerScreen(com.media.notabadplayer.Audio.AudioTrack track);
}
