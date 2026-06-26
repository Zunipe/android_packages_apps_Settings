package com.android.settings.zunipe;

import android.content.Context;
import android.provider.Settings;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.core.TogglePreferenceController;
import com.android.settings.widget.SettingsMainSwitchPreference;

public class PeriodicClosePreferenceController extends TogglePreferenceController {
    private AutoHotspotSettings mHost;

    public PeriodicClosePreferenceController(@NonNull Context context, @NonNull String preferenceKey) {
        super(context, preferenceKey);
    }

    public PeriodicClosePreferenceController(@NonNull Context context, @NonNull String preferenceKey, AutoHotspotSettings host) {
        this(context, preferenceKey);
        mHost = host;
    }

    @Override
    public boolean isChecked() {
        return Settings.System.getInt(mContext.getContentResolver(),
                getPreferenceKey(), 0) != 0;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        Settings.System.putInt(mContext.getContentResolver(), getPreferenceKey(),
                isChecked ? 1 : 0);
        if (mHost != null) {
            mHost.refresh();
        }
        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        SwitchPreferenceCompat pref = screen.findPreference(
                getPreferenceKey());
        if (pref != null) {
            pref.setOnPreferenceChangeListener(this);
            pref.setChecked(isChecked());
        }
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE_UNSEARCHABLE;
    }
}
