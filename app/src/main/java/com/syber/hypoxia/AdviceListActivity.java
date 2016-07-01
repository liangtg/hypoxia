package com.syber.hypoxia;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.syber.base.BaseActivity;
import com.syber.base.BaseViewHolder;
import com.syber.base.util.Extra;
import com.syber.base.view.ViewPost;
import com.syber.hypoxia.data.AdviceResponse;
import com.syber.hypoxia.data.IRequester;
import com.syber.hypoxia.data.User;

import java.util.ArrayList;
import java.util.Date;

public class AdviceListActivity extends BaseActivity {
    private ViewHolder viewHolder;
    private DataProvicer dataProvicer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advice_list);
        initAppBar();
        dataProvicer = new DataProvicer();
        viewHolder = new ViewHolder(findViewById(R.id.view_holder));
        ViewPost.postOnAnimation(viewHolder.recyclerView, new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) return;
                dataProvicer.refresh();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportActionBar().setTitle(getTitle());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewHolder = null;
    }

    private class ViewHolder extends BaseViewHolder {
        private final AdviceAdapter adapter;
        int spaceDouble;
        private RecyclerView recyclerView;

        public ViewHolder(View view) {
            super(view);
            spaceDouble = getResources().getDimensionPixelSize(R.dimen.spacing_double);
            recyclerView = get(R.id.advice_list);
            LinearLayoutManager layoutManager = new LinearLayoutManager(AdviceListActivity.this, LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    super.getItemOffsets(outRect, view, parent, state);
                    int positon = parent.getChildAdapterPosition(view);
                    if (positon == 0) {
                        outRect.set(0, spaceDouble, 0, 0);
                    } else if (positon == parent.getAdapter().getItemCount() - 1) {
                        outRect.set(0, 0, 0, spaceDouble);
                    }
                }
            });
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            adapter = new AdviceAdapter();
            recyclerView.setAdapter(adapter);
        }
    }

    private class AdviceAdapter extends RecyclerView.Adapter<AdapterViewHolder> {

        @Override
        public AdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AdapterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_advice, parent, false));
        }

        @Override
        public void onBindViewHolder(AdapterViewHolder holder, int position) {
            AdviceResponse.DataBean item = dataProvicer.data.get(position);
            holder.title.setText(item.content);
            holder.date.setText(IApplication.dateFormat.format(new Date(item.time_create)));
            holder.state.setVisibility(item.readed ? View.INVISIBLE : View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            return dataProvicer.data.size();
        }
    }

    private class AdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title, date, state;

        public AdapterViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = ViewHolder.get(itemView, R.id.title);
            date = ViewHolder.get(itemView, R.id.date);
            state = ViewHolder.get(itemView, R.id.state);
        }

        @Override
        public void onClick(View v) {
            AdviceResponse.DataBean bean = dataProvicer.data.get(getAdapterPosition());
            if (!bean.readed) {
                bean.readed = true;
                IRequester.getInstance().adviceReaded(dataProvicer.bus, bean.id);
                viewHolder.adapter.notifyItemChanged(getAdapterPosition());
            }
            AdviceDetailFragment fragment = new AdviceDetailFragment();
            Bundle args = new Bundle();
            args.putString(Extra.TITLE, IApplication.dateFormat.format(bean.time_create));
            args.putString(Extra.CONTENT, bean.content);
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                    fragment,
                    "advice_detail").addToBackStack("advice_detail").commit();
        }
    }

    private class DataProvicer {
        public ArrayList<AdviceResponse.DataBean> data = new ArrayList<>();

        private String startTime;
        private Bus bus = new Bus();
        private int page = 1;
        private String uid = User.getUserInfoExt().user_id;
        private boolean working = false;
        private boolean haveMore = true;

        public DataProvicer() {
            startManageBus(bus, this);
        }

        public boolean refresh() {
            if (working) return false;
            startTime = IApplication.dateFormat.format(new Date());
            page = 1;
            haveMore = true;
            nextPage();
            return true;
        }

        public void nextPage() {
            if (working || !haveMore) return;
            working = true;
            IRequester.getInstance().adviceList(bus, uid, startTime, page);
        }

        @Subscribe
        public void withData(AdviceResponse event) {
            if (isFinishing()) return;
            working = false;
            if (event.isSuccess()) {
                page++;
                haveMore = event.data.size() == 20;
                data.addAll(event.data);
                viewHolder.adapter.notifyItemRangeInserted(data.size() - event.data.size(), event.data.size());
            } else {
            }
            nextPage();
        }

    }

}
