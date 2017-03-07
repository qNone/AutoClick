package com.heyniu.auto;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

class InitAnimation {

    private final Context context;
    private final android.os.Handler handler;
    private final Activity proxyActivity;

    private RelativeLayout relativeLayout;
    private TextView textView;
    private ScaleAnimation scaleAnimation;

    private final int UPDATE_UI = 1000;
    private final int ADD_VIEW = 1001;
    private final int IGNORE_VIEW = 1002;
    private String message = "";
    private int countDown = 3;

    InitAnimation(Instrumentation instrumentation, Activity proxyActivity) {
        this.context = instrumentation.getTargetContext();
        this.proxyActivity = proxyActivity;
        this.handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                switch (msg.what) {
                    case UPDATE_UI:
                        textView.setText(message);
                        break;
                    case ADD_VIEW:
                        addView();
                        break;
                    case IGNORE_VIEW:
                        textView.clearAnimation();
                        relativeLayout.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }
        };
        initAnimation();
        handler.sendEmptyMessage(ADD_VIEW);
    }

    void init() {
        while (countDown > 0) {
            message = String.valueOf(countDown);
            handler.sendEmptyMessage(UPDATE_UI);
            SystemClock.sleep(1000);
            countDown --;
        }
        handler.sendEmptyMessage(IGNORE_VIEW);
        SystemClock.sleep(500);
    }

    private void initAnimation() {
        scaleAnimation = new ScaleAnimation(1.0f, 2.0f, 1.0f, 2.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(1000);
        scaleAnimation.setFillAfter(false);
        scaleAnimation.setRepeatCount(countDown);
    }

    private void addView() {
        Window window = proxyActivity.getWindow();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        relativeLayout = new RelativeLayout(context);
        relativeLayout.setBackgroundColor(Color.argb(150, 50, 50, 50));
        textView = new TextView(context);
        textView.setBackgroundColor(Color.TRANSPARENT);
        textView.setTextColor(Color.rgb(255, 255, 255));
        textView.setTextSize(100);
        textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        textView.setLayoutParams(params);
        textView.startAnimation(scaleAnimation);
        relativeLayout.addView(textView);
        window.addContentView(relativeLayout, params);
    }

}
