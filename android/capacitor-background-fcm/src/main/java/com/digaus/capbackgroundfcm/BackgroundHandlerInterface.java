package com.digaus.capbackgroundfcm;


import android.content.Context;

public interface BackgroundHandlerInterface {

    void setContext(Context context);
    BackgroundFCMData handleNotification(BackgroundFCMRemoteMessage remoteMessage);
}