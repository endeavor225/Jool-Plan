package com.jool.plugin.mapping.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.jool.plugin.mapping.mission.MissionStep;

import java.util.concurrent.CopyOnWriteArrayList;

public interface OnMissionSplitComplete {
    void onError(@Nullable String errorDetail);
    void onSucces(final MissionStep himSelf, @NonNull final CopyOnWriteArrayList<LatLng> flyPointsAfterRebuild);
    void onPerformSaveAndContinu(final MissionStep himSelf, @NonNull final CopyOnWriteArrayList<LatLng> flyPointsAfterRebuild);
}
