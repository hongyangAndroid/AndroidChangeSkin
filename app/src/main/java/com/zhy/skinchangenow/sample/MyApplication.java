package com.zhy.skinchangenow.sample;

import android.app.Application;

import com.zhy.changeskin.SkinManager;


/**
 * Created by zhy on 15/9/22.
 */
public class MyApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        SkinManager.getInstance().init(this);
    }
}
