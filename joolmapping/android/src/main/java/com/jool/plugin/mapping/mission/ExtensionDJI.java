package com.jool.plugin.mapping.mission;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.jool.plugin.mapping.interfaces.OnAsyncGridProcessComplete;
import com.jool.plugin.mapping.interfaces.OnProcessingCompleteListener;
import com.jool.plugin.mapping.mapping.Grid;
import com.jool.plugin.mapping.mapping.Mapping;
import com.jool.plugin.mapping.mapping.PointLatLngAlt;
import com.jool.plugin.mapping.model.DroneDJI;
import com.jool.plugin.mapping.utils.Constants;
import com.jool.plugin.mapping.utils.Utils;

import java.util.concurrent.CopyOnWriteArrayList;


public class ExtensionDJI {
    private final String TAG = getClass().getSimpleName();



    private void toLog( @NonNull String msg){
        Utils.toLog(TAG, msg);
    }

  public void getMissionInfo(final @NonNull OnProcessingCompleteListener callback,final @NonNull Activity activity, final @NonNull MissionSetting missionSetting,final @NonNull CopyOnWriteArrayList<LatLng> coords) {
    try {
      toLog("getMissionInfo fired");
      if (Utils.isNull(callback)){
        toLog("getMissionInfo callback is Null");
        return;
      }else {
        if (Utils.isNull(activity)){
          toLog(" getMissionInfo activity is null");
          callback.onError(" getMissionInfo activity is null");
          return;
        }else {
          if (Utils.isNull(coords)){
            toLog(" getMissionInfo points is null");
            callback.onError(" getMissionInfo points is null");
            return;
          }else {
            if (Utils.isNull(missionSetting)){
              toLog(" getMissionInfo MissionSetting isNull");
              callback.onError(" getMissionInfo MissionSetting isNull");
              return;
            }else {
              if (!missionSetting.isValid(null)){
                String msg = " getMissionInfo "+missionSetting.getErrorMsg();
                toLog(msg);
                callback.onError(msg);
                return;
              }else {
                if (Utils.isNull(missionSetting.getHomeLocation())){
                  toLog(" getMissionInfo missionSetting.HomeLocation is Null");
                  callback.onError("missionSetting.HomeLocation is Null");
                  return;
                }else {
                  CameraParameter params = CameraParameter.getDJIPhantom4Advanced(missionSetting.getAltitudePrefered());
                  if (Utils.isNull(params)){
                    toLog(" getMissionInfo params is Null");
                    callback.onError(" getMissionInfo homeLocation is BAD");
                    return;
                  }else {
                    GridSetting setting  = MissionDJI.calcGridSetting(params);
                    if (Utils.isNull(setting)){
                      toLog(" getMissionInfo setting is Null");
                      callback.onError(" getMissionInfo setting is Null");
                      return;
                    }else {
                      PointLatLngAlt home = new PointLatLngAlt(missionSetting.getHomeLocation().latitude,missionSetting.getHomeLocation().longitude);
                      CopyOnWriteArrayList<PointLatLngAlt> list =  new CopyOnWriteArrayList<>();
                      for (LatLng current: coords) {
                        list.add(new PointLatLngAlt(current.latitude, current.longitude,0));
                      }

                      toLog("getMissionInfo Will generateSurveyPath");
                      toLog("getMissionInfo Altitude "+setting.getAltitude() +" Distance:"+setting.getDistance()+" Spacing:"+setting.getSpacing()+ " getAngle:"+setting.getAngle() +" OverShoot1:"+setting.getOverShoot1()
                        +" getOverShoot2:"+setting.getOverShoot2()+ " Leadin1"+setting.getLeadin1()+" Leadin2:"+setting.getLeadin2() +" homeLatitude "+home.getLat()+ " homeLong"+home.getLng());
                      Mapping.generateSurveyPath(list, setting.getAltitude(), setting.getDistance(), setting.getSpacing(), setting.getAngle(), setting.getOverShoot1(), setting.getOverShoot2(),
                        Grid.StartPosition.Point, false, 0, setting.getLeadin1(), setting.getLeadin2(),home, false,
                        new Mapping.OnComputingComplete() {

                          @Override
                          public void OnError() {
                            toLog("getMissionInfo OnError");
                            callback.onError(" Cannot Generate GRID PLEASE VERIFY POLYGONE AND CAMERA PARAMETER");
                            return;
                          }

                          @RequiresApi(api = Build.VERSION_CODES.N)
                          @Override
                          public void OnSucces(@NonNull CopyOnWriteArrayList<PointLatLngAlt> grid) {
                            try {
                              if (grid == null){
                                toLog("getMissionInfo onCreate grid IS NULL");
                                callback.onError(" Cannot Generate GRID");
                                return;
                              }else{
                                if (grid.size() == 0){
                                  toLog("getMissionInfo Is empty");
                                  callback.onError("Grid is Empty");
                                  return;
                                }else {
                                  toLog("getMissionInfo grid SIZE "+grid.size());
                                  CopyOnWriteArrayList<LatLng> flyPoints = new CopyOnWriteArrayList<>();
                                  // Count All Step Images
                                  double distanceInMeters = Constants.DOUBLE_NULL;
                                  LatLng previous = null;
                                  for (PointLatLngAlt item: grid) {
                                    try {
                                      if (MissionDJI.isRightWaypoint(item)){
                                        LatLng current = new LatLng(Utils.clone(item.getLat()), Utils.clone(item.getLng()));
                                        flyPoints.add(current);
                                        if (Utils.isNull(previous)){
                                          // Case First Point
                                        }else {
                                          distanceInMeters += SphericalUtil.computeDistanceBetween(previous,current);
                                        }
                                        previous = null;
                                        previous = new LatLng(current.latitude,current.longitude);
                                      }
                                    }catch (Exception e){
                                      Utils.toLog(TAG, "grid.forEach",null,e);
                                    }catch (Error er){
                                      Utils.toLog(TAG, "grid.forEach",er,null);
                                    }
                                  }
                                  if (distanceInMeters <= 0){
                                    toLog(" getMissionInfo distanceInMeters <= 0");
                                    callback.onError("BAD distanceInMeters <= 0");
                                    return;
                                  }else {
                                    // All Step TakeOff go To Altitude -> go to First Waypopint+ Mission
                                    // From LastPoint go to HomePoint + TakeOff
                                    final double mappingTime = distanceInMeters / missionSetting.getSpeed();
                                    int batteriesCount = 0;
                                    if (mappingTime <= Constants.TIME_16_MINUTES_TO_SECONDES){
                                      // One Battery for 0 to 16 Min(960 Sec)
                                      batteriesCount = 1;
                                    }else {
                                      // Arrondi ceil(5.2) = 6.0
                                      double missionTime = Utils.clone(mappingTime); // Made Copy
                                      batteriesCount = (int) Math.ceil(missionTime / Constants.TIME_16_MINUTES_TO_SECONDES);
                                    }

                                    int imageCount = (int) Math.ceil(mappingTime / missionSetting.getShootIntervalInSec());
                                    if (missionSetting.getArea() >= Constants.HECTARE_100 ){
                                      imageCount = (int) (imageCount + imageCount * 0.186);
                                    }else {
                                      if (missionSetting.getArea() >= Constants.HECTARE_50){
                                        imageCount = (int) (imageCount + imageCount * 0.273);
                                      }else {
                                        if (missionSetting.getArea() >= Constants.HECTARE_6){
                                          imageCount = (int) (imageCount + imageCount * 0.486);
                                        }else {
                                          imageCount = (int) (imageCount + imageCount * 0.200);
                                        }
                                      }
                                    }
                                    toLog("MISSION Result distanceInMeters:"+distanceInMeters+ " batterie "+batteriesCount+" imageCount "+imageCount);
                                    toLog("MISSION Result Succes Speed():"+missionSetting.getSpeed()+ " shootIntervalInSec "+missionSetting.getShootIntervalInSec());

                                    missionSetting.setBattery(batteriesCount);
                                    missionSetting.setImageCount(imageCount);
                                    // Notify image Count
                                    MissionStep missionStep = new MissionStep();

                                    missionStep.addAllOk(flyPoints, new OnAsyncGridProcessComplete() {
                                      @Override
                                      public void onError(@Nullable String errorDetail) {
                                        toLog(" getMissionInfo Convert Mission to StepList of 99 points failed");
                                        callback.onError("Convert Mission to StepList of 99 points failed");
                                        return;
                                      }

                                      @Override
                                      public void onSucces(@NonNull final CopyOnWriteArrayList<LatLng> flyPointsAfterRebuild) {
                                        missionSetting.setFlyPoints(flyPointsAfterRebuild);
                                        callback.onSucces(mappingTime,missionSetting.getImageCount(),missionSetting.getBattery(),missionSetting.getSpeed(),missionSetting.getFlyPoints(),
                                          missionSetting,missionStep);
                                        return;
                                      }
                                    });
                                  }
                                }
                              }
                            }catch (Exception e){
                              Utils.toLog(TAG, "getMissionInfo",null,e);
                              if (Utils.isNull(callback)){
                                return;
                              }else {
                                callback.onError("Error");
                              }
                            }catch (Error er){
                              Utils.toLog(TAG, "getMissionInfo",er,null);
                              if (Utils.isNull(callback)){
                                return;
                              }else {
                                callback.onError("Error");
                              }
                            }
                          }

                          @Override
                          public void OnNotifyState(int percentGoToUIThread) {
                            callback.onGridProgressing(percentGoToUIThread);
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
      Utils.toLog(TAG, "getMissionInfo",null,e);
      if (Utils.isNull(callback)){
        return;
      }else {
        callback.onError("Error");
      }
    }catch (Error er){
      Utils.toLog(TAG, "getMissionInfo",er,null);
      if (Utils.isNull(callback)){
        return;
      }else {
        callback.onError("Error");
      }
    }
  }
}
