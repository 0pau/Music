package com.opau.music;

public class PermissionItem {
    public int title_res;
    public int explain_res;
    public int icon_resource;
    public String permission;
    boolean allowed = false;
    public PermissionItem(int title_res, int explain_res, int icon_resource, String permission) {
        this.title_res = title_res;
        this.explain_res = explain_res;
        this.icon_resource = icon_resource;
        this.permission = permission;
    }
}
