package com.jool.plugin.mapping.utils.underscore;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.List;
import java.util.function.Function;

@RequiresApi(api = Build.VERSION_CODES.N)
public interface Template<T> extends Function<T, String> {
    List<String> check(T arg);
}
