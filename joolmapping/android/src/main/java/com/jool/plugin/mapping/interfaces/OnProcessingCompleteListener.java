package com.jool.plugin.mapping.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.jool.plugin.mapping.mission.MissionSetting;
import com.jool.plugin.mapping.mission.MissionStep;

import java.util.concurrent.CopyOnWriteArrayList;

public interface OnProcessingCompleteListener {
    void onError(@Nullable String errorDetail);
    void onGridProgressing(int progress);
    void onSucces(double flyTime, int imageCount, int battery, float speed, CopyOnWriteArrayList<LatLng> flyPoints,
                  final @NonNull MissionSetting missionSetting, final @NonNull MissionStep steps);
}
