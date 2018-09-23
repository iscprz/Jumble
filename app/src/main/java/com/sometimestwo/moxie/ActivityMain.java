package com.sometimestwo.moxie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;

import com.sometimestwo.moxie.Utils.Constants;

import java.lang.ref.WeakReference;

/*
        TODOS:
            - double tap to upvote
            - view pager
            - exoplayer releases
            - crash after token expires
            settings options:
            - browse mode (no comments, upvoting , etc) for lurkers
            - hide progress bar on exoplayer
            -NSFW icon
            - play gif icon
            - When large previewer fails to load, loads previously successful image instead

            layout issues:
            - centering very tall image in large hover
            - can open drawer layout while large hover previewing
            - exo player not reloading when tabbing back into app
            - create truncated title for long titles. To be useful in previewers
            - Go to Subreddit edit text box is too short (height wise) for small screen

            names:
            Unfold
 */
public class ActivityMain extends AppCompatActivity  /*implements ActivityHome.ActivityHomeEventListener*/ {

    private final String TAG = this.getClass().getSimpleName();

    private SharedPreferences prefs_settings;
    private SharedPreferences.Editor prefs_settings_editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.drawer_layout);
        setContentView(R.layout.activity_main);

        /* Set default settings/preferences if never initialized.
         *
         * This is more of a precautionary mesaure to ensure we don't find null preferences later.
         */
        prefs_settings = this.getSharedPreferences(Constants.KEY_GET_PREFS_SETTINGS, Context.MODE_PRIVATE);
        prefs_settings_editor =
                 this.getSharedPreferences(Constants.KEY_GET_PREFS_SETTINGS, Context.MODE_PRIVATE)
                .edit();

        // Hover previewer size - defaults to small
        if(prefs_settings.getString(Constants.SETTINGS_PREVIEW_SIZE,null) == null) {
            prefs_settings_editor.putString(Constants.SETTINGS_PREVIEW_SIZE, Constants.SETTINGS_PREVIEW_SIZE_LARGE);
        }



        // Allow NSFW
        if(prefs_settings.getBoolean(Constants.SETTINGS_ALLOW_NSFW, false) == false){
            prefs_settings_editor.putBoolean(Constants.SETTINGS_ALLOW_NSFW, false);
        }

        // Hide NSFW thumbs
        if(prefs_settings.getBoolean(Constants.SETTINGS_HIDE_NSFW_THUMBS, false) == false){
            prefs_settings_editor.putBoolean(Constants.SETTINGS_HIDE_NSFW_THUMBS, false);
        }

        // Allow hover previewer - default to yes
        if(prefs_settings.getBoolean(Constants.SETTINGS_ALLOW_HOVER_PREVIEW, true) == true){
            prefs_settings_editor.putBoolean(Constants.SETTINGS_ALLOW_HOVER_PREVIEW, true);
        }

        // Allow tap-to-close big display
        if(prefs_settings.getBoolean(Constants.SETTINGS_ALLOW_BIGDISPLAY_CLOSE_CLICK, false) == false){
            prefs_settings_editor.putBoolean(Constants.SETTINGS_ALLOW_BIGDISPLAY_CLOSE_CLICK, false);
        }

        // Allow domain icons - defaults to false
        if(prefs_settings.getBoolean(Constants.SETTINGS_ALLOW_DOMAIN_ICON, false) == false){
            prefs_settings_editor.putBoolean(Constants.SETTINGS_ALLOW_DOMAIN_ICON, false);
        }

        // Allow filetype icons - defaults to false
        if(prefs_settings.getBoolean(Constants.SETTINGS_ALLOW_FILETYPE_ICON, false) == false){
            prefs_settings_editor.putBoolean(Constants.SETTINGS_ALLOW_FILETYPE_ICON, false);
        }

        // Show NSFW icon on NSFW submissions
        if(prefs_settings.getBoolean(Constants.SETTINGS_SHOW_NSFW_ICON, false) == false){
            prefs_settings_editor.putBoolean(Constants.SETTINGS_SHOW_NSFW_ICON, false);
        }

        prefs_settings_editor.commit();

        new FetchRedditUser(this).execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == Constants.INTENT_SETTINGS) {
            if (resultCode == RESULT_OK) {
            }
        }*/
    }

    private class FetchRedditUser extends AsyncTask<Void, Void, Boolean> {
        private final WeakReference<ActivityMain> activity;

        public FetchRedditUser(ActivityMain activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            // Load most recently logged in user
            String mostRecentUser = prefs_settings.getString(Constants.MOST_RECENT_USER, Constants.USERNAME_USERLESS);

            if(!Constants.USERNAME_USERLESS.equalsIgnoreCase(mostRecentUser)){
                try{
                    App.getAccountHelper().switchToUser(mostRecentUser);
                }
                catch (Exception e){
                    App.getAccountHelper().switchToUser(mostRecentUser);
                }
            }
            //TODO Test to check if issue is here
            else{
                try {
                    App.getAccountHelper().switchToUserless();
                }
                catch (Exception e){
                    App.getAccountHelper().switchToUserless();
                }
            }

                // TODO CRASH HERE ON TAB BACK IN:
                // Caused by: java.lang.IllegalStateException: No unexpired OAuthData or refresh token available for user '<userless>'
                // check authentication stuff and redo if necessary
                // https://mattbdean.gitbooks.io/jraw/content/v/v1.1.0/oauth2.html


            return true;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            Activity activity = this.activity.get();
            if (activity != null) {
                activity.startActivity(new Intent(activity, ActivityHome.class));
            }
        }
    }
}
