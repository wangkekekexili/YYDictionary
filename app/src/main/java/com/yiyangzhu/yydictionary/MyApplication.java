package com.yiyangzhu.yydictionary;

import android.app.Application;

import com.firebase.client.Firebase;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Created by kewang on 1/1/16.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        JodaTimeAndroid.init(this);
    }
}
