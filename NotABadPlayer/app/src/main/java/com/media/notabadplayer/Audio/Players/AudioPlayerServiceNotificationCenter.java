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

public class AudioPlayerServiceNotificationCenter {
    final static int IDENTIFIER = 1000;
    final static String CHANNEL_ID = "NotABadPlayer";
    final static String CHANNEL_NAME = "NotABadPlayer";

    final static String BROADCAST_ACTION_PLAY = "AudioPlayerService.play";
    final static String BROADCAST_ACTION_PAUSE = "AudioPlayerService.pause";
    final static String BROADCAST_ACTION_PREVIOUS = "AudioPlayerService.previous";
    final static String BROADCAST_ACTION_NEXT = "AudioPlayerService.next";

    final private @NonNull Service _service;

    private final String _actionResumeString;
    private final String _actionPauseString;
    private final String _actionPreviousString;
    private final String _actionNextString;

    private final String _notificationPrefix;
    private final String _notificationSuffix;

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

        // Intent flags
        int contentIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        int previousActionFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        int nextActionFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        int playPauseActionFlags = PendingIntent.FLAG_UPDATE_CURRENT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            contentIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
            previousActionFlags |= PendingIntent.FLAG_IMMUTABLE;
            nextActionFlags |= PendingIntent.FLAG_IMMUTABLE;
            playPauseActionFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        // Base intent
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, contentIntentFlags);

        Intent actionIntentData;

        // Build basics
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setOngoing(true)
                .setSmallIcon(R.drawable.media_play)
                .setContentTitle(content)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent);

        // Previous and next action buttons
        actionIntentData = new Intent();
        actionIntentData.setAction(BROADCAST_ACTION_PREVIOUS);

        PendingIntent previousAction = PendingIntent.getBroadcast(getApplicationContext(), 1, actionIntentData, previousActionFlags);

        actionIntentData = new Intent();
        actionIntentData.setAction(BROADCAST_ACTION_NEXT);
        PendingIntent nextAction = PendingIntent.getBroadcast(getApplicationContext(), 1, actionIntentData, nextActionFlags);

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

        PendingIntent playPauseAction = PendingIntent.getBroadcast(getApplicationContext(), 1, actionIntentData, playPauseActionFlags);

        // Add all intent actions
        builder.addAction(0, _actionPreviousString, previousAction)
                .addAction(0, playPauseString, playPauseAction)
                .addAction(0, _actionNextString, nextAction);

        // Register channel
        registerChannelForNotification();

        // Notify
        _service.startForeground(IDENTIFIER, builder.build());
    }

    void clear()
    {
        _service.stopForeground(true);
    }

    private void registerChannelForNotification()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
            NotificationManager manager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(c);
        }
    }
}