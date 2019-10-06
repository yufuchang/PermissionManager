package com.yufuchang.permission.logic;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.WindowManager;

import com.yufuchang.permission.Permissions;
import com.yufuchang.permission.R;
import com.yufuchang.permission.interfaces.PermissionManagerListener;
import com.yufuchang.permission.utils.ObjectUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Created by yufuchang on 2019/10/1.
 */
public class PermissionManagerActivity extends AppCompatActivity {
    public static final int REQ_CODE_PERMISSION_REQUEST = 10;
    public static final int REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST = 30;
    public static final int REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST_SETTING = 31;

    public static final String EXTRA_PERMISSIONS = "permissions";
    public static final String EXTRA_RATIONALE_TITLE = "rationale_title";
    public static final String EXTRA_RATIONALE_MESSAGE = "rationale_message";
    public static final String EXTRA_DENY_TITLE = "deny_title";
    public static final String EXTRA_DENY_MESSAGE = "deny_message";
    public static final String EXTRA_PACKAGE_NAME = "package_name";
    public static final String EXTRA_SETTING_BUTTON = "setting_button";
    public static final String EXTRA_SETTING_BUTTON_TEXT = "setting_button_text";
    public static final String EXTRA_RATIONALE_CONFIRM_TEXT = "rationale_confirm_text";
    public static final String EXTRA_DENIED_DIALOG_CLOSE_TEXT = "denied_dialog_close_text";
    public static final String EXTRA_SCREEN_ORIENTATION = "screen_orientation";
    public static final String EXTRA_BUTTON_NEGATIVE = "#36A5F6";
    public static final String EXTRA_BUTTON_POSITIVE = "#36A5F6";
    private static Deque<PermissionManagerListener> permissionListenerStack;
    private CharSequence rationaleTitle;
    private CharSequence rationale_message;
    private CharSequence denyTitle;
    private CharSequence denyMessage;
    private String[] permissions;
    private String packageName;
    private boolean hasSettingButton;
    private String settingButtonText;
    private String deniedCloseButtonText;
    private String rationaleConfirmText;
    //    private boolean isShownRationaleDialog;
    private int requestedOrientation;

    public static void startActivity(Context context, Intent intent, PermissionManagerListener listener) {
        if (permissionListenerStack == null) {
            permissionListenerStack = new ArrayDeque<>();
        }
        permissionListenerStack.push(listener);
        context.startActivity(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        setupFromSavedInstanceState(savedInstanceState);
        if (needWindowPermission()) {
            requestWindowPermission();
        } else {
            checkPermissions(false);
        }
        setRequestedOrientation(requestedOrientation);
    }

    /**
     * 恢复activity中的数据
     *
     * @param savedInstanceState
     */
    private void setupFromSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            permissions = savedInstanceState.getStringArray(EXTRA_PERMISSIONS);
            rationaleTitle = savedInstanceState.getCharSequence(EXTRA_RATIONALE_TITLE);
            rationale_message = savedInstanceState.getCharSequence(EXTRA_RATIONALE_MESSAGE);
            denyTitle = savedInstanceState.getCharSequence(EXTRA_DENY_TITLE);
            denyMessage = savedInstanceState.getCharSequence(EXTRA_DENY_MESSAGE);
            packageName = savedInstanceState.getString(EXTRA_PACKAGE_NAME);
            hasSettingButton = savedInstanceState.getBoolean(EXTRA_SETTING_BUTTON, true);
            rationaleConfirmText = savedInstanceState.getString(EXTRA_RATIONALE_CONFIRM_TEXT);
            deniedCloseButtonText = savedInstanceState.getString(EXTRA_DENIED_DIALOG_CLOSE_TEXT);
            settingButtonText = savedInstanceState.getString(EXTRA_SETTING_BUTTON_TEXT);
            requestedOrientation = savedInstanceState.getInt(EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            Intent intent = getIntent();
            permissions = intent.getStringArrayExtra(EXTRA_PERMISSIONS);
            rationaleTitle = intent.getCharSequenceExtra(EXTRA_RATIONALE_TITLE);
            rationale_message = intent.getCharSequenceExtra(EXTRA_RATIONALE_MESSAGE);
            denyTitle = intent.getCharSequenceExtra(EXTRA_DENY_TITLE);
            denyMessage = intent.getCharSequenceExtra(EXTRA_DENY_MESSAGE);
            packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
            hasSettingButton = intent.getBooleanExtra(EXTRA_SETTING_BUTTON, true);
            rationaleConfirmText = intent.getStringExtra(EXTRA_RATIONALE_CONFIRM_TEXT);
            deniedCloseButtonText = intent.getStringExtra(EXTRA_DENIED_DIALOG_CLOSE_TEXT);
            settingButtonText = intent.getStringExtra(EXTRA_SETTING_BUTTON_TEXT);
            requestedOrientation = intent.getIntExtra(EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    private boolean needWindowPermission() {
        for (String permission : permissions) {
            if (permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                return !hasWindowPermission();
            }
        }
        return false;
    }

    @TargetApi(VERSION_CODES.M)
    private boolean hasWindowPermission() {
        return Settings.canDrawOverlays(getApplicationContext());
    }

    @TargetApi(VERSION_CODES.M)
    private void requestWindowPermission() {
        Uri uri = Uri.fromParts("package", packageName, null);
        final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
        if (!TextUtils.isEmpty(rationale_message)) {
            new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setMessage(rationale_message)
                    .setCancelable(false)

                    .setNegativeButton(rationaleConfirmText, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivityForResult(intent, REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST);
                        }
                    })
                    .show();
//            isShownRationaleDialog = true;
        } else {
            startActivityForResult(intent, REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST);
        }
    }

    private void checkPermissions(boolean fromOnActivityResult) {
        List<String> needPermissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                if (!hasWindowPermission()) {
                    needPermissionList.add(permission);
                }
            } else {
                if (PermissionManagerBase.isDenied(this, permission)) {
                    needPermissionList.add(permission);
                }
            }
        }

