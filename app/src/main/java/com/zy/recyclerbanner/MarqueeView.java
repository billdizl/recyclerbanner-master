package com.zy.recyclerbanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import java.util.concurrent.atomic.AtomicBoolean;

public class MarqueeView extends RecyclerView {
    Thread thread = null;
    AtomicBoolean shouldContinue = new AtomicBoolean(false);
    Handler mHandler;

    public MarqueeView(Context context) {
        super(context);
    }

    public MarqueeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressLint("HandlerLeak")
    private void init() {
        //主线程的handler，用于执行Marquee的滚动消息
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1://不论是竖直滚动还是水平滚动，都是偏移5个像素
                        MarqueeView.this.scrollBy(0, 3);
                        break;
                }
            }
        };
        if (thread == null) {
            thread = new Thread() {
                public void run() {
                    while (shouldContinue.get()) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Message msg = mHandler.obtainMessage();
                        msg.what = 1;
                        msg.sendToTarget();
                    }
                    //退出循环时清理handler
                    //mHandler = null;
                }
            };
        }
    }

    @Override
    /**
     * 在附到窗口的时候开始滚动
     */
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        shouldContinue.set(true);
        init();
        thread.start();
    }

    @Override
    /**
     * 在脱离窗口时处理相关内容
     */
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopMarquee();
    }

    /**
     * 停止滚动
     */
    public void stopMarquee() {
        shouldContinue.set(false);
        thread = null;
    }
}
