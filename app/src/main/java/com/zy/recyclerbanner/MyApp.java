package com.zy.recyclerbanner;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.zy.net.ConnectivityStatus;
import com.zy.net.ReactiveNetwork;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MyApp extends Application {

    private ReactiveNetwork reactiveNetwork;
    private Subscription networkConnectivitySubscription;
    private Subscription internetConnectivitySubscription;
    private Subscription wifiSwitchSubscription ;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context=this;
        reactiveNetwork = new ReactiveNetwork();

        //监听网络连接类型的 （数据流量 、wifi 、断线）
        networkConnectivitySubscription =
                reactiveNetwork.observeNetworkConnectivity(getApplicationContext())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<ConnectivityStatus>() {
                            @Override public void call(final ConnectivityStatus status) {

                               // Log.d(TAG, status.toString());
                                //tvConnectivityStatus.setText( "网络连接状态： " + status.description);
                            }
                        });

        //监听是否链接互联网的 （ 是 ， 否）
        internetConnectivitySubscription =
                reactiveNetwork.observeInternetConnectivity()
                        .observeOn( AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                              //  tvInternetStatus.setText( "是否有可用的网络： "+ aBoolean.toString());
                                if (aBoolean.toString().equals("true")){
                                    Toast.makeText(context, "网络可用", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(context, "网络不可用", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }) ;

        //监听wifi 开关状态的（ wifi 正在打开 、 wifi 打开 、wifi 正在关闭、 wifi 关闭）
        wifiSwitchSubscription =
                reactiveNetwork.observeWifiSwitch( this )
                        .subscribe(new Action1<ConnectivityStatus>() {
                            @Override
                            public void call(ConnectivityStatus connectivityStatus) {
                                //wifiSwitchStatus.setText( "wifi 是否打开： " + connectivityStatus.description);
                                if (connectivityStatus.description.equals("wifi closed")){
                                    Toast.makeText(context, "WLAN未开启请开启", Toast.LENGTH_SHORT).show();
                                   // startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); //直接进入手机中的wifi网络设置界面
                                }
                            }
                        });



    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        safelyUnsubscribe(networkConnectivitySubscription, internetConnectivitySubscription , wifiSwitchSubscription );
    }
    private void safelyUnsubscribe(Subscription... subscriptions) {
        for (Subscription subscription : subscriptions) {
            if (subscription != null && !subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
        }
    }

}
