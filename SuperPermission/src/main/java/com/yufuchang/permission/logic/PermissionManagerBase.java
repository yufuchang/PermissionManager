package com.yufuchang.permission.logic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2019/10/1.
 */
public abstract class PermissionManagerBase {
    public static final int REQ_CODE_REQUEST_SETTING = 2000;
    private static final String PREFS_NAME_PERMISSION = "PREFS_NAME_PERMISSION";
    private static final String PREFS_IS_FIRST_REQUEST = "IS_FIRST_REQUEST";

    /**
     * 判断一组权限是否授予，传入string数组
     *
     * @param context
     * @param permissions
     * @return
     */
    public static boolean isGranted(Context context, @NonNull String... permissions) {
        for (String permission : permissions) {
            if (isDenied(context, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断权限一个权限是否被拒绝
     *
     * @param context
     * @param permission
     * @return
     */
    public static boolean isDenied(Context context, @NonNull String permission) {
        return !isGranted(context, permission);
    }

    /**
     * 判断一个权限是否被授予
     *
     * @param context
     * @param permission
     * @return
     */
    private static boolean isGranted(Context context, @NonNull String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 获取传入权限中被拒绝的权限
     *
     * @param context
     * @param permissions
     * @return
     */
    public static List<String> getDeniedPermissions(Context context, @NonNull String... permissions) {
        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (isDenied(context, permission)) {
                deniedPermissions.add(permission);
            }
        }
        return deniedPermissions;
    }

    /**
     * 获取传入权限中被授予权限的列表
     *
     * @param context
     * @param permissions
     * @return
     */
    public static List<String> getGrantPermissions(Context context, @NonNull String... permissions) {
        List<String> grantPermissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (!isDenied(context, permission)) {
                grantPermissionList.add(permission);
            }
        }
        return grantPermissionList;
    }

    /**
     * 判断是否永久拒绝不再提醒
     *
     * @param activity
     * @param permissions
     * @return
     */
    public static boolean canRequestPermission(Activity activity, @NonNull String... permissions) {
        if (isFirstRequest(activity, permissions)) {
            return true;
        }
        for (String permission : permissions) {
            boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
            if (isDenied(activity, permission) && !showRationale) {
                return false;
            }
        }
        return true;
    }

    private static boolean isFirstRequest(Context context, @NonNull String[] permissions) {
        for (String permission : permissions) {
            if (!isFirstRequest(context, permission)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isFirstRequest(Context context, String permission) {
        return getSharedPreferences(context).getBoolean(getPrefsNamePermission(permission), true);
    }

    private static String getPrefsNamePermission(String permission) {
        return PREFS_IS_FIRST_REQUEST + "_" + permission;
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME_PERMISSION, Context.MODE_PRIVATE);
    }

    public static void startSettingActivityForResult(Activity activity) {
        startSettingActivityForResult(activity, REQ_CODE_REQUEST_SETTING);
    }

    public static void startSettingActivityForResult(Activity activity, int requestCode) {
        activity.startActivityForResult(getSettingIntent(activity), requestCode);
    }

    public static Intent getSettingIntent(Context context) {
        return new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + context.getPackageName()));
    }

    public static void startSettingActivityForResult(Fragment fragment) {
        startSettingActivityForResult(fragment, REQ_CODE_REQUEST_SETTING);
    }

    public static void startSettingActivityForResult(Fragment fragment, int requestCode) {
        fragment.startActivityForResult(getSettingIntent(fragment.getActivity()), requestCode);
    }

    static void setFirstRequest(Context context, @NonNull String[] permissions) {
        for (String permission : permissions) {
            setFirstRequest(context, permission);
        }
    }

    private static void setFirstRequest(Context context, String permission) {
        getSharedPreferences(context).edit().putBoolean(getPrefsNamePermission(permission), false).apply();
    }
}
