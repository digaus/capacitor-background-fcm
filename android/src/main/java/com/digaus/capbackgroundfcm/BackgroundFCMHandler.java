package com.digaus.capbackgroundfcm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class BackgroundFCMHandler extends Activity {

    private static String TAG = "BackgroundFCMHandler";
    /*
     * this activity will be started if the user touches a notification that we own.
     * We send it's data off to the push plugin for processing.
     * If needed, we boot up the main activity to kickstart the application.
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        String id = intent.getExtras().getString("id", "");
        String data = intent.getExtras().getString("data", "");
        BackgroundFCM.onNotificationTap(id, data);

        startActivity(this.getPackageManager()
                .getLaunchIntentForPackage(getApplicationContext().getPackageName())
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));

        finish();
    }

}