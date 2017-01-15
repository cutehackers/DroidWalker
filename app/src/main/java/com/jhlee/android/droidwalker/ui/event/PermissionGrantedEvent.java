package com.jhlee.android.droidwalker.ui.event;

/**
 * DroidWalker
 * <p>
 * date 2017-01-15
 * author Jun-hyoung, Lee
 */

public class PermissionGrantedEvent {

    private String permissions;

    public PermissionGrantedEvent(String permissions) {
        this.permissions = permissions;
    }

    public String getPermissions() {
        return permissions;
    }

}
