import { Plugins } from '@capacitor/core';
import {CallbackID, IniOptions, MainServiceModulePluginInterface, WatchPositionCallback, PositionOptions} from './definitions';

const _MainServiceModulePlugin: MainServiceModulePluginInterface = Plugins.MainServiceModulePlugin as any;

export class MainServiceModule implements MainServiceModulePluginInterface {
  echo(options: { value: string }): Promise<{ value: string }> {
    return _MainServiceModulePlugin.echo(options);
  }

  getAllPreviousPositions(): Promise<{ value: string }> {
    return _MainServiceModulePlugin.getAllPreviousPositions();
  }

  getCurrentPosition(): Promise<{ location: string }> {
    return _MainServiceModulePlugin.getCurrentPosition();
  }

  ini(options: IniOptions): Promise<void> {
    return _MainServiceModulePlugin.ini(options);
  }

  isDeviceSupported(): Promise<{ location: string; supported: boolean }> {
    return _MainServiceModulePlugin.isDeviceSupported();
  }

  stopTracking(): Promise<void> {
    return _MainServiceModulePlugin.stopTracking();
  }

  stopWatchPosition(): Promise<void> {
    return _MainServiceModulePlugin.stopWatchPosition();
  }

  watchPosition(options: PositionOptions,
                callback: WatchPositionCallback): Promise<CallbackID> {
    return _MainServiceModulePlugin.watchPosition(options, callback);
  }
}
