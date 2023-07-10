package com.jool.plugin.mapping;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.jool.plugin.mapping.interfaces.OnAsyncOperationComplete;
import com.jool.plugin.mapping.interfaces.OnAsyncOperationCompleteBool;
import com.jool.plugin.mapping.interfaces.OnCheckListChangeListener;
import com.jool.plugin.mapping.interfaces.OnMissionRunningListener;
import com.jool.plugin.mapping.interfaces.OnProcessingCompleteListener;
import com.jool.plugin.mapping.interfaces.OnStateChangeListener;
import com.jool.plugin.mapping.mission.Constant;
import com.jool.plugin.mapping.mission.MissionSetting;
import com.jool.plugin.mapping.mission.MissionStep;
import com.jool.plugin.mapping.presenters.LandedDetectorModule;
import com.jool.plugin.mapping.presenters.PermissionManager;
import com.jool.plugin.mapping.utils.Constants;
import com.jool.plugin.mapping.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.CopyOnWriteArrayList;


@CapacitorPlugin(name = "JoolMapping")
public class JoolMappingPlugin extends Plugin {
  private  final String TAG = JoolMappingPlugin.this.getClass().getSimpleName();

  private boolean hasSave = false;
  private @Nullable
  Context ctx = null;
  private @Nullable static Application mainApp = null;
  private @Nullable String callBackIdConnect,callBackIdDisconnect,
    callBackIdwatchDrone,callBackIdstopWatchDrone,
    callBackIdsReturnToHome,
    callBackIdStartMission,
    callBackIdMissionInfo,
    callBackIdCheckList,
    callBackIdContinuMission,callBackIdsMissionExist,callBackIdsCleanData;
  private @Nullable LandedDetectorModule landedDetector;
  private final String startSuccesName      = "startSucces";
  private final String finishSuccesName     = "finishSucces";
  private final String stepRemainName       = "stepRemain";
  private final String gridProgressingName  = "gridProgressing";
  private final String batteryName          = "battery";
  private final String imageCountName       = "imageCount";
  private final String speedName            = "speed";
  private final String surveyPointsName     = "surveyPoints";
  private final String errorName            = "error";
  private final String lowBattery           = "lowBattery";
  private final String showLoader           = "showLoader";
  private final String hideLoader           = "hideLoader";
  private final String isMissionExistName   = "isExist";

  private final String flyTimeName          = "flyTime";
  private final String phoneBatteryStatutsName          = "phoneBattery";
  private final String droneBatteryStatutsName          = "droneBattery";
  private final String accesName            = "acces";
  private final String configName           = "config";
  private final String controleName         = "controle";
  private final String cameraName           = "camera";
  private final String cameraCalibratedName = "cameraCalibrated";
  private final String sdcardName           = "sdcard";
  private final String flyplanName          = "flyplan";
  private final String gpsName              = "gps";
  private final String diagnosticName       = "diagnostic";
  private final String checkSuccesName      = "checkSucces";
  private volatile @Nullable JSObject missionSetting = null;

  private @Nullable PluginCall currentBridge = null;

  @Nullable
  public PluginCall getCurrentBridge() throws NullPointerException {
    return currentBridge;
  }

  public void setCurrentBridge(@Nullable PluginCall currentBridge) {
    this.currentBridge = currentBridge;
  }

  @Nullable
  public static Application getMainApp()throws NullPointerException {
    return mainApp;
  }

