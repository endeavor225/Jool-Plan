export interface ComputationResult {
    flyTime?: number;
    imageCount?: number;
    battery?: number;
    speed?: number;
    surveyPoints?: any;
    gridProgressing?: number;
    error?: string;
}
export interface CheckListResult {
    phoneBattery?: boolean;
    droneBattery?: boolean;
    acces?: boolean;
    config?: boolean;
    controle?: boolean;
    camera?: boolean;
    flyplan?: boolean;
    gps?: boolean;
    cameraCalibrated?: boolean;
    sdcard?: boolean;
    error?: string;
    diagnostic?: string;
    checkSucces?: boolean;
}
export interface CurrentDroneState {
    uplinkSignalQuality?: number;
    satelliteCount?: number;
    chargeRemainingInPercent?: number;
    chargeRemainingInMAh?: number;
    temperature?: number;
    altitudeInMeters?: number;
    speedInMeterPerSec?: number;
    heading?: number;
    numberOfDischarges?: number;
    lifetimeRemainingInPercent?: number;
    longitude?: number;
    latitude?: number;
    altitude?: number;
    droneHeadingInDegrees?: number;
    droneModelName?: number;
    distanceInMeter?: number;
}
export interface CurrentMissionState {
    startSucces?: boolean;
    finishSucces?: boolean;
    stepRemain?: number;
    gridProgressing?: number;
    battery?: number;
    imageCount?: number;
    speed?: number;
    surveyPoints?: string;
    error?: string;
    lowBattery?: string;
}
export interface StoreState {
    isExist?: boolean;
}
export interface MissionOptions {
    altitude?: number;
    exitMissionOnRCLost?: boolean;
    autoGoHomeOnLowBattery?: boolean;
    daylight?: number;
    phoneLatitude?: number;
    phoneLongitude?: number;
    polygonPoints?: any;
}
export interface CurrentPhoneLocation {
    longitude?: number;
    latitude?: number;
}
export declare type CallbackID = string;
export declare type InfoCallback = (computationSettings: ComputationResult | null, err?: any) => void;
export declare type CheckListCallback = (checkListSettings: CheckListResult | null, err?: any) => void;
export declare type WatchPositionCallback = (position: CurrentDroneState | null, err?: any) => void;
export declare type MissionCallback = (missionSettings: CurrentMissionState | null, err?: any) => void;
export interface JoolMappingPlugin {
    /**
     * Test of Bridge between JS and Java works great.
     */
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
    /**
     * Write Production in File.txt stored in /Documents/YouAppName
     */
    logToFile(parameter: {
        message: string;
    }): Promise<void>;
    /**
     * Enable USB connection between Mobile device and Drone Remote Control
     */
    ini(): Promise<void>;
    /**
     * Initialise Drone DJI SDK and connect SDK to this Application (Done Automatically)
     */
    attachBaseContext(): Promise<void>;
    /**
     * Connect DJI Singleton Class to current Application Singleton Class (Done Automatically)
     */
    onCreate(): Promise<void>;
    /**
     * Connect DJI to current Drone and perform DJI Registration
     */
    connect(): Promise<void>;
    /**
     * Disconnect the drone currently connected to this app (not available for DJI drone at this time)
     */
    disconnect(): Promise<void>;
    /**
     * Watch Drone Telemetry information
     *
     * Type Object: callback.position - Contain Telemetry info
     *
     * Type number: callback.position.uplinkSignalQuality - Remote Controller Signal quality
     *
     * Type number: callback.position.satelliteCount - Nombre de satellites utilisés pour la précision GPS
     *
     * Type number: callback.position.chargeRemainingInPercent - Battery remaining in percent
     *
     * Type number: [callback.position.chargeRemainingInMAh - Battery remaining in mAh
     *
     * Type number: [callback.position.temperature - Drone temperature
     *
     * Type number: [callback.position.altitudeInMeters - Current Drone altitude in meter
     *
     * Type number: [callback.position.speedInMeterPerSec - Current Drone Speed in meter/sec (2 dimensions)
     *
     * Type number: [callback.position.heading - Compass heading value is [-180 to 180] ..0 is North
     *
     * Type number: [callback.position.droneHeadingInDegrees - Compass heading in Degree value is [0 to 360]
     *
     * Type number: [callback.position.numberOfDischarges - number of drone battery discharges
     *
     * Type number: [callback.position.lifetimeRemainingInPercent - Drone battery lifetime remaining in percent ..Warning if is not supported the value is always 0
     *
     * Type number: [callback.position.longitude  - Current drone longitude, Can be null if no satellite found for precision
     *
     * Type number: [callback.position.latitude  - Current drone latitude, Can be null if no satellite found for precision
     *
     * Type number: [callback.position.altitude  - Current drone altitude
     *
     * Type number: [callback.position.droneModelName  - Current drone model Name
     *
     * Type number: callback.err - Will be not null if error happen and given more information about this error
     */
    watchDroneTelemetry(callback: WatchPositionCallback): Promise<CallbackID>;
    /**
     * Stop watchDroneTelemetry function and disable drone telemetry
     */
    stopWatchDroneTelemetry(): Promise<void>;
    /**
     * Safely go back at home
     */
    returnToHome(): Promise<void>;
    /**
     * Configure new mission setting
     *
     * Type number: options.altitude - Set Mission altitude in meter. from 50 to 100 meters
     *
     * Type boolean: options.exitMissionOnRCLost - Cancel or Continue mission When signal lost with Drone
     *
     * Type boolean: options.autoGoHomeOnLowBattery - Drone go back to Home When mission done
     *
     * Type boolean: options.useSimulator - Test in Simulator
     *
     * Type number: options.daylight - Select current daylight between 1-NORMAL, 2- NUAGEUSE, 3-ENSOLEILLEE
     *
     * Type number: options.phoneLatitude - set Current Android phone location latitude
     *
     * Type number: options.phoneLongitude - set Current Android phone location longitude
     *
     * Type Object: options.polygonPoints - set GEOJSON polygon to process
     *
     */
    setMissionSetting(options: MissionOptions): Promise<void>;
    /**
     * Start New Mission with given Polygon
     *
     * Type number: callback.missionState.startSucces - Give Waypoint Mission starting state : Started or Not. Type: boolean but can by null
     *
     * Type number: callback.missionState.finishSucces - Give Waypoint Mission ending state : Finish or Error. Type: boolean but can by null
     *
     * Type number: callback.missionState.stepLeft - Mission steps count . 1 step = 0 to 99 Waypoints. Type: number but can by null
     */
    startWaypointMission(callback: MissionCallback): Promise<CallbackID>;
    /**
     * Check if old Waypoint Mission Exist
     */
    isMissionExist(): Promise<StoreState>;
    /**
     * Resume old mission if exist
     *
     * Type Object: callback.isExist - Type: boolean but can by null
     */
    continuWaypointMission(callback: MissionCallback): Promise<CallbackID>;
    /**
     * Delete old mission
     */
    cleanUserData(): Promise<void>;
    /**
     * Verify settings before start Mission
     *
     * Type boolean: callback.checkListSettings.phoneBattery - Give phone battery checking state : ok or Not. Type: boolean but can by null
     *
     * Type boolean: callback.checkListSettings.droneBattery - Give drone battery checking state : ok or Not. Type: boolean but can by null
     *
     * Type boolean: callback.checkListSettings.acces - Check if connection to drone RC control is ok or Not. Type: boolean but can by null
     *
     * Type boolean: callback.checkListSettings.config - Check if drone configuration done: ok or Not. Type: boolean but can by null
     *
     * Type boolean: callback.checkListSettings.controle - Check if can take drone control: ok or Not. Type: boolean but can by null
     *
     * Type boolean: callback.checkListSettings.camera - Check if drone camera config is ok : ok or Not. Type: boolean but can by null
     *
     * Type boolean: callback.checkListSettings.flyplan - Verify if drone flight path is ok and drone ready .Type: boolean but can by null
     *
     * Type boolean: callback.checkListSettings.gps - Verify is GPS signal is enough or not Type: boolean but can by null
     *
     * Type boolean: callback.checkListSettings.cameraCalibrated - Verify if Camera is calibrated .  Type: boolean but can by null
     *
     * Type boolean: callback.checkListSettings.sdcard - Verify if Camera is connected and ready to use .  Type: boolean but can by null
     *
     * Type string: callback.checkListSettings.diagnostic - Give all drone components diagnostic result .  Type: string but can by null
     *
     * Type string: callback.checkListSettings.error - Give error detail when something wrong: string but can by null
     *
     * Type boolean: callback.checkListSettings.checkSucces - Mean checklist done and everything is fine .Type: boolean but can by null
     */
    performChecklist(parameter: {
        caseContinuMission: boolean;
    }, callback: CheckListCallback): Promise<CallbackID>;
    /**
     * Show Mission Evaluation
     *
     * Type number: callback.computationSettings.flyTime - Give mission duration. number but can by null
     *
     * Type number: callback.computationSettings.imageCount - Mission image count. Type: number but can by null
     *
     * Type number: callback.computationSettings.battery - Show how many battery required for this  mission. Type: number but can by null
     *
     * Type boolean: callback.computationSettings.speed - Give drone speed in meter / second for this mission. Type: number but can by null
     *
     * Type number: callback.computationSettings.speed - Give drone speed in meter / second for this mission. Type: number but can by null
     *
     * Type number: [callback.computationSettings.gridProgressing - Asynchronous processing percent. Type: number but can by null
     *
     * Type any: callback.computationSettings.surveyPoints - Give mission fly path polyline. Type: any but can by null
     *
     * Type string: callback.computationSettings.error - Give error detail when something wrong: string but can by null
     *
     */
    getMissionInfo(callback: InfoCallback): Promise<CallbackID>;
}
