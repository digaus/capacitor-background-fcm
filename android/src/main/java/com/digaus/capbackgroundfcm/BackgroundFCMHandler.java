package com.digaus.capbackgroundfcm;

import android.content.Context;
import android.nfc.Tag;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BackgroundFCMHandler {

    private static String TAG = "BackgroundFCMHandler";
    private Context context;

    public BackgroundFCMHandler(Context context){
        this.context = context;
    }
    public BackgroundFCMData handleNotification(RemoteMessage remoteMessage) {
       JSONObject obj = readFile();
       if (remoteMessage.getData() != null && remoteMessage.getData().get("type") != null) {
           if (remoteMessage.getData().get("type").equals("device-update2")) {
               return this.handleDeviceUpdate(remoteMessage, obj);
           } else if (remoteMessage.getData().get("type").equals("app-update2")) {
               return this.handleAppUpdate(remoteMessage, obj);
           }
       }
       return null;
    }
    private BackgroundFCMData handleAppUpdate(RemoteMessage remoteMessage, JSONObject obj) {
        try {
            JSONObject translations = obj.getJSONObject("translations");
            String title = translations.getString("app.shelly-home.app-update.update.label");
            String body = translations.getString("app.shelly-home.app-update.update-installed.label");
            return new BackgroundFCMData(title, body);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }
    private BackgroundFCMData handleDeviceUpdate(RemoteMessage remoteMessage, JSONObject obj) {
        try {
            JSONObject translations = obj.getJSONObject("translations");
            List<String> filteredDevices = this.checkFirmware(obj, remoteMessage);
            if (filteredDevices.size() > 0) {
                String title = translations.getString("app.shelly-home.device-update.update.label");
                String body = translations.getString("app.shelly-home.device-update.available.label").replace("{{count}}", filteredDevices.size() + "");
                return new BackgroundFCMData(title, body);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }
    private JSONObject readFile() {
        JSONObject jsonObject = new JSONObject();
        try {
            FileInputStream in = this.context.openFileInput("config.txt");
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
            try {
                JSONObject firmwareListRequest = this.getJSONObjectFromURL("https://api.shelly.cloud/files/firmware");
                JSONObject firmwareList = firmwareListRequest.getJSONObject("data");
                Log.d(TAG, firmwareList.toString());
                for (String device : devices) {
                    String id = device.substring(0, 6);
                    if (deviceStatusList.getJSONObject(id) != null) {
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
                            if (updateDate > statusDate && updateTime > statusTime) {
                                filteredDevices.add(device);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        return filteredDevices;
    }
    private JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(5000 /* milliseconds */ );
        urlConnection.setConnectTimeout(5000 /* milliseconds */ );
        urlConnection.setDoOutput(true);
        urlConnection.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();

        String jsonString = sb.toString();
        System.out.println("JSON: " + jsonString);

        return new JSONObject(jsonString);
    }
}