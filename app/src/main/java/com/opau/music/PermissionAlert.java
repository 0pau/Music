package com.opau.music;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class PermissionAlert extends AppCompatActivity {

    int currentItem = -1;
    PermissionPagerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_alert);
        ViewPager2 vp = findViewById(R.id.viewPager);
        adapter = new PermissionPagerAdapter();
        vp.setAdapter(adapter);
        goToNextPermission();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void startRequest(View v) {

        if (shouldShowRequestPermissionRationale(Permissions.getNeededPermissions().get(currentItem).permission)) {
            requestPermissions(new String[]{Permissions.getNeededPermissions().get(currentItem).permission}, 10);
        } else {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    class PermissionPagerAdapter extends RecyclerView.Adapter<PermissionPagerAdapter.PermissionViewHolder> {

        @NonNull
        @Override
        public PermissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.permission_page, parent, false);
            return new PermissionViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PermissionViewHolder holder, int position) {
            if (currentItem != -1) {
                View v = holder.itemView;
                ImageView icon = v.findViewById(R.id.permissionIllustration);
                TextView title = v.findViewById(R.id.permissionTitleTextView);
                TextView explainer = v.findViewById(R.id.permissionExplainTextView);
                icon.setImageResource(Permissions.getNeededPermissions().get(currentItem).icon_resource);
                title.setText(getString(Permissions.getNeededPermissions().get(currentItem).title_res));
                explainer.setText(getString(Permissions.getNeededPermissions().get(currentItem).explain_res));
            }
        }

        @Override
        public int getItemCount() {
            return 1;
        }

        public class PermissionViewHolder extends RecyclerView.ViewHolder {

            public PermissionViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    void goToNextPermission() {
        currentItem++;
        //Log.i("INDEX", currentItem+"");
        if (currentItem == Permissions.getNeededPermissions().size()) {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        } else {
            if (checkSelfPermission(Permissions.getNeededPermissions().get(currentItem).permission) == PackageManager.PERMISSION_GRANTED) {
                goToNextPermission();
            } else {
                adapter.notifyItemChanged(0);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkSelfPermission(Permissions.getNeededPermissions().get(currentItem).permission) == PackageManager.PERMISSION_GRANTED) {
            Permissions.getNeededPermissions().get(currentItem).allowed = true;
            goToNextPermission();
        }
    }
}