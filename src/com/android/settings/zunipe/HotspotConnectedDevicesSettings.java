package com.android.settings.zunipe;

import android.content.Context;
import android.net.TetheredClient;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;

import java.util.Collection;
import java.util.List;

public class HotspotConnectedDevicesSettings extends DashboardFragment {
    private static final String TAG = "HotspotConnectedDevicesSettings";
    private static final String CONNECTED_DEVICES_CATEGORY_KEY = "hotspot_connected_devices_category";
    private static final String BLACK_LIST_DEVICES_CATEGORY_KEY = "hotspot_black_list_devices";
    private PreferenceCategory mDevicesCategory;
    private PreferenceCategory mBlacklistCategory;
    private Context mContext;
    private HotspotConnectedDevicesModel mModel;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mContext = getContext();
        mDevicesCategory = findPreference(CONNECTED_DEVICES_CATEGORY_KEY);
        mBlacklistCategory = findPreference(BLACK_LIST_DEVICES_CATEGORY_KEY);

        mModel = new ViewModelProvider(this).get(HotspotConnectedDevicesModel.class);
        mModel.getConnectedDevices().observe(this, this::onClientsChanged);
        mModel.getBlacklistEntries().observe(this, this::onBlacklistChanged);
    }

    private TetheredClient.AddressInfo getBestAddressInfo(List<TetheredClient.AddressInfo> list) {
        if (list == null || list.isEmpty()) return null;

        TetheredClient.AddressInfo bestChoose = null;
        for (TetheredClient.AddressInfo ipAddress : list) {
            if (bestChoose == null) {
                bestChoose = ipAddress;
                continue;
            }

            if (bestChoose.getHostname() == null && ipAddress.getHostname() != null) {
                bestChoose = ipAddress;
            }
        }
        return bestChoose;
    }

    public void onBlacklistChanged(List<HotspotConnectedDevicesModel.BlacklistEntry> macs) {
        mBlacklistCategory.removeAll();

        if (macs == null || macs.isEmpty()) {
            Preference emptyPref = new Preference(mContext);
            emptyPref.setTitle(R.string.hotspot_black_list_empty_title);
            emptyPref.setSelectable(false);
            mBlacklistCategory.addPreference(emptyPref);
            return;
        }

        for (HotspotConnectedDevicesModel.BlacklistEntry entry : macs) {
            HotspotClientPreference devicePref = new HotspotClientPreference(mContext);
            String hostname = entry.hostname;
            if (hostname == null) {
                hostname = getString(R.string.hotspot_connected_devices_unknown_hostname);
            }
            devicePref.setTitle(hostname);
            devicePref.setSummary(entry.mac);
            devicePref.setOnGearClickListener(p -> {
                mModel.removeBlacklist(entry.mac);
            });
            mBlacklistCategory.addPreference(devicePref);
        }
    }

    public void onClientsChanged(Collection<TetheredClient> clients) {
        mDevicesCategory.removeAll();

        if (clients == null || clients.isEmpty()) {
            Preference emptyPref = new Preference(mContext);
            emptyPref.setTitle(R.string.hotspot_connected_devices_empty_title);
            emptyPref.setSelectable(false);
            mDevicesCategory.addPreference(emptyPref);
            return;
        }

        for (TetheredClient client : clients) {
            android.net.MacAddress mac = client.getMacAddress();
            TetheredClient.AddressInfo bestChoose = getBestAddressInfo(client.getAddresses());
            if (bestChoose != null) {
                java.net.InetAddress ipAddress = bestChoose.getAddress().getAddress();
                String hostname = bestChoose.getHostname();
                HotspotClientPreference devicePref = new HotspotClientPreference(mContext);
                if (hostname == null) {
                    hostname = getString(R.string.hotspot_connected_devices_unknown_hostname);
                }
                String title = String.format("%s\n%s", hostname, ipAddress.getHostAddress());
                devicePref.setTitle(title);
                devicePref.setSummary(mac.toString());
                devicePref.setOnGearClickListener(p -> {
                    mModel.addBlacklist(bestChoose.getHostname(), mac.toString());
                });
                mDevicesCategory.addPreference(devicePref);
            }
        }
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.hotspot_connected_devices;
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
