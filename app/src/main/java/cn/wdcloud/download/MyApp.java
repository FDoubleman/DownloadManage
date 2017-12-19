package cn.wdcloud.download;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;

/**
 * Created by fmm on 2017/12/18.
 */

public class MyApp extends Application {
    public static Context sContext;
    @Override
    public void onCreate() {
        super.onCreate();
        sContext =this.getApplicationContext();

        Stetho.initializeWithDefaults(this);
    }
}
