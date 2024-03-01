package com.opau.music;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.media.MediaRouter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

public class MediaRouterDialog extends BottomSheetDialog {
    public MediaRouterDialog(@NonNull Context context) {
        super(context);
    }
    ArrayList<MediaRouter.RouteInfo> routes = new ArrayList<>();
    MediaRouter mr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_router_layout);
        RecyclerView rv = findViewById(R.id.mediaRouterRecycler);
        mr = (MediaRouter)getContext().getSystemService(Context.MEDIA_ROUTER_SERVICE);
        for (int i = 0; i < mr.getRouteCount(); i++) {
            routes.add(mr.getRouteAt(i));
        }
        MediaRouteListAdapter adapter = new MediaRouteListAdapter(getLayoutInflater());
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rv.setAdapter(adapter);
    }

    class MediaRouteListAdapter extends RecyclerView.Adapter<MediaRouteListAdapter.MediaRouteViewHolder> {

        LayoutInflater inf;
        public MediaRouteListAdapter(LayoutInflater i) {
            inf = i;
        }

        @NonNull
        @Override
        public MediaRouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = inf.inflate(R.layout.media_route_item, parent, false);
            return new MediaRouteViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MediaRouteViewHolder holder, int position) {
            ImageView mediaRouteIcon = holder.itemView.findViewById(R.id.mediaRouteIcon);
            TextView mediaRouteName = holder.itemView.findViewById(R.id.mediaRouteName);
            mediaRouteName.setText(routes.get(position).getName());
            mediaRouteIcon.setImageResource(getDrawableForDeviceType(routes.get(position).getDeviceType()));
            holder.itemView.setTag(routes.get(position));
            holder.itemView.setOnClickListener((v)->{
                MediaRouter.RouteInfo ri = (MediaRouter.RouteInfo) holder.itemView.getTag();
                mr.selectRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO,ri);
                dismiss();
            });
        }

        @Override
        public int getItemCount() {
            return routes.size();
        }

        public class MediaRouteViewHolder extends RecyclerView.ViewHolder {

            public MediaRouteViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }

    public int getDrawableForDeviceType(int deviceType) {

        switch (deviceType) {
            case MediaRouter.RouteInfo.DEVICE_TYPE_SPEAKER:
                return R.drawable.speaker;
            case MediaRouter.RouteInfo.DEVICE_TYPE_BLUETOOTH:
                return R.drawable.bluetooth;
            case MediaRouter.RouteInfo.DEVICE_TYPE_TV:
                return R.drawable.tv;
        }

        return R.drawable.cast;
    }

}
