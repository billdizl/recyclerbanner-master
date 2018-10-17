package com.zy.recyclerbanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zy.base.Javabean;
import com.zy.net.ConnectivityStatus;
import com.zy.net.ReactiveNetwork;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private List<Javabean> list = new ArrayList<>();
    boolean isRunning = false;
    private int currentpager=0;
    private ScrollSpeedLinearLayoutManger layoutManager;
    private RecyclerView recyclerView;

    private ReactiveNetwork reactiveNetwork ;
    private Subscription networkConnectivitySubscription;
    private Subscription internetConnectivitySubscription;
    private Subscription wifiSwitchSubscription ;

    private static final String TAG = "ReactiveNetwork";
    private TextView tvConnectivityStatus;
    private TextView tvInternetStatus;
    private TextView wifiSwitchStatus ;
    private EditText et_test;
    private Button btn_test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        list.add(R.drawable.b1);
//        list.add(R.drawable.b2);
//        list.add(R.drawable.b3);
//        list.add(R.drawable.b4);
//        list.add(R.drawable.b3);

        et_test = findViewById(R.id.et_test);
        btn_test = findViewById(R.id.btn_test);
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String trim = et_test.getText().toString().trim();
                double value = Double.parseDouble(trim);
                String strD = String.valueOf(value*10);
                String[] strArr = strD.split("\\.");
                Toast.makeText(MainActivity.this,  Double.parseDouble(strArr[0])/10+"", Toast.LENGTH_SHORT).show();



                //  BigDecimal bigDecimal = new BigDecimal(v);
                //   bigDecimal = bigDecimal.setScale(1, BigDecimal.ROUND_DOWN);
                // Toast.makeText(MainActivity.this,bigDecimal.doubleValue()+"", Toast.LENGTH_SHORT).show();
            }
        });
        String appMetaData = getAppMetaData(this, "UMENG_CHANNEL");
        et_test.setText(appMetaData);
        Toast.makeText(this, ""+appMetaData, Toast.LENGTH_SHORT).show();


//        BannerAdapter adapter = new BannerAdapter(this, list);
        BannerReAdapter adapter = new BannerReAdapter(this, list);
        recyclerView = findViewById(R.id.recycler);
        layoutManager = new ScrollSpeedLinearLayoutManger(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(list.size() * 10);



        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                recyclerView.smoothScrollToPosition(layoutManager.findFirstVisibleItemPosition() + 1);
            }
        }, 10000, 10000, TimeUnit.MILLISECONDS);
        tvConnectivityStatus = (TextView) findViewById( R.id.tv1 );
        tvInternetStatus= (TextView) findViewById( R.id.tv2 );
        wifiSwitchStatus= (TextView) findViewById( R.id.tv3 );

        reactiveNetwork = new ReactiveNetwork() ;

        //监听网络连接类型的 （数据流量 、wifi 、断线）
        networkConnectivitySubscription =
                reactiveNetwork.observeNetworkConnectivity(getApplicationContext())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<ConnectivityStatus>() {
                            @SuppressLint("SetTextI18n")
                            @Override public void call(final ConnectivityStatus status) {

                                Log.d(TAG, status.toString());
                                tvConnectivityStatus.setText( "网络连接状态： " + status.description);
                            }
                        });

        //监听是否链接互联网的 （ 是 ， 否）
        internetConnectivitySubscription =
                reactiveNetwork.observeInternetConnectivity()
                        .observeOn( AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Boolean>() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void call(Boolean aBoolean) {
                                tvInternetStatus.setText( "是否有可用的网络： "+ aBoolean.toString());
                                if (aBoolean.toString().equals("true")){
                                    // Toast.makeText(MainActivity.this, "网络可用", Toast.LENGTH_SHORT).show();
                                }else{
                                    // Toast.makeText(MainActivity.this, "网络不可用", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }) ;

        //监听wifi 开关状态的（ wifi 正在打开 、 wifi 打开 、wifi 正在关闭、 wifi 关闭）
        wifiSwitchSubscription =
                reactiveNetwork.observeWifiSwitch( this )
                        .subscribe(new Action1<ConnectivityStatus>() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void call(ConnectivityStatus connectivityStatus) {
                                wifiSwitchStatus.setText( "wifi 是否打开： " + connectivityStatus.description);
                                if (connectivityStatus.description.equals("wifi closed")){
                                    //startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); //直接进入手机中的wifi网络设置界面
                                }
                            }
                        });





//        // 开启轮询
//        new Thread(){
//            public void run() {
//                isRunning = true;
//                while(isRunning){
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//
//                                if ((layoutManager.findFirstVisibleItemPosition()%list.size())+1==list.size()){
//                                    currentpager=0;
//                                }else{
//                                    currentpager= (layoutManager.findFirstVisibleItemPosition()%list.size())+1;
//                                }
//                            Toast.makeText(MainActivity.this, "设置当前位置: " + currentpager, Toast.LENGTH_SHORT).show();
//
//
//                        }
//                    });
//
//                    try {
//
////                            if (currentpager==0){
//                              Thread.sleep(8000);
////                            }else{
// //                               Thread.sleep(2000);
//                         //   }
//
//
//
//
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    // 往下跳一位
//                    runOnUiThread(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            recyclerView.smoothScrollToPosition(layoutManager.findFirstVisibleItemPosition() + 1);
//                        }
//                    });
//
//                }
//            };
//        }.start();
        initdata();
    }

    private void initdata() {
        Javabean javabean1=new Javabean();
        javabean1.setBean(R.drawable.b1);
        javabean1.setItem_type(0);
        list.add(javabean1);


        Javabean javabean2=new Javabean();
        javabean2.setBean(R.drawable.b2);
        javabean2.setItem_type(1);
        list.add(javabean2);


        Javabean javabean3=new Javabean();
        javabean3.setBean(R.drawable.b3);
        javabean3.setItem_type(2);
        list.add(javabean3);
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);


        final BannerIndicator bannerIndicator = findViewById(R.id.indicator);
        bannerIndicator.setNumber(list.size());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int i = layoutManager.findFirstVisibleItemPosition() % list.size();
                    bannerIndicator.setPosition(i);
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        safelyUnsubscribe(networkConnectivitySubscription, internetConnectivitySubscription , wifiSwitchSubscription );
    }

    private void safelyUnsubscribe(Subscription... subscriptions) {
        for (Subscription subscription : subscriptions) {
            if (subscription != null && !subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
        }
    }

    /**
     * 获取application中指定的meta-data 调用方法时key就是UMENG_CHANNEL
     * @return 如果没有获取成功(没有对应值，或者异常)，则返回值为空
     */
    public static String getAppMetaData(Context ctx, String key) {
        if (ctx == null || TextUtils.isEmpty(key)) {
            return null;
        }
        String resultData = null;
        try {
            PackageManager packageManager = ctx.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        resultData = applicationInfo.metaData.getString(key);
                    }
                }

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return resultData;
    }



}
