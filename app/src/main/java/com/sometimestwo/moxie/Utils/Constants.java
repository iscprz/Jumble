package com.sometimestwo.moxie.Utils;

import android.os.Environment;

import com.sometimestwo.moxie.R;

import java.io.File;

public class Constants {

    public final static String APP_NAME = "Moxie";
    public final static String APP_DOWNLOAD_PATH =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    + File.separator
                    + APP_NAME;
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
    public final static String TAG_FRAG_FULL_DISPLAYER = "TAG_FRAG_FULL_DISPLAYER";
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
    public final static String EXTRA_GOTO_WEB_URL = "EXTRA_GOTO_WEB_URL";

    public final static int REQUESTCODE_GOTO_SUBREDDIT_VIEWER = 200;
    public final static int REQUESTCODE_GOTO_LOG_IN = 300;
    public final static int REQUESTCODE_GOTO_BIG_DISPLAY = 400;
    public final static int RESULT_OK_START_OVER = 201;
    public final static int RESULT_OK_INVALIDATE_DATA = 101;

    /* Default values*/
    public final static String DEFAULT_SUBREDDIT = "pics";
    public final static int DEFAULT_NUM_DISPLAY_COLS = 3;
    public final static String DEFAULT_DOWNLOAD_FILENAME = APP_NAME + "." + "download";

    /* Query values */
    //the size of a page that we want
    public static final int QUERY_PAGE_SIZE = 50;
    //public static final int QUERY_LOAD_LIMIT = 50;


    /*SharedPrefs */
    public final static String KEY_SHARED_PREFS = "KEY_SHARED_PREFS";
    public final static String PREFS_HIDE_NSFW = "PREFS_HIDE_NSFW";
    public final static String PREFS_ALLOW_HOVER_PREVIEW = "PREFS_ALLOW_HOVER_PREVIEW";
    public final static String PREFS_PREVIEW_SIZE = "PREFS_PREVIEW_SIZE";
    public final static String PREFS_ALLOW_CLOSE_CLICK = "PREFS_ALLOW_CLOSE_CLICK";
    public final static String PREFS_ALLOW_DOMAIN_ICON = "PREFS_ALLOW_DOMAIN_ICON";
    public final static String PREFS_ALLOW_FILETYPE_ICON = "PREFS_ALLOW_FILETYPE_ICON";
    public final static String PREFS_HIDE_NSFW_THUMBS = "PREFS_HIDE_NSFW_THUMBS";
    public final static String PREFS_SHOW_NSFW_ICON = "PREFS_SHOW_NSFW_ICON";
    public final static String PREFS_USER_SUBS_ = "PREFS_USER_SUBS_";
    public final static String SETTINGS_PREVIEW_SIZE_SMALL = "SETTINGS_PREVIEW_SIZE_SMALL";
    public final static String PREFS_PREVIEW_SIZE_LARGE = "PREFS_PREVIEW_SIZE_LARGE";
    public final static String MOST_RECENT_USER = "MOST_RECENT_USER";

    /* API */
    public final static String BASE_URL_GFYCAT = "https://gfycat.com/cajax/get/";
    public final static String YOUTUBE_API_KEY = "AIzaSyASn4HW4p60zBsAuaiejx5jU_rxjqhUNbA";

    /* Misc*/
    public final static String URI_404 = "android.resource://com.sometimestwo.moxie/"
            + R.drawable.reddit_404_transparent;
    public final static String URI_404_thumbnail = "android.resource://com.sometimestwo.moxie/"
            + R.drawable.reddit_404_thumb_black;
    public final static String[] VALID_MEDIA_EXTENSION
            = {"jpg", "jpeg", "png",/*maybe remove */"gifv", "gif", "mp4"};
    public final static String[] VALID_IMAGE_EXTENSION = {"jpg", "jpeg", "png"};
    public final static String[] VALID_GIF_EXTENSION = {"gifv", "gif"};
    public final static String USERNAME_USERLESS = "<userless>";
    public final static String USERNAME_USERLESS_PRETTY = "Guest";
    public final static int MAX_TITLE_LENGTH = 120;
    public final static int REFRESH_PULL_TOLERANCE = 500;
    public final static int COMMENT_LOAD_ROOT_LIMIT = 50;
    public final static int COMMENT_LOAD_CHILD_LIMIT = 6;
    public final static int PERMISSIONS_DOWNLOAD_MEDIA = 900;
    public final static int COMMENTS_CLOSE_SWIPE_DURATION = 200;
    public final static int COMMENTS_OPEN_SWIPE_DURATION = 500;
    public final static int COMMENTS_ROOT_MAX_LOAD_SIZE = 50;
    public final static int COMMENTS_MAX_DEPTH = 7;
    public final static int COMMENTS_MAX_CURR_DEPTH = 25;
    public final static int MAX_LENGTH_URL_DISPLAY = 25;
    public final static int COMMENTS_INDENTATION_PADDING_ROOT = 5;
    // Need to add root padding so that children comments align with parent's start
    public final static int COMMENTS_INDENTATION_PADDING = 50 + COMMENTS_INDENTATION_PADDING_ROOT;
    public final static int COMMENTS_MIN_SCORE = 1;

    // public final static int KEY_INTENT_GOTO_SUBMISSIONVIEWER = 1;


    /* Enums*/
    public enum HoverPreviewSize {
        SMALL,
        LARGE
    }

    public enum SubmissionType {
        IMAGE,
        GIF,
        VIDEO,
        ALBUM
    }

    public enum SubmissionDomain {
        IMGUR,
        GFYCAT,
        VREDDIT,
        IREDDIT,
        YOUTUBE,
        OTHER
    }
}
