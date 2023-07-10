package com.wefly.tracking;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.StrictMode;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.wefly.tracking.WeflyTracking.R;
import com.wefly.tracking.model.ResultCode;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Created by Obrina.KIMI on 1/10/2018.
 */

public class MainService extends Service implements LocationListener{
    private final String TAG = getClass().getSimpleName();
    static final String ACTION_BROADCAST = (
            MainService.class.getPackage().getName() + ".broadcast"
    );
    ScheduledExecutorService schTracker, schUpload, schWatcher;
    private @Nullable volatile String idCurrentPos = null, idPointNotFound = null,
            idLocationUpdate = null, idDesuscribe = null;
    private PowerManager.WakeLock wakeLock = null;
    private volatile static boolean isRunning;
    Intent intent;
    public static String str_receiver = "gps.service.receiver";
    public @Nullable AlertDialog aDialog = null;
    private PermissionUtil pm = null;
    private volatile boolean notifyStarted, sending = false, stopWatching= false, stopLooping = false;

    CopyOnWriteArrayList<Location> locations = new CopyOnWriteArrayList<>();
    int motionLoopCount = 0;
    private @Nullable Location changed = null;

    // ---> add Me 1 Instance Gradle HttpRequest dependencies: implementation 'org.jbundle.util.osgi.wrapped:org.jbundle.util.osgi.wrapped.org.apache.http.client:4.1.2'


    @Nullable
    public String getIdCurrentPos()throws NullPointerException {
        return idCurrentPos;
    }

    public void setIdCurrentPos(@Nullable String idCurrentPos) {
        this.idCurrentPos = idCurrentPos;
    }

    @Nullable
    public String getIdPointNotFound()throws NullPointerException {
        return idPointNotFound;
    }

    public void setIdPointNotFound(@Nullable String idPointNotFound) {
        this.idPointNotFound = idPointNotFound;
    }

    @Nullable
    public String getIdLocationUpdate()throws NullPointerException {
        return idLocationUpdate;
    }

    public void setIdLocationUpdate(@Nullable String idLocationUpdate) {
        this.idLocationUpdate = idLocationUpdate;
    }

    @Nullable
    public String getIdDesuscribe()throws NullPointerException {
        return idDesuscribe;
    }

    public void setIdDesuscribe(@Nullable String idDesuscribe) {
        this.idDesuscribe = idDesuscribe;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean needStopLooping() {
        return stopLooping;
    }

    public void setStopLooping(boolean stopLooping) {
        this.stopLooping = stopLooping;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Utils.logIt(TAG, " onCreate fired");

        resetIds();
        setSending(false);
        setStopWatching(false);
        setStopLooping(false);
        this.resetMotionCount();

        try {

            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(
                        getPackageName()
                );
                if (launchIntent != null) {
                    Intent notificationIntent = launchIntent;
                    PendingIntent pendingIntent=PendingIntent.getActivity(this,0,notificationIntent,0);
                    NotificationChannel channel = new NotificationChannel(ConstantsRes.APP_CHANNEL_ID,
                            ConstantsRes.APP_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
                    Notification notification = new NotificationCompat.Builder(this, ConstantsRes.APP_CHANNEL_ID)
                            .setContentTitle("")
                            .setContentIntent(pendingIntent)
                            .setSmallIcon(R.drawable.ic_notification_default)
                            .setContentText("").build();
                    startForeground(ConstantsRes.NOTIFICATION_APP_ID, notification);
                }

            }

            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            setRunning(true);


            LocalBroadcastManager.getInstance(this.getBaseContext()).registerReceiver(
                    new ReceiverService(),
                    new IntentFilter(MainServiceModulePlugin.ACTION_EVENT)
            );
        }catch (Exception e){
            e.printStackTrace();
        }catch (Error er){
            er.printStackTrace();
        }


        intent = new Intent(str_receiver);


        pm = new PermissionUtil(getBaseContext());

        if(pm.isAllPermissionsGranded(false)){
            try {
                PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        TAG);
                if (!wakeLock.isHeld()){
                    wakeLock.acquire();
                }
            }catch (Exception e){
                e.printStackTrace();
            }catch (Error er){
                er.printStackTrace();
            }

        }


    }

    private void resetIds() {
        setIdCurrentPos(null);
        setIdDesuscribe(null);
        setIdLocationUpdate(null);
        setIdPointNotFound(null);
    }

