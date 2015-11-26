package com.zhy.changeskin.attr;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.zhy.changeskin.R;
import com.zhy.changeskin.constant.SkinConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhy on 15/9/23.
 */
public class SkinAttrSupport
{

    private static SkinAttrType getSupportAttrType(String attrName)
    {
        for (SkinAttrType attrType : SkinAttrType.values())
        {
            if (attrType.getAttrType().equals(attrName))
                return attrType;
        }
        return null;
    }

    /**
     * 传入activity，找到content元素，递归遍历所有的子View，根据tag命名，记录需要换肤的View
     *
     * @param activity
     */
    public static List<SkinView> getSkinViews(Activity activity)
    {
        List<SkinView> skinViews = new ArrayList<SkinView>();
        ViewGroup content = (ViewGroup) activity.findViewById(android.R.id.content);
        addSkinViews(content, skinViews);
        return skinViews;
    }

    public static void addSkinViews(View view, List<SkinView> skinViews)
    {
        SkinView skinView = getSkinView(view);
        if (skinView != null) skinViews.add(skinView);

        if (view instanceof ViewGroup)
        {
            ViewGroup container = (ViewGroup) view;

            for (int i = 0, n = container.getChildCount(); i < n; i++)
            {
                View child = container.getChildAt(i);
                addSkinViews(child, skinViews);
            }
        }

    }

    public static SkinView getSkinView(View view)
    {
        Object tag = view.getTag(R.id.skin_tag_id);
        if (tag == null)
        {
            tag = view.getTag();
        }
        if (tag == null) return null;
        if (!(tag instanceof String)) return null;
        String tagStr = (String) tag;

        List<SkinAttr> skinAttrs = parseTag(tagStr);
        if (!skinAttrs.isEmpty())
        {
            changeViewTag(view);
            return new SkinView(view, skinAttrs);
        }
        return null;
    }

    private static void changeViewTag(View view)
    {
        Object tag = view.getTag(R.id.skin_tag_id);
        if(tag == null )
        {
            tag = view.getTag();
            view.setTag(R.id.skin_tag_id, tag);
            view.setTag(null);
        }
    }

    //skin:left_menu_icon:src|skin:color_red:textColor
    private static List<SkinAttr> parseTag(String tagStr)
    {
        List<SkinAttr> skinAttrs = new ArrayList<SkinAttr>();
        if (TextUtils.isEmpty(tagStr)) return skinAttrs;

        String[] items = tagStr.split("[|]");
        for (String item : items)
        {
            if (!item.startsWith(SkinConfig.SKIN_PREFIX))
                continue;
            String[] resItems = item.split(":");
            if (resItems.length != 3)
                continue;

            String resName = resItems[1];
            String resType = resItems[2];

            SkinAttrType attrType = getSupportAttrType(resType);
            if (attrType == null) continue;
            SkinAttr attr = new SkinAttr(attrType, resName);
            skinAttrs.add(attr);
        }
        return skinAttrs;
    }
}
