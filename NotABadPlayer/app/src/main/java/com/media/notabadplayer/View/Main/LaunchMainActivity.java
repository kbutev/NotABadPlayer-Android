package com.media.notabadplayer.View.Main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

public class LaunchMainActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        
        overridePendingTransition(0, 0);
        
        finish();
    }
}
