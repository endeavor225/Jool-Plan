package com.jool.plugin.mapping.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;

import com.jool.plugin.mapping.DroneManager;
import com.jool.plugin.mapping.JoolMappingPlugin;
import com.jool.plugin.mapping.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.TimeUnit;

public class Utils {
  private static String TAG = "Utils";
  private static final Object object = new Object();
  private static Handler handler = null;
  private static String logData = "";

  private static Runnable writeDailyLog = () ->{
    try {
      // Get Directory
      synchronized (object){
        if (Utils.isNull(logData)){
          Log.v(TAG, " writeDailyLog logData is Null");
          return;
        }else {
          if (Utils.isNull(DroneManager.getInstance())){
            Log.v(TAG, " writeDailyLog DroneManager.getInstance is Null");
            return;
          }else {
            Context ctx = DroneManager.getInstance().getContext();
            if (Utils.isNull(ctx)){
              Log.v(TAG, " writeDailyLog ctx is Null");
              return;
            }else{
              File directory  = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS+"/"+ctx.getString(R.string.app_name)).getAbsoluteFile();
              if(!directory.exists() && !directory.mkdirs()){
                //Log.v(Constants.APP_NAME, TAG + "Error creating directory " + directory);
                return;
              }
              String backupName = "MappingLog "+Utils.getTodayNoHH();
              String FILE_EXTENSION_BKP = ".txt";
              String bkpPath = backupName + FILE_EXTENSION_BKP;
              File bkp = new File (directory, bkpPath);
              FileOutputStream fos = new FileOutputStream(bkp, true); // save
              fos.write(logData.getBytes());
              fos.close();
              logData = "";
            }
          }
        }
      }
    }catch (Exception e){
      e.printStackTrace();
    }catch (Error er){
      er.printStackTrace();
    }
  };


  public static boolean isNull(Object obj){
    return obj == null;
  }
  public static boolean isNullOrEmpty(String str){
    if (isNull(str)){
      return true;
    }else{
      return str.trim().contentEquals("");
    }
  }

  public static void toLog(String tag, String str){
    AsyncExecute(()->{
      synchronized (object){
        if (isNull(str)){
          return;
        }else{
          if (Utils.isNull(tag)){
            Log.v(TAG, " tag is Null");
            return;
          }else {
            DroneManager instance = DroneManager.getInstance();
            if (Utils.isNull(instance)){
              // Skip
            }else {
              if (Utils.isNull(instance.getLoggerListener())){
                // Skip
              }else {
                instance.getLoggerListener().notifyUI(tag+str);
              }
            }

            Log.v(tag, str);

            if (Utils.isNull(handler)){
              handler = new Handler(Looper.getMainLooper());
            }
            // Cancel other
            logData += (Utils.clone(str)+"\n");
            handler.removeCallbacks(writeDailyLog);
            if (logData.length() < 10000){
              // Wait
              handler.postDelayed(writeDailyLog,TimeUnit.SECONDS.toMillis(10));
            }else {
              // Save Immediatly to avoid OutOfMemory
              handler.post(writeDailyLog);
            }

          }
        }
      }
    });
  }

  public static int getBatteryPercent(@NonNull final Context context)throws Exception, Error{

    IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    Intent batteryStatus = context.registerReceiver(null, ifilter);
    // CheckBattery state
    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

    float batteryPct = level / (float)scale;
    int p = (int) (batteryPct * 100);
    return p;
  }

  public static void toLog(String tag,@NonNull String funcName, @Nullable Error er, @Nullable Exception e){
    AsyncExecute(()->{
      synchronized (object){
        Log.v(tag, " EXCEPTION OR  HAPPEN  funcName "+funcName);
        if (Utils.isNull(tag)){
          Log.d(TAG, " tag is Null");
          return;
        }else {
          String stacktrace = "";
          if (!Utils.isNull(er)){
            er.printStackTrace();

            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            er.printStackTrace(printWriter);
            stacktrace = result.toString();
            printWriter.close();

            toLog(tag,funcName+""+stacktrace);
          }
          if (!Utils.isNull(e)){
            e.printStackTrace();
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            stacktrace = result.toString();
            printWriter.close();

            toLog(tag,funcName+""+stacktrace);
          }

          DroneManager manager = DroneManager.getInstance();
          if (Utils.isNull(manager)){
            return;
          }else {
            manager.writeLogOnDeviceDocument(stacktrace);
          }
        }
      }
    });
  }

  public static void add(String tag, String funcName, @NonNull String msg) {
    Log.v(tag, " EXCEPTION OR  HAPPEN  funcName "+funcName);
    // Log Error before Add To DB
    if (isNull(msg)){
      Log.v(tag, " EXCEPTION OR  HAPPEN  msg IS NULL ");
    }else {
      Log.v(tag, " EXCEPTION OR  HAPPEN  msg : "+msg);
    }
  }

  public static void caughtExceptionOrError(){
    // Setup handler for uncaught exceptions.
    Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
    {
      @Override
      public void uncaughtException (Thread thread, Throwable throwable)
      {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        throwable.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        Log.d("stacktrace", stacktrace);
        add(TAG,"uncaughtException", stacktrace);
        System.exit(1);
      }
    });
  }

  public static void showToast(@NonNull Context ctx, String txt){
    if (Utils.isNull(ctx)){
      return;
    }else {
      if (Utils.isNullOrEmpty(txt)){
        return;
      }else {
        Toast.makeText(ctx, txt, Toast.LENGTH_LONG).show();
      }
    }
  }

  public static long getCurrentTime(){
    long timeInMillis       = System.currentTimeMillis();
    return timeInMillis;
  }

  public static void showToastValue(@NonNull Context ctx, @StringRes int valueString){
    if (Utils.isNull(ctx)){
      return;
    }else {
      Toast.makeText(ctx, valueString, Toast.LENGTH_LONG).show();
    }
  }

  public static boolean isNullOrEmpty(JSONObject obj){
    if (isNull(obj)){
      return true;
    }else{
      JSONObject emptyObj = new JSONObject();
      if (obj.toString().toLowerCase().trim().contentEquals(emptyObj.toString().toLowerCase().trim())){
        return true;
      }else{
        return false;
      }
    }
  }
  public static String toStringNonNull(String value) {
    if (isNullOrEmpty(value)){
      return "";
    }else {
      return value;
    }
  }

