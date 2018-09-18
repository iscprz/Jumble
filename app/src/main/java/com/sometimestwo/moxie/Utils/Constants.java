package com.sometimestwo.moxie.Utils;

public class Constants {

    /* Test vals*/
    public final static String TEST_SUBREDDIT_EARTHPORN = "earthporn";
    public final static String TEST_SUBREDDIT_PICS = "pics";
    public final static String TEST_SUBREDDIT_GIFS = "gifs";

    /* Intents */
    public final static int INTENT_SETTINGS = 1;

    /* Fragment tags */
    public final static String TAG_FRAG_HOME = "TAG_FRAGMENT_HOME";
    public final static String TAG_FRAG_SUBREDDIT_VIEWER = "TAG_FRAG_SUBREDDIT_VIEWER";
    public final static String TAG_ACTIVITY_MEDIA_VIEWER = "TAG_ACTIVITY_MEDIA_VIEWER";
    public final static String TAG_FRAG_MEDIA_DISPLAY = "TAG_FRAG_MEDIA_DISPLAY";

    /* Arguments, Extras and Result codes*/
    public final static String ARGS_NUM_DISPLAY_COLS = "ARGS_NUM_DISPLAY_COLS";
    public final static String ARGS_REDDIT_STATE_OBJ = "ARGS_REDDIT_STATE_OBJ";
    public final static String ARGS_SUBMISSION_OBJ = "ARGS_SUBMISSION_OBJ";
    public final static String ARGS_INVALIDATE_DATASOURCE = "ARGS_INVALIDATE_DATASOURCE";
    public final static String ARGS_CURR_SUBREDDIT = "ARGS_CURR_SUBREDDIT";
    public final static String EXTRA_SUBMISSION_OBJ = "EXTRA_SUBMISSION_OBJ";
    public final static String EXTRA_GOTO_SUBREDDIT = "EXTRA_GOTO_SUBREDDIT";
    public final static int RESULT_OK_INVALIDATE_DATA = 100;


    /* Default values*/
    public final static String DEFAULT_SUBREDDIT = "pics";
    public final static int DEFAULT_NUM_DISPLAY_COLS = 3;

    /* Query values */
    //the size of a page that we want
    public static final int QUERY_PAGE_SIZE = 50;
    //public static final int QUERY_LOAD_LIMIT = 50;

    /* API */
    public final static String API_GRANT_TYPE = "https://oauth.reddit.com/grants/installed_client";
    public final static String API_CONTENT_TYPE = "application/x-www-form-urlencoded";
    public final static String[] API_SCOPES = {"read", "identity","vote"};
    public final static String TEST_CLIENT_ID = "x0CvI7eA_5Qchg";

    /*SharedPrefs */
    public final static String KEY_GET_PREFS_SETTINGS = "KEY_GET_PREFS_SETTINGS";
    public final static String KEY_GET_PREFS_LOGIN_DATA = "KEY_GET_PREFS_LOGIN_DATA";
    public final static String KEY_ALLOW_NSFW = "KEY_ALLOW_NSFW";
    public final static String KEY_ALLOW_HOVER_PREVIEW = "KEY_ALLOW_HOVER_PREVIEW";
    public final static String KEY_PREVIEW_SIZE = "KEY_PREVIEW_SIZE";
    public final static String KEY_ALLOW_BIGDISPLAY_CLOSE_CLICK = "KEY_ALLOW_BIGDISPLAY_CLOSE_CLICK";
    public final static String KEY_CURR_USERNAME = "KEY_CURR_USERNAME";

    public final static String SETTINGS_NO = "SETTINGS_NO";
    public final static String SETTINGS_YES = "SETTINGS_YES";
    public final static String SETTINGS_PREVIEW_SIZE_SMALL = "SETTINGS_PREVIEW_SIZE_SMALL";
    public final static String SETTINGS_PREVIEW_SIZE_LARGE = "SETTINGS_PREVIEW_SIZE_LARGE";

    /* Exception tags*/

    /* Misc*/
    public final static String[] VALID_MEDIA_EXTENSION = {"jpg", "jpeg", "png","gifv" , "gif"};
    public final static String[] VALID_IMAGE_EXTENSION = {"jpg", "jpeg", "png"};
    public final static String[] VALID_GIF_EXTENSION = {"gifv", "gif"};
    public final static String THUMBNAIL_NOT_FOUND = "THUMBNAIL_NOT_FOUND";
    public final static String USERNAME_USERLESS = "<userless>";

    /* Enums*/
    public enum HoverPreviewSize{
        SMALL,
        LARGE
    }
    public enum SubmissionType{
        IMAGE,
        GIF,
        VIDEO
    }
}
