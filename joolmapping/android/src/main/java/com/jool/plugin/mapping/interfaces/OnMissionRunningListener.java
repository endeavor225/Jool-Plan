package com.jool.plugin.mapping.interfaces;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.concurrent.CopyOnWriteArrayList;

public interface OnMissionRunningListener {
    void onError(@Nullable String errorDetail);
    void onCalibrationRequire(@Nullable String detail);
    void onStartSucces();
    void onNotifyStepRemain(int stepLeft);
    void onSucces();
    void onGridProgressing(int progress);
    void onNotifyState(int battery,int imageCount,float speed,CopyOnWriteArrayList<LatLng> flyPoints);
    void mustGoHomeLowBattery();
    void mustGoHomeError(); // Case Mission Finish Or Low Battery
    void onEnableLoadingForNextMission();
    void onDisableLoadingForNextMission();
}
