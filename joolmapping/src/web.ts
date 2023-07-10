import { Plugins, WebPlugin } from '@capacitor/core';

import type {
  JoolMappingPlugin,
  WatchPositionCallback,
  CallbackID,
  MissionCallback,
  MissionOptions,
  CheckListCallback,
  InfoCallback,
  StoreState
} from './definitions';

const _JoolMappingPlugin: JoolMappingPlugin = Plugins.DroneControllerPlugin as any;


export class JoolMappingWeb extends WebPlugin implements JoolMappingPlugin {
  startWaypointMission(callback: MissionCallback): Promise<CallbackID> {
    return _JoolMappingPlugin.startWaypointMission(callback);
  }

  continuWaypointMission(callback: MissionCallback): Promise<CallbackID> {
    return _JoolMappingPlugin.continuWaypointMission(callback);
  }

  isMissionExist(): Promise<StoreState> {
    return _JoolMappingPlugin.isMissionExist();
  }

  cleanUserData(): Promise<void> {
    return _JoolMappingPlugin.cleanUserData();
  }

  async echo(options: { value: string }): Promise<{ value: string }> {
    return _JoolMappingPlugin.echo(options);
  }

  attachBaseContext(): Promise<void> {
    return _JoolMappingPlugin.attachBaseContext();
  }

  disconnect(): Promise<void> {
    return _JoolMappingPlugin.disconnect();
  }

  ini(): Promise<void> {
    return _JoolMappingPlugin.ini();
  }

  connect(): Promise<void> {
    return _JoolMappingPlugin.connect();
  }

  onCreate(): Promise<void> {
    return _JoolMappingPlugin.onCreate();
  }

  watchDroneTelemetry(callback: WatchPositionCallback): Promise<CallbackID> {
    return _JoolMappingPlugin.watchDroneTelemetry(callback);
  }

  stopWatchDroneTelemetry(): Promise<void> {
    return _JoolMappingPlugin.stopWatchDroneTelemetry();
  }

  returnToHome(): Promise<void> {
    return _JoolMappingPlugin.returnToHome();
  }

  setMissionSetting(options: MissionOptions): Promise<void> {
    return _JoolMappingPlugin.setMissionSetting(options);
  }

  logToFile(parameter: { message: string }): Promise<void> {
    return _JoolMappingPlugin.logToFile(parameter);
  }

  performChecklist(parameter: { caseContinuMission: boolean }, callback:CheckListCallback): Promise<CallbackID> {
    return _JoolMappingPlugin.performChecklist(parameter,callback);
  }

  getMissionInfo(callback: InfoCallback): Promise<CallbackID> {
    return _JoolMappingPlugin.getMissionInfo(callback);
  }
}
