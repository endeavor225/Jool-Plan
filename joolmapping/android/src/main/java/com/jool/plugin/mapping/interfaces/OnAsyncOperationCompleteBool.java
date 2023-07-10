package com.jool.plugin.mapping.interfaces;

import androidx.annotation.Nullable;

public interface OnAsyncOperationCompleteBool {
    void onResultNo(@Nullable String errorDetail);
    void onResultYes();
}
