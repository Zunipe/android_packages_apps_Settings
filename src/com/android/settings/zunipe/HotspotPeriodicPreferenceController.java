package com.android.settings.zunipe;

import android.app.TimePickerDialog;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.display.darkmode.TimeFormatter;

import java.time.LocalTime;

public class HotspotPeriodicPreferenceController extends BasePreferenceController {
    private static final String PERIODIC_CLOSE_KEY = "periodic_close";
    private static final String PERIODIC_CLOSE_START_KEY = "periodic_close_start_time";
    private static final String PERIODIC_CLOSE_END_KEY = "periodic_close_end_time";
    private static final int DEFAULT_START_TIME = LocalTime.of(23, 0).toSecondOfDay();
    private static final int DEFAULT_END_TIME = LocalTime.of(9, 0).toSecondOfDay();
    private AutoHotspotSettings mHost;
    private final TimeFormatter mFormat;

    public HotspotPeriodicPreferenceController(@NonNull Context context, @NonNull String preferenceKey) {
        super(context, preferenceKey);
        mFormat = new TimeFormatter(mContext);
    }

    public HotspotPeriodicPreferenceController(@NonNull Context context, @NonNull String preferenceKey, AutoHotspotSettings fragment) {
        this(context, preferenceKey);
        mHost = fragment;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference preference = screen.findPreference(getPreferenceKey());
        if (preference != null) {
            preference.setOnPreferenceClickListener(p -> {
                getDialog().show();
                return true;
            });
        }
    }

    public TimePickerDialog getDialog() {
        final LocalTime initialTime;
        int nanoOfDay = Settings.System.getInt(
                mContext.getContentResolver(),
                getPreferenceKey(),
                PERIODIC_CLOSE_START_KEY.equals(getPreferenceKey()) ? DEFAULT_START_TIME : DEFAULT_END_TIME);
        initialTime = LocalTime.ofSecondOfDay(nanoOfDay);
        return  new TimePickerDialog(mContext, (view, hourOfDay, minute) -> {
            final LocalTime time = LocalTime.of(hourOfDay, minute);
            Settings.System.putInt(mContext.getContentResolver(), getPreferenceKey(), time.toSecondOfDay());

            if (mHost != null) {
                mHost.refresh();
            }
        }, initialTime.getHour(), initialTime.getMinute(), mFormat.is24HourFormat());
    }

    @Override
    public int getAvailabilityStatus() {
        return Settings.System.getInt(mContext.getContentResolver(), PERIODIC_CLOSE_KEY, 0) == 1 ?
                AVAILABLE_UNSEARCHABLE :
                CONDITIONALLY_UNAVAILABLE;
    }

    @Override
    protected void refreshSummary(Preference preference) {
        final LocalTime time;
        int nanoOfDay = Settings.System.getInt(
                mContext.getContentResolver(),
                getPreferenceKey(),
                PERIODIC_CLOSE_START_KEY.equals(getPreferenceKey()) ? DEFAULT_START_TIME : DEFAULT_END_TIME);
        time = LocalTime.ofSecondOfDay(nanoOfDay);
        preference.setSummary(mFormat.of(time));
    }
}
