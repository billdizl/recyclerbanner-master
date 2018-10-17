package com.zy.base;

import com.chad.library.adapter.base.entity.MultiItemEntity;

public class Javabean implements MultiItemEntity {
    private int Bean;
    private int item_type;

    public int getItem_type() {
        return item_type;
    }

    public void setItem_type(int item_type) {
        this.item_type = item_type;
    }

    public int getBean() {
        return Bean;
    }

    public void setBean(int bean) {
        Bean = bean;
    }

    @Override
    public int getItemType() {
        return item_type;
    }
}
