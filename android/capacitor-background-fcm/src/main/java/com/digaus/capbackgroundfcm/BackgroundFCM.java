package com.digaus.capbackgroundfcm;

import android.content.Context;
import android.util.Log;

import com.getcapacitor.Bridge;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginHandle;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.PluginRequestCodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@NativePlugin(requestCodes = PluginRequestCodes.NOTIFICATION_OPEN)
public class BackgroundFCM extends Plugin {
    private static String TAG = "BackgroundFCM";

    public static Bridge staticBridge = null;
    public static BackgroundFCMRemoteMessage remoteMessage;

    public void load() {
        staticBridge = this.bridge;
        if (this.remoteMessage != null) {
            this.handleNotificationTap(this.remoteMessage);
            this.remoteMessage = null;
        }
    }

    @PluginMethod()
    public void writeToFile(PluginCall call) {
        String data = call.getString("value");
        Context context = staticBridge.getContext();
        File path = context.getFilesDir();
        File file = new File(path, "config.txt");
        FileOutputStream stream;
        try {
            stream = new FileOutputStream(file);
            stream.write(data.getBytes());

            stream.close();
            JSObject ret = new JSObject();
            ret.put("value", data);
            call.success(ret);
        } catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
            call.error("File write failed: " + e.toString());
        }
    }
    public void handleNotificationTap(BackgroundFCMRemoteMessage remoteMessage) {
        JSObject notificationJson = new JSObject();
        notificationJson.put("id", remoteMessage.getId());
        notificationJson.put("data", remoteMessage.getData());
        JSObject actionJson = new JSObject();
        actionJson.put("actionId", "tap");
        actionJson.put("notification", notificationJson);
        notifyListeners("pushNotificationActionPerformed", actionJson, true);
    }
    public static void onNotificationTap(BackgroundFCMRemoteMessage remoteMessage) {
        BackgroundFCM pushPlugin = BackgroundFCM.getBackgroundFCMInstance();
        if (pushPlugin == null) {
            BackgroundFCM.remoteMessage = remoteMessage;
        } else {
            pushPlugin.handleNotificationTap(remoteMessage);
        }

    }
    public static BackgroundFCM getBackgroundFCMInstance() {
        if (staticBridge != null && staticBridge.getWebView() != null) {
            PluginHandle handle = staticBridge.getPlugin("BackgroundFCM");
            if (handle == null) {
                return null;
            }
            return (BackgroundFCM) handle.getInstance();
        }
        return null;
    }
}
