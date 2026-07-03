package com.android.settings.zunipe;

import android.content.Context;
import android.net.TetheredClient;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

import java.util.Collection;

public class HotspotConnectedDevicesPreferenceController extends BasePreferenceController {
    private Preference mPreference;
    private int mDeviceCount = 0;
    private final Context mContext;

    public HotspotConnectedDevicesPreferenceController(@NonNull Context context, @NonNull String preferenceKey) {
        super(context, preferenceKey);
        mContext = context;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
    }

    public void onClientsChanged(Collection<TetheredClient> clients) {
        mDeviceCount = clients.size();
        refreshSummary(mPreference);
    }

    @Override
    public CharSequence getSummary() {
        return mContext.getString(R.string.hotspot_connected_devices_summary, mDeviceCount);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }
}
