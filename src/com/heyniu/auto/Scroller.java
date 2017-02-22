package com.heyniu.auto;

import android.app.Instrumentation;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.robotium.solo.Sleeper;
import com.robotium.solo.ViewFetcher;

class Scroller extends com.robotium.solo.Scroller{

    /**
     * Constructs this object.
     *
     * @param config
     * @param inst        the {@code Instrumentation} instance
     * @param viewFetcher the {@code ViewFetcher} instance
     * @param sleeper     the {@code Sleeper} instance
     */
    Scroller(Solo.Config config, Instrumentation inst, ViewFetcher viewFetcher, Sleeper sleeper) {
        super(config, inst, viewFetcher, sleeper);
    }

    /**
     * Scroll the list to a given line
     *
     * @param view the {@link RecyclerView} to scroll
     * @param line the line to scroll to
     */

    <T extends RecyclerView> void scrollRecyclerViewToLine(final T view, final int line){
        if(view == null) Log.e(Solo.LOG_TAG, "RecyclerView is null!");

        if (view != null) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    view.scrollToPosition(line);
                }
            });
        }
        sleeper.sleep();
    }


}
