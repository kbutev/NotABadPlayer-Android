package com.media.notabadplayer.Controlls;

import com.media.notabadplayer.Audio.MediaPlayer;
import com.media.notabadplayer.Audio.MediaPlayerPlaylist;
import com.media.notabadplayer.Audio.MediaPlayerPlaylistPlayOrder;

public class KeyBinds
{
    private static KeyBinds _singleton;
    
    public static synchronized KeyBinds getShared()
    {
        if (_singleton == null)
        {
            _singleton = new KeyBinds();
        }
        
        return _singleton;
    }
    
    public synchronized ApplicationAction respondToInput(ApplicationInput input)
    {
        switch (input)
        {
            case HOME_BUTTON:
                return performAction(ApplicationAction.DO_NOTHING);
            case PLAYER_VOLUME_UP_BUTTON:
                return performAction(ApplicationAction.VOLUME_UP);
            case PLAYER_VOLUME_DOWN_BUTTON:
                return performAction(ApplicationAction.VOLUME_DOWN);
            case QUICK_PLAYER_VOLUME_UP_BUTTON:
                return performAction(ApplicationAction.JUMP_FORWARDS_15);
            case QUICK_PLAYER_VOLUME_DOWN_BUTTON:
                return performAction(ApplicationAction.JUMP_BACKWARDS_15);
            case SCREEN_LOCK_BUTTON:
                return performAction(ApplicationAction.DO_NOTHING);
            case PLAYER_PLAY_BUTTON:
                return performAction(ApplicationAction.PAUSE_OR_RESUME);
            case PLAYER_NEXT_BUTTON:
                return performAction(ApplicationAction.NEXT);
            case PLAYER_PREVIOUS_BUTTON:
                return performAction(ApplicationAction.PREVIOUS);
            case QUICK_PLAYER_PLAY_BUTTON:
                return performAction(ApplicationAction.PAUSE_OR_RESUME);
            case QUICK_PLAYER_NEXT_BUTTON:
                return performAction(ApplicationAction.JUMP_FORWARDS_15);
            case QUICK_PLAYER_PREVIOUS_BUTTON:
                return performAction(ApplicationAction.JUMP_BACKWARDS_15);
        }
        
        return ApplicationAction.DO_NOTHING;
    }
    
    public ApplicationAction performAction(ApplicationAction action)
    {
        switch (action)
        {
            case DO_NOTHING:
                break;
            case EXIT:
                System.exit(0);
                break;
            case PLAY:
                MediaPlayer.getShared().resume();
                break;
            case PAUSE:
                MediaPlayer.getShared().pause();
                break;
            case PAUSE_OR_RESUME:
                MediaPlayer.getShared().pauseOrResume();
                break;
            case NEXT:
                MediaPlayer.getShared().playNext();
                break;
            case PREVIOUS:
                MediaPlayer.getShared().playPrevious();
                break;
            case SHUFFLE:
                MediaPlayer.getShared().shuffle();
                break;
            case VOLUME_UP:
                MediaPlayer.getShared().volumeUp();
                break;
            case VOLUME_DOWN:
                MediaPlayer.getShared().volumeDown();
                break;
            case MUTE_OR_UNMUTE:
                MediaPlayer.getShared().muteOrUnmute();
                break;
            case JUMP_FORWARDS_5:
                MediaPlayer.getShared().jumpForwards(5);
                break;
            case JUMP_FORWARDS_8:
                MediaPlayer.getShared().jumpForwards(8);
                break;
            case JUMP_FORWARDS_10:
                MediaPlayer.getShared().jumpForwards(10);
                break;
            case JUMP_FORWARDS_15:
                MediaPlayer.getShared().jumpForwards(15);
                break;
            case JUMP_BACKWARDS_5:
                MediaPlayer.getShared().jumpBackwards(5);
                break;
            case JUMP_BACKWARDS_8:
                MediaPlayer.getShared().jumpBackwards(8);
                break;
            case JUMP_BACKWARDS_10:
                MediaPlayer.getShared().jumpBackwards(10);
                break;
            case JUMP_BACKWARDS_15:
                MediaPlayer.getShared().jumpBackwards(15);
                break;
            case CHANGE_PLAY_ORDER:
                MediaPlayerPlaylist playlist = MediaPlayer.getShared().getPlaylist();
                
                if (playlist != null)
                {
                    MediaPlayerPlaylistPlayOrder order = playlist.getPlayOrder();
                    
                    switch (order)
                    {
                        case FORWARDS:
                            playlist.setPlayOrder(MediaPlayerPlaylistPlayOrder.ONCE_FOREVER);
                            break;
                        case ONCE_FOREVER:
                            playlist.setPlayOrder(MediaPlayerPlaylistPlayOrder.SHUFFLE);
                            break;
                        case SHUFFLE:
                            playlist.setPlayOrder(MediaPlayerPlaylistPlayOrder.FORWARDS);
                            break;
                        default:
                            playlist.setPlayOrder(MediaPlayerPlaylistPlayOrder.FORWARDS);
                            break;
                    }
                }
                
                break;
        }
        
        return action;
    }
}
