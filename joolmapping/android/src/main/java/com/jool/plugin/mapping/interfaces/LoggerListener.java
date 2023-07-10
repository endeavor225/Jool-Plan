package com.jool.plugin.mapping.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

public interface LoggerListener {
  void notifyUI(@NonNull String msg);
}
