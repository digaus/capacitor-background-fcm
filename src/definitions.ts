declare global {
  interface PluginRegistry {
    BackgroundFCM: BackgroundFCMPlugin;
  }
}

export interface BackgroundFCMPlugin {
  writeToFile(options: { value: string }): Promise<{value: string}>;
}
