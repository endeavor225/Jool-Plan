package com.wefly.tracking;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.wefly.tracking.model.ResultCode;

import org.json.JSONArray;

@NativePlugin
public class MainServiceModulePlugin extends Plugin {

  private final String TAG = "MainServiceModule";
  static final String ACTION_EVENT = (
    MainServiceModulePlugin.class.getPackage().getName() + ".broadcast.event"
  );

  private  boolean hasSave= false;
  private Context ctx  = null;
  private volatile boolean allowNextWatch= false,isDoingCurrentPosition = false;

  private PluginCall currentBridge = null;

  public PluginCall getCurrentBridge()throws NullPointerException {
    return currentBridge;
  }

  public boolean isDoingCurrentPosition() {
    return isDoingCurrentPosition;
  }

  public void setDoingCurrentPosition(boolean doingCurrentPosition) {
    isDoingCurrentPosition = doingCurrentPosition;
  }

  public void setCurrentBridge(PluginCall currentBridge) {
    this.currentBridge = null;
    this.currentBridge = currentBridge;
  }

  public boolean isAllowNextWatch() {
    return allowNextWatch;
  }

  public void setAllowNextWatch(boolean allowNextWatch) {
    this.allowNextWatch = allowNextWatch;
  }

  @Override
  public void load() {
    super.load();
    ctx = this.getContext();

    LocalBroadcastManager.getInstance(ctx).registerReceiver(
      new ReceiverModule(),
      new IntentFilter(MainService.ACTION_BROADCAST)
    );

    setAllowNextWatch(true);
    setDoingCurrentPosition(false);
    PermissionUtil pm = new PermissionUtil(ctx);
    if(pm.isAllPermissionsGranded(false)){
      startTracking("myUser");
    }else {
      Utils.logIt(TAG, " CANNOT START SERVICE CAUSE MISSING GPS AND OTHER PERMISSION");
    }


  }



  @PluginMethod()
  public void echo(PluginCall call) {

    JSObject ret = new JSObject();
    ret.put("value", " lilou test from JAVA");
    call.success(ret);
  }

  @PluginMethod()
  public void stopTracking(){
    MainService.stopTrackUser(ctx);
  }

  @PluginMethod()
  public void ini(PluginCall call){
    Utils.logIt(TAG, " ini Start ");
    try {
      String keyName = "interval";
      if (!call.getData().has(keyName)) {
        Utils.logIt(TAG, " ini Must provide an interval ");

        call.reject("Must provide an interval");
        return;
      }else {
        Utils.logIt(TAG, " ini  keyName isValid");

        @Nullable Integer intervalInMin = call.getInt(keyName);
        ConstantsRes c = new ConstantsRes();
        if (intervalInMin == null){
          intervalInMin = ConstantsRes.TRACKING_30_MIN;
        }else {
          Utils.logIt(TAG, " ini  intervalInMin isValid");
          if (intervalInMin <= 0){
            intervalInMin = ConstantsRes.TRACKING_30_MIN;
          }
          Save.defaultSaveInt(c.PREF_TRACKING_CUSTOM_INTERVAL,intervalInMin, ctx);
          call.resolve();

          Utils.logIt(TAG, " ini  intervalInMin defaultSaveInt Succes");
          return;



        }

      }
    }catch (Exception e){
      Utils.logIt(TAG, " ini  Exception happen");

      e.printStackTrace();
      call.reject(e.getLocalizedMessage(), e);
    }catch (Error er){
      Utils.logIt(TAG, " ini  Error happen");

      er.printStackTrace();
      call.reject(er.getLocalizedMessage());
    }
    call.reject("bad Setting");

    Utils.logIt(TAG, " ini  DEFAULT CASE bad Setting");



  }

