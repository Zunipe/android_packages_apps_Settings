package com.android.settings.applications;

import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.ListFormatter;
import android.provider.Settings;
import android.text.TextUtils;
import android.content.pm.ResolveInfo;
import com.android.settings.R;
import androidx.annotation.NonNull;
import androidx.core.text.BidiFormatter;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.SubSettingLauncher;
import com.android.settingslib.applications.AppUtils;

import java.util.ArrayList;
import java.util.List;

public class CustomHideAppsPreferenceController extends BasePreferenceController {

    private final PackageManager mPackageManager;
    private final RoleManager mRoleManager;
    private final Context mContext;

    public CustomHideAppsPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);

        mContext = context;
        mPackageManager = context.getPackageManager();
        mRoleManager = context.getSystemService(RoleManager.class);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(@NonNull PreferenceScreen screen) {
        super.displayPreference(screen);

        Preference pref = screen.findPreference(getPreferenceKey());
        if (pref != null) {
            Intent intent = new SubSettingLauncher(mContext)
                    .setDestination(CustomHideAppsFragment.class.getName())
                    .setTitleText(mContext.getString(R.string.app_custom_hide_dashboard_title))
                    .setSourceMetricsCategory(MetricsEvent.VIEW_UNKNOWN)
                    .toIntent();
            pref.setIntent(intent);
        }
    }

//    @Override
//    public CharSequence getSummary() {
//        List<CharSequence> hideAppLabels = getLauncherAppLabels();
//
//        if (hideAppLabels.isEmpty()) {
//            return null;
//        }
//        return ListFormatter.getInstance().format(hideAppLabels);
//    }

    private List<CharSequence> getLauncherAppLabels() {
        List<CharSequence> appLabels = new ArrayList<>();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = mPackageManager.queryIntentActivities(mainIntent, 0);

        for (ResolveInfo resolveInfo : apps) {
            CharSequence label = resolveInfo.loadLabel(mPackageManager);
            if (label != null) {
                appLabels.add(label);
            }
        }
        return appLabels;
    }
}

