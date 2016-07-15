package com.syber.hypoxia;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;
import com.syber.base.data.PageDataProvider;
import com.syber.hypoxia.data.BloodLipidResponse;
import com.syber.hypoxia.data.IRequester;
import com.syber.hypoxia.data.User;

import java.util.ArrayList;
import java.util.Date;

public class BloodLipidActivity extends BaseActivity {
    private ArrayList<BloodLipidResponse.DataItem> data = new ArrayList<>();
    private ViewHolder viewHolder;
    private Bus bus = new Bus();
    private DataProvider dataProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_lipid);
        initAppBar();
        viewHolder = new ViewHolder(findViewById(R.id.view_holder));
        dataProvider = new DataProvider();
        startManageBus(bus, dataProvider);
        dataProvider.refresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewHolder = null;
    }

    private class ViewHolder extends BaseViewHolder implements SwipeRefreshLayout.OnRefreshListener {
        private RecyclerView recyclerView;
        private SwipeRefreshLayout refreshLayout;
        private LipidAdapter adapter;

        public ViewHolder(View view) {
            super(view);
            refreshLayout = get(R.id.refresh);
            refreshLayout.setOnRefreshListener(this);
            recyclerView = get(R.id.recycler);
            adapter = new LipidAdapter();
            recyclerView.setLayoutManager(new LinearLayoutManager(BloodLipidActivity.this, LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(adapter);
        }

        @Override
        public void onRefresh() {
            dataProvider.refresh();
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
                holder.update();
            } else {
                if (!viewHolder.refreshLayout.isRefreshing() && dataProvider.haveMoreData()) {
                    holder.loading.setVisibility(View.VISIBLE);
                    dataProvider.nextPage();
                } else {
                    holder.loading.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return data.size() + 1;
        }
    }

    private class AdapterViewHolder extends RecyclerView.ViewHolder {
        private View loading;
        private TextView date, time, type, value, ref, flag;
        private ViewGroup itemContainer;
        private int position;
        private TextView dateTime;

        public AdapterViewHolder(View itemView) {
            super(itemView);
            loading = itemView.findViewById(R.id.progress);
            itemContainer = BaseViewHolder.get(itemView, R.id.item_container);
            dateTime = BaseViewHolder.get(itemView, R.id.date);
        }

        private void update() {
            BloodLipidResponse.DataItem item = data.get(getAdapterPosition());
            position = 0;
            dateTime.setText(item.bftime_test_string);
            updateChol(item);
            updateTg(item);
            updateHdlc(item);
            updateLdlc(item);
            updateUa(item);
            updateGlu(item);
            if (position != itemContainer.getChildCount()) {
                itemContainer.removeViews(position, itemContainer.getChildCount() - position);
            }
        }

        private void updateChol(BloodLipidResponse.DataItem item) {
            View group;
            if (!TextUtils.isEmpty(item.chol)) {
                group = setupGroup();
                BaseViewHolder.<TextView>get(group, R.id.type).setText("总胆固醇(CHOL)");
                BaseViewHolder.<TextView>get(group, R.id.value).setText(item.chol);
                BaseViewHolder.<TextView>get(group, R.id.ref).setText(item.cholref);
                TextView text = (TextView) group.findViewById(R.id.flag);
                if ("↑".equals(item.cholflag)) {
                    text.setText("偏高");
                    text.setBackgroundColor(0xFFCF4C4C);
                    text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lipid_high, 0, 0, 0);
                } else if ("↓".equals(item.cholflag)) {
                    text.setText("偏低");
                    text.setBackgroundColor(0xFF2F76AD);
                    text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lipid_low, 0, 0, 0);
                } else {
                    text.setText("正常");
                    text.setBackgroundColor(0xFF8EC975);
                    text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lipid_normal, 0, 0, 0);
                }
            }
        }

        private void updateTg(BloodLipidResponse.DataItem item) {
            View group;
            if (!TextUtils.isEmpty(item.tg)) {
                group = setupGroup();
                BaseViewHolder.<TextView>get(group, R.id.type).setText("甘油三酯(TG)");
                BaseViewHolder.<TextView>get(group, R.id.value).setText(item.tg);
                BaseViewHolder.<TextView>get(group, R.id.ref).setText(item.tgref);
                TextView text = (TextView) group.findViewById(R.id.flag);
                if ("↑".equals(item.tgflag)) {
                    text.setText("偏高");
                    text.setBackgroundColor(0xFFCF4C4C);
                    text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lipid_high, 0, 0, 0);
                } else if ("↓".equals(item.tgflag)) {
                    text.setText("偏低");
                    text.setBackgroundColor(0xFF2F76AD);
                    text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lipid_low, 0, 0, 0);
                } else {
                    text.setText("正常");
                    text.setBackgroundColor(0xFF8EC975);
                    text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lipid_normal, 0, 0, 0);
                }
            }
        }

        private void updateHdlc(BloodLipidResponse.DataItem item) {
            View group;
            if (!TextUtils.isEmpty(item.hdl)) {
                group = setupGroup();
                BaseViewHolder.<TextView>get(group, R.id.type).setText("高密度脂蛋白(HDLC)");
                BaseViewHolder.<TextView>get(group, R.id.value).setText(item.hdl);
                BaseViewHolder.<TextView>get(group, R.id.ref).setText(item.hdlref);
                TextView text = (TextView) group.findViewById(R.id.flag);
                if ("↑".equals(item.hdlflag)) {
                    text.setText("偏高");
                    text.setBackgroundColor(0xFFCF4C4C);
                    text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lipid_high, 0, 0, 0);
                } else if ("↓".equals(item.hdlflag)) {
                    text.setText("偏低");
                    text.setBackgroundColor(0xFF2F76AD);
                    text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lipid_low, 0, 0, 0);
                } else {
                    text.setText("正常");
                    text.setBackgroundColor(0xFF8EC975);
                    text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lipid_normal, 0, 0, 0);
                }
            }
        }

        private void updateLdlc(BloodLipidResponse.DataItem item) {
            View group;
            if (!TextUtils.isEmpty(item.ldl)) {
                group = setupGroup();
                BaseViewHolder.<TextView>get(group, R.id.type).setText("低密度脂蛋白(LDLC)");
                BaseViewHolder.<TextView>get(group, R.id.value).setText(item.ldl);
                BaseViewHolder.<TextView>get(group, R.id.ref).setText(item.ldlref);
                TextView text = (TextView) group.findViewById(R.id.flag);
                if ("↑".equals(item.ldlflag)) {
                    text.setText("偏高");
                    text.setBackgroundColor(0xFFCF4C4C);
                    text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lipid_high, 0, 0, 0);
                } else if ("↓".equals(item.ldlflag)) {
                    text.setText("偏低");
                    text.setBackgroundColor(0xFF2F76AD);
                    text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lipid_low, 0, 0, 0);
                } else {
                    text.setText("正常");
                    text.setBackgroundColor(0xFF8EC975);
                    text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lipid_normal, 0, 0, 0);
                }
            }
        }

        private void updateUa(BloodLipidResponse.DataItem item) {
            View group;
            if (!TextUtils.isEmpty(item.uricacid)) {
                group = setupGroup();
                BaseViewHolder.<TextView>get(group, R.id.type).setText("血尿酸(UA)");
                BaseViewHolder.<TextView>get(group, R.id.value).setText(item.uricacid);
                BaseViewHolder.<TextView>get(group, R.id.ref).setText(item.uricacidref);
                TextView text = (TextView) group.findViewById(R.id.flag);
                if ("↑".equals(item.uricacidflag)) {
                    text.setText("偏高");
                    text.setBackgroundColor(0xFFCF4C4C);
                    text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lipid_high, 0, 0, 0);
                } else if ("↓".equals(item.uricacidflag)) {
                    text.setText("偏低");
                    text.setBackgroundColor(0xFF2F76AD);
                    text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lipid_low, 0, 0, 0);
                } else {
                    text.setText("正常");
                    text.setBackgroundColor(0xFF8EC975);
                    text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lipid_normal, 0, 0, 0);
                }
            }
        }

        private void updateGlu(BloodLipidResponse.DataItem item) {
            View group;
            if (!TextUtils.isEmpty(item.bloodsugar)) {
                group = setupGroup();
                BaseViewHolder.<TextView>get(group, R.id.type).setText("血糖(GLU)");
                BaseViewHolder.<TextView>get(group, R.id.value).setText(item.bloodsugar);
                BaseViewHolder.<TextView>get(group, R.id.ref).setText(item.bloodsugarref);
                TextView text = (TextView) group.findViewById(R.id.flag);
                if ("↑".equals(item.bloodsugarflag)) {
                    text.setText("偏高");
                    text.setBackgroundColor(0xFFCF4C4C);
                    text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lipid_high, 0, 0, 0);
                } else if ("↓".equals(item.bloodsugarflag)) {
                    text.setText("偏低");
                    text.setBackgroundColor(0xFF2F76AD);
                    text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lipid_low, 0, 0, 0);
                } else {
                    text.setText("正常");
                    text.setBackgroundColor(0xFF8EC975);
                    text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lipid_normal, 0, 0, 0);
                }
            }
        }

        private View setupGroup() {
            View group;
            group = itemContainer.getChildAt(position);
            if (null == group) {
                group = getLayoutInflater().inflate(R.layout.item_item_lipid, itemContainer, false);
                itemContainer.addView(group);
            }
            position++;
            return group;
        }

    }

    private class DataProvider extends PageDataProvider {
        private String time;

        public DataProvider() {
            time = IApplication.dateFormat.format(new Date());
        }

        @Override
        public void doWork(int page) {
            IRequester.getInstance().bloodLipidList(bus, User.getUserInfoExt().user_id, time, page);
        }

        @Override
        public void onResetData() {
            int count = data.size();
            data.clear();
            viewHolder.adapter.notifyItemRangeRemoved(0, count);
        }

        @Subscribe
        public void withData(BloodLipidResponse event) {
            if (isFinishing()) return;
            viewHolder.refreshLayout.setRefreshing(false);
            endPage(event.isSuccess(), !event.isSuccess() || !event.list.isEmpty());
            if (event.isSuccess()) {
                int start = data.size();
                data.addAll(event.list);
                if (event.list.isEmpty()) {
                    viewHolder.adapter.notifyItemChanged(data.size());
                } else if (data.size() == event.list.size()) {
                    viewHolder.adapter.notifyDataSetChanged();
                } else {
                    viewHolder.adapter.notifyItemRangeInserted(start, event.list.size());
                }
            } else {
                viewHolder.adapter.notifyItemChanged(data.size());
            }
        }

    }


}
