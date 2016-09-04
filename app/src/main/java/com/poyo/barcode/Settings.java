package com.poyo.barcode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
public class Settings extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();
    }


    public static class PrefsFragment extends PreferenceFragment {
        private SharedPreferences prefs;
        private MultiSelectListPreference select;
        private List<Retailer> retailers = new ArrayList<>();
        private ListPreference history;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings);
            PreferenceManager.setDefaultValues(getActivity(), R.xml.settings, false);
            select = (MultiSelectListPreference) findPreference("select");
            history = (ListPreference) findPreference("history");


            Intent returnIntent = new Intent();
            returnIntent.putExtra("itemlistRefresh", false);
            getActivity().setResult(Activity.RESULT_OK, returnIntent);


            prefs = getActivity().getSharedPreferences("com.poyo.barcode",
                    Context.MODE_PRIVATE);

            try {
                retailers = (List<Retailer>) InternalStorage.readObject(getActivity(), "retailers");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }


            updateSelect();


            select.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    updateShownRetailers((Set<String>) newValue);
                    updateSelect();
                    return false;
                }
            });


            history.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int index = Integer.valueOf((String) newValue);
                    prefs.edit().putInt("historyItemLimit", index).apply();
                    history.setValueIndex(getHistoryIndex());
                    return false;
                }
            });
        }

        private int getHistoryIndex() {
            switch (prefs.getInt("historyItemLimit", 100)) {
                case 10:
                    return 1;
                case 25:
                    return 2;
                case 100:
                    return 3;
                case 1000000:
                    return 4;
                default:
                    return 0;
            }
        }

        void updateShownRetailers(Set<String> rets) {
            for (int i = 0; i < retailers.size(); i++) {
                retailers.get(i).setShowRetailer(false);
                for (String setElement : rets) {
                    if (retailers.get(i).getName().equals(setElement)) {
                        retailers.get(i).setShowRetailer(true);
                    }
                }

            }
            try {
                Collections.sort(retailers, new RetailNameComparator());
                InternalStorage.writeObject(getActivity(), "retailers", retailers);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        void updateSelect() {
            List<String> retailerNames = new ArrayList<>();
            Set<String> validRetailers = new HashSet<>();
            for (Retailer ret : retailers) {
                retailerNames.add(ret.getName());
                if (ret.getShowRetailer()) {
                    validRetailers.add(ret.getName());
                }
            }

            select.setEntries(retailerNames.toArray(new CharSequence[retailerNames.size()]));
            select.setEntryValues(retailerNames.toArray(new CharSequence[retailerNames.size()]));
            select.setValues(validRetailers);
        }

        class RetailNameComparator implements Comparator<Retailer> {
            @Override
            public int compare(Retailer o1, Retailer o2) {
                return o1.getName().compareTo(o2.getName());
            }
        }
    }


}