  @PluginMethod()
  public void getAllPreviousPositions(PluginCall call){
    Utils.logIt(TAG, " getAllPreviousPositions  Start");

    ConstantsRes c = new ConstantsRes();
    JSONArray res = null;
    try {
      final Context context = ctx;
      String data = Save.defaultLoadString(c.PREF_TRACKING_OBJ_LIST, context);
      if (data == null){
        Utils.logIt(TAG, " getAllPreviousPositions  data Is Null");

        res = new JSONArray(c.PREF_EMPTY_LIST);
      }else {
        if (data.trim().toLowerCase().contentEquals("")){
          Utils.logIt(TAG, " getAllPreviousPositions  data Is Empty");

          res = new JSONArray(c.PREF_EMPTY_LIST);
        }else {
          JSONArray arraysTracking = new JSONArray(data);
          if (arraysTracking.length() == 0){
            Utils.logIt(TAG, " getAllPreviousPositions  arraysTracking Is Empty");

            res = new JSONArray(c.PREF_EMPTY_LIST);
          }else {
            Utils.logIt(TAG, " getAllPreviousPositions  arraysTracking Is Full");

            res = new JSONArray(data);
          }
        }
        // Clean old
        Save.defaultSaveString(c.PREF_TRACKING_OBJ_LIST, c.PREF_EMPTY_LIST,ctx);

        JSObject ret = new JSObject();
        ret.put("value", res.toString());
        call.success(ret);

        Utils.logIt(TAG, " getAllPreviousPositions  success Done");
        return;
      }
    }catch (Exception e){
      Utils.logIt(TAG, " getAllPreviousPositions  Exception");

      e.printStackTrace();
      call.reject(e.getLocalizedMessage(), e);
    }catch (Error er){
      Utils.logIt(TAG, " getAllPreviousPositions  Error Happen");

      er.printStackTrace();
      call.reject(er.getLocalizedMessage());
    }


  }

  @PluginMethod()
  public void getCurrentPosition(PluginCall call){
    Utils.logIt(TAG, " getCurrentPosition  Start");
    try {
      if (isDoingCurrentPosition()){
        Utils.logIt(TAG, " getCurrentPosition  ALREADY DoingCurrentPosition");
        return;
      }else {
        setDoingCurrentPosition(true);
        PermissionUtil pm = new PermissionUtil(ctx);
        if(!pm.isAllPermissionsGranded(false)){
          setDoingCurrentPosition(false);
          Utils.logIt(TAG, " CANNOT START SERVICE CAUSE MISSING GPS AND OTHER PERMISSION");
          call.reject(" REQUIRE ALL PERMISSIONS");
          return;
        }else {
          // Keep At the End of function
          call.save();
          ConstantsRes c = new ConstantsRes();
          Utils.logIt(ConstantsRes.APP_NAME, TAG + " getCurrentPosition fired");
          // Send Position Signal
          Intent intent = new Intent(ACTION_EVENT);
          intent.putExtra(c.BUNDLE_CODE, ResultCode.GET_CURRENT_LOCATION.toInteger());
          intent.putExtra(c.BUNDLE_ID, call.getCallbackId());
          sendToModule(intent);

          Utils.logIt(ConstantsRes.APP_NAME, TAG + " getCurrentPosition sended");
        }
      }

    }catch (Exception e){
      setDoingCurrentPosition(false);
      e.printStackTrace();
      call.reject(e.getLocalizedMessage(), e);
    }catch (Error er){
      setDoingCurrentPosition(false);
      er.printStackTrace();
      call.reject(er.getLocalizedMessage());
    }

  }

  @PluginMethod(returnType=PluginMethod.RETURN_CALLBACK)
  public void watchPosition(PluginCall call){
    try {
      if (!isAllowNextWatch()){
        return;
      }else {
        PermissionUtil pm = new PermissionUtil(ctx);
        if(!pm.isAllPermissionsGranded(false)){
          Utils.logIt(TAG, " CANNOT START SERVICE CAUSE MISSING GPS AND OTHER PERMISSION");
          call.reject(" REQUIRE ALL PERMISSIONS");
        }else {
          ConstantsRes c = new ConstantsRes();
          if (!call.getData().has(c.BUNDLE_TIME)) {
            Utils.logIt(TAG, " watchPosition Must provide a timeout ");
            call.reject("Must provide a timeout");
            return;
          }else {
            @Nullable Integer intervalInSec = call.getInt(c.BUNDLE_TIME);
            if (Utils.isNull(intervalInSec)){
              Utils.logIt(TAG, " watchPosition timeout Not valid : It's  null ,Must provide a timeout ");
              call.reject("timeout Not valid , Must provide a timeout");
              return;
            }else {
              if (intervalInSec < c.TIME_MIN){
                Utils.logIt(TAG, " watchPosition timeout Not valid , Must be greater or equal 1");
                call.reject("timeout Not valid , Must be greater or equal 1");
                return;
              }else {
                if (intervalInSec > c.TIME_MAX){
                  Utils.logIt(TAG, " watchPosition timeout Not valid , Must be less or equal "+c.TIME_MAX);
                  call.reject("timeout Not valid , Must be less or equal "+c.TIME_MAX);
                  return;
                }else {
                  // Keep At the End of function
                  call.save();
                  Utils.logIt(ConstantsRes.APP_NAME, TAG + " watchPosition fired");
                  // Send Position Signal
                  Intent intent = new Intent(ACTION_EVENT);
                  intent.putExtra(c.BUNDLE_CODE, ResultCode.SUSCRIBE_LOCATION_UPDATE.toInteger());
                  intent.putExtra(c.BUNDLE_ID, call.getCallbackId());
                  intent.putExtra(c.BUNDLE_TIME, intervalInSec);
                  // Remeber dont Send Again
                  setAllowNextWatch(false);
                  sendToModule(intent);

                  Utils.logIt(ConstantsRes.APP_NAME, TAG + " watchPosition sended");
                }
              }
            }
          }
        }
      }
    }catch (Exception e){
      e.printStackTrace();
      call.reject(e.getLocalizedMessage(), e);
    }catch (Error er){
      er.printStackTrace();
      call.reject(er.getLocalizedMessage());
    }

  }

