package com.jool.plugin.mapping.interfaces;

import androidx.annotation.Nullable;

public interface OnAsyncOperationComplete {
    void onError(@Nullable String errorDetail);
    void onSucces(@Nullable String succesMsg);
}
