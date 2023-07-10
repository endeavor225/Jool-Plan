package com.jool.plugin.mapping.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.concurrent.CopyOnWriteArrayList;

public interface OnAsyncOperationCompleteBoolArray {
    void onResultNo(@Nullable String errorDetail);
    void onResultYes(@NonNull CopyOnWriteArrayList<CopyOnWriteArrayList<LatLng>> list);
}