  @PluginMethod()
  public void stopWatchPosition(PluginCall call){
    Utils.logIt(ConstantsRes.APP_NAME, TAG + " stopWatchPosition fired");

    try {
      PermissionUtil pm = new PermissionUtil(ctx);
      if(!pm.isAllPermissionsGranded(false)){
        Utils.logIt(TAG, " CANNOT START SERVICE CAUSE MISSING GPS AND OTHER PERMISSION");
        call.reject(" REQUIRE ALL PERMISSIONS");
      }else {
        // Keep At the End of function
        call.save();
        ConstantsRes c = new ConstantsRes();
        Utils.logIt(ConstantsRes.APP_NAME, TAG + " stopWatchPosition Start");
        // Send Position Signal
        Intent intent = new Intent(ACTION_EVENT);
        intent.putExtra(c.BUNDLE_CODE, ResultCode.DESUSCRIBE_LOCATION_UPDATE.toInteger());
        intent.putExtra(c.BUNDLE_ID, call.getCallbackId());
        setAllowNextWatch(true);
        sendToModule(intent);


        Utils.logIt(ConstantsRes.APP_NAME, TAG + " stopWatchPosition sended");
      }
    }catch (Exception e){
      e.printStackTrace();
      call.reject(e.getLocalizedMessage(), e);
    }catch (Error er){
      er.printStackTrace();
      call.reject(er.getLocalizedMessage());
    }
  }

  @PluginMethod()
  public void isDeviceSupported(PluginCall call){

    Utils.logIt(ConstantsRes.APP_NAME, TAG + " isDeviceSupported fired");
    try {
      PermissionUtil pm = new PermissionUtil(ctx);
      if(!pm.isAllPermissionsGranded(false)){
        Utils.logIt(TAG, " CANNOT START SERVICE CAUSE MISSING GPS AND OTHER PERMISSION");
        call.reject(" REQUIRE ALL PERMISSIONS");
      }else {
        // Keep At the End of function
        call.save();
        ConstantsRes c = new ConstantsRes();
        // Send Position Signal
        Intent intent = new Intent(ACTION_EVENT);
        intent.putExtra(c.BUNDLE_CODE, ResultCode.PRECISED_POINT_NOT_FOUND.toInteger());
        intent.putExtra(c.BUNDLE_ID, call.getCallbackId());
        sendToModule(intent);

        Utils.logIt(ConstantsRes.APP_NAME, TAG + " isDeviceSupported sended");
      }
    }catch (Exception e){
      e.printStackTrace();
      call.reject(e.getLocalizedMessage(), e);
    }catch (Error er){
      er.printStackTrace();
      call.reject(er.getLocalizedMessage());
    }

  }

  public void startTracking(String trackingConfig ) {

    // Listen DbUnitwork change import android.os.Build;
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
      MainService.startTrackNewUser(trackingConfig, ctx);

      scheduleJob();
    }else {
      Utils.logIt(TAG,"startTracking SDK_INT LOWER THAN LOLLIPOP");
    }

