package com.manager.app.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.manager.app.R;
import com.manager.app.async.ExtractFileInBackground;
import com.manager.app.model.AppInfo;
import com.manager.app.util.Utils;
import com.manager.app.util.UtilsApp;


public class AppActivity extends AppCompatActivity {

    private int UNINSTALL_REQUEST_CODE = 1;

    private AppInfo appInfo;
    private Context context;
    private Activity activity;

    public static Intent show(Context context, AppInfo info) {
        Intent i = new Intent(context, AppActivity.class);
        i.putExtra("pkgName", info.pkgName);
        i.putExtra("data", info.data);
        i.putExtra("appName", info.appName);
        i.putExtra("apkSource", info.apkSource);
        i.putExtra("system", info.system);
        i.putExtra("version", info.version);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        this.context = this;
        this.activity = (Activity) context;
        Intent intent = getIntent();
        String name = intent.getStringExtra("appName");
        String apk = intent.getStringExtra("pkgName");
        String version = intent.getStringExtra("version");
        String source = intent.getStringExtra("apkSource");
        String data = intent.getStringExtra("data");
        Drawable drawable = UtilsApp.getAppIconByPkgName(this, apk);
        boolean system = intent.getBooleanExtra("system", false);
        appInfo = new AppInfo(name, apk, version, source, data, drawable, system);
        initToolbar();
        setScreenElements();

    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Utils.darker(getResources().getColor(R.color.primary), 0.8));
            toolbar.setBackgroundColor(getResources().getColor(R.color.primary));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.primary));
        }
    }

    private void setScreenElements() {
        ImageView icon = findViewById(R.id.app_icon);
        TextView name = findViewById(R.id.app_name);
        TextView version = findViewById(R.id.app_version);
        TextView apk = findViewById(R.id.app_apk);
        CardView start = findViewById(R.id.start_card);
        CardView extract = findViewById(R.id.extract_card);
        CardView uninstall = findViewById(R.id.uninstall_card);

        icon.setImageDrawable(appInfo.icon);
        name.setText(appInfo.appName);
        apk.setText(appInfo.pkgName);
        version.setText(appInfo.version);

        if (appInfo.system) {//系统应用
            start.setVisibility(View.GONE);
        }
        else {
            apk.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    ClipData clipData;

                    ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    clipData = ClipData.newPlainText("text", appInfo.pkgName);
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(activity, context.getResources().getString(R.string.copied_clipboard), Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent intent = getPackageManager().getLaunchIntentForPackage(appInfo.pkgName);
                        startActivity(intent);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Toast.makeText(activity, String.format(getResources().getString(R.string.dialog_cannot_open), appInfo.appName), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            uninstall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
                    intent.setData(Uri.parse("package:" + appInfo.pkgName));
                    intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                    startActivityForResult(intent, UNINSTALL_REQUEST_CODE);
                }
            });
        }
        extract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ExtractFileInBackground(context, appInfo).execute();
            }
        });

        if (appInfo.system) {
            uninstall.setVisibility(View.GONE);
            uninstall.setForeground(null);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UNINSTALL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.i("App", "OK");
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                finish();
                startActivity(intent);
            }
            else if (resultCode == RESULT_CANCELED) {
                Log.i("App", "CANCEL");
            }
        }
    }

}
