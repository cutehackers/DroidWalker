package com.jhlee.android.droidwalker.model;

import com.nhn.android.maps.maplib.NGeoPoint;

/**
 * DroidWalker
 *
 * date 2017-01-14
 * author Jun-hyoung, Lee
 */

public class WalkSet {
    private int step;
    private int distance;
    private NGeoPoint location;

    public WalkSet(int step, int distance, NGeoPoint location) {
        this.step = step;
        this.distance = distance;
        this.location = location;
    }

    public int getSteps() {
        return step;
    }

    public int getDistance() {
        return distance;
    }

    public NGeoPoint getLocation() {
        return location;
    }
}
