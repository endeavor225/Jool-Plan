package com.jool.plugin.mapping.interfaces;

import androidx.annotation.Nullable;

public interface OnAsyncGPSOperationComplete {
    void onError(@Nullable String errorDetail);
    void onLocationChanged(@Nullable Double uLatitude, @Nullable Double uLongitude);
}
