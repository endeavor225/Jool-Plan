'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

var core = require('@capacitor/core');

const JoolMapping = core.registerPlugin('JoolMapping', {
    web: () => Promise.resolve().then(function () { return web; }).then(m => new m.JoolMappingWeb()),
});

const _JoolMappingPlugin = core.Plugins.DroneControllerPlugin;
class JoolMappingWeb extends core.WebPlugin {
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

var web = /*#__PURE__*/Object.freeze({
    __proto__: null,
    JoolMappingWeb: JoolMappingWeb
});

exports.JoolMapping = JoolMapping;
//# sourceMappingURL=plugin.cjs.js.map
