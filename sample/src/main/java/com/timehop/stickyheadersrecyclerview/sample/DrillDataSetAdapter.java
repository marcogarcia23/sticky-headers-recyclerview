package com.timehop.stickyheadersrecyclerview.sample;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.util.List;

public class DrillDataSetAdapter extends RecyclerView.Adapter implements StickyRecyclerHeadersAdapter {

    private final List<MainActivity.Drill> drillList;

    public DrillDataSetAdapter(List<MainActivity.Drill> drillList) {
        this.setHasStableIds(true);
        this.drillList = drillList;
    }


    @Override
    public long getHeaderId(int position) {
        return Math.abs(drillList.get(position).sectionId.hashCode());
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_header, parent, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView;
        textView.setText(drillList.get(position).sectionName);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_item, parent, false);

        return new RecyclerView.ViewHolder(rootView) {
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView;
        textView.setText(drillList.get(position).drillName);
    }

    @Override
    public int getItemCount() {
        return drillList.size();
    }

    @Override
    public long getItemId(int position) {
        return drillList.get(position).hashCode();
    }

}
