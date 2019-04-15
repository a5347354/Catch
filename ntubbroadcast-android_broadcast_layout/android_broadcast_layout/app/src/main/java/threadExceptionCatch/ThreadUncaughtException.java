package threadExceptionCatch;

import android.util.Log;

import blueToothDevices.DeviceInfo;

/**
 * Created by SilentWolf on 2015/5/10.
 */
public class ThreadUncaughtException implements Thread.UncaughtExceptionHandler{
    private DeviceInfo deviceInfo;

    public ThreadUncaughtException(){
        deviceInfo=null;
    }

    public ThreadUncaughtException(DeviceInfo deviceInfo){
        this.deviceInfo=deviceInfo;
    }
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
      //  Log.i("Catch!", deviceInfo.getDevice().getName() + "  Uncaught exception: " + throwable.getCause().toString());
    }
}
