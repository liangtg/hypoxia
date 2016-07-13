package com.syber.hypoxia;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;
import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;
import com.syber.hypoxia.data.BloodLipidResponse;

import java.util.ArrayList;

public class BloodLipidActivity extends BaseActivity {
    private ArrayList<BloodLipidResponse> data = new ArrayList<>();
    private ViewHolder viewHolder;
    private Bus bus = new Bus();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_lipid);
        initAppBar();
        viewHolder = new ViewHolder(findViewById(R.id.view_holder));
        startManageBus(bus, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewHolder = null;
    }

    private class ViewHolder extends BaseViewHolder {
        private RecyclerView recyclerView;
        private SwipeRefreshLayout refreshLayout;
        private LipidAdapter adapter;

        public ViewHolder(View view) {
            super(view);
            refreshLayout = get(R.id.refresh);
            recyclerView = get(R.id.recycler);
            adapter = new LipidAdapter();
            recyclerView.setLayoutManager(new LinearLayoutManager(BloodLipidActivity.this, LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(adapter);
        }
    }

    private class LipidAdapter extends RecyclerView.Adapter<AdapterViewHolder> {

        @Override
        public int getItemViewType(int position) {
            return position == data.size() ? 1 : 0;
        }

        @Override
        public AdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 0) {
                return new AdapterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_lipid, parent, false));
            } else {
                return new AdapterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_loading, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(AdapterViewHolder holder, int position) {
            if (holder.getItemViewType() == 0) {
            } else {
                holder.loading.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return data.size() + 1;
        }
    }

    private class AdapterViewHolder extends RecyclerView.ViewHolder {
        private View loading;

        public AdapterViewHolder(View itemView) {
            super(itemView);
            loading = itemView.findViewById(R.id.progress);
        }
    }

}