    private void resetMotionCount() {
        synchronized (MainService.this){
            this.motionLoopCount = 0;
        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        // Start and Keep running
        return START_STICKY;

    }


    @Override
    public void onDestroy() {

        setRunning(false);
        setSending(false);
        setStopWatching(false);
        setStopLooping(false);

        // Remove All Scheduler
        removeTracking();
        removeUploadTask();

        //reset Ids
        resetIds();



        if(wakeLock != null){
            wakeLock.release();
        }

        super.onDestroy();
    }



    public String convertMinToHourMin(int min){
        SimpleDateFormat sdf = new SimpleDateFormat("mm");

        try {
            Date dt = sdf.parse(String.valueOf(min));
            sdf = new SimpleDateFormat("HH:mm");
            return sdf.format(dt);
        } catch (ParseException e) {
            e.printStackTrace();
        }catch (Error er){
            er.printStackTrace();
        }
        return "";
    }

    public void uploadLocation(){
        removeUploadTask();
        schUpload =
                Executors.newSingleThreadScheduledExecutor();
        schUpload.scheduleAtFixedRate
                (new Runnable() {
                    @Override
                    public void run() {
                        // check if 5 min
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                // send time change Request
                                synchronized (MainService.this){
                                    final Context ctx = getBaseContext();
                                    //sendAllRemoved(ctx);
                                }
                            }

                        });
                    }
                }, 0, ConstantsRes.UPLOAD_1_MIN, TimeUnit.MINUTES);


    }


    public void recordLocation() {
        Utils.logIt(ConstantsRes.APP_NAME, TAG + " recordLocation Start ");

        setRunning(true);
        if (pm != null && pm.isAllPermissionsGranded(false)){
            // Listen Synchornisation
            try {
                if (wakeLock != null){
                    if (!wakeLock.isHeld()){
                        wakeLock.acquire();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }catch (Error er){
                er.printStackTrace();
            }

            Utils.logIt(ConstantsRes.APP_NAME, TAG + " recordLocation Permission OK");


            try {
                if (!isNotifyStarted()){
                    // Disable Old instance
                    removeTracking();

                    // Use New Instance
                    setNotifyStarted(true);

                    ConstantsRes c = new ConstantsRes();
                    int interval = Save.defaultLoadInt(c.PREF_TRACKING_CUSTOM_INTERVAL,getBaseContext());
                    if (interval <= 0 || interval > ConstantsRes.HOUR_24_IN_MIN){
                        interval = ConstantsRes.TRACKING_30_MIN;
                    }

                    Utils.logIt(ConstantsRes.APP_NAME, TAG + " recordLocation Will scheduleAtFixedRate");
                    schTracker =
                            Executors.newSingleThreadScheduledExecutor();
                    schTracker.scheduleAtFixedRate
                            (new Runnable() {
                                public void run() {
                                    setRunning(true);
                                    // check if 30 min
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // send time change Request
                                            synchronized (MainService.this){
                                                Utils.logIt(ConstantsRes.APP_NAME, TAG + " recordLocation check if 30 min Start ");

                                                final Context ctx = getBaseContext();
                                                final ConstantsRes c = new ConstantsRes();
                                                try {
                                                    // Get GPS position
                                                    try {

                                                        Location loc                                = null;
                                                        loc = getCurrentLocation();

                                                        if (loc == null){
                                                            // Not Location found
                                                            Utils.logIt(ConstantsRes.APP_NAME, TAG + " recordLocation Location Not Location found");
                                                            return;
                                                        }else {
                                                            Utils.logIt(ConstantsRes.APP_NAME, TAG + " recordLocation Location found ");

                                                            final Location locCopy = loc;
                                                            AsyncTask.execute(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    try {
                                                                        // Add to Old List
                                                                        String oldData = Save.defaultLoadString(c.PREF_TRACKING_OBJ_LIST, ctx);
                                                                        if (oldData == null){
                                                                            return;
                                                                        }else {
                                                                            // Empty Not accept
                                                                            if (oldData.trim().toLowerCase().contentEquals("")){
                                                                                return;
                                                                            }else {
                                                                                JSONArray allItems = new JSONArray(oldData);

                                                                                JSONObject currentPos = locationToJSON(locCopy);
                                                                                if (currentPos == null){
                                                                                    return;
                                                                                }else {
                                                                                    allItems.put(currentPos);

                                                                                    // Save All
                                                                                    boolean isSaveOk = Save.defaultSaveString(c.PREF_TRACKING_OBJ_LIST, allItems.toString(), ctx);
                                                                                    Utils.logIt(ConstantsRes.APP_NAME, TAG + " recordLocation Location found NORMAL TRACKING SAVE OK ");

                                                                                    return;
                                                                                }
                                                                            }
                                                                        }
                                                                    }catch (Exception e){
                                                                        e.printStackTrace();

                                                                    }catch (Error er){
                                                                        er.printStackTrace();

                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }catch (Exception ex){
                                                        ex.printStackTrace();
                                                    }catch (Error er){
                                                        er.printStackTrace();
                                                    }
                                                }catch (Exception e){
                                                    e.printStackTrace();
                                                }catch (Error er){
                                                    er.printStackTrace();
                                                }

                                            }
                                        }
                                    });

                                }
                            }, 0, interval, TimeUnit.MINUTES);
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
            if (intent == null){
                return;
            }else {
                LocalBroadcastManager.getInstance(
                        getApplicationContext()
                ).sendBroadcast(intent);
            }
        }catch (Exception e){
            e.printStackTrace();
        }catch (Error er){
            er.printStackTrace();
        }


    }

    private Location getCurrentLocation(){
        synchronized (MainService.this){
            PermissionUtil permission = new PermissionUtil(getBaseContext());

            if(!permission.isAllPermissionsGranded(false)){
                Utils.logIt(ConstantsRes.APP_NAME, TAG + " getCurrentLocation Permission Denied");
                return null;
            }else {
                final Context ctx = getBaseContext();
                final ConstantsRes c = new ConstantsRes();
                try {
                    // Get GPS position
                    try {

                        boolean isGPSEnable, isNetworkEnable;
                        LocationManager locationManager             = null;
                        Location loc                                = null;
                        LocationManager locManager                  = null;
                        locManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
                        isGPSEnable = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                        isNetworkEnable = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                        if (!isGPSEnable && !isNetworkEnable) {
                            Utils.logIt(ConstantsRes.APP_NAME, TAG + " recordLocation Location No GPS & No Network");
                        } else {

                            if (isNetworkEnable) {
                                Utils.logIt(ConstantsRes.APP_NAME, TAG + " recordLocation Location isNetworkEnable");

                                loc = null;
                                if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return null;
                                }
                                locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, MainService.this);
                                if (locManager!=null){
                                    loc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                }

                            }

                            if (isGPSEnable) {
                                Utils.logIt(ConstantsRes.APP_NAME, TAG + " recordLocation Location isGPSEnable");

                                loc = null;
                                if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return null;
                                }
                                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, MainService.this);
                                if (locManager!=null){
                                    loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                }
                            }

                        }
                        if (Utils.isNull(changed)){
                            // Skip
                        }else {
                            loc = Utils.clone(changed);
                            changed = null;
                        }
                        if (loc == null){
                            // Not Location found
                            Utils.logIt(ConstantsRes.APP_NAME, TAG + " recordLocation Location Not Location found");
                            return null;
                        }else {
                            Utils.logIt(ConstantsRes.APP_NAME, TAG + " recordLocation Location found ");
                            return loc;
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }catch (Error er){
                        er.printStackTrace();
                    }
                    return null;
                }catch (Exception e){
                    e.printStackTrace();
                }catch (Error er){
                    er.printStackTrace();
                }
                return null;
            }

        }
    }

    private void get2LastLocations(final ResultCode code){
        try {
            synchronized (MainService.this){
                Utils.logIt(ConstantsRes.APP_NAME, TAG + " get4LastLocations motionLoopCount Start "+this.motionLoopCount);
                if (code == null){
                    return;
                }else {
                    if (code == ResultCode.DEFAULT){
                        return;
                    }else {
                        // Fix position not found When Person moving Quickly: Invalidate Position after 100 Count
                      int max = ConstantsRes.MAX_MOTION_LOOP;
                      if (code == ResultCode.PRECISED_POINT_NOT_FOUND){
                        // Wait to Get Point
                        max = ConstantsRes.MAX_LOOP_IS_SUPPORTED;
                      }
                        if (this.motionLoopCount > max){
                            if (locations == null){
                                locations = new CopyOnWriteArrayList<>();
                            }else {
                                resetArray();
                            }
                            this.resetMotionCount();

                            if (code == ResultCode.PRECISED_POINT_NOT_FOUND){
                                // Send Position Signal
                                ConstantsRes c = new ConstantsRes();
                                Intent intent = new Intent(ACTION_BROADCAST);
                                intent.putExtra(c.BUNDLE_LOCATION, "");
                                intent.putExtra(c.BUNDLE_ID, getIdFromCode(code));
                                intent.putExtra(c.BUNDLE_CODE, code.toInteger());


                                sendToModule(intent);
                            }else {
                                this.getNewPosition(code);
                            }
                            return;
                        }else {
                            Utils.logIt(ConstantsRes.APP_NAME, TAG + " get4LastLocations motionLoopCount "+this.motionLoopCount);
                            if (this.motionLoopCount < 0){
                                this.resetMotionCount();
                            }

                            this.motionLoopCount = this.motionLoopCount +1;
                            if (locations == null){
                                locations = new CopyOnWriteArrayList<>();
                            }else{

                                if (locations.size() < ConstantsRes.POINT_MIN_REQUIRE){
                                    Utils.logIt(ConstantsRes.APP_NAME, TAG + " get4LastLocations  locations.size() < 4 V1  motionLoopCount "+this.motionLoopCount+ " locations.size() "+locations.size());
                                    Location loc  = getCurrentLocation();
                                    if (loc == null){
                                        Utils.logIt(ConstantsRes.APP_NAME, TAG + " get4LastLocations  LOCATION IS NULL ");

                                        this.getNewPosition(code);
                                        return;
                                    }else {
                                        if (locations.size() == 0){
                                            // Case Empty Add First
                                            locations.add(loc);
                                            this.getNewPosition(code);
                                            return;
                                        }else {
                                            Utils.logIt(ConstantsRes.APP_NAME, TAG + " get4LastLocations New Location is OKAY ");
                                            // Delete loose Signal Point example getLastPoint when noGPS Found
                                            float distanceInMeter = loc.distanceTo(locations.get(locations.size() - 1));
                                            if (distanceInMeter > ConstantsRes.DISTANCE_7_METERS){
                                                // To Far so skip him
                                                locations.remove(locations.get(locations.size() - 1));
                                                this.getNewPosition(code);
                                                return;
                                            }else {
                                                if (loc.getAccuracy() > ConstantsRes.DISTANCE_3_METERS){
                                                    // Skip we want more accuracy
                                                    Utils.logIt(ConstantsRes.APP_NAME, TAG + " get4LastLocations Skip we want more accurate LoopCount "+this.motionLoopCount+ " locations.size() "+locations.size() + " current Accuracy "+loc.getAccuracy());

                                                    this.getNewPosition(code);
                                                    return;
                                                }else {
                                                    // Avoid multi thread acces conflit
                                                    Utils.logIt(ConstantsRes.APP_NAME, TAG + " get4LastLocations Avoid multi thread acces conflit locations.size() < 4 V2  motionLoopCount "+this.motionLoopCount+ " locations.size() "+locations.size());
                                                    synchronized (locations){
                                                        locations.add(loc);
                                                        if (locations.size() < ConstantsRes.POINT_MIN_REQUIRE){
                                                            // Get more point
                                                            this.getNewPosition(code);
                                                            return;
                                                        }else {
                                                            // Enough So Stop
                                                            this.notifyAccuratedPosition(code);
                                                            return;
                                                        }
                                                    }
                                                }
                                            }
                                        }


                                    }

                                }else {
                                    // Enough So Stop
                                    this.notifyAccuratedPosition(code);
                                    return;
                                }

                            }
                        }
                    }
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }catch (Error er){
            er.printStackTrace();
        }


    }

    private String getIdFromCode(ResultCode code)throws Exception,Error {
        switch (code){
            case GET_CURRENT_LOCATION:
                return  getIdCurrentPos();
            case SUSCRIBE_LOCATION_UPDATE:
                return getIdLocationUpdate();
            case DESUSCRIBE_LOCATION_UPDATE:
                return getIdDesuscribe();
            case PRECISED_POINT_NOT_FOUND:
                return getIdPointNotFound();
            default:
                return null;
        }
    }

    private void resetArray() {
        try {
            synchronized (MainService.this){
                if (locations == null){
                    locations = new CopyOnWriteArrayList<>();
                }else {
                    locations.clear();
                    locations = null;
                    locations = new CopyOnWriteArrayList<>();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }catch (Error er){
            er.printStackTrace();
        }



    }

    private void getNewPosition(final ResultCode code) {
        if (code == null){
            return;
        }else {
            if (code == ResultCode.DEFAULT){
                return;
            }else {
              if (this.isStopWatching()){
                  try {
                      Utils.logIt(ConstantsRes.APP_NAME, TAG + " getNewPosition Stop CAUSE DESUSCRIBE FIRED");
                      // Send Position Signal
                      ConstantsRes c = new ConstantsRes();
                      Intent intent = new Intent(ACTION_BROADCAST);
                      intent.putExtra(c.BUNDLE_LOCATION, "");
                      intent.putExtra(c.BUNDLE_ID, getIdFromCode(code));
                      //Stop CAUSE DESUSCRIBE And Disable for next time
                      setStopWatching(false);
                      intent.putExtra(c.BUNDLE_CODE, ResultCode.DESUSCRIBE_LOCATION_UPDATE.toInteger());

                      sendToModule(intent);
                      return;
                  }catch (Exception e){
                      e.printStackTrace();
                  }catch (Error er){
                      er.printStackTrace();
                  }
              }else {
                new Handler().postDelayed(() -> get2LastLocations(code),ConstantsRes.DELAY_SHORT_150);
              }
            }
        }
    }

    private void notifyAccuratedPosition (final ResultCode code){
        try {
            synchronized (MainService.this){
                Utils.logIt(ConstantsRes.APP_NAME, TAG + " notifyAccuratedPosition locations.size() "+locations.size());
                if (code == null){
                    return;
                }else {
                    if (code == ResultCode.DEFAULT){
                        return;
                    }else {
                        if (this.locations == null){
                            // Bad So Retry
                            get2LastLocations(code);
                            return;
                        }else {
                            if (this.locations.size() < ConstantsRes.POINT_MIN_REQUIRE){
                                // We need 4
                                get2LastLocations(code);
                                return;
                            }else {
                                Utils.logIt(ConstantsRes.APP_NAME, TAG + " notifyAccuratedPosition  More than 4 point locations.size() "+locations.size());

                                Location lastPos = locations.get(0);
                                if (lastPos == null){
                                    // We need 4
                                    get2LastLocations(code);
                                    return;
                                }else {
                                    Location found = lastPos;

                                    for (int i = 0; i < locations.size() ; i++){
                                        Location current = locations.get(i);
                                        if (current == null){
                                            // Skip
                                        }else {
                                            Utils.logIt(ConstantsRes.APP_NAME, TAG + " notifyAccuratedPosition  LOOP ACCURACY N° "+i+" currentAccuracy "+current.getAccuracy() + " Found Accuracy "+found.getAccuracy());
                                            // select Small
                                            if (current.getAccuracy() < found.getAccuracy()){
                                                found = current;
                                            }
                                        }
                                    }
                                    this.resetMotionCount();

                                    JSONObject position = locationToJSON(found);
                                    if (position == null){
                                        get2LastLocations(code);
                                        return;
                                    }else{
                                        Utils.logIt(ConstantsRes.APP_NAME, TAG + " notifyAccuratedPosition Found" + " location accuracy "+found.getAccuracy()
                                                + " latitude "+found.getLatitude() + " longitude "+found.getLongitude());
                                        Utils.logIt(ConstantsRes.APP_NAME, TAG + " notifyAccuratedPosition Found" + " code ordinal "+code.toInteger());

                                        // Send Position Signal
                                        ConstantsRes c = new ConstantsRes();
                                        Intent intent = new Intent(ACTION_BROADCAST);
                                        intent.putExtra(c.BUNDLE_LOCATION, position.toString());
                                        intent.putExtra(c.BUNDLE_ID, getIdFromCode(code));


                                        if (this.isStopWatching()){
                                            Utils.logIt(ConstantsRes.APP_NAME, TAG + " notifyAccuratedPosition Stop CAUSE DESUSCRIBE FIRED");
                                            //Stop CAUSE DESUSCRIBE And Disable for next time
                                            setStopWatching(false);
                                            intent.putExtra(c.BUNDLE_CODE, ResultCode.DESUSCRIBE_LOCATION_UPDATE.toInteger());

                                            sendToModule(intent);
                                            return;
                                        }else {
                                            intent.putExtra(c.BUNDLE_CODE, code.toInteger());

                                            resetArray();
                                            switch (code){
                                                case GET_CURRENT_LOCATION:
                                                    // Continu notify position and Reset for Next Time
                                                    resetMotionCount();
                                                    sendToModule(intent);
                                                    return;
                                                case SUSCRIBE_LOCATION_UPDATE:
                                                    // Continu notify position and Reset for Next Time
                                                    resetMotionCount();
                                                    sendToModule(intent);
                                                    /// notifyEvery1Min();
                                                    return;
                                                case DESUSCRIBE_LOCATION_UPDATE:
                                                    // Dont Send and Stop
                                                    return;
                                                case PRECISED_POINT_NOT_FOUND:
                                                    // Case Empty Location Mean device not precised
                                                    sendToModule(intent);
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
        }catch (Exception e){
            e.printStackTrace();
        }catch (Error er){
            er.printStackTrace();
        }


    }

    private void notifyEvery1Min(final int timeSeconds, boolean immediaty) {
        try {
            removeWatcher();
            schWatcher =
                    Executors.newSingleThreadScheduledExecutor();
            long waitDelay = 0;
            if (!immediaty){
                // Wait 1 Min Before Take Position
                waitDelay = timeSeconds;
            }
            if (needStopLooping()){
                Utils.logIt(ConstantsRes.APP_NAME, TAG + " notifyEvery1Min STOPPED cause needStopLooping: ");
                return;
            }else {
                schWatcher.scheduleAtFixedRate(() ->{
                    synchronized (MainService.this){
                        removeWatcher();
                        fetchPositions(timeSeconds);

                    }
                },waitDelay, timeSeconds, TimeUnit.SECONDS);
            }
        }catch (Exception e){
            e.printStackTrace();
        }catch (Error er){
            er.printStackTrace();
        }

    }

    private void fetchPositions(final int timeSeconds) {
        try {
            synchronized (MainService.this){
                Runnable actionCollectAgain = () ->{
                    new Handler(Looper.getMainLooper()).postDelayed(()->{
                        fetchPositions(timeSeconds);
                    },1000);
                };
                new Handler(Looper.getMainLooper()).post(() ->{
                    synchronized (MainService.this){
                        try{
                            if (locations == null){
                                locations = new CopyOnWriteArrayList<>();
                            }else{
                                if (locations.size() < ConstantsRes.POINT_MIN_REQUIRE){
                                    CopyOnWriteArrayList<Location> collectors = new CopyOnWriteArrayList<>();
                                    for(int i = 0; i< 7; i++){
                                        Location current = getCurrentLocation();
                                        if (Utils.isNull(current)){
                                            // Skip
                                        }else {
                                            Location updated = Utils.clone(current);
                                            if (Utils.isNull(updated)){
                                                // Skip
                                            }else {
                                                collectors.add(current);
                                            }
                                        }

                                    }
                                    Utils.logIt(ConstantsRes.APP_NAME, TAG + " fetchPositions collectors: "+collectors.size());
                                    Utils.AsyncExecute(() ->{
                                        try{
                                            if (collectors.size() == 0){
                                                actionCollectAgain.run();
                                                return;
                                            }
                                            for (Location currentLoc:collectors) {
                                                if (currentLoc == null){
                                                    Utils.logIt(ConstantsRes.APP_NAME, TAG + " fetchPositions  currentLoc IS NULL ");
                                                    return;
                                                }
                                            }
                                            locations.addAll(collectors);
                                            if (locations.size() == 0){
                                                // Case Empty Add First
                                                actionCollectAgain.run();
                                                return;
                                            }else {
                                                Utils.logIt(ConstantsRes.APP_NAME, TAG + " fetchPositions New Location is OKAY ");
                                                // Delete loose Signal Point example getLastPoint when noGPS Found
                                                CopyOnWriteArrayList<Location> arrayCopy = Utils.clone(locations);
                                                for (Location loc: locations) {
                                                    for (Location next:arrayCopy){
                                                        float distanceInMeter = loc.distanceTo(next);
                                                        if (Float.isNaN(distanceInMeter)){
                                                            // Skip Mean 0
                                                        }else {
                                                            // Use 7 instead Of 5 because his walking Fast
                                                            if (distanceInMeter > ConstantsRes.DISTANCE_7_METERS){
                                                                arrayCopy.remove(next);
                                                                break;
                                                            }else {
                                                                if (next.getAccuracy() > ConstantsRes.DISTANCE_3_METERS){
                                                                    arrayCopy.remove(next);
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                locations.clear();
                                                locations.addAll(arrayCopy);
                                                if (locations.size() < ConstantsRes.POINT_MIN_REQUIRE){
                                                    // Get more point
                                                    actionCollectAgain.run();
                                                    return;
                                                }else {
                                                    // Enough So Stop
                                                    this.notifyWatcher(timeSeconds,locations.get(0));
                                                    return;
                                                }
                                            }
                                        }catch (Exception e){
                                            toLog(" EXCEPTION HAPPEN 3 fetchPositions");
                                            e.printStackTrace();
                                        }catch (Error er){
                                            toLog(" ERROR HAPPEN 3 fetchPositions");
                                            er.printStackTrace();
                                        }
                                    });

                                }else {
                                    // Enough So Stop
                                    this.notifyWatcher(timeSeconds,locations.get(0));
                                    return;
                                }

                            }
                        }catch (Exception e){
                            toLog(" EXCEPTION HAPPEN 2 fetchPositions");
                            e.printStackTrace();
                        }catch (Error er){
                            toLog(" ERROR HAPPEN 2 fetchPositions");
                            er.printStackTrace();
                        }
                    }

                });

            }
        }catch (Exception e){
            toLog(" EXCEPTION HAPPEN fetchPositions");
            e.printStackTrace();
        }catch (Error er){
            toLog(" ERROR HAPPEN fetchPositions");
            er.printStackTrace();
        }
    }

    private void toLog(String msg){
        if (Utils.isNull(msg)){
            return;
        }else {
            Utils.logIt(ConstantsRes.APP_NAME, TAG +" "+msg);
        }

    }

    private void notifyWatcher(final int timeSeconds,@NonNull Location found) {
        try {
            if (Utils.isNull(found)){
                // We need 4
                toLog("notifyWatcher found is Null");
                return;
            }else {
                this.resetMotionCount();

                JSONObject position = locationToJSON(found);
                if (Utils.isNull(position)){
                    toLog("notifyWatcher position is Null");
                    return;
                }else{
                    Utils.logIt(ConstantsRes.APP_NAME, TAG + " notifyAccuratedPosition Found" + " location accuracy "+found.getAccuracy()
                            + " latitude "+found.getLatitude() + " longitude "+found.getLongitude());
                    Utils.logIt(ConstantsRes.APP_NAME, TAG + " notifyAccuratedPosition Found");

                    // Send Position Signal
                    ResultCode code = ResultCode.SUSCRIBE_LOCATION_UPDATE;
                    ConstantsRes c = new ConstantsRes();
                    Intent intent = new Intent(ACTION_BROADCAST);
                    intent.putExtra(c.BUNDLE_LOCATION, position.toString());
                    intent.putExtra(c.BUNDLE_ID, getIdFromCode(code));

//                    if (this.isStopWatching()){
//                        Utils.logIt(ConstantsRes.APP_NAME, TAG + " notifyAccuratedPosition Stop CAUSE DESUSCRIBE FIRED");
//                        //Stop CAUSE DESUSCRIBE And Disable for next time
//                        setStopWatching(false);
//                        intent.putExtra(c.BUNDLE_CODE, code.toInteger());
//
//                        sendToModule(intent);
//                        return;
//                    }else {
//                        intent.putExtra(c.BUNDLE_CODE, code.toInteger());
//                        resetArray();
//                        // Continu notify position and Reset for Next Time
//                        resetMotionCount();
//                        sendToModule(intent);
//                        notifyEvery1Min(false);
//                    }
                    intent.putExtra(c.BUNDLE_CODE, code.toInteger());
                    resetArray();
                    // Continu notify position and Reset for Next Time
                    resetMotionCount();
                    sendToModule(intent);
                    notifyEvery1Min(timeSeconds,false);
                }
            }
        }catch (Exception e){
            toLog(" EXCEPTION HAPPEN notifyWatcher");
            e.printStackTrace();
        }catch (Error er){
            toLog(" ERROR HAPPEN notifyWatcher");
            er.printStackTrace();
        }
    }

    private JSONObject locationToJSON(Location found) throws NullPointerException {
        JSONObject currentPos = null;
        try {
            currentPos = new JSONObject();

            ConstantsRes c = new ConstantsRes();
            currentPos.put(c.JSON_KEY_LATITUDE,             found.getLatitude());
            currentPos.put(c.JSON_KEY_LONGITUDE,            found.getLongitude());
            currentPos.put(c.JSON_KEY_DATE,                 getTodayISO());
            currentPos.put(c.JSON_KEY_ACCURACY,             found.getAccuracy());
            currentPos.put(c.JSON_KEY_ALTITUDE,             found.getAltitude());
        }catch (Exception | Error e){
            e.printStackTrace();
        }
        return currentPos;

    }

    // Receives messages from the service.
    private class ReceiverService extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Utils.logIt(ConstantsRes.APP_NAME, TAG + "ReceiverService onReceive Start ");

                if (context == null || intent == null){
                    return;
                }else {
                    ConstantsRes c = new ConstantsRes();
                    String id = intent.getStringExtra(c.BUNDLE_ID);
                    int code = intent.getIntExtra(c.BUNDLE_CODE,0);
                    int time = intent.getIntExtra(c.BUNDLE_TIME, 0);
                    ResultCode mCode = ResultCode.find(code);
                    if (mCode == null){
                        mCode = ResultCode.DEFAULT;
                    }
                    if (id == null){
                        id = "";
                    }
                    Utils.logIt(ConstantsRes.APP_NAME, TAG + "ReceiverService onReceive id "+id + " code "+code);

                    if (id.trim().contentEquals("")){
                        Utils.logIt(ConstantsRes.APP_NAME, TAG + "ReceiverService onReceive id "+id + "BAD PLUGIN ID");
                        return;
                    }else {
                        switch (mCode){
                            case GET_CURRENT_LOCATION:
                                setIdCurrentPos(null);
                                setIdCurrentPos(id);
                                getCurrentPosition();
                                return;
                            case SUSCRIBE_LOCATION_UPDATE:
                                setStopLooping(false);
                                if (time < c.TIME_MIN ){
                                    Utils.logIt(ConstantsRes.APP_NAME, TAG + "ReceiverService SUSCRIBE_LOCATION_UPDATE Stop cause Bad Time in Sec"+time +"Must be > MIN("+c.TIME_MIN+")");
                                    return;
                                }else {
                                    if (time > c.TIME_MAX){
                                        Utils.logIt(ConstantsRes.APP_NAME, TAG + "ReceiverService SUSCRIBE_LOCATION_UPDATE Stop cause Bad Time in Sec"+time +"Must be < MAX("+c.TIME_MAX+")");
                                        return;
                                    }else {
                                        setIdLocationUpdate(null);
                                        setIdLocationUpdate(id);
                                        notifyEvery1Min(time,true);
                                        return;
                                    }
                                }
                            case DESUSCRIBE_LOCATION_UPDATE:
                                setIdDesuscribe(null);
                                setIdDesuscribe(id);
                                removeWatcher();
                                setStopLooping(true);
                                return;
                            case PRECISED_POINT_NOT_FOUND:
                                setIdPointNotFound(null);
                                setIdPointNotFound(id);
                                checkIfHasPrecision();
                                return;

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

    private void checkIfHasPrecision() {
        Utils.logIt(ConstantsRes.APP_NAME, TAG + " checkIfHasPrecision Start ");
        get2LastLocations(ResultCode.PRECISED_POINT_NOT_FOUND);
    }

    private void suscribeToLocationUpdate() {
        Utils.logIt(ConstantsRes.APP_NAME, TAG + " suscribeToLocationUpdate Start ");
        get2LastLocations(ResultCode.SUSCRIBE_LOCATION_UPDATE);
    }

    private void getCurrentPosition(){
        Utils.logIt(ConstantsRes.APP_NAME, TAG + " getCurrentPosition Start ");
        get2LastLocations(ResultCode.GET_CURRENT_LOCATION);
    }


    private void removeTracking() {
        setNotifyStarted(false);
        if (schTracker != null){
            schTracker.shutdown();
            schTracker = null;
        }
    }
    private void removeWatcher() {
        try {
            if (schWatcher != null){
                schWatcher.shutdown();
                schWatcher = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }catch (Error er){
            er.printStackTrace();
        }
    }

    private void removeUploadTask() {
        if (schUpload != null){
            schUpload.shutdown();
            schUpload = null;
        }
    }

    public boolean isNotifyStarted() {
        return notifyStarted;
    }

    public void setNotifyStarted(boolean notifyStarted) {
        this.notifyStarted = notifyStarted;
    }

    public static synchronized boolean isRunning() {
        return isRunning;
    }


    public static void setRunning(boolean isRunning) {
        synchronized (MainService.class){
            MainService.isRunning = isRunning;
        }

    }

    private boolean isAppOnForeground(Context context) {
        /**
         We need to check if app is in foreground otherwise the app will crash.
         http://stackoverflow.com/questions/8489993/check-android-application-is-in-foreground-or-not
         **/
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses =
                activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance ==
                    ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }


    public static @NonNull String getTodayName() {
        String dFormat = "EEEE";
        long timeInMillis = System.currentTimeMillis();
        return DateFormat.format(dFormat, timeInMillis).toString();
    }


    public static String getTodayName2() {
        try{
            String dFormat = "yyyy-M-d";
            long timeInMillis = System.currentTimeMillis();
            String currentTime = DateFormat.format(dFormat, timeInMillis).toString();

            Date date = new SimpleDateFormat("yyyy-M-d").parse(currentTime);
            String dayOfWeek = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date);
            return dayOfWeek;
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public static String getTodayHourMin() {
        try{
            String dFormat = "hh:mm aa";
            long timeInMillis = System.currentTimeMillis();
            String hour = "";
            String currentTime = DateFormat.format(dFormat, timeInMillis).toString();


            SimpleDateFormat h_mm_a   = new SimpleDateFormat("h:mm a");
            SimpleDateFormat hh_mm_ss = new SimpleDateFormat("HH:mm");

            Date d1 = h_mm_a.parse(currentTime);

            hour = hh_mm_ss.format(d1);
            return hour.trim();
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public static void startTrackNewUser(String newUserStr, @NonNull final Context ctx){
        ConstantsRes c = new ConstantsRes();
        if (newUserStr == null || ctx ==null){
            return;
        }else {

            try {
                String prev = Save.defaultLoadString(c.PREF_PROFIL_PREVIOUS,ctx);
                if (prev == null){

                }else {
                    if (prev.trim().contentEquals(newUserStr.trim())){
                        // Case SAME CONFIG LIKE LAST // Don not clean old tracking
                        return;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }catch (Error er){
                er.printStackTrace();
            }

            Save.defaultSaveString(c.PREF_USER_PROFIL, newUserStr,ctx);
            // Member to compara after
            Save.defaultSaveString(c.PREF_PROFIL_PREVIOUS, newUserStr,ctx);
            Save.defaultSaveString(c.PREF_TRACKING_OBJ_LIST, c.PREF_EMPTY_LIST,ctx);
        }
    }

    public static void stopTrackUser(@NonNull final Context ctx){
        ConstantsRes c = new ConstantsRes();
        if (ctx ==null){
            return;
        }else {
            // Stop Current Loop
            Save.defaultSaveString(c.PREF_USER_PROFIL, "",ctx);
            Save.defaultSaveString(c.PREF_TRACKING_OBJ_LIST, c.PREF_EMPTY_LIST,ctx);
            // continu Loop
        }
    }

    public static @NonNull String getTodayISO() {
        try{
            String dFormat = "yyyy-MM-dd hh:mm:ss aa";
            long timeInMillis = System.currentTimeMillis();
            String hour = "";
            String currentTime = DateFormat.format(dFormat, timeInMillis).toString();


            SimpleDateFormat h_mm_a   = new SimpleDateFormat("yyyy-MM-dd h:mm:ss a");
            SimpleDateFormat hh_mm_ss = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

            Date d1 = h_mm_a.parse(currentTime);
            hour = hh_mm_ss.format(d1);
            return hour.trim();
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public static void waitOneSecond() throws InterruptedException{
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        toLog("onLocationChanged fired");
        this.changed = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    public boolean isSending() {
        return sending;
    }

    public void setSending(boolean sending) {
        this.sending = sending;
    }

    public boolean isStopWatching() {
        return stopWatching;
    }

    public void setStopWatching(boolean stopWatching) {
        this.stopWatching = stopWatching;
    }
}
