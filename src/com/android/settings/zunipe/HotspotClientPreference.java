package com.android.settings.zunipe;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settings.widget.GearPreference;

public class HotspotClientPreference extends GearPreference {
    public HotspotClientPreference(Context context) {
        this(context, null);
    }

    public HotspotClientPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getSecondTargetResId() {
        return R.layout.hotspot_connected_devices_preference_layout;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View divider = holder.findViewById(com.android.settingslib.widget.preference.twotarget.R.id.two_target_divider);
        if (divider != null) {
            divider.setVisibility(View.GONE);
        }
    }
}
