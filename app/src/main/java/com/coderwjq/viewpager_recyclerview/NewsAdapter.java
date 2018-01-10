package com.coderwjq.viewpager_recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @author: wangjiaqi
 * @data: 2018/1/10
 */

public class NewsAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private String mTitle;

    public NewsAdapter(Context context, String title) {
        mContext = context;
        mTitle = title;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NormalViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_home_page_normal, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof NormalViewHolder) {
            NormalViewHolder holder = (NormalViewHolder) viewHolder;
            holder.mTvContent.setText(mTitle + ": " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 30;
    }

    class NormalViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTvContent;

        public NormalViewHolder(View itemView) {
            super(itemView);
            mTvContent = itemView.findViewById(R.id.tv_content);
        }
    }
}
