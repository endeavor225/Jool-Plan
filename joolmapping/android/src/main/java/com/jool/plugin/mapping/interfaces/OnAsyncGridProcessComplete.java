package com.jool.plugin.mapping.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.concurrent.CopyOnWriteArrayList;

public interface OnAsyncGridProcessComplete {
    void onError(@Nullable String errorDetail);
    void onSucces(@NonNull final CopyOnWriteArrayList<LatLng> flyPointsAfterRebuild);
}
