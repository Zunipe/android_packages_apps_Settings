package com.android.settings.zunipe;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class GameSettings extends DashboardFragment {
    private static final String TAG = "GameSettings";
    private String[] mLabels;
    private boolean test = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        mLabels = getPrefContext().getResources().getStringArray(R.array.perf_monitor_option_items);

        Preference binaryPref = findPreference("binary_multi_select");
        binaryPref.setOnPreferenceClickListener(preference -> {
            ChooseLockSettingsHelper.Builder builder = new ChooseLockSettingsHelper.Builder(getActivity(), this);
            boolean launched = builder.setRequestCode(1001)
                    .setTitle("安全验证")
                    .setDescription("请验证密码以继续")
                    .show();

            if (!launched) {
                showMultiSelectDialog();
            }
            return true;
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            if (resultCode == Activity.RESULT_OK) {
                showMultiSelectDialog();
            }
        }
    }

    private void showMultiSelectDialog() {
        Context context = getPrefContext();
        if (context == null || mLabels == null) return;

        ContentResolver resolver = context.getContentResolver();

        int currentValue = Settings.Secure.getInt(resolver, "perf_monitor_option", 0);

        boolean[] checkedItems = new boolean[mLabels.length];
        for (int i = 0; i < mLabels.length; i++) {
            int mask = 1 << i;
            checkedItems[i] = (currentValue & mask) != 0;
        }

        final boolean[] tempCheckedItems = checkedItems.clone();

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.perf_monitor_option_title)
                .setMultiChoiceItems(mLabels, tempCheckedItems, (dialog, which, isChecked) -> {
                    tempCheckedItems[which] = isChecked;
                })
                .setPositiveButton(R.string.okay, (dialog, id) -> {
                    int newValue = 0;
                    for (int i = 0; i < mLabels.length; i++) {
                        int mask = 1 << i;
                        if (tempCheckedItems[i]) {
                            newValue |= mask;
                        }
                    }

                    Settings.Secure.putInt(resolver, "perf_monitor_option", newValue);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.game_settings;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }
}
