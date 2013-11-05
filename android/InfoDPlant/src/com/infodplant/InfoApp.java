package com.infodplant;

import android.app.Application;

/**
 * Created by marto on 11/5/13.
 */
public class InfoApp extends Application {


    private static InfoApp singleton; //Application is a singleton


    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }

    public InfoApp getInstance(){
        return singleton;
    }

    /**
     * Global Variables
     */


}
