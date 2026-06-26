package com.android.settings.applications;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.zunipe.ZunipePackageManager;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomHideAppsFragment extends DashboardFragment {

    private static final String TAG = "CustomHideAppsFragment";
    private static final String KEY_CATEGORY = "custom_hide_apps_category";

    private PackageManager mPackageManager;
    private ZunipePackageManager mZunipePackageManager;

    private PreferenceCategory mAppCategory;
    private final Set<String> mHiddenPackageNames = new HashSet<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mPackageManager = context.getPackageManager();
        mZunipePackageManager = (ZunipePackageManager) context.getSystemService(ZunipePackageManager.class);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mAppCategory = findPreference(KEY_CATEGORY);

        loadApplications();
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.custom_hide_apps_setting;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.VIEW_UNKNOWN;
    }

    private void loadApplications() {
        if (mAppCategory == null) return;
        mAppCategory.removeAll();
        mHiddenPackageNames.clear();

        if (mZunipePackageManager != null) {
            List<String> currentHiddenApps = mZunipePackageManager.getHideApplicationList();
            if (currentHiddenApps != null) {
                mHiddenPackageNames.addAll(currentHiddenApps);
            }
        }

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = mPackageManager.queryIntentActivities(mainIntent, 0);

        for (ResolveInfo resolveInfo : apps) {
            String packageName = resolveInfo.activityInfo.packageName;
            try {
                ApplicationInfo appInfo = mPackageManager.getApplicationInfo(packageName, PackageManager.MATCH_UNINSTALLED_PACKAGES);
                if (appInfo.isSystemApp() && appInfo.isPrivilegedApp()) {
                    continue;
                }
            } catch (PackageManager.NameNotFoundException e) {
                continue;
            }

            CharSequence label = resolveInfo.loadLabel(mPackageManager);
            String appName = label.toString();
            Drawable appIcon = resolveInfo.loadIcon(mPackageManager);

            SwitchPreference appPref = new SwitchPreference(getPrefContext());
            appPref.setKey(packageName);
            appPref.setTitle(appName);
            appPref.setIcon(appIcon);

            boolean isHidden = mHiddenPackageNames.contains(packageName);
            appPref.setChecked(isHidden);

            appPref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean isChecked = (Boolean) newValue;
                onHideStatusChanged(packageName, isChecked);
                return true;
            });

            mAppCategory.addPreference(appPref);
        }
    }

    private void onHideStatusChanged(String packageName, boolean isChecked) {
        if (isChecked) {
            mHiddenPackageNames.add(packageName);
            if (mZunipePackageManager != null) {
                mZunipePackageManager.hideApplication(packageName);
            }
        } else {
            mHiddenPackageNames.remove(packageName);
            if (mZunipePackageManager != null) {
                mZunipePackageManager.revealApplication(packageName);
            }
        }
    }
}