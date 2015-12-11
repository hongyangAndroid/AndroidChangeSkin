package com.zhy.changeskin.attr;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhy.changeskin.ResourceManager;
import com.zhy.changeskin.SkinManager;


/**
 * Created by zhy on 15/9/28.
 */
public enum SkinAttrType
{
    BACKGROUND("background")
            {
                @Override
                public void apply(View view, String resName)
                {
                    Drawable drawable = getResourceManager().getDrawableByName(resName);
                    if (drawable != null)
                    {
                        view.setBackgroundDrawable(drawable);
                    } else
                    {
                        int color = getResourceManager().getColor(resName);
                        if (color == -1) return;
                        view.setBackgroundColor(color);
                    }
                }
            }, COLOR("textColor")
        {
            @Override
            public void apply(View view, String resName)
            {
                ColorStateList colorlist = getResourceManager().getColorStateList(resName);
                if (colorlist == null) return;
                ((TextView) view).setTextColor(colorlist);
            }
        }, SRC("src")
        {
            @Override
            public void apply(View view, String resName)
            {
                if (view instanceof ImageView)
                {
                    Drawable drawable = getResourceManager().getDrawableByName(resName);
                    if (drawable == null) return;
                    ((ImageView) view).setImageDrawable(drawable);
                }

            }
        };

    String attrType;

    SkinAttrType(String attrType)
    {
        this.attrType = attrType;
    }

    public String getAttrType()
    {
        return attrType;
    }


    public abstract void apply(View view, String resName);

    public ResourceManager getResourceManager()
    {
        return SkinManager.getInstance().getResourceManager();
    }

}
