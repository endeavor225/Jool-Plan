package com.jool.plugin.mapping.presenters;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.getcapacitor.JSObject;
import com.jool.plugin.mapping.JoolMappingPlugin;
import com.jool.plugin.mapping.utils.Utils;

import java.util.concurrent.CopyOnWriteArrayList;

public class LandedDetectorModule {
  private final String TAG = this.getClass().getSimpleName();
  protected @Nullable boolean locked = false;
  protected @Nullable CopyOnWriteArrayList<? extends Object> bundle = null;

  @Nullable
  protected boolean isLocked() {
    return locked;
  }

  protected void setLocked(@Nullable boolean locked) {
    this.locked = locked;
  }

  @Nullable
  protected CopyOnWriteArrayList<? extends Object> getBundle() {
    return bundle;
  }

  protected void setBundle(@Nullable CopyOnWriteArrayList<? extends Object> params) {
    this.bundle = params;
  }

  public LandedDetectorModule(){
    this.ini();
  }

  public void ini(){
    setLocked(false);
    setBundle(null);
  }


  public void register(final JoolMappingPlugin plugin, @NonNull final Activity activity) {
    if (Utils.isNull(plugin)){
      return;
    }else {
      if (Utils.isNull(activity)){
        return;
      }else {
        CopyOnWriteArrayList<JoolMappingPlugin> list = new CopyOnWriteArrayList<>();
        list.add(plugin);
        setBundle(list);
      }

    }
  }

  public void unRegister() {
    this.ini();
  }

  public boolean isValid() {
    if (isLocked()){
      return false;
    }else {
      if (Utils.isNull(getBundle())){
        return false;
      }else {
        if (getBundle().size() == 0){
          return false;
        }else {
          if (!(getBundle().get(0) instanceof JoolMappingPlugin)){
            return false;
          }else {
            final JoolMappingPlugin plugin = (JoolMappingPlugin) getBundle().get(0);
            if (Utils.isNull(plugin)){
              return false;
            }else {
              if (plugin.getActivity().isFinishing()){
                return false;
              }else {
                return true;
              }
            }

          }
        }
      }
    }

  }

  public int getSize() {
    return Utils.isNull(getBundle())?  0: getBundle().size();
  }

  public void hasEvent(boolean canTakeOff) {
    CopyOnWriteArrayList<Boolean> list = new CopyOnWriteArrayList<>();
    list.add(canTakeOff);
    hasEvent(list);
  }

  public void hasEvent(@Nullable @org.jetbrains.annotations.Nullable CopyOnWriteArrayList<?> params) {
    if (!isValid()){
      return;
    }else {
      onEvent(params);
    }
  }

  public void onEvent(@Nullable @org.jetbrains.annotations.Nullable CopyOnWriteArrayList<?> params) {
    if (!isValid()){
      return;
    }else {
      if (Utils.isNull(params)){
        return;
      }else {
        if (params.size() != 1){
          return;
        }else {
          setLocked(true);
          try {
            boolean value = (boolean) params.get(0);
            notifyEvent(value);
          }catch (Exception  e){
            com.jool.plugin.mapping.mapping.Utils.logContent(getClass().getSimpleName(),"onEvent",e,null);
          }catch (Error er){
            com.jool.plugin.mapping.mapping.Utils.logContent(getClass().getSimpleName(),"onEvent",null,er);
          }
          finally {
            setLocked(false);
          }
        }
      }
    }
  }

  protected void notifyEvent(boolean isReady) {
    // Already Valid and Locked
    if (Utils.isNull(getBundle())){
      Utils.toLog(TAG, "getBundle is null");
      return;
    }else {
      JoolMappingPlugin plugin = (JoolMappingPlugin) getBundle().get(0);
      if (Utils.isNull(plugin)){
        Utils.toLog(TAG, "notifyEvent plugin is null");
        return;
      }else{
        // Conitune here
        JSObject ret = new JSObject();
        ret.put("isDroneReady", isReady);
        plugin.notifyListeners("onReadyToTakeOff", ret);
      }
    }

  }
}
