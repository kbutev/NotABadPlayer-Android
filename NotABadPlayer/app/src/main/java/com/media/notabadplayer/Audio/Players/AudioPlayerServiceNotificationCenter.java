package com.media.notabadplayer.Audio.Players;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.media.notabadplayer.MainActivity;

import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.R;

import static android.content.Context.NOTIFICATION_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

public class AudioPlayerServiceNotificationCenter {
    final static String BROADCAST_ACTION_PLAY = "AudioPlayerService.play";
    final static String BROADCAST_ACTION_PAUSE = "AudioPlayerService.pause";
    final static String BROADCAST_ACTION_PREVIOUS = "AudioPlayerService.previous";
    final static String BROADCAST_ACTION_NEXT = "AudioPlayerService.next";

    private NotificationManager _notificationManager;
    private NotificationChannel _notificationChannel;

    final private @NonNull Service _service;

    private String _channelID = "NotABadPlayer";
    private String _channelName = "playing";
    private String _channelDescription = "playing audio in the background";
    private int _notificationID = 1;

    private String _actionResumeString;
    private String _actionPauseString;
    private String _actionPreviousString;
    private String _actionNextString;

    private String _notificationPrefix;
    private String _notificationSuffix;

    AudioPlayerServiceNotificationCenter(@NonNull Service service)
    {
        _service = service;

        _notificationManager = (NotificationManager)service.getSystemService(NOTIFICATION_SERVICE);

        // After Android 8, it is required to register with the system before pushing notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            _notificationChannel = new NotificationChannel(_channelID, _channelName, NotificationManager.IMPORTANCE_LOW);
            _notificationChannel.setDescription(_channelDescription);
            _notificationChannel.setShowBadge(false);
            _notificationManager.createNotificationChannel(_notificationChannel);
        }

        Resources resources = _service.getResources();
        _actionResumeString = resources.getString(R.string.notification_resume);
        _actionPauseString = resources.getString(R.string.notification_pause);
        _actionPreviousString = resources.getString(R.string.notification_previous);
        _actionNextString = resources.getString(R.string.notification_next);
        _notificationPrefix = resources.getString(R.string.notification_title_prefix);
        _notificationSuffix = resources.getString(R.string.notification_title_suffix);
    }

    Context getApplicationContext() {
        return _service.getApplicationContext();
    }

    void showNotificationForPlayingTrack(@NonNull BaseAudioTrack track, boolean isPlaying)
    {
        String playingTrackName = track.getTitle();
        String content = _notificationPrefix + playingTrackName + _notificationSuffix + track.getAlbumTitle();

        showNotification(content, isPlaying);
    }

    private void showNotification(@NonNull String content, boolean isPlaying)
    {
        Log.v(AudioPlayerService.class.getCanonicalName(), "Showing notification '" + content + "' " + (isPlaying ? "playing" : "paused"));

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        Intent actionIntentData;

        // Build basics
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), _channelID)
                .setSmallIcon(R.drawable.media_play)
                .setContentTitle(content)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(contentIntent);

        // Previous and next actions
        actionIntentData = new Intent();
        actionIntentData.setAction(BROADCAST_ACTION_PREVIOUS);
        PendingIntent previousAction = PendingIntent.getBroadcast(getApplicationContext(), 1, actionIntentData, PendingIntent.FLAG_UPDATE_CURRENT);

        actionIntentData = new Intent();
        actionIntentData.setAction(BROADCAST_ACTION_NEXT);
        PendingIntent nextAction = PendingIntent.getBroadcast(getApplicationContext(), 1, actionIntentData, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build pause/play action
        actionIntentData = new Intent();
        String playPauseString = null;

        if (isPlaying)
        {
            actionIntentData.setAction(BROADCAST_ACTION_PAUSE);
            playPauseString = _actionPauseString;
        }
        else
        {
            actionIntentData.setAction(BROADCAST_ACTION_PLAY);
            playPauseString = _actionResumeString;
        }

        PendingIntent playPauseAction = PendingIntent.getBroadcast(getApplicationContext(), 1, actionIntentData, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(0, _actionPreviousString, previousAction)
                .addAction(0, playPauseString, playPauseAction)
                .addAction(0, _actionNextString, nextAction);

        // Result
        Notification n = builder.build();

        // Notify
        _notificationManager.notify(_notificationID, n);
    }

    void clear()
    {
        _notificationManager.cancel(_notificationID);
    }
}