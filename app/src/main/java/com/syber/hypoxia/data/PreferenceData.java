package com.syber.hypoxia.data;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.syber.hypoxia.IApplication;

import java.util.Map;
import java.util.Set;

/**
 * Created by liangtg on 16-3-12.
 */
public class PreferenceData {

    private static PreferenceData instance;
    private SharedPreferences sp;

    public PreferenceData() {
        sp = PreferenceManager.getDefaultSharedPreferences(IApplication.getContext());
    }

    public static PreferenceData getInstance() {
        if (null == instance) {
            synchronized (PreferenceData.class) {
                if (null == instance) instance = new PreferenceData();
            }
        }
        return instance;
    }

    public SharedPreferences getSp() {
        return sp;
    }

    public Map<String, ?> getAll() {
        return sp.getAll();
    }

    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    public int getInt(String key, int defValue) {
        return sp.getInt(key, defValue);
    }

    public boolean contains(String key) {
        return sp.contains(key);
    }

    public float getFloat(String key, float defValue) {
        return sp.getFloat(key, defValue);
    }

    public Set<String> getStringSet(String key, Set<String> defValues) {
        return sp.getStringSet(key, defValues);
    }

    public SharedPreferences.Editor edit() {
        return sp.edit();
    }

    public String getString(String key, String defValue) {
        return sp.getString(key, defValue);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sp.getBoolean(key, defValue);
    }

    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public long getLong(String key, long defValue) {
        return sp.getLong(key, defValue);
    }
}
