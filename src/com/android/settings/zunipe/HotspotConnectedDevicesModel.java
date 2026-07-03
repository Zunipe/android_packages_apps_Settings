package com.android.settings.zunipe;

import android.app.Application;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.MacAddress;
import android.net.TetheredClient;
import android.net.TetheringManager;
import android.net.wifi.SoftApConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class HotspotConnectedDevicesModel extends AndroidViewModel {

    private static final String HOTSPOT_BLACKLIST_MACS_KEY = "hotspot_blacklist_macs";

    protected TetheringManager mTetheringManager;
    protected WifiManager mWifiManager;
    protected EventCallback mEventCallback = new EventCallback();
    protected MacObserver mMacObserver = new MacObserver(null);
    protected MutableLiveData<Collection<TetheredClient>> mConnectedDevices =
            new MutableLiveData<>();

    private final ContentResolver mContentResolver;
    protected MutableLiveData<List<BlacklistEntry>> mBlacklist =
            new MutableLiveData<>();

    public HotspotConnectedDevicesModel(@NonNull Application application) {
        super(application);
        mContentResolver = application.getContentResolver();
        mTetheringManager = application.getSystemService(TetheringManager.class);
        mWifiManager = application.getSystemService(WifiManager.class);
        mTetheringManager.registerTetheringEventCallback(
                application.getMainExecutor(), mEventCallback);
        mContentResolver.registerContentObserver(
                Settings.System.getUriFor(HOTSPOT_BLACKLIST_MACS_KEY),
                false,
                mMacObserver);
        mBlacklist.setValue(getBlacklist());
    }

    public static final class BlacklistEntry {
        public final String mac;
        @Nullable
        public final String hostname;

        public BlacklistEntry(@NonNull String mac, @Nullable String hostname) {
            this.mac = mac;
            this.hostname = hostname;
        }

        @NonNull
        public String getDisplayName() {
            return (hostname != null && !hostname.isEmpty()) ? hostname : mac;
        }
    }

    public void addBlacklist(@Nullable String hostname, @NonNull String mac) {
        List<BlacklistEntry> blacklist = new ArrayList<>(getBlacklist());

        boolean updated = false;
        for (int i = 0; i < blacklist.size(); i++) {
            if (blacklist.get(i).mac.equalsIgnoreCase(mac)) {
                blacklist.set(i, new BlacklistEntry(mac, hostname));
                updated = true;
                break;
            }
        }
        if (!updated) {
            blacklist.add(new BlacklistEntry(mac, hostname));
        }

        applyBlacklist(blacklist);
    }

    public void removeBlacklist(@NonNull String mac) {
        List<BlacklistEntry> blacklist = new ArrayList<>(getBlacklist());
        blacklist.removeIf(entry -> entry.mac.equalsIgnoreCase(mac));
        applyBlacklist(blacklist);
    }

    private void applyBlacklist(@NonNull List<BlacklistEntry> blacklist) {
        List<MacAddress> macAddresses = new ArrayList<>();
        for (BlacklistEntry entry : blacklist) {
            macAddresses.add(MacAddress.fromString(entry.mac));
        }

        SoftApConfiguration cur = mWifiManager.getSoftApConfiguration();
        SoftApConfiguration newConfig = new SoftApConfiguration.Builder(cur)
                .setClientControlByUserEnabled(false)
                .setBlockedClientList(macAddresses)
                .build();
        mWifiManager.setSoftApConfiguration(newConfig);

        saveBlacklist(blacklist);
        mBlacklist.setValue(blacklist);
    }

    @NonNull
    private List<BlacklistEntry> getBlacklist() {
        String raw = Settings.System.getString(mContentResolver, HOTSPOT_BLACKLIST_MACS_KEY);
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }
        return parseJsonBlacklist(raw);
    }

    @NonNull
    private List<BlacklistEntry> parseJsonBlacklist(@NonNull String json) {
        List<BlacklistEntry> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String mac = obj.getString("mac");
                String hostname = obj.isNull("hostname") ? null : obj.optString("hostname");
                list.add(new BlacklistEntry(mac, hostname));
            }
        } catch (JSONException e) {
            Log.e("HotspotModel", "parse blacklist failed: " + json, e);
        }
        return list;
    }

    private void saveBlacklist(@NonNull List<BlacklistEntry> blacklist) {
        JSONArray arr = new JSONArray();
        try {
            for (BlacklistEntry entry : blacklist) {
                JSONObject obj = new JSONObject();
                obj.put("mac", entry.mac);
                if (entry.hostname == null) {
                    obj.put("hostname", JSONObject.NULL);
                } else {
                    obj.put("hostname", entry.hostname);
                }
                arr.put(obj);
            }
        } catch (JSONException e) {
            Log.e("HotspotModel", "save blacklist failed", e);
            return;
        }
        Settings.System.putString(mContentResolver, HOTSPOT_BLACKLIST_MACS_KEY, arr.toString());
    }

    public LiveData<List<BlacklistEntry>> getBlacklistEntries() {
        return Transformations.distinctUntilChanged(mBlacklist);
    }

    public LiveData<Collection<TetheredClient>> getConnectedDevices() {
        return Transformations.distinctUntilChanged(mConnectedDevices);
    }

    @Override
    protected void onCleared() {
        mTetheringManager.unregisterTetheringEventCallback(mEventCallback);
        mContentResolver.unregisterContentObserver(mMacObserver);
    }

    protected class MacObserver extends ContentObserver {
        public MacObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            mBlacklist.setValue(getBlacklist());
        }
    }

    protected class EventCallback implements TetheringManager.TetheringEventCallback {
        @Override
        public void onClientsChanged(Collection<TetheredClient> clients) {
            mConnectedDevices.setValue(clients);
        }
    }
}