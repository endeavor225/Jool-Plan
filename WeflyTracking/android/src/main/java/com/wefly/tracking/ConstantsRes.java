package com.wefly.tracking;

public final class ConstantsRes {

    public static final String APP_NAME                     = "trackingv3";
    public static final String PACKAGE_RES_NAME             = "com.weflyagri.trackingv3.";
    public static final String PREF_PERMISSION              = PACKAGE_RES_NAME+ "permission_preference";
    public  final String PREF_USER_PROFIL                   = PACKAGE_RES_NAME+ ".user.profil";
    public  final String PREF_PROFIL_PREVIOUS               = PACKAGE_RES_NAME+ ".user.profil.previous";
    public  final String PREF_TRACKING_OBJ_LIST             = PACKAGE_RES_NAME+ ".tracking.full";
    public  final String PREF_TRACKING_REQUIRE_RESET        = PACKAGE_RES_NAME+ ".tracking.reset.list";
    public  final String PREF_EMPTY_LIST                    = "[]";
    public  final String PREF_TRACKING_CUSTOM_INTERVAL      = PACKAGE_RES_NAME+ ".tracking.interval";

    //Service Notification
    public static final String APP_CHANNEL_ID               = APP_NAME+"_chan";

    public static final int NOTIFICATION_SERVICE_AUTO_POST_ID = 98783;
    public static final int NOTIFICATION_APP_ID               = 987684;
    public static final int NOTIFICATION_SYNC_PROGRESS        = 987685;
    public static final String CHANNEL_PROGRESS_ID            = APP_CHANNEL_ID+"step";
    public static final String SAVE_PREFERENCE_NAME           = APP_NAME +"Save";
    public static final String PREF_FIRED_ON_NETWORK          = APP_NAME + ".service.fired.DbUnitwork";

    // REQUEST SETTING
    public static final int VOLLEY_TIME_OUT                    = 7200000; //2h
    public static final int VOLLEY_PING_TIME                   = 60000; //1 min
    public static final String API_URL                         =  "http://178.33.130.195:8000/";
    public static final String BASE_URL                        = API_URL + "admin/";
    public static final String GRAPH_QL_URL                    = API_URL + "graphql/";

    public static final String TAG_MAIN_SERVICE                = "MainService";

    //TRACKING
    public static final int TRACKING_30_MIN                  = 30;
    public static final int TRACKING_5_SEC                   = 5;
    public static final int DISTANCE_7_METERS                = 7;
    public static final int POINT_MIN_REQUIRE                = 2;
    public static final int DELAY_SHORT_150                  = 150; //Millisec
    public static final double DISTANCE_3_METERS             = 3.7;
    public static final int UPLOAD_1_MIN                     = 1;
    public static final int MIN_1                            = 1;
    public static final int MAX_MOTION_LOOP                  = 100;
    public static final int MAX_LOOP_IS_SUPPORTED            = 1000;
    public static final int HOUR_24_IN_MIN                   = 1440;
    public final int        TIME_MIN                         = 1;
    public final int        TIME_MAX                         = 120;
    public static final String LISTENER_ID                   = "1";
    public final String JSON_KEY_LATITUDE                    = "latitude";
    public final String JSON_KEY_LONGITUDE                   = "longitude";
    public final String JSON_KEY_ACCURACY                    = "accuracy";
    public final String JSON_KEY_DATE                        = "time";
    public final String JSON_KEY_TRACK_ID                    = "trackId";
    public final String JSON_KEY_ALTITUDE                    = "altitude";

    // Bundle
    public final String BUNDLE_CODE                         = "code";
    public final String BUNDLE_ID                           = "id";
    public final String BUNDLE_LOCATION                     = "location";
    public final String BUNDLE_SUPPORTED                    = "supported";
    public final String BUNDLE_TIME                         = "timeout";


}
