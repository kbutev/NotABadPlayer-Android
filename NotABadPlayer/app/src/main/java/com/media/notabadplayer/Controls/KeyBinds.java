package com.media.notabadplayer.Controls;

import android.util.Log;

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
    
    public ApplicationAction getActionForInput(ApplicationInput input)
    {
        return GeneralStorage.getShared().getSettingsAction(input);
    }
    
    public ApplicationAction evaluateInput(ApplicationInput input)
    {
        ApplicationAction action = getActionForInput(input);
        
        return performAction(action);
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
                AudioPlayer.getShared().playNext();
                break;
            case PREVIOUS:
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
            case FORWARDS_8:
                AudioPlayer.getShared().jumpForwards(8);
                break;
            case FORWARDS_10:
                AudioPlayer.getShared().jumpForwards(10);
                break;
            case FORWARDS_15:
                AudioPlayer.getShared().jumpForwards(15);
                break;
            case FORWARDS_60:
                AudioPlayer.getShared().jumpForwards(60);
                break;
            case BACKWARDS_8:
                AudioPlayer.getShared().jumpBackwards(8);
                break;
            case BACKWARDS_10:
                AudioPlayer.getShared().jumpBackwards(10);
                break;
            case BACKWARDS_15:
                AudioPlayer.getShared().jumpBackwards(15);
                break;
            case BACKWARDS_60:
                AudioPlayer.getShared().jumpBackwards(60);
                break;
            case CHANGE_PLAY_ORDER:
                AudioPlayer player = AudioPlayer.getShared();

                AudioPlayOrder order = player.getPlayOrder();
                
                switch (order)
                {
                    case FORWARDS:
                        player.setPlayOrder(AudioPlayOrder.FORWARDS_REPEAT);
                        break;
                    case FORWARDS_REPEAT:
                        player.setPlayOrder(AudioPlayOrder.ONCE_FOREVER);
                        break;
                    case ONCE_FOREVER:
                        player.setPlayOrder(AudioPlayOrder.SHUFFLE);
                        break;
                    case SHUFFLE:
                        player.setPlayOrder(AudioPlayOrder.FORWARDS);
                        break;
                    default:
                        player.setPlayOrder(AudioPlayOrder.FORWARDS);
                        break;
                }
                
                break;
            case RECALL:
                AudioPlayer.getShared().playHistory.playPrevious();
                break;
        }
        
        return action;
    }
}
