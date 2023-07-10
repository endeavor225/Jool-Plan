package com.jool.plugin.mapping.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface OnCheckListChangeListener {
  void onError(@Nullable String errorDetail);
  void onPhoneBatterieStep(boolean isOK);
  void onAccesStep(boolean isOK);
  void onDroneStep(boolean isOK);
  void onControleStep(boolean isOK);
  void onCameraStep(boolean isOK);
  void onCameraCalibratedStep(boolean isOK);
  void onSDCardStep(boolean isOK);
  void onFlyPlanStep(boolean isOK);
  void onBatterieStep(boolean isOK);
  void onGPSStep(boolean isOK);
  void onDiagnosticStep(@NonNull String msg);
  void onSucces();
}
