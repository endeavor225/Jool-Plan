import { WebPlugin } from '@capacitor/core';
import type { JoolMappingPlugin, WatchPositionCallback, CallbackID, MissionCallback, MissionOptions, CheckListCallback, InfoCallback, StoreState } from './definitions';
export declare class JoolMappingWeb extends WebPlugin implements JoolMappingPlugin {
    startWaypointMission(callback: MissionCallback): Promise<CallbackID>;
    continuWaypointMission(callback: MissionCallback): Promise<CallbackID>;
    isMissionExist(): Promise<StoreState>;
    cleanUserData(): Promise<void>;
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
    attachBaseContext(): Promise<void>;
    disconnect(): Promise<void>;
    ini(): Promise<void>;
    connect(): Promise<void>;
    onCreate(): Promise<void>;
    watchDroneTelemetry(callback: WatchPositionCallback): Promise<CallbackID>;
    stopWatchDroneTelemetry(): Promise<void>;
    returnToHome(): Promise<void>;
    setMissionSetting(options: MissionOptions): Promise<void>;
    logToFile(parameter: {
        message: string;
    }): Promise<void>;
    performChecklist(parameter: {
        caseContinuMission: boolean;
    }, callback: CheckListCallback): Promise<CallbackID>;
    getMissionInfo(callback: InfoCallback): Promise<CallbackID>;
}
