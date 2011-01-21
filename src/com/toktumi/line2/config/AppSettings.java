/* © Copyright 2010 to the present, Toktumi, Inc.  */

package com.toktumi.line2.config;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class AppSettings extends BaseSettings {
	@Override
    protected SharedPreferences getPrefs() {
   		return PreferenceManager.getDefaultSharedPreferences(Main.MainActivity);
    }


}
