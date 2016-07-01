package com.syber.hypoxia;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.syber.base.BaseFragment;
import com.syber.base.util.Extra;

/**
 * Created by liangtg on 16-7-1.
 */
public class AdviceDetailFragment extends BaseFragment {
    private TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getBaseActivity().getSupportActionBar().setTitle(getArguments().getString(Extra.TITLE));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_advice_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        textView = get(R.id.content);
        textView.setText(getArguments().getString(Extra.CONTENT));
    }
}
