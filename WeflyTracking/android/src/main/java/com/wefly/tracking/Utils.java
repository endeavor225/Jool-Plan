package com.wefly.tracking;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class Utils {
    private static String TAG = "Utils";
    private static Object object = new Object();


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

    public static void logIt(String mTag,String message){
        synchronized (object){
            if (false){
                return;
            }else {
                if (isNullOrEmpty(mTag)){
                    Log.v(TAG, "logIt mTag IS NULL OR EMPTY");
                    return;
                }else {
                    if (isNullOrEmpty(message)){
                        Log.v(mTag, "logIt message IS NULL OR EMPTY");
                        return;
                    }else{
                        AsyncExecute(() -> {
                            Log.v(mTag, message);
                        });
                    }
                }
            }
        }
    }


    public static void logCaseSomeThingWrong(String mTag,String message){
        if (false){
            return;
        }else {
            if (isNullOrEmpty(mTag)){
                Log.v(TAG, "LogCaseSomeThingWrong mTag IS NULL OR EMPTY");
                return;
            }else {
                if (isNullOrEmpty(message)){
                    Log.v(mTag, "LogCaseSomeThingWrong message IS NULL OR EMPTY");
                    return;
                }else{
                    Log.v(mTag, message);
                }
            }
        }
    }

    public static void logContent(String tag, String funcName, @Nullable Exception exception,  @Nullable Error error) {
        Log.v(tag, " EXCEPTION OR  HAPPEN  funcName "+funcName);
        // Log Error before Add To DB
        if (!Utils.isNull(exception)){
            exception.printStackTrace();
        }
        if (!Utils.isNull(error)){
            error.printStackTrace();
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
    public static @Nullable JSONArray clone (final JSONArray array)throws Exception,Error{
        if (isNull(array)){
            return null;
        }else{
            String data = array.toString();
            if (isNull(data)){
                return null;
            }else {
                JSONArray body = new JSONArray(data);
                return body;
            }
        }
    }
    public static @Nullable JSONObject clone (final JSONObject object)throws Exception,Error{
        if (isNull(object)){
            return null;
        }else{
            String data = object.toString();
            if (isNull(data)){
                return null;
            }else {
                JSONObject body = new JSONObject(data);
                return body;
            }
        }
    }

    public static @Nullable String clone (final String value)throws Exception,Error{
        if (isNull(value)){
            return null;
        }else{
            // get New Address
            String data = new String(value.getBytes());
            return data;
        }
    }

    public static @Nullable Integer clone (final Integer value)throws Exception,Error{
        if (isNull(value)){
            return null;
        }else{
            // get New Address
            Integer data = Integer.valueOf((value+""));
            return data;
        }
    }
    public static int clone (int value){
          // get New Address
        Integer data = Integer.valueOf((value+""));
        return data;
    }
    public static Location clone( final Location old){
        // get New Address
        if (Utils.isNull(old)){
            return null;
        }else {
            String value = new String(old.getProvider().getBytes());
            Location item = new Location(value);
            item.setLatitude(Utils.clone(old.getLatitude()));
            item.setLongitude(Utils.clone(old.getLongitude()));
            return item;
        }

    }

    public static CopyOnWriteArrayList<Location> clone ( final CopyOnWriteArrayList<Location> list){
        // get New Address
        CopyOnWriteArrayList<Location> cloneArray = new CopyOnWriteArrayList<>();
        for (Location loc:list){
            if (loc == null){
                // skip
            }else {
                String value = null;
                try {
                    value = Utils.clone(loc.getProvider());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (Utils.isNull(value)){
                    // skip
                }else {
                    Location item = new Location(value);
                    item.setLatitude(Utils.clone(loc.getLatitude()));
                    item.setLongitude(Utils.clone(loc.getLongitude()));

                    cloneArray.add(item);
                }

            }
        }
        return cloneArray;
    }



    public static @Nullable Double clone (final Double value)throws Exception,Error{
        if (isNull(value)){
            return null;
        }else{
            // get New Address
            Double data = Double.valueOf((value+""));
            return data;
        }
    }
    public static double clone (final double value){
        // get New Address
        return Double.parseDouble((value+""));
    }

    public static void AsyncExecute(@NonNull Runnable runnable){
        if (isNull(runnable)){
            return;
        }else{
            AsyncTask.execute(runnable);
        }
    }


}
