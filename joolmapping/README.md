# joolmapping

Perform mapping mission with DJI Drone

## Install

```bash
npm install joolmapping
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`logToFile(...)`](#logtofile)
* [`ini()`](#ini)
* [`attachBaseContext()`](#attachbasecontext)
* [`onCreate()`](#oncreate)
* [`connect()`](#connect)
* [`disconnect()`](#disconnect)
* [`watchDroneTelemetry(...)`](#watchdronetelemetry)
* [`stopWatchDroneTelemetry()`](#stopwatchdronetelemetry)
* [`returnToHome()`](#returntohome)
* [`setMissionSetting(...)`](#setmissionsetting)
* [`startWaypointMission(...)`](#startwaypointmission)
* [`isMissionExist()`](#ismissionexist)
* [`continuWaypointMission(...)`](#continuwaypointmission)
* [`cleanUserData()`](#cleanuserdata)
* [`performChecklist(...)`](#performchecklist)
* [`getMissionInfo(...)`](#getmissioninfo)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

Test of Bridge between JS and Java works great.

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### logToFile(...)

```typescript
logToFile(parameter: { message: string; }) => Promise<void>
```

Write Production in File.txt stored in /Documents/YouAppName

| Param           | Type                              |
| --------------- | --------------------------------- |
| **`parameter`** | <code>{ message: string; }</code> |

--------------------


### ini()

```typescript
ini() => Promise<void>
```

Enable USB connection between Mobile device and Drone Remote Control

--------------------


### attachBaseContext()

```typescript
attachBaseContext() => Promise<void>
```

Initialise Drone DJI SDK and connect SDK to this Application (Done Automatically)

--------------------


### onCreate()

```typescript
onCreate() => Promise<void>
```

Connect DJI Singleton Class to current Application Singleton Class (Done Automatically)

--------------------


### connect()

```typescript
connect() => Promise<void>
```

Connect DJI to current Drone and perform DJI Registration

--------------------


### disconnect()

```typescript
disconnect() => Promise<void>
```

Disconnect the drone currently connected to this app (not available for DJI drone at this time)

--------------------


### watchDroneTelemetry(...)

```typescript
watchDroneTelemetry(callback: WatchPositionCallback) => Promise<CallbackID>
```

Watch Drone Telemetry information

Type Object: callback.position - Contain Telemetry info

Type number: callback.position.uplinkSignalQuality - Remote Controller Signal quality

Type number: callback.position.satelliteCount - Nombre de satellites utilisés pour la précision GPS

Type number: callback.position.chargeRemainingInPercent - Battery remaining in percent

Type number: [callback.position.chargeRemainingInMAh - Battery remaining in mAh

Type number: [callback.position.temperature - Drone temperature

Type number: [callback.position.altitudeInMeters - Current Drone altitude in meter

Type number: [callback.position.speedInMeterPerSec - Current Drone Speed in meter/sec (2 dimensions)

Type number: [callback.position.heading - Compass heading value is [-180 to 180] ..0 is North

Type number: [callback.position.droneHeadingInDegrees - Compass heading in Degree value is [0 to 360]

Type number: [callback.position.numberOfDischarges - number of drone battery discharges

Type number: [callback.position.lifetimeRemainingInPercent - Drone battery lifetime remaining in percent ..Warning if is not supported the value is always 0

Type number: [callback.position.longitude  - Current drone longitude, Can be null if no satellite found for precision

Type number: [callback.position.latitude  - Current drone latitude, Can be null if no satellite found for precision

Type number: [callback.position.altitude  - Current drone altitude

Type number: [callback.position.droneModelName  - Current drone model Name

Type number: callback.err - Will be not null if error happen and given more information about this error

| Param          | Type                                                                     |
| -------------- | ------------------------------------------------------------------------ |
| **`callback`** | <code>(position: CurrentDroneState \| null, err?: any) =&gt; void</code> |

**Returns:** <code>Promise&lt;string&gt;</code>

--------------------


### stopWatchDroneTelemetry()

```typescript
stopWatchDroneTelemetry() => Promise<void>
```

Stop watchDroneTelemetry function and disable drone telemetry

--------------------


### returnToHome()

```typescript
returnToHome() => Promise<void>
```

Safely go back at home

--------------------


### setMissionSetting(...)

```typescript
setMissionSetting(options: MissionOptions) => Promise<void>
```

Configure new mission setting

Type number: options.altitude - Set Mission altitude in meter. from 50 to 100 meters

Type boolean: options.exitMissionOnRCLost - Cancel or Continue mission When signal lost with Drone

Type boolean: options.autoGoHomeOnLowBattery - Drone go back to Home When mission done

Type boolean: options.useSimulator - Test in Simulator

Type number: options.daylight - Select current daylight between 1-NORMAL, 2- NUAGEUSE, 3-ENSOLEILLEE

Type number: options.phoneLatitude - set Current Android phone location latitude

Type number: options.phoneLongitude - set Current Android phone location longitude

Type Object: options.polygonPoints - set GEOJSON polygon to process

| Param         | Type                                                      |
| ------------- | --------------------------------------------------------- |
| **`options`** | <code><a href="#missionoptions">MissionOptions</a></code> |

--------------------


### startWaypointMission(...)

```typescript
startWaypointMission(callback: MissionCallback) => Promise<CallbackID>
```

Start New Mission with given Polygon

Type number: callback.missionState.startSucces - Give Waypoint Mission starting state : Started or Not. Type: boolean but can by null

Type number: callback.missionState.finishSucces - Give Waypoint Mission ending state : Finish or Error. Type: boolean but can by null

Type number: callback.missionState.stepLeft - Mission steps count . 1 step = 0 to 99 Waypoints. Type: number but can by null

| Param          | Type                                                                              |
| -------------- | --------------------------------------------------------------------------------- |
| **`callback`** | <code>(missionSettings: CurrentMissionState \| null, err?: any) =&gt; void</code> |

**Returns:** <code>Promise&lt;string&gt;</code>

--------------------


### isMissionExist()

```typescript
isMissionExist() => Promise<StoreState>
```

Check if old Waypoint Mission Exist

**Returns:** <code>Promise&lt;<a href="#storestate">StoreState</a>&gt;</code>

--------------------


### continuWaypointMission(...)

```typescript
continuWaypointMission(callback: MissionCallback) => Promise<CallbackID>
```

Resume old mission if exist

Type Object: callback.isExist - Type: boolean but can by null

| Param          | Type                                                                              |
| -------------- | --------------------------------------------------------------------------------- |
| **`callback`** | <code>(missionSettings: CurrentMissionState \| null, err?: any) =&gt; void</code> |

**Returns:** <code>Promise&lt;string&gt;</code>

--------------------


### cleanUserData()

```typescript
cleanUserData() => Promise<void>
```

Delete old mission

--------------------


### performChecklist(...)

```typescript
performChecklist(parameter: { caseContinuMission: boolean; }, callback: CheckListCallback) => Promise<CallbackID>
```

Verify settings before start Mission

Type boolean: callback.checkListSettings.phoneBattery - Give phone battery checking state : ok or Not. Type: boolean but can by null

Type boolean: callback.checkListSettings.droneBattery - Give drone battery checking state : ok or Not. Type: boolean but can by null

Type boolean: callback.checkListSettings.acces - Check if connection to drone RC control is ok or Not. Type: boolean but can by null

Type boolean: callback.checkListSettings.config - Check if drone configuration done: ok or Not. Type: boolean but can by null

Type boolean: callback.checkListSettings.controle - Check if can take drone control: ok or Not. Type: boolean but can by null

Type boolean: callback.checkListSettings.camera - Check if drone camera config is ok : ok or Not. Type: boolean but can by null

Type boolean: callback.checkListSettings.flyplan - Verify if drone flight path is ok and drone ready .Type: boolean but can by null

Type boolean: callback.checkListSettings.gps - Verify is GPS signal is enough or not Type: boolean but can by null

Type boolean: callback.checkListSettings.cameraCalibrated - Verify if Camera is calibrated .  Type: boolean but can by null

Type boolean: callback.checkListSettings.sdcard - Verify if Camera is connected and ready to use .  Type: boolean but can by null

Type string: callback.checkListSettings.diagnostic - Give all drone components diagnostic result .  Type: string but can by null

Type string: callback.checkListSettings.error - Give error detail when something wrong: string but can by null

Type boolean: callback.checkListSettings.checkSucces - Mean checklist done and everything is fine .Type: boolean but can by null

| Param           | Type                                                                            |
| --------------- | ------------------------------------------------------------------------------- |
| **`parameter`** | <code>{ caseContinuMission: boolean; }</code>                                   |
| **`callback`**  | <code>(checkListSettings: CheckListResult \| null, err?: any) =&gt; void</code> |

**Returns:** <code>Promise&lt;string&gt;</code>

--------------------


### getMissionInfo(...)

```typescript
getMissionInfo(callback: InfoCallback) => Promise<CallbackID>
```

Show Mission Evaluation

Type number: callback.computationSettings.flyTime - Give mission duration. number but can by null

Type number: callback.computationSettings.imageCount - Mission image count. Type: number but can by null

Type number: callback.computationSettings.battery - Show how many battery required for this  mission. Type: number but can by null

Type boolean: callback.computationSettings.speed - Give drone speed in meter / second for this mission. Type: number but can by null

Type number: callback.computationSettings.speed - Give drone speed in meter / second for this mission. Type: number but can by null

Type number: [callback.computationSettings.gridProgressing - Asynchronous processing percent. Type: number but can by null

Type any: callback.computationSettings.surveyPoints - Give mission fly path polyline. Type: any but can by null

Type string: callback.computationSettings.error - Give error detail when something wrong: string but can by null

| Param          | Type                                                                                |
| -------------- | ----------------------------------------------------------------------------------- |
| **`callback`** | <code>(computationSettings: ComputationResult \| null, err?: any) =&gt; void</code> |

**Returns:** <code>Promise&lt;string&gt;</code>

--------------------


### Interfaces


#### MissionOptions

| Prop                         | Type                 |
| ---------------------------- | -------------------- |
| **`altitude`**               | <code>number</code>  |
| **`exitMissionOnRCLost`**    | <code>boolean</code> |
| **`autoGoHomeOnLowBattery`** | <code>boolean</code> |
| **`daylight`**               | <code>number</code>  |
| **`phoneLatitude`**          | <code>number</code>  |
| **`phoneLongitude`**         | <code>number</code>  |
| **`polygonPoints`**          | <code>any</code>     |


#### StoreState

| Prop          | Type                 |
| ------------- | -------------------- |
| **`isExist`** | <code>boolean</code> |

</docgen-api>
