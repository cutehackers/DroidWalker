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
    private int distance;

    public DailyWalkSet(long date, int steps, int distance) {
        this.date = date;
        this.steps = steps;
        this.distance = distance;
    }

    public long getDate() {
        return date;
    }

    public int getSteps() {
        return steps;
    }

    public int getDistance() {
        return distance;
    }

}
