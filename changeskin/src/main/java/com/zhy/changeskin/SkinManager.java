package com.zhy.changeskin;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;

import com.zhy.changeskin.attr.SkinAttrSupport;
import com.zhy.changeskin.attr.SkinView;
import com.zhy.changeskin.callback.ISkinChangingCallback;
import com.zhy.changeskin.utils.L;
import com.zhy.changeskin.utils.PrefUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhy on 15/9/22.
 */
public class SkinManager
{
    private Context mContext;
    private Resources mResources;
    private ResourceManager mResourceManager;
    private PrefUtils mPrefUtils;

    private boolean usePlugin;

    private String mSuffix = "";
    private String mCurPluginPath;
    private String mCurPluginPkg;


    private List<Activity> mActivities = new ArrayList<Activity>();

    private SkinManager()
    {
    }

    private static class SingletonHolder
    {
        static SkinManager sInstance = new SkinManager();
    }

    public static SkinManager getInstance()
    {
        return SingletonHolder.sInstance;
    }


    public void init(Context context)
    {
        mContext = context.getApplicationContext();
        mPrefUtils = new PrefUtils(mContext);

        String skinPluginPath = mPrefUtils.getPluginPath();
        String skinPluginPkg = mPrefUtils.getPluginPkgName();
        mSuffix = mPrefUtils.getSuffix();

        if (!validPluginParams(skinPluginPath, skinPluginPkg))
            return;

        try
        {
            loadPlugin(skinPluginPath, skinPluginPkg, mSuffix);
            mCurPluginPath = skinPluginPath;
            mCurPluginPkg = skinPluginPkg;
        } catch (Exception e)
        {
            mPrefUtils.clear();
            e.printStackTrace();
        }
    }

    private PackageInfo getPackageInfo(String skinPluginPath) {
        PackageManager pm = mContext.getPackageManager();
        return pm.getPackageArchiveInfo(skinPluginPath, PackageManager.GET_ACTIVITIES);
    }


    private void loadPlugin(String skinPath, String skinPkgName, String suffix) throws Exception
    {
        AssetManager assetManager = AssetManager.class.newInstance();
        Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
        addAssetPath.invoke(assetManager, skinPath);

        Resources superRes = mContext.getResources();
        mResources = new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
        mResourceManager = new ResourceManager(mResources, skinPkgName, suffix);
        usePlugin = true;
    }

    private boolean validPluginParams(String skinPath, String skinPkgName)
    {
        if (TextUtils.isEmpty(skinPath) || TextUtils.isEmpty(skinPkgName))
        {
            return false;
        }

        File file = new File(skinPath);
        if (!file.exists())
            return false;

        PackageInfo info = getPackageInfo(skinPath);
        if (!info.packageName.equals(skinPkgName))
            return false;
        return true;
    }

    private void checkPluginParamsThrow(String skinPath, String skinPkgName)
    {
        if (!validPluginParams(skinPath, skinPkgName))
        {
            throw new IllegalArgumentException("skinPluginPath or skinPkgName not valid ! ");
        }
    }


    public void removeAnySkin()
    {
        L.e("removeAnySkin");
        clearPluginInfo();
        notifyChangedListeners();
    }


    public boolean needChangeSkin()
    {
        return usePlugin || !TextUtils.isEmpty(mSuffix);
    }


    public ResourceManager getResourceManager()
    {
        if (!usePlugin)
        {
            mResourceManager = new ResourceManager(mContext.getResources(), mContext.getPackageName(), mSuffix);
        }
        return mResourceManager;
    }


    /**
     * 应用内换肤，传入资源区别的后缀
     *
     * @param suffix
     */
    public void changeSkin(String suffix)
    {
        clearPluginInfo();//clear before
        mSuffix = suffix;
        mPrefUtils.putPluginSuffix(suffix);
        notifyChangedListeners();
    }

    private void clearPluginInfo()
    {
        mCurPluginPath = null;
        mCurPluginPkg = null;
        usePlugin = false;
        mSuffix = null;
        mPrefUtils.clear();
    }

    private void updatePluginInfo(String skinPluginPath, String pkgName, String suffix)
    {
        mPrefUtils.putPluginPath(skinPluginPath);
        mPrefUtils.putPluginPkg(pkgName);
        mPrefUtils.putPluginSuffix(suffix);

        mCurPluginPkg = pkgName;
        mCurPluginPath = skinPluginPath;
        mSuffix = suffix;
    }


    public void changeSkin(final String skinPluginPath, final String skinPluginPkg, ISkinChangingCallback callback)
    {
        changeSkin(skinPluginPath, skinPluginPkg, null, callback);
    }


    /**
     * 根据suffix选择插件内某套皮肤，默认为""
     *
     * @param skinPluginPath
     * @param skinPluginPkg
     * @param suffix
     * @param callback
     */
    public void changeSkin(final String skinPluginPath, final String skinPluginPkg, final String suffix, ISkinChangingCallback callback)
    {
        L.e("changeSkin = " + skinPluginPath + " , " + skinPluginPkg);
        if (callback == null)
            callback = ISkinChangingCallback.DEFAULT_SKIN_CHANGING_CALLBACK;
        final ISkinChangingCallback skinChangingCallback = callback;

        skinChangingCallback.onStart();

        try {
            checkPluginParamsThrow(skinPluginPath, skinPluginPkg);
        } catch (IllegalArgumentException e) {
            skinChangingCallback.onError(new RuntimeException("checkPlugin occur error"));
            return;
        }

        new AsyncTask<Void, Void, Integer>()
        {
            @Override
            protected Integer doInBackground(Void... params)
            {
                try
                {
                    loadPlugin(skinPluginPath, skinPluginPkg, suffix);
                    return 1;
                } catch (Exception e)
                {
                    e.printStackTrace();
                    return 0;
                }

            }

            @Override
            protected void onPostExecute(Integer res)
            {
                if (res == 0)
                {
                    skinChangingCallback.onError(new RuntimeException("loadPlugin occur error"));
                    return;
                }
                try
                {
                    updatePluginInfo(skinPluginPath, skinPluginPkg, suffix);
                    notifyChangedListeners();
                    skinChangingCallback.onComplete();
                } catch (Exception e)
                {
                    e.printStackTrace();
                    skinChangingCallback.onError(e);
                }

            }
        }.execute();
    }


    public void apply(Activity activity)
    {
        List<SkinView> skinViews = SkinAttrSupport.getSkinViews(activity);
        if (skinViews == null) return;
        for (SkinView skinView : skinViews)
        {
            skinView.apply();
        }
    }

    public void register(final Activity activity)
    {
        mActivities.add(activity);

        activity.findViewById(android.R.id.content).post(new Runnable()
        {
            @Override
            public void run()
            {
                apply(activity);
            }
        });
    }

    public void unregister(Activity activity)
    {
        mActivities.remove(activity);
    }

    public void notifyChangedListeners()
    {

        for (Activity activity : mActivities)
        {
            apply(activity);
        }
    }

    /**
     * apply for dynamic construct view
     *
     * @param view
     */
    public void injectSkin(View view)
    {
        List<SkinView> skinViews = new ArrayList<SkinView>();
        SkinAttrSupport.addSkinViews(view, skinViews);
        for (SkinView skinView : skinViews)
        {
            skinView.apply();
        }
    }


}
