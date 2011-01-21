/* © Copyright 2010 to the present, Toktumi, Inc.  */

package com.toktumi.line2.config;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public abstract class BaseSettings {
    protected abstract SharedPreferences getPrefs();

    public String getStringPref(String name, String defValue) {
        return getPrefs().getString(name, defValue);
    }

    public void setStringPref(String name, String value) {
        Editor ed = getPrefs().edit();
        ed.putString(name, value);
        ed.commit();
    }
    
    public long getLongPref(String name, long defValue) {
        return getPrefs().getLong(name, defValue);
    }

    public void setLongPref(String name, long value) {
        Editor ed = getPrefs().edit();
        ed.putLong(name, value);
        ed.commit();
    }
    
    public int getIntPref(String name, int defValue) {
        return getPrefs().getInt(name, defValue);
    }
    
    public void setIntPref(String name, int value) {
        Editor ed = getPrefs().edit();
        ed.putInt(name, value);
        ed.commit();
    }

    public boolean getBoolPref(String name, boolean defValue) {
        return getPrefs().getBoolean(name, defValue);
    }

    public void setBoolPref(String name, boolean value) {
        Editor ed = getPrefs().edit();
        ed.putBoolean(name, value);
        ed.commit();
    }

    public float getFloatPref(String name, float defValue) {
        return getPrefs().getFloat(name, defValue);
    }

    public void setFloatPref(String name, float value) {
        Editor ed = getPrefs().edit();
        ed.putFloat(name, value);
        ed.commit();
    }
}
