package org.capacitor.jool.plan;
import com.getcapacitor.BridgeActivity;

import android.content.Intent;
import android.hardware.usb.UsbManager;
import androidx.annotation.NonNull;

import com.jool.plugin.mapping.DJISampleApplication;
import com.jool.plugin.mapping.DroneManager;
import com.jool.plugin.mapping.JoolMappingPlugin;
import com.jool.plugin.mapping.utils.Utils;

import dji.sdk.sdkmanager.DJISDKManager;

import android.os.Bundle;
import android.util.Log;

import com.getcapacitor.Plugin;
import com.wefly.tracking.MainServiceModulePlugin;
import java.util.ArrayList;
// import android.util.Log;
public class MainActivity extends BridgeActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    DJISampleApplication.getEventBus().register(this);
    DroneManager.createInstance(this);
    JoolMappingPlugin.setMainApp(this.getApplication());
    //Initializes the Bridge
    this.init(savedInstanceState, new ArrayList<Class<? extends Plugin>>() {{
      //Additional plugins you've installed go here
      //Ex: add(TotallyAwesomePlugin.class);
      add(MainServiceModulePlugin.class);
      add(JoolMappingPlugin.class);
    }});
  }


  @Override
  protected void onNewIntent(@NonNull Intent intent) {
    String action = intent.getAction();
    if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
      Intent attachedIntent = new Intent();
      attachedIntent.setAction(DJISDKManager.USB_ACCESSORY_ATTACHED);
      sendBroadcast(attachedIntent);
    }
    super.onNewIntent(intent);
  }

  @Override
  public void onDestroy() {
    DJISampleApplication.getEventBus().unregister(this);
    super.onDestroy();
    DroneManager manager = DroneManager.getInstance();
    if (Utils.isNull(manager)){
      Log.v(MainActivity.this.getClass().getSimpleName(), "ini stop cause manager is Null");
      return;
    }else {
      manager.onDestroy();
    }
  }
}

