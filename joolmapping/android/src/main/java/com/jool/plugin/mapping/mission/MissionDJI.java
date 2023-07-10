package com.jool.plugin.mapping.mission;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.jool.plugin.mapping.interfaces.OnAsyncGridProcessComplete;
import com.jool.plugin.mapping.interfaces.OnAsyncOperationComplete;
import com.jool.plugin.mapping.interfaces.OnAsyncOperationCompleteBool;
import com.jool.plugin.mapping.interfaces.OnCheckListChangeListener;
import com.jool.plugin.mapping.interfaces.OnMissionProcessingComplete;
import com.jool.plugin.mapping.interfaces.OnMissionRunningListener;
import com.jool.plugin.mapping.interfaces.OnMissionSplitComplete;
import com.jool.plugin.mapping.mapping.Grid;
import com.jool.plugin.mapping.mapping.Mapping;
import com.jool.plugin.mapping.mapping.PointLatLngAlt;
import com.jool.plugin.mapping.model.DroneDJI;
import com.jool.plugin.mapping.model.DroneState;
import com.jool.plugin.mapping.utils.Constants;
import com.jool.plugin.mapping.utils.Save;
import com.jool.plugin.mapping.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.model.LocationCoordinate2D;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.TimelineEvent;
import dji.sdk.mission.timeline.TimelineMission;
import dji.sdk.mission.timeline.actions.TakeOffAction;
import dji.sdk.mission.timeline.triggers.BatteryPowerLevelTrigger;
import dji.sdk.mission.timeline.triggers.Trigger;
import dji.sdk.mission.timeline.triggers.WaypointReachedTrigger;

public class MissionDJI {
  private final String TAG = getClass().getSimpleName();

  private @Nullable MissionStep missionStepEnd = null;
  private @Nullable MissionControl missionControl;
  private @Nullable TimelineEvent preEvent;
  private @Nullable TimelineElement preElement;
  private @Nullable DJIError preError;
  private volatile boolean computing = false;
  private @Nullable Runnable landingCheckAction = null;
  private @Nullable Handler handlerLock  = null;

  public MissionDJI(){
    setComputing(false);
  }

  public static boolean isRightWaypoint(@NonNull final PointLatLngAlt item)throws Exception,Error {
    String tagTrimed = item.getTag().trim();
    if (tagTrimed.contentEquals("S") || tagTrimed.contentEquals("E") )
    {
      return true;
    }
    return false;
  }

  public boolean isComputing() {
    return computing;
  }

  public void setComputing(boolean computing) {
    this.computing = computing;
  }


  @RequiresApi(api = Build.VERSION_CODES.N)
  public void startNewMission(@NonNull final Activity activity, @NonNull final MissionSetting missionSetting,@NonNull final MissionStep missionStep,final @NonNull DroneState droneState, final @NonNull OnMissionRunningListener callback) {
    try {
      setComputing(false);
      toLog("startWaypointMission startNewMission fired");
      if (Utils.isNull(callback)){
        toLog("startNewMission callback is Null");
        return;
      }else {
        if (Utils.isNull(activity)){
          toLog(" startNewMission activity is null");
          callback.onError(" startNewMission activity is null");
          return;
        }else {
          if (Utils.isNull(droneState)){
            toLog(" startNewMission droneState is Null");
            callback.onError("droneState is Null");
            return;
          }else {
            if (Utils.isNull(missionSetting)){
              toLog(" startNewMission MissionSetting isNull");
              callback.onError(" startNewMission MissionSetting isNull");
              return;
            }else {
              if (Utils.isNull(missionStep)){
                toLog(" startNewMission missionStep isNull");
                callback.onError(" startNewMission missionStep isNull");
                return;
              }else {
                shouldCancelOrContinu(null,missionSetting,missionStep,activity,droneState,callback);
              }
            }
          }
        }
      }
    }catch (Exception e){
      Utils.toLog(TAG, "startNewMission",null,e);
      if (Utils.isNull(callback)){
        return;
      }else {
        callback.onError("Error");
      }
    }catch (Error er){
      Utils.toLog(TAG, "startNewMission",er,null);
      if (Utils.isNull(callback)){
        return;
      }else {
        callback.onError("Error");
      }
    }
  }


