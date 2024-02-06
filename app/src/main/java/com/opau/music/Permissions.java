package com.opau.music;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;

public class Permissions {

    static ArrayList<PermissionItem> neededPermissions = new ArrayList<>(Arrays.asList(
            new PermissionItem(R.string.permission_audio_file_title, R.string.permission_audio_file_explain, R.drawable.audio_file, android.Manifest.permission.READ_MEDIA_AUDIO),
            new PermissionItem(R.string.permission_notifications_title, R.string.permission_notifications_explain, R.drawable.notifications, Manifest.permission.POST_NOTIFICATIONS)));
    public static boolean checkPermissions(Context c) {

        for (PermissionItem pi: neededPermissions) {
            if (ContextCompat.checkSelfPermission(c, pi.permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    public static ArrayList<PermissionItem> getNeededPermissions() {
        return neededPermissions;
    }
}
