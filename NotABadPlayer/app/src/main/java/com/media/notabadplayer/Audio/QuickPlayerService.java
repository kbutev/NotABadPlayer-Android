package com.media.notabadplayer.Audio;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import android.util.Log;

import com.google.common.base.Function;
import com.media.notabadplayer.Audio.Model.AudioPlayOrder;
import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.Audio.Players.Player;
import com.media.notabadplayer.Utilities.LooperClient;
import com.media.notabadplayer.Utilities.LooperService;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.ArrayList;

/*
 * Listens to player events from the AudioPlayer.
 * Note: All observer delegation is performed on the main thread.
 */
public class QuickPlayerService implements AudioPlayerObserver, LooperClient {
    private static QuickPlayerService _singleton;

    private final Object lock = new Object();

    private @NonNull AudioPlayer _player = Player.getShared();
    
    private boolean _started = false;
    
    private ArrayList<QuickPlayerObserver> _observers = new ArrayList<>();
    
    private QuickPlayerService()
    {
        Log.v(QuickPlayerService.class.getCanonicalName(), "Initialized!");
    }

    public synchronized static QuickPlayerService getShared()
    {
        if (_singleton == null)
        {
            _singleton = new QuickPlayerService();
        }

        return _singleton;
    }
    
    private void startIfNotStarted()
    {
        synchronized (lock) {
            if (_started) {
                return;
            }

            _started = true;
        }
        
        _player.observers().attach(this);

        LooperService.getShared().subscribe(this);
    }

    private ArrayList<QuickPlayerObserver> observersCopy() 
    {
        synchronized (lock) {
            return new ArrayList<>(_observers);
        }
    }

    public void attach(@NonNull QuickPlayerObserver observer) 
    {
        startIfNotStarted();
        
        synchronized (lock) {
            if (_observers.contains(observer)) {
                return;
            }

            _observers.add(observer);
        }
    }

    public void detach(@NonNull QuickPlayerObserver observer)
    {
        synchronized (lock) {
            _observers.remove(observer);
        }
    }

    @Override
    public void onPlayerPlay(@NonNull final BaseAudioTrack current)
    {
        final ArrayList<QuickPlayerObserver> observers = observersCopy();

        performOnMain(new Function<Void, Void>() {
            @NullableDecl
            @Override
            public Void apply(@NullableDecl Void input) {

                for (QuickPlayerObserver observer : observers) {
                    observer.onPlayerPlay(current);
                }
                
                return null;
            }
        });
    }

    @Override
    public void onPlayerFinish()
    {
        final ArrayList<QuickPlayerObserver> observers = observersCopy();

        performOnMain(new Function<Void, Void>() {
            @NullableDecl
            @Override
            public Void apply(@NullableDecl Void input) {

                for (QuickPlayerObserver observer : observers) {
                    observer.onPlayerFinish();
                }

                return null;
            }
        });
    }

    @Override
    public void onPlayerStop()
    {
        final ArrayList<QuickPlayerObserver> observers = observersCopy();

        performOnMain(new Function<Void, Void>() {
            @NullableDecl
            @Override
            public Void apply(@NullableDecl Void input) {

                for (QuickPlayerObserver observer : observers) {
                    observer.onPlayerStop();
                }

                return null;
            }
        });
    }

    @Override
    public void onPlayerPause(@NonNull final BaseAudioTrack track)
    {
        final ArrayList<QuickPlayerObserver> observers = observersCopy();

        performOnMain(new Function<Void, Void>() {
            @NullableDecl
            @Override
            public Void apply(@NullableDecl Void input) {

                for (QuickPlayerObserver observer : observers) {
                    observer.onPlayerPause(track);
                }

                return null;
            }
        });
    }

    @Override
    public void onPlayerResume(@NonNull final BaseAudioTrack track)
    {
        final ArrayList<QuickPlayerObserver> observers = observersCopy();

        performOnMain(new Function<Void, Void>() {
            @NullableDecl
            @Override
            public Void apply(@NullableDecl Void input) {

                for (QuickPlayerObserver observer : observers) {
                    observer.onPlayerResume(track);
                }

                return null;
            }
        });
    }

    @Override
    public void onPlayOrderChange(final AudioPlayOrder order)
    {
        final ArrayList<QuickPlayerObserver> observers = observersCopy();

        performOnMain(new Function<Void, Void>() {
            @NullableDecl
            @Override
            public Void apply(@NullableDecl Void input) {

                for (QuickPlayerObserver observer : observers) {
                    observer.onPlayOrderChange(order);
                }

                return null;
            }
        });
    }

    @Override
    public void loop()
    {
        final double currentTime = _player.getCurrentPositionMSec() / 1000.0;
        final double totalTime = _player.getDurationMSec() / 1000.0;
        
        final ArrayList<QuickPlayerObserver> observers = observersCopy();

        performOnMain(new Function<Void, Void>() {
            @NullableDecl
            @Override
            public Void apply(@NullableDecl Void input) {

                for (QuickPlayerObserver observer : observers) {
                    observer.updateTime(currentTime, totalTime);
                }

                return null;
            }
        });
    }
    
    private void performOnMain(@NonNull final Function<Void, Void> callback) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            callback.apply(null);
            return;
        }
        
        Handler mainHandler = new Handler(Looper.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                callback.apply(null);
            }
        };
        mainHandler.post(myRunnable);
    }
}
