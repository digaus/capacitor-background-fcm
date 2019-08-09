package com.digaus.capbackgroundfcm;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.getcapacitor.CapacitorFirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 *
 * In order to make this Java sample functional, you must remove the following from the Kotlin messaging
 * service in the AndroidManifest.xml:
 *
 * <intent-filter>
 *   <action android:name="com.google.firebase.MESSAGING_EVENT" />
 * </intent-filter>
 */
public class BackgroundFCMService extends CapacitorFirebaseMessagingService {

  private static final String TAG = "BackgroundFCMService";

  /**
   * Called when message is received.
   *
   * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
   */
  // [START receive_message]
  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    // Check if message contains a data payload
     Log.d(TAG, remoteMessage.getMessageId());
    if (remoteMessage.getNotification() == null && remoteMessage.getData().size() > 0) {
      Log.d(TAG, "Message data payload: " + remoteMessage.getData());
      BackgroundFCMHandler converter = new BackgroundFCMHandler(this);
      BackgroundFCMData data = converter.handleNotification(remoteMessage);
      if (data != null) {
          this.sendNotification(remoteMessage.getMessageId(), data.getTitle(), data.getBody(), remoteMessage.getData());
      }

    }
    super.onMessageReceived(remoteMessage);

  }

  /**
   * Create and show a simple notification containing the received FCM message.
   *
   * @param message FCM message body received.
   */
  private void sendNotification(String id, String title, String message, Map<String, String> data) {
      Intent intent = new Intent(this, BackgroundFCMTapHandler.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      intent.putExtra("id", id);
      intent.putExtra("data", data.toString());
      PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
              PendingIntent.FLAG_ONE_SHOT);

      String channelId = "1";
      Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
      int icon =  getApplicationContext().getResources().getIdentifier("icon_notification", "mipmap", getApplicationContext().getPackageName());
      if (icon == 0) {
          icon = getApplicationContext().getApplicationInfo().icon;
      }
      NotificationCompat.Builder notificationBuilder =
              new NotificationCompat.Builder(this, channelId)
                      .setSmallIcon(icon)
                      .setContentTitle(title)
                      .setContentText(message)
                      .setAutoCancel(true)
                      .setSound(defaultSoundUri)
                      .setContentIntent(pendingIntent);
      NotificationManager notificationManager =
              (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

      // Since android Oreo notification channel is needed.
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          NotificationChannel channel = new NotificationChannel(channelId,
                  "Channel human readable title",
                  NotificationManager.IMPORTANCE_DEFAULT);
          notificationManager.createNotificationChannel(channel);
      }
      int notId = 0;
      if (data.get("id") != null) {
          notId = Integer.parseInt(data.get("id"));
      }
      notificationManager.notify(notId, notificationBuilder.build());
  }
}