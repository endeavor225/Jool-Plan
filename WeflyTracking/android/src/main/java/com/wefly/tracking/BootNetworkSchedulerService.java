package com.wefly.tracking;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.annotation.RequiresApi;


/**
 * Created by admin on 07/05/2018.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BootNetworkSchedulerService extends JobService {
    private static volatile boolean reScheduleDone = false;

    private static final String TAG = BootNetworkSchedulerService.class.getSimpleName();

    public static boolean isReScheduleDone() {
        return reScheduleDone;
    }

    public static void setReScheduleDone(boolean reScheduleDone) {
        BootNetworkSchedulerService.reScheduleDone = reScheduleDone;
    }
    // ==========> Add Me 9

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            try {
                BootConnectivityReceiver bcr = new BootConnectivityReceiver();
                bcr.onNetworkDetected(context);
            }catch (Exception e){
                e.printStackTrace();
            }catch (Error er){
                er.printStackTrace();
            }


        }
    };

    private BroadcastReceiver screenOnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            try {
                if (!MainService.isRunning()){
                    BootConnectivityReceiver br = new BootConnectivityReceiver();
                    br.startMainServ(context);
                }
            }catch (Exception e){
                e.printStackTrace();
            }catch (Error er){
                er.printStackTrace();
            }


        }
    };


    private BroadcastReceiver userPresentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            try {
                if (!MainService.isRunning()){
                    BootConnectivityReceiver br = new BootConnectivityReceiver();
                    br.startMainServ(context);
                }
            }catch (Exception e){
                e.printStackTrace();
            }catch (Error er){
                er.printStackTrace();
            }



        }
    };


    @Override
    public void onCreate() {
        super.onCreate();

        // Service just started
    }

    /**
     * When the app's NetworkConnectionActivity is created, it starts this service. This is so that the
     * activity and this service can communicate back and forth. See "setUiCallback()"
     */
    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }


    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean onStartJob(JobParameters params) {
        try {
            // Reister for next Reboot or DbUnitwork detect
            registerReceiver(networkReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
            // Detected Screen On
            registerReceiver(screenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
            // Detected Screen On
            registerReceiver(userPresentReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));

            // Check Runninig service Fix getRunningServices DEPRECATED on Oreo
            // ALWAYS RUNNNNNNN

            // Start Service For Tracking
            if (!MainService.isRunning()){
                BootConnectivityReceiver br = new BootConnectivityReceiver();
                br.startMainServ(this);
            }

            // Start on Reboot
            if (!isReScheduleDone()){
                registerNextStartJob();

                setReScheduleDone(true);
            }
        }catch (Exception e){
            e.printStackTrace();
        }catch (Error er){
            er.printStackTrace();
        }


        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void registerNextStartJob() {
        try {
            JobInfo myJob = new JobInfo.Builder(0, new ComponentName(this, BootNetworkSchedulerService.class))
                    .setRequiresCharging(false)
                    .setMinimumLatency(1000)
                    .setOverrideDeadline(3000)
                    .setPersisted(true)
                    .build();

            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(myJob);
        }catch (Exception e){
            e.printStackTrace();
        }catch (Error er){
            er.printStackTrace();
        }

    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean onStopJob(JobParameters params) {
        try {
            unregisterReceiver(networkReceiver);
            unregisterReceiver(screenOnReceiver);
            unregisterReceiver(userPresentReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }catch (Error er){
            er.printStackTrace();
        }

        return true;
    }


}