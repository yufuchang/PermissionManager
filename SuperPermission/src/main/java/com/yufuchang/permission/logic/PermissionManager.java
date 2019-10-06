package com.yufuchang.permission.logic;

import android.content.Context;

/**
 * Created by yufuchang on 2019/10/1.
 */
public class PermissionManager extends PermissionManagerBase {

    public static Builder with(Context context) {
        return new Builder(context);
    }

    public static class Builder extends PermissionManagerBuilder<Builder> {

        private Builder(Context context) {
            super(context);
        }

        public void check() {
            checkPermissions();
        }

    }
}
