package com.annimon.androidplugindemo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PackageUtil {

    public static boolean isApplicationInstalled(Context context, String packageName) {
        return (getApplicationInfo(context, packageName) != null);
    }

    @Nullable
    public static ApplicationInfo getApplicationInfo(Context context, String packageName) {
        PackageManager pacman = context.getApplicationContext().getPackageManager();
        return getApplicationInfo(pacman, packageName);
    }

    @Nullable
    public static ApplicationInfo getApplicationInfo(PackageManager pacman, String packageName) {
        try {
            return pacman.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Nullable
    public static Resources getResources(PackageManager pacman, ApplicationInfo appInfo) {
        try {
            return pacman.getResourcesForApplication(appInfo);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @NonNull
    public static List<ApplicationInfo> filterApplicationsByIntent(PackageManager pm, Intent intent) {
        final List<ApplicationInfo> result = new ArrayList<>();
        List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : infos) {
            if (resolveInfo.activityInfo != null) {
                result.add(resolveInfo.activityInfo.applicationInfo);
            }
        }
        return result;
    }

    @Nullable
    public static Context getPackageContext(Context context, String packageName) {
        try {
            return context.getApplicationContext().createPackageContext(packageName,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
