
declare module "@capacitor/core" {
  interface PluginRegistry {
    MainServiceModulePlugin: MainServiceModulePluginInterface;
  }
}

export type CallbackID = string;

export type WatchPositionCallback = (
  position: Position | null,
  err?: any,
) => void;

export interface PositionOptions {
  enableHighAccuracy?: boolean;
  timeout?: number;
  maximumAge?: number;
}

export type GeolocationWatchCallback = WatchPositionCallback;

export interface IniOptions {
  interval?: number;
}


export interface Location {
  location: string | null;
  supported: boolean;
}

export interface CallbackError extends Error {
  code?: string;
}

export interface MainServiceModulePluginInterface {
  echo(options: { value: string }): Promise<{ value: string }>;
  stopTracking(): Promise<void>;
  ini(options: IniOptions): Promise<void>;
  getAllPreviousPositions(): Promise<{ value: string }>;
  getCurrentPosition(): Promise<{ location: string }>;
  watchPosition(
    options: PositionOptions,
    callback: WatchPositionCallback,
  ): Promise<CallbackID>;
  stopWatchPosition(): Promise<void>;
  isDeviceSupported(): Promise<{ location: string, supported: boolean  }>;
}

