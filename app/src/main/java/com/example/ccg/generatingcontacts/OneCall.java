package com.example.ccg.generatingcontacts;

import android.support.annotation.NonNull;

/**
 * author:chen hao
 * email::
 * time:2019/06/27
 * desc:
 * version:1.0
 */
public class OneCall {

    /**
     *  number 通话号码
     *  duration 通话时长（响铃时长）以秒为单位 1分30秒则输入90
     *  type  通话类型  1呼入 2呼出 3未接
     *  isNew 是否已查看    0已看1未看
     */
    private String number;
    private String duration;
    private String type;
    //public String isNew;

    public OneCall(){

    }

    public OneCall(String number, String duration, String type) {
        this.number = number;
        this.duration = duration;
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @NonNull
    @Override
    public String toString() {
        return "OneCall{" +
                "number='" + number + '\'' +
                ", duration='" + duration + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
