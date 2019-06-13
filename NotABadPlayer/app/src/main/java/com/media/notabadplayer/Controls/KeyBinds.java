package com.media.notabadplayer.Controls;

import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Audio.Model.AudioPlayOrder;
import com.media.notabadplayer.Storage.GeneralStorage;

import javax.annotation.Nullable;

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
    
    public @Nullable Exception evaluateInput(ApplicationInput input)
    {
        ApplicationAction action = getActionForInput(input);
        
        return performAction(action);
    }
    
    public @Nullable Exception performAction(ApplicationAction action)
    {
        Exception exception = null;
        
        switch (action)
        {
            case DO_NOTHING:
                break;
            case EXIT:
                System.exit(0);
                break;
            case PLAY:
                Player.getShared().resume();
                break;
            case PAUSE:
                Player.getShared().pause();
                break;
            case PAUSE_OR_RESUME:
                Player.getShared().pauseOrResume();
                break;
            case NEXT:
                try {
                    Player.getShared().playNext();
                } catch (Exception e)
                {
                    exception = e;
                }
                break;
            case PREVIOUS:
                try {
                    Player.getShared().playPrevious();
                } catch (Exception e)
                {
                    exception = e;
                }
                break;
            case SHUFFLE:
                try {
                    Player.getShared().shuffle();
                } catch (Exception e)
                {
                    exception = e;
                }
                break;
            case VOLUME_UP:
                Player.getShared().volumeUp();
                break;
            case VOLUME_DOWN:
                Player.getShared().volumeDown();
                break;
            case MUTE_OR_UNMUTE:
                Player.getShared().muteOrUnmute();
                break;
            case MUTE:
                Player.getShared().mute();
                break;
            case FORWARDS_8:
                Player.getShared().jumpForwards(8);
                break;
            case FORWARDS_15:
                Player.getShared().jumpForwards(15);
                break;
            case FORWARDS_30:
                Player.getShared().jumpForwards(30);
                break;
            case FORWARDS_60:
                Player.getShared().jumpForwards(60);
                break;
            case BACKWARDS_8:
                Player.getShared().jumpBackwards(8);
                break;
            case BACKWARDS_15:
                Player.getShared().jumpBackwards(15);
                break;
            case BACKWARDS_30:
                Player.getShared().jumpBackwards(30);
                break;
            case BACKWARDS_60:
                Player.getShared().jumpBackwards(60);
                break;
            case CHANGE_PLAY_ORDER:
                Player player = Player.getShared();

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
                try {
                    Player.getShared().playHistory.playPreviousInHistory();
                } catch (Exception e) {
                    exception = e;
                }
                break;
        }
        
        return exception;
    }
}
