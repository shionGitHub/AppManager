package com.manager.app.util;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;

import com.manager.app.R;
import com.manager.app.model.AppInfo;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class UtilsApp {

    //获取自己app的存放地址
    public static File getDefaultAppFolder() {
        return new File(Environment.getExternalStorageDirectory() + "/MLManager");
    }

    //从data/app下复制apk到存储路径中
    public static boolean copyFile(AppInfo appInfo) {
        boolean res = false;

        File initialFile = new File(appInfo.apkSource);
        File finalFile = getOutputFilename(appInfo);

        try {
            FileUtils.copyFile(initialFile, finalFile);
            res = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    //根据apk包名获取apk的图标Drawable
    public static Drawable getAppIconByPkgName(Context context, String packageId) {
        try {
            return context.getPackageManager().getApplicationIcon(packageId);
        } catch (PackageManager.NameNotFoundException e) {
            return ContextCompat.getDrawable(context, R.drawable.ic_android);
        }
    }

    //获取Apk文件的前缀名称
    private static String getAPKFilename(AppInfo appInfo) {
        String res;
        Random r = new Random();
        switch (r.nextInt() % 4) {
            case 1:
                res = appInfo.pkgName + "_" + appInfo.version;
                break;
            case 2:
                res = appInfo.appName + "_" + appInfo.version;
                break;
            case 3:
                res = appInfo.appName;
                break;
            default:
                res = appInfo.pkgName;
                break;
        }

        return res;
    }

    //获取apk的输出文件路径名File
    public static File getOutputFilename(AppInfo appInfo) {
        return new File(getDefaultAppFolder().getPath() + File.separator
                + getAPKFilename(appInfo) + ".pkgName");
    }

    //删除app存储存储路径下的所有文件
    public static boolean deleteAppFiles() {
        boolean res = false;
        File f = getDefaultAppFolder();
        if (f.exists()
                && f.isDirectory()) {
            File[] files = f.listFiles();
            for (File file : files) {
                file.delete();
            }
            if (f.listFiles().length == 0) {
                res = true;
            }
        }
        return res;
    }

    public static void goUrl(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://blog.csdn.net/eyishion/article/details/82787310")));
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://blog.csdn.net/eyishion/article/details/82787310")));
        }
    }

    //获取App的版本名称
    public static String getAppVersionName(Context context) {
        String res = "0.0.0.0";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
            res = info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;

    }

    //获取App的版本Code
    public static int getAppVersionCode(Context context) {
        int res = 0;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
            res = info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    //获取分享的Intent
    public static Intent getShareIntent(Context context, File file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        Uri uri;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(file);
        }
        else {
            String packageName = context.getPackageName();
            uri = FileProvider.getUriForFile(context, packageName + ".provider", file);
        }
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;

    }

    //保存应用的图片到应用本身的缓存路径中
    public static boolean saveIcontoCache(Context context, AppInfo appInfo) {
        boolean res = false;
        try {
            //创建存储路径
            File fileUri = new File(context.getCacheDir(), appInfo.pkgName);
            FileOutputStream out = new FileOutputStream(fileUri);//输出到这里，存储到这里

            PackageManager pm = context.getPackageManager();
            ApplicationInfo applicationInfo = pm.getApplicationInfo(appInfo.pkgName, 0);
            Drawable icon = pm.getApplicationIcon(applicationInfo);//获取Drawable
            Bitmap bitmap = drawableToBitmap(icon);//Drawable---bitmap
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);//压缩
            res = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    //Drawable convert to Bitmap
    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) return null;
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        else {
            int w = drawable.getIntrinsicWidth();
            int h = drawable.getIntrinsicHeight();
            Log.e("1234", "Drawable转Bitmap");
            Bitmap.Config config =
                    (drawable.getOpacity() != PixelFormat.OPAQUE)
                            ? Bitmap.Config.ARGB_8888
                            : Bitmap.Config.RGB_565;

            Bitmap bitmap = Bitmap.createBitmap(w, h, config);
            //注意，下面三行代码要用到，否则在View或者SurfaceView里的canvas.drawBitmap会看不到图
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, w, h);
            drawable.draw(canvas);
            return bitmap;
        }
    }

    //删除缓存下面的图片
    public static boolean removeIconFromCache(Context context, AppInfo appInfo) {
        File file = new File(context.getCacheDir(), appInfo.pkgName);
        return file.delete();
    }

    //从缓存中获取图片Drawable
    public static Drawable getIconFromCache(Context context, AppInfo appInfo) {
        Drawable res;
        try {
            File fileUri = new File(context.getCacheDir(), appInfo.pkgName);
            Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath());
            res = new BitmapDrawable(context.getResources(), bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            res = context.getResources().getDrawable(R.drawable.ic_android);
        }
        return res;
    }

    private static final int MY_PERMISSIONS_REQUEST_WRITE_READ = 1;

    //检查权限 申请
    public static boolean checkPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        boolean res = false;
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]
                            {
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                            },
                    MY_PERMISSIONS_REQUEST_WRITE_READ);
        }
        else {
            res = true;
        }
        return res;
    }

}