        if (needPermissionList.isEmpty()) {
            permissionResult(null, new ArrayList<String>());
        } else if (fromOnActivityResult) { //From Setting Activity
            permissionResult(needPermissionList, new ArrayList<String>());
        } else if (needPermissionList.size() == 1 && needPermissionList
                .contains(Manifest.permission.SYSTEM_ALERT_WINDOW)) {   // window permission deny
            permissionResult(needPermissionList, new ArrayList<String>());
        } else if (/*!isShownRationaleDialog && */!TextUtils.isEmpty(rationale_message)) { // //Need Show Rationale
            requestPermissions(needPermissionList);
//            isShownRationaleDialog = true;
        } else { //Need Request Permissions
            requestPermissions(needPermissionList);
        }
    }

    private void permissionResult(List<String> deniedPermissionList, List<String> grantPermissionList) {
        finish();
        overridePendingTransition(0, 0);
        if (permissionListenerStack != null) {
            PermissionManagerListener listener = permissionListenerStack.pop();
            if (ObjectUtils.isEmpty(deniedPermissionList)) {
                listener.onPermissionGranted(grantPermissionList);
            } else {
                listener.onPermissionDenied(deniedPermissionList);
            }
            if (permissionListenerStack.size() == 0) {
                permissionListenerStack = null;
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void requestPermissions(List<String> needPermissionList) {
        ActivityCompat.requestPermissions(this, needPermissionList.toArray(new String[needPermissionList.size()]),
                REQ_CODE_PERMISSION_REQUEST);
    }

    /**
     * 回收资源时保存
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArray(EXTRA_PERMISSIONS, permissions);
        outState.putCharSequence(EXTRA_RATIONALE_TITLE, rationaleTitle);
        outState.putCharSequence(EXTRA_RATIONALE_MESSAGE, rationale_message);
        outState.putCharSequence(EXTRA_DENY_TITLE, denyTitle);
        outState.putCharSequence(EXTRA_DENY_MESSAGE, denyMessage);
        outState.putString(EXTRA_PACKAGE_NAME, packageName);
        outState.putBoolean(EXTRA_SETTING_BUTTON, hasSettingButton);
        outState.putString(EXTRA_DENIED_DIALOG_CLOSE_TEXT, deniedCloseButtonText);
        outState.putString(EXTRA_RATIONALE_CONFIRM_TEXT, rationaleConfirmText);
        outState.putString(EXTRA_SETTING_BUTTON_TEXT, settingButtonText);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        List<String> deniedPermissionList = PermissionManagerBase.getDeniedPermissions(this, permissions);
        List<String> grantPermissionList = PermissionManagerBase.getGrantPermissions(this, permissions);
        if (deniedPermissionList.isEmpty()) {
            permissionResult(null, grantPermissionList);
        } else {
            showPermissionDenyDialog(deniedPermissionList, grantPermissionList,"App");
        }
    }

    private void showPermissionDenyDialog(final List<String> deniedPermissionList, final List<String> grantPermissonList,String appName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        List<String> permissionNameList = Permissions.transformText(this, deniedPermissionList);
        builder.setTitle(denyTitle)
                .setMessage(getString(R.string.permission_message_always_failed, appName, TextUtils.join(" ", permissionNameList)))
                .setCancelable(false)
                .setNegativeButton(deniedCloseButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        permissionResult(deniedPermissionList, grantPermissonList);
                    }
                });
        if (hasSettingButton) {
            if (TextUtils.isEmpty(settingButtonText)) {
                settingButtonText = getString(R.string.settings);
            }
            builder.setPositiveButton(settingButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PermissionManagerBase.startSettingActivityForResult(PermissionManagerActivity.this);
                }
            });

        }
        AlertDialog alertDialog = builder.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor(EXTRA_BUTTON_POSITIVE));
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor(EXTRA_BUTTON_NEGATIVE));
    }

    private boolean shouldShowRequestPermissionRationale(List<String> needPermissions) {
        if (needPermissions == null) {
            return false;
        }
        for (String permission : needPermissions) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(PermissionManagerActivity.this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void showWindowPermissionDenyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setMessage(denyMessage)
                .setCancelable(false)
                .setNegativeButton(deniedCloseButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkPermissions(false);
                    }
                });

        if (hasSettingButton) {
            if (TextUtils.isEmpty(settingButtonText)) {
                settingButtonText = getString(R.string.settings);
            }
            builder.setPositiveButton(settingButtonText, new DialogInterface.OnClickListener() {
                @TargetApi(VERSION_CODES.M)
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Uri uri = Uri.fromParts("package", packageName, null);
                    final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
                    startActivityForResult(intent, REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST_SETTING);
                }
            });

        }
        AlertDialog alertDialog = builder.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor(EXTRA_BUTTON_POSITIVE));
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor(EXTRA_BUTTON_NEGATIVE));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PermissionManagerBase.REQ_CODE_REQUEST_SETTING:
                checkPermissions(true);
                break;
            case REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST:   // 최초 ALERT WINDOW 요청에 대한 결과
                if (!hasWindowPermission() && !TextUtils.isEmpty(denyMessage)) {  // 권한이 거부되고 denyMessage 가 있는 경우
                    showWindowPermissionDenyDialog();
                } else {     // 권한있거나 또는 denyMessage가 없는 경우는 일반 permission 을 확인한다.
                    checkPermissions(false);
                }
                break;
            case REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST_SETTING:   //  ALERT WINDOW 권한 설정 실패후 재 요청에 대한 결과
                checkPermissions(false);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }

    }
}
