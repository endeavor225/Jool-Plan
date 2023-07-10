package com.wefly.tracking;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.util.concurrent.CopyOnWriteArrayList;

import static com.wefly.tracking.ConstantsRes.PACKAGE_RES_NAME;

/**
 * Created by Obrina.KIMI on 11/23/2017.
 */

public class PermissionUtil {
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    boolean hasRequest = false;

    private static boolean isPermissionNeed = false;


    public PermissionUtil(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PACKAGE_RES_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        isPermissionNeed = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }




    public boolean isAllPermissionsGranded(boolean skipStatic){
        try{
            if (skipStatic){
                isPermissionNeed = true;
            }
            if(isPermissionNeed){
                CopyOnWriteArrayList<String> permissionNeeded = new CopyOnWriteArrayList<>();
                CopyOnWriteArrayList<String> permissionAvailable = new CopyOnWriteArrayList<>();
                permissionAvailable.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                permissionAvailable.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                permissionAvailable.add(Manifest.permission.ACCESS_FINE_LOCATION);
                permissionAvailable.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    permissionAvailable.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                }
                permissionAvailable.add(Manifest.permission.READ_PHONE_STATE);
                for(String permission: permissionAvailable){
                    if(ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                        permissionNeeded.add(permission);
                }
                if(permissionNeeded.size() > 0){
                    return false;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }catch (Error er){
            er.printStackTrace();
        }
        return true;
    }



}
