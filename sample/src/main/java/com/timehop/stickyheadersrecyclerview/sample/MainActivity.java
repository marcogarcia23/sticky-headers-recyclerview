package com.timehop.stickyheadersrecyclerview.sample;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        final DrillDataSetAdapter adapter = new DrillDataSetAdapter(getDataSet());
        recyclerView.setAdapter(adapter);

        // Set layout manager
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // Add the sticky headers decoration
        final StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(adapter);
        recyclerView.addItemDecoration(headersDecor);

        // Add decoration for dividers between list items
        recyclerView.addItemDecoration(new DividerDecoration(this));

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                headersDecor.invalidateHeaders();
            }
        });

    }

    private List<Drill> getDataSet() {
        final List<Drill> data = new ArrayList<>();
        for (int i = 0; i < 100; ++i) {
            data.add(new Drill(i + " Drill : " + i, "Section: " + i / 10));
        }

        return data;
    }

    private int getLayoutManagerOrientation(int activityOrientation) {
        if (activityOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            return LinearLayoutManager.VERTICAL;
        } else {
            return LinearLayoutManager.HORIZONTAL;
        }
    }

    public static class Drill {
        public String drillName;
        public String sectionName;
        public String sectionId;

        public Drill(String drillName, String sectionName) {
            this.drillName = drillName;
            this.sectionName = sectionName;
            this.sectionId = sectionName;
        }
    }

}
