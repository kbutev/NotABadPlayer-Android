package com.media.notabadplayer.Launch;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.media.notabadplayer.R;
import com.media.notabadplayer.Storage.GeneralStorage;
import com.media.notabadplayer.Utilities.AlertWindows;
import com.media.notabadplayer.View.Main.MainActivity;

public class LaunchActivity extends AppCompatActivity {
    public static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;
    
    private boolean _launchedFromFile;
    private Uri _launchedFromFileUri;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        GeneralStorage.getShared().init(getApplication());

        boolean firstTimeLaunch = GeneralStorage.getShared().isFirstApplicationLaunch();
        
        _launchedFromFile = Intent.ACTION_VIEW.equals(getIntent().getAction());
        
        if (_launchedFromFile)
        {
            _launchedFromFileUri = getIntent().getData();
        }
        
        requestPermissionForReadExtertalStorage();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
    
    private void startPlayerApp()
    {
        if (!_launchedFromFile)
        {
            openMainScreen();
        }
        else
        {
            startAppWithTrack(_launchedFromFileUri);
        }
    }
    
    private void openMainScreen()
    {
        Log.v(LaunchActivity.class.getCanonicalName(), "Launching player default way...");
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
        
        overridePendingTransition(0, 0);
    }
    
    private void startAppWithTrack(Uri path)
    {
        Log.v(LaunchActivity.class.getCanonicalName(), "Launching player with initial track...");
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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
            }
            else
            {
                startPlayerApp();
            }
        } catch (Exception e) {
            Log.v(LaunchActivity.class.getCanonicalName(), "Error: cannot request permission for read external storage: " + e.toString());
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
            startPlayerApp();
        }
        else
        {
            DialogInterface.OnClickListener action = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            };
            
            AlertWindows.showAlert(this, 0, R.string.error_need_storage_permission, R.string.ok, action);
        }
    }
}
