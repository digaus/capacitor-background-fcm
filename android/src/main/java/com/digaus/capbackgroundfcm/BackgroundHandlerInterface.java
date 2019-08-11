package com.digaus.capbackgroundfcm;


public interface BackgroundHandlerInterface {

    BackgroundFCMData handleNotification(BackgroundFCMRemoteMessage remoteMessage);
}