package com.syber.base;

import android.content.Context;
import android.view.View;

/**
 * Created by liangtg on 15-8-11.
 */
public class BaseViewHolder implements View.OnClickListener {
    private final View holder;

    public BaseViewHolder(View view) {
        holder = view;
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T get(int id) {
        return (T) holder.findViewById(id);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T get(View view, int id) {
        return (T) view.findViewById(id);
    }

    public View getContainer() {
        return holder;
    }

    public Context getContext() {
        if (null != holder) {
            return holder.getContext();
        }
        return null;
    }

    @Override
    public void onClick(View v) {
    }
}
