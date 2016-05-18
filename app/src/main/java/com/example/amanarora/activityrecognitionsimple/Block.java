package com.example.amanarora.activityrecognitionsimple;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Aman's Laptop on 5/13/2016.
 */
public class Block implements Parcelable {

    public String startTime;
    public String endTime;
    public String state;

    Block(String start, String state) {
        this.startTime = start;
        this.state = state;
    }

    Block(String start, String end, String state) {
        this.startTime = start;
        this.endTime = end;
        this.state = state;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    Block(Parcel in) {
        startTime = in.readString();
        endTime = in.readString();
        state = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeString(state);

    }

    public static final Parcelable.Creator<Block> CREATOR = new Parcelable.Creator<Block>() {
        @Override
        public Block createFromParcel(Parcel source) {
            return new Block(source);
        }

        @Override
        public Block[] newArray(int size) {
            return new Block[size];
        }
    };
}
