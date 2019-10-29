package com.manager.app.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;


import com.manager.app.R;
import com.manager.app.activity.AppActivity;
import com.manager.app.async.ExtractFileInBackground;
import com.manager.app.model.AppInfo;
import com.manager.app.util.UtilsApp;

import java.util.ArrayList;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder>
        implements Filterable {

    private List<AppInfo> appList;
    private List<AppInfo> appListSearch;
    private Context context;

    public AppAdapter(List<AppInfo> appList, Context context) {
        this.appList = appList;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public void clear() {
        appList.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int pos) {
        AppInfo appInfo = appList.get(pos);
        holder.tvName.setText(appInfo.appName);
        holder.tvApk.setText(appInfo.pkgName);
        holder.ivIcon.setImageDrawable(appInfo.icon);
        setButtonEvents(holder, appInfo);
    }

    private void setButtonEvents(AppViewHolder holder, final AppInfo appInfo) {
        final ImageView appIcon = holder.ivIcon;
        final CardView cardView = holder.vCard;

        //提取apk文件
        holder.tvExtract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ExtractFileInBackground(context, appInfo).execute();
            }
        });
        //分享apk文件
        holder.tvShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UtilsApp.copyFile(appInfo);
                Intent shareIntent = UtilsApp.getShareIntent(context, UtilsApp.getOutputFilename(appInfo));
                context.startActivity(Intent.createChooser(
                        shareIntent, String.format(context.getResources().getString(R.string.send_to), appInfo.appName)));
            }
        });
        //查看apk文件
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity activity = (Activity) context;
                Intent intent = AppActivity.show(context, appInfo);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    String transitionName = context.getResources().getString(R.string.transition_app_icon);
                    ActivityOptions transitionActivityOptions =
                            ActivityOptions.makeSceneTransitionAnimation(activity, appIcon, transitionName);
                    context.startActivity(intent, transitionActivityOptions.toBundle());
                }
                else {
                    context.startActivity(intent);
                    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_back);
                }
            }
        });
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                final FilterResults filterResults = new FilterResults();
                final List<AppInfo> resultList = new ArrayList<>();
                if (appListSearch == null) {
                    appListSearch = appList;//在这里给适配器的数据是全部的数据appListSearch
                }
                if (charSequence != null) {
                    if (appListSearch != null && appListSearch.size() > 0) {
                        for (AppInfo appInfo : appListSearch) {
                            if (appInfo.appName.toLowerCase().contains(charSequence.toString())) {
                                resultList.add(appInfo);
                            }
                        }
                    }
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                appList = (List<AppInfo>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.item_app_layout, viewGroup, false);
        return new AppViewHolder(view);
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvApk;
        ImageView ivIcon;
        TextView tvExtract;
        TextView tvShare;
        CardView vCard;

        AppViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.txtName);
            tvApk = v.findViewById(R.id.txtApk);
            ivIcon = v.findViewById(R.id.imgIcon);
            tvExtract = v.findViewById(R.id.btnExtract);
            tvShare = v.findViewById(R.id.btnShare);
            vCard = v.findViewById(R.id.app_card);
        }

    }

}