    if (!hasSave()){
      BootConnectivityReceiver.grantFire(ctx);
      setHasSave(true);

      Utils.logIt(TAG,"startTracking HasSave OKay");
    }else {
      Utils.logIt(TAG,"startTracking HasSave OKay FAILED");
    }
  }

  private static JSObject formatLocation(String location)throws Exception {
    JSObject obj = new JSObject();
    ConstantsRes c = new ConstantsRes();
    if (location == null){
      return obj;
    }else {
      obj.put(c.BUNDLE_LOCATION, location);
    }
    return obj;
  }

  private static JSObject formatLocationCheck(String location)throws Exception {
    JSObject obj = new JSObject();
    ConstantsRes c = new ConstantsRes();
    if (location == null){
      return obj;
    }else {
      if (location.trim().contentEquals("")){
        obj.put(c.BUNDLE_SUPPORTED, false);
        obj.put(c.BUNDLE_LOCATION, "");
      }else {
        obj.put(c.BUNDLE_SUPPORTED, true);
        obj.put(c.BUNDLE_LOCATION, location);
      }

    }
    return obj;
  }


  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private void scheduleJob() {
    try {
      if (BootConnectivityReceiver.stopFired(ctx)){
        JobInfo myJob = new JobInfo.Builder(0, new ComponentName(ctx, BootNetworkSchedulerService.class))
          .setRequiresCharging(false)
          .setMinimumLatency(1000)
          .setOverrideDeadline(3000)
          .setPersisted(true)
          .build();

        JobScheduler jobScheduler = (JobScheduler) ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(myJob);
      }
    }catch (Exception e){
      e.printStackTrace();
    }

  }

  public boolean hasSave() {
    return hasSave;
  }

  public void setHasSave(boolean hasSave) {
    this.hasSave = hasSave;
  }

  // Receives messages from the service.
  private class ReceiverModule extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      Utils.logIt(ConstantsRes.APP_NAME, TAG + "ReceiverModule onReceive Start ");
      try {
        if (context == null || intent == null){
          return;
        }else {
          ConstantsRes c = new ConstantsRes();
          String id = intent.getStringExtra(c.BUNDLE_ID);
          int code = intent.getIntExtra(c.BUNDLE_CODE,0);
          String location = intent.getStringExtra(c.BUNDLE_LOCATION);
          ResultCode mCode = ResultCode.find(code);
          if (mCode == null){
            mCode = ResultCode.DEFAULT;
          }
          if (location == null){
            location = "";
          }
          if (id == null){
            id = "";
          }

          Utils.logIt(ConstantsRes.APP_NAME, TAG + "ReceiverModule onReceive id "+id + " mCode" + mCode.toInteger() );
          if (id.trim().contentEquals("")){
            Utils.logIt(ConstantsRes.APP_NAME, TAG + "ReceiverModule onReceive id  is Null or Empty ");
          }else {
            PluginCall call = bridge.getSavedCall(id);
            if (call == null) {
              Utils.logIt(ConstantsRes.APP_NAME, TAG + "ReceiverModule onReceive Bridge Is Null ");
              return;
            }else {
              Utils.logIt(ConstantsRes.APP_NAME, TAG + "ReceiverModule onReceive Bridge IS OKay ");

              setDoingCurrentPosition(false);
              switch (mCode){
                case GET_CURRENT_LOCATION:
                  call.resolve(formatLocation(location));
                  call.release(bridge);
                  Utils.logIt(ConstantsRes.APP_NAME, TAG + "ReceiverModule GetCurrentPosition SUCCES id "+id + " location "+location);
                  return;
                case SUSCRIBE_LOCATION_UPDATE:
                  call.success(formatLocation(location));
                  Utils.logIt(ConstantsRes.APP_NAME, TAG + "ReceiverModule watchPosition SUCCES id "+id + " location "+location);
                  return;
                case PRECISED_POINT_NOT_FOUND:
                  // Device not supported
                  call.resolve(formatLocationCheck(location));
                  call.release(bridge);
                  Utils.logIt(ConstantsRes.APP_NAME, TAG + "ReceiverModule isDeviceSupported SUCCES id "+id + " location "+location);
                  return;
                case DESUSCRIBE_LOCATION_UPDATE:
                  Utils.logIt(ConstantsRes.APP_NAME, TAG + "ReceiverModule Desuscribe SUCCES id "+id + " location "+location);
                  setAllowNextWatch(true);
                  call.success();
                  call.release(bridge);
                  return;

              }

              Utils.logIt(ConstantsRes.APP_NAME, TAG + "ReceiverModule onReceive END DEFAULT CASE ");
            }
          }
        }
      }catch (Exception e){
        e.printStackTrace();
      }catch (Error er){
        er.printStackTrace();
      }


    }
  }

  private void sendToModule(Intent intent) {
    try {
      if (intent == null || ctx == null){
        return;
      }else {
        LocalBroadcastManager.getInstance(
          ctx
        ).sendBroadcast(intent);
      }
    }catch (Exception e){
      e.printStackTrace();
    }catch (Error er){
      er.printStackTrace();
    }
  }


}
