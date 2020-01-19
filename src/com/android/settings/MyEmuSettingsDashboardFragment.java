package com.android.settings;

import android.util.Log;
import android.os.SystemProperties;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.PowerManager;


public class MyEmuSettingsDashboardFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "MyEmuSettings";

    private static final String KEY_DENSITY_LIST = "density_setting_list";

    private ListPreference densityListPreference;

    private AlertDialog alertDialog;

    @Override
    public void onCreate(Bundle icicle){
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.myemu_settings);

        densityListPreference = (ListPreference)findPreference(KEY_DENSITY_LIST);
        if(densityListPreference == null){
            Log.e(TAG, "densityListPreference is not found!");
            return;
        }
        String device_setting = SystemProperties.get("persist.sys.device", "none");
        if(!device_setting.equals("none")){
            densityListPreference.setValue(device_setting);
        }
        densityListPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (densityListPreference == preference) {
            if(!SystemProperties.get("persist.sys.device", "none").equals((String)newValue)){
                Log.i(TAG, "onPreferenceChange is called. Set to " + (String)newValue);
                switch ((String)newValue){
                    case "Phone":
                        SystemProperties.set("persist.sys.device", "Phone");
                        SystemProperties.set("persist.sys.density", SystemProperties.get("ro.sys.phone.density"));
                        break;
                    case "Tablet":
                        SystemProperties.set("persist.sys.device", "Tablet");
                        SystemProperties.set("persist.sys.density", SystemProperties.get("ro.sys.tablet.density"));
                        break;
                    case "LargeTablet":
                        SystemProperties.set("persist.sys.device", "LargeTablet");
                        SystemProperties.set("persist.sys.density", SystemProperties.get("ro.sys.largetablet.density"));
                        break;
                }
                alertDialog = new AlertDialog.Builder(getContext())
                                .setTitle("")
                                .setMessage("Restart device now?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i){
                                        alertDialog.dismiss();
                                        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                                        powerManager.reboot(null);
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i){
                                        alertDialog.dismiss();
                                    }
                                }).show();
            }
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.APPLICATION;
    }
};
