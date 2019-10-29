package com.manager.app.async;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.manager.app.R;
import com.manager.app.model.AppInfo;
import com.manager.app.util.UtilsApp;

public class ExtractFileInBackground extends AsyncTask<Void, String, Boolean> {

    private Activity activity;
    private AppInfo appInfo;

    public ExtractFileInBackground(Context context, AppInfo appInfo) {
        this.activity = (Activity) context;
        this.appInfo = appInfo;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        boolean status = false;
        if (UtilsApp.checkPermissions(activity)) {
            status = UtilsApp.copyFile(appInfo);
        }
        return status;
    }

    @Override
    protected void onPostExecute(Boolean status) {
        if (status) {
            Toast.makeText(activity,
                    String.format(activity.getResources().getString(R.string.dialog_saved_description), appInfo.appName),
                    Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(activity,
                    activity.getResources().getString(R.string.dialog_extract_fail_description)
                    , Toast.LENGTH_LONG).show();
        }

    }

}