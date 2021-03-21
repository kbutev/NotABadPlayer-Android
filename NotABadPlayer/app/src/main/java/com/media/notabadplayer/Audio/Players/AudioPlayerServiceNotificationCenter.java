package com.media.notabadplayer.Audio.Players;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.media.notabadplayer.MainActivity;

import com.media.notabadplayer.Audio.Model.BaseAudioTrack;
import com.media.notabadplayer.R;

public class AudioPlayerServiceNotificationCenter {
    final static int IDENTIFIER = 1000;
    final static String CHANNEL_ID = "NotABadPlayer";

    final static String BROADCAST_ACTION_PLAY = "AudioPlayerService.play";
    final static String BROADCAST_ACTION_PAUSE = "AudioPlayerService.pause";
    final static String BROADCAST_ACTION_PREVIOUS = "AudioPlayerService.previous";
    final static String BROADCAST_ACTION_NEXT = "AudioPlayerService.next";

    final private @NonNull Service _service;

    private String _actionResumeString;
    private String _actionPauseString;
    private String _actionPreviousString;
    private String _actionNextString;

    private String _notificationPrefix;
    private String _notificationSuffix;

    AudioPlayerServiceNotificationCenter(@NonNull Service service)
    {
        _service = service;

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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.media_play)
                .setContentTitle(content)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(contentIntent);

        // Previous and next action buttons
        actionIntentData = new Intent();
        actionIntentData.setAction(BROADCAST_ACTION_PREVIOUS);
        PendingIntent previousAction = PendingIntent.getBroadcast(getApplicationContext(), 1, actionIntentData, PendingIntent.FLAG_UPDATE_CURRENT);

        actionIntentData = new Intent();
        actionIntentData.setAction(BROADCAST_ACTION_NEXT);
        PendingIntent nextAction = PendingIntent.getBroadcast(getApplicationContext(), 1, actionIntentData, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build pause/play action button
        actionIntentData = new Intent();
        String playPauseString;

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
        _service.startForeground(IDENTIFIER, n);
    }

    void clear()
    {
        _service.stopForeground(true);
    }
}