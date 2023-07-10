package com.jool.plugin.mapping.model;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;
import com.jool.plugin.mapping.DJISampleApplication;
import com.jool.plugin.mapping.interfaces.OnAsyncOperationComplete;
import com.jool.plugin.mapping.interfaces.OnAsyncOperationCompleteBool;
import com.jool.plugin.mapping.interfaces.OnCheckListChangeListener;
import com.jool.plugin.mapping.interfaces.OnMissionProcessingComplete;
import com.jool.plugin.mapping.interfaces.OnMissionRunningListener;
import com.jool.plugin.mapping.interfaces.OnMissionSplitComplete;
import com.jool.plugin.mapping.interfaces.OnStateChangeListener;
import com.jool.plugin.mapping.mapping.PointLatLngAlt;
import com.jool.plugin.mapping.mission.MissionDJI;
import com.jool.plugin.mapping.mission.MissionSetting;
import com.jool.plugin.mapping.mission.MissionStep;
import com.jool.plugin.mapping.presenters.LocationManager;
import com.jool.plugin.mapping.presenters.PermissionManager;
import com.jool.plugin.mapping.utils.Constants;
import com.jool.plugin.mapping.utils.Dummy;
import com.jool.plugin.mapping.utils.Save;
import com.jool.plugin.mapping.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import dji.common.camera.SettingsDefinitions;
import dji.common.camera.WhiteBalance;
import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionState;
import dji.common.model.LocationCoordinate2D;
import dji.common.product.Model;
import dji.common.util.CommonCallbacks;
import dji.keysdk.CameraKey;
import dji.keysdk.DiagnosticsKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.keysdk.callback.ActionCallback;
import dji.keysdk.callback.GetCallback;
import dji.log.DJILog;
import dji.midware.data.model.P3.DataCameraGetPushShotInfo;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LDMModule;
import dji.sdk.sdkmanager.LDMModuleType;

public class DroneDJI extends Drone {
  private float heading = 0;
  private int numberOfDischarges = 0;
  private int lifetimeRemainingInPercent = 0;
  private @Nullable Context context;
  private @Nullable OnStateChangeListener listener;
  private final String TAG = this.getClass().getSimpleName();
  private boolean isRegisted;
  private @Nullable Aircraft aircraft ;
  private @Nullable DroneState  droneState;
  private FlightController mFlightController;
  private CopyOnWriteArrayList<Waypoint> waypointList = new CopyOnWriteArrayList<>();
  private volatile boolean notify =false,joystickEnable;
  private @Nullable MissionDJI mission = null;
  private @Nullable static Boolean dontTakeOff = null;
  private static @Nullable MissionSetting inMemoryMissionSetting = null;
  private static @Nullable MissionStep inMemoryMissionStep = null;
  private static @Nullable Boolean checkListDone = null;

  public static Boolean getDontTakeOff() {
    if (Utils.isNull(dontTakeOff)){
      return false;
    }
    return dontTakeOff;
  }

  public static void setDontTakeOff(@Nullable Boolean dontTakeOff) {
    DroneDJI.dontTakeOff = dontTakeOff;
  }

  public static void resetMemoryMission(@Nullable Boolean onlyCheckList) {
    try {
      if (Utils.isNull(onlyCheckList)){
        onlyCheckList = false;
      }
      if (onlyCheckList){
        checkListDone = null;
        return;
      }else {
        checkListDone = null;
        inMemoryMissionSetting = null;
        inMemoryMissionStep = null;
      }

    }catch (Exception e){
      Utils.toLog(DroneDJI.class.getSimpleName(), "resetInMemoryMission",null,e);
    }catch (Error er){
      Utils.toLog(DroneDJI.class.getSimpleName(), "resetInMemoryMission",er,null);
    }
  }

  public static void saveMemoryMission(final MissionSetting missionSetting, final  MissionStep steps) {
    inMemoryMissionSetting = missionSetting;
    inMemoryMissionStep = steps;
  }


  public boolean isAlreadyNotify() {
    return notify;
  }

  public void setAlreadyNotify(boolean notify) {
    this.notify = notify;
  }

  public boolean isJoystickEnable() {
    return joystickEnable;
  }

  public void setJoystickEnable(boolean joystickEnable) {
    this.joystickEnable = joystickEnable;
  }

  @Nullable
  public Aircraft getAircraft() {
    if (aircraft == null) {
      // Refresh aircraft
      makeBind();
    }
    return aircraft;
  }

  public void setAircraft(@Nullable Aircraft aircraft) {
    this.aircraft = aircraft;
  }

  public boolean isRegisted() {
    return isRegisted;
  }

  public void setRegisted(boolean registed) {
    isRegisted = registed;
  }

  public DroneDJI (){
    setRegisted(false);
    setAlreadyNotify(false);
    setJoystickEnable(false);
    setDontTakeOff(null);
    makeBind();

  }

  private void makeBind() {
    BaseProduct baseProduct = DJISampleApplication.getProductInstance();
    if (Utils.isNull(baseProduct)){
      toLog(" baseProduct  IS NULL ----------->PLEASE RECONNECT DRONE <----------- ");
      return;
    }else {
      setAircraft ((Aircraft) baseProduct);
    }
  }

  @Nullable
  public Context getContext() {
    return context;
  }

  public void setContext(@Nullable Context context) {
    this.context = context;
  }


  private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
  private static final int REQUEST_PERMISSION_CODE = 12345;

  @Override
  public void ini(@NonNull Context ctx) {
    try {
      setContext(ctx);
      if (Utils.isNull(getContext())){
        toLog("ini  getContext() Is Null");
        return;
      }
    }catch (Exception e){
      Utils.toLog(TAG, "ini",null,e);
    }catch (Error er){
      Utils.toLog(TAG, "ini",er,null);
    }
  }

  public void setGoBackHomePosition(OnAsyncOperationComplete callback){
    try {
      if (Utils.isNull(callback)){
        toLog("setGoBackHomePosition callback Is Null");
        return;
      }else {
        if (Utils.isNull(getContext())){
          toLog("setGoBackHomePosition is Null");
          callback.onError("setGoBackHomePosition is Null");
          return;
        }else {
          if (!isValid(false, null, null)){
            toLog("setGoBackHomePosition isValid FALSE");
            callback.onError("setGoBackHomePosition isValid FALSE");
            return;
          }else {
            Double androidPhoneLat = LocationManager.getuLatitude();
            Double androidPhoneLng = LocationManager.getuLongitude();
            if (androidPhoneLat == Constants.DOUBLE_NULL || androidPhoneLng == Constants.DOUBLE_NULL){
              toLog(" androidPhoneLat or androidPhoneLng Is DOUBLE_NULL");
              callback.onError(" androidPhoneLat or androidPhoneLng Is DOUBLE_NULL");
              return;
            }else {
              if (androidPhoneLat.isNaN() || androidPhoneLng.isNaN()){
                toLog(" androidPhoneLat or androidPhoneLng Is NaN");
                callback.onError(" androidPhoneLat or androidPhoneLng Is DOUBLE_NULL");
                return;
              }else {
                LatLng homeLatLng = new LatLng(LocationManager.getuLatitude(),LocationManager.getuLongitude());
                LocationCoordinate2D homeLocation = new LocationCoordinate2D(homeLatLng.latitude, homeLatLng.longitude);
                mFlightController.setHomeLocation(homeLocation,locationError ->{
                  try {
                    if (!Utils.isNull(locationError)){
                      String detail = locationError.getDescription();
                      if (Utils.isNull(detail)){
                        detail ="";
                      }
                      toLog(" setHomeLocation  Failed "+detail);
                      callback.onError(detail);
                    }else {
                      callback.onSucces("");
                    }
                  }catch (Exception e){
                    Utils.toLog(TAG, "setHomeLocation",null,e);
                  }catch (Error er){
                    Utils.toLog(TAG, "setHomeLocation",er,null);
                  }
                });
              }
            }
          }
        }
      }
    }catch (Exception e){
      Utils.toLog(TAG, "setGoBackHomePosition",null,e);
    }catch (Error er){
      Utils.toLog(TAG, "setGoBackHomePosition",er,null);
    }
  }

  public void connect(final @NonNull OnAsyncOperationComplete callback) {

    if (Utils.isNull(callback)){
      toLog("connect callback Is Null");
      return;
    }else {
      try {
        if (Utils.isNull(getContext())){
          toLog("getContext() is Null");
          callback.onError("getContext() is Null");
          return;
        }else {
          startSDKRegistration(callback);
        }
      }catch (Exception e){
        Utils.toLog(TAG, "connect",null,e);
        callback.onError("Error");
      }catch (Error er){
        Utils.toLog(TAG, "connect",er,null);
        callback.onError("Error");
      }
    }
  }

