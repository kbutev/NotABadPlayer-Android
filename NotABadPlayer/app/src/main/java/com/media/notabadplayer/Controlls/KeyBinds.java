package com.media.notabadplayer.Controlls;

import com.media.notabadplayer.Audio.AudioPlayer;
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
            case VOLUME_UP_BUTTON:
                return performAction(ApplicationAction.JUMP_FORWARDS_15);
            case VOLUME_DOWN_BUTTON:
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
                AudioPlayer.getShared().resume();
                break;
            case PAUSE:
                AudioPlayer.getShared().pause();
                break;
            case PAUSE_OR_RESUME:
                AudioPlayer.getShared().pauseOrResume();
                break;
            case NEXT:
                AudioPlayer.getShared().next();
                break;
            case PREVIOUS:
                AudioPlayer.getShared().previous();
                break;
            case SHUFFLE:
                AudioPlayer.getShared().shuffle();
                break;
            case VOLUME_UP:
                AudioPlayer.getShared().volumeUp();
                break;
            case VOLUME_DOWN:
                AudioPlayer.getShared().volumeDown();
                break;
            case MUTE_OR_UNMUTE:
                AudioPlayer.getShared().muteOrUnmute();
                break;
            case JUMP_FORWARDS_5:
                AudioPlayer.getShared().jumpForwards(5);
                break;
            case JUMP_FORWARDS_8:
                AudioPlayer.getShared().jumpForwards(8);
                break;
            case JUMP_FORWARDS_10:
                AudioPlayer.getShared().jumpForwards(10);
                break;
            case JUMP_FORWARDS_15:
                AudioPlayer.getShared().jumpForwards(15);
                break;
            case JUMP_BACKWARDS_5:
                AudioPlayer.getShared().jumpBackwards(5);
                break;
            case JUMP_BACKWARDS_8:
                AudioPlayer.getShared().jumpBackwards(8);
                break;
            case JUMP_BACKWARDS_10:
                AudioPlayer.getShared().jumpBackwards(10);
                break;
            case JUMP_BACKWARDS_15:
                AudioPlayer.getShared().jumpBackwards(15);
            case CHANGE_PLAY_ORDER:
                MediaPlayerPlaylist playlist = AudioPlayer.getShared().getPlaylist();
                
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
