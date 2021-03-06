import { PushNotificationActionPerformed, PluginListenerHandle } from '@capacitor/core'
declare module '@capacitor/core' {
  interface PluginRegistry {
    BackgroundFCM: BackgroundFCMPlugin;
  }
}

export interface BackgroundFCMPlugin  {
  setAdditionalData(options: { value: string }): Promise<{value: string}>;
  addListener(eventName: 'pushNotificationActionPerformed', listenerFunc: (notification: PushNotificationActionPerformed) => void): PluginListenerHandle;
}