//    public void myGenericMethod(Runnable runnable){
//        common task1;
//        common task2;
//        //consider checking if runnable != null to avoid NPE
//        runnable.run();
//    }

  public static void AsyncExecute(@NonNull Runnable runnable){
    if (isNull(runnable)){
      return;
    }else{
      AsyncTask.execute(runnable);
    }
  }

  public static void launchCall(@NonNull String phoneNumber, @NonNull Context ctx) {
    try {
      if (!(phoneNumber.trim().contentEquals("")) || !(phoneNumber.length() < 8)){
        phoneNumber = "tel:" + phoneNumber;
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse(phoneNumber));
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
          // TODO: Consider calling
          //    ActivityCompat#requestPermissions
          // here to request the missing permissions, and then overriding
          //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
          //                                          int[] grantResults)
          // to handle the case where the user grants the permission. See the documentation
          // for ActivityCompat#requestPermissions for more details.
          return;
        }
        ctx.startActivity(callIntent);
      }

    }catch (Exception e){
      e.printStackTrace();
    }catch (Error er){
      er.printStackTrace();
    }
  }

  public static boolean isStorageMemoryEnough(){
    // Min 1 méga
    long oneMega = 1;

    try {
      StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
      long freeSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
      long megaAvailable = freeSpace / (1024 * 1024);

      if(megaAvailable > oneMega){
        return true;
      }else{
        return false;
      }
    } catch (Exception e) {e.printStackTrace(); }
    return false;
  }

  public static @Nullable JSONArray clone (final JSONArray array){
    if (isNull(array)){
      return null;
    }else{
      String data = array.toString();
      if (isNull(data)){
        return null;
      }else {
        try {
          JSONArray body = new JSONArray(data);
          return body;
        }catch (Exception e){
          e.printStackTrace();
        }catch (Error e){
          e.printStackTrace();
        }
        return null;

      }
    }
  }
  public static @Nullable JSONObject clone (final JSONObject object){
    if (isNull(object)){
      return null;
    }else{
      String data = object.toString();
      if (isNull(data)){
        return null;
      }else {
        try{
          JSONObject body = new JSONObject(data);
          return body;
        }catch (Exception e){
          e.printStackTrace();
        }catch (Error e){
          e.printStackTrace();
        }
        return null;
      }
    }
  }

  public static @Nullable String clone (final String value){
    if (isNull(value)){
      return null;
    }else{
      // Test
      // String a = "milou";
      // String b = Utils1.clone(a);
      // Log.v(TAG, " activityCreated a "+System.identityHashCode(a) + " b "+System.identityHashCode(b));
      // get New Address
      String data = new String(value.getBytes());
      return data;
    }
  }

  public static @Nullable Integer clone (final Integer value){
    if (isNull(value)){
      return null;
    }else{
      try{
        // get New Address
        Integer data = Integer.valueOf((value+""));
        return data;
      }catch (Exception e){
        e.printStackTrace();
      }catch (Error e){
        e.printStackTrace();
      }
      return null;
    }
  }
  public static int clone (int value){
    // get New Address
    Integer data = Integer.valueOf((value+""));
    return data;
  }

  public static @Nullable Double clone (final Double value){
    if (isNull(value)){
      return null;
    }else{
      try{
        // get New Address
        Double data = Double.valueOf((value+""));
        return data;
      }catch (Exception e){
        e.printStackTrace();
      }catch (Error e){
        e.printStackTrace();
      }
      return null;
    }
  }

  public static double clone (final double value){
    // get New Address
    Double data = Double.valueOf((value+""));
    return data;
  }
  public static float clone (final float value){
    // get New Address
    Float data = Float.valueOf((value+""));
    return data;
  }
  public static @Nullable Float clone (final Float value){
    if (isNull(value)){
      return null;
    }else{
      try{
        // get New Address
        Float data = Float.valueOf((value+""));
        return data;
      }catch (Exception e){
        e.printStackTrace();
      }catch (Error e){
        e.printStackTrace();
      }
      return null;
    }
  }

  public static @NonNull String getToday() {
    String dFormat = "yyyy MM dd hh mm ss";
    long timeInMillis = System.currentTimeMillis();
    return DateFormat.format(dFormat, timeInMillis).toString();
  }

  public static @NonNull String getTodayNoHH() {
    String dFormat = "yyyy MM dd";
    long timeInMillis = System.currentTimeMillis();
    return DateFormat.format(dFormat, timeInMillis).toString();
  }


  public static void runAfterWait(@NonNull final Activity activity,final Runnable runnable) {
    if (Utils.isNull(runnable)){
      toLog(TAG, "runAfterWait runnable IS NULL");
      return;
    }else {
      if (Utils.isNull(activity)){
        toLog(TAG, "activity runnable IS NULL");
        return;
      }else {
        new Handler(activity.getMainLooper()).postDelayed(runnable, TimeUnit.SECONDS.toMillis(Constants.DELAY));
      }
    }
  }

  public static void runOnUIWithDelay(final Runnable runnable, Activity activity) {
    if (Utils.isNull(activity)){
      toLog(TAG,"runWithDelay stop cause activity IS Null");
      return;
    }else {
      if (Utils.isNull(runnable)){
        toLog(TAG,"runWithDelay stop cause runnable IS Null");
        return;
      }else {
        new Handler(activity.getMainLooper()).postDelayed(runnable, TimeUnit.SECONDS.toMillis(Constants.DELAY));
      }
    }

  }
}
