package com.poyo.barcode;
/*
 * Created by Matthew on 7/21/2016.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.poyo.barcode.Fragment.EmptyFragment;
import com.poyo.barcode.Fragment.HistoryFragment;
import com.poyo.barcode.Fragment.MainFragment;
import com.poyo.barcode.Fragment.ResultFragment;
import com.poyo.barcode.Fragment.SavedFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unchecked")
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static FragmentManager fm;
    private static NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private static FragmentTransaction ft;
    private static int historyItemLimit;
    private int cur_version_num;
    private SharedPreferences prefs;
    private DrawerLayout drawer;
    private static Context context;

    private static ArrayList<Product> savedItemList = new ArrayList<>();
    private static ArrayList<Product> historyItemList = new ArrayList<>();
    private static ArrayList<Retailer> retailers = new ArrayList<>();

    private static int navigationCheck;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_fragment_holder);
        context = this;

        prefs = this.getSharedPreferences("com.poyo.barcode", Context.MODE_PRIVATE);
        historyItemLimit = prefs.getInt(getString(R.string.historyItemLimit), 100);


        retailerSetup();

        try {
            savedItemList = (ArrayList<Product>) InternalStorage.readObject(this.getApplicationContext(), "savedItemList");
            historyItemList = (ArrayList<Product>) InternalStorage.readObject(this.getApplicationContext(), "historyItemList");
            retailers = (ArrayList<Retailer>) InternalStorage.readObject(this, getString(R.string.retailers));

            cur_version_num = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            cur_version_num = 0;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }


        checkForUpdate(prefs.getInt(getString(R.string.historyItemLimit), 0), cur_version_num);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        Fragment frag = fm.findFragmentById(R.id.mainFragmentHolder);
        if (frag == null) {
            frag = new MainFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("historyItemList", historyItemList);
            bundle.putSerializable("retailers", retailers);
            frag.setArguments(bundle);
            fm.beginTransaction().add(R.id.mainFragmentHolder, frag).commit();
            navigationView.getMenu().findItem(R.id.nav_main).setChecked(true);
            navigationCheck = R.id.nav_main;
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.scan, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_barcode) {
            doBarcodeScan();
            return true;
        }
        if (id == R.id.settings_menu) {
            startActivityForResult(new Intent(MainActivity.this, Settings.class), 1);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            startActivityForResult(new Intent(MainActivity.this, Settings.class), 1);
        } else {
            if (id == R.id.nav_main) {
                popBackStack();
            } else if (id == R.id.nav_saved) {
                SavedFragment savedFragment = new SavedFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("savedItemList", savedItemList);
                savedFragment.setArguments(bundle);

                doFragmentTransaction(savedFragment);
            } else if (id == R.id.nav_history) {
                HistoryFragment historyFragment = new HistoryFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("historyItemLimit", historyItemLimit);
                bundle.putSerializable("historyItemList", historyItemList);
                historyFragment.setArguments(bundle);

                doFragmentTransaction(historyFragment);

            }
            navigationCheck = id;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }


    public static void doBarcodeScan() {
        IntentIntegrator integrator = new IntentIntegrator((Activity) context);
        integrator.setOrientationLocked(false);
        integrator.setPrompt("Scan a barcode");
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            //This was a settings page result
            historyItemLimit = prefs.getInt("historyItemLimit", 100);
            setNavigationChecked(navigationCheck);
            //TODO reload retailers here
        } else {
            //This was a Barcode scan result
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            String resultupc = result.getContents() + "";

            if (result.getContents() == null) {
                Toast.makeText(this, "No Barcode Detected", Toast.LENGTH_LONG).show();
                noResults();
            }
            resetToBasic(resultupc);
        }
    }

    public static void resetToBasic(String upc) {
        navigationCheck = -1;
        navigationView.getMenu().findItem(R.id.nav_main).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_saved).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_history).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_settings).setChecked(false);

        ResultFragment resultFragment = new ResultFragment();
        Bundle bundle = new Bundle();
        bundle.putString("productUpc", upc);
        bundle.putSerializable("savedItemList", savedItemList);
        bundle.putSerializable("historyItemList", historyItemList);
        bundle.putSerializable("retailers", retailers);
        resultFragment.setArguments(bundle);

        doFragmentTransaction(resultFragment);
    }

    public static void noResults() {
        EmptyFragment emptyFragment = new EmptyFragment();
        doFragmentTransaction(emptyFragment);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        navigationView = null;
        ft = null;
        fm = null;
        context = null;

        try {
            InternalStorage.writeObject(this, "retailers", retailers);
            InternalStorage.writeObject(this, "savedItemList", savedItemList);
            InternalStorage.writeObject(this, "historyItemList", historyItemList);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void checkForUpdate(int old_version, int new_version) {
        if (new_version > old_version) {
            prefs.edit().putInt(getString(R.string.historyItemLimit), new_version).apply();
            retailerSetup();
        }
    }


    //Call this when the app has been updated, if I support more APIs
    private void retailerSetup() {
        List<Retailer> retailers = new ArrayList<>();
        //String name, String endpoint, String api_key, String return_type
        retailers.add(new Retailer(getString(R.string.walmart), getString(R.string.walmart_endpoint), getString(R.string.walmart_api_key), getString(R.string.json), R.drawable.logo_walmart));
        retailers.add(new Retailer(getString(R.string.bestbuy), getString(R.string.bestbuy_endpoint), getString(R.string.bestbuy_api_key), getString(R.string.json), R.drawable.logo_bestbuy));
        retailers.add(new Retailer(getString(R.string.amazon), getString(R.string.amazon_endpoint), getString(R.string.amazon_api_key), getString(R.string.xml), R.drawable.logo_amazon));
        retailers.add(new Retailer(getString(R.string.ebay), getString(R.string.ebay_endpoint), getString(R.string.ebay_api_key), getString(R.string.xml), R.drawable.logo_ebay));

        //TODO - Get these retailers working in the future
        //retailers.add(new Retailer("Macy's", "https://api.macys.com/v4/", "zgyp5bghk58aykybt3h8jgtd", "json"));

        //Sort by alphabetical order
        Collections.sort(retailers, new RetailNameComparator());

        //Write to local memory
        try {
            InternalStorage.writeObject(this, "retailers", retailers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Orders retailer list alphabetically
    private class RetailNameComparator implements Comparator<Retailer> {
        @Override
        public int compare(Retailer o1, Retailer o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }


    public void onSavedUpdated(ArrayList<Product> saved) {
        savedItemList = saved;
    }

    public void onHistoryUpdated(ArrayList<Product> history) {
        historyItemList = history;
    }

    public void popBackStack() {
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

    }

    private void setNavigationChecked(int nav_id) {
        if (nav_id > 0) {
            navigationView.getMenu().findItem(nav_id).setChecked(true);
        }
    }


    @Override
    public void onBackPressed() {
        setNavigationChecked(R.id.nav_main);
        super.onBackPressed();  // optional depending on your needs
    }

    private static void doFragmentTransaction(Fragment frag) {
        ft = fm.beginTransaction();
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        ft.replace(R.id.mainFragmentHolder, frag);
        ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
    }
}
