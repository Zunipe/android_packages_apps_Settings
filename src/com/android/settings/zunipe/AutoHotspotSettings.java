package com.android.settings.zunipe;

import android.content.Context;

import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class AutoHotspotSettings extends DashboardFragment {
    private static final String TAG = "AutoHotspotSettings";
    private static final String PERIODIC_CLOSE_START_KEY = "periodic_close_start_time";
    private static final String PERIODIC_CLOSE_END_KEY = "periodic_close_end_time";
    private static final String PERIODIC_CLOSE_SWITCH_KEY = "periodic_close";
    private HotspotPeriodicPreferenceController mStartController;
    private HotspotPeriodicPreferenceController mEndController;
    private PeriodicClosePreferenceController mPeriodicSwitchController;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.auto_hotspot_settings;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers =  new ArrayList<>(3);

        mStartController = new HotspotPeriodicPreferenceController(context, PERIODIC_CLOSE_START_KEY, this);
        mEndController = new HotspotPeriodicPreferenceController(context, PERIODIC_CLOSE_END_KEY, this);
        mPeriodicSwitchController = new PeriodicClosePreferenceController(context, PERIODIC_CLOSE_SWITCH_KEY, this);
        controllers.add(mStartController);
        controllers.add(mEndController);
        controllers.add(mPeriodicSwitchController);
        return controllers;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    public void refresh() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        mStartController.displayPreference(preferenceScreen);
        mEndController.displayPreference(preferenceScreen);
        this.updatePreferenceStates();
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.auto_hotspot_settings);
}
