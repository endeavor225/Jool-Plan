package com.wefly.tracking;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;


/**
 * Created by admin on 23/03/2018.
 */

public class BootConnectivityReceiver extends BroadcastReceiver {
    private final String TAG = getClass().getSimpleName();
    boolean isServiceRunning;

    // ==========> Add Me 8


    @Override
    public void onReceive(Context context, Intent intent) {

        try {

            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){

                if (intent != null && intent.getAction() != null && intent.getAction().trim().contentEquals(android.net.ConnectivityManager.CONNECTIVITY_ACTION)){
                    onNetworkDetected(context);
                    return;
                }
                try {
                    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                        if ( MainService.class.getName().equals(service.service.getClassName())) {
                            isServiceRunning = true;
                        }
                    }

                    if (!isServiceRunning){
                        Intent inte = new Intent(context, MainService.class);
                        inte.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startService(inte);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }else {
                sendEvent(context);
            }

        }catch (Exception e){
            e.printStackTrace();
        }catch (Error er){
            er.printStackTrace();
        }


    }

    public void sendEvent(@NonNull final Context context) {
        Intent newIntent = new Intent(context, MainService.class);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Start MainService : Will get User position or Send All trackand Parcelle
        try {
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
                context.startForegroundService(newIntent);
            }
            else {
                context.startService(newIntent);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }catch (Error er){
            er.printStackTrace();
        }

    }

    public void startMainServ(@NonNull final Context ctx){
        BootConnectivityReceiver b = new BootConnectivityReceiver();
        b.sendEvent(ctx);
    }

    public void  onNetworkDetected(@NonNull final Context context){
        //
        if (canFire(context)){
            if (!MainService.isRunning()){
                // Start Main Service
                startMainServ(context);
            }
        }else {
            // Allow next
            // In MainActivity
        }
    }

    public static boolean canFire(@NonNull final  Context ctx){
        try {
            return Save.defaultLoadBoolean(ConstantsRes.PREF_FIRED_ON_NETWORK, ctx);
        }catch (Exception e){
            e.printStackTrace();
        }catch (Error er){
            er.printStackTrace();
        }
        return false;
    }

    public static boolean grantFire(@NonNull final Context ctx){

        try {
            return Save.defaultSaveBoolean(ConstantsRes.PREF_FIRED_ON_NETWORK, true, ctx);
        }catch (Exception e){
            e.printStackTrace();
        }catch (Error er){
            er.printStackTrace();
        }
        return false;
    }


    public static boolean stopFired(@NonNull final Context ctx){

        try {
            return Save.defaultSaveBoolean(ConstantsRes.PREF_FIRED_ON_NETWORK, false, ctx);
        }catch (Exception e){
            e.printStackTrace();
        }catch (Error er){
            er.printStackTrace();
        }
        return false;
    }

}
