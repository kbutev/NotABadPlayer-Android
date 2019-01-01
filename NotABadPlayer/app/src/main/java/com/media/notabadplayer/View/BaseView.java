package com.media.notabadplayer.View;

import java.util.ArrayList;

public interface BaseView {
    void setPresenter(BasePresenter presenter);
    
    void openAlbumScreen(com.media.notabadplayer.Audio.MediaInfo mediaInfo, String albumID, String albumTitle, String albumCover);
    
    void onMediaAlbumsLoad(ArrayList<com.media.notabadplayer.Audio.AlbumInfo> albums);
    void onAlbumSongsLoad(ArrayList<com.media.notabadplayer.Audio.AudioTrack> songs);
    
    void openPlayer();
    void startPlayer(com.media.notabadplayer.Audio.AudioTrack track);
    
    void onPlayerPlay(com.media.notabadplayer.Audio.AudioTrack current);
    void onPlayerStop();
    void onPlayerPause();
    void onPlayerResume();
    void onPlayerVolumeChanged();
}
