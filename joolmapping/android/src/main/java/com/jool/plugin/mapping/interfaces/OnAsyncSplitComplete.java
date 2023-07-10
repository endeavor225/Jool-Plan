package com.jool.plugin.mapping.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.concurrent.CopyOnWriteArrayList;

public interface OnAsyncSplitComplete {
    void onResultNo(@Nullable String errorDetail);
    void onResultYes(@NonNull CopyOnWriteArrayList<CopyOnWriteArrayList<LatLng>> newMain,CopyOnWriteArrayList<CopyOnWriteArrayList<LatLng>> bigArea);
}
