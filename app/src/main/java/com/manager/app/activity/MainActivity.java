package com.manager.app.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Filter;

import com.manager.app.R;
import com.manager.app.adapter.AppAdapter;
import com.manager.app.model.AppInfo;
import com.manager.app.util.UtilsApp;
import com.manager.app.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private MenuItem searchItem;
    private SearchView searchView;

    private List<AppInfo> appList;
    private List<AppInfo> appSystemList;

    private Context context;
    private AppAdapter appAdapter;
    private AppAdapter appSystemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;
        checkAndAddPermissions(this);
        createAppDir();

        initToolbar();
        initRecyclerView();

        new GetInstalledAppsAsyncTask().execute();
    }

    private void createAppDir() {
        File appDir = UtilsApp.getDefaultAppFolder();
        if (!appDir.exists()) {
            appDir.mkdir();
        }
    }

    private void checkAndAddPermissions(Activity activity) {
        UtilsApp.checkPermissions(activity);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.app_name);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Utils.darker(getResources().getColor(R.color.colorPrimary), 0.8));
            window.setNavigationBarColor(getResources().getColor(R.color.colorPrimary));
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        TabLayout tab = findViewById(R.id.tab);
        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText().equals("第三方的应用")) {
                    recyclerView.setAdapter(appAdapter);
                }
                else {
                    recyclerView.setAdapter(appSystemAdapter);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.appList);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
    }


    class GetInstalledAppsAsyncTask extends AsyncTask<Void, String, Void> {
        GetInstalledAppsAsyncTask() {
            appList = new ArrayList<>();
            appSystemList = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            final PackageManager packageManager = getPackageManager();
            List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);
            Random random = new Random();

            switch (random.nextInt() % 4) {
                case 0:
                    // 按照应用名称来排序
                    Collections.sort(packages, new Comparator<PackageInfo>() {
                        @Override
                        public int compare(PackageInfo p1, PackageInfo p2) {
                            String p1Name = packageManager.getApplicationLabel(p1.applicationInfo).toString().toLowerCase();
                            String p2Name = packageManager.getApplicationLabel(p2.applicationInfo).toString().toLowerCase();
                            return p1Name.compareTo(p2Name);
                        }
                    });
                    break;
                case 1:
                    // 按照apk的大小来排序
                    Collections.sort(packages, new Comparator<PackageInfo>() {
                        @Override
                        public int compare(PackageInfo p1, PackageInfo p2) {
                            Long size1 = new File(p1.applicationInfo.sourceDir).length();
                            Long size2 = new File(p2.applicationInfo.sourceDir).length();
                            return size2.compareTo(size1);
                        }
                    });
                    break;
                case 2:
                    // 按照第一次安装的时间来排序
                    Collections.sort(packages, new Comparator<PackageInfo>() {
                        @Override
                        public int compare(PackageInfo p1, PackageInfo p2) {
                            Long time1 = p1.firstInstallTime;
                            Long time2 = p2.firstInstallTime;
                            return time1.compareTo(time2);
                        }
                    });
                    break;
                case 3:
                    // 按照最后一次更新的时间来排序
                    Collections.sort(packages, new Comparator<PackageInfo>() {
                        @Override
                        public int compare(PackageInfo p1, PackageInfo p2) {
                            Long time1 = p1.lastUpdateTime;
                            Long time2 = p2.lastUpdateTime;
                            return time1.compareTo(time2);
                        }
                    });
                    break;
            }

            // Installed & System Apps
            for (PackageInfo packageInfo : packages) {
                if (!(packageManager.getApplicationLabel(packageInfo.applicationInfo).equals("") || packageInfo.packageName.equals(""))) {

                    if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        try {
                            // 非系统而是第三方App
                            AppInfo tempApp = new AppInfo(
                                    packageManager.getApplicationLabel(packageInfo.applicationInfo).toString(),
                                    packageInfo.packageName,
                                    packageInfo.versionName,
                                    packageInfo.applicationInfo.sourceDir,
                                    packageInfo.applicationInfo.dataDir,
                                    packageManager.getApplicationIcon(packageInfo.applicationInfo),
                                    false);
                            appList.add(tempApp);
                        } catch (OutOfMemoryError e) {
                            AppInfo tempApp = new AppInfo(
                                    packageManager.getApplicationLabel(packageInfo.applicationInfo).toString(),
                                    packageInfo.packageName,
                                    packageInfo.versionName,
                                    packageInfo.applicationInfo.sourceDir,
                                    packageInfo.applicationInfo.dataDir,
                                    getResources().getDrawable(R.drawable.ic_android),
                                    false);
                            appList.add(tempApp);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    else {
                        try {
                            // 系统Apps
                            AppInfo tempApp = new AppInfo(
                                    packageManager.getApplicationLabel(packageInfo.applicationInfo).toString(),
                                    packageInfo.packageName,
                                    packageInfo.versionName,
                                    packageInfo.applicationInfo.sourceDir,
                                    packageInfo.applicationInfo.dataDir,
                                    packageManager.getApplicationIcon(packageInfo.applicationInfo),
                                    true);
                            appSystemList.add(tempApp);
                        } catch (OutOfMemoryError e) {
                            AppInfo tempApp = new AppInfo(
                                    packageManager.getApplicationLabel(packageInfo.applicationInfo).toString(),
                                    packageInfo.packageName,
                                    packageInfo.versionName,
                                    packageInfo.applicationInfo.sourceDir,
                                    packageInfo.applicationInfo.dataDir,
                                    getResources().getDrawable(R.drawable.ic_android),
                                    false);
                            appSystemList.add(tempApp);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            appAdapter = new AppAdapter(appList, context);
            appSystemAdapter = new AppAdapter(appSystemList, context);
            recyclerView.setAdapter(appAdapter);
            searchItem.setVisible(true);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String search) {
        AppAdapter adapter = (AppAdapter) recyclerView.getAdapter();
        if (adapter == null) return false;
        Filter filter = adapter.getFilter();
        if (search.isEmpty()) {
            filter.filter("");
        }
        else {
            filter.filter(search.toLowerCase());
        }
        return false;
    }

}
