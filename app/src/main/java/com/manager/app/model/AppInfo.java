package com.manager.app.model;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class AppInfo implements Serializable {
    // {appName='QQ浏览器',
    // pkgName='com.tencent.mtt',
    // version='9.5.0.5050',
    // apkSource='/data/app/com.tencent.mtt-1/base.apk',
    // data='/data/data/com.tencent.mtt',
    // icon=android.graphics.drawable.BitmapDrawable@71c8ff,
    // system=false}
    public String appName;
    public String pkgName;
    public String version;
    public String apkSource;//apk的存放地址 应用内的路径
    public String data;//data目录的路径，里面含有数据库和shareprent
    public Drawable icon;
    public boolean system;

    public AppInfo(String appName, String pkgName, String version, String apkSource, String data, Drawable icon, boolean isSystem) {
        this.appName = appName;
        this.pkgName = pkgName;
        this.version = version;
        this.apkSource = apkSource;
        this.data = data;
        this.icon = icon;
        this.system = isSystem;
    }

    public AppInfo(String string) {
        String[] split = string.split("##");
        if (split.length == 6) {
            this.appName = split[0];
            this.pkgName = split[1];
            this.version = split[2];
            this.apkSource = split[3];
            this.data = split[4];
            this.system = Boolean.getBoolean(split[5]);
        }
    }


    @Override
    public String toString() {
        return "AppInfo{" +
                "appName='" + appName + '\'' +
                ", pkgName='" + pkgName + '\'' +
                ", version='" + version + '\'' +
                ", apkSource='" + apkSource + '\'' +
                ", data='" + data + '\'' +
                ", icon=" + icon +
                ", system=" + system +
                '}';
    }

}
