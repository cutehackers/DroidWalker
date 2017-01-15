package com.jhlee.android.droidwalker.model;

/**
 * DroidWalker
 * <p>
 * date 2017-01-15
 * author Jun-hyoung, Lee
 */
public class DailyWalkSet {

    private long date;
    private int steps;

    public DailyWalkSet(long date, int steps) {
        this.date = date;
        this.steps = steps;
    }

    public long getDate() {
        return date;
    }

    public int getSteps() {
        return steps;
    }

}
