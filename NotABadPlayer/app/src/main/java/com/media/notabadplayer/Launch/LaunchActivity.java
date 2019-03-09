package com.media.notabadplayer.Launch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.View.Main.MainActivity;

public class LaunchActivity extends AppCompatActivity {
    public static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;
    
    private boolean _firstTimeLaunch;
    
    private boolean _launchedFromFile;
    private Uri _launchedFromFileUri;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        
        _firstTimeLaunch = GeneralStorage.getShared().isFirstApplicationLaunch(this);
        
        _launchedFromFile = Intent.ACTION_VIEW.equals(getIntent().getAction());
        
        if (_launchedFromFile)
        {
            _launchedFromFileUri = getIntent().getData();
        }
        
        if (_firstTimeLaunch)
        {
            GeneralStorage.getShared().resetDefaultSettingsActions(this);
        }
        
        requestPermissionForReadExtertalStorage();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        
        if (!_launchedFromFile)
        {
            GeneralStorage.getShared().restorePlayerState(getApplication(), this);
            GeneralStorage.getShared().restorePlayerPlayHistoryState(getApplication(), this);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
    
    private void openMainScreen()
    {
        Log.v("LaunchActivity", "Launching player default way...");
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
        
        overridePendingTransition(0, 0);
    }
    
    private void startAppWithTrack(Uri path)
    {
        Log.v("LaunchActivity", "Launching player with initial track...");
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("launchTrackPath", path);
        startActivity(intent);
        finish();
        
        overridePendingTransition(0, 0);
    }
    
    public void requestPermissionForReadExtertalStorage()
    {
        try {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        } catch (Exception e) {
            Log.v("LaunchActivity", "Cannot request permission for read external storage: " + e.toString());
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE
                && grantResults.length == 1 
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (!_launchedFromFile)
            {
                openMainScreen();
            }
            else
            {
                startAppWithTrack(_launchedFromFileUri);
            }
        }
        else
        {
            requestPermissionForReadExtertalStorage();
        }
    }
}
