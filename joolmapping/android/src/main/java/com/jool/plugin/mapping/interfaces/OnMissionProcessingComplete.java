package com.jool.plugin.mapping.interfaces;

import androidx.annotation.NonNull;

import com.jool.plugin.mapping.mission.MissionStep;


public interface OnMissionProcessingComplete {
    void onError();
    void onSucces(@NonNull final MissionStep steps);
}
