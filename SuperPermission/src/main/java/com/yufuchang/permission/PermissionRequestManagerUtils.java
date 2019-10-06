package com.yufuchang.permission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.yufuchang.permission.interfaces.PermissionManagerListener;
import com.yufuchang.permission.interfaces.PermissionRequestCallback;
import com.yufuchang.permission.logic.PermissionManager;
import com.yufuchang.permission.logic.PermissionManagerBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yufuchang on 2019/10/1.
 */
public class PermissionRequestManagerUtils {

    private static PermissionRequestManagerUtils permissionManagerUtils;
    private PermissionRequestCallback callback;
    private Context context;

    private PermissionRequestManagerUtils() {
    }

    public static PermissionRequestManagerUtils getInstance() {
        if (permissionManagerUtils == null) {
            synchronized (PermissionRequestManagerUtils.class) {
                if (permissionManagerUtils == null) {
                    permissionManagerUtils = new PermissionRequestManagerUtils();
                }
            }
        }
        return permissionManagerUtils;
    }

    /**
     * 请求单个权限
     *
     * @param context
     */
    public void requestRuntimePermission(Context context, String permission, PermissionRequestCallback callback) {
        requestRuntimePermission(context, new String[]{permission}, callback);
    }

    /**
     * 请求一组权限
     *
     * @param context
     */
    public void requestRuntimePermission(final Context context, String[] permissionGroup, final PermissionRequestCallback callback) {
        if (callback == null) {
            return;
        }
        this.callback = callback;
        this.context = context;
        if (permissionGroup == null || permissionGroup.length == 0 || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            callback.onPermissionRequestSuccess(new ArrayList<String>());
            return;
        }
        if (PermissionRequestManagerUtils.getInstance().isHasPermission(context, permissionGroup)) {
            callback.onPermissionRequestSuccess(Arrays.asList(permissionGroup));
        } else {
            PermissionManagerListener permissionlistener = new PermissionManagerListener() {
                @Override
                public void onPermissionGranted(List<String> grantPermissions) {
                    if (callback != null) {
                        callback.onPermissionRequestSuccess(grantPermissions);
                    }
                }

                @Override
                public void onPermissionDenied(List<String> deniedPermissions) {
                    exitByPermission(deniedPermissions);
                }
            };
            PermissionManager.with(context)
                    .setPermissionListener(permissionlistener)
                    .setGotoSettingButtonText(R.string.ok)
                    .setPermissions(permissionGroup)
                    .check();
        }
    }

    private void exitByPermission(List<String> permissionList) {
        if (!(PermissionRequestManagerUtils.getInstance().isHasPermission(context, Permissions.STORAGE)
                && PermissionRequestManagerUtils.getInstance().isHasPermission(context, Permissions.READ_PHONE_STATE))) {
            if (callback != null) {
                callback.onPermissionRequestFail(permissionList);
            }

        } else {
            if (isHasPermission(context, stringList2StringArray(permissionList))) {
                if (callback != null) {
                    callback.onPermissionRequestSuccess(permissionList);
                }
            } else {
                if (callback != null) {
                    callback.onPermissionRequestFail(permissionList);
                }
            }
        }
    }

    /**
     * 检测单个权限
     *
     * @param context
     * @param permission
     * @return
     */
    public boolean isHasPermission(Context context, String permission) {
        return PermissionManagerBase.isGranted(context, permission);
    }

    /**
     * 检查一组权限
     *
     * @param context
     * @param permissions
     * @return
     */
    public boolean isHasPermission(Context context, String[] permissions) {
        return PermissionManagerBase.isGranted(context, permissions);
    }

    private String[] stringList2StringArray(List<String> permissionList) {
        String[] strings = new String[permissionList.size()];
        return permissionList.toArray(strings);
    }

    @SuppressLint("StringFormatInvalid")
    public String getPermissionToast(Context context, List<String> permissionList,String appName) {
        List<String> permissionNameList = Permissions.transformText(context, permissionList);
        return context.getString(R.string.permission_grant_fail, appName, TextUtils.join(" ", permissionNameList));
    }
}
