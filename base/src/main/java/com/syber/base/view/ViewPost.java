package com.syber.base.view;

import android.os.Build;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by liangtg on 16-2-21.
 */
public class ViewPost {
    public static void postOnAnimation(View v, Runnable runnable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            v.postOnAnimation(runnable);
        } else if (v.getWidth() > 0) {
            v.post(runnable);
        } else {
            v.addOnLayoutChangeListener(new LayoutChangedListener(v, runnable));
        }
    }

    static class LayoutChangedListener implements View.OnLayoutChangeListener {
        Runnable task;
        WeakReference<View> reference;

        public LayoutChangedListener(View v, Runnable runnable) {
            reference = new WeakReference<>(v);
            task = runnable;
        }

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            v.removeOnLayoutChangeListener(this);
            if (reference.get() != null) v.post(task);
            task = null;
        }
    }

}
