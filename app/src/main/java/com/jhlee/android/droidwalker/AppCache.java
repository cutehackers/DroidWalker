package com.jhlee.android.droidwalker;

import java.util.HashMap;
import java.util.Map;

/**
 * date     2017-01-14
 * author   Jun-hyoung, Lee
 * description
 * history
 */

public final class AppCache {

    // NAVER MAP
    public static final String NMAP_BASE_URL = "https://openapi.naver.com/v1/map/";
    public static final String NMAP_CLIENT_ID = "2A5SHJ9w32DpvqRw1oyR";
    public static final String NMAP_CLIENT_SECRET = "POOkOz0dqh";

    private static Map<String, String> sNMapClient;
    public static Map<String, String> getNMapClient() {
        if (sNMapClient == null) {
            sNMapClient = new HashMap<>();
            sNMapClient.put("X-Naver-Client-Id", NMAP_CLIENT_ID);
            sNMapClient.put("X-Naver-Client-Secret", NMAP_CLIENT_SECRET);
        }
        return sNMapClient;
    }

    public static final String PREFS_WALKER_ENABLED = "prefs_walker_enabled";

    public static final String PREFS_WALKER_START_OFFSET = "prefs_walker_start_offset";
}
