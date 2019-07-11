package com.aidl.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zy
 * date 2019/7/9 0009.
 * desc:
 */

public class Msg implements Parcelable {

    private String msg;
    private long time;

    public Msg(String msg){
        this.msg = msg;
    }

    public Msg(String msg, long time) {
        this.msg = msg;
        this.time = time;
    }

    protected Msg(Parcel in) {
        msg = in.readString();
    }

    public static final Creator<Msg> CREATOR = new Creator<Msg>() {
        @Override
        public Msg createFromParcel(Parcel in) {
            return new Msg(in);
        }

        @Override
        public Msg[] newArray(int size) {
            return new Msg[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(msg);
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
