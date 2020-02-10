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

import android.view.Window;
import android.graphics.Color;
import android.app.ThemeManager;
import com.android.settings.SettingsActivity;

public class MyEmuSettingsDashboardFragment extends SettingsPreferenceFragment
     implements Preference.OnPreferenceChangeListener, ThemeManager.OnThemeChangedListener{

    private static final String TAG = "MyEmuSettings";  
    private static final String THEME_ACTION = "android.intent.action.THEME_CHANGE"; 
    private static final String THEME_INTENT_KEY = "new_theme";
    private static final String KEY_DENSITY_LIST = "density_setting_list";
    private static final String KEY_THEME_LIST = "theme_setting_list";

    private ListPreference densityListPreference;
    private ListPreference themeListPreference;

    private AlertDialog alertDialog;

    private ThemeManager tm;

    @Override
    public void onCreate(Bundle icicle){
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.myemu_settings);

        densityListPreference = (ListPreference)findPreference(KEY_DENSITY_LIST);
        if(densityListPreference == null){
            Log.e(TAG, "densityListPreference is not found!");
            return;
        }
        themeListPreference = (ListPreference)findPreference(KEY_THEME_LIST);

        String density_setting = SystemProperties.get("persist.sys.device", "none");
        if(!density_setting.equals("none")){
            densityListPreference.setValue(density_setting);
            densityListPreference.setSummary(density_setting);
        }
        densityListPreference.setOnPreferenceChangeListener(this);

        String theme_setting = SystemProperties.get("persist.sys.theme", "Default");
        themeListPreference.setValue(theme_setting);
        themeListPreference.setSummary(theme_setting);
        themeListPreference.setOnPreferenceChangeListener(this);
        
        tm = (ThemeManager)getContext().getSystemService(Context.THEME_SERVICE);
        tm.registerThemeCallback(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (densityListPreference == preference) {
            if(!SystemProperties.get("persist.sys.device", "none").equals((String)newValue)){
                Log.i(TAG, "onPreferenceChange is called. Set to " + (String)newValue);
                SystemProperties.set("persist.sys.device", (String)newValue);
                densityListPreference.setSummary(SystemProperties.get((String)newValue));
                switch ((String)newValue){
                    case "Phone":
                        SystemProperties.set("persist.sys.density", SystemProperties.get("ro.sys.phone.density"));
                        // Window window = this.getActivity().getWindow();
                        // window.setStatusBarColor(Color.rgb(0, 255, 0));
                        break;
                    case "Tablet":
                        SystemProperties.set("persist.sys.density", SystemProperties.get("ro.sys.tablet.density"));
                        break;
                    case "LargeTablet":
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
        }else if(themeListPreference == preference){
            if(!SystemProperties.get("persist.sys.theme").equals((String)newValue)){
                SystemProperties.set("persist.sys.theme", (String)newValue);
                themeListPreference.setSummary((String)newValue);
                Intent intent = new Intent(THEME_ACTION);
                intent.putExtra(THEME_INTENT_KEY, (String)newValue);
                getContext().sendBroadcast(intent);
            }
            return true;

        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.APPLICATION;
    }

    @Override
    public void onThemeChanged(String theme){
        String new_theme = "MySettings" + theme;
        this.getActivity().setTheme(getResources().getIdentifier(new_theme, "style", getContext().getPackageName()));
        Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(getContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.getActivity().startActivity(intent);
    }
};
