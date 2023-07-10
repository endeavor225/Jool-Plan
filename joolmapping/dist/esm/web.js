import { Plugins, WebPlugin } from '@capacitor/core';
const _JoolMappingPlugin = Plugins.DroneControllerPlugin;
export class JoolMappingWeb extends WebPlugin {
    startWaypointMission(callback) {
        return _JoolMappingPlugin.startWaypointMission(callback);
    }
    continuWaypointMission(callback) {
        return _JoolMappingPlugin.continuWaypointMission(callback);
    }
    isMissionExist() {
        return _JoolMappingPlugin.isMissionExist();
    }
    cleanUserData() {
        return _JoolMappingPlugin.cleanUserData();
    }
    async echo(options) {
        return _JoolMappingPlugin.echo(options);
    }
    attachBaseContext() {
        return _JoolMappingPlugin.attachBaseContext();
    }
    disconnect() {
        return _JoolMappingPlugin.disconnect();
    }
    ini() {
        return _JoolMappingPlugin.ini();
    }
    connect() {
        return _JoolMappingPlugin.connect();
    }
    onCreate() {
        return _JoolMappingPlugin.onCreate();
    }
    watchDroneTelemetry(callback) {
        return _JoolMappingPlugin.watchDroneTelemetry(callback);
    }
    stopWatchDroneTelemetry() {
        return _JoolMappingPlugin.stopWatchDroneTelemetry();
    }
    returnToHome() {
        return _JoolMappingPlugin.returnToHome();
    }
    setMissionSetting(options) {
        return _JoolMappingPlugin.setMissionSetting(options);
    }
    logToFile(parameter) {
        return _JoolMappingPlugin.logToFile(parameter);
    }
    performChecklist(parameter, callback) {
        return _JoolMappingPlugin.performChecklist(parameter, callback);
    }
    getMissionInfo(callback) {
        return _JoolMappingPlugin.getMissionInfo(callback);
    }
}
//# sourceMappingURL=web.js.map