  @RequiresApi(api = Build.VERSION_CODES.N)
  public synchronized void saveAndGoHome(@Nullable Boolean caseResume,@Nullable Boolean caseError,@Nullable Boolean caseLowBattery,@Nullable Integer indexLog, int max,@NonNull MissionSetting missionSetting, @NonNull MissionStep missionStep, @NonNull Context ctx, @NonNull OnMissionRunningListener callback){
    toLog("saveAndGoHome Start");
    if (Utils.isNull(caseLowBattery)){
      caseLowBattery = false;
    }
    if (Utils.isNull(caseError)){
      caseError = false;
    }
    if (Utils.isNull(caseResume)){
      caseResume = false;
    }
    if(Utils.isNull(indexLog)){
      indexLog = 0;
    }
    Boolean finalCaseError = caseError;
    Boolean finalCaseLowBattery = caseLowBattery;
    Runnable goHome = ()->{
      if (finalCaseError){
        callback.mustGoHomeError();
        return;
      }
      if (finalCaseLowBattery){
        if (missionSetting.autoReturnOnLowBatteryEnable()){
          callback.mustGoHomeLowBattery();
          return;
        }
      }
    };
    if (finalCaseError || finalCaseLowBattery){
      toLog(" saveAndGoHome caseError or caseLowBattery Fired");
      goHome.run();
      missionStep.to99And120Hect(true, true, new OnMissionSplitComplete() {
        @Override
        public void onError(@Nullable String errorDetail) {
          if (Utils.isNull(errorDetail)){
            errorDetail = "";
          }
          toLog(" saveAndGoHome to99And120Hect error:"+errorDetail);
          if (finalCaseError){
            if (Utils.isNull(callback)){
              // Skip
            }else {
              callback.onError("Error split before Save failed"+errorDetail);
            }
          }
          // Dont Notify Low battery is Normal need change battery
          return;
        }

        @Override
        public void onSucces(MissionStep himSelf, @NonNull final CopyOnWriteArrayList<LatLng> flyPointsAfterRebuild) {
          // Will not fired cause saveInPref is True
        }

        @Override
        public void onPerformSaveAndContinu(MissionStep himSelf, @NonNull final CopyOnWriteArrayList<LatLng> flyPointsAfterRebuild) {
          saveMission(ctx, himSelf, missionSetting, new OnMissionProcessingComplete() {
            @Override
            public void onError() {
              toLog(" saveAndGoHome onPerformSaveAndContinu Error Save after split failed");
              if (finalCaseError){
                if (Utils.isNull(callback)){
                  // Skip
                }else {
                  callback.onError("Error Save after split failed");
                }
              }
              // Dont Notify Low battery is Normal need change battery
              return;
            }

            @Override
            public void onSucces(@NonNull @NotNull MissionStep steps) {
              toLog(" saveAndGoHome onPerformSaveAndContinu Error Save after split failed");
              if (finalCaseError){
                if (Utils.isNull(callback)){
                  // Skip
                }else {
                  callback.onError("Error Happen");
                }
              }
              // Dont Notify Low battery is Normal need change battery
              return;
            }
          });
        }
      });
      return;
    }
    if (Utils.isNull(callback)){
      toLog("saveAndGoHome callback is Null");
      return;
    }else {
      try {
        if (Utils.isNull(ctx)){
          toLog(" saveAndGoHome ctx is Null");
          callback.onError("saveAndGoHome ctx is Null");
          return;
        }else {
          if (Utils.isNull(missionSetting)){
            toLog(" saveAndGoHome missionSetting is Null");
            callback.onError("saveAndGoHome missionSetting is Null");
            return;
          }else {
            if (Utils.isNull(missionStep)){
              toLog(" saveAndGoHome missionStep is Null");
              callback.onError("saveAndGoHome missionStep is Null");
              return;
            }else {
              if (!missionSetting.isValid(true)){
                toLog(" saveAndGoHome missionSetting is Not valid");
                callback.onError("saveAndGoHome missionSetting is Not valid");
                return;
              }else {
                if (!missionStep.hasNext()){
                  toLog(" saveAndGoHome Save Mission For Resume hasNext is FALSE");
                  callback.onError("Save Mission For Resume hasNext is FALSE");
                  return;
                }else {
                  if (caseResume){
                    toLog(" saveAndGoHome caseResume N°"+indexLog);
                    if (indexLog == max && max != 0){
                      // Remove Step DONE
                      int mSize = missionStep.getMainList().size();
                      if (mSize > 0){
                        missionStep.removeFirstOk();
                      }
                      int sizeAfterRemove = missionStep.getSize();
                      if (sizeAfterRemove == 0){
                        // Case WAS LAST SO DELETE SHARED SAVE
                        toLog(" saveAndGoHome AFTER REMOVE STEP WAS LAST SO DELETE SHARED SAVE ");
                        // Case All point Done Remove Shared
                        resetMemory(null, true, ctx, new OnAsyncOperationComplete() {
                          @Override
                          public void onError(@Nullable String errorDetail) {
                            toLog("saveAndGoHome Case All point Done Remove Shared failed");
                            return;
                          }

                          @Override
                          public void onSucces(@Nullable String succesMsg) {
                            toLog("saveAndGoHome Case All point Done Remove Shared Succes");
                          }
                        });
                      }else {
                        callback.onNotifyStepRemain(Utils.clone(sizeAfterRemove));
                        toLog(" saveAndGoHome AFTER REMOVE STEP missionStep.getMainList().size() = "+Utils.clone(sizeAfterRemove));
                        toLog(" saveAndGoHome AFTER REMOVE STEP DONT DELETE SHARED & CONTINU CAUSE STEP ARRAY NOT EMPTY ");
                        return;
                      }
                    }else {
                      if (missionStep.getFirst().size() > 2){
                        // If 0nly 1 Point keep last Segement(2 last Point)
                        // Remove Step Already Done and Save
                        missionStep.getFirst().remove(0);
                        saveMission(ctx, missionStep, missionSetting, new OnMissionProcessingComplete() {
                          @Override
                          public void onError() {
                            toLog("saveAndGoHome Save Mission For Resume failed");
                            return;
                          }

                          @Override
                          public void onSucces(@NonNull @NotNull MissionStep steps) {
                            toLog(" saveAndGoHome Save Mission For Resume Succes");
                            return;
                          }
                        });
                      }
                    }
                    return;
                  }
                }


              }
            }

          }

        }
      }catch (Exception e){
        Utils.toLog(TAG, "saveAndGoHome saveAndGoHome",null,e);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("Error");
        }
      }catch (Error er){
        Utils.toLog(TAG, "saveAndGoHome",er,null);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("saveAndGoHome Error");
        }
      }

    }
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public synchronized void shouldCancelOrContinu(@Nullable Boolean onExecutionUpdate,@NonNull final MissionSetting missionSetting, @NonNull final MissionStep missionStep, @NonNull final Activity activity, final @NonNull DroneState droneState,final @NonNull OnMissionRunningListener callback) {
    toLog("shouldCancelOrContinu Start");
    try {
      if (isComputing()){
        // Skip
        toLog("shouldCancelOrContinu SKIPPED");
        return;
      }else{
        setComputing(true);
        if (Utils.isNull(onExecutionUpdate)){
          onExecutionUpdate = false;
        }
        if (Utils.isNull(callback)){
          setComputing(false);
          toLog("startWaypointMission callback is Null");
          return;
        }else {
          Boolean finalOnExecutionUpdate = onExecutionUpdate;
          Utils.AsyncExecute(()->{
            try {
              if (Utils.isNull(activity)){
                setComputing(false);
                toLog(" startWaypointMission Activity is Null");
                callback.onError("Activity is Null");
                return;
              }else {
                if (Utils.isNull(missionStep)){
                  setComputing(false);
                  toLog(" startWaypointMission missionStep is Null");
                  callback.onError("missionStep is Null");
                  return;
                }else {
                  if (Utils.isNull(droneState)){
                    setComputing(false);
                    toLog(" startWaypointMission droneState is Null");
                    callback.onError("droneState is Null");
                    return;
                  }else {
                    if (Utils.isNull(missionSetting)){
                      setComputing(false);
                      toLog(" startWaypointMission missionSetting is Null");
                      callback.onError("missionSetting is Null");
                      return;
                    }else {
                      if (!missionStep.hasNext()){
                        setComputing(false);
                        toLog(" startWaypointMission missionStep is EMPTY");
                        // Dont Play Error ON_LOW_BATTERY AND RETURN
                        // callback.onError("missionSetting is EMPTY");
                        return;
                      }else {
                        if (Utils.isNull(droneState)){
                          setComputing(false);
                          toLog(" startWaypointMission droneState is Null IS NULL ----------->PLEASE RECONNECT DRONE AND ENABLE TELEMETRIE<-----------");
                          callback.onError("missionSetting is Null ----------->PLEASE RECONNECT DRONE AND ENABLE TELEMETRIE<-----------");
                          return;
                        }else {
                          if (finalOnExecutionUpdate){
                            // Stop Here
                            setComputing(false);
//                            if (!Utils.isNull(onUpdateCallBack)){
//                              onUpdateCallBack.run();
//                            }
                            return;
                          }
                          // Case Continu to Next Step
                          // Copy to Continu at step End
                          this.missionStepEnd = missionStep;
                          toLog(" startWaypointMission saveMission Will Start:");
                          CopyOnWriteArrayList<LatLng> stepPoints = missionStep.getFirst();
                          if (Utils.isNull(stepPoints)){
                            setComputing(false);
                            toLog(" startWaypointMission step Waypoint is Null");
                            callback.onError("step Waypoint is Null");
                            return;
                          }else {
                            if (stepPoints.size() == 0){
                              setComputing(false);
                              toLog(" startWaypointMission step Waypoint is EMPTY");
                              callback.onError("step Waypoint is EMPTY");
                              return;
                            }else {
                              if (Utils.isNull(droneState.getAircraft())){
                                setComputing(false);
                                toLog("startWaypointMission droneState.getAircraft() Is Null");
                                callback.onError("startWaypointMission droneState.getAircraft() Is Null ");
                                return;
                              }else {
                                FlightController controller = droneState.getAircraft().getFlightController();
                                if (Utils.isNull(controller)){
                                  setComputing(false);
                                  toLog("startWaypointMission FlightController Is Null");
                                  callback.onError("startWaypointMission FlightController Is Null ");
                                  return;
                                }else {
                                  final double droneLatitude = Utils.clone(droneState.getLatitude());
                                  final double droneLongitude = Utils.clone(droneState.getLongitude());
                                  if (droneLatitude == Constants.DOUBLE_NULL || droneLatitude == Constants.DOUBLE_NULL_2){
                                    setComputing(false);
                                    toLog(" startWaypointMission Bad Drone latitute");
                                    callback.onError("Bad Drone longitude");
                                    return;
                                  }else {
                                    toLog(" startWaypointMission Drone Location right");
                                    if (droneLongitude == Constants.DOUBLE_NULL || droneLongitude == Constants.DOUBLE_NULL_2){
                                      setComputing(false);
                                      toLog(" startWaypointMission Bad Drone droneLongitude");
                                      callback.onError("Bad Drone droneLongitude");
                                      return;
                                    }else {
                                      if (Double.isNaN(droneLatitude)){
                                        setComputing(false);
                                        toLog(" startWaypointMission Bad Drone latitute : isNaN");
                                        callback.onError("Bad Drone droneLatitude: isNaN");
                                        return;
                                      }else {
                                        if (Double.isNaN(droneLongitude)){
                                          setComputing(false);
                                          toLog(" startWaypointMission Bad Drone droneLongitude :isNaN");
                                          callback.onError("Bad Drone droneLongitude: isNaN");
                                          return;
                                        }else {
                                          resetMemory(true,null,activity, new OnAsyncOperationComplete() {
                                            @Override
                                            public void onError(@Nullable String errorDetail) {
                                              if (Utils.isNull(errorDetail)){
                                                errorDetail = "";
                                              }
                                              toLog("resetMemory error happen errorDetail: "+errorDetail);
                                              callback.onError("resetMemory error happen");
                                              return;
                                            }

                                            @Override
                                            public void onSucces(@Nullable String succesMsg) {
                                              try {
                                                toLog(" startWaypointMission resetMemory Succes");
                                                if (Utils.isNull(missionControl)){
                                                  setComputing(false);
                                                  toLog("startWaypointMission missionControl is Null ");
                                                  callback.onError("startWaypointMission missionControl is Null");
                                                  return;
                                                }else {
                                                  MissionControl.Listener listener = (element, event, error) -> updateTimelineStatus(element, event, error,callback,activity,missionSetting,missionStep,controller,droneState);
                                                  List<TimelineElement> elements = new CopyOnWriteArrayList<>();
                                                  BatteryPowerLevelTrigger batteryTrigger = new BatteryPowerLevelTrigger();
                                                  CopyOnWriteArrayList<Trigger> reachList = new CopyOnWriteArrayList<>();

                                                  batteryTrigger.setPowerPercentageTriggerValue(Constants.LOW_BATTERY_LEVEL);
                                                  batteryTrigger.setAction(() -> {
                                                    toLog("startWaypointMission BatteryPowerLevelTrigger Action Fired");
                                                    saveAndGoHome(null,null,true,null,0,missionSetting,missionStep,activity,callback);
                                                  });

                                                  List<Trigger> triggers = missionControl.getTriggers();
                                                  if (triggers == null) {
                                                    triggers = new CopyOnWriteArrayList<>();
                                                  }
                                                  // Only One 1 tem
                                                  if (missionStep.getSize() == 1){
                                                    Container container = buildMission(missionSetting,stepPoints,true,null,droneLatitude,droneLongitude,missionStep,activity,callback);
                                                    if (Utils.isNull(container)){
                                                      setComputing(false);
                                                      toLog("startWaypointMission BUILD MISSION FAILED container is Null ");
                                                      callback.onError("BUILD MISSION FAILED container is Null ");
                                                      return;
                                                    }else {
                                                      if (!container.isValid()){
                                                        setComputing(false);
                                                        toLog("startWaypointMission BUILD MISSION FAILED container is not valid ");
                                                        callback.onError("BUILD MISSION FAILED container is not valid ");
                                                        return;
                                                      }else {
                                                        elements.add(container.getTimelineElement());
                                                        // Add Trigger
                                                        reachList.addAll(container.getReachList());
                                                      }
                                                    }
                                                  }else {
                                                    // 2 or More
                                                    if (missionStep.getSize() > 1){
                                                      for (int i = 0 ; i< missionStep.getMainList().size() ; i++){
                                                        CopyOnWriteArrayList<LatLng> item = missionStep.getMainList().get(i);
                                                        Container container = null;
                                                        // Case First Step or Other
                                                        if (i != (missionStep.getMainList().size() -1)){
                                                          container = buildMission(missionSetting,item,null,true,droneLatitude,droneLongitude,missionStep,activity,callback);
                                                        }else {
                                                          // Case Last
                                                          container = buildMission(missionSetting,item,true,null,droneLatitude,droneLongitude,missionStep,activity,callback);
                                                        }
                                                        if (Utils.isNull(container)){
                                                          setComputing(false);
                                                          toLog("startWaypointMission BUILD MISSION FAILED container is Null N="+i);
                                                          callback.onError("BUILD MISSION FAILED container is Null N"+i);
                                                          return;
                                                        }else {
                                                          if (!container.isValid()){
                                                            setComputing(false);
                                                            toLog("startWaypointMission BUILD MISSION FAILED container is not valid N"+i);
                                                            callback.onError("BUILD MISSION FAILED container is not valid N"+i);
                                                            return;
                                                          }else {
                                                            elements.add(container.getTimelineElement());
                                                            // Add Trigger
                                                            reachList.addAll(container.getReachList());
                                                          }
                                                        }
                                                      }

                                                    }
                                                  }

                                                  triggers.add(batteryTrigger);
                                                  triggers.addAll(reachList);
                                                  missionControl.setTriggers(triggers);
                                                  missionControl.scheduleElements(elements);
                                                  missionControl.addListener(listener);
                                                  missionControl.startTimeline();
                                                  callback.onStartSucces();
                                                  toLog(" startWaypointMission scheduleElements Succes");
                                                  return;
                                                }
                                              }catch (Exception e){
                                                setComputing(false);
                                                Utils.toLog(TAG, "resetMemory(true onSucces",null,e);
                                                if (Utils.isNull(callback)){
                                                  return;
                                                }else {
                                                  callback.onError("Error");
                                                }
                                              }catch (Error er){
                                                setComputing(false);
                                                Utils.toLog(TAG, "resetMemory(true onSucces",er,null);
                                                if (Utils.isNull(callback)){
                                                  return;
                                                }else {
                                                  callback.onError("Error");
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
            }catch (Exception e){
              setComputing(false);
              Utils.toLog(TAG, "shouldCancelOrContinu",null,e);
              if (Utils.isNull(callback)){
                return;
              }else {
                callback.onError("Error");
              }
            }catch (Error er){
              setComputing(false);
              Utils.toLog(TAG, "shouldCancelOrContinu",er,null);
              if (Utils.isNull(callback)){
                return;
              }else {
                callback.onError("Error");
              }
            }
          });

        }
      }
    }catch (Exception e){
      setComputing(false);
      Utils.toLog(TAG, "shouldCancelOrContinu",null,e);
    }catch (Error er){
      setComputing(false);
      Utils.toLog(TAG, "shouldCancelOrContinu",er,null);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  private void updateTimelineStatus(@Nullable TimelineElement element, @Nullable TimelineEvent event, @Nullable DJIError error, final @NonNull OnMissionRunningListener callback, @NonNull final Activity activity, @NonNull final MissionSetting settings, @NonNull final MissionStep step, @NonNull final FlightController controller, final @NonNull DroneState droneState) {
    toLog("updateTimelineStatus Start");
    try {
      if (element == preElement && event == preEvent && error == preError) {
        setComputing(false);
        return;
      }

      if (element != null) {
        // Case Error
        if (!Utils.isNull(error)){
          String detail = error.getDescription();
          if (Utils.isNull(detail)){
            detail = "";
          }
          toLog("Timeline Event is Error Happen: "+detail);
          saveAndGoHome(null,true,null,null,0,settings,step,activity,callback);
          return;
        }
      } else {
        // Case Error
        if (!Utils.isNull(error)){
          String detail = error.getDescription();
          if (Utils.isNull(detail)){
            detail = "";
          }
          toLog("Timeline Event is Error Happen: "+detail);
          saveAndGoHome(null,true,null,null,0,settings,step,activity,callback);
          return;
        }
        if (Utils.isNull(event)){
          // Skip
        }else {
          // Case No Error
          String msg = "Timeline Event is " + event.toString();
          if (Utils.isNullOrEmpty(msg)){
            // Skip
          }else {
            toLog(Utils.clone(msg));
            if (msg.contains("Timeline Event is FINISHED")){
              callback.onSucces();
              enableLock(callback,controller,droneState);
            }
          }
        }
      }

      preEvent = event;
      preElement = element;
      preError = error;
    }catch (Exception e){
      setComputing(false);
      saveAndGoHome(null,true,null,null,0,settings,step,activity,callback);
      Utils.toLog(TAG, "updateTimelineStatus",null,e);
    }catch (Error er){
      setComputing(false);
      saveAndGoHome(null,true,null,null,0,settings,step,activity,callback);
      Utils.toLog(TAG, "updateTimelineStatus",er,null);
    }
  }

  private void enableLock(@NonNull final OnMissionRunningListener callback, @NonNull final FlightController controller,@NonNull final DroneState droneState) {
    if (Utils.isNull(callback)){
      toLog("enableLock callback is Null");
      return;
    }else {
      try {
        if (Utils.isNull(controller)){
          toLog("enableLock FightController is Null");
          return;
        }else {
          if (Utils.isNull(droneState)){
            toLog("enableLock droneState is Null");
            return;
          }else {
            if (Utils.isNull(handlerLock)){
              // Skip
            }else {
              if (Utils.isNull(landingCheckAction)){
                // Skip
              }else {
                // caseNext Time So clean Old
                handlerLock.removeCallbacks(landingCheckAction);
                handlerLock = null;
                landingCheckAction = null;
              }
            }
            DroneDJI.setDontTakeOff(true);
            callback.onEnableLoadingForNextMission();
            handlerLock = new Handler(Looper.getMainLooper());
            landingCheckAction = () ->{
              checkAndLock7sec(callback,controller,droneState);
            };
            handlerLock.postDelayed(landingCheckAction, TimeUnit.SECONDS.toMillis(Constants.DELAY_5_SEC));
          }

        }
      }catch (Exception e){
        setComputing(false);
        Utils.toLog(TAG, "enableLock",null,e);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("Error");
        }
      }catch (Error er){
        setComputing(false);
        Utils.toLog(TAG, "enableLock",er,null);
        if (Utils.isNull(callback)){
          return;
        }else {
          callback.onError("Error");
        }
      }
    }

  }

  private void checkAndLock7sec(final OnMissionRunningListener callback, final FlightController controller, final DroneState droneState) {
    try {
      Handler retry = new Handler(Looper.getMainLooper());
      Runnable action = ()->{
        checkAndLock7sec(callback,controller,droneState);
      };
      int altitudeInt = (int) droneState.getAltitude();
      // Wait till drone is Landing Done(ZERO ALTITUDE) and Motors are Off
      if (altitudeInt != 0){
        // try again
        toLog("checkAndLock7sec try again CAUSE DRONE ALTITUDE not ZERO: "+Utils.clone(altitudeInt)+" Meter(s)");
        retry.postDelayed(action,TimeUnit.SECONDS.toMillis(Constants.DELAY_5_SEC));
        return;
      }else {
        if (controller.getState().isFlying()){
          // try again
          toLog("checkAndLock7sec try again CAUSE DRONE isFlying");
          retry.postDelayed(action,TimeUnit.SECONDS.toMillis(Constants.DELAY_5_SEC));
          return;
        }else {
          if (controller.getState().areMotorsOn()){
            // try again
            toLog("checkAndLock7sec try again CAUSE DRONE areMotorsOn");
            retry.postDelayed(action,TimeUnit.SECONDS.toMillis(Constants.DELAY_5_SEC));
            return;
          }else {
            Handler h = new Handler(Looper.getMainLooper());
            Runnable removeLockAction = () ->{
              try {
                DroneDJI.setDontTakeOff(null);
                callback.onDisableLoadingForNextMission();
              }catch (Exception e){
                setComputing(false);
                Utils.toLog(TAG, "removeLockAction",null,e);
                if (Utils.isNull(callback)){
                  return;
                }else {
                  callback.onError("Error");
                }
              }catch (Error er){
                setComputing(false);
                Utils.toLog(TAG, "removeLockAction",er,null);
                if (Utils.isNull(callback)){
                  return;
                }else {
                  callback.onError("Error");
                }
              }
            };
            h.postDelayed(removeLockAction,TimeUnit.SECONDS.toMillis(7));
            return;

          }
        }
      }
    }catch (Exception e){
      setComputing(false);
      Utils.toLog(TAG, "enableLock",null,e);
      if (Utils.isNull(callback)){
        return;
      }else {
        callback.onError("Error");
      }
    }catch (Error er){
      setComputing(false);
      Utils.toLog(TAG, "enableLock",er,null);
      if (Utils.isNull(callback)){
        return;
      }else {
        callback.onError("Error");
      }
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  private @Nullable Container buildMission(@NonNull final MissionSetting missionSetting, @NonNull CopyOnWriteArrayList<LatLng> points, @Nullable Boolean addLast, @Nullable Boolean addFirstOrNext, double droneLatitude, double droneLongitude, @NonNull MissionStep missionStep, @NonNull Context ctx, @NonNull OnMissionRunningListener callback)throws Exception,Error {
    if (Utils.isNull(missionSetting)){
      toLog("buildMission missionSetting is Null");
      return null;
    }else {
      if (Utils.isNull(points)){
        toLog("buildMission missionSetting is null");
        return null;
      }else {
        if (points.size() == 0){
          toLog("buildMission missionSetting is Empty");
          return null;
        }else {
          if (Utils.isNull(missionStep)){
            toLog("buildMission missionStep is null");
            return null;
          }else {
            if (Utils.isNull(ctx)){
              toLog("buildMission ctx is null");
              return null;
            }else {
              if (Utils.isNull(callback)){
                toLog("buildMission callback is null");
                return null;
              }else {
                if (Utils.isNull(addLast)){
                  addLast = false;
                }
                if (Utils.isNull(addFirstOrNext)){
                  addFirstOrNext = false;
                }
                CopyOnWriteArrayList<Trigger> reachList = new CopyOnWriteArrayList<>();
                CopyOnWriteArrayList<LatLng> pointsClone = new CopyOnWriteArrayList<>();
                for (LatLng item: points) {
                  // Avoid Same Address Case Delete Later
                  pointsClone.add(new LatLng(Utils.clone(item.latitude),Utils.clone(item.longitude)));
                }
                if (addLast){
                  CopyOnWriteArrayList<Waypoint> waypointList = new CopyOnWriteArrayList<>();
                  int max = Utils.clone(pointsClone.size() -1);
                  for (int i = 0 ; i< pointsClone.size() ; i++){
                    LatLng point = pointsClone.get(i);
                    Waypoint mWaypoint = new Waypoint(Utils.clone(point.latitude), Utils.clone(point.longitude), missionSetting.getAltitudePrefered());
                    mWaypoint.shootPhotoTimeInterval = missionSetting.getShootIntervalInSec(); // each 3 seconds
                    waypointList.add(mWaypoint);

                    if (i <= 1){
                      // Skip 0 and 1 are not Good 2 is out 1
                    }else {
                      WaypointReachedTrigger waypointTrigger = new WaypointReachedTrigger();
                      waypointTrigger.setWaypointIndex(Utils.clone(i));
                      waypointTrigger.setAction(() -> {
                        toLog("startWaypointMission FIRST WAYPOINT Action Fired N°"+waypointTrigger.getWaypointIndex());
                        saveAndGoHome(true,null,null,waypointTrigger.getWaypointIndex(),max,missionSetting,missionStep,ctx,callback);
                      });
                      reachList.add(waypointTrigger);
                    }

                  }
                  toLog("addPoints addLast waypointList size "+waypointList.size());
                  WaypointMissionHeadingMode mHeadingMode = WaypointMissionHeadingMode.AUTO;
                  WaypointMissionFinishedAction mFinishedAction = WaypointMissionFinishedAction.GO_HOME;
                  WaypointMission.Builder waypointMissionBuilder = new WaypointMission.Builder()
                    .finishedAction(mFinishedAction)
                    .headingMode(mHeadingMode)
                    .autoFlightSpeed(missionSetting.getSpeed())
                    .maxFlightSpeed(Constants.DJI_MAX_SPEED)
                    .waypointList(waypointList)
                    .waypointCount(waypointList.size())
                    .setExitMissionOnRCSignalLostEnabled(missionSetting.isExitMissionOnRCSignalLostEnabled())
                    .flightPathMode(WaypointMissionFlightPathMode.NORMAL);
                  TimelineElement waypointMission = TimelineMission.elementFromWaypointMission(waypointMissionBuilder.build());
                  Container container = new Container();
                  container.setTimelineElement(waypointMission);
                  container.setReachList(reachList);
                  return container;
                }
                if (addFirstOrNext){
                  CopyOnWriteArrayList<Waypoint> waypointList = new CopyOnWriteArrayList<>();
                  int max = Utils.clone(pointsClone.size() -1);
                  for (int i = 0 ; i< pointsClone.size() ; i++){
                    LatLng point = pointsClone.get(i);
                    Waypoint mWaypoint = new Waypoint(Utils.clone(point.latitude), Utils.clone(point.longitude), missionSetting.getAltitudePrefered());
                    mWaypoint.shootPhotoTimeInterval = missionSetting.getShootIntervalInSec(); // each 3 seconds
                    waypointList.add(mWaypoint);

                    if (i <= 1){
                      // Skip 0 and 1 are not Good 2 is out 1
                    }else {
                      WaypointReachedTrigger waypointTrigger = new WaypointReachedTrigger();
                      waypointTrigger.setWaypointIndex(Utils.clone(i));
                      waypointTrigger.setAction(() -> {
                        toLog("startWaypointMission FIRST WAYPOINT Action Fired N°"+waypointTrigger.getWaypointIndex());
                        saveAndGoHome(true,null,null,waypointTrigger.getWaypointIndex(),max,missionSetting,missionStep,ctx,callback);
                      });
                      reachList.add(waypointTrigger);
                    }

                  }
                  // Add LoadNextMission With Good SIGNAL ALTITUDE
                  Waypoint goBack = new Waypoint(Utils.clone(droneLatitude), Utils.clone(droneLongitude), missionSetting.getAltitudePrefered());
                  goBack.speed = Constants.DJI_MAX_SPEED;
                  Waypoint getCloseToUser = new Waypoint(Utils.clone(droneLatitude), Utils.clone(droneLongitude), Constants.SIGNAL_SECURITY_ALTITUDE);
                  waypointList.add(goBack);
                  waypointList.add(getCloseToUser);

                  // Build Everything
                  toLog("addPoints addFirstOrNext waypointList size "+waypointList.size());
                  WaypointMissionHeadingMode mHeadingMode = WaypointMissionHeadingMode.AUTO;
                  WaypointMissionFinishedAction mFinishedAction = WaypointMissionFinishedAction.NO_ACTION;
                  WaypointMission.Builder waypointMissionBuilder = new WaypointMission.Builder()
                    .finishedAction(mFinishedAction)
                    .headingMode(mHeadingMode)
                    .autoFlightSpeed(missionSetting.getSpeed())
                    .maxFlightSpeed(Constants.DJI_MAX_SPEED)
                    .waypointList(waypointList)
                    .waypointCount(waypointList.size())
                    .setExitMissionOnRCSignalLostEnabled(missionSetting.isExitMissionOnRCSignalLostEnabled())
                    .flightPathMode(WaypointMissionFlightPathMode.NORMAL);
                  TimelineElement waypointMission = TimelineMission.elementFromWaypointMission(waypointMissionBuilder.build());
                  Container container = new Container();
                  container.setTimelineElement(waypointMission);
                  container.setReachList(reachList);
                  return container;
                }

                toLog("buildMission CASE NOT addLast and NOT addFirstOrNext SHOULD NOT HAPPEN");
                return null;
              }
            }
          }
        }
      }
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public void resetMemory(@Nullable Boolean onlyVar,@Nullable Boolean onlyShared, @Nullable Context ctx, OnAsyncOperationComplete callback) {
    if (Utils.isNull(onlyVar)){
      onlyVar = false;
    }
    if (Utils.isNull(onlyShared)){
      onlyShared = false;
    }
    try {
      if (Utils.isNull(callback)){
        setComputing(false);
        toLog(" resetMemory CallBack is Null");
        return;
      }else {
        if (Utils.isNull(ctx)){
          setComputing(false);
          toLog("resetMemory ctx is Null");
          callback.onError("resetMemory ctx is Null");
          return;
        }else {
          // Auto Ini
          if (Utils.isNull(missionControl)){
            missionControl = MissionControl.getInstance();
          }
          if (onlyVar){
            if (Utils.isNull(missionControl)){
              toLog(" resetMemory missionControl is Null");
              callback.onError("resetMemory missionControl is Null");
              return;
            }else {
              preEvent = null;
              preElement = null;
              preError = null;
              // AllowTake Off
              DroneDJI.setDontTakeOff(null);
              DroneDJI.resetMemoryMission(true);
              if (missionControl.scheduledCount() > 0) {
                missionControl.unscheduleEverything();
                missionControl.removeAllListeners();
                if (missionControl.getTriggers().size()>0){
                  missionControl.getTriggers().clear();
                }
                missionControl.setTriggers(new CopyOnWriteArrayList<>());
              }
              if (!onlyShared){
                callback.onSucces("");
                return;
              }
            }
            setComputing(false);
          }
          if (onlyShared){
            resetMission(ctx, new OnAsyncOperationComplete() {
              @Override
              public void onError(@Nullable String errorDetail) {
                if (Utils.isNull(errorDetail)){
                  errorDetail = "";
                }
                setComputing(false);
                toLog("resetMemory resetMission onError : "+errorDetail);
                callback.onError("resetMemory resetMission onError : "+errorDetail);
                return;
              }

              @Override
              public void onSucces(@Nullable String succesMsg) {
                callback.onSucces("");
              }
            });
          }
        }
      }
    }catch (Exception e){
      setComputing(false);
      Utils.toLog(TAG, "resetMemory",null,e);
      if (Utils.isNull(callback)){
        return;
      }else {
        callback.onError("Error");
      }
    }catch (Error er){
      setComputing(false);
      Utils.toLog(TAG, "resetMemory",er,null);
      if (Utils.isNull(callback)){
        return;
      }else {
        callback.onError("Error");
      }
    }
  }

  private void toLog( @NonNull String msg){
    Utils.toLog(TAG, msg);
  }

  // Phantom 4 TXT_senswidth =4.7
  //           TXT_sensheight= 6.3
  //           NUM_focallength= 20
  public static @Nullable GridSetting calcGridSetting(@NonNull CameraParameter params) throws Exception,Error
  {
    try
    {
      if (Utils.isNull(params)){
        return null;
      }else {
        if (!params.isValid()){
          return null;
        }else {
          float flyalt = (float) fromDistDisplayUnit((float)params.getAltitudeForGrid());

          int overlap = (int)params.getOverlap();
          int sidelap = (int)params.getSidelap();

          double viewwidth = 0;
          double viewheight = 0;

          // Start getFOV
          double focallen = (double)params.getFocalLengthInMM();
          double sensorwidth = params.getSensorWidth();
          double sensorheight = params.getSensorHeight();

          // scale      mm / mm
          double flscale = (1000 * flyalt) / focallen;

          //   mm * mm / 1000
          double viewwidthgetFOV = (sensorwidth * flscale / 1000);
          double viewheightgetFOV = (sensorheight * flscale / 1000);

          viewwidth = viewwidthgetFOV;
          viewheight = viewheightgetFOV;
          // End getFOV
          double spacingValue = (double)((1 - (overlap / 100.0f)) * viewheight);
          double distanceValue = (double)((1 - (sidelap / 100.0f)) * viewwidth);

          GridSetting res = new GridSetting();
          res.setSpacing(spacingValue);
          res.setDistance(distanceValue);
          res.setAltitude(params.getAltitudeForGrid());
          res.setAngle(params.getAngleForGrid());
          if (!res.isValid()){
            return null;
          }else {
            return res;
          }

        }
      }
    }
    catch (Exception e){
      Utils.toLog("MissionDJI", "calcGridSetting",null,e);

    }catch (Error er){
      Utils.toLog("MissionDJI", "v",er,null);

    }
    return null;
  }

  public static double fromDistDisplayUnit(double input)
  {
    return input / Utils.clone(multiplierdist);
  }
  public static float multiplierdist = 1;
  public static float multiplierspeed = 1;


  @RequiresApi(api = Build.VERSION_CODES.N)
  public void saveMission(@NonNull final Context ctx, @NonNull MissionStep steps,@NonNull MissionSetting setting, final @NonNull OnMissionProcessingComplete callback){
    Utils.AsyncExecute(()->{
      if (Utils.isNull(callback)){
        toLog(" saveMission callback is null");
        return;
      }else {
        try {
          if (Utils.isNull(steps)){
            toLog(" saveMission step is null");
            callback.onError();
            return;
          }else {
            if (Utils.isNull(setting)){
              toLog(" saveMission setting is null");
              callback.onError();
              return;
            }else {
              if (Utils.isNull(ctx)){
                toLog(" saveMission Context is null");
                callback.onError();
                return;
              }else {
                // Clean old Save
                // Dont Clean Mission in Memory
                resetMission(ctx, new OnAsyncOperationComplete() {
                  @Override
                  public void onError(@Nullable  String errorDetail) {
                    toLog(" saveMission reset before Error happen");
                    callback.onError();
                    return;
                  }

                  @Override
                  public void onSucces(@Nullable String succesMsg) {
                    Utils.AsyncExecute(()->{
                      try {
                        if (steps.getSize() == 0){
                          toLog(" saveMission Step is empty");
                          callback.onError();
                          return;
                        }else {
                          String parameterStr = setting.toStringJSON();
                          if (Utils.isNullOrEmpty(parameterStr)){
                            toLog(" saveMission parameterStr is Null or empty");
                            callback.onError();
                            return;
                          }else {
                            steps.toStringJSON(new OnAsyncOperationComplete() {
                              @Override
                              public void onError(@Nullable String errorDetail) {
                                toLog(" saveMission convert Mission List to String Failed");
                                callback.onError();
                                return;
                              }

                              @Override
                              public void onSucces(@Nullable String value) {
                                try {
                                  if (Utils.isNullOrEmpty(value)){
                                    toLog(" saveMission convert Mission List value is Null or Empty");
                                    callback.onError();
                                    return;
                                  }else {
                                    boolean isSaved = Save.defaultSaveString(Constants.PREF_WAYPOINT_MISSION, value, ctx);
                                    boolean isSavedParameter = Save.defaultSaveString(Constants.PREF_WAYPOINT_PARAMETER, parameterStr, ctx);
                                    if (!isSaved){
                                      toLog(" saveMission defaultSaveString Failed");
                                      callback.onError();
                                      return;
                                    }else {
                                      if (!isSavedParameter){
                                        toLog(" saveMission SaveString for parameter Failed");
                                        callback.onError();
                                        return;
                                      }else {
                                        toLog(" saveMission SaveString for parameter Succes");
                                        callback.onSucces(steps);
                                      }
                                    }
                                  }
                                }catch (Exception e){
                                  Utils.toLog(TAG, "toStringJSON onSucces",null,e);
                                }catch (Error er){
                                  Utils.toLog(TAG, "toStringJSON onSucces",er,null);
                                }
                              }
                            });
                          }
                        }
                      }catch (Exception e){
                        Utils.toLog(TAG, "saveMission reset before",null,e);
                        if (Utils.isNull(callback)){
                          return;
                        }else {
                          callback.onError();
                        }
                      }catch (Error er){
                        Utils.toLog(TAG, "saveMission reset before",er,null);
                        if (Utils.isNull(callback)){
                          return;
                        }else {
                          callback.onError();
                        }
                      }
                    });
                  }
                });
              }
            }

          }
        }catch (Exception e){
          Utils.toLog(TAG, "saveMission",null,e);
          if (Utils.isNull(callback)){
            return;
          }else {
            callback.onError();
          }
        }catch (Error er){
          Utils.toLog(TAG, "saveMission",er,null);
          if (Utils.isNull(callback)){
            return;
          }else {
            callback.onError();
          }
        }
      }

    });
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public void loadMission(@NonNull final Context ctx,final @NonNull OnMissionProcessingComplete callback){
    Utils.AsyncExecute(()->{
      if (Utils.isNull(callback)){
        toLog(" loadMission callback is null");
        return;
      }else {
        try {
          if (Utils.isNull(ctx)){
            toLog(" loadMission Context is null");
            callback.onError();
            return;
          }else {
            String data = Save.defaultLoadString(Constants.PREF_WAYPOINT_MISSION,ctx);
            if (Utils.isNullOrEmpty(data)){
              toLog(" loadMission Data Saved is null or Empty");
              callback.onError();
              return;
            }else {
              MissionStep steps = new MissionStep();
              steps.fillFromString(data, new OnMissionProcessingComplete() {
                @Override
                public void onError() {
                  toLog(" loadMission fillFromString Error Happen");
                  callback.onError();
                  return;
                }

                @Override
                public void onSucces(@NonNull final MissionStep himSelf) {
                  if (Utils.isNull(himSelf)){
                    toLog(" loadMission After Fill himSelf is Null");
                    callback.onError();
                    return;
                  }else {
                    callback.onSucces(himSelf);
                  }
                }
              });
            }
          }
        }catch (Exception e){
          Utils.toLog(TAG, "loadMission",null,e);
          if (Utils.isNull(callback)){
            return;
          }else {
            callback.onError();
          }
        }catch (Error er){
          Utils.toLog(TAG, "loadMission",er,null);
          if (Utils.isNull(callback)){
            return;
          }else {
            callback.onError();
          }
        }
      }
    });
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public void resetMission(@NonNull final Context ctx,final @NonNull OnAsyncOperationComplete callback) {
    Utils.AsyncExecute(()->{
      if (Utils.isNull(callback)){
        toLog(" resetMission callback is null");
        return;
      }else {
        try {
          if (Utils.isNull(ctx)){
            toLog(" resetMission Context is null");
            callback.onError("");
            return;
          }else {
            boolean isDone = Save.defaultSaveString(Constants.PREF_WAYPOINT_MISSION,"",ctx);
            boolean isDoneParameter = Save.defaultSaveString(Constants.PREF_WAYPOINT_PARAMETER,"",ctx);
            if (!isDone){
              toLog(" resetMission defaultSaveString PREF_WAYPOINT_MISSION Failed");
              callback.onError("");
            }else {
              if (!isDoneParameter){
                toLog(" resetMission defaultSaveString PREF_WAYPOINT_PARAMETER Failed");
                callback.onError("");
                return;
              }else {
                toLog(" resetMission CLEANING Succes");
                callback.onSucces("");
              }
            }

          }
        }catch (Exception e){
          Utils.toLog(TAG, "resetMission",null,e);
          if (Utils.isNull(callback)){
            return;
          }else {
            callback.onError("");
          }
        }catch (Error er){
          Utils.toLog(TAG, "resetMission",er,null);
          if (Utils.isNull(callback)){
            return;
          }else {
            callback.onError("");
          }
        }
      }

    });
  }


  public void cancelCurrentCauseGoHome(@Nullable Context ctx, OnAsyncOperationComplete callBack) {
    synchronized (this){
      try {
        setComputing(false);
        if (Utils.isNull(callBack)){
          return;
        }else {
          if (Utils.isNull(ctx)){
            // skip
            toLog("cancelCurrentCauseGoHome waypointMissionOperator is Null");
            callBack.onError("cancelCurrentCauseGoHome ctx is Null");
            return;
          }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
              // Case Low Battery Dont Clean Shared
              resetMemory(true,null, ctx, new OnAsyncOperationComplete() {
                @Override
                public void onError(@Nullable String errorDetail) {
                  if (Utils.isNull(errorDetail)){
                    errorDetail = "";
                  }
                  toLog("cancelCurrentCauseGoHome resetMemory error "+errorDetail);
                  callBack.onError("cancelCurrentCauseGoHome resetMemory error "+errorDetail);
                  return;
                }

                @Override
                public void onSucces(@Nullable String succesMsg) {
                  if (Utils.isNull(missionStepEnd)){
                    // skip
                    toLog("cancelCurrentCauseGoHome missionStepEnd is Null");
                    // Continu If Never started missionStepEnd is Null
                    callBack.onSucces("");
                    return;
                  }else {
                    missionStepEnd.clearAll();
                    callBack.onSucces("");
                    return;
                  }
                }
              });
            }
          }

        }
      }catch (Exception e){
        Utils.toLog(TAG, "cancelCurrentCauseGoHome",null,e);
      }catch (Error er){
        Utils.toLog(TAG, "cancelCurrentCauseGoHome",er,null);
      }
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public void continuMission(@NonNull final MissionStep steps,@NonNull final MissionSetting setting,@NonNull final Activity activity,final @NonNull DroneState droneState, final @NonNull OnMissionRunningListener callback) {
    setComputing(false);
    try {
      if (Utils.isNull(callback)){
        toLog(" continuMission callback is null");
        return;
      }else {
        if (Utils.isNull(steps)){
          toLog("continuMission steps is Null");
          callback.onError("continuMission steps is Null");
          return;
        }else {
          if (Utils.isNull(setting)){
            toLog("continuMission setting is Null");
            callback.onError("continuMission setting is Null");
            return;
          }else {
            if (Utils.isNull(activity)){
              toLog("continuMission activity is Null");
              callback.onError("continuMission activity is Null");
              return;
            }else {
              if (Utils.isNull(droneState)){
                toLog("continuMission droneState is Null");
                callback.onError("continuMission droneState is Null");
                return;
              }else {
                callback.onNotifyState(setting.getBattery(),setting.getImageCount(),setting.getSpeed(),setting.getFlyPoints());
                toLog(" continuMission Will Play: shouldCancelOrContinu");
                shouldCancelOrContinu(null,setting,steps,activity,droneState,callback);
              }
            }
          }
        }
      }
    }catch (Exception e){
      Utils.toLog(TAG, "continuMission",null,e);
    }catch (Error er){
      Utils.toLog(TAG, "continuMission",er,null);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public void isMappingMissionExist(final Context ctx, OnAsyncOperationCompleteBool callback) {
    if (Utils.isNull(callback)){
      toLog(" isMappingMissionExist callback is null");
      return;
    }else {
      if (Utils.isNull(ctx)){
        toLog("isMappingMissionExist ctx is Null");
        callback.onResultNo("isMappingMissionExist ctx is Null");
        return;
      }else {
        loadMission(ctx, new OnMissionProcessingComplete() {
          @Override
          public void onError() {
            toLog("isMappingMissionExist load existing failed");
            callback.onResultNo("isMappingMissionExist load existing failed");
            return;
          }

          @Override
          public void onSucces(@NonNull MissionStep steps) {
            if (Utils.isNull(steps)){
              toLog("isMappingMissionExist steps is Null");
              callback.onResultNo("isMappingMissionExist Cannot load Saved steps: Null");
              return;
            }else {
              if (!steps.hasNext()){
                toLog("isMappingMissionExistisMappingMissionExist steps is EMPTY");
                callback.onResultNo("isMappingMissionExist Cannot load Saved steps: EMPTY");
                return;
              }else {
                Utils.AsyncExecute(()->{
                  try {
                    String data = Save.defaultLoadString(Constants.PREF_WAYPOINT_PARAMETER,ctx);
                    if (Utils.isNullOrEmpty(data)){
                      toLog("isMappingMissionExist data is EMPTY or data");
                      callback.onResultNo("isMappingMissionExist Cannot load Saved steps: Parameters is empty or Null");
                      return;
                    }else {
                      MissionSetting setting = new MissionSetting(data);
                      if (!setting.isValid(null)){
                        toLog("isMappingMissionExist data is EMPTY or data");
                        callback.onResultNo("isMappingMissionExist Cannot load Saved steps: Parameters is Not valid");
                        return;
                      }else {
                        callback.onResultYes();
                        return;
                      }
                    }
                  }catch (Exception e){
                    Utils.toLog(TAG, "isMappingMissionExist",null,e);
                    if (Utils.isNull(callback)){
                      return;
                    }else {
                      callback.onResultNo("");
                    }
                  }catch (Error er){
                    Utils.toLog(TAG, "isMappingMissionExist",er,null);
                    if (Utils.isNull(callback)){
                      return;
                    }else {
                      callback.onResultNo("");
                    }
                  }
                });

              }
            }
          }
        });
      }
    }

  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public void cleanUserData(final Context ctx, OnAsyncOperationCompleteBool callback) {
    if (Utils.isNull(callback)){
      toLog(" cleanUserData callback is null");
      return;
    }else {
      if (Utils.isNull(ctx)){
        toLog("cleanUserData ctx is Null");
        callback.onResultNo("cleanUserData ctx is Null");
        return;
      }else {
        resetMemory(true,true,ctx, new OnAsyncOperationComplete() {
          @Override
          public void onError(@Nullable String errorDetail) {
            if (Utils.isNullOrEmpty(errorDetail)){
              errorDetail = "";
            }
            callback.onResultNo(errorDetail);
          }

          @Override
          public void onSucces(@Nullable String succesMsg) {
            callback.onResultYes();
          }
        });
      }
    }

  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public void performCheckList(@NonNull final MissionStep inMemoryMissionStep, @NonNull final MissionSetting inMemoryMissionSetting, @NonNull final Activity activity, final @NonNull DroneState droneState, @NonNull final OnCheckListChangeListener callback) {
    try {
      if (Utils.isNull(callback)){
        toLog(" performCheckList callback is null");
        return;
      }else {
        if (Utils.isNull(inMemoryMissionStep)){
          toLog("performCheckList inMemoryMissionStep is Null");
          callback.onFlyPlanStep(false);
          return;
        }else {
          if (Utils.isNull(inMemoryMissionSetting)){
            toLog("performCheckList inMemoryMissionSetting is Null");
            callback.onFlyPlanStep(false);
            return;
          }else {
            if (Utils.isNull(activity)){
              toLog("performCheckList activity is Null");
              callback.onFlyPlanStep(false);
              return;
            }else {
              if (Utils.isNull(droneState)){
                toLog("performCheckList droneState is Null");
                callback.onFlyPlanStep(false);
                return;
              }else {
                CameraParameter params = CameraParameter.getDJIPhantom4Advanced(inMemoryMissionSetting.getAltitudePrefered());
                if (Utils.isNull(params)){
                  toLog(" performCheckList params is Null");
                  callback.onFlyPlanStep(false);
                  return;
                }else {
                  GridSetting gridSetting = MissionDJI.calcGridSetting(params);
                  if (Utils.isNull(gridSetting)){
                    toLog(" performCheckList gridSetting is Null");
                    callback.onFlyPlanStep(false);
                    return;
                  }else {
                    saveMission(activity, inMemoryMissionStep,inMemoryMissionSetting, new OnMissionProcessingComplete() {
                      @Override
                      public void onError() {
                        toLog(" performCheckList saveMission failed");
                        callback.onFlyPlanStep(false);
                        return;
                      }

                      @Override
                      public void onSucces(@NonNull MissionStep himSelf) {
                        try {
                          toLog(" performCheckList saveMission onSucces");
                          CopyOnWriteArrayList<LatLng> stepPoints = himSelf.getFirst();
                          if (Utils.isNull(stepPoints)){
                            toLog(" performCheckList step Waypoint is Null");
                            callback.onFlyPlanStep(false);
                            return;
                          }else {
                            if (stepPoints.size() == 0){
                              toLog(" performCheckList step Waypoint is EMPTY");
                              callback.onFlyPlanStep(false);
                              return;
                            }else {
                              callback.onFlyPlanStep(true);
                              // Continu Mission
                              //Ini Mission Setting
                              if (droneState.getChargeRemainingInPercent() < Constants.LOW_BATTERY_LEVEL){
                                toLog("performCheckList LOW BATTERY. PLEASE CHARGE");
                                callback.onBatterieStep(false);
                                return;
                              }else {
                                callback.onBatterieStep(true);
                                if (droneState.getSatelliteCount() < Constants.MIN_SATELLITE_COUNT){
                                  toLog("performCheckList GPS FAILED. WAIT FOR BETTER GPS");
                                  callback.onGPSStep(false);
                                  return;
                                }else {
                                  if (Utils.isNull(droneState.getAircraft())){
                                    toLog("performCheckList droneState.getAircraft() Is Null");
                                    callback.onError("droneState.getAircraft() Is Null");
                                    return;
                                  }else {
                                    FlightController controller = droneState.getAircraft().getFlightController();
                                    if (Utils.isNull(controller)){
                                      toLog("performCheckList FlightController Is Null");
                                      callback.onError("FlightController Is Null");
                                      return;
                                    }else {
                                      final double droneLatitude = Utils.clone(droneState.getLatitude());
                                      final double droneLongitude = Utils.clone(droneState.getLongitude());
                                      if (droneLatitude == Constants.DOUBLE_NULL || droneLatitude == Constants.DOUBLE_NULL_2){
                                        toLog(" performCheckList Bad Drone latitute");
                                        callback.onGPSStep(false);
                                        return;
                                      }else {
                                        toLog(" performCheckList Drone Location right");
                                        if (droneLongitude == Constants.DOUBLE_NULL || droneLongitude == Constants.DOUBLE_NULL_2){
                                          toLog(" performCheckList Bad Drone droneLongitude");
                                          callback.onGPSStep(false);
                                          return;
                                        }else {
                                          if (Double.isNaN(droneLatitude)){
                                            setComputing(false);
                                            toLog(" performCheckList Bad Drone latitute : isNaN");
                                            callback.onGPSStep(false);
                                            return;
                                          }else {
                                            if (Double.isNaN(droneLongitude)){
                                              setComputing(false);
                                              toLog(" performCheckList Bad Drone droneLongitude :isNaN");
                                              callback.onGPSStep(false);
                                              return;
                                            }else {
                                              controller.setHomeLocation(new LocationCoordinate2D(droneLatitude, droneLongitude), (DJIError homeError) -> {
                                                try {
                                                  if (!Utils.isNull(homeError)){
                                                    String detail = homeError.getDescription();
                                                    if (Utils.isNull(detail)){
                                                      detail = "";
                                                    }
                                                    toLog(" performCheckList SET HOME LOCATION FAILED WAIT FOR BETTER GPS"+detail);
                                                    callback.onGPSStep(false);
                                                    return;
                                                  }else {
                                                    callback.onGPSStep(true);
                                                    toLog(" performCheckList SET HOME SUCCES");
                                                    resetMemory(true,null,activity, new OnAsyncOperationComplete() {
                                                      @Override
                                                      public void onError(@Nullable String errorDetail) {
                                                        if (Utils.isNull(errorDetail)){
                                                          errorDetail = "";
                                                        }
                                                        toLog("resetMemory error happen errorDetail: "+errorDetail);
                                                        callback.onError("resetMemory error happen");
                                                        return;
                                                      }

                                                      @Override
                                                      public void onSucces(@Nullable String succesMsg) {
                                                        try {
                                                          toLog(" performCheckList resetMemory Succes");
                                                          if (Utils.isNull(missionControl)){
                                                            toLog("performCheckList missionControl is Null ");
                                                            callback.onError("resetMemory error happen");
                                                            return;
                                                          }else {
                                                            // EveryThing is Okay Succes
                                                            callback.onSucces();
                                                            toLog(" performCheckList onSucces");
                                                            return;
                                                          }
                                                        }catch (Exception e){
                                                          Utils.toLog(TAG, "resetMemory(true onSucces",null,e);
                                                          if (Utils.isNull(callback)){
                                                            return;
                                                          }else {
                                                            callback.onError("Exception Happen");
                                                          }
                                                        }catch (Error er){
                                                          Utils.toLog(TAG, "resetMemory(true onSucces",er,null);
                                                          if (Utils.isNull(callback)){
                                                            return;
                                                          }else {
                                                            callback.onError("Error Happen");
                                                          }
                                                        }
                                                      }
                                                    });
                                                  }
                                                }catch (Exception e){
                                                  Utils.toLog(TAG, "setHomeLocation ",null,e);
                                                  if (Utils.isNull(callback)){
                                                    return;
                                                  }else {
                                                    callback.onError("Exception Happen");
                                                    return;
                                                  }
                                                }catch (Error er){
                                                  Utils.toLog(TAG, "setHomeLocation",er,null);
                                                  if (Utils.isNull(callback)){
                                                    return;
                                                  }else {
                                                    callback.onError("Error Happen");
                                                    return;
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
                        }catch (Exception e){
                          Utils.toLog(TAG, "saveMission onSucces",null,e);
                          if (Utils.isNull(callback)){
                            return;
                          }else {
                            callback.onError("Exception Happen");
                          }
                        }catch (Error er){
                          Utils.toLog(TAG, "saveMission onSucces",er,null);
                          if (Utils.isNull(callback)){
                            return;
                          }else {
                            callback.onError("Error Happen");
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

    }catch (Exception e){
      Utils.toLog(TAG, "performCheckList",null,e);
      if (Utils.isNull(callback)){
        return;
      }else {
        callback.onError("Exception Happen");
      }
    }catch (Error er){
      Utils.toLog(TAG, "performCheckList",er,null);
      if (Utils.isNull(callback)){
        return;
      }else {
        callback.onError("Error Happen");
      }
    }
  }
}
