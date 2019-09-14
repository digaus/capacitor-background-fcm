package com.digaus.capbackgroundfcm;


import android.content.Context;

import org.json.JSONObject;

public interface BackgroundFCMHandlerInterface {

    void setContext(Context context);
    void setAdditionalData(JSONObject additionalData);
    BackgroundFCMData handleNotification(BackgroundFCMRemoteMessage remoteMessage);
}