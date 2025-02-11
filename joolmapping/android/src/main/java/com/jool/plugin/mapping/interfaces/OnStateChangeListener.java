package com.jool.plugin.mapping.interfaces;

import androidx.annotation.NonNull;

public interface OnStateChangeListener {
    void onChange(int uplinkSignalQuality, short satelliteCount,int chargeRemainingInPercent, int chargeRemainingInMAh ,
             float temperature,
             float altitudeInMeters,
             float speedInMeterPerSec,
             float heading ,
             int numberOfDischarges,
             int lifetimeRemainingInPercent,
             double longitude,
             double latitude,
             float altitude,
             int droneHeadingInDegrees,
             @NonNull String droneModelName,
             float distanceInMeter);
    void onLog(String msg);
    void onConnectFailed();
    void onConnectSucces();
}
