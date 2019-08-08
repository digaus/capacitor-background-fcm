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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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
    // Check if message contains a data payload.
    if (remoteMessage.getData().size() > 0) {
      Log.d(TAG, "Message data payload: " + remoteMessage.getData());
      JSONObject obj = this.readFile();
      try {
        JSONObject translations = obj.getJSONObject("translations");
        List<String> filteredDevices = this.checkFirmware(obj, remoteMessage);

        if (filteredDevices.size() > 0) {
            String title = translations.getString("app.shelly-home.device-update.update.label");
            String message = translations.getString("app.shelly-home.device-update.available.label").replace("{{count}}", filteredDevices.size() + "");
            this.sendNotification(remoteMessage.getMessageId(), title, message, remoteMessage.getData());
        }
      } catch (JSONException e) {
        Log.e(TAG, e.toString());
      }

      Log.d(TAG, obj.toString());

    }
    super.onMessageReceived(remoteMessage);

  }

  private List<String> checkFirmware(JSONObject obj, RemoteMessage remoteMessage) {
      List<String> filteredDevices = new ArrayList<>();
      try {
          JSONArray devicesJSONArray  = obj.getJSONArray("devices");
          ArrayList<String> devices = new ArrayList<>();
          if (devicesJSONArray != null) {
              for (int i=0; i<devicesJSONArray.length(); i++){
                  devices.add(devicesJSONArray.getString(i));
              }
          }
          JSONObject deviceList = obj.getJSONObject("deviceList");
          JSONObject deviceStatusList = obj.getJSONObject("deviceStatusList");
          JSONObject firmwareList = new JSONObject(remoteMessage.getData().get("updates"));


          for (String device : devices) {
              String id = device.substring(0, 6);
              if (deviceStatusList.getJSONObject(id) != null) {
                  // 20190531-075825
                  String statusFw = deviceStatusList.getJSONObject(id).getJSONObject("update").getString("old_version").split("/")[0];
                  String type = deviceList.getJSONObject(id).getString("type");
                  JSONObject firmware = firmwareList.getJSONObject(type);
                  if (firmware != null) {
                      String updateFw = firmware.getString("version").split("/")[0];
                      Integer updateDate = Integer.parseInt(updateFw.split("-")[0]);
                      Integer statusDate = Integer.parseInt(statusFw.split("-")[0]);
                      Integer updateTime = Integer.parseInt(updateFw.split("-")[1]);
                      Integer statusTime = Integer.parseInt(statusFw.split("-")[1]);
                      Log.d(TAG, updateDate.toString() + ">" + statusDate.toString() + "," + updateTime.toString() + ">" + statusTime.toString());
                      if (updateDate < statusDate && updateTime < statusTime) {
                          filteredDevices.add(device);
                      }
                  }
              }
          }
      } catch (JSONException e) {
          Log.e(TAG, e.toString());
      }
      return filteredDevices;
  }
  private JSONObject readFile() {
    JSONObject jsonObject = new JSONObject();
    try {
      FileInputStream in = openFileInput("config.txt");
      InputStreamReader inputStreamReader = new InputStreamReader(in);
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        sb.append(line);
      }
      inputStreamReader.close();
      try {
        jsonObject = new JSONObject(sb.toString());
      }catch (JSONException err){
        Log.e(TAG, err.toString());
      }
    } catch(Exception e){
      Log.e(TAG, e.toString());
    }
    return jsonObject;
  }

  /**
   * Create and show a simple notification containing the received FCM message.
   *
   * @param message FCM message body received.
   */
  private void sendNotification(String id, String title, String message, Map<String, String> data) {
      Intent intent = new Intent(this, BackgroundFCMHandler.class);
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

      notificationManager.notify(0, notificationBuilder.build());

  }
}