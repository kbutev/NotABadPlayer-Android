package com.media.notabadplayer.Controls;

import android.content.Context;

import com.media.notabadplayer.Audio.AudioPlayer;
import com.media.notabadplayer.Audio.AudioPlaylist;
import com.media.notabadplayer.Audio.AudioPlayOrder;
import com.media.notabadplayer.Storage.GeneralStorage;

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
    
    public synchronized ApplicationAction evaluateInput(Context context, ApplicationInput input)
    {
        return performAction(GeneralStorage.getShared().getSettingsAction(context, input));
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
            case NEXT_TRACK:
                AudioPlayer.getShared().playNext();
                break;
            case PREVIOUS_TRACK:
                AudioPlayer.getShared().playPrevious();
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
                break;
            case CHANGE_PLAY_ORDER:
                AudioPlaylist playlist = AudioPlayer.getShared().getPlaylist();
                
                if (playlist != null)
                {
                    AudioPlayOrder order = playlist.getPlayOrder();
                    
                    switch (order)
                    {
                        case FORWARDS:
                            playlist.setPlayOrder(AudioPlayOrder.ONCE_FOREVER);
                            break;
                        case ONCE_FOREVER:
                            playlist.setPlayOrder(AudioPlayOrder.SHUFFLE);
                            break;
                        case SHUFFLE:
                            playlist.setPlayOrder(AudioPlayOrder.FORWARDS);
                            break;
                        default:
                            playlist.setPlayOrder(AudioPlayOrder.FORWARDS);
                            break;
                    }
                }
                
                break;
            case PREVIOUS_PLAYED_TRACK:
                AudioPlayer.getShared().playPreviousInPlayHistory();
                break;
        }
        
        return action;
    }
}
