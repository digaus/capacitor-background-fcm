declare global {
  interface PluginRegistry {
    BackgroundFCM: BackgroundFCMPlugin;
  }
}

export interface BackgroundFCMPlugin {
  setAdditionalData(options: { value: string }): Promise<{value: string}>;
}
