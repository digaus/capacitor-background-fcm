# capacitor-background-fcm [![npm version](https://badge.fury.io/js/capacitor-background-fcm.svg)](https://badge.fury.io/js/capacitor-background-fcm)

Capacitor plugin to enable features from Firebase Cloud Messaging

> ### Notice
>
> This plugin is intended to be used together with the capacitor api for [Push Notifications](https://capacitor.ionicframework.com/docs/apis/push-notifications).

## API

- setAdditionalData

## Usage

```js

import { Plugins } from "@capacitor/core";
const { PushNotifications, BackgroundFCM } = Plugins;

//
// Add additional data which will be available in your BackgroundFCMHandler
const value = {
    translations: this.translateService.translations[this.translateService.currentLang]
};
BackgroundFCM.setAdditionalData({value: JSON.stringify(value)});
```
## iOS setup

> Currently not implemented

## Android setup

- `npm install --save capacitor-background-fcm`
- `npx cap sync android`
- `npx cap open android`
- add `google-services.json` to your `android/app` folder
- `[extra step]` in android case we need to tell Capacitor to initialise the plugin:

> on your `MainActivity.java` file add `import com.digaus.capbackgroundfcm.BackgroundFCM;` and then inside the init callback `add(BackgroundFCM.class);`

Now you can implement the `BackgroundFCMHandlerInterface` where you can handle all notifications which only have a data payload. The file must be named `BackgroundFCMHandler.java` and located next to the MainActivity.java

```java
package com.home.shelly;

import android.content.Context;
import android.util.Log;

import com.digaus.capbackgroundfcm.BackgroundFCMData;
import com.digaus.capbackgroundfcm.BackgroundFCMRemoteMessage;
import com.digaus.capbackgroundfcm.BackgroundHandlerInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BackgroundFCMHandler implements BackgroundHandlerInterface {

    private static String TAG = "BackgroundFCMHandler";
    private Context context;
    private JSONObject additionalData;

    public void setContext(Context context){
        this.context = context;
    }
    public void setAdditionalData(JSONObject additionalData){
        this.additionalData = additionalData;
    }

    public BackgroundFCMData handleNotification(BackgroundFCMRemoteMessage remoteMessage) {
       if (remoteMessage.getData() != null && remoteMessage.getData().getString("type") != null) {
           if (remoteMessage.getData().getString("type").equals("device-update2")) {
               return this.handleDeviceUpdate(remoteMessage, this.additionalData);
           } else if (remoteMessage.getData().getString("type").equals("app-update2")) {
               return this.handleAppUpdate(remoteMessage, this.additionalData);
           }
       }
       return null;
    }

    // show a translated message based on the translation which we passed with setAdditionalData
    private BackgroundFCMData handleAppUpdate(BackgroundFCMRemoteMessage remoteMessage, JSONObject obj) {
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

    // show a translated message based on the translation which we passed with setAdditionalData and some information about devices
    private BackgroundFCMData handleDeviceUpdate(BackgroundFCMRemoteMessage remoteMessage, JSONObject obj) {
        try {
            JSONObject translations = obj.getJSONObject("translations");
            List<String> filteredDevices = this.checkFirmware(obj);
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

    private List<String> checkFirmware(JSONObject obj) {
        // Do some logic to check firmware of devices
        return filteredDevices;
    }

    // example http request to fetch some information for the notification
    private JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {
        HttpURLConnection urlConnection;
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
```

> Tip: every time you change a native code you may need to clean up the cache (Build > Clean Project | Build > Rebuild Project) and then run the app again.


## License

MIT
