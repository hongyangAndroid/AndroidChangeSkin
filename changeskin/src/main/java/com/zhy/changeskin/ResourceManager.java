package com.zhy.changeskin;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.zhy.changeskin.utils.L;

/**
 * Created by zhy on 15/9/22.
 */
public class ResourceManager
{
    private static final String DEFTYPE_DRAWABLE = "drawable";
    private static final String DEFTYPE_COLOR = "color";
    private Resources mResources;
    private String mPluginPackageName;
    private String mSuffix;


    public ResourceManager(Resources res, String pluginPackageName, String suffix)
    {
        mResources = res;
        mPluginPackageName = pluginPackageName;

        if (suffix == null)
        {
            suffix = "";
        }
        mSuffix = suffix;

    }

    public Drawable getDrawableByName(String name)
    {
        try
        {
            name = appendSuffix(name);
            L.e("name = " + name + " , " + mPluginPackageName);
            return mResources.getDrawable(mResources.getIdentifier(name, DEFTYPE_DRAWABLE, mPluginPackageName));
        } catch (Resources.NotFoundException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public int getColor(String name) throws Resources.NotFoundException
    {
        name = appendSuffix(name);
        L.e("name = " + name);
        return mResources.getColor(mResources.getIdentifier(name, DEFTYPE_COLOR, mPluginPackageName));
    }

    public ColorStateList getColorStateList(String name)
    {
        try
        {
            name = appendSuffix(name);
            L.e("name = " + name);
            return mResources.getColorStateList(mResources.getIdentifier(name, DEFTYPE_COLOR, mPluginPackageName));

        } catch (Resources.NotFoundException e)
        {
            e.printStackTrace();
            return null;
        }

    }

    private String appendSuffix(String name)
    {
        if (!TextUtils.isEmpty(mSuffix))
            return name += "_" + mSuffix;
        return name;
    }

}
