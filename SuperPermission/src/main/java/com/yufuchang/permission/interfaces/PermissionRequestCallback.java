package com.yufuchang.permission.interfaces;

import java.util.List;

/**
 * Created by yufuchang on 2018/10/23.
 */
public interface PermissionRequestCallback {
    void onPermissionRequestSuccess(List<String> permissions);

    void onPermissionRequestFail(List<String> permissions);
}
