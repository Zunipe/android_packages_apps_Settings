package com.android.settings.wifi.tether;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;

import com.android.settingslib.core.AbstractPreferenceController;

public class WifiTetherAutoEnablePreferenceController extends AbstractPreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String PREF_KEY = "wifi_hotspot_auto_enable";

    private static final int DEFAULT_VALUE = 0;

    private TwoStatePreference mPreference;

    private final ContentObserver mObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            updateDisplay();
        }
    };

    private final Uri mSettingUri = Settings.System.getUriFor(PREF_KEY);

    public WifiTetherAutoEnablePreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return PREF_KEY;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final Preference pref = screen.findPreference(getPreferenceKey());
        if (!(pref instanceof TwoStatePreference)) {
            return;
        }
        mPreference = (TwoStatePreference) pref;
        mPreference.setOnPreferenceChangeListener(this);
        updateDisplay();
        mContext.getContentResolver().registerContentObserver(mSettingUri, false, mObserver);
    }

    public void unregisterObserver() {
        mContext.getContentResolver().unregisterContentObserver(mObserver);
    }

    public void updateDisplay() {
        if (mPreference == null) {
            return;
        }
        final int v = Settings.System.getInt(
                mContext.getContentResolver(), PREF_KEY, DEFAULT_VALUE);
        final boolean on = v != 0;
        if (mPreference.isChecked() != on) {
            mPreference.setChecked(on);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!(newValue instanceof Boolean)) {
            return false;
        }
        final boolean enabled = (Boolean) newValue;
        return Settings.System.putInt(
                mContext.getContentResolver(),
                PREF_KEY,
                enabled ? 1 : 0);
    }
}