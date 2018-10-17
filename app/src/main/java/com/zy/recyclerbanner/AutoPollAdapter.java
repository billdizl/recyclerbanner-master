package com.zy.recyclerbanner;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.List;

public class AutoPollAdapter extends RecyclerView.Adapter<AutoPollAdapter.BaseViewHolder> {
    /**自动跑马灯似的recyclew适配器
     * */
    private final Context mContext;
    private final List<String> mData;
    public AutoPollAdapter(Context context, List<String> list) {
        this.mContext = context;
        this.mData = list;
    }
    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
        BaseViewHolder holder = new BaseViewHolder(view);
        return holder;
    }
    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        String data = mData.get(position % mData.size());
        holder.content.setText(data);
    }
    @Override
    public int getItemCount() {
        if (mData.size()!=0){
            if (mData.size()<10){
                return  mData.size();
            }else{
                return  Integer.MAX_VALUE;
            }
        }else{
            return 0;
        }

    }
    class BaseViewHolder extends RecyclerView.ViewHolder {
        TextView content;
        public BaseViewHolder(View itemView) {
            super(itemView);
            content = (TextView) itemView.findViewById(R.id.tv_text);
        }
    }
}

