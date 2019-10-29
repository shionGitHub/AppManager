package com.manager.app.util;

import android.graphics.Color;

import java.io.File;

public class Utils {

    public static int darker(int color, double factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(a, Math.max((int) (r * factor), 0), Math.max((int) (g * factor), 0), Math.max((int) (b * factor), 0));
    }

    //获取文件夹的大小
    public static long getFolderSizeInMB(String directory) {
        File f = new File(directory);
        long size = 0;
        if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                size += getFolderSizeInMB(file.getAbsolutePath());
            }
        }
        else {
            size = f.length() / 1024 / 2024;//MB
        }
        return size;
    }

}