  public static void setMainApp(@Nullable Application mainApp) {
    JoolMappingPlugin.mainApp = mainApp;
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(MessageEvent event) {
    if (Utils.isNull(event)){
      toLog(TAG, "onMessageEvent event is Null ");
    }else {
      JSObject ret = new JSObject();
      ret.put("value", "some value");
      if (event.getCode() == Constants.CODE_ON_BACK_PRESSED){
        notifyListeners("backPressEvent", ret);
        return;
      }
      if (event.getCode() == Constants.CODE_DRONE_LOW_BATTERY){
        notifyListeners("droneLowBatteryEvent", ret);
        return;
      }
    }
  }

  @Override
  protected void handleOnStop() {
    super.handleOnStop();
    // Remove EventBus listener
    // EventBus.getDefault().unregister(this);
  }

  @Override
  public void load() {
    super.load();

    // Register EventBus
    // EventBus.getDefault().register(this);
    ctx = this.getContext();

    PermissionManager pm = new PermissionManager(ctx);
    if (!pm.isPermissionsGranted()){
      toLog(TAG, "MISSING PERMISSION ");
    }
    if (!pm.isSDKreadyForMappingProcess()){
      toLog(TAG, "SDK Not Ready for Mapping Process MIN IS API 24 ");
    }
    landedDetector  = new LandedDetectorModule();
    landedDetector.ini();
    landedDetector.register(this,this.getActivity());

  }

  @PluginMethod
  public void echo(PluginCall call) {
    String value = call.getString("value");

    JSObject ret = new JSObject();
    ret.put("value", "implementation.echo(value) Examle");
    call.resolve(ret);
  }

  @PluginMethod
  public void ini(PluginCall call){
    toLog(TAG, "ini start");
    try {
      if (Utils.isNull(ctx)){
        toLog(TAG, "ini stop cause ctx is Null");
        return;
      }else {
        if (Utils.isNull(call)){
          toLog(TAG, "ini stop cause call is Null");
          return;
        }else {
          PermissionManager pm = new PermissionManager(ctx);
          if (!pm.isPermissionsGranted()){
            toLog(TAG, "ini stop cause REQUIRE ALL PERMISSIONS");
            call.reject("REQUIRE ALL PERMISSIONS");
            return;
          }else{
            if (!pm.isSDKreadyForMappingProcess()){
              toLog(TAG, "SDK Not Ready for Mapping Process MIN IS API 24 ");
              call.reject("SDK Not Ready for Mapping Process MIN IS API 24");
              return;
            }else {
              DroneManager manager = DroneManager.getInstance();
              if (Utils.isNull(manager)){
                toLog(TAG, "ini stop cause manager is Null");
                call.reject("DroneManager is Null");
                return;
              }else {
                if (Utils.isNull(getMainApp())){
                  toLog(TAG, "ini stop cause getMainApp() is Null");
                  call.reject("PLEASE SET MainApp BEFORE RUN Apps (setMainApp) ...MainApp IS NULL RIGHT NOW");
                  return;
                }else {
                  manager.ini(ctx);
                  call.resolve();
                  toLog(TAG, "ini Launch Succes");
                }
              }
            }
          }
        }

      }
    }catch (Exception e){
      Utils.toLog(TAG, "ini",null,e);
      call.reject(e.getLocalizedMessage(), e);
    }catch (Error er){
      Utils.toLog(TAG, "ini",er,null);
      call.reject(er.getLocalizedMessage());
    }

  }

  @Override
  protected void handleOnDestroy() {
    super.handleOnDestroy();
    if (Utils.isNull(landedDetector)){
      return;
    }else {
      landedDetector.unRegister();
    }
  }

  @PluginMethod
  public void attachBaseContext(PluginCall call){
    toLog(TAG, "attachBaseContext start");
    try {
      if (Utils.isNull(ctx)){
        toLog(TAG, "attachBaseContext stop cause ctx is Null");
        return;
      }else {
        if (Utils.isNull(call)){
          toLog(TAG, "attachBaseContext stop cause call is Null");
          return;
        }else {
          PermissionManager pm = new PermissionManager(ctx);
          if (!pm.isPermissionsGranted()){
            toLog(TAG, "attachBaseContext stop cause REQUIRE ALL PERMISSIONS");
            call.reject("REQUIRE ALL PERMISSIONS");
            return;
          }else{
            if (!pm.isSDKreadyForMappingProcess()){
              toLog(TAG, "SDK Not Ready for Mapping Process MIN IS API 24 ");
              call.reject("SDK Not Ready for Mapping Process MIN IS API 24");
              return;
            }else {
              DroneManager manager = DroneManager.getInstance();
              if (Utils.isNull(manager)){
                toLog(TAG, "attachBaseContext stop cause manager is Null");
                call.reject("DroneManager is Null");
                return;
              }else {
                if (Utils.isNull(getMainApp())){
                  toLog(TAG, "attachBaseContext stop cause getMainApp() is Null");
                  call.reject("PLEASE SET MainApp BEFORE RUN Apps (setMainApp) ...MainApp IS NULL RIGHT NOW");
                  return;
                }else {
                  manager.attachBaseContext(ctx,getMainApp());
                  call.resolve();
                  toLog(TAG, "attachBaseContext Success launch");
                }
              }
            }
          }
        }
      }
    }catch (Exception e){
      Utils.toLog(TAG, "attachBaseContext",null,e);
      call.reject(e.getLocalizedMessage(), e);
    }catch (Error er){
      Utils.toLog(TAG, "attachBaseContext",er,null);
      call.reject(er.getLocalizedMessage());
    }
  }

  @PluginMethod
  public void onCreate(PluginCall call){
    toLog(TAG, "onCreate start");
    try {
      if (Utils.isNull(ctx)){
        toLog(TAG, "onCreate stop cause ctx is Null");
        return;
      }else {
        if (Utils.isNull(call)){
          toLog(TAG, "onCreate stop cause call is Null");
          return;
        }else {
          PermissionManager pm = new PermissionManager(ctx);
          if (!pm.isPermissionsGranted()){
            toLog(TAG, "onCreate stop cause REQUIRE ALL PERMISSIONS");
            call.reject("REQUIRE ALL PERMISSIONS");
            return;
          }else{
            if (!pm.isSDKreadyForMappingProcess()){
              toLog(TAG, "SDK Not Ready for Mapping Process MIN IS API 24 ");
              call.reject("SDK Not Ready for Mapping Process MIN IS API 24");
              return;
            }else {
              DroneManager manager = DroneManager.getInstance();
              if (Utils.isNull(manager)){
                toLog(TAG, "onCreate stop cause manager is Null");
                call.reject("DroneManager is Null");
                return;
              }else {
                if (Utils.isNull(getMainApp())){
                  toLog(TAG, "onCreate stop cause getMainApp() is Null");
                  call.reject("PLEASE SET MainApp BEFORE RUN Apps (setMainApp) ...MainApp IS NULL RIGHT NOW");
                  return;
                }else {
                  manager.onCreate();
                  call.resolve();
                  toLog(TAG, "onCreate Success launch");
                }
              }
            }
          }
        }
      }
    }catch (Exception e){
      Utils.toLog(TAG, "onCreate",null,e);
      call.reject(e.getLocalizedMessage(), e);
    }catch (Error er){
      Utils.toLog(TAG, "onCreate",er,null);
      call.reject(er.getLocalizedMessage());
    }
  }

  @PluginMethod
  public void connect(PluginCall call){
    toLog(TAG, "connect start");
    try {
      if (Utils.isNull(ctx)){
        toLog(TAG, "connect stop cause ctx is Null");
        return;
      }else {
        if (Utils.isNull(call)){
          toLog(TAG, "connect stop cause call is Null");
          return;
        }else {
          PermissionManager pm = new PermissionManager(ctx);
          if (!pm.isPermissionsGranted()){
            toLog(TAG, "connect stop cause REQUIRE ALL PERMISSIONS");
            call.reject("REQUIRE ALL PERMISSIONS");
            return;
          }else{
            if (!pm.isSDKreadyForMappingProcess()){
              toLog(TAG, "SDK Not Ready for Mapping Process MIN IS API 24 ");
              call.reject("SDK Not Ready for Mapping Process MIN IS API 24");
              return;
            }else {
              DroneManager manager = DroneManager.getInstance();
              if (Utils.isNull(manager)){
                toLog(TAG, "connect stop cause manager is Null");
                call.reject("DroneManager is Null");
                return;
              }else {
                if (Utils.isNull(getMainApp())){
                  toLog(TAG, "connect stop cause getMainApp() is Null");
                  call.reject("PLEASE SET MainApp BEFORE RUN Apps (setMainApp) ...MainApp IS NULL RIGHT NOW");
                  return;
                }else {
                  // Keep At the End of function
                  callBackIdConnect = null;
                  call.save();
                  callBackIdConnect = Utils.clone(call.getCallbackId());

                  manager.connect(new OnAsyncOperationComplete() {
                    @Override
                    public void onError(@Nullable String errorDetail) {
                      try {
                        if (Utils.isNull(errorDetail)){
                          errorDetail = "";
                        }
                        PluginCall call = bridge.getSavedCall(callBackIdConnect);
                        if (call == null) {
                          toLog(TAG , "connect onError call Is Null ");
                          return;
                        }else {
                          call.reject(errorDetail);
                          call.release(bridge);
                          callBackIdConnect = null;
                        }
                      }catch (Exception e){
                        Utils.toLog(TAG, "connect",null,e);
                        call.reject(e.getLocalizedMessage(), e);
                      }catch (Error er){
                        Utils.toLog(TAG, "connect",er,null);
                        call.reject(er.getLocalizedMessage());
                      }

                    }

                    @Override
                    public void onSucces(@Nullable String succesMsg) {
                      // bridge.getSavedCall(id) if not working
                      try {
                        PluginCall call = bridge.getSavedCall(callBackIdConnect);
                        if (call == null) {
                          toLog(TAG , "connect onError call Is Null ");
                          return;
                        }else {
                          if (Utils.isNull(succesMsg)){
                            succesMsg = "";
                          }
                          call.resolve();
                          call.release(bridge);
                          callBackIdConnect =null;
                        }
                      }catch (Exception e){
                        Utils.toLog(TAG, "connect",null,e);
                        call.reject(e.getLocalizedMessage(), e);
                      }catch (Error er){
                        Utils.toLog(TAG, "connect",er,null);
                        call.reject(er.getLocalizedMessage());
                      }

                    }
                  });
                  toLog(TAG, "connect Success launch");
                }
              }
            }
          }
        }
      }
    }catch (Exception e){
      Utils.toLog(TAG, "connect",null,e);
      call.reject(e.getLocalizedMessage(), e);
    }catch (Error er){
      Utils.toLog(TAG, "connect",er,null);
      call.reject(er.getLocalizedMessage());
    }
  }

  @PluginMethod
  public void setMissionSetting(PluginCall call){
    toLog(TAG, "setMissionSetting start");
    missionSetting =null;
    try {
      if (Utils.isNull(ctx)){
        toLog(TAG, "setMissionSetting stop cause ctx is Null");
        return;
      }else {
        if (Utils.isNull(call)){
          toLog(TAG, "setMissionSetting stop cause call is Null");
          return;
        }else {
          PermissionManager pm = new PermissionManager(ctx);
          if (!pm.isPermissionsGranted()){
            toLog(TAG, "setMissionSetting stop cause REQUIRE ALL PERMISSIONS");
            call.reject("REQUIRE ALL PERMISSIONS");
            return;
          }else{
            if (!pm.isSDKreadyForMappingProcess()){
              toLog(TAG, "SDK Not Ready for Mapping Process MIN IS API 24 ");
              call.reject("SDK Not Ready for Mapping Process MIN IS API 24");
              return;
            }else {
              DroneManager manager = DroneManager.getInstance();
              if (Utils.isNull(manager)){
                toLog(TAG, "setMissionSetting stop cause manager is Null");
                call.reject("DroneManager is Null");
                return;
              }else {
                if (Utils.isNull(getMainApp())){
                  toLog(TAG, "setMissionSetting stop cause getMainApp() is Null");
                  call.reject("PLEASE SET MainApp BEFORE RUN Apps (setMainApp) ...MainApp IS NULL RIGHT NOW");
                  return;
                }else {
                  if (Utils.isNull(getActivity())){
                    toLog(TAG, "setMissionSetting stop cause getActivity() is Null");
                    call.reject("getActivity() IS NULL RIGHT NOW");
                    return;
                  }else {
                    missionSetting = call.getData();
                    if (Utils.isNull(missionSetting)){
                      toLog(TAG, " setMissionSetting Must provide : missionParamsObj key not found");
                      call.reject("Must provide : missionParamsObj key not found ");
                      return;
                    }else {
                      if ((!missionSetting.has("polygonPoints")) ||
                        (!missionSetting.has("altitude")) ||
                        (!missionSetting.has("exitMissionOnRCLost")) ||
                        (!missionSetting.has("autoGoHomeOnLowBattery")) ||
                        (!missionSetting.has("daylight")) ||
                        (!missionSetting.has("phoneLatitude")) ||
                        (!missionSetting.has("phoneLongitude")) ) {
                        toLog(TAG, " setMissionSetting Must provide : polygonPoints, altitude, exitMissionOnRCLost, autoGoHomeOnLowBattery,phoneLatitude,phoneLongitude,daylight");
                        call.reject("Must provide : polygonPoints, altitude, exitMissionOnRCLost, autoGoHomeOnLowBattery,phoneLatitude,phoneLongitude,daylight");
                        return;
                      }else {
                        String polygonPoints = missionSetting.getString("polygonPoints");
                        Double altitude = missionSetting.getDouble("altitude");
                        Boolean exitMissionOnRCLost = missionSetting.getBoolean("exitMissionOnRCLost");
                        Boolean autoGoHomeOnLowBattery = missionSetting.getBoolean("autoGoHomeOnLowBattery");
                        Integer daylight = missionSetting.getInteger("daylight");
                        Double phoneLatitude = missionSetting.getDouble("phoneLatitude");
                        Double phoneLongitude = missionSetting.getDouble("phoneLongitude");
                        if (Utils.isNullOrEmpty(polygonPoints)){
                          toLog(TAG, " setMissionSetting Must provide : polygonPoints As GeoJSON because isNull or Empty");
                          call.reject("Must provide polygonPoints As GeoJSON because isNull or Empty");
                          return;
                        }else {
                          if (Utils.isNull(exitMissionOnRCLost)){
                            toLog(TAG, " setMissionSetting Must provide : exitMissionOnRCLost is Null");
                            call.reject("exitMissionOnRCLost is Null");
                            return;
                          }else {
                            if (Utils.isNull(altitude)){
                              toLog(TAG, " setMissionSetting Must provide : altitude");
                              call.reject("Must provide altitude");
                              return;
                            }else {

                              if (Utils.isNull(daylight)){
                                toLog(TAG, " setMissionSetting Must provide daylight: 1-NORMAL, 2- NUAGEUSE, 3-ENSOLEILLEE");
                                call.reject("Must provide daylight: 1-NORMAL, 2- NUAGEUSE, 3-ENSOLEILLEE");
                                return;
                              }else {
                                if (Utils.isNull(phoneLatitude)){
                                  toLog(TAG, " setMissionSetting Must provide : phoneLatitude");
                                  call.reject("Must provide phoneLatitude");
                                  return;
                                }else {
                                  if (Utils.isNull(phoneLongitude)){
                                    toLog(TAG, " setMissionSetting Must provide : phoneLongitude");
                                    call.reject("Must provide phoneLongitude");
                                    return;
                                  }else {
                                    if (Utils.isNull(autoGoHomeOnLowBattery)){
                                      toLog(TAG, " setMissionSetting Must provide : autoGoHomeOnLowBattery");
                                      call.reject("Must provide autoGoHomeOnLowBattery");
                                      return;
                                    }else {
                                      JSONObject geoJSON = new JSONObject(polygonPoints);
                                      if (Utils.isNullOrEmpty(geoJSON)){
                                        toLog(TAG, " setMissionSetting Must provide : polygonPoints As GeoJSON ");
                                        call.reject("Must provide polygonPoints As GeoJSON ");
                                        return;
                                      }else {
                                        JSONObject geometry = geoJSON.getJSONObject("geometry");
                                        if (Utils.isNull(geometry)){
                                          toLog(TAG, " setMissionSetting Must provide : polygonPoints As GeoJSON because geometry KEY is null ");
                                          call.reject("Must provide polygonPoints As GeoJSON because geometry KEY is null");
                                          return;
                                        }else {
                                          String errorDet = "Good Polygon like: {\\n\" +\n" +
                                            "                                      \"    \\\"type\\\": \\\"Feature\\\",\\n\" +\n" +
                                            "                                      \"    \\\"properties\\\": {\\\"party\\\": \\\"Republican\\\"},\\n\" +\n" +
                                            "                                      \"    \\\"geometry\\\": {\\n\" +\n" +
                                            "                                      \"        \\\"type\\\": \\\"Polygon\\\",\\n\" +\n" +
                                            "                                      \"        \\\"coordinates\\\": [[\\n\" +\n" +
                                            "                                      \"            [-104.05, 48.99],\\n\" +\n" +
                                            "                                      \"            [-97.22,  48.98],\\n\" +\n" +
                                            "                                      \"            [-96.58,  45.94],\\n\" +\n" +
                                            "                                      \"            [-104.03, 45.94],\\n\" +\n" +
                                            "                                      \"            [-104.05, 48.99]\\n\" +\n" +
                                            "                                      \"        ]]\\n\" +\n" +
                                            "                                      \"    }\\n\" +\n" +
                                            "                                      \"}";
                                          JSONArray arrayMain = geometry.getJSONArray("coordinates");
                                          if (Utils.isNull(arrayMain)){
                                            toLog(TAG, " setMissionSetting Must provide : polygonPoints "+errorDet);
                                            call.reject("Must provide polygonPoints "+errorDet);
                                            return;
                                          }else {
                                            if (arrayMain.length() == 0){
                                              toLog(TAG, " setMissionSetting Must provide : polygonPoints "+errorDet);
                                              call.reject("Must provide polygonPoints "+errorDet);
                                              return;
                                            }else {
                                              JSONArray coordinates = arrayMain.getJSONArray(0);
                                              if (coordinates.length() == 0){
                                                toLog(TAG, " setMissionSetting Must provide : polygonPoints As GeoJSON because coordinates is EMPTY");
                                                call.reject("Must provide polygonPoints As GeoJSON because coordinates is EMPTY");
                                                return;
                                              }else {
                                                if (coordinates.length() < Constants.POLYGON_SIZE){
                                                  toLog(TAG, " setMissionSetting Must provide : polygonPoints must have at least 3 points");
                                                  call.reject("Must provide polygonPoints must have at least 3 points");
                                                  return;
                                                }else {
                                                  CopyOnWriteArrayList<LatLng> points = new CopyOnWriteArrayList<>();
                                                  for(int i = 0; i < coordinates.length(); i++){
                                                    JSONArray item = coordinates.getJSONArray(i);
                                                    double longitude = item.getDouble(0);
                                                    double latitude = item.getDouble(1);
                                                    LatLng myLatLng = new LatLng(latitude,longitude);
                                                    points.add(myLatLng);
                                                  }

                                                  if (points.size() < Constants.POLYGON_SIZE){
                                                    toLog(TAG, " setMissionSetting Must provide :  must have at least 3 points");
                                                    call.reject("Decode GEOJSON failed: must have at least 3 points");
                                                    return;
                                                  }else {
                                                    if (altitude < 0){
                                                      toLog(TAG, " setMissionSetting Bad altitude must be positive");
                                                      call.reject("Bad altitude must be positive");
                                                      return;
                                                    }else {
                                                      if (altitude <Constant.MIN_ALTITUDE_IN_CI){
                                                        toLog(TAG, " setMissionSetting Bad altitude must be 50 meters at least");
                                                        call.reject("Bad altitude must be 50 meters at least");
                                                        return;
                                                      }else {
                                                        if (altitude > Constant.MAX_ALTITUDE_IN_CI){
                                                          toLog(TAG, " setMissionSetting Bad altitude must be 100 meters MAX");
                                                          call.reject("Bad altitude must be 100 meters MAX");
                                                          return;
                                                        }else {
                                                          if (phoneLatitude == Constants.DOUBLE_NULL){
                                                            toLog(TAG, " setMissionSetting Bad phoneLatitude");
                                                            call.reject("Bad phoneLatitude");
                                                            return;
                                                          }else {
                                                            if (phoneLongitude == Constants.DOUBLE_NULL){
                                                              toLog(TAG, " setMissionSetting Bad phoneLongitude");
                                                              call.reject("Bad phoneLongitude");
                                                              return;
                                                            }else {
                                                              if (phoneLatitude.isNaN()){
                                                                toLog(TAG, " setMissionSetting Bad phoneLatitude is NaN");
                                                                call.reject("Bad phoneLatitude is NaN");
                                                                return;
                                                              }else {
                                                                if (phoneLongitude.isNaN()){
                                                                  toLog(TAG, " setMissionSetting Bad phoneLongitude is NaN");
                                                                  call.reject("Bad phoneLongitude is NaN");
                                                                  return;
                                                                }else {
                                                                  // Keep At the End of function
                                                                  call.resolve();
                                                                }
                                                              }
                                                            }
                                                          }
                                                        }
                                                      }
                                                    }
                                                  }
                                                }
                                              }
                                            }
                                          }
                                        }
                                      }
                                    }
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }catch (Exception e){
      Utils.toLog(TAG, "setMissionSetting",null,e);
      call.reject(e.getLocalizedMessage(), e);
    }catch (Error er){
      Utils.toLog(TAG, "setMissionSetting",er,null);
      call.reject(er.getLocalizedMessage());
    }
  }

  @PluginMethod
  public void disconnect(PluginCall call){
    toLog(TAG, "disconnect start");
    try {
      if (Utils.isNull(ctx)){
        toLog(TAG, "disconnect stop cause ctx is Null");
        return;
      }else {
        if (Utils.isNull(call)){
          toLog(TAG, "disconnect stop cause call is Null");
          return;
        }else {
          PermissionManager pm = new PermissionManager(ctx);
          if (!pm.isPermissionsGranted()){
            toLog(TAG, "disconnect stop cause REQUIRE ALL PERMISSIONS");
            call.reject("REQUIRE ALL PERMISSIONS");
            return;
          }else{
            if (!pm.isSDKreadyForMappingProcess()){
              toLog(TAG, "SDK Not Ready for Mapping Process MIN IS API 24 ");
              call.reject("SDK Not Ready for Mapping Process MIN IS API 24");
              return;
            }else {
              DroneManager manager = DroneManager.getInstance();
              if (Utils.isNull(manager)){
                toLog(TAG, "disconnect stop cause manager is Null");
                call.reject("DroneManager is Null");
                return;
              }else {
                if (Utils.isNull(getMainApp())){
                  toLog(TAG, "disconnect stop cause getMainApp() is Null");
                  call.reject("PLEASE SET MainApp BEFORE RUN Apps (setMainApp) ...MainApp IS NULL RIGHT NOW");
                  return;
                }else {
                  // Keep At the End of function
                  callBackIdDisconnect = null;
                  call.save();
                  callBackIdDisconnect = Utils.clone(call.getCallbackId());

                  manager.disconnect(new OnAsyncOperationComplete() {
                    @Override
                    public void onError(@Nullable String errorDetail) {
                      try {
                        PluginCall call = bridge.getSavedCall(callBackIdDisconnect);
                        if (call == null) {
                          toLog(TAG , "disconnect onError call Is Null");
                          return;
                        }else {
                          if (Utils.isNull(errorDetail)){
                            errorDetail = "";
                          }
                          call.reject(errorDetail);
                          call.release(bridge);
                          callBackIdDisconnect = null;
                        }
                      }catch (Exception e){
                        Utils.toLog(TAG, "disconnect",null,e);
                        call.reject(e.getLocalizedMessage(), e);
                      }catch (Error er){
                        Utils.toLog(TAG, "disconnect",er,null);
                        call.reject(er.getLocalizedMessage());
                      }

                    }

                    @Override
                    public void onSucces(@Nullable String succesMsg) {
                      try {
                        PluginCall call = bridge.getSavedCall(callBackIdDisconnect);
                        if (call == null) {
                          toLog(TAG , "disconnect onSucces call Is Null ");
                          return;
                        }else {
                          if (Utils.isNull(succesMsg)){
                            succesMsg = "";
                          }
                          call.resolve();
                          call.release(bridge);
                          callBackIdDisconnect = null;
                        }
                      }catch (Exception e){
                        Utils.toLog(TAG, "disconnect",null,e);
                        call.reject(e.getLocalizedMessage(), e);
                      }catch (Error er){
                        Utils.toLog(TAG, "disconnect",er,null);
                        call.reject(er.getLocalizedMessage());
                      }
                    }
                  });
                  toLog(TAG, "disconnect Success launch");
                }
              }
            }
          }
        }
      }
    }catch (Exception e){
      Utils.toLog(TAG, "disconnect",null,e);
      call.reject(e.getLocalizedMessage(), e);
    }catch (Error er){
      Utils.toLog(TAG, "disconnect",er,null);
      call.reject(er.getLocalizedMessage());
    }
  }


  @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
  public void watchDroneTelemetry(PluginCall call){
    toLog(TAG, "watchDroneTelemetry start");
    try {
      if (Utils.isNull(ctx)){
        toLog(TAG, "watchDroneTelemetry stop cause ctx is Null");
        return;
      }else {
        if (Utils.isNull(call)){
          toLog(TAG, "watchDroneTelemetry stop cause call is Null");
          return;
        }else {
          PermissionManager pm = new PermissionManager(ctx);
          if (!pm.isPermissionsGranted()){
            toLog(TAG, "watchDroneTelemetry stop cause REQUIRE ALL PERMISSIONS");
            call.reject("REQUIRE ALL PERMISSIONS");
            return;
          }else{
            if (!pm.isSDKreadyForMappingProcess()){
              toLog(TAG, "SDK Not Ready for Mapping Process MIN IS API 24 ");
              call.reject("SDK Not Ready for Mapping Process MIN IS API 24");
              return;
            }else {
              DroneManager manager = DroneManager.getInstance();
              if (Utils.isNull(manager)){
                toLog(TAG, "watchDroneTelemetry stop cause manager is Null");
                call.reject("DroneManager is Null");
                return;
              }else {
                if (Utils.isNull(getMainApp())){
                  toLog(TAG, "watchDroneTelemetry stop cause getMainApp() is Null");
                  call.reject("PLEASE SET MainApp BEFORE RUN Apps (setMainApp) ...MainApp IS NULL RIGHT NOW");
                  return;
                }else {
                  // Keep At the End of function
                  callBackIdwatchDrone = null;
                  call.save();
                  callBackIdwatchDrone = Utils.clone(call.getCallbackId());

                  manager.registerToListenDroneState(new OnStateChangeListener() {
                    @Override
                    public void onChange(int uplinkSignalQuality, short satelliteCount, int chargeRemainingInPercent, int chargeRemainingInMAh, float temperature, float altitudeInMeters, float speedInMeterPerSec, float heading, int numberOfDischarges, int lifetimeRemainingInPercent, double longitude, double latitude, float altitude, int droneHeadingInDegrees, @NonNull String droneModelName,float distanceInMeter) {
                      synchronized (JoolMappingPlugin.this){
                        try {
                          PluginCall call = bridge.getSavedCall(callBackIdwatchDrone);
                          if (call == null) {
                            toLog(TAG , "watchDroneTelemetry onChange call Is Null ");
                            return;
                          }else {
                            if (Utils.isNull(droneModelName)){
                              droneModelName = "";
                            }
                            Double mLatitude =latitude;
                            Double mLongitude =longitude;
                            if (mLatitude.isNaN()){
                              mLatitude = Constants.DOUBLE_NULL_2;
                            }
                            if (mLongitude.isNaN()){
                              mLongitude = Constants.DOUBLE_NULL_2;
                            }
                            JSObject ret = new JSObject();
                            ret.put("uplinkSignalQuality", uplinkSignalQuality);
                            ret.put("satelliteCount", satelliteCount);
                            ret.put("chargeRemainingInPercent",chargeRemainingInPercent );
                            ret.put("chargeRemainingInMAh", chargeRemainingInMAh);
                            ret.put("temperature", temperature);
                            ret.put("altitudeInMeters", altitudeInMeters);
                            ret.put("speedInMeterPerSec", speedInMeterPerSec);
                            ret.put("heading", heading);
                            ret.put("numberOfDischarges", numberOfDischarges);
                            ret.put("lifetimeRemainingInPercent", lifetimeRemainingInPercent);
                            ret.put("longitude", mLongitude);
                            ret.put("latitude", mLatitude);
                            ret.put("altitude", altitude);
                            ret.put("droneHeadingInDegrees", droneHeadingInDegrees);
                            ret.put("droneModelName", droneModelName);
                            ret.put("distanceInMeter", distanceInMeter);
                            call.success(ret);
                            // callBackIdwatchDrone =null; require For other callBack
                          }
                        }catch (Exception e){
                          Utils.toLog(TAG, "watchDroneTelemetry",null,e);
                          call.reject(e.getLocalizedMessage(), e);
                        }catch (Error er){
                          Utils.toLog(TAG, "watchDroneTelemetry",er,null);
                          call.reject(er.getLocalizedMessage());
                        }
                      }
                    }

                    @Override
                    public void onLog(String msg) {
                      toLog(TAG, msg);
                    }

                    @Override
                    public void onConnectFailed() {
                      try {
                        PluginCall call = bridge.getSavedCall(callBackIdwatchDrone);
                        if (call == null) {
                          toLog(TAG , "watchDroneTelemetry onConnectFailed Call Is Null ");
                          return;
                        }else {
                          call.resolve();
                          toLog(TAG, "registerToListenDroneState onConnectFailed");
                          // callBackIdwatchDrone =null; require For other callBack
                        }
                      }catch (Exception e){
                        Utils.toLog(TAG, "watchDroneTelemetry",null,e);
                        call.reject(e.getLocalizedMessage(), e);
                      }catch (Error er){
                        Utils.toLog(TAG, "watchDroneTelemetry",er,null);
                        call.reject(er.getLocalizedMessage());
                      }
                    }

                    @Override
                    public void onConnectSucces() {
                      try {
                        PluginCall call = bridge.getSavedCall(callBackIdwatchDrone);
                        if (call == null) {
                          toLog(TAG , "watchDroneTelemetry onConnectSucces Call Is Null ");
                          return;
                        }else {
                          call.resolve();
                          toLog(TAG, "registerToListenDroneState onConnectSucces");
                          // callBackIdwatchDrone =null; require For other callBack
                        }
                      }catch (Exception e){
                        Utils.toLog(TAG, "watchDroneTelemetry",null,e);
                        call.reject(e.getLocalizedMessage(), e);
                      }catch (Error er){
                        Utils.toLog(TAG, "watchDroneTelemetry",er,null);
                        call.reject(er.getLocalizedMessage());
                      }
                    }
                  });
                  toLog(TAG, "registerToListenDroneState Success launch");
                }
              }
            }
          }
        }
      }
    }catch (Exception e){
      Utils.toLog(TAG, "watchDroneTelemetry",null,e);
      call.reject(e.getLocalizedMessage(), e);
    }catch (Error er){
      Utils.toLog(TAG, "watchDroneTelemetry",er,null);
      call.reject(er.getLocalizedMessage());
    }
  }

  @PluginMethod
  public void stopWatchDroneTelemetry(PluginCall call){
    toLog(TAG, "stopWatchDroneTelemetry start");
    try {
      if (Utils.isNull(ctx)){
        toLog(TAG, "stopWatchDroneTelemetry stop cause ctx is Null");
        return;
      }else {
        if (Utils.isNull(call)){
          toLog(TAG, "stopWatchDroneTelemetry stop cause call is Null");
          return;
        }else {
          PermissionManager pm = new PermissionManager(ctx);
          if (!pm.isPermissionsGranted()){
            toLog(TAG, "stopWatchDroneTelemetry stop cause REQUIRE ALL PERMISSIONS");
            call.reject("REQUIRE ALL PERMISSIONS");
            return;
          }else{
            if (!pm.isSDKreadyForMappingProcess()){
              toLog(TAG, "SDK Not Ready for Mapping Process MIN IS API 24 ");
              call.reject("SDK Not Ready for Mapping Process MIN IS API 24");
              return;
            }else {
              DroneManager manager = DroneManager.getInstance();
              if (Utils.isNull(manager)){
                toLog(TAG, "stopWatchDroneTelemetry stop cause manager is Null");
                call.reject("DroneManager is Null");
                return;
              }else {
                if (Utils.isNull(getMainApp())){
                  toLog(TAG, "stopWatchDroneTelemetry stop cause getMainApp() is Null");
                  call.reject("PLEASE SET MainApp BEFORE RUN Apps (setMainApp) ...MainApp IS NULL RIGHT NOW");
                  return;
                }else {
                  // Keep At the End of function
                  callBackIdstopWatchDrone = null;
                  call.save();
                  callBackIdstopWatchDrone = Utils.clone(call.getCallbackId());

                  manager.stopWatchDroneTelemetry(new OnAsyncOperationComplete() {
                    @Override
                    public void onError(@Nullable String errorDetail) {
                      try {
                        PluginCall call = bridge.getSavedCall(callBackIdstopWatchDrone);
                        if (call == null) {
                          toLog(TAG , "stopWatchDroneTelemetry onError Call Is Null ");
                          return;
                        }else {
                          if (Utils.isNull(errorDetail)){
                            errorDetail = "";
                          }
                          call.reject(errorDetail);
                          call.release(bridge);
                          callBackIdstopWatchDrone = null;
                        }
                      }catch (Exception e){
                        Utils.toLog(TAG, "stopWatchDroneTelemetry",null,e);
                        call.reject(e.getLocalizedMessage(), e);
                      }catch (Error er){
                        Utils.toLog(TAG, "stopWatchDroneTelemetry",er,null);
                        call.reject(er.getLocalizedMessage());
                      }

                    }

                    @Override
                    public void onSucces(@Nullable String succesMsg) {
                      try {
                        PluginCall call = bridge.getSavedCall(callBackIdstopWatchDrone);
                        if (call == null) {
                          toLog(TAG , "stopWatchDroneTelemetry onSucces call Is Null ");
                          return;
                        }else {
                          if (Utils.isNull(succesMsg)){
                            succesMsg = "";
                          }
                          call.resolve();
                          call.release(bridge);
                          callBackIdstopWatchDrone = null;
                        }
                      }catch (Exception e){
                        Utils.toLog(TAG, "stopWatchDroneTelemetry",null,e);
                        call.reject(e.getLocalizedMessage(), e);
                      }catch (Error er){
                        Utils.toLog(TAG, "stopWatchDroneTelemetry",er,null);
                        call.reject(er.getLocalizedMessage());
                      }
                    }
                  });
                  toLog(TAG, "stopWatchDroneTelemetry Success launch");
                }
              }
            }
          }
        }
      }
    }catch (Exception e){
      Utils.toLog(TAG, "stopWatchDroneTelemetry",null,e);
      call.reject(e.getLocalizedMessage(), e);
    }catch (Error er){
      Utils.toLog(TAG, "stopWatchDroneTelemetry",er,null);
      call.reject(er.getLocalizedMessage());
    }
  }


  @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
  public void startWaypointMission(PluginCall call){
    toLog(TAG, "startWaypointMission start");
    try {
      // Case NextTime clean Old
      if (Utils.isNull(callBackIdStartMission)){
        // Skip
      }else {
        PluginCall notReleasedCall = bridge.getSavedCall(callBackIdStartMission);
        if (notReleasedCall == null) {
          // Skip
        }else {
          notReleasedCall.release(bridge);
          callBackIdStartMission = null;
        }
      }
      if (Utils.isNull(ctx)){
        toLog(TAG, "startWaypointMission stop cause ctx is Null");
        return;
      }else {
        if (Utils.isNull(call)){
          toLog(TAG, "startWaypointMission stop cause call is Null");
          return;
        }else {
          PermissionManager pm = new PermissionManager(ctx);
          if (!pm.isPermissionsGranted()){
            toLog(TAG, "startWaypointMission stop cause REQUIRE ALL PERMISSIONS");
            playError(call,"REQUIRE ALL PERMISSIONS");
            return;
          }else{
            if (!pm.isSDKreadyForMappingProcess()){
              toLog(TAG, "SDK Not Ready for Mapping Process MIN IS API 24 ");
              playError(call,"SDK Not Ready for Mapping Process MIN IS API 24");
              return;
            }else {
              DroneManager manager = DroneManager.getInstance();
              if (Utils.isNull(manager)){
                toLog(TAG, "startWaypointMission stop cause manager is Null");
                playError(call,"DroneManager is Null");
                return;
              }else {
                if (Utils.isNull(getMainApp())){
                  toLog(TAG, "startWaypointMission stop cause getMainApp() is Null");
                  playError(call,"PLEASE SET MainApp BEFORE RUN Apps (setMainApp) ...MainApp IS NULL RIGHT NOW");
                  return;
                }else {
                  if (Utils.isNull(getActivity())){
                    toLog(TAG, "startWaypointMission stop cause getActivity() is Null");
                    playError(call,"getActivity() IS NULL RIGHT NOW");
                    return;
                  }else {
                    // Keep At the End of function
                    callBackIdStartMission = null;
                    call.save();
                    callBackIdStartMission = Utils.clone(call.getCallbackId());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                      manager.startWaypointMission(getActivity(),new OnMissionRunningListener() {
                        @Override
                        public void onError(@Nullable String errorDetail) {
                          toLog(TAG , "startWaypointMission onError Start");
                          synchronized (JoolMappingPlugin.this){
                            try {
                              PluginCall call = bridge.getSavedCall(callBackIdStartMission);
                              if (call == null) {
                                toLog(TAG , "startWaypointMission onError call Is Null ");
                                return;
                              }else {
                                if (Utils.isNull(errorDetail)){
                                  errorDetail = "";
                                }
                                playError(call,errorDetail);
                                call.release(bridge);
                                callBackIdStartMission =null;
                                missionSetting = null;
                              }
                            }catch (Exception e){
                              Utils.toLog(TAG, "startWaypointMission Exception",null,e);
                              playError(call,e.getLocalizedMessage());
                            }catch (Error er){
                              Utils.toLog(TAG, "startWaypointMission Error",er,null);
                              playError(call,er.getLocalizedMessage());
                            }
                          }

                        }

                        @Override
                        public void onCalibrationRequire(@Nullable String detail) {
                          synchronized (JoolMappingPlugin.this){
                            try {
                              toLog(TAG , "startWaypointMission onCalibrationRequire start");
                              PluginCall call = bridge.getSavedCall(callBackIdStartMission);
                              if (call == null) {
                                toLog(TAG , "startWaypointMission onCalibrationRequire call Is Null ");
                                return;
                              }else {
                                if (Utils.isNull(detail)){
                                  detail = "";
                                }
                                playError(call,detail);
                                call.release(bridge);
                                callBackIdStartMission =null;
                                missionSetting = null;
                              }
                            }catch (Exception e){
                              Utils.toLog(TAG, "startWaypointMission Exception",null,e);
                              playError(call,e.getLocalizedMessage());
                            }catch (Error er){
                              Utils.toLog(TAG, "startWaypointMission Error",er,null);
                              playError(call,er.getLocalizedMessage());
                            }
                          }
                        }

                        @Override
                        public void onStartSucces() {
                          synchronized (JoolMappingPlugin.this){
                            try {
                              toLog(TAG , "startWaypointMission onStartSucces start");
                              PluginCall call = bridge.getSavedCall(callBackIdStartMission);
                              if (call == null) {
                                toLog(TAG , "startWaypointMission onStartSucces onCalibrationRequire ");
                                return;
                              }else {
                                JSObject ret = new JSObject();
                                ret.put(startSuccesName, true);
                                ret.put(finishSuccesName, null);
                                ret.put(stepRemainName, null);
                                ret.put(gridProgressingName, null);
                                ret.put(batteryName, null);
                                ret.put(imageCountName, null);
                                ret.put(speedName, null);
                                ret.put(surveyPointsName, null);
                                ret.put(errorName, null);
                                ret.put(lowBattery, null);
                                ret.put(showLoader, null);
                                ret.put(hideLoader, null);
                                call.success(ret);
                              }
                            }catch (Exception e){
                              Utils.toLog(TAG, "startWaypointMission onStartSucces Exception ",null,e);
                              playError(call,e.getLocalizedMessage());
                            }catch (Error er){
                              Utils.toLog(TAG, "startWaypointMission onStartSucces Error",er,null);
                              playError(call,er.getLocalizedMessage());
                            }
                          }
                        }

                        @Override
                        public void onNotifyStepRemain(int stepRemain) {
                          synchronized (JoolMappingPlugin.this){
                            try {
                              toLog(TAG , "startWaypointMission onNotifyStepRemain start");
                              PluginCall call = bridge.getSavedCall(callBackIdStartMission);
                              if (call == null) {
                                toLog(TAG , "startWaypointMission onNotifyStepRemain call Is Null ");
                                return;
                              }else {
                                JSObject ret = new JSObject();
                                ret.put(startSuccesName, null);
                                ret.put(finishSuccesName, null);
                                ret.put(stepRemainName, stepRemain);
                                ret.put(gridProgressingName, null);
                                ret.put(batteryName, null);
                                ret.put(imageCountName, null);
                                ret.put(speedName, null);
                                ret.put(surveyPointsName, null);
                                ret.put(errorName, null);
                                ret.put(lowBattery, null);
                                ret.put(showLoader, null);
                                ret.put(hideLoader, null);
                                call.success(ret);
                              }
                            }catch (Exception e){
                              Utils.toLog(TAG, "startWaypointMission onNotifyStepRemain Exception ",null,e);
                              playError(call,e.getLocalizedMessage());
                            }catch (Error er){
                              Utils.toLog(TAG, "startWaypointMission onNotifyStepRemain Error",er,null);
                              playError(call,er.getLocalizedMessage());
                            }
                          }
                        }

                        @Override
                        public void onSucces() {
                          synchronized (JoolMappingPlugin.this){
                            try {
                              toLog(TAG , "startWaypointMission onSucces start");
                              PluginCall call = bridge.getSavedCall(callBackIdStartMission);
                              if (call == null) {
                                toLog(TAG , "startWaypointMission onSucces call Is Null ");
                                return;
                              }else {
                                JSObject ret = new JSObject();
                                ret.put(startSuccesName, null);
                                ret.put(finishSuccesName, true);
                                ret.put(stepRemainName, null);
                                ret.put(gridProgressingName, null);
                                ret.put(batteryName, null);
                                ret.put(imageCountName, null);
                                ret.put(speedName, null);
                                ret.put(surveyPointsName, null);
                                ret.put(errorName, null);
                                ret.put(lowBattery, null);
                                ret.put(showLoader, null);
                                ret.put(hideLoader, null);
                                call.success(ret);
                              }
                            }catch (Exception e){
                              Utils.toLog(TAG, "startWaypointMission onSucces Exception ",null,e);
                              playError(call,e.getLocalizedMessage());
                            }catch (Error er){
                              Utils.toLog(TAG, "startWaypointMission onSucces Error",er,null);
                              playError(call,er.getLocalizedMessage());
                            }
                          }
                        }

                        @Override
                        public void onGridProgressing(int progress) {
                          synchronized (JoolMappingPlugin.this){
                            try {
                              toLog(TAG , "startWaypointMission onGridProgressing start");
                              PluginCall call = bridge.getSavedCall(callBackIdStartMission);
                              if (call == null) {
                                toLog(TAG , "startWaypointMission onGridProgressing call Is Null ");
                                return;
                              }else {
                                JSObject ret = new JSObject();
                                ret.put(startSuccesName, null);
                                ret.put(finishSuccesName, null);
                                ret.put(stepRemainName, null);
                                ret.put(gridProgressingName, progress);
                                ret.put(batteryName, null);
                                ret.put(imageCountName, null);
                                ret.put(speedName, null);
                                ret.put(surveyPointsName, null);
                                ret.put(errorName, null);
                                ret.put(lowBattery, null);
                                ret.put(showLoader, null);
                                ret.put(hideLoader, null);
                                call.success(ret);
                              }
                            }catch (Exception e){
                              Utils.toLog(TAG, "startWaypointMission onGridProgressing Exception ",null,e);
                              playError(call,e.getLocalizedMessage());
                            }catch (Error er){
                              Utils.toLog(TAG, "startWaypointMission onGridProgressing Error",er,null);
                              playError(call,er.getLocalizedMessage());
                            }
                          }
                        }

                        @Override
                        public void onNotifyState(int battery, int imageCount, float speed, CopyOnWriteArrayList<LatLng> flyPoints) {
                          synchronized (JoolMappingPlugin.this){
                            toLog(TAG , "startWaypointMission onNotifyState start");
                            try {
                              PluginCall call = bridge.getSavedCall(callBackIdStartMission);
                              if (call == null) {
                                toLog(TAG , "startWaypointMission onNotifyState call Is Null ");
                                return;
                              }else {
                                JSONArray array = new JSONArray();
                                for (LatLng item :flyPoints) {
                                  JSONObject o = new JSONObject();
                                  o.put("latitude",item.latitude);
                                  o.put("longitude",item.longitude);
                                  array.put(o);
                                }
                                JSObject ret = new JSObject();
                                ret.put(startSuccesName, null);
                                ret.put(finishSuccesName, null);
                                ret.put(stepRemainName, null);
                                ret.put(gridProgressingName, null);
                                ret.put(batteryName, battery);
                                ret.put(imageCountName, imageCount);
                                ret.put(speedName, speed);
                                ret.put(surveyPointsName, array.toString());
                                ret.put(errorName, null);
                                ret.put(lowBattery, null);
                                ret.put(showLoader, null);
                                ret.put(hideLoader, null);
                                call.success(ret);
                              }
                            }catch (Exception e){
                              Utils.toLog(TAG, "startWaypointMission",null,e);
                              playError(call,e.getLocalizedMessage());
                            }catch (Error er){
                              Utils.toLog(TAG, "startWaypointMission",er,null);
                              playError(call,er.getLocalizedMessage());
                            }
                          }
                        }

                        @Override
                        public void mustGoHomeError() {
                          toLog(TAG , "startWaypointMission mustGoHome start");
                          synchronized (JoolMappingPlugin.this){
                            try {
                              Runnable notifyAction = ()->{
                                try {
                                  PluginCall call = bridge.getSavedCall(callBackIdStartMission);
                                  if (call == null) {
                                    toLog(TAG , "startWaypointMission mustGoHomeError call Is Null ");
                                    return;
                                  }else {
                                    playError(call,"DRONE GOING HOME cause Error");
                                    call.release(bridge);
                                    callBackIdStartMission =null;
                                    missionSetting = null;
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "startWaypointMission notifyAction Exception ",null,e);
                                  playError(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "startWaypointMission notifyAction Error",er,null);
                                  playError(call,er.getLocalizedMessage());
                                }
                              };
                              goHome(manager, new OnAsyncOperationComplete() {
                                @Override
                                public void onError(@Nullable String errorDetail) {
                                  toLog(TAG , "startWaypointMission goHome RESULT onError Normal if Force GoBack ");
                                  notifyAction.run();
                                }

                                @Override
                                public void onSucces(@Nullable String succesMsg) {
                                  toLog(TAG , "startWaypointMission goHome RESULT onSucces ");
                                  notifyAction.run();
                                }
                              });
                            }catch (Exception e){
                              Utils.toLog(TAG, "startWaypointMission mustGoHome Exception ",null,e);
                              playError(call,e.getLocalizedMessage());
                            }catch (Error er){
                              Utils.toLog(TAG, "startWaypointMission mustGoHome Error",er,null);
                              playError(call,er.getLocalizedMessage());
                            }
                          }
                        }

                        @Override
                        public void onEnableLoadingForNextMission() {
                          synchronized (JoolMappingPlugin.this){
                            toLog(TAG , "startWaypointMission onEnableLoadingForNextMission start");
                            try {
                              PluginCall call = bridge.getSavedCall(callBackIdStartMission);
                              if (call == null) {
                                toLog(TAG , "startWaypointMission onEnableLoadingForNextMission call Is Null ");
                                return;
                              }else {
                                notifyReady(false);
                                playLoading(call,true);
                              }
                            }catch (Exception e){
                              Utils.toLog(TAG, "startWaypointMission onEnableLoadingForNextMission",null,e);
                              playError(call,e.getLocalizedMessage());
                            }catch (Error er){
                              Utils.toLog(TAG, "startWaypointMission onEnableLoadingForNextMission",er,null);
                              playError(call,er.getLocalizedMessage());
                            }
                          }
                        }

                        @Override
                        public void onDisableLoadingForNextMission() {
                          synchronized (JoolMappingPlugin.this){
                            toLog(TAG , "startWaypointMission onDisableLoadingForNextMission start");
                            try {
                              PluginCall call = bridge.getSavedCall(callBackIdStartMission);
                              if (call == null) {
                                toLog(TAG , "startWaypointMission onDisableLoadingForNextMission call Is Null ");
                                return;
                              }else {
                                notifyReady(true);
                                playLoading(call,false);
                              }
                            }catch (Exception e){
                              Utils.toLog(TAG, "startWaypointMission onDisableLoadingForNextMission",null,e);
                              playError(call,e.getLocalizedMessage());
                            }catch (Error er){
                              Utils.toLog(TAG, "startWaypointMission onDisableLoadingForNextMission",er,null);
                              playError(call,er.getLocalizedMessage());
                            }
                          }
                        }

                        @Override
                        public void mustGoHomeLowBattery() {
                          toLog(TAG , "startWaypointMission mustGoHomeLowBattery start");
                          synchronized (JoolMappingPlugin.this){
                            try {
                              Runnable notifyAction = ()->{
                                try {
                                  PluginCall call = bridge.getSavedCall(callBackIdStartMission);
                                  if (call == null) {
                                    toLog(TAG , "startWaypointMission notifyAction call Is Null ");
                                    return;
                                  }else {
                                    playLowBattery(call,"DRONE GOING HOME cause LowBattery");
                                    call.release(bridge);
                                    callBackIdStartMission =null;
                                    missionSetting = null;
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "startWaypointMission notifyAction Exception ",null,e);
                                  playError(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "startWaypointMission notifyAction Error",er,null);
                                  playError(call,er.getLocalizedMessage());
                                }
                              };
                              goHome(manager, new OnAsyncOperationComplete() {
                                @Override
                                public void onError(@Nullable String errorDetail) {
                                  toLog(TAG , "startWaypointMission goHome RESULT onError Normal if Force GoBack ");
                                  notifyAction.run();
                                }

                                @Override
                                public void onSucces(@Nullable String succesMsg) {
                                  toLog(TAG , "startWaypointMission goHome RESULT onSucces ");
                                  notifyAction.run();
                                }
                              });
                            }catch (Exception e){
                              Utils.toLog(TAG, "startWaypointMission mustGoHome Exception ",null,e);
                              playError(call,e.getLocalizedMessage());
                            }catch (Error er){
                              Utils.toLog(TAG, "startWaypointMission mustGoHome Error",er,null);
                              playError(call,er.getLocalizedMessage());
                            }
                          }
                        }
                      });
                    }
                  }
                }
              }
            }
          }
        }
      }
    }catch (Exception e){
      Utils.toLog(TAG, "startWaypointMission",null,e);
      playError(call,e.getLocalizedMessage());
    }catch (Error er){
      Utils.toLog(TAG, "startWaypointMission",er,null);
      playError(call,er.getLocalizedMessage());
    }
  }

  private void notifyReady(boolean isReady) {
    try {
      if (Utils.isNull(landedDetector)){
        return;
      }else {
        CopyOnWriteArrayList<Boolean> list = new CopyOnWriteArrayList<>();
        list.add(isReady);
        landedDetector.onEvent(list);;
      }
    }catch (Exception e){
      Utils.toLog(TAG, "notifyReady",null,e);
    }catch (Error er){
      Utils.toLog(TAG, "notifyReady",er,null);
    }
  }


  @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
  public void getMissionInfo(PluginCall call){
    toLog(TAG, "getMissionInfo start");
    try {
      // Case NextTime clean Old
      if (Utils.isNull(callBackIdMissionInfo)){
        // Skip
      }else {
        PluginCall notReleasedCall = bridge.getSavedCall(callBackIdMissionInfo);
        if (notReleasedCall == null) {
          // Skip
        }else {
          notReleasedCall.release(bridge);
          callBackIdMissionInfo = null;
        }
      }
      if (Utils.isNull(ctx)){
        toLog(TAG, "getMissionInfo stop cause ctx is Null");
        return;
      }else {
        if (Utils.isNull(call)){
          toLog(TAG, "getMissionInfo stop cause call is Null");
          return;
        }else {
          PermissionManager pm = new PermissionManager(ctx);
          if (!pm.isPermissionsGranted()){
            toLog(TAG, "getMissionInfo stop cause REQUIRE ALL PERMISSIONS");
            playErrorShort(call,"REQUIRE ALL PERMISSIONS");
            return;
          }else{
            if (!pm.isSDKreadyForMappingProcess()){
              toLog(TAG, "SDK Not Ready for Mapping Process MIN IS API 24 ");
              playErrorShort(call,"SDK Not Ready for Mapping Process MIN IS API 24");
              return;
            }else {
              DroneManager manager = DroneManager.getInstance();
              if (Utils.isNull(manager)){
                toLog(TAG, "getMissionInfo stop cause manager is Null");
                playErrorShort(call,"DroneManager is Null");
                return;
              }else {
                if (Utils.isNull(getMainApp())){
                  toLog(TAG, "getMissionInfo stop cause getMainApp() is Null");
                  playErrorShort(call,"PLEASE SET MainApp BEFORE RUN Apps (setMainApp) ...MainApp IS NULL RIGHT NOW");
                  return;
                }else {
                  if (Utils.isNull(getActivity())){
                    toLog(TAG, "getMissionInfo stop cause getActivity() is Null");
                    playErrorShort(call,"getActivity() IS NULL RIGHT NOW");
                    return;
                  }else {
                    if (Utils.isNull(missionSetting)){
                      toLog(TAG, " getMissionInfo missionSetting is Null please call setMissionSetting first");
                      playErrorShort(call," missionSetting is Null please call setMissionSetting first");
                      return;
                    }else {
                      JSObject missionParamsObj = missionSetting;
                      if (Utils.isNull(missionParamsObj)){
                        toLog(TAG, " getMissionInfo Must provide : missionParamsObj key not found");
                        playErrorShort(call,"Must provide : missionParamsObj key not found ");
                        return;
                      }else {
                        if ((!missionParamsObj.has("polygonPoints")) ||
                          (!missionParamsObj.has("altitude")) ||
                          (!missionParamsObj.has("exitMissionOnRCLost")) ||
                          (!missionParamsObj.has("autoGoHomeOnLowBattery")) ||
                          (!missionParamsObj.has("daylight")) ||
                          (!missionParamsObj.has("phoneLatitude")) ||
                          (!missionParamsObj.has("phoneLongitude")) ) {
                          toLog(TAG, " getMissionInfo Must provide : polygonPoints, altitude, exitMissionOnRCLost, autoGoHomeOnLowBattery,phoneLatitude,phoneLongitude,daylight");
                          playErrorShort(call,"Must provide : polygonPoints, altitude, exitMissionOnRCLost, autoGoHomeOnLowBattery,phoneLatitude,phoneLongitude,daylight");
                          return;
                        }else {
                          String polygonPoints = missionParamsObj.getString("polygonPoints");
                          Double altitude = missionParamsObj.getDouble("altitude");
                          Boolean exitMissionOnRCLost = missionParamsObj.getBoolean("exitMissionOnRCLost");
                          Boolean autoGoHomeOnLowBattery = missionParamsObj.getBoolean("autoGoHomeOnLowBattery");
                          Integer daylight = missionParamsObj.getInteger("daylight");
                          Double phoneLatitude = missionParamsObj.getDouble("phoneLatitude");
                          Double phoneLongitude = missionParamsObj.getDouble("phoneLongitude");
                          if (Utils.isNullOrEmpty(polygonPoints)){
                            toLog(TAG, " getMissionInfo Must provide : polygonPoints As GeoJSON because isNull or Empty");
                            playErrorShort(call,"Must provide polygonPoints As GeoJSON because isNull or Empty");
                            return;
                          }else {
                            if (Utils.isNull(exitMissionOnRCLost)){
                              toLog(TAG, " getMissionInfo Must provide : exitMissionOnRCLost is Null");
                              playErrorShort(call,"exitMissionOnRCLost is Null");
                              return;
                            }else {
                              if (Utils.isNull(altitude)){
                                toLog(TAG, " getMissionInfo Must provide : altitude");
                                playErrorShort(call,"Must provide altitude");
                                return;
                              }else {
                                if (Utils.isNull(daylight)){
                                  toLog(TAG, " getMissionInfo Must provide daylight: 1-NORMAL, 2- NUAGEUSE, 3-ENSOLEILLEE");
                                  playErrorShort(call,"Must provide daylight: 1-NORMAL, 2- NUAGEUSE, 3-ENSOLEILLEE");
                                  return;
                                }else {
                                  if (Utils.isNull(phoneLatitude)){
                                    toLog(TAG, " getMissionInfo Must provide : phoneLatitude");
                                    playErrorShort(call,"Must provide phoneLatitude");
                                    return;
                                  }else {
                                    if (Utils.isNull(phoneLongitude)){
                                      toLog(TAG, " getMissionInfo Must provide : phoneLongitude");
                                      playErrorShort(call,"Must provide phoneLongitude");
                                      return;
                                    }else {
                                      if (Utils.isNull(autoGoHomeOnLowBattery)){
                                        toLog(TAG, " getMissionInfo Must provide : autoGoHomeOnLowBattery");
                                        playErrorShort(call,"Must provide autoGoHomeOnLowBattery");
                                        return;
                                      }else {
                                        JSONObject geoJSON = new JSONObject(polygonPoints);
                                        if (Utils.isNullOrEmpty(geoJSON)){
                                          toLog(TAG, " getMissionInfo Must provide : polygonPoints As GeoJSON ");
                                          playErrorShort(call,"Must provide polygonPoints As GeoJSON ");
                                          return;
                                        }else {
                                          JSONObject geometry = geoJSON.getJSONObject("geometry");
                                          if (Utils.isNull(geometry)){
                                            toLog(TAG, " getMissionInfo Must provide : polygonPoints As GeoJSON because geometry KEY is null ");
                                            playErrorShort(call,"Must provide polygonPoints As GeoJSON because geometry KEY is null");
                                            return;
                                          }else {
                                            String errorDet = "Good Polygon like: {\\n\" +\n" +
                                              "                                      \"    \\\"type\\\": \\\"Feature\\\",\\n\" +\n" +
                                              "                                      \"    \\\"properties\\\": {\\\"party\\\": \\\"Republican\\\"},\\n\" +\n" +
                                              "                                      \"    \\\"geometry\\\": {\\n\" +\n" +
                                              "                                      \"        \\\"type\\\": \\\"Polygon\\\",\\n\" +\n" +
                                              "                                      \"        \\\"coordinates\\\": [[\\n\" +\n" +
                                              "                                      \"            [-104.05, 48.99],\\n\" +\n" +
                                              "                                      \"            [-97.22,  48.98],\\n\" +\n" +
                                              "                                      \"            [-96.58,  45.94],\\n\" +\n" +
                                              "                                      \"            [-104.03, 45.94],\\n\" +\n" +
                                              "                                      \"            [-104.05, 48.99]\\n\" +\n" +
                                              "                                      \"        ]]\\n\" +\n" +
                                              "                                      \"    }\\n\" +\n" +
                                              "                                      \"}";
                                            JSONArray arrayMain = geometry.getJSONArray("coordinates");
                                            if (Utils.isNull(arrayMain)){
                                              toLog(TAG, " getMissionInfo Must provide : polygonPoints "+errorDet);
                                              playErrorShort(call,"Must provide polygonPoints "+errorDet);
                                              return;
                                            }else {
                                              if (arrayMain.length() == 0){
                                                toLog(TAG, " getMissionInfo Must provide : polygonPoints "+errorDet);
                                                playErrorShort(call,"Must provide polygonPoints "+errorDet);
                                                return;
                                              }else {
                                                JSONArray coordinates = arrayMain.getJSONArray(0);
                                                if (coordinates.length() == 0){
                                                  toLog(TAG, " getMissionInfo Must provide : polygonPoints As GeoJSON because coordinates is EMPTY");
                                                  playErrorShort(call,"Must provide polygonPoints As GeoJSON because coordinates is EMPTY");
                                                  return;
                                                }else {
                                                  if (coordinates.length() < Constants.POLYGON_SIZE){
                                                    toLog(TAG, " getMissionInfo Must provide : polygonPoints must have at least 3 points");
                                                    playErrorShort(call,"Must provide polygonPoints must have at least 3 points");
                                                    return;
                                                  }else {
                                                    CopyOnWriteArrayList<LatLng> points = new CopyOnWriteArrayList<>();
                                                    for(int i = 0; i < coordinates.length(); i++){
                                                      JSONArray item = coordinates.getJSONArray(i);
                                                      double longitude = item.getDouble(0);
                                                      double latitude = item.getDouble(1);
                                                      LatLng myLatLng = new LatLng(latitude,longitude);
                                                      points.add(myLatLng);
                                                    }

                                                    if (points.size() < Constants.POLYGON_SIZE){
                                                      toLog(TAG, " getMissionInfo Must provide :  must have at least 3 points");
                                                      playErrorShort(call,"Decode GEOJSON failed: must have at least 3 points");
                                                      return;
                                                    }else {
                                                      if (altitude < 0){
                                                        toLog(TAG, " getMissionInfo Bad altitude must be positive");
                                                        playErrorShort(call,"Bad altitude must be positive");
                                                        return;
                                                      }else {
                                                        if (altitude <Constant.MIN_ALTITUDE_IN_CI){
                                                          toLog(TAG, " getMissionInfo Bad altitude must be 50 meters at least");
                                                          playErrorShort(call,"Bad altitude must be 50 meters at least");
                                                          return;
                                                        }else {
                                                          if (altitude > Constant.MAX_ALTITUDE_IN_CI){
                                                            toLog(TAG, " getMissionInfo Bad altitude must be 100 meters MAX");
                                                            playErrorShort(call,"Bad altitude must be 100 meters MAX");
                                                            return;
                                                          }else {
                                                            if (phoneLatitude == Constants.DOUBLE_NULL){
                                                              toLog(TAG, " getMissionInfo Bad phoneLatitude");
                                                              playErrorShort(call,"Bad phoneLatitude");
                                                              return;
                                                            }else {
                                                              if (phoneLongitude == Constants.DOUBLE_NULL){
                                                                toLog(TAG, " getMissionInfo Bad phoneLongitude");
                                                                playErrorShort(call,"Bad phoneLongitude");
                                                                return;
                                                              }else {
                                                                if (phoneLatitude.isNaN()){
                                                                  toLog(TAG, " getMissionInfo Bad phoneLatitude is NaN");
                                                                  playErrorShort(call,"Bad phoneLatitude is NaN");
                                                                  return;
                                                                }else {
                                                                  if (phoneLongitude.isNaN()){
                                                                    toLog(TAG, " getMissionInfo Bad phoneLongitude is NaN");
                                                                    playErrorShort(call,"Bad phoneLongitude is NaN");
                                                                    return;
                                                                  }else {
                                                                    // Keep At the End of function
                                                                    callBackIdMissionInfo = null;
                                                                    call.save();
                                                                    callBackIdMissionInfo = Utils.clone(call.getCallbackId());

                                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                                      manager.getMissionInfo(getActivity(), points, Float.parseFloat(altitude + ""), exitMissionOnRCLost, autoGoHomeOnLowBattery, phoneLatitude, phoneLongitude, daylight, new OnProcessingCompleteListener() {
                                                                        @Override
                                                                        public void onError(@Nullable String errorDetail) {
                                                                          synchronized (JoolMappingPlugin.this){
                                                                            try {
                                                                              PluginCall call = bridge.getSavedCall(callBackIdMissionInfo);
                                                                              if (call == null) {
                                                                                toLog(TAG , "getMissionInfo onError call Is Null ");
                                                                                return;
                                                                              }else {
                                                                                if (Utils.isNull(errorDetail)){
                                                                                  errorDetail = "";
                                                                                }
                                                                                playErrorShort(call,errorDetail);
                                                                                call.release(bridge);
                                                                                callBackIdMissionInfo =null;
                                                                              }
                                                                            }catch (Exception e){
                                                                              Utils.toLog(TAG, "getMissionInfo Exception",null,e);
                                                                              playErrorShort(call,e.getLocalizedMessage());
                                                                            }catch (Error er){
                                                                              Utils.toLog(TAG, "getMissionInfo Error",er,null);
                                                                              playErrorShort(call,er.getLocalizedMessage());
                                                                            }
                                                                          }
                                                                        }

                                                                        @Override
                                                                        public void onGridProgressing(int progress) {
                                                                          synchronized (JoolMappingPlugin.this){
                                                                            try {
                                                                              toLog(TAG , "getMissionInfo onGridProgressing start");
                                                                              PluginCall call = bridge.getSavedCall(callBackIdMissionInfo);
                                                                              if (call == null) {
                                                                                toLog(TAG , "getMissionInfo onGridProgressing call Is Null ");
                                                                                return;
                                                                              }else {
                                                                                JSObject ret = new JSObject();
                                                                                ret.put(gridProgressingName, progress);
                                                                                call.success(ret);
                                                                              }
                                                                            }catch (Exception e){
                                                                              Utils.toLog(TAG, "getMissionInfo onGridProgressing Exception ",null,e);
                                                                              playErrorShort(call,e.getLocalizedMessage());
                                                                            }catch (Error er){
                                                                              Utils.toLog(TAG, "getMissionInfo onGridProgressing Error",er,null);
                                                                              playErrorShort(call,er.getLocalizedMessage());
                                                                            }
                                                                          }
                                                                        }

                                                                        @Override
                                                                        public void onSucces(double flyTime, int imageCount, int battery, float speed, CopyOnWriteArrayList<LatLng> flyPoints, @NonNull MissionSetting missionSetting, @NonNull MissionStep steps) {
                                                                          synchronized (JoolMappingPlugin.this){
                                                                            try {
                                                                              toLog(TAG , "getMissionInfo onSucces start");
                                                                              PluginCall call = bridge.getSavedCall(callBackIdMissionInfo);
                                                                              if (call == null) {
                                                                                toLog(TAG , "getMissionInfo onSucces call Is Null ");
                                                                                return;
                                                                              }else {
                                                                                JSObject ret = new JSObject();
                                                                                ret.put(flyTimeName, flyTime);
                                                                                ret.put(batteryName, battery);
                                                                                ret.put(imageCountName, imageCount);
                                                                                ret.put(speedName, speed);
                                                                                ret.put(surveyPointsName, flyPoints.toString());
                                                                                call.success(ret);
                                                                              }
                                                                            }catch (Exception e){
                                                                              Utils.toLog(TAG, "getMissionInfo onSucces Exception ",null,e);
                                                                              playErrorShort(call,e.getLocalizedMessage());
                                                                            }catch (Error er){
                                                                              Utils.toLog(TAG, "getMissionInfo onSucces Error",er,null);
                                                                              playErrorShort(call,er.getLocalizedMessage());
                                                                            }
                                                                          }
                                                                        }
                                                                      });
                                                                    }
                                                                  }
                                                                }
                                                              }
                                                            }
                                                          }
                                                        }
                                                      }
                                                    }
                                                  }
                                                }
                                              }
                                            }
                                          }
                                        }
                                      }
                                    }
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }catch (Exception e){
      Utils.toLog(TAG, "getMissionInfo",null,e);
      playErrorShort(call,e.getLocalizedMessage());
    }catch (Error er){
      Utils.toLog(TAG, "getMissionInfo",er,null);
      playErrorShort(call,er.getLocalizedMessage());
    }
  }

  @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
  public void performChecklist(PluginCall call){
    toLog(TAG, "performCheckList start");
    try {
      // Case NextTime clean Old
      if (Utils.isNull(callBackIdCheckList)){
        // Skip
      }else {
        PluginCall notReleasedCall = bridge.getSavedCall(callBackIdCheckList);
        if (notReleasedCall == null) {
          // Skip
        }else {
          notReleasedCall.release(bridge);
          callBackIdCheckList = null;
        }
      }
      if (Utils.isNull(ctx)){
        toLog(TAG, "performCheckList stop cause ctx is Null");
        return;
      }else {
        if (Utils.isNull(call)){
          toLog(TAG, "performCheckList stop cause call is Null");
          return;
        }else {
          PermissionManager pm = new PermissionManager(ctx);
          if (!pm.isPermissionsGranted()){
            toLog(TAG, "performCheckList stop cause REQUIRE ALL PERMISSIONS");
            playErrorShort(call,"REQUIRE ALL PERMISSIONS");
            return;
          }else{
            if (!pm.isSDKreadyForMappingProcess()){
              toLog(TAG, "SDK Not Ready for Mapping Process MIN IS API 24 ");
              playErrorShort(call,"SDK Not Ready for Mapping Process MIN IS API 24");
              return;
            }else {
              DroneManager manager = DroneManager.getInstance();
              if (Utils.isNull(manager)){
                toLog(TAG, "performCheckList stop cause manager is Null");
                playErrorShort(call,"DroneManager is Null");
                return;
              }else {
                if (Utils.isNull(getMainApp())){
                  toLog(TAG, "performCheckList stop cause getMainApp() is Null");
                  playErrorShort(call,"PLEASE SET MainApp BEFORE RUN Apps (setMainApp) ...MainApp IS NULL RIGHT NOW");
                  return;
                }else {
                  if (Utils.isNull(getActivity())){
                    toLog(TAG, "performCheckList stop cause getActivity() is Null");
                    playErrorShort(call,"getActivity() IS NULL RIGHT NOW");
                    return;
                  }else {
                    JSObject data = call.getData();
                    if (Utils.isNull(data)){
                      toLog(TAG, " performCheckList Must provide : data key not found");
                      playErrorShort(call,"Must provide : data key not found ");
                      return;
                    }else {
                      if (!data.has("caseContinuMission")) {
                        toLog(TAG, " performCheckList Must provide : caseContinuMission");
                        playErrorShort(call,"Must provide : caseContinuMission");
                        return;
                      }else {
                        Boolean caseContinuMission = data.getBoolean("caseContinuMission");
                        if(Utils.isNull(caseContinuMission)){
                          toLog(TAG, " performCheckList Must provide : caseContinuMission");
                          playErrorShort(call,"Must provide caseContinuMission");
                          return;
                        }else {
                          // Keep At the End of function
                          callBackIdCheckList = null;
                          call.save();
                          callBackIdCheckList = Utils.clone(call.getCallbackId());

                          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            manager.performCheckList(caseContinuMission, getActivity(), new OnCheckListChangeListener() {
                              @Override
                              public void onError(@Nullable String errorDetail) {
                                synchronized (JoolMappingPlugin.this){
                                  try {
                                    PluginCall call = bridge.getSavedCall(callBackIdCheckList);
                                    if (call == null) {
                                      toLog(TAG , "performCheckList onError call Is Null ");
                                      return;
                                    }else {
                                      if (Utils.isNull(errorDetail)){
                                        errorDetail = "";
                                      }
                                      playErrorShort(call,errorDetail);
                                      call.release(bridge);
                                      callBackIdCheckList =null;
                                    }
                                  }catch (Exception e){
                                    Utils.toLog(TAG, "performCheckList Exception",null,e);
                                    playErrorShort(call,e.getLocalizedMessage());
                                  }catch (Error er){
                                    Utils.toLog(TAG, "performCheckList Error",er,null);
                                    playErrorShort(call,er.getLocalizedMessage());
                                  }
                                }
                              }

                              @Override
                              public void onPhoneBatterieStep(boolean isOK) {
                                try {
                                  toLog(TAG , "performCheckList onPhoneBatterieStep start");
                                  PluginCall call = bridge.getSavedCall(callBackIdCheckList);
                                  if (call == null) {
                                    toLog(TAG , "performCheckList onPhoneBatterieStep call Is Null ");
                                    return;
                                  }else {
                                    JSObject ret = new JSObject();
                                    ret.put(phoneBatteryStatutsName, isOK);
                                    call.success(ret);
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "performCheckList onPhoneBatterieStep Exception ",null,e);
                                  playErrorShort(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "performCheckList onPhoneBatterieStep Error",er,null);
                                  playErrorShort(call,er.getLocalizedMessage());
                                }
                              }

                              @Override
                              public void onAccesStep(boolean isOK) {
                                try {
                                  toLog(TAG , "performCheckList onAccesStep start");
                                  PluginCall call = bridge.getSavedCall(callBackIdCheckList);
                                  if (call == null) {
                                    toLog(TAG , "performCheckList onAccesStep call Is Null ");
                                    return;
                                  }else {
                                    JSObject ret = new JSObject();
                                    ret.put(accesName, isOK);
                                    call.success(ret);
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "performCheckList onAccesStep Exception ",null,e);
                                  playErrorShort(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "performCheckList onAccesStep Error",er,null);
                                  playErrorShort(call,er.getLocalizedMessage());
                                }
                              }

                              @Override
                              public void onDroneStep(boolean isOK) {
                                try {
                                  toLog(TAG , "performCheckList onDroneStep start");
                                  PluginCall call = bridge.getSavedCall(callBackIdCheckList);
                                  if (call == null) {
                                    toLog(TAG , "performCheckList onDroneStep call Is Null ");
                                    return;
                                  }else {
                                    JSObject ret = new JSObject();
                                    ret.put(configName, isOK);
                                    call.success(ret);
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "performCheckList onDroneStep Exception ",null,e);
                                  playErrorShort(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "performCheckList onDroneStep Error",er,null);
                                  playErrorShort(call,er.getLocalizedMessage());
                                }
                              }

                              @Override
                              public void onControleStep(boolean isOK) {
                                try {
                                  toLog(TAG , "performCheckList onControleStep start");
                                  PluginCall call = bridge.getSavedCall(callBackIdCheckList);
                                  if (call == null) {
                                    toLog(TAG , "performCheckList onControleStep call Is Null ");
                                    return;
                                  }else {
                                    JSObject ret = new JSObject();
                                    ret.put(controleName, isOK);
                                    call.success(ret);
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "performCheckList onControleStep Exception ",null,e);
                                  playErrorShort(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "performCheckList onControleStep Error",er,null);
                                  playErrorShort(call,er.getLocalizedMessage());
                                }
                              }

                              @Override
                              public void onCameraStep(boolean isOK) {
                                try {
                                  toLog(TAG , "performCheckList onCameraStep start");
                                  PluginCall call = bridge.getSavedCall(callBackIdCheckList);
                                  if (call == null) {
                                    toLog(TAG , "performCheckList onCameraStep call Is Null ");
                                    return;
                                  }else {
                                    JSObject ret = new JSObject();
                                    ret.put(cameraName, isOK);
                                    call.success(ret);
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "performCheckList onCameraStep Exception ",null,e);
                                  playErrorShort(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "performCheckList onCameraStep Error",er,null);
                                  playErrorShort(call,er.getLocalizedMessage());
                                }
                              }

                              @Override
                              public void onCameraCalibratedStep(boolean isOK) {
                                try {
                                  toLog(TAG , "performCheckList onCameraCalibrated start");
                                  PluginCall call = bridge.getSavedCall(callBackIdCheckList);
                                  if (call == null) {
                                    toLog(TAG , "performCheckList onCameraCalibrated call Is Null ");
                                    return;
                                  }else {
                                    JSObject ret = new JSObject();
                                    ret.put(cameraCalibratedName, isOK);
                                    call.success(ret);
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "performCheckList onCameraCalibrated Exception ",null,e);
                                  playErrorShort(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "performCheckList onCameraCalibrated Error",er,null);
                                  playErrorShort(call,er.getLocalizedMessage());
                                }
                              }

                              @Override
                              public void onSDCardStep(boolean isOK) {
                                try {
                                  toLog(TAG , "performCheckList onSDCardStep start");
                                  PluginCall call = bridge.getSavedCall(callBackIdCheckList);
                                  if (call == null) {
                                    toLog(TAG , "performCheckList onSDCardStep call Is Null ");
                                    return;
                                  }else {
                                    JSObject ret = new JSObject();
                                    ret.put(sdcardName, isOK);
                                    call.success(ret);
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "performCheckList onSDCardStep Exception ",null,e);
                                  playErrorShort(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "performCheckList onSDCardStep Error",er,null);
                                  playErrorShort(call,er.getLocalizedMessage());
                                }
                              }

                              @Override
                              public void onFlyPlanStep(boolean isOK) {
                                try {
                                  toLog(TAG , "performCheckList onFlyPlanStep start");
                                  PluginCall call = bridge.getSavedCall(callBackIdCheckList);
                                  if (call == null) {
                                    toLog(TAG , "performCheckList onFlyPlanStep call Is Null ");
                                    return;
                                  }else {
                                    JSObject ret = new JSObject();
                                    ret.put(flyplanName, isOK);
                                    call.success(ret);
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "performCheckList onFlyPlanStep Exception ",null,e);
                                  playErrorShort(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "performCheckList onFlyPlanStep Error",er,null);
                                  playErrorShort(call,er.getLocalizedMessage());
                                }
                              }

                              @Override
                              public void onBatterieStep(boolean isOK) {
                                try {
                                  toLog(TAG , "performCheckList onBatterieStep start");
                                  PluginCall call = bridge.getSavedCall(callBackIdCheckList);
                                  if (call == null) {
                                    toLog(TAG , "performCheckList onBatterieStep call Is Null ");
                                    return;
                                  }else {
                                    JSObject ret = new JSObject();
                                    ret.put(droneBatteryStatutsName, isOK);
                                    call.success(ret);
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "performCheckList onBatterieStep Exception ",null,e);
                                  playErrorShort(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "performCheckList onBatterieStep Error",er,null);
                                  playErrorShort(call,er.getLocalizedMessage());
                                }
                              }

                              @Override
                              public void onGPSStep(boolean isOK) {
                                try {
                                  toLog(TAG , "performCheckList onGPSStep start");
                                  PluginCall call = bridge.getSavedCall(callBackIdCheckList);
                                  if (call == null) {
                                    toLog(TAG , "performCheckList onGPSStep call Is Null ");
                                    return;
                                  }else {
                                    JSObject ret = new JSObject();
                                    ret.put(gpsName, isOK);
                                    call.success(ret);
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "performCheckList onGPSStep Exception ",null,e);
                                  playErrorShort(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "performCheckList onGPSStep Error",er,null);
                                  playErrorShort(call,er.getLocalizedMessage());
                                }
                              }

                              @Override
                              public void onDiagnosticStep(@NonNull @NotNull String msg) {
                                try {
                                  toLog(TAG , "performCheckList onDiagnosticStep start");
                                  PluginCall call = bridge.getSavedCall(callBackIdCheckList);
                                  if (call == null) {
                                    toLog(TAG , "performCheckList onDiagnosticStep call Is Null ");
                                    return;
                                  }else {
                                    if (Utils.isNull(msg)){
                                      msg = "";
                                    }
                                    JSObject ret = new JSObject();
                                    ret.put(diagnosticName, msg);
                                    call.success(ret);
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "performCheckList onDiagnosticStep Exception ",null,e);
                                  playErrorShort(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "performCheckList onDiagnosticStep Error",er,null);
                                  playErrorShort(call,er.getLocalizedMessage());
                                }
                              }

                              @Override
                              public void onSucces() {
                                try {
                                  toLog(TAG , "performCheckList onSucces start");
                                  PluginCall call = bridge.getSavedCall(callBackIdCheckList);
                                  if (call == null) {
                                    toLog(TAG , "performCheckList onSucces call Is Null ");
                                    return;
                                  }else {
                                    JSObject ret = new JSObject();
                                    ret.put(checkSuccesName, true);
                                    call.success(ret);
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "performCheckList onSucces Exception ",null,e);
                                  playErrorShort(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "performCheckList onSucces Error",er,null);
                                  playErrorShort(call,er.getLocalizedMessage());
                                }
                              }
                            });
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }catch (Exception e){
      Utils.toLog(TAG, "performCheckList",null,e);
      playErrorShort(call,e.getLocalizedMessage());
    }catch (Error er){
      Utils.toLog(TAG, "performCheckList",er,null);
      playErrorShort(call,er.getLocalizedMessage());
    }
  }

  private void playLoading(PluginCall call, boolean showLoading) {
    if (Utils.isNull(call)){
      return;
    }else {
      try {
        JSObject ret = new JSObject();
        ret.put(startSuccesName, null);
        ret.put(finishSuccesName, null);
        ret.put(stepRemainName, null);
        ret.put(gridProgressingName, null);
        ret.put(batteryName, null);
        ret.put(imageCountName, null);
        ret.put(speedName, null);
        ret.put(surveyPointsName, null);
        ret.put(errorName, null);
        ret.put(lowBattery, null);
        if (showLoading){
          ret.put(showLoader, true);
          ret.put(hideLoader, null);
        }else {
          ret.put(showLoader, null);
          ret.put(hideLoader, true);
        }
        call.success(ret);
      }catch (Exception e){
        Utils.toLog(TAG, "playLoading",null,e);
        call.reject(e.getLocalizedMessage());
      }catch (Error er){
        Utils.toLog(TAG, "playLoading",er,null);
        call.reject(er.getLocalizedMessage());
      }
    }
  }

  private void goHome(DroneManager manager,final OnAsyncOperationComplete callback)throws Exception,Error {
    manager.returnToHome(callback);
  }

  private void playError(PluginCall call,@Nullable String errDetail) {
    if (Utils.isNull(call)){
      return;
    }else {
      try {
        if (Utils.isNull(errDetail)){
          errDetail = "";
        }
        JSObject ret = new JSObject();
        ret.put(startSuccesName, null);
        ret.put(finishSuccesName, null);
        ret.put(stepRemainName, null);
        ret.put(gridProgressingName, null);
        ret.put(batteryName, null);
        ret.put(imageCountName, null);
        ret.put(speedName, null);
        ret.put(surveyPointsName, null);
        ret.put(errorName, errDetail);
        ret.put(lowBattery, null);
        ret.put(showLoader, null);
        ret.put(hideLoader, null);
        call.success(ret);
      }catch (Exception e){
        Utils.toLog(TAG, "playError",null,e);
        call.reject(e.getLocalizedMessage());
      }catch (Error er){
        Utils.toLog(TAG, "playError",er,null);
        call.reject(er.getLocalizedMessage());
      }
    }
  }
  private void playLowBattery(PluginCall call,@Nullable String errDetail) {
    if (Utils.isNull(call)){
      return;
    }else {
      try {
        if (Utils.isNull(errDetail)){
          errDetail = "";
        }
        JSObject ret = new JSObject();
        ret.put(startSuccesName, null);
        ret.put(finishSuccesName, null);
        ret.put(stepRemainName, null);
        ret.put(gridProgressingName, null);
        ret.put(batteryName, null);
        ret.put(imageCountName, null);
        ret.put(speedName, null);
        ret.put(surveyPointsName, null);
        ret.put(errorName, null);
        ret.put(lowBattery, errDetail);
        ret.put(showLoader, null);
        ret.put(hideLoader, null);
        call.success(ret);
      }catch (Exception e){
        Utils.toLog(TAG, "playError",null,e);
        call.reject(e.getLocalizedMessage());
      }catch (Error er){
        Utils.toLog(TAG, "playError",er,null);
        call.reject(er.getLocalizedMessage());
      }
    }
  }
  private void playErrorShort(PluginCall call,@Nullable String errDetail) {
    if (Utils.isNull(call)){
      return;
    }else {
      try {
        if (Utils.isNull(errDetail)){
          errDetail = "";
        }
        JSObject ret = new JSObject();
        ret.put(errorName, errDetail);
        call.success(ret);
      }catch (Exception e){
        Utils.toLog(TAG, "playErrorShort",null,e);
        call.reject(e.getLocalizedMessage());
      }catch (Error er){
        Utils.toLog(TAG, "playErrorShort",er,null);
        call.reject(er.getLocalizedMessage());
      }
    }
  }


  @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
  public void continuWaypointMission(PluginCall call){
    toLog(TAG, "continuWaypointMission start");
    try {
      // Case NextTime clean Old
      if (Utils.isNull(callBackIdContinuMission)){
        // Skip
      }else {
        PluginCall notReleasedCall = bridge.getSavedCall(callBackIdContinuMission);
        if (notReleasedCall == null) {
          // Skip
        }else {
          notReleasedCall.release(bridge);
          callBackIdContinuMission = null;
        }
      }

      if (Utils.isNull(ctx)){
        toLog(TAG, "continuWaypointMission stop cause ctx is Null");
        return;
      }else {
        if (Utils.isNull(call)){
          toLog(TAG, "continuWaypointMission stop cause call is Null");
          return;
        }else {
          PermissionManager pm = new PermissionManager(ctx);
          if (!pm.isPermissionsGranted()){
            toLog(TAG, "continuWaypointMission stop cause REQUIRE ALL PERMISSIONS");
            playError(call,"REQUIRE ALL PERMISSIONS");
            return;
          }else{
            if (!pm.isSDKreadyForMappingProcess()){
              toLog(TAG, "SDK Not Ready for Mapping Process MIN IS API 24 ");
              playError(call,"SDK Not Ready for Mapping Process MIN IS API 24");
              return;
            }else {
              DroneManager manager = DroneManager.getInstance();
              if (Utils.isNull(manager)){
                toLog(TAG, "continuWaypointMission stop cause manager is Null");
                playError(call,"DroneManager is Null");
                return;
              }else {
                if (Utils.isNull(getMainApp())){
                  toLog(TAG, "continuWaypointMission stop cause getMainApp() is Null");
                  playError(call,"PLEASE SET MainApp BEFORE RUN Apps (setMainApp) ...MainApp IS NULL RIGHT NOW");
                  return;
                }else {
                  if (Utils.isNull(getActivity())){
                    toLog(TAG, "continuWaypointMission stop cause getActivity() is Null");
                    playError(call,"getActivity() IS NULL RIGHT NOW");
                    return;
                  }else {
                    // Keep At the End of function
                    callBackIdContinuMission = null;
                    call.save();
                    callBackIdContinuMission = Utils.clone(call.getCallbackId());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                      manager.isMissionExist(new OnAsyncOperationCompleteBool() {
                        @Override
                        public void onResultNo(@Nullable String errorDetail) {
                          synchronized (JoolMappingPlugin.this){
                            try {
                              PluginCall call = bridge.getSavedCall(callBackIdContinuMission);
                              if (call == null) {
                                toLog(TAG , "continuWaypointMission isMissionExist onResultNo Mean No Mission Found");
                                return;
                              }else {
                                if (Utils.isNull(errorDetail)){
                                  errorDetail = "";
                                }
                                playError(call,errorDetail);
                                call.release(bridge);
                                callBackIdContinuMission =null;
                              }
                            }catch (Exception e){
                              Utils.toLog(TAG, "continuWaypointMission onResultNo Exception ",null,e);
                              playError(call,e.getLocalizedMessage());
                            }catch (Error er){
                              Utils.toLog(TAG, "continuWaypointMission onResultNo Error",er,null);
                              playError(call,er.getLocalizedMessage());
                            }
                          }
                        }

                        @Override
                        public void onResultYes() {
                          manager.continuWaypointMission(getActivity(), new OnMissionRunningListener() {
                            @Override
                            public void onError(@Nullable String errorDetail) {
                              synchronized (JoolMappingPlugin.this){
                                try {
                                  PluginCall call = bridge.getSavedCall(callBackIdContinuMission);
                                  if (call == null) {
                                    toLog(TAG , "continuWaypointMission onError call Is Null ");
                                    return;
                                  }else {
                                    if (Utils.isNull(errorDetail)){
                                      errorDetail = "";
                                    }
                                    playError(call,errorDetail);
                                    call.release(bridge);
                                    callBackIdContinuMission =null;
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "continuWaypointMission onError Exception ",null,e);
                                  playError(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "continuWaypointMission onError Error",er,null);
                                  playError(call,er.getLocalizedMessage());
                                }
                              }

                            }

                            @Override
                            public void onCalibrationRequire(@Nullable String detail) {
                              synchronized (JoolMappingPlugin.this){
                                try {
                                  PluginCall call = bridge.getSavedCall(callBackIdContinuMission);
                                  if (call == null) {
                                    toLog(TAG , "continuWaypointMission onCalibrationRequire call Is Null ");
                                    return;
                                  }else {
                                    if (Utils.isNull(detail)){
                                      detail = "";
                                    }
                                    playError(call,detail);
                                    call.release(bridge);
                                    callBackIdContinuMission =null;
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "continuWaypointMission onCalibrationRequire Exception ",null,e);
                                  playError(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "continuWaypointMission onCalibrationRequire Error",er,null);
                                  playError(call,er.getLocalizedMessage());
                                }
                              }
                            }

                            @Override
                            public void onStartSucces() {
                              synchronized (JoolMappingPlugin.this){
                                try {
                                  PluginCall call = bridge.getSavedCall(callBackIdContinuMission);
                                  if (call == null) {
                                    toLog(TAG , "continuWaypointMission onStartSucces call Is Null ");
                                    return;
                                  }else {
                                    JSObject ret = new JSObject();
                                    ret.put(startSuccesName, true);
                                    ret.put(finishSuccesName, null);
                                    ret.put(stepRemainName, null);
                                    ret.put(gridProgressingName, null);
                                    ret.put(batteryName, null);
                                    ret.put(imageCountName, null);
                                    ret.put(speedName, null);
                                    ret.put(surveyPointsName, null);
                                    ret.put(errorName, null);
                                    ret.put(lowBattery, null);
                                    ret.put(showLoader, null);
                                    ret.put(hideLoader, null);
                                    call.success(ret);
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "continuWaypointMission onStartSucces Exception ",null,e);
                                  playError(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "continuWaypointMission onStartSucces Error",er,null);
                                  playError(call,er.getLocalizedMessage());
                                }
                              }
                            }

                            @Override
                            public void onNotifyStepRemain(int stepRemain) {
                              synchronized (JoolMappingPlugin.this){
                                try {
                                  PluginCall call = bridge.getSavedCall(callBackIdContinuMission);
                                  if (call == null) {
                                    toLog(TAG , "continuWaypointMission onNotifyStepRemain call Is Null ");
                                    return;
                                  }else {
                                    JSObject ret = new JSObject();
                                    ret.put(startSuccesName, null);
                                    ret.put(finishSuccesName, null);
                                    ret.put(stepRemainName, stepRemain);
                                    ret.put(gridProgressingName, null);
                                    ret.put(batteryName, null);
                                    ret.put(imageCountName, null);
                                    ret.put(speedName, null);
                                    ret.put(surveyPointsName, null);
                                    call.success(ret);
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "continuWaypointMission onNotifyStepRemain onStepSucces Exception ",null,e);
                                  playError(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "continuWaypointMission onNotifyStepRemain onStepSucces Error",er,null);
                                  playError(call,er.getLocalizedMessage());
                                }
                              }
                            }

                            @Override
                            public void onSucces() {
                              synchronized (JoolMappingPlugin.this){
                                try {
                                  PluginCall call = bridge.getSavedCall(callBackIdContinuMission);
                                  if (call == null) {
                                    toLog(TAG , "continuWaypointMission onSucces call Is Null ");
                                    return;
                                  }else {
                                    JSObject ret = new JSObject();
                                    ret.put(startSuccesName, null);
                                    ret.put(finishSuccesName, true);
                                    ret.put(stepRemainName, null);
                                    ret.put(gridProgressingName, null);
                                    ret.put(batteryName, null);
                                    ret.put(imageCountName, null);
                                    ret.put(speedName, null);
                                    ret.put(surveyPointsName, null);
                                    call.success(ret);
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "continuWaypointMission onSucces Exception ",null,e);
                                  playError(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "continuWaypointMission onSucces Error",er,null);
                                  playError(call,er.getLocalizedMessage());
                                }
                              }
                            }

                            @Override
                            public void onGridProgressing(int progress) {
                              synchronized (JoolMappingPlugin.this){
                                try {
                                  PluginCall call = bridge.getSavedCall(callBackIdContinuMission);
                                  if (call == null) {
                                    toLog(TAG , "continuWaypointMission onGridProgressing call Is Null ");
                                    return;
                                  }else {
                                    JSObject ret = new JSObject();
                                    ret.put(startSuccesName, null);
                                    ret.put(finishSuccesName, null);
                                    ret.put(stepRemainName, null);
                                    ret.put(gridProgressingName, progress);
                                    ret.put(batteryName, null);
                                    ret.put(imageCountName, null);
                                    ret.put(speedName, null);
                                    ret.put(surveyPointsName, null);
                                    call.success(ret);
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "continuWaypointMission onGridProgressing Exception ",null,e);
                                  playError(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "continuWaypointMission onGridProgressing Error",er,null);
                                  playError(call,er.getLocalizedMessage());
                                }
                              }
                            }

                            @Override
                            public void onNotifyState(int battery, int imageCount, float speed, CopyOnWriteArrayList<LatLng> flyPoints) {
                              synchronized (JoolMappingPlugin.this){
                                try {
                                  PluginCall call = bridge.getSavedCall(callBackIdContinuMission);
                                  if (call == null) {
                                    toLog(TAG , "continuWaypointMission onNotifyState call Is Null ");
                                    return;
                                  }else {
                                    JSONArray array = new JSONArray();
                                    for (LatLng item :flyPoints) {
                                      JSONObject o = new JSONObject();
                                      o.put("latitude",item.latitude);
                                      o.put("longitude",item.longitude);
                                      array.put(o);
                                    }
                                    JSObject ret = new JSObject();
                                    ret.put(startSuccesName, null);
                                    ret.put(finishSuccesName, null);
                                    ret.put(stepRemainName, null);
                                    ret.put(gridProgressingName, null);
                                    ret.put(batteryName, battery);
                                    ret.put(imageCountName, imageCount);
                                    ret.put(speedName, speed);
                                    ret.put(surveyPointsName, array.toString());
                                    call.success(ret);
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "continuWaypointMission",null,e);
                                  playError(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "continuWaypointMission",er,null);
                                  playError(call,er.getLocalizedMessage());
                                }
                              }
                            }

                            @Override
                            public void mustGoHomeError() {
                              synchronized (JoolMappingPlugin.this){
                                try {
                                  Runnable notifyAction = () ->{
                                    try {
                                      PluginCall call = bridge.getSavedCall(callBackIdContinuMission);
                                      if (call == null) {
                                        toLog(TAG , "continuWaypointMission notifyAction call Is Null ");
                                        return;
                                      }else {
                                        playError(call,"DRONE GOING HOME cause Error");
                                        call.release(bridge);
                                        callBackIdContinuMission=null;
                                      }
                                    }catch (Exception e){
                                      Utils.toLog(TAG, "continuWaypointMission mustGoHomeError Exception ",null,e);
                                      playError(call,e.getLocalizedMessage());
                                    }catch (Error er){
                                      Utils.toLog(TAG, "continuWaypointMission mustGoHomeError Error",er,null);
                                      playError(call,er.getLocalizedMessage());
                                    }
                                  };
                                  goHome(manager, new OnAsyncOperationComplete() {
                                    @Override
                                    public void onError(@Nullable String errorDetail) {
                                      toLog(TAG , "continuWaypointMission goHome RESULT onError Normal if Force GoBack ");
                                      notifyAction.run();
                                    }

                                    @Override
                                    public void onSucces(@Nullable String succesMsg) {
                                      toLog(TAG , "continuWaypointMission goHome RESULT onSucces ");
                                      notifyAction.run();
                                    }
                                  });
                                }catch (Exception e){
                                  Utils.toLog(TAG, "continuWaypointMission mustGoHome Exception ",null,e);
                                  playError(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "continuWaypointMission mustGoHome Error",er,null);
                                  playError(call,er.getLocalizedMessage());
                                }
                              }
                            }

                            @Override
                            public void onEnableLoadingForNextMission() {
                              synchronized (JoolMappingPlugin.this){
                                try {
                                  PluginCall call = bridge.getSavedCall(callBackIdContinuMission);
                                  if (call == null) {
                                    toLog(TAG , "continuWaypointMission onEnableLoadingForNextMission call Is Null ");
                                    return;
                                  }else {
                                    notifyReady(false);
                                    playLoading(call,true);
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "continuWaypointMission onEnableLoadingForNextMission",null,e);
                                  playError(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "continuWaypointMission onEnableLoadingForNextMission",er,null);
                                  playError(call,er.getLocalizedMessage());
                                }
                              }
                            }

                            @Override
                            public void onDisableLoadingForNextMission() {
                              synchronized (JoolMappingPlugin.this){
                                try {
                                  PluginCall call = bridge.getSavedCall(callBackIdContinuMission);
                                  if (call == null) {
                                    toLog(TAG , "continuWaypointMission onDisableLoadingForNextMission call Is Null ");
                                    return;
                                  }else {
                                    notifyReady(true);
                                    playLoading(call,false);
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "continuWaypointMission onDisableLoadingForNextMission",null,e);
                                  playError(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "continuWaypointMission onDisableLoadingForNextMission",er,null);
                                  playError(call,er.getLocalizedMessage());
                                }
                              }
                            }

                            @Override
                            public void mustGoHomeLowBattery() {
                              synchronized (JoolMappingPlugin.this){
                                try {
                                  Runnable notifyAction = () ->{
                                    try {
                                      PluginCall call = bridge.getSavedCall(callBackIdContinuMission);
                                      if (call == null) {
                                        toLog(TAG , "continuWaypointMission notifyAction call Is Null ");
                                        return;
                                      }else {
                                        playLowBattery(call,"DRONE GOING HOME cause LowBattery");
                                        call.release(bridge);
                                        callBackIdContinuMission=null;
                                      }
                                    }catch (Exception e){
                                      Utils.toLog(TAG, "continuWaypointMission mustGoHomeError Exception ",null,e);
                                      playError(call,e.getLocalizedMessage());
                                    }catch (Error er){
                                      Utils.toLog(TAG, "continuWaypointMission mustGoHomeError Error",er,null);
                                      playError(call,er.getLocalizedMessage());
                                    }
                                  };
                                  goHome(manager, new OnAsyncOperationComplete() {
                                    @Override
                                    public void onError(@Nullable String errorDetail) {
                                      toLog(TAG , "continuWaypointMission goHome RESULT onError Normal if Force GoBack ");
                                      notifyAction.run();
                                    }

                                    @Override
                                    public void onSucces(@Nullable String succesMsg) {
                                      toLog(TAG , "continuWaypointMission goHome RESULT onSucces ");
                                      notifyAction.run();
                                    }
                                  });
                                }catch (Exception e){
                                  Utils.toLog(TAG, "continuWaypointMission mustGoHome Exception ",null,e);
                                  playError(call,e.getLocalizedMessage());
                                }catch (Error er){
                                  Utils.toLog(TAG, "continuWaypointMission mustGoHome Error",er,null);
                                  playError(call,er.getLocalizedMessage());
                                }
                              }
                            }
                          });
                        }
                      });
                    }
                  }
                }
              }
            }
          }
        }
      }
    }catch (Exception e){
      Utils.toLog(TAG, "continuWaypointMission",null,e);
      playError(call,e.getLocalizedMessage());
    }catch (Error er){
      Utils.toLog(TAG, "continuWaypointMission",er,null);
      playError(call,er.getLocalizedMessage());
    }
  }

  @PluginMethod
  public void isMissionExist(PluginCall call){
    toLog(TAG, "isMissionExist start");
    try {
      if (Utils.isNull(ctx)){
        toLog(TAG, "isMissionExist stop cause ctx is Null");
        return;
      }else {
        if (Utils.isNull(call)){
          toLog(TAG, "isMissionExist stop cause call is Null");
          return;
        }else {
          PermissionManager pm = new PermissionManager(ctx);
          if (!pm.isPermissionsGranted()){
            toLog(TAG, "isMissionExist stop cause REQUIRE ALL PERMISSIONS");
            call.reject("REQUIRE ALL PERMISSIONS");
            return;
          }else{
            if (!pm.isSDKreadyForMappingProcess()){
              toLog(TAG, "SDK Not Ready for Mapping Process MIN IS API 24 ");
              call.reject("SDK Not Ready for Mapping Process MIN IS API 24");
              return;
            }else {
              DroneManager manager = DroneManager.getInstance();
              if (Utils.isNull(manager)){
                toLog(TAG, "isMissionExist stop cause manager is Null");
                call.reject("DroneManager is Null");
                return;
              }else {
                if (Utils.isNull(getMainApp())){
                  toLog(TAG, "isMissionExist stop cause getMainApp() is Null");
                  call.reject("PLEASE SET MainApp BEFORE RUN Apps (setMainApp) ...MainApp IS NULL RIGHT NOW");
                  return;
                }else {
                  // Keep At the End of function
                  callBackIdsMissionExist = null;
                  call.save();
                  callBackIdsMissionExist = Utils.clone(call.getCallbackId());

                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    manager.isMissionExist(new OnAsyncOperationCompleteBool() {
                      @Override
                      public void onResultNo(@Nullable String errorDetail) {
                        try {
                          PluginCall call = bridge.getSavedCall(callBackIdsMissionExist);
                          if (call == null) {
                            toLog(TAG , "isMissionExist onResultNo call Is Null ");
                            return;
                          }else {
                            if (Utils.isNull(errorDetail)){
                              errorDetail = "";
                            }

                            JSObject ret = new JSObject();
                            ret.put(isMissionExistName, false);
                            call.resolve(ret);
                            call.release(bridge);
                            callBackIdsMissionExist = null;
                          }
                        }catch (Exception e){
                          Utils.toLog(TAG, "isMissionExist",null,e);
                          call.reject(e.getLocalizedMessage(), e);
                        }catch (Error er){
                          Utils.toLog(TAG, "isMissionExist",er,null);
                          call.reject(er.getLocalizedMessage());
                        }
                      }

                      @Override
                      public void onResultYes() {
                        try {
                          PluginCall call = bridge.getSavedCall(callBackIdsMissionExist);
                          if (call == null) {
                            toLog(TAG , "isMissionExist onResultYes call Is Null ");
                            return;
                          }else {
                            JSObject ret = new JSObject();
                            ret.put(isMissionExistName, true);
                            call.resolve(ret);
                            call.release(bridge);
                            callBackIdsMissionExist = null;
                          }
                        }catch (Exception e){
                          Utils.toLog(TAG, "isMissionExist",null,e);
                          call.reject(e.getLocalizedMessage(), e);
                        }catch (Error er){
                          Utils.toLog(TAG, "isMissionExist",er,null);
                          call.reject(er.getLocalizedMessage());
                        }
                      }
                    });
                  }
                  toLog(TAG, "isMissionExist Success launch");
                }
              }
            }
          }
        }
      }
    }catch (Exception e){
      Utils.toLog(TAG, "isMissionExist",null,e);
      call.reject(e.getLocalizedMessage(), e);
    }catch (Error er){
      Utils.toLog(TAG, "isMissionExist",er,null);
      call.reject(er.getLocalizedMessage());
    }
  }

  @PluginMethod
  public void cleanUserData(PluginCall call){
    toLog(TAG, "cleanUserData start");
    try {
      if (Utils.isNull(ctx)){
        toLog(TAG, "cleanUserData stop cause ctx is Null");
        return;
      }else {
        if (Utils.isNull(call)){
          toLog(TAG, "cleanUserData stop cause call is Null");
          return;
        }else {
          PermissionManager pm = new PermissionManager(ctx);
          if (!pm.isPermissionsGranted()){
            toLog(TAG, "cleanUserData stop cause REQUIRE ALL PERMISSIONS");
            call.reject("REQUIRE ALL PERMISSIONS");
            return;
          }else{
            if (!pm.isSDKreadyForMappingProcess()){
              toLog(TAG, "SDK Not Ready for Mapping Process MIN IS API 24 ");
              call.reject("SDK Not Ready for Mapping Process MIN IS API 24");
              return;
            }else {
              DroneManager manager = DroneManager.getInstance();
              if (Utils.isNull(manager)){
                toLog(TAG, "cleanUserData stop cause manager is Null");
                call.reject("DroneManager is Null");
                return;
              }else {
                if (Utils.isNull(getMainApp())){
                  toLog(TAG, "cleanUserData stop cause getMainApp() is Null");
                  call.reject("PLEASE SET MainApp BEFORE RUN Apps (setMainApp) ...MainApp IS NULL RIGHT NOW");
                  return;
                }else {
                  // Keep At the End of function
                  callBackIdsCleanData = null;
                  call.save();
                  callBackIdsCleanData = Utils.clone(call.getCallbackId());

                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    manager.cleanUserData(new OnAsyncOperationCompleteBool() {
                      @Override
                      public void onResultNo(@Nullable String errorDetail) {
                        try {
                          PluginCall call = bridge.getSavedCall(callBackIdsCleanData);
                          if (call == null) {
                            toLog(TAG , "cleanUserData onResultNo call Is Null ");
                            return;
                          }else {
                            if (Utils.isNull(errorDetail)){
                              errorDetail = "";
                            }
                            call.reject(errorDetail);
                            call.release(bridge);
                            callBackIdsCleanData = null;
                          }
                        }catch (Exception e){
                          Utils.toLog(TAG, "cleanUserData",null,e);
                          call.reject(e.getLocalizedMessage(), e);
                        }catch (Error er){
                          Utils.toLog(TAG, "cleanUserData",er,null);
                          call.reject(er.getLocalizedMessage());
                        }
                      }

                      @Override
                      public void onResultYes() {
                        try {
                          PluginCall call = bridge.getSavedCall(callBackIdsCleanData);
                          if (call == null) {
                            toLog(TAG , "cleanUserData onResultYes call Is Null ");
                            return;
                          }else {
                            call.resolve();
                            call.release(bridge);
                            callBackIdsCleanData = null;
                          }
                        }catch (Exception e){
                          Utils.toLog(TAG, "cleanUserData",null,e);
                          call.reject(e.getLocalizedMessage(), e);
                        }catch (Error er){
                          Utils.toLog(TAG, "cleanUserData",er,null);
                          call.reject(er.getLocalizedMessage());
                        }
                      }
                    });
                  }
                  toLog(TAG, "cleanUserData Success launch");
                }
              }
            }
          }
        }
      }
    }catch (Exception e){
      Utils.toLog(TAG, "cleanUserData",null,e);
      call.reject(e.getLocalizedMessage(), e);
    }catch (Error er){
      Utils.toLog(TAG, "cleanUserData",er,null);
      call.reject(er.getLocalizedMessage());
    }
  }

  @PluginMethod
  public void returnToHome(PluginCall call){
    toLog(TAG, "returnToHome start");
    try {
      if (Utils.isNull(ctx)){
        toLog(TAG, "returnToHome stop cause ctx is Null");
        return;
      }else {
        if (Utils.isNull(call)){
          toLog(TAG, "returnToHome stop cause call is Null");
          return;
        }else {
          PermissionManager pm = new PermissionManager(ctx);
          if (!pm.isPermissionsGranted()){
            toLog(TAG, "returnToHome stop cause REQUIRE ALL PERMISSIONS");
            call.reject("REQUIRE ALL PERMISSIONS");
            return;
          }else{
            if (!pm.isSDKreadyForMappingProcess()){
              toLog(TAG, "SDK Not Ready for Mapping Process MIN IS API 24 ");
              call.reject("SDK Not Ready for Mapping Process MIN IS API 24");
              return;
            }else {
              DroneManager manager = DroneManager.getInstance();
              if (Utils.isNull(manager)){
                toLog(TAG, "returnToHome stop cause manager is Null");
                call.reject("DroneManager is Null");
                return;
              }else {
                if (Utils.isNull(getMainApp())){
                  toLog(TAG, "returnToHome stop cause getMainApp() is Null");
                  call.reject("PLEASE SET MainApp BEFORE RUN Apps (setMainApp) ...MainApp IS NULL RIGHT NOW");
                  return;
                }else {
                  // Keep At the End of function
                  callBackIdsReturnToHome= null;
                  call.save();
                  callBackIdsReturnToHome = Utils.clone(call.getCallbackId());

                  goHome(manager,new OnAsyncOperationComplete() {
                    @Override
                    public void onError(@Nullable String errorDetail) {
                      try {
                        PluginCall call = bridge.getSavedCall(callBackIdsReturnToHome);
                        if (call == null) {
                          toLog(TAG , "returnToHome onError call Is Null ");
                          return;
                        }else {
                          if (Utils.isNull(errorDetail)){
                            errorDetail = "";
                          }
                          call.reject(errorDetail);
                          call.release(bridge);
                          callBackIdsReturnToHome = null;
                        }
                      }catch (Exception e){
                        Utils.toLog(TAG, "returnToHome",null,e);
                        call.reject(e.getLocalizedMessage(), e);
                      }catch (Error er){
                        Utils.toLog(TAG, "returnToHome",er,null);
                        call.reject(er.getLocalizedMessage());
                      }

                    }

                    @Override
                    public void onSucces(@Nullable String succesMsg) {
                      try {
                        PluginCall call = bridge.getSavedCall(callBackIdsReturnToHome);
                        if (call == null) {
                          toLog(TAG , "returnToHome onSucces call Is Null ");
                          return;
                        }else {
                          if (Utils.isNull(succesMsg)){
                            succesMsg = "";
                          }
                          call.resolve();
                          call.release(bridge);
                          callBackIdsReturnToHome = null;
                        }
                      }catch (Exception e){
                        Utils.toLog(TAG, "returnToHome",null,e);
                        call.reject(e.getLocalizedMessage(), e);
                      }catch (Error er){
                        Utils.toLog(TAG, "returnToHome",er,null);
                        call.reject(er.getLocalizedMessage());
                      }
                    }
                  });
                  toLog(TAG, "returnToHome Success launch");
                }
              }
            }
          }
        }
      }
    }catch (Exception e){
      Utils.toLog(TAG, "returnToHome",null,e);
      call.reject(e.getLocalizedMessage(), e);
    }catch (Error er){
      Utils.toLog(TAG, "returnToHome",er,null);
      call.reject(er.getLocalizedMessage());
    }
  }

  @PluginMethod
  public void logToFile(PluginCall call){
    toLog(TAG, "logToFile start");
    try {
      if (Utils.isNull(ctx)){
        toLog(TAG, "logToFile stop cause ctx is Null");
        return;
      }else {
        if (Utils.isNull(call)){
          toLog(TAG, "logToFile stop cause call is Null");
          return;
        }else {
          PermissionManager pm = new PermissionManager(ctx);
          if (!pm.isPermissionsGranted()){
            toLog(TAG, "logToFile stop cause REQUIRE ALL PERMISSIONS");
            call.reject("REQUIRE ALL PERMISSIONS");
            return;
          }else{
            if (!pm.isSDKreadyForMappingProcess()){
              toLog(TAG, "logToFile SDK Not Ready for Mapping Process MIN IS API 24 ");
              call.reject("SDK Not Ready for Mapping Process MIN IS API 24");
              return;
            }else {
              JSObject data = call.getData();
              if (Utils.isNull(data)){
                toLog(TAG, "logToFile data Object is null");
                call.reject("data Object is null");
                return;
              }else {
                if (!data.has("message")){
                  toLog(TAG, "logToFile Must provide : key message");
                  call.reject("Must provide : key message");
                  return;
                }else {
                  String message = data.getString("message");
                  if (Utils.isNullOrEmpty(message)){
                    toLog(TAG, "logToFile Must provide : key message. message IS NULL or EMPTY");
                    call.reject("Must provide : key message.  message IS NULL or EMPTY");
                    return;
                  }else {
                    toLog(TAG,message);
                    call.resolve();
                    return;
                  }
                }
              }
            }
          }
        }
      }
    }catch (Exception e){
      Utils.toLog(TAG, "logToFile",null,e);
      call.reject(e.getLocalizedMessage(), e);
    }catch (Error er){
      Utils.toLog(TAG, "logToFile",er,null);
      call.reject(er.getLocalizedMessage());
    }
  }




  private void toLog(@NonNull String tag, @NonNull String msg){
    Utils.toLog(tag, msg);
  }
}
