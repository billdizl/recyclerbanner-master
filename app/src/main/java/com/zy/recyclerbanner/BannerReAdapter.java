package com.zy.recyclerbanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zy.base.AutoPollRecyclerView;
import com.zy.base.Javabean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class BannerReAdapter extends BaseMultiItemQuickAdapter<Javabean, BaseViewHolder> {
    private Context context;
    private MarqueeView rc_view;
    private AtomicBoolean shouldContinue = new AtomicBoolean(false);
    private Thread thread = null;
    private static final int RECYCLERVIEWRollROLL = 1;
    private Handler mRecyclerViewHandler;
    private boolean first=true;

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data A new list is created out of this one to avoid mutable list
     */
    public BannerReAdapter(Context context, List<Javabean> data) {
        super(data);
        this.context = context;
        addItemType(0, R.layout.view_multiltem_def_item);
        addItemType(1, R.layout.view_list_item);
        addItemType(2, R.layout.recyview_item);
    }

    @Override
    public int getItemCount() {

        return Integer.MAX_VALUE;
    }

    //重写getItem以免出现无限滑动时当position大于data的size时获得对象为空
    @Nullable
    @Override
    public Javabean getItem(int position) {
        int newPosition = position % getData().size();
        return getData().get(newPosition);
    }

    //重写此方法，因为BaseQuickAdapter里绘制view时会调用此方法判断，position减去getHeaderLayoutCount小于data.size()时才会调用调用cover方法绘制我们自定义的view
    @Override
    public int getItemViewType(int position) {
        int count = getHeaderLayoutCount() + getData().size();
        //刚开始进入包含该类的activity时,count为0。就会出现0%0的情况，这会抛出异常，所以我们要在下面做一下判断
        if (count <= 0) {
            count = 1;
        }
        int newPosition = position % count;
        Log.d("TEST", "newPosition：" + newPosition);
        return super.getItemViewType(newPosition);
    }


    @Override
    protected void convert(BaseViewHolder helper, Javabean item) {
        switch (helper.getItemViewType()) {
            case 0:
                // helper.setText(R.id.multiltem_def_tv,"悯农－－－唐代诗人李绅");
                Glide.with(context).load(item.getBean()).into((ImageView) helper.getView(R.id.multiltem_def_tv));

                break;
            case 1:
                helper.setText(R.id.content_text_tv, "悯农－－－唐代诗人李绅");
                break;

            case 2:
                List<String>   mData = new ArrayList<>();
                for (int i = 0; i < 90; i++) {
                    mData.add("加载的第" + i + "条数据");
                }
                rc_view = (MarqueeView) helper.getView(R.id.rc_view);
                Glide.with(context).load(R.drawable.b4).asBitmap()//签到整体 背景
                        .into(new SimpleTarget<Bitmap>(180, 180) {        //设置宽高
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                Drawable drawable = new BitmapDrawable(resource);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    rc_view.setBackground(drawable);    //设置背景
                                }
                            }
                        });
                AutoPollAdapter mAdapter = new AutoPollAdapter(context, mData);
                GridLayoutManager layoutManager = new GridLayoutManager(context,2);
                //rc_view.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));//设置LinearLayoutManager.HORIZONTAL  则水平滚动
                rc_view.setLayoutManager(new GridLayoutManager(context,2));
                rc_view.setAdapter(mAdapter);
               // rc_view.start();//不能掉，不然不会自动滚动，根据自己的需求来
                shouldContinue.set(true);
                initScroll();
                if (first){
                    thread.start();
                    first=false;
                }

                break;
        }
    }
    /**RecyclerView条目的滚动操作*/
    @SuppressLint("HandlerLeak")
    private void initScroll() {
        /**主线程的handler，用于执行Marquee的滚动消息*/
        mRecyclerViewHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case RECYCLERVIEWRollROLL:
                        /**垂直移动偏移2个像素*/
                        rc_view.scrollBy(0, 3);
                        /**如果移动到底部*/
                        if (isSlideToBottom(rc_view)) {
                            Log.i(TAG, "滑动到了底部");
                            /**跳至顶部*/
                            rc_view.scrollToPosition(0);
                            /**如果没有移动到底部*/
                        } else {
                            Log.i(TAG, "没有滑动到底部");
                        }
                        break;
                }
            }
        };

        if (thread == null) {
            thread = new Thread() {
                public void run() {
                    while (shouldContinue.get()) {
                        /**每次滚动睡眠0.2秒*/
                        SystemClock.sleep(12);
                        mRecyclerViewHandler.sendEmptyMessage(RECYCLERVIEWRollROLL);
                    }
                    /**退出循环时清理handler*/
                    mRecyclerViewHandler = null;
                }
            };
        }
    }

    /**判断Recycler是否滑动至最底部  是返回true  不是返回false*/
    public static boolean isSlideToBottom(RecyclerView recyclerView) {
        if (recyclerView == null) return false;
        if (recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset()
                >= recyclerView.computeVerticalScrollRange())
            return true;
        return false;
    }
}

