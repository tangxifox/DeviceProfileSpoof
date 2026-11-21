package com.deviceprofile.spoof;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import io.github.libxposed.api.XposedModule;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class Model extends XposedModule {
  @Override public void onPackageReady(PackageReadyParam p) {
    if (!p.isFirstPackage() || Config.MODULE.equals(p.getPackageName())) return;
    final String[] values=Config.DEFAULTS.clone();
    try {
      SharedPreferences prefs=getRemotePreferences(Config.FILE);
      for(int i=0;i<values.length;i++){
        String value=prefs.getString(Config.KEYS[i],values[i]);
        if(Config.valid(value)) values[i]=value;
      }
    } catch(RuntimeException e){log(Log.WARN,"DeviceProfile","Could not read remote settings; using preset",e);}
    try {
      Class<?> sys=Class.forName("android.os.SystemProperties",false,null);
      hookGet(sys.getDeclaredMethod("get",String.class),values);
      hookGet(sys.getDeclaredMethod("get",String.class,String.class),values);
    } catch(ReflectiveOperationException | RuntimeException e){log(Log.WARN,"DeviceProfile","Could not register SystemProperties hooks",e);}
    for(int i=0;i<values.length;i++){
      try{Field f=Build.class.getDeclaredField(Config.FIELDS[i]); f.setAccessible(true); f.set(null,values[i]);}
      catch(ReflectiveOperationException | RuntimeException e){log(Log.WARN,"DeviceProfile","Could not set Build."+Config.FIELDS[i],e);}
    }
  }
  private void hookGet(Method m,String[] values){
    hook(m).intercept(chain->{
      Object key=chain.getArg(0);
      for(int i=0;i<values.length;i++) if(Config.key(i).equals(key)) return values[i];
      return chain.proceed();
    });
  }
}