  private void startSDKRegistration(final @NonNull OnAsyncOperationComplete callback) {
    String msg = "";
    if (Utils.isNull(callback)){
      toLog("startSDKRegistration listener is Null");
      return;
    }else {
      try {
        if (Utils.isNull(getContext())){
          msg = "startSDKRegistration  getContext() Is Null";
          toLog(msg);
          callback.onError(msg);
          return;
        }else {
          if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(new Runnable() {
              @Override
              public void run() {
                // ToastUtils.setResultToToast(mContext.getString(R.string.sdk_registration_doing_message));
                //if we hope the Firmware Upgrade module could access the network under LDM mode, we need call the setModuleNetworkServiceEnabled()
                //method before the registerAppForLDM() method
                DJISDKManager.getInstance().getLDMManager().setModuleNetworkServiceEnabled(new LDMModule.Builder().moduleType(
                  LDMModuleType.FIRMWARE_UPGRADE).enabled(false).build());
                DJISDKManager.getInstance().registerApp(getContext().getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
                  @Override
                  public void onRegister(DJIError djiError) {
                    try {
                      toLog("startSDKRegistration onRegister start");
                      if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                        DJILog.e("App registration", DJISDKError.REGISTRATION_SUCCESS.getDescription());
                        DJISDKManager.getInstance().startConnectionToProduct();
                        toLog("startSDKRegistration Register Success");
                        return;
                      } else {
                        // ToastUtils.setResultToToast(mContext.getString(R.string.sdk_registration_message) + djiError.getDescription());
                        String description = djiError.getDescription();
                        if (Utils.isNull(description)){
                          description = "";
                        }
                        toLog("startSDKRegistration Register sdk fails, check network is available");
                        setRegisted(false);
                        callback.onError("startSDKRegistration Register sdk fails, check network is available.. Detail:"+description);
                        return;
                      }
                    }catch (Exception e){
                      setRegisted(false);
                      Utils.toLog(TAG, "onRegister",null,e);
                      if (Utils.isNull(callback)){
                        return;
                      }else {
                        callback.onError("Error");
                      }
                    }catch (Error er){
                      setRegisted(false);
                      Utils.toLog(TAG, "onRegister",er,null);
                      if (Utils.isNull(callback)){
                        return;
                      }else {
                        callback.onError("Error");
                      }
                    }

                  }
                  @Override
                  public void onProductDisconnect() {
                    Log.d(TAG, "onProductDisconnect");
                    toLog("startSDKRegistration Product Disconnected");
                    notifyStatusChange(callback);
                  }
                  @Override
                  public void onProductConnect(BaseProduct baseProduct) {
                    Log.d(TAG, String.format("onProductConnect newProduct:%s", baseProduct));
                    toLog("startSDKRegistration Product onProductConnect start");
                    notifyStatusChange(callback);
                  }

                  @Override
                  public void onProductChanged(BaseProduct baseProduct) {
                    toLog("startSDKRegistration onProductChanged start");
                    notifyStatusChange(null);
                  }

                  @Override
                  public void onComponentChange(BaseProduct.ComponentKey componentKey,
                                                BaseComponent oldComponent,
                                                BaseComponent newComponent) {
                    toLog("startSDKRegistration onComponentChange start");
                    if (newComponent != null) {
                      newComponent.setComponentListener(new BaseComponent.ComponentListener() {

                        @Override
                        public void onConnectivityChange(boolean isConnected) {
                          Log.d("TAG", "onComponentConnectivityChanged: " + isConnected);
                          notifyStatusChange(null);
                        }
                      });
                    }
                  }

                  @Override
                  public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {
                    toLog("startSDKRegistration onInitProcess start");
                  }

                  @Override
                  public void onDatabaseDownloadProgress(long current, long total) {
                    toLog("startSDKRegistration onDatabaseDownloadProgress");
                  }
                });
              }


            });
          }else {
            playSucces(callback);

            return;
          }

        }
      }catch (Exception e){
        Utils.toLog(TAG, "startSDKRegistration",null,e);
        callback.onError("Error");
      }catch (Error er){
        Utils.toLog(TAG, "startSDKRegistration",er,null);
        callback.onError("Error");
      }
    }
  }

  private void notifyStatusChange(@Nullable OnAsyncOperationComplete callback) {
    if (Utils.isNull(callback)){
      return;
    }else {
      // BIN TO GET Aircraft instance
      if (Utils.isNull(getAircraft())){
        toLog("notifyStatusChange Stop cause getAircraft is Null ----------->PLEASE RECONNECT DRONE <-----------");
        callback.onError("notifyStatusChange Stop cause getAircraft is Null ----------->PLEASE RECONNECT DRONE <-----------");
        return;
      }else {
        playSucces(callback);
      }
    }
  }
  private void playSucces(OnAsyncOperationComplete callback) {
    try {
      new Handler(Looper.getMainLooper()).postDelayed(()->{
        try {
          toLog("connect BIND ---AFTER DONE");
          if (Utils.isNull(getAircraft())){
            toLog("connect getAircraft() is Null ----------->PLEASE RECONNECT DRONE <-----------");
            callback.onError("connect getAircraft() is Null ----------->PLEASE RECONNECT DRONE <-----------");
            return;
          }else {
            mFlightController = getAircraft().getFlightController();
            if (Utils.isNull(mFlightController)){
              toLog(" mFlightController Is Null or No Connected ----------->PLEASE RECONNECT DRONE <-----------");
              callback.onError(" mFlightController Is Null or No Connected ----------->PLEASE RECONNECT DRONE <-----------");
              return;
            }else {
              setRegisted(true);
              callback.onSucces("");
            }
          }
        }catch (Exception e){
          Utils.toLog(TAG, "playSucces",null,e);
          callback.onError("Error");
        }catch (Error er){
          Utils.toLog(TAG, "playSucces",er,null);
          callback.onError("Error");
        }
      }, 400);
    }catch (Exception e){
      Utils.toLog(TAG, "playSucces",null,e);
      callback.onError("Error");
    }catch (Error er){
      Utils.toLog(TAG, "playSucces",er,null);
      callback.onError("Error");
    }


  }

  @Override
  public void disconnect(@NonNull OnAsyncOperationComplete callback) {
    toLog("disconnect   start ");
    setJoystickEnable(false);

  }

  public void registerToListenDroneState(@NonNull OnStateChangeListener callBack) {
    this.listener = callBack;
    toLog("registerToListenDroneState  SET OKAY ");

    refreshSDKAndFlightState();
  }

  public void stopNotifyTelemetry(@NonNull OnAsyncOperationComplete callback){
    synchronized (DroneDJI.this){
      toLog("stopNotifyTelemetry Start");
      try {
        if (Utils.isNull(callback)){
          toLog("callback is Null");
          callback.onError("Error");
          return;
        }else {
          if (Utils.isNull(droneState)){
            toLog("droneState is Null");
            callback.onError("Error");
            return;
          }else {
            setAlreadyNotify(false);
            droneState.stopNotify();
            droneState = null;
            toLog("stopNotifyTelemetry SUCCES droneState to NULL");
            callback.onSucces("");
          }
        }
      }catch (Exception e){
        Utils.toLog(TAG, "stopNotifyTelemetry",null,e);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("Error");
        }
      }catch (Error er){
        Utils.toLog(TAG, "stopNotifyTelemetry",er,null);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("Error");
        }
      }
    }
  }

  @Override
  public void doTest(@NonNull final Activity activity, final OnAsyncOperationComplete callback) {
    try {
      if (Utils.isNull(callback)){
        toLog("callback is Null");
        return;
      }else {
        if (Utils.isNull(activity)){
          String msg = " Activity is Null";
          toLog(msg);
          callback.onError("Activity is Null  ----------->PLEASE RECONNECT DRONE <-----------");
          return;
        }else {
          if (!isValid(true, null, null)){
            String msg = " isValid FALSE not ready to FLY";
            toLog(msg);
            callback.onError("Config Not Valid ----------->PLEASE RECONNECT DRONE <-----------");
            return;
          }else {
            // doDummyMapping();
            goTo70MetersWaypoint(activity,callback);

          }
        }

      }
    }catch (Exception e){
      Utils.toLog(TAG, "doTest",null,e);
      if (Utils.isNull(callback)){
        return;
      }else {
        callback.onError("Error");
      }
    }catch (Error er){
      Utils.toLog(TAG, "doTest",er,null);
      if (Utils.isNull(callback)){
        return;
      }else {
        callback.onError("Error");
      }
    }
  }

  @Override
  public void onDestroy() {
    if (Utils.isNull(getContext())){
      toLog("onDestroy  getContext( Is Null");
      return;
    }else {
      try {
        setDontTakeOff(null);
        setRegisted(false);
        setAlreadyNotify(false);
        setJoystickEnable(false);
        checkListDone = null;
        resetMemoryMission(null);
        LocationManager locationManager =LocationManager.getInstance();
        if (Utils.isNull(locationManager)){
          toLog("onDestroy locationManager is Null CANNOT Get Current Android POSITION");
          return;
        }else {
          locationManager.stopNotify();
        }
      }catch (Exception e){
        Utils.toLog(TAG, "onDestroy",null,e);
      }catch (Error er){
        Utils.toLog(TAG, "onDestroy",er,null);
      }
    }
  }

  public synchronized void sendJoystickData(float leftY, float leftX, float rightY, float rightX, final @NonNull OnAsyncOperationComplete callback){
//    var radians: Float = 0.0
//    let velocity: Float = 0.1
//    var x: Float = 0.0
//    var y: Float = 0.0
//    var z: Float = 0.0
//    var yaw: Float = 0.0
//    var yawSpeed: Float = 30.0
//    var throttle: Float = 0.0
//    var roll: Float = 0.0
//    var pitch: Float = 0.0
    if (Utils.isNull(callback)){
      toLog("sendJoystickData callback callback is Null");
      return;
    }else {
      try {
        String errorMSg = "";
        if (!isJoystickEnable()){
          errorMSg ="Joystick Is not enable ----------->PLEASE ENABLE JOYSTICK <-----------";
          toLog("sendJoystickData "+errorMSg);
          callback.onError(errorMSg);
          return;
        }else {
          if (!isValid(false,true, null)){
            toLog("sendJoystickData Stop cause isValid IS FALSE ");
            callback.onError("Parameter not valid  ----------->PLEASE RECONNECT DRONE <-----------");
            return;
          }else {
            if (Utils.isNull(mFlightController)){
              toLog("sendJoystickData mFlightController Is Null or No Connected 3----------->PLEASE RECONNECT DRONE <-----------");
              callback.onError(" mFlightController Is Null or No Connected ----------->PLEASE RECONNECT DRONE <-----------");
              return;
            }else {
              float pitch = 0.0f;
              float yawSpeed = 30.0f;
              float throttle = 0.0f;
              float roll =  0.0f;
              float yaw = (float) leftX * yawSpeed;

              throttle = ((float) leftY) * 5.0f * -1.0f; // inverting joystick for throttle

              pitch = ((float)rightY) * 1.0f;
              roll = ((float)rightX) * 1.0f;

              toLog(" sendJoystickData  yaw:"+Utils.clone(yaw )+ " roll:"+Utils.clone(roll) +" pitch "+Utils.clone(pitch) + " throttle:"+Utils.clone(throttle)+" yawSpeed "+Utils.clone(yawSpeed ));

              FlightControlData data = new FlightControlData(pitch,  roll, yaw, throttle);
              // data.setPitch();
              if (Utils.isNull(data)){
                toLog("sendJoystickData data Is Null ");
                callback.onError(" FlightControlData error ----------->PLEASE RETRY OR RESTART APP <-----------");
                return;
              }else {
                mFlightController.sendVirtualStickFlightControlData(data, (DJIError error) -> {
                  if (Utils.isNull(error)){
                    callback.onSucces("");
                    return;
                  }else {
                    String description = error.getDescription();
                    if (Utils.isNull(description)){
                      description = "";
                    }
                    toLog("sendJoystickData Error sending Joystick command "+description);
                    callback.onError(" Error sending Joystick command  ----------->PLEASE RETRY LATER <-----------");
                    return;
                  }
                });
              }
            }
          }
        }
      }catch (Exception e){
        Utils.toLog(TAG, "sendJoystickData",null,e);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("Error");
        }
      }catch (Error er){
        Utils.toLog(TAG, "sendJoystickData",er,null);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("Error");
        }
      }

    }

  }

  public synchronized void enableJoystickCommand(final @NonNull OnAsyncOperationComplete callback){
    if (Utils.isNull(callback)){
      toLog("enableJoystickCommand callback callback is Null");
      return;
    }else {
      try {
        if (!isValid(false, null,null)){
          toLog("enableJoystickCommand callback callback is Null");
          callback.onError("Parameter not valid  ----------->PLEASE RECONNECT DRONE <-----------");
          return;
        }else {
          if (Utils.isNull(mFlightController)){
            toLog("enableJostickCommand mFlightController Is Null or No Connected 2----------->PLEASE RECONNECT DRONE <-----------");
            callback.onError(" mFlightController Is Null or No Connected ----------->PLEASE RECONNECT DRONE <-----------");
            return;
          }else {
            if(isJoystickEnable()){
              toLog("enableJostickCommand isJoystickEnable ALREADY ENABLE");
              callback.onSucces("");
              return;
            }else {
              setJoystickEnable(false);
              connect(new OnAsyncOperationComplete() {
                @Override
                public void onError(@Nullable String errorDetail) {
                  setJoystickEnable(false);
                  if (Utils.isNull(errorDetail)){
                    errorDetail = "";
                  }
                  toLog("enableJostickCommand connect onError CAN NOT CONNETC TO DRONE "+errorDetail);
                  callback.onError("cannot connect to drone  ----------->PLEASE RECONNECT DRONE <-----------"+errorDetail);
                  return;
                }

                @Override
                public void onSucces(@Nullable String succesMsg) {
                  mFlightController.setVirtualStickModeEnabled(true, (DJIError error) -> {
                    if (Utils.isNull(error)){
                      setJoystickEnable(true);
                      callback.onSucces("");
                      return;
                    }else {
                      String description = error.getDescription();
                      if (Utils.isNull(description)){
                        description = "";
                      }
                      setJoystickEnable(false);
                      toLog("enableJostickCommand Failed Error Description:"+description);
                      callback.onError("enable joystick Failed Error detail: "+description);
                    }
                  });
                }
              });
            }
          }

        }
      }catch (Exception e){
        Utils.toLog(TAG, "enableJoystickCommand",null,e);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("Error");
        }
      }catch (Error er){
        Utils.toLog(TAG, "enableJoystickCommand",er,null);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("Error");
        }
      }
    }
  }

  public synchronized void disableJoystickCommand(final @NonNull OnAsyncOperationComplete callback){
    if (Utils.isNull(callback)){
      toLog("disableJoystickCommand callback callback is Null");
      return;
    }else {
      try {
        if (!isValid(false, null,null)){
          toLog("disableJoystickCommand callback callback is Null");
          callback.onError("Parameter not valid  ----------->PLEASE RECONNECT DRONE <-----------");
          return;
        }else {
          if (Utils.isNull(mFlightController)){
            toLog("disableJoystickCommand  mFlightController Is Null or No Connected 2----------->PLEASE RECONNECT DRONE <-----------");
            callback.onError(" mFlightController Is Null or No Connected ----------->PLEASE RECONNECT DRONE <-----------");
            return;
          }else {
            if(!isJoystickEnable()){
              toLog("disableJoystickCommand ALREADY DISABLED");
              callback.onSucces("");
              return;
            }else {
              connect(new OnAsyncOperationComplete() {
                @Override
                public void onError(@Nullable String errorDetail) {
                  setJoystickEnable(false);
                  if (Utils.isNull(errorDetail)){
                    errorDetail = "";
                  }
                  toLog("disableJoystickCommand connect onError CAN NOT CONNETC TO DRONE "+errorDetail);
                  callback.onError("cannot connect to drone  ----------->PLEASE RECONNECT DRONE <-----------"+errorDetail);
                  return;
                }

                @Override
                public void onSucces(@Nullable String succesMsg) {
                  mFlightController.setVirtualStickModeEnabled(false, (DJIError error) -> {
                    if (Utils.isNull(error)){
                      setJoystickEnable(false);
                      callback.onSucces("");
                    }else {
                      String description = error.getDescription();
                      if (Utils.isNull(description)){
                        description = "";
                      }
                      setJoystickEnable(true);
                      toLog("disableJoystickCommand Failed Error Description:"+description);
                      callback.onError("disable joystick Failed Error detail: "+description);
                    }
                  });
                }
              });
            }
          }
        }
      }catch (Exception e){
        Utils.toLog(TAG, "disableJoystickCommand",null,e);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("Error");
        }
      }catch (Error er){
        Utils.toLog(TAG, "disableJoystickCommand",er,null);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("Error");
        }
      }
    }
  }

  @Override
  public synchronized void takeOff(final @NonNull OnAsyncOperationComplete callback) {
    if (Utils.isNull(callback)){
      toLog("takeOff callback is Null");
      return;
    }else {
      try {
        if (!isValid(true,null,true)){
          toLog("takeOff Parameter not valid");
          callback.onError("Parameter not valid  ----------->PLEASE RECONNECT DRONE <-----------");
          return;
        }else {
          FlightControllerKey keyTakeOff = FlightControllerKey.create(FlightControllerKey.TAKE_OFF);
          if (Utils.isNull(keyTakeOff)){
            toLog("takeOff keyTakeOff is Null");
            callback.onError("keyTakeOff is Null  ----------->CANNOT PERFORM TAKE OFF<-----------");
            return;
          }else {
            performDroneAction(keyTakeOff,callback);
          }
        }
      }catch (Exception e){
        Utils.toLog(TAG, "takeOff",null,e);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("Error");
        }
      }catch (Error er){
        Utils.toLog(TAG, "takeOff",er,null);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("Error");
        }
      }
    }
  }

  @Override
  public synchronized void landing(final @NonNull OnAsyncOperationComplete callback) {
    if (Utils.isNull(callback)){
      toLog("landing callback is Null");
      return;
    }else {
      try {
        if (!isValid(true,null,true)){
          toLog("landing Parameter not valid");
          callback.onError("Parameter not valid  ----------->PLEASE RECONNECT DRONE <-----------");
          return;
        }else {
          if (Utils.isNull(mFlightController)){
            toLog("mFlightController is Null");
            callback.onError("mFlightController is Null  ----------->PLEASE RECONNECT DRONE <-----------");
            return;
          }else {
            mFlightController.startLanding((DJIError djiError) ->{
              if (djiError == null){
                toLog("startLanding  onResult LANDINg SUCCES");
                callback.onSucces("");
                return;
              }else {
                String description = djiError.getDescription();
                if (Utils.isNull(description)){
                  description = "";
                }
                toLog("startLanding  onResult Error Happen  "+ description);
                callback.onError("startLanding Failed "+description);
                return;
              }
            });
          }
        }
      }catch (Exception e){
        Utils.toLog(TAG, "landing",null,e);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("Error");
        }
      }catch (Error er){
        Utils.toLog(TAG, "landing",er,null);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("Error");
        }
      }
    }
  }

  @Override
  public synchronized void returnToHome(final @NonNull OnAsyncOperationComplete callback) {
    if (Utils.isNull(callback)){
      toLog("returnToHome callback is Null");
      return;
    }else {
      try {
        Runnable actionReturn = ()->{
          if (!isValid(true,null,true)){
            toLog("returnToHome Parameter not valid");
            callback.onError("Parameter not valid  ----------->PLEASE RECONNECT DRONE <-----------");
            return;
          }else {
            FlightControllerKey keyGoHome = FlightControllerKey.create(FlightControllerKey.START_GO_HOME);
            if (Utils.isNull(keyGoHome)){
              toLog("returnToHome keyGoHome is Null");
              callback.onError("keyGoHome is Null  ----------->CANNOT PERFORM GO HOME<-----------");
              return;
            }else {
              performDroneAction(keyGoHome,callback);
            }
          }
        };
        // Case Mission occcur
        if (!Utils.isNull(mission)){
          // Cancel all Next Step
          mission.cancelCurrentCauseGoHome(getContext(),new OnAsyncOperationComplete() {
            @Override
            public void onError(@Nullable String errorDetail) {
              toLog("returnToHome Cannot Stop Current Mission");
              callback.onError("returnToHome Cannot Stop Current Mission  ----------->CANNOT PERFORM GO HOME<-----------");
              return;
            }

            @Override
            public void onSucces(@Nullable String succesMsg) {
              actionReturn.run();
            }
          });
        }else {
          // Not Cartho Mission
          actionReturn.run();
        }

      }catch (Exception e){
        Utils.toLog(TAG, "returnToHome",null,e);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("Error");
        }
      }catch (Error er){
        Utils.toLog(TAG, "returnToHome",er,null);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("Error");
        }
      }
    }

  }

  private  void performDroneAction(@NonNull FlightControllerKey keyGoHome, @NonNull OnAsyncOperationComplete callback) {
    synchronized (this){
      try {
        if (Utils.isNull(callback)){
          toLog("performDroneAction callback is Null");
          return;
        }else {
          if (Utils.isNull(callback)){
            toLog("performDroneAction keyGoHome is Null");
            callback.onError("keyGoHome is Null 2 ----------->CANNOT PERFORM ACTION<-----------");
            return;
          }else {
            if (KeyManager.getInstance() != null) {
              KeyManager.getInstance().performAction(keyGoHome, new ActionCallback() {
                public void onSuccess() {
                  callback.onSucces("");
                }

                public void onFailure(@NonNull DJIError error) {
                  if (Utils.isNull(error)){
                    callback.onError("keyGoHome Error ");
                    return;
                  }else {
                    String des = error.getDescription();
                    if (Utils.isNull(des)){
                      des = "";
                    }
                    callback.onError(des);
                  }
                }
              }, new Object[0]);
            }else {
              toLog("performDroneAction KeyManager.getInstance() is Null");
              callback.onError("KeyManager instance is Null ----------->CANNOT PERFORM ACTION<-----------");
              return;
            }
          }
        }
      }catch (Exception e){
        Utils.toLog(TAG, "performDroneAction",null,e);
      }catch (Error er){
        Utils.toLog(TAG, "performDroneAction",er,null);
      }
    }
  }

  public static boolean isModelSupported(@NonNull Model model)throws Exception,Error{
    if (Utils.isNull(model)){
      return false;
    }else {
      if (model != Model.PHANTOM_4 && model != Model.PHANTOM_4_ADVANCED){
        Utils.toLog("DroneDJI"," Drone Model Not Supported ----------->PLEASE USE PHONTOM 4 ADVANCED OR ADD CAMERA SETTING FOR NEW DRONE <-----------");
        return false;
      }else {
        return true;
      }
    }

  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public synchronized void performCheckList(boolean caseContinuMission, @NonNull final Activity activity, @NonNull final OnCheckListChangeListener callback) {
    toLog("performCheckList START");
    if (Utils.isNull(callback)){
      toLog("performCheckList callback is Null");
      return;
    }else {
      resetMemoryMission(true);
      try{
        if (Utils.isNull(activity)){
          toLog(" performCheckList activity is null");
          callback.onError(" performCheckList activity is null");
          return;
        }else {
          if (activity.isDestroyed()){
            toLog(" performCheckList activity is isDestroyed");
            callback.onError(" performCheckList activity is isDestroyed");
            return;
          }else {
            int batteryPct = Utils.getBatteryPercent(activity);
            if (batteryPct <= Constants.PHONE_LOW_BATTERY_LEVEL){
              toLog("performCheckList stop CAUSE DRONE areMotorsOn ");
              callback.onPhoneBatterieStep(false);
              return;
            }else {
              callback.onPhoneBatterieStep(true);
              if (getDontTakeOff() == true){
                toLog(" performCheckList DRONE NOT READY PLEASE WAIT.");
                callback.onAccesStep(false);
                return;
              }else {
                if (!isValid(true,null,true)){
                  toLog("performCheckList Parameter not valid");
                  callback.onAccesStep(false);
                  return;
                }else {
                  callback.onAccesStep(true);
                  if (Utils.isNull(droneState)){
                    toLog(" performCheckList droneState isNull");
                    callback.onDroneStep(false);
                    return;
                  }else {
                    if (Utils.isNull(mFlightController)){
                      toLog("performCheckList mFlightController is Null");
                      callback.onDroneStep(false);
                      return;
                    }else {
                      if (Utils.isNull(getAircraft())){
                        toLog("performCheckList getAircraft() is Null ----------->PLEASE RECONNECT DRONE <-----------");
                        callback.onDroneStep(false);
                        return;
                      }else{
                        DJISDKManager djiSDKManager = DJISDKManager.getInstance();
                        if (Utils.isNull(djiSDKManager)){
                          toLog("performCheckList djiSDKManager is Null");
                          callback.onDroneStep(false);
                          return;
                        }else {
                          KeyManager keyManager = djiSDKManager.getKeyManager();
                          if (Utils.isNull(keyManager)){
                            toLog("performCheckList keyManager is Null");
                            callback.onDroneStep(false);
                            return;
                          }else {
                            Model model = getAircraft().getModel();
                            if (Utils.isNull(model)){
                              toLog("performCheckList Drone Model is Null ----------->PLEASE RECONNECT DRONE <-----------");
                              callback.onDroneStep(false);
                              return;
                            }else {
                              if (!isModelSupported(model)){
                                toLog("performCheckList Drone Model Not Supported ----------->PLEASE USE PHONTOM 4 ADVANCED OR ADD CAMERA SETTING FOR NEW DRONE <-----------");
                                callback.onDroneStep(false);
                                return;
                              }else {
                                Runnable checkInMemory = () ->{
                                  try {
                                    if (Utils.isNull(inMemoryMissionSetting)){
                                      toLog("performCheckList stop inMemoryMissionSetting Is Null");
                                      callback.onDroneStep(false);
                                      return;
                                    }else {
                                      if (!inMemoryMissionSetting.isValid(null)){
                                        toLog("performCheckList stop inMemoryMissionSetting Is not valid");
                                        callback.onDroneStep(false);
                                        return;
                                      }else {
                                        if (Utils.isNull(inMemoryMissionStep)){
                                          toLog("performCheckList stop inMemoryMissionStep Is Null");
                                          callback.onDroneStep(false);
                                          return;
                                        }else {
                                          if (!inMemoryMissionStep.isValid()){
                                            toLog("performCheckList stop inMemoryMissionStep Is Not valid");
                                            callback.onDroneStep(false);
                                            return;
                                          }else {
                                            if (Utils.isNull(inMemoryMissionSetting.getHomeLocation())){
                                              toLog(" startWaypointMission missionSetting.HomeLocation is Null");
                                              callback.onDroneStep(false);
                                              return;
                                            }else {
                                              FlightControllerState currentState = mFlightController.getState();
                                              if (Utils.isNull(currentState)){
                                                toLog("performCheckList currentState is Null");
                                                callback.onDroneStep(false);
                                                return;
                                              }else {
                                                mFlightController.setSmartReturnToHomeEnabled(true, (DJIError returnToHome) -> {
                                                  if (!Utils.isNull(returnToHome)){
                                                    String detail = returnToHome.getDescription();
                                                    if (Utils.isNull(detail)){
                                                      detail = "";
                                                    }
                                                    toLog(" SET RTH (ReturnToHome) Enable failed detail: "+detail);
                                                    callback.onDroneStep(false);
                                                    return;
                                                  }else {
                                                    mFlightController.setLowBatteryWarningThreshold(Constants.LOW_BATTERY_LEVEL_WARNING, (DJIError lowBatteryWarning) -> {
                                                      if (!Utils.isNull(lowBatteryWarning)){
                                                        String detail = lowBatteryWarning.getDescription();
                                                        if (Utils.isNull(detail)){
                                                          detail = "";
                                                        }
                                                        toLog(" SET Low Battery Warning Threshold failed detail: "+detail);
                                                        callback.onDroneStep(false);
                                                        return;
                                                      }else {
                                                        mFlightController.setSeriousLowBatteryWarningThreshold(Constants.LOW_BATTERY_LEVEL_SERIOUS, (DJIError seriousLowBattery) -> {
                                                          if (!Utils.isNull(seriousLowBattery)){
                                                            String detail = seriousLowBattery.getDescription();
                                                            if (Utils.isNull(detail)){
                                                              detail = "";
                                                            }
                                                            toLog(" SET Serious Low Battery Warning Threshold failed detail: "+detail);
                                                            callback.onDroneStep(false);
                                                            return;
                                                          }else {
                                                            try {
                                                              callback.onDroneStep(true);
                                                              if (currentState.isFlying()){
                                                                toLog("performCheckList stop CAUSE DRONE isFlying ");
                                                                callback.onControleStep(false);
                                                                return;
                                                              }else {
                                                                if (currentState.areMotorsOn()){
                                                                  // try again
                                                                  toLog("performCheckList stop CAUSE DRONE areMotorsOn ");
                                                                  callback.onControleStep(false);
                                                                  return;
                                                                }else {
                                                                  callback.onControleStep(true);
                                                                  Camera camera =  getAircraft().getCamera();
                                                                  if (Utils.isNull(camera)){
                                                                    toLog("performCheckList camera is Null");
                                                                    callback.onCameraStep(false);
                                                                    return;
                                                                  }else {
                                                                    CameraKey sdCardKey = CameraKey.create(CameraKey.SDCARD_STATE);
                                                                    if (Utils.isNull(sdCardKey)){
                                                                      toLog("performCheckList sdCardKey is Null");
                                                                      callback.onSDCardStep(false);
                                                                      return;
                                                                    }else {
                                                                      if (!keyManager.isKeySupported(sdCardKey)){
                                                                        toLog("performCheckList sdCardKey is Not supported on this device");
                                                                        callback.onSDCardStep(false);
                                                                        return;
                                                                      }else {
                                                                        keyManager.getValue(sdCardKey, new GetCallback() {
                                                                          @Override
                                                                          public void onSuccess(@NonNull @NotNull Object responseObj) {
                                                                            try {
                                                                              if (Utils.isNull(responseObj)){
                                                                                toLog("performCheckList responseObj is Null");
                                                                                callback.onSDCardStep(false);
                                                                                return;
                                                                              }else {
                                                                                SettingsDefinitions.SDCardOperationState code = SettingsDefinitions.SDCardOperationState.valueOf(responseObj.toString());
                                                                                if (Utils.isNull(code)){
                                                                                  toLog("performCheckList code is Null");
                                                                                  callback.onSDCardStep(false);
                                                                                  return;
                                                                                }else {
                                                                                  if (code != SettingsDefinitions.SDCardOperationState.NORMAL){
                                                                                    toLog("performCheckList SDCard State is Not Normal");
                                                                                    toLog("performCheckList SDCard State should be Normal but it's:"+code.name());
                                                                                    callback.onSDCardStep(false);
                                                                                    return;
                                                                                  }else {
                                                                                    callback.onSDCardStep(true);

                                                                                    DiagnosticsKey diagKey = DiagnosticsKey.create(DiagnosticsKey.SYSTEM_STATUS);
                                                                                    if (Utils.isNull(diagKey)){
                                                                                      toLog("performCheckList diagKey is null");
                                                                                      callback.onDiagnosticStep("diagnostic not supported on this device");
                                                                                      return;
                                                                                    }else {
                                                                                      if (!keyManager.isKeySupported(diagKey)){
                                                                                        toLog("performCheckList sdCardKey is Not supported on this device");
                                                                                        callback.onDiagnosticStep("diagnostic not supported on this device");
                                                                                        return;
                                                                                      }else {
                                                                                        keyManager.getValue(diagKey, new GetCallback() {
                                                                                          @Override
                                                                                          public void onSuccess(@NonNull @NotNull Object response) {
                                                                                            try {
                                                                                              if (Utils.isNull(response)){
                                                                                                response = "";
                                                                                              }
                                                                                              String result = response.toString();
                                                                                              if ((!result.trim().contentEquals(Constants.STATUS_READY_GO)) && (!result.trim().contentEquals(Constants.STATUS_READY_GO_2))){
                                                                                                toLog("performCheckList diagnostic failed Must be  ready to go But it's:"+result);
                                                                                                callback.onDiagnosticStep(result);
                                                                                                return;
                                                                                              }else {
                                                                                                // Succes
                                                                                                callback.onDiagnosticStep(result);

                                                                                                configShutterSpeedAndAngle(new OnMissionRunningListener() {
                                                                                                  @Override
                                                                                                  public void onError(@Nullable @org.jetbrains.annotations.Nullable String errorDetail) {
                                                                                                    toLog("performCheckList configShutterSpeedAndAngle failed");
                                                                                                    callback.onCameraStep(false);
                                                                                                    return;
                                                                                                  }

                                                                                                  @Override
                                                                                                  public void onCalibrationRequire(@Nullable @org.jetbrains.annotations.Nullable String detail) {
                                                                                                    // Will not fired
                                                                                                  }

                                                                                                  @Override
                                                                                                  public void onStartSucces() {
                                                                                                    // Will not fired
                                                                                                  }

                                                                                                  @Override
                                                                                                  public void onNotifyStepRemain(int stepLeft) {
                                                                                                    // Will not fired
                                                                                                  }

                                                                                                  @Override
                                                                                                  public void onSucces() {
                                                                                                    // Will not fired
                                                                                                  }

                                                                                                  @Override
                                                                                                  public void onGridProgressing(int progress) {
                                                                                                    // Will not fired
                                                                                                  }

                                                                                                  @Override
                                                                                                  public void onNotifyState(int battery, int imageCount, float speed, CopyOnWriteArrayList<LatLng> flyPoints) {
                                                                                                    // Will not fired
                                                                                                  }

                                                                                                  @Override
                                                                                                  public void mustGoHomeLowBattery() {
                                                                                                    // Will not fired
                                                                                                  }

                                                                                                  @Override
                                                                                                  public void mustGoHomeError() {
                                                                                                    // Will not fired
                                                                                                  }

                                                                                                  @Override
                                                                                                  public void onEnableLoadingForNextMission() {
                                                                                                    // Will not fired
                                                                                                  }

                                                                                                  @Override
                                                                                                  public void onDisableLoadingForNextMission() {
                                                                                                    // Will not fired
                                                                                                  }
                                                                                                }, camera, inMemoryMissionSetting, () -> {
                                                                                                  // Set Camera Succes
                                                                                                  Runnable executeMission = ()->{
                                                                                                    try {
                                                                                                      callback.onCameraStep(true);
                                                                                                      mission =  new MissionDJI();
                                                                                                      Runnable checkMission = () ->{
                                                                                                        mission.performCheckList(inMemoryMissionStep, inMemoryMissionSetting, activity, droneState, new OnCheckListChangeListener() {
                                                                                                          @Override
                                                                                                          public void onPhoneBatterieStep(boolean isOK) {
                                                                                                            callback.onPhoneBatterieStep(isOK);
                                                                                                          }

                                                                                                          @Override
                                                                                                          public void onAccesStep(boolean isOK) {
                                                                                                            callback.onAccesStep(isOK);
                                                                                                          }

                                                                                                          @Override
                                                                                                          public void onDroneStep(boolean isOK) {
                                                                                                            callback.onDroneStep(isOK);
                                                                                                          }

                                                                                                          @Override
                                                                                                          public void onControleStep(boolean isOK) {
                                                                                                            callback.onControleStep(isOK);
                                                                                                          }

                                                                                                          @Override
                                                                                                          public void onCameraStep(boolean isOK) {
                                                                                                            callback.onCameraStep(isOK);
                                                                                                          }

                                                                                                          @Override
                                                                                                          public void onCameraCalibratedStep(boolean isOK) {
                                                                                                            callback.onCameraCalibratedStep(isOK);
                                                                                                          }

                                                                                                          @Override
                                                                                                          public void onSDCardStep(boolean isOK) {
                                                                                                            callback.onSDCardStep(isOK);
                                                                                                          }

                                                                                                          @Override
                                                                                                          public void onFlyPlanStep(boolean isOK) {
                                                                                                            callback.onFlyPlanStep(isOK);
                                                                                                          }

                                                                                                          @Override
                                                                                                          public void onBatterieStep(boolean isOK) {
                                                                                                            callback.onBatterieStep(isOK);
                                                                                                          }

                                                                                                          @Override
                                                                                                          public void onGPSStep(boolean isOK) {
                                                                                                            callback.onGPSStep(isOK);
                                                                                                          }

                                                                                                          @Override
                                                                                                          public void onDiagnosticStep(@NonNull @NotNull String msg) {
                                                                                                            callback.onDiagnosticStep(msg);
                                                                                                          }

                                                                                                          @Override
                                                                                                          public void onSucces() {
                                                                                                            checkListDone = true;
                                                                                                            callback.onSucces();
                                                                                                          }

                                                                                                          @Override
                                                                                                          public void onError(@Nullable String errorDetail) {
                                                                                                            checkListDone = null;
                                                                                                            callback.onError(errorDetail);
                                                                                                          }
                                                                                                        });
                                                                                                      };
                                                                                                      if (caseContinuMission){
                                                                                                        // Continu Mission
                                                                                                        checkMission.run();
                                                                                                        return;
                                                                                                      }else {
                                                                                                        // Case New Mission
                                                                                                        mission.resetMemory(null, true, activity, new OnAsyncOperationComplete() {
                                                                                                          @Override
                                                                                                          public void onError(@Nullable String errorDetail) {
                                                                                                            toLog("performCheckList resetMemory failed");
                                                                                                            callback.onFlyPlanStep(false);
                                                                                                            return;
                                                                                                          }

                                                                                                          @Override
                                                                                                          public void onSucces(@Nullable String succesMsg) {
                                                                                                            checkMission.run();
                                                                                                          }
                                                                                                        });
                                                                                                        return;
                                                                                                      }
                                                                                                    }catch (Exception e){
                                                                                                      Utils.toLog(TAG, "executeMission",null,e);
                                                                                                      if (Utils.isNull(callback)){
                                                                                                        return;
                                                                                                      }else {
                                                                                                        callback.onError("");
                                                                                                        return;
                                                                                                      }
                                                                                                    }catch (Error er){
                                                                                                      Utils.toLog(TAG, "executeMission",er,null);
                                                                                                      if (Utils.isNull(callback)){
                                                                                                        return;
                                                                                                      }else {
                                                                                                        callback.onError("");
                                                                                                        return;
                                                                                                      }
                                                                                                    }
                                                                                                  };
                                                                                                  if (!Utils.isNull(mission)){
                                                                                                    // May Be other mission occur
                                                                                                    mission.cancelCurrentCauseGoHome(activity,new OnAsyncOperationComplete() {
                                                                                                      @Override
                                                                                                      public void onError(@Nullable String errorDetail) {
                                                                                                        toLog("startWaypointMission  cancelCurrentCauseGoHome onError");
                                                                                                        callback.onError("startWaypointMission  cancelCurrentCauseGoHome onError");
                                                                                                        return;
                                                                                                      }

                                                                                                      @Override
                                                                                                      public void onSucces(@Nullable String succesMsg) {
                                                                                                        toLog("startWaypointMission cancelCurrentCauseGoHome onSucces");
                                                                                                        executeMission.run();
                                                                                                      }
                                                                                                    });
                                                                                                  }else {
                                                                                                    executeMission.run();
                                                                                                  }
                                                                                                });
                                                                                              }
                                                                                            }catch (Exception e){
                                                                                              Utils.toLog(TAG, "performCheckList 5",null,e);
                                                                                              if (Utils.isNull(callback)){
                                                                                                return;
                                                                                              }else {
                                                                                                callback.onError("performCheckList Error");
                                                                                              }
                                                                                            }catch (Error er){
                                                                                              Utils.toLog(TAG, "performCheckList 5",er,null);
                                                                                              if (Utils.isNull(callback)){
                                                                                                return;
                                                                                              }else {
                                                                                                callback.onError("performCheckList Error");
                                                                                              }
                                                                                            }
                                                                                          }

                                                                                          @Override
                                                                                          public void onFailure(@NonNull @NotNull DJIError djiError) {
                                                                                            String description = "";
                                                                                            if (!Utils.isNull(djiError)){
                                                                                              description = djiError.getDescription();
                                                                                              if (Utils.isNull(description)){
                                                                                                description = "";
                                                                                              }
                                                                                            }
                                                                                            toLog("performCheckList Diagnostics failed:"+description);
                                                                                            callback.onDiagnosticStep(description);
                                                                                            return;
                                                                                          }
                                                                                        });
                                                                                      }
                                                                                    }
                                                                                  }
                                                                                }
                                                                              }
                                                                            }catch (Exception e){
                                                                              Utils.toLog(TAG, "performCheckList 4",null,e);
                                                                              if (Utils.isNull(callback)){
                                                                                return;
                                                                              }else {
                                                                                callback.onError("performCheckList Error");
                                                                              }
                                                                            }catch (Error er){
                                                                              Utils.toLog(TAG, "performCheckList 4",er,null);
                                                                              if (Utils.isNull(callback)){
                                                                                return;
                                                                              }else {
                                                                                callback.onError("performCheckList Error");
                                                                              }
                                                                            }
                                                                          }

                                                                          @Override
                                                                          public void onFailure(@NonNull @NotNull DJIError sdError) {
                                                                            if (!Utils.isNull(sdError)){
                                                                              String detail = sdError.getDescription();
                                                                              if (Utils.isNull(detail)){
                                                                                detail = "";
                                                                              }
                                                                              toLog("performCheckList Get SDCARD state Failed can't know if is connected or full or empty:"+detail);
                                                                            }
                                                                            callback.onSDCardStep(false);
                                                                            return;
                                                                          }
                                                                        });
                                                                      }
                                                                    }
                                                                  }
                                                                }
                                                              }
                                                            }catch (Exception e){
                                                              Utils.toLog(TAG, "performCheckList 3",null,e);
                                                              if (Utils.isNull(callback)){
                                                                return;
                                                              }else {
                                                                callback.onError("");
                                                              }
                                                            }catch (Error er){
                                                              Utils.toLog(TAG, "performCheckList 3",er,null);
                                                              if (Utils.isNull(callback)){
                                                                return;
                                                              }else {
                                                                callback.onError("");
                                                              }
                                                            }
                                                          }
                                                        });
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
                                  }catch (Exception e){
                                    Utils.toLog(TAG, "performCheckList 2",null,e);
                                    if (Utils.isNull(callback)){
                                      return;
                                    }else {
                                      callback.onError("");
                                    }
                                  }catch (Error er){
                                    Utils.toLog(TAG, "performCheckList 2",er,null);
                                    if (Utils.isNull(callback)){
                                      return;
                                    }else {
                                      callback.onError("");
                                    }
                                  }
                                };
                                if (caseContinuMission){
                                  // Continu Mission
                                  // load MissionSetting,MissionStep in Memory
                                  MissionDJI newMission = new MissionDJI();
                                  newMission.loadMission(activity, new OnMissionProcessingComplete() {
                                    @Override
                                    public void onError() {
                                      toLog("performCheckList load existing failed");
                                      callback.onError("performCheckList load existing failed");
                                      return;
                                    }

                                    @Override
                                    public void onSucces(@NonNull MissionStep stepsLoaded) {
                                      if (Utils.isNull(stepsLoaded)){
                                        toLog("performCheckList stepsLoaded is Null");
                                        callback.onError("performCheckList Cannot load Saved stepsLoaded: Null");
                                        return;
                                      }else {
                                        stepsLoaded.to99And120Hect(null, null, new OnMissionSplitComplete() {
                                          @Override
                                          public void onError(@Nullable String errorDetail) {
                                            if (Utils.isNull(errorDetail)){
                                              errorDetail = "";
                                            }
                                            toLog("performCheckList to99And120Hect Split array Error:"+errorDetail);
                                            callback.onError("Split array Error: "+errorDetail);
                                            return;
                                          }

                                          @Override
                                          public void onPerformSaveAndContinu(MissionStep himSelf,@NonNull final CopyOnWriteArrayList<LatLng> flyPointsAfterRebuild) {
                                            // Will Not Fired cause saveInPref = null = false we will save
                                            // later
                                          }

                                          @Override
                                          public void onSucces(final MissionStep steps,@NonNull final CopyOnWriteArrayList<LatLng> flyPointsAfterRebuild) {
                                            if (Utils.isNull(steps)){
                                              toLog("performCheckList steps is Null");
                                              callback.onError("performCheckList Cannot load Saved steps: Null");
                                              return;
                                            }else {
                                              if (!steps.hasNext()){
                                                toLog("performCheckList steps is EMPTY");
                                                callback.onError("performCheckList Cannot load Saved steps: EMPTY");
                                                return;
                                              }else {
                                                Utils.AsyncExecute(()->{
                                                  try {
                                                    String data = Save.defaultLoadString(Constants.PREF_WAYPOINT_PARAMETER,activity);
                                                    if (Utils.isNullOrEmpty(data)){
                                                      toLog("performCheckList data is EMPTY or data is Nulll");
                                                      callback.onError("performCheckList Cannot load Saved steps: Parameters is empty or Null");
                                                      return;
                                                    }else {
                                                      MissionSetting setting = new MissionSetting(data);
                                                      if (!setting.isValid(true)){
                                                        toLog("performCheckList Remove Me : "+data);
                                                        toLog("performCheckList MissionSetting is not Valid Data: "+setting.getErrorMsg() );
                                                        callback.onError("performCheckList Cannot load Saved steps: Parameters is Not valid:"+setting.getErrorMsg());
                                                        return;
                                                      }else {
                                                        // Load in Memory
                                                        inMemoryMissionSetting = setting;
                                                        inMemoryMissionStep = steps;
                                                        checkInMemory.run();
                                                        return;
                                                      }
                                                    }
                                                  }catch (Exception e){
                                                    Utils.toLog(TAG, "performCheckList",null,e);
                                                    if (Utils.isNull(callback)){
                                                      return;
                                                    }else {
                                                      callback.onError("");
                                                    }
                                                  }catch (Error er){
                                                    Utils.toLog(TAG, "performCheckList",er,null);
                                                    if (Utils.isNull(callback)){
                                                      return;
                                                    }else {
                                                      callback.onError("");
                                                    }
                                                  }
                                                });

                                              }
                                            }
                                          }
                                        });

                                      }
                                    }
                                  });
                                }else {
                                  checkInMemory.run();
                                  return;
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
        Utils.toLog(TAG, "performCheckList 1",null,e);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("");
        }
      }catch (Error er){
        Utils.toLog(TAG, "performCheckList 1",er,null);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("");
        }
      }
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public synchronized void startWaypointMission(@NonNull final Activity activity, final @NonNull OnMissionRunningListener callback) {
    toLog(" startWaypointMission start");
    if (Utils.isNull(callback)){
      toLog("startWaypointMission callback is Null");
      return;
    }else {
      try {
        if (Utils.isNull(activity)){
          toLog(" startWaypointMission activity is null");
          callback.onError(" startWaypointMission activity is null");
          return;
        }else {
          if(Utils.isNull(checkListDone)){
            checkListDone = false;
          }
          if (!checkListDone){
            toLog(" startWaypointMission DRONE NOT READY PLEASE EXECUTE CHECKLIST FIRST.");
            callback.onError("DRONE NOT READY PLEASE EXECUTE CHECKLIST FIRST.");
            return;
          }else {
            if (Utils.isNull(inMemoryMissionStep)){
              toLog(" startWaypointMission inMemoryMissionStep is NULL NO MISSION FOUND PLEASE RUN getMissionInfo FIRST");
              callback.onError("NO MISSION FOUND PLEASE RUN getMissionInfo FIRST");
              return;
            }else {
              if (Utils.isNull(inMemoryMissionSetting)){
                toLog(" startWaypointMission inMemoryMissionSetting is NULL  NO MISSION FOUND PLEASE RUN getMissionInfo FIRST");
                callback.onError("NO MISSION FOUND PLEASE RUN getMissionInfo FIRST");
                return;
              }else {
                if (!inMemoryMissionStep.isValid()){
                  toLog(" startWaypointMission inMemoryMissionStep is NOT VALID NO MISSION FOUND PLEASE RUN getMissionInfo FIRST");
                  callback.onError("NO VALID MISSION FOUND PLEASE RUN getMissionInfo FIRST");
                  return;
                }else {
                  if (!inMemoryMissionSetting.isValid(true)){
                    toLog(" startWaypointMission inMemoryMissionSetting is NOT VALID NO MISSION FOUND PLEASE RUN getMissionInfo FIRST");
                    callback.onError("NO VALID MISSION FOUND PLEASE RUN getMissionInfo FIRST");
                    return;
                  }else {
                    MissionSetting missionSetting = inMemoryMissionSetting;
                    MissionStep step = inMemoryMissionStep;

                    if (getDontTakeOff() == true){
                      toLog(" startWaypointMission DRONE NOT READY PLEASE WAIT.");
                      callback.onError("DRONE NOT READY PLEASE WAIT.");
                      return;
                    }else {
                      if (Utils.isNull(missionSetting)){
                        toLog(" startWaypointMission Mission Setting isNull");
                        callback.onError(" startWaypointMission Mission Setting isNull");
                        return;
                      }else {
                        if (Utils.isNull(droneState)){
                          toLog(" startWaypointMission droneState isNull");
                          callback.onError(" Mission Telemetry is Null");
                          return;
                        }else {
                          if (!missionSetting.isValid(null)){
                            String msg = " startWaypointMission "+missionSetting.getErrorMsg();
                            toLog(msg);
                            callback.onError(msg);
                            return;
                          }else {
                            if (!isValid(true,null,true)){
                              toLog("startWaypointMission Parameter not valid");
                              callback.onError("Parameter not valid  ----------->PLEASE RECONNECT DRONE AND SET SET GO BACK HOME LOCATION");
                              return;
                            }else {
                              if (Utils.isNull(mFlightController)){
                                toLog("startWaypointMission mFlightController is Null");
                                callback.onError("mFlightController is Null  ----------->PLEASE RECONNECT DRONE <-----------");
                                return;
                              }else {
                                FlightControllerState currentState = mFlightController.getState();
                                if (Utils.isNull(currentState)){
                                  toLog("startWaypointMission currentState is Null");
                                  callback.onError(" currentState is Null");
                                  return;
                                }else {
                                  if (currentState.isFlying()){
                                    toLog("startWaypointMission stop CAUSE DRONE isFlying ");
                                    callback.onError("DRONE IS FLYING READY PLEASE WAIT.");
                                    return;
                                  }else {
                                    if (mFlightController.getState().areMotorsOn()){
                                      // try again
                                      toLog("startWaypointMission stop CAUSE DRONE areMotorsOn ");
                                      callback.onError("DRONE NOT READY PLEASE WAIT.");
                                      return;
                                    }else {
                                      Runnable executeMission = ()->{
                                        try {
                                          mission =  new MissionDJI();
                                          mission.startNewMission(activity,missionSetting,step,droneState,callback);
                                        }catch (Exception e){
                                          Utils.toLog(TAG, "executeMission",null,e);
                                          if (Utils.isNull(callback)){
                                            return;
                                          }else {
                                            callback.onError("executeMission Error");
                                          }
                                        }catch (Error er){
                                          Utils.toLog(TAG, "executeMission",er,null);
                                          if (Utils.isNull(callback)){
                                            return;
                                          }else {
                                            callback.onError("executeMission Error");
                                          }
                                        }
                                      };
                                      if (!Utils.isNull(mission)){
                                        // May Be other mission occur
                                        mission.cancelCurrentCauseGoHome(activity,new OnAsyncOperationComplete() {
                                          @Override
                                          public void onError(@Nullable String errorDetail) {
                                            toLog("startWaypointMission  cancelCurrentCauseGoHome onError");
                                            callback.onError("startWaypointMission  cancelCurrentCauseGoHome onError");
                                            return;
                                          }

                                          @Override
                                          public void onSucces(@Nullable String succesMsg) {
                                            toLog("startWaypointMission cancelCurrentCauseGoHome onSucces");
                                            executeMission.run();
                                          }
                                        });
                                      }else {
                                        executeMission.run();
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
        Utils.toLog(TAG, "startWaypointMission",null,e);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("");
        }
      }catch (Error er){
        Utils.toLog(TAG, "startWaypointMission",er,null);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("");
        }
      }
    }
  }

  private void configShutterSpeedAndAngle(@NonNull OnMissionRunningListener callback,@NonNull Camera camera,@NonNull MissionSetting mSetting, Runnable actionContinu)throws Exception,Error {
    if (Utils.isNull(callback)){
      return;
    }else {
      if (Utils.isNull(mSetting)){
        toLog("configShutterSpeedAndAngle mSetting Is Null CANNOT SET DAYLIGHTING ");
        callback.onError("error: mSetting Is Null CANNOT SET DAYLIGHTING");
        return;
      }else {
        if (Utils.isNull(camera)){
          toLog("configShutterSpeedAndAngle Camera Is Null ");
          callback.onError("error: CANNOT config Camera cause Object is Null");
          return;
        }else {
          if (Utils.isNull(actionContinu)){
            toLog("configShutterSpeedAndAngle actionContinu Is Null ");
            callback.onError("error: CANNOT config Camera cause actionContinu is Null");
            return;
          }else {
            camera.setExposureMode(SettingsDefinitions.ExposureMode.SHUTTER_PRIORITY, (DJIError exposureModeError) -> {
              if (!Utils.isNull(exposureModeError)){
                String detail = exposureModeError.getDescription();
                if (Utils.isNull(detail)){
                  detail = "";
                }
                toLog(" error: CANNOT SET ExposureMode TO SHUTTER_PRIORITY detail: "+detail);
                callback.onError("error: CANNOT SET ExposureMode TO SHUTTER_PRIORITY   ----------->DRONE MAY BE NOT SUPPORTED <-----------");
                return;
              }else {
                toLog("GIMBAL Rotation setExposureMode Succes");
                int daylighting = mSetting.getLighting();
                SettingsDefinitions.WhiteBalancePreset balancePreset = null;
                SettingsDefinitions.ShutterSpeed  shutterSpeed = null;
                if (daylighting == Constants.DAYSLIGHT_SUNNY){
                  // Case Sunny
                  balancePreset = SettingsDefinitions.WhiteBalancePreset.SUNNY;
                  shutterSpeed = SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_400;
                }else {
                  // Case Normal
                  balancePreset = SettingsDefinitions.WhiteBalancePreset.WATER_SURFACE;
                  shutterSpeed = SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_800;
                }
                SettingsDefinitions.ShutterSpeed finalShutterSpeed = shutterSpeed;
                camera.setWhiteBalance(new WhiteBalance(balancePreset), new CommonCallbacks.CompletionCallback() {
                  @Override
                  public void onResult(DJIError whiteBalanceError) {
                    if (!Utils.isNull(whiteBalanceError)){
                      String detail = whiteBalanceError.getDescription();
                      if (Utils.isNull(detail)){
                        detail = "";
                      }
                      toLog(" error: CANNOT SET WhiteBalance detail: "+detail);
                      callback.onError("error: CANNOT SET WhiteBalance   ----------->DRONE MAY BE NOT SUPPORTED <-----------");
                      return;
                    }else {
                      // SUNNy  =Sunny + ShutterSpeed.SHUTTER_SPEED_1_400
                      // camera.setShutterSpeed(SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_800, shutterSpeedError -> {
                      // SUNNy camera.setShutterSpeed(SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_400, shutterSpeedError -> {
                      camera.setShutterSpeed(finalShutterSpeed, shutterSpeedError -> {
                        if (!Utils.isNull(shutterSpeedError)){
                          String detail = shutterSpeedError.getDescription();
                          if (Utils.isNull(detail)){
                            detail = "";
                          }
                          toLog(" error: CANNOT SET ShutterSpeed TO SHUTTER_PRIORITY detail: "+detail);
                          callback.onError("error: CANNOT SET ShutterSpeed   ----------->DRONE MAY BE NOT SUPPORTED <-----------");
                          return;
                        }else {
                          camera.setPhotoFileFormat(SettingsDefinitions.PhotoFileFormat.JPEG, (@Nullable DJIError cameraError) -> {
                            if (!Utils.isNull(cameraError)){
                              String detail = cameraError.getDescription();
                              if (Utils.isNull(detail)){
                                detail = "";
                              }
                              toLog("startWaypointMission camera setPhotoFileFormat Error Happen: "+detail);
                              callback.onError("Cannot set Photo file format to JPEG Please Retry later");
                              return;
                            }else {
                              if (Utils.isNull(getAircraft())){
                                toLog(" getAircraft() is Null ----------->PLEASE RECONNECT DRONE <-----------");
                                callback.onError(" getAircraft() is Null ----------->PLEASE RECONNECT DRONE <-----------");
                                return;
                              }else {
                                Gimbal gimbal = getAircraft().getGimbal();
                                if (Utils.isNull(gimbal)){
                                  toLog("gimbal is Null");
                                  callback.onError("gimbal is Null  ----------->PLEASE RECONNECT DRONE <-----------");
                                  return;
                                }else {
                                  gimbal.rotate(new Rotation.Builder()
                                          .pitch(-90)
                                          .mode(RotationMode.ABSOLUTE_ANGLE)
                                          .yaw(Rotation.NO_ROTATION)
                                          .roll(Rotation.NO_ROTATION)
                                          .time(0)
                                          .build(), rotateError -> {
                                    if (!Utils.isNull(rotateError)){
                                      String detail = rotateError.getDescription();
                                      if (Utils.isNull(detail)){
                                        detail = "";
                                      }
                                      toLog("CANNOT move GIMBAL to Angle ");
                                      callback.onError("CANNOT move GIMBAL to Angle   ----------->DRONE MAY BE NOT SUPPORTED <-----------");
                                      return;
                                    }else {
                                      actionContinu.run();
                                    }
                                  });
                                }
                              }
                            }
                          });
                        }
                      });
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

  @RequiresApi(api = Build.VERSION_CODES.N)
  public synchronized void continuWaypointMission(@NonNull final Activity activity, final @NonNull OnMissionRunningListener callback) {
    toLog(" continuWaypointMission ");
    try {
      if (Utils.isNull(callback)){
        toLog("continuWaypointMission callback is Null");
        return;
      }else {
        if (Utils.isNull(activity)){
          toLog(" continuWaypointMission activity is null");
          callback.onError(" continuWaypointMission activity is null");
          return;
        }else {
          if(Utils.isNull(checkListDone)){
            checkListDone = false;
          }
          if (!checkListDone){
            toLog(" startWaypointMission DRONE NOT READY PLEASE EXECUTE CHECKLIST FIRST.");
            callback.onError("DRONE NOT READY PLEASE EXECUTE CHECKLIST FIRST.");
            return;
          }else {
            if (Utils.isNull(inMemoryMissionStep)){
              toLog(" startWaypointMission inMemoryMissionStep is NULL NO MISSION FOUND PLEASE RUN getMissionInfo FIRST");
              callback.onError("NO MISSION FOUND PLEASE RUN getMissionInfo FIRST");
              return;
            }else {
              if (Utils.isNull(inMemoryMissionSetting)){
                toLog(" startWaypointMission inMemoryMissionSetting is NULL  NO MISSION FOUND PLEASE RUN getMissionInfo FIRST");
                callback.onError("NO MISSION FOUND PLEASE RUN getMissionInfo FIRST");
                return;
              }else {
                if (!inMemoryMissionStep.isValid()){
                  toLog(" startWaypointMission inMemoryMissionStep is NOT VALID NO MISSION FOUND PLEASE RUN getMissionInfo FIRST");
                  callback.onError("NO VALID MISSION FOUND PLEASE RUN getMissionInfo FIRST");
                  return;
                }else {
                  if (!inMemoryMissionSetting.isValid(true)){
                    toLog(" startWaypointMission inMemoryMissionSetting is NOT VALID NO MISSION FOUND PLEASE RUN getMissionInfo FIRST");
                    callback.onError("NO VALID MISSION FOUND PLEASE RUN getMissionInfo FIRST");
                    return;
                  }else {
                    MissionStep steps = inMemoryMissionStep;
                    MissionSetting setting = inMemoryMissionSetting;
                    if (getDontTakeOff() == true){
                      toLog(" continuWaypointMission DRONE NOT READY PLEASE WAIT.");
                      callback.onError("DRONE NOT READY PLEASE WAIT.");
                      return;
                    }else {
                      if (!isValid(true,null,true)){
                        toLog("continuWaypointMission Parameter not valid");
                        callback.onError("Parameter not valid  ----------->PLEASE RECONNECT DRONE THEN SET SET GO BACK HOME LOCATION AND LISTEN TELEMETRY");
                        return;
                      }else {
                        if (Utils.isNull(mFlightController)){
                          toLog("continuWaypointMission mFlightController is Null");
                          callback.onError("mFlightController is Null  ----------->PLEASE RECONNECT DRONE <-----------");
                          return;
                        }else {
                          FlightControllerState currentState = mFlightController.getState();
                          if (Utils.isNull(currentState)){
                            toLog("continuWaypointMission currentState is Null");
                            callback.onError(" currentState is Null");
                            return;
                          }else {
                            if (currentState.isFlying()){
                              toLog("continuWaypointMission stop CAUSE DRONE isFlying ");
                              callback.onError("DRONE IS FLYING READY PLEASE WAIT.");
                              return;
                            }else {
                              if (currentState.areMotorsOn()){
                                // try again
                                toLog("continuWaypointMission stop CAUSE DRONE areMotorsOn ");
                                callback.onError("DRONE NOT READY PLEASE WAIT.");
                                return;
                              }else {
                                if (Utils.isNull(droneState)){
                                  toLog(" continuWaypointMission droneState isNull");
                                  callback.onError(" Mission Telemetry is Null");
                                  return;
                                }else {
                                  Runnable actionContinu = () ->{
                                    mission = new MissionDJI();
                                    mission.continuMission(steps,setting,activity,droneState,callback);
                                  };
                                  if (!Utils.isNull(mission)){
                                    // May Be other mission occur
                                    mission.cancelCurrentCauseGoHome(activity,new OnAsyncOperationComplete() {
                                      @Override
                                      public void onError(@Nullable String errorDetail) {
                                        toLog("continuWaypointMission cancelCurrentCauseGoHome onError");
                                      }

                                      @Override
                                      public void onSucces(@Nullable String succesMsg) {
                                        toLog("continuWaypointMission cancelCurrentCauseGoHome onSucces");
                                        actionContinu.run();
                                      }
                                    });
                                  }else {
                                    actionContinu.run();
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
      Utils.toLog(TAG, "continuWaypointMission",null,e);
    }catch (Error er){
      Utils.toLog(TAG, "continuWaypointMission",er,null);
    }

  }

  @Override
  @RequiresApi(api = Build.VERSION_CODES.N)
  public void isMappingMissionExist(final Context ctx, OnAsyncOperationCompleteBool callback) {
    if (Utils.isNull(callback)){
      toLog("isMappingMissionExist callback is Null");
      return;
    }else {
      if (Utils.isNull(ctx)){
        toLog(" isMappingMissionExist context is null");
        callback.onResultNo(" isMappingMissionExist activity is null");
        return;
      }else {
        MissionDJI missionDJI =  new MissionDJI();
        missionDJI.isMappingMissionExist(ctx,callback);
      }
    }

  }

  @Override
  @RequiresApi(api = Build.VERSION_CODES.N)
  public void cleanUserData(Context ctx, OnAsyncOperationCompleteBool callback) {
    if (Utils.isNull(callback)){
      toLog("isMappingMissionExist callback is Null");
      return;
    }else {
      if (Utils.isNull(ctx)){
        toLog(" isMappingMissionExist context is null");
        callback.onResultNo(" isMappingMissionExist activity is null");
        return;
      }else {
        MissionDJI missionDJI =  new MissionDJI();
        resetMemoryMission(null);
        missionDJI.cleanUserData(ctx,callback);
      }
    }
  }


  public boolean isValidWithouRegister(){
    try {
      if (Utils.isNull(getContext())){
        return false;
      }else{
        if (getAircraft() == null) {
          toLog(" isValid getAircraft()  IS NULL ----------->PLEASE RECONNECT DRONE <-----------");
          return false;
        }else {
          if (!getAircraft().isConnected()){
            toLog(" isValid getAircraft() Is Not Connected  IS NULL ----------->PLEASE RECONNECT DRONE <-----------");
            return false;
          }else {
            // Try again
            if (Utils.isNull(mFlightController)){
              mFlightController = getAircraft().getFlightController();
            }
            if (Utils.isNull(mFlightController)){
              toLog(" mFlightController IS NULL ----------->PLEASE RECONNECT DRONE <-----------");
              return false;
            }else{
              if (getAircraft().getModel() == Model.UNKNOWN_AIRCRAFT) {
                toLog("isValid  getAircraft() Is UNKNOWN_AIRCRAFT ----------->PLEASE RECONNECT DRONE <-----------");
                return false;
              }else {
                PermissionManager pm =new PermissionManager(getContext());
                // If there is enough permission, we will start the registration
                if (!pm.isPermissionsGranted()){
                  toLog("Missing permissions!!!");
                  return false;
                }else {
                  return true;
                }

              }
            }

          }
        }
      }
    }catch (Exception e){
      Utils.toLog(TAG, "isValidWithouRegister",null,e);
    }catch (Error er){
      Utils.toLog(TAG, "isValidWithouRegister",er,null);
    }
    return false;
  }


  @Override
  public boolean isValid(boolean caseToFly,@Nullable Boolean caseJoystick,@Nullable Boolean landingOrTakeOff) {
    try {
      if (Utils.isNull(caseJoystick)){
        caseJoystick = false;
      }
      if (Utils.isNull(landingOrTakeOff)){
        landingOrTakeOff = false;
      }
      if (caseJoystick){
        if (!isValid(true,null, null)){
          toLog("isValid  caseJoystick  !isValid(true,null) IS FALSE");
          return false;
        }else {
          if (!isJoystickEnable()){
            toLog("isValid  caseJoystick  JoystickEnable IS NOT Enable");
            return false;
          }else {
            return true;
          }
        }
      }
      if (caseToFly){
        if (Utils.isNull(listener)){
          toLog("listener is Null");
          return false;
        }else {
          if (!isRegisted()){
            toLog("isValid  is Not registed");
            return false;
          }else {
            if (Utils.isNull(mFlightController)){
              toLog("mFlightController is Null ----------->PLEASE RECONNECT DRONE <-----------");
              return false;
            }else {
              if (landingOrTakeOff){
                return isValidWithouRegister();
              }else {
                if (!mFlightController.getState().isHomeLocationSet()){
                  toLog("isHomeLocationSet is False ----------->PLEASE RECONNECT DRONE <-----------");
                  return false;
                }else {
                  return isValidWithouRegister();
                }
              }

            }
          }
        }
      }else {
        return isValidWithouRegister();
      }
    }catch (Exception e){
      Utils.toLog(TAG, "isValid",null,e);
    }catch (Error er){
      Utils.toLog(TAG, "isValid",er,null);
    }
    return false;

  }

  public void attachBaseContext(@NonNull Context paramContext,@NonNull Application app){
    Utils.toLog(TAG, " attachBaseContext SetSUCCES");
  }

  public void onCreate() {

    Utils.toLog(TAG, " onCreate SUCCES");
  }



  private void refreshSDKAndFlightState() {
    try {
      synchronized (DroneDJI.this){
        toLog("refreshSDKAndFlightState Start");
        if (isAlreadyNotify()){
          toLog("refreshSDKAndFlightState Stop cause Not isAlreadyNotify");
          return;
        }else {
          if (Utils.isNull(getAircraft())){
            toLog("refreshSDKAndFlightState Stop cause getAircraft is Null ----------->PLEASE RECONNECT DRONE <-----------");
            setAlreadyNotify(false);
            return;
          }else {
            if (!isValid(false,null, null)){
              toLog("refreshSDKAndFlightState Stop cause Not valid");
              setAlreadyNotify(false);
              return;
            }else {
              Model model = getAircraft().getModel();
              if (Utils.isNull(model)){
                toLog("refreshSDKAndFlightState Stop cause model is Null ----------->PLEASE RECONNECT DRONE <-----------");
                setAlreadyNotify(false);
                return;
              }else {
                String displayName = getAircraft().getModel().getDisplayName();
                if (Utils.isNull(displayName)){
                  toLog("refreshSDKAndFlightState Stop cause displayName is Null ----------->PLEASE RECONNECT DRONE <-----------");
                  setAlreadyNotify(false);
                  return;
                }else {
                  setName(displayName);
                  setAlreadyNotify(true);
                  droneState = null;
                  droneState = new DroneState(getAircraft(), new OnStateChangeListener() {
                    @Override
                    public void onChange(int uplinkSignalQuality, short satelliteCount, int chargeRemainingInPercent, int chargeRemainingInMAh, float temperature, float altitudeInMeters, float speedInMeterPerSec, float heading, int numberOfDischarges, int lifetimeRemainingInPercent, double longitude, double latitude, float altitude, int droneHeadingInDegrees, @NonNull String droneModelName,float distanceInMeter) {
                      synchronized (DroneDJI.this){
                        try {
                          // Copy to Object
                          setLatitude(latitude);
                          setLongitude(longitude);
                          setAltitude(altitude);
                          setUplinkSignalQuality(uplinkSignalQuality);
                          setSatelliteCount(satelliteCount);
                          setChargeRemainingInPercent(chargeRemainingInPercent);
                          setChargeRemainingInMAh(chargeRemainingInMAh);
                          setTemperature(temperature);
                          setSpeedInMeterPerSec(speedInMeterPerSec);
                          // Notify Listener
                          if (Utils.isNull(listener)){
                            toLog("refreshSDKAndFlightState onChange stop cause listener is Null");
                            return;
                          }else {
                            listener.onChange(uplinkSignalQuality,satelliteCount,chargeRemainingInPercent,chargeRemainingInMAh,temperature,altitudeInMeters,speedInMeterPerSec, heading, numberOfDischarges, lifetimeRemainingInPercent, longitude,latitude,altitude,droneHeadingInDegrees,getName(),distanceInMeter);
                          }
                        }catch (Exception e){
                          Utils.toLog(TAG, "refreshSDKAndFlightState onChange",null,e);

                        }catch (Error er){
                          Utils.toLog(TAG, "refreshSDKAndFlightState onChange",er,null);
                        }
                      }
                    }

                    @Override
                    public void onLog(String msg) {
                      toLog(msg);
                    }

                    @Override
                    public void onConnectFailed() {
                      try {
                        if (Utils.isNull(listener)){
                          toLog("refreshSDKAndFlightState onChange stop cause listener is Null 2");
                          return;
                        }else {
                          listener.onConnectFailed();
                        }
                      }catch (Exception e){
                        Utils.toLog(TAG, "refreshSDKAndFlightState onChange",null,e);
                      }catch (Error er){
                        Utils.toLog(TAG, "refreshSDKAndFlightState onChange",er,null);
                      }

                    }

                    @Override
                    public void onConnectSucces() {
                      try {
                        if (Utils.isNull(listener)){
                          toLog("refreshSDKAndFlightState onChange stop cause listener is Null 2");
                          return;
                        }else {
                          // Will Not fired
                          listener.onConnectFailed();
                        }
                      }catch (Exception e){
                        Utils.toLog(TAG, "refreshSDKAndFlightState onChange",null,e);
                      }catch (Error er){
                        Utils.toLog(TAG, "refreshSDKAndFlightState onChange",er,null);
                      }
                    }
                  });
                  droneState.initStateListener(()->{

                  });


                }
              }

            }
          }
        }
      }
    }catch (Exception e){
      Utils.toLog(TAG, "refreshSDKAndFlightState",null,e);

    }catch (Error er){
      Utils.toLog(TAG, "refreshSDKAndFlightState",er,null);
    }

  }
  private void toLog( @NonNull String msg){
    Utils.toLog(TAG, msg);
  }


  private void goTo70MetersWaypoint(@NonNull final Activity activity,OnAsyncOperationComplete callback) {
    if (Utils.isNull(callback)){
      toLog("goTo70MetersWaypoint Stop cause callback");
      return;
    }else {
      try {
        if (Utils.isNull(activity)){
          callback.onError("Activity is Null is Null");
          return;
        }else {
          CopyOnWriteArrayList<LatLng> pointList = new Dummy().getPointsAsLatLng();
          WaypointMissionHeadingMode mHeadingMode = WaypointMissionHeadingMode.AUTO;
          float mSpeed = 10.0f;
          float ALTITUDE_PREFERED = 70.0f;

          if (Utils.isNull(pointList)){
            toLog("addPoints Stop cause pointList IS Null");
            callback.onError("pointList is Null");
            return;
          }else {
            for (LatLng point :pointList) {
              Waypoint mWaypoint = new Waypoint(point.latitude, point.longitude, ALTITUDE_PREFERED);
              waypointList.add(mWaypoint);
            }
            toLog("addPoints Succes");
          }

          WaypointMission.Builder waypointMissionBuilder;
          WaypointMissionFinishedAction mFinishedAction = WaypointMissionFinishedAction.GO_HOME;
          waypointMissionBuilder = new WaypointMission.Builder().finishedAction(mFinishedAction)
                  .headingMode(mHeadingMode)
                  .autoFlightSpeed(mSpeed)
                  .maxFlightSpeed(mSpeed)
                  .waypointList(waypointList)
                  .waypointCount(waypointList.size())
                  .setExitMissionOnRCSignalLostEnabled(true)
                  .flightPathMode(WaypointMissionFlightPathMode.NORMAL);
          if (Utils.isNull(waypointMissionBuilder)){
            toLog("goTo70MetersWaypoint stop cause waypointMissionBuilder IS Null");
            callback.onError("goTo70MetersWaypoint stop cause waypointMissionBuilder IS Null");
            return;
          }else{
            if (Utils.isNull(mFlightController)){
              toLog("goTo70MetersWaypoint mFlightController IS Null ----------->PLEASE RECONNECT DRONE <-----------");
              callback.onError("goTo70MetersWaypoint mFlightController IS Null ----------->PLEASE RECONNECT DRONE <-----------");
              return;
            }else {
              if (!mFlightController.getState().isHomeLocationSet()){
                toLog("goTo70MetersWaypoint Home Location not Set POSITION Not found YET");
                callback.onError("goTo70MetersWaypoint Home Location not Set POSITION Not found YET");
                return;
              }else {
                toLog("loadWaypoint Before getMaxFlightSpeed: "+ Utils.clone(waypointMissionBuilder.getMaxFlightSpeed()));
                toLog("loadWaypoint Before getAutoFlightSpeed: "+ Utils.clone(waypointMissionBuilder.getAutoFlightSpeed()));
                toLog("loadWaypoint Before getWaypointCount : "+ Utils.clone(waypointMissionBuilder.getWaypointCount()));
                toLog("loadWaypoint Before LastPoint Altitude : "+ Utils.clone(waypointMissionBuilder.getWaypointList().get(waypointMissionBuilder.getWaypointList().size() -1).altitude));
                toLog("loadWaypoint Before LastPoint Latitude: "+ Utils.clone(waypointMissionBuilder.getWaypointList().get(waypointMissionBuilder.getWaypointList().size() -1).coordinate.getLatitude()));
                toLog("loadWaypoint Before LastPoint Latitude: "+ Utils.clone(waypointMissionBuilder.getWaypointList().get(waypointMissionBuilder.getWaypointList().size() -1).coordinate.getLongitude()));

                WaypointMissionOperator waypointMissionOperator = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();

                DJIError error = waypointMissionOperator.loadMission(waypointMissionBuilder.build());
                if (error == null) {
                  toLog("loadWaypoint succeeded");
                } else {
                  toLog("loadWaypoint failed " + error.getDescription());
                }

                /// If startMission Not not Working try uploadWayPointMission first Before startMission
                waypointMissionOperator.uploadMission(uploadErr ->{
                  try {
                    if (!Utils.isNull(uploadErr)){
                      toLog("uploadErr Happen Detail: " + uploadErr.getDescription());
                      callback.onError("uploadErr Happen Detail: " + uploadErr.getDescription());
                      return;
                    }else {
                      // We Need to Wait else State Will Stay UPLOADING
                      toLog("uploading Successfully");
                      toLog("Now Wait  "+Constants.DELAY+" Sec to Transferts to Drone to be ok");

                      Utils.runAfterWait(activity,() ->{
                        try {
                          WaypointMissionState current = waypointMissionOperator.getCurrentState();
                          if (WaypointMissionState.DISCONNECTED.equals(current)) {
                            toLog("WaypointMissionState DISCONNECTED so Stop");
                            callback.onError("WaypointMissionState DISCONNECTED so Stop");
                            return;
                          }else if (WaypointMissionState.EXECUTING.equals(current)){
                            toLog("WaypointMissionState EXECUTING so Stop");
                            callback.onError("WaypointMissionState EXECUTING so Stop");
                            return;
                          }else if (WaypointMissionState.EXECUTION_PAUSED.equals(current)){
                            toLog("WaypointMissionState EXECUTING so Stop" );
                            callback.onError("WaypointMissionState EXECUTING so Stop");
                            return;
                          }else if (WaypointMissionState.NOT_SUPPORTED.equals(current)){
                            toLog("WaypointMissionState NOT_SUPPORTED so Stop" );
                            callback.onError("WaypointMissionState NOT_SUPPORTED so Stop");
                            return;
                          }else if (WaypointMissionState.READY_TO_UPLOAD.equals(current)){
                            toLog("WaypointMissionState READY_TO_UPLOAD so Stop cause already UPLOADED");
                            callback.onError("WaypointMissionState READY_TO_UPLOAD so Stop cause already UPLOADED");
                            return;
                          }else if (WaypointMissionState.RECOVERING.equals(current)){
                            toLog("WaypointMissionState RECOVERING so Stop ");
                            callback.onError("WaypointMissionState RECOVERING so Stop ");
                            return;
                          }else if (WaypointMissionState.UNKNOWN.equals(current)){
                            toLog("WaypointMissionState UNKNOWN so Stop ");
                            callback.onError("WaypointMissionState UNKNOWN so Stop ");
                            return;
                          }else if (WaypointMissionState.UPLOADING.equals(current)){
                            toLog("WaypointMissionState UPLOADING so Stop ");
                            callback.onError("WaypointMissionState UPLOADING so Stop ");
                            return;
                          }else if (WaypointMissionState.READY_TO_EXECUTE.equals(current)){
                            toLog("WaypointMissionState READY_TO_EXECUTE" );
                            waypointMissionOperator.startMission( startError ->{
                                      try {
                                        if (startError == null ){
                                          toLog("Mission Start: Successfully");
                                          callback.onSucces("");
                                        }else {
                                          toLog("Mission Start: Failed "+startError.getDescription());
                                          callback.onError("Mission Start: Failed "+startError.getDescription());
                                        }
                                      }catch (Exception e){
                                        Utils.toLog(TAG, "startMission",null,e);
                                        if (Utils.isNull(callback)){
                                          return;
                                        }else {
                                          callback.onError("Error");
                                        }
                                      }catch (Error er){
                                        Utils.toLog(TAG, "startMission",er,null);
                                        if (Utils.isNull(callback)){
                                          return;
                                        }else {
                                          callback.onError("Error");
                                        }
                                      }
                                    }
                            );
                          }
                        }catch (Exception e){
                          Utils.toLog(TAG, "runAfterWait",null,e);
                          if (Utils.isNull(callback)){
                            return;
                          }else {
                            callback.onError("Error");
                          }
                        }catch (Error er){
                          Utils.toLog(TAG, "runAfterWait",er,null);
                          if (Utils.isNull(callback)){
                            return;
                          }else {
                            callback.onError("Error");
                          }
                        }
                      });
                    }
                  }catch (Exception e){
                    Utils.toLog(TAG, "uploadMission",null,e);
                    if (Utils.isNull(callback)){
                      return;
                    }else {
                      callback.onError("Error");
                    }
                  }catch (Error er){
                    Utils.toLog(TAG, "uploadMission",er,null);
                    if (Utils.isNull(callback)){
                      return;
                    }else {
                      callback.onError("Error");
                    }
                  }
                });
              }

            }
          }
        }
      }catch (Exception e){
        Utils.toLog(TAG, "goTo70MetersWaypoint",null,e);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("Error");
        }
      }catch (Error er){
        Utils.toLog(TAG, "goTo70MetersWaypoint",er,null);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("Error");
        }
      }
    }
  }


  public static double fromSpeedDisplayUnit(double input)
  {
    return input / Utils.clone(MissionDJI.multiplierspeed);
  }
  public float getPhotoEveryDouble(double flySpeed,double spacing){
    double flyspeedms = fromSpeedDisplayUnit((double)flySpeed);
    double photoEvery = ((double)spacing / flyspeedms);
    toLog(" getPhotoEveryDouble: "+photoEvery);
    return (float) photoEvery;
  }

  public String getFlyTime(@NonNull WaypointMission.Builder builder) throws Exception,Error{
    //        double seconds = ((routetotal * 1000.0) / ((flyspeedms) * 0.8));
    //        reduce flying speed by 20 %
    //        lbl_flighttime.Text = secondsToNice(seconds);
    String empty = "";
    if (Utils.isNull(builder)){
      return empty;
    }else {
      double seconds = builder.calculateTotalTime();
      String flighttime = secondsToNice(seconds);
      if (Utils.isNull(flighttime)){
        flighttime = "";
      }
      return flighttime;
    }
  }

  public int getPicturesSize(@NonNull CopyOnWriteArrayList<PointLatLngAlt> grid)throws Exception,Error{
    if (Utils.isNull(grid)){
      return 0;
    }else {
      if (grid.size() == 0){
        return 0;
      }else{
        AtomicInteger images = new AtomicInteger();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          grid.forEach(item ->{
            if (item.getTag().trim().contentEquals("M") )
            {
              images.getAndIncrement();
            }
          });
        }
        return images.get();

      }
    }


  }
  private String secondsToNice(double seconds)
  {
    if (seconds < 0)
      return "Infinity Seconds";

    double secs = seconds % 60;
    int mins = (int)(seconds / 60) % 60;
    int hours = (int)(seconds / 3600);// % 24;

    if (hours > 0)
    {
      return hours + ":" + String.format(Locale.FRANCE,"%02d", mins) + ":" + String.format(Locale.FRANCE,"%02f", secs)+ " Hours";
    }
    else if (mins > 0)
    {
      return mins + ":" + String.format(Locale.FRANCE,"%02f", secs) + " Minutes";
    }
    else
    {
      return String.format(Locale.FRANCE,"%02f", secs) + " Seconds";
    }
  }
}
