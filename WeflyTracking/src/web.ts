import { WebPlugin } from '@capacitor/core';
import {CallbackID, IniOptions, MainServiceModulePluginInterface, WatchPositionCallback, PositionOptions} from './definitions';

export class MainServiceModulePluginWeb extends WebPlugin implements MainServiceModulePluginInterface {

  constructor() {
    super({
      name: 'MainServiceModulePlugin',
      platforms: ['web']
    });
  }

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.warn('echo Not Yet Implemented options', options);
    return {
      value: ''
    };
  }

  async getAllPreviousPositions(): Promise<{ value: string }> {
    console.warn('getAllPreviousPositions Not Yet Implemented');
    return {
      value: ''
    };
  }

  async getCurrentPosition(): Promise<{ location: string }> {
    console.warn('getCurrentPosition Not Yet Implemented');
    return {
      location: ''
    };
  }

  async ini(options: IniOptions): Promise<void> {
    console.warn('getCurrentPosition Not Yet Implemented ', options);
  }

  async isDeviceSupported(): Promise<{ location: string; supported: boolean }> {
    console.warn('isDeviceSupported Not Yet Implemented ');
    return {
      location: '',
      supported: false
    };
  }

  async stopTracking(): Promise<void> {
    console.warn('stopTracking Not Yet Implemented ');
  }

  async stopWatchPosition(): Promise<void> {
    console.warn('stopWatchPosition Not Yet Implemented ');
  }

  async watchPosition(
    options: PositionOptions,
    callback: WatchPositionCallback,
  ): Promise<CallbackID> {
    console.warn('stopWatchPosition Not Yet Implemented ', options, ' callback ', callback);
    const id = 3;
    return `${id}`;
  }
}

const MainServiceModulePlugin = new MainServiceModulePluginWeb();

export { MainServiceModulePlugin };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(MainServiceModulePlugin);
