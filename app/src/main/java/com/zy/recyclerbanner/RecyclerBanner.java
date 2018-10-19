package com.zy.recyclerbanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zy.base.Javabean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Describe the function of the class
 *
 * @author zhujinlong@ichoice.com
 * @date 2016/10/22
 * @time 9:21
 * @description Describe the place where the class needs to pay attention.
 */
public class RecyclerBanner extends FrameLayout {

    RecyclerView recyclerView;
    LinearLayout linearLayout;
    GradientDrawable defaultDrawable,selectedDrawable;

    BannerReAdapter adapter;
    OnPagerClickListener onPagerClickListener;
    private List<Javabean> datas= new ArrayList<>();


    int size,startX, startY,currentIndex;
    boolean isPlaying;
    private int howlong=2000;

    public interface OnPagerClickListener{

        void onClick(BannerEntity entity);
    }

    public interface BannerEntity{
        String getUrl();
    }

    private Handler handler = new Handler();

    private Runnable playTask = new  Runnable(){

        @Override
        public void run() {
            recyclerView.smoothScrollToPosition(++currentIndex);
            changePoint();
            handler.postDelayed(this,howlong);
        }
    };

    public RecyclerBanner(Context context) {
        this(context,null);
    }

    public RecyclerBanner(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RecyclerBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        size = (int) (6 * context.getResources().getDisplayMetrics().density + 0.5f);
        defaultDrawable = new GradientDrawable();
        defaultDrawable.setSize(size,size);
        defaultDrawable.setCornerRadius(size);
        defaultDrawable.setColor(0xffffffff);
        selectedDrawable = new GradientDrawable();
        selectedDrawable.setSize(size,size);
        selectedDrawable.setCornerRadius(size);
        selectedDrawable.setColor(0xff0094ff);

        recyclerView = new RecyclerView(context);
        LayoutParams vpLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams linearLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setPadding(size * 2,size * 2,size * 2,size * 2);
        linearLayoutParams.gravity = Gravity.BOTTOM;
        addView(recyclerView,vpLayoutParams);
        addView(linearLayout,linearLayoutParams);

        new PagerSnapHelper().attachToRecyclerView(recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false));
        adapter = new BannerReAdapter(context,datas);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int first = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                int last = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                if(currentIndex != (first + last) / 2){
                    currentIndex = (first + last) / 2;
                    changePoint();
                }
            }
        });
    }

    public void setOnPagerClickListener(OnPagerClickListener onPagerClickListener) {
        this.onPagerClickListener = onPagerClickListener;
    }

    public synchronized void setPlaying(boolean playing){
        if(!isPlaying && playing && adapter != null && adapter.getItemCount() > 2){
            handler.postDelayed(playTask,howlong);
            isPlaying = true;
        }else if(isPlaying && !playing){
            handler.removeCallbacksAndMessages(null);
            isPlaying = false;
        }
    }

    public int setDatas(List<Javabean> datas, int howlong){
        setPlaying(false);
        this.datas.clear();
        this.howlong=howlong;
        linearLayout.removeAllViews();
        if(datas != null){
            this.datas.addAll(datas);
        }
        if(this.datas.size() > 1){
            currentIndex = this.datas.size() * 10000;
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(currentIndex);
            for (int i = 0; i < this.datas.size(); i++) {
                ImageView img = new ImageView(getContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.leftMargin = size/2;
                lp.rightMargin = size/2;
                img.setImageDrawable(i == 0 ? selectedDrawable : defaultDrawable);
                linearLayout.addView(img,lp);
            }
            setPlaying(true);
        }else {
            currentIndex = 0;
            adapter.notifyDataSetChanged();
        }
        return this.datas.size();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = (int) ev.getX();
                startY = (int) ev.getY();
                getParent().requestDisallowInterceptTouchEvent(true);
                setPlaying(false);
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) ev.getX();
                int moveY = (int) ev.getY();
                int disX = moveX - startX;
                int disY = moveY - startY;
                getParent().requestDisallowInterceptTouchEvent(2 * Math.abs(disX) > Math.abs(disY));
                setPlaying(false);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setPlaying(true);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setPlaying(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setPlaying(false);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if(visibility == View.GONE){
            // 停止轮播
            setPlaying(false);
        }else if(visibility == View.VISIBLE){
            // 开始轮播
            setPlaying(true);
        }
        super.onWindowVisibilityChanged(visibility);
    }
    private class BannerReAdapter extends BaseMultiItemQuickAdapter<Javabean, BaseViewHolder> {
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
        public  boolean isSlideToBottom(RecyclerView recyclerView) {
            if (recyclerView == null) return false;
            if (recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset()
                    >= recyclerView.computeVerticalScrollRange())
                return true;
            return false;
        }
    }

    private class PagerSnapHelper extends LinearSnapHelper {

        @Override
        public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
            int targetPos = super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
            final View currentView = findSnapView(layoutManager);
            if(targetPos != RecyclerView.NO_POSITION && currentView != null){
                int currentPostion = layoutManager.getPosition(currentView);
                int first = ((LinearLayoutManager)layoutManager).findFirstVisibleItemPosition();
                int last = ((LinearLayoutManager)layoutManager).findLastVisibleItemPosition();
                currentPostion = targetPos < currentPostion ? last : (targetPos > currentPostion ? first : currentPostion);
                targetPos = targetPos < currentPostion ? currentPostion - 1 : (targetPos > currentPostion ? currentPostion + 1 : currentPostion);
            }
            return targetPos;
        }
    }

    private void changePoint(){
        if(linearLayout != null && linearLayout.getChildCount() > 0){
            for (int i = 0; i < linearLayout.getChildCount(); i++) {
                ((ImageView)linearLayout.getChildAt(i)).setImageDrawable(i == currentIndex % datas.size() ? selectedDrawable : defaultDrawable);
            }
        }
    }
}