package com.sometimestwo.moxie.Utils;

public class Constants {

    /* Test vals*/
    public final static String TEST_SUBREDDIT_EARTHPORN = "earthporn";
    public final static String TEST_SUBREDDIT_PICS = "pics";
    public final static String TEST_SUBREDDIT_GIFS = "gifs";

    /* Intents */
    public final static int INTENT_SETTINGS = 1;

    /* Fragment tags */
    public final static String TAG_FRAG_HOME = "TAG_FRAG_HOME";
    public final static String TAG_FRAG_SUBREDDIT_VIEWER = "TAG_FRAG_SUBREDDIT_VIEWER";
    public final static String TAG_FRAG_404 = "TAG_FRAG_404";
    public final static String TAG_FRAG_MEDIA_DISPLAY = "TAG_FRAG_MEDIA_DISPLAY";
    public final static String TAG_FRAG_SIMPLE_DISPLAYER = "TAG_FRAG_SIMPLE_DISPLAYER";

    /* Arguments, Extras and Result codes*/
    public final static String ARGS_NUM_DISPLAY_COLS = "ARGS_NUM_DISPLAY_COLS";
    public final static String ARGS_REDDIT_STATE_OBJ = "ARGS_REDDIT_STATE_OBJ";
    public final static String ARGS_SUBMISSION_OBJ = "ARGS_SUBMISSION_OBJ";
    public final static String ARGS_INVALIDATE_DATASOURCE = "ARGS_INVALIDATE_DATASOURCE";
    public final static String ARGS_CURR_SUBREDDIT = "ARGS_CURR_SUBREDDIT";
    public final static String EXTRA_GOTO_EXPLORE_CATEGORY = "EXTRA_GOTO_EXPLORE_CATEGORY";
    public final static String EXTRA_SUBMISSION_OBJ = "EXTRA_SUBMISSION_OBJ";
    public final static String EXTRA_GOTO_SUBREDDIT = "EXTRA_GOTO_SUBREDDIT";
    public final static String ARGS_IMAGE_ONLY_REQUEST= "ARGS_IMAGE_ONLY_REQUEST";
    public final static int RESULT_OK_INVALIDATE_DATA = 100;


    /* Default values*/
    public final static String DEFAULT_SUBREDDIT = "pics";
    public final static int DEFAULT_NUM_DISPLAY_COLS = 3;

    /* Query values */
    //the size of a page that we want
    public static final int QUERY_PAGE_SIZE = 50;
    //public static final int QUERY_LOAD_LIMIT = 50;


    /*SharedPrefs */
    public final static String KEY_GET_PREFS_SETTINGS = "KEY_GET_PREFS_SETTINGS";
    public final static String SETTINGS_HIDE_NSFW = "SETTINGS_HIDE_NSFW";
    public final static String SETTINGS_ALLOW_HOVER_PREVIEW = "SETTINGS_ALLOW_HOVER_PREVIEW";
    public final static String SETTINGS_PREVIEW_SIZE = "SETTINGS_PREVIEW_SIZE";
    public final static String SETTINGS_ALLOW_BIGDISPLAY_CLOSE_CLICK = "SETTINGS_ALLOW_BIGDISPLAY_CLOSE_CLICK";
    public final static String SETTINGS_ALLOW_DOMAIN_ICON = "SETTINGS_ALLOW_DOMAIN_ICON";
    public final static String SETTINGS_ALLOW_FILETYPE_ICON = "SETTINGS_ALLOW_FILETYPE_ICON";
    public final static String SETTINGS_HIDE_NSFW_THUMBS = "SETTINGS_HIDE_NSFW_THUMBS";
    public final static String SETTINGS_SHOW_NSFW_ICON = "SETTINGS_SHOW_NSFW_ICON";

    public final static String SETTINGS_PREVIEW_SIZE_SMALL = "SETTINGS_PREVIEW_SIZE_SMALL";
    public final static String SETTINGS_PREVIEW_SIZE_LARGE = "SETTINGS_PREVIEW_SIZE_LARGE";
    public final static String MOST_RECENT_USER = "MOST_RECENT_USER";

    /* API */
    public final static String BASE_URL_GFYCAT = "https://gfycat.com/cajax/get/";

    /* Misc*/
    public final static String[] VALID_MEDIA_EXTENSION = {"jpg", "jpeg", "png","gifv" , "gif"};
    public final static String[] VALID_IMAGE_EXTENSION = {"jpg", "jpeg", "png"};
    public final static String[] VALID_GIF_EXTENSION = {"gifv", "gif"};
    public final static String THUMBNAIL_NOT_FOUND = "THUMBNAIL_NOT_FOUND";
    public final static String USERNAME_USERLESS = "<userless>";
    public final static String USERNAME_USERLESS_PRETTY = "Guest";
    public final static int MAX_TITLE_LENGTH = 120;
    public final static int REFRESH_PULL_TOLERANCE = 700;


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
