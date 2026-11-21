package com.deviceprofile.spoof;
import android.app.Application;
import io.github.libxposed.service.XposedService;
import io.github.libxposed.service.XposedServiceHelper;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Bridge extends Application implements XposedServiceHelper.OnServiceListener {
  volatile XposedService service;
  final CopyOnWriteArrayList<Runnable> listeners=new CopyOnWriteArrayList<>();
  @Override public void onCreate(){super.onCreate();XposedServiceHelper.registerListener(this);}
  @Override public void onServiceBind(XposedService next){service=next;notifyListeners();}
  @Override public void onServiceDied(XposedService old){if(service==old){service=null;notifyListeners();}}
  private void notifyListeners(){for(Runnable listener:listeners)listener.run();}
}
