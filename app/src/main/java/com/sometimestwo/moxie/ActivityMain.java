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
            - invalid subreddit
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
            - hide toolbar on large hover preview
            - can open drawer layout while large hover previewing
            - exo player not reloading when tabbing back into app

            names:
            Unfold
 */
public class ActivityMain extends AppCompatActivity  /*implements ActivityHome.ActivityHomeEventListener*/ {

    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.drawer_layout);
        setContentView(R.layout.activity_main);

        /***************** Set default settings/preferences if never initialized ******************/
        SharedPreferences prefs_settings = this.getSharedPreferences(Constants.KEY_GET_PREFS_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefs_settings_editor =
                 this.getSharedPreferences(Constants.KEY_GET_PREFS_SETTINGS, Context.MODE_PRIVATE)
                .edit();

        // Hover previewer size - defaults to small
        if(prefs_settings.getString(Constants.KEY_PREVIEW_SIZE,null) == null) {
            prefs_settings_editor.putString(Constants.KEY_PREVIEW_SIZE, Constants.SETTINGS_PREVIEW_SIZE_LARGE);
        }

        // Allow media icons - defaults to true
        if(prefs_settings.getBoolean(Constants.KEY_SETTINGS_ALLOW_MEDIA_ICON, true) == true){
            prefs_settings_editor.putBoolean(Constants.KEY_SETTINGS_ALLOW_MEDIA_ICON, true);
        }

        // Allow NSFW
        if(prefs_settings.getBoolean(Constants.KEY_ALLOW_NSFW, false) == false){
            prefs_settings_editor.putBoolean(Constants.KEY_ALLOW_NSFW, false);
        }

        // Allow hover previewer - default to yes
        if(prefs_settings.getBoolean(Constants.KEY_ALLOW_HOVER_PREVIEW, true) == true){
            prefs_settings_editor.putBoolean(Constants.KEY_ALLOW_HOVER_PREVIEW, true);
        }


        // Allow tap-to-close big display
        if(prefs_settings.getBoolean(Constants.KEY_ALLOW_BIGDISPLAY_CLOSE_CLICK, false) == false){
            prefs_settings_editor.putBoolean(Constants.KEY_ALLOW_BIGDISPLAY_CLOSE_CLICK, false);
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

           /*   if(App.getAccountHelper().isAuthenticated()){
              String currUsername = App.getAccountHelper().getReddit().getAuthManager().currentUsername();
                //App.getAccountHelper().switchToUser(currUsername);
            }
            else if(!App.getAccountHelper().isAuthenticated()
                    || Constants.USERNAME_USERLESS.equalsIgnoreCase(App.getAccountHelper().getReddit().getAuthManager().currentUsername())) {
                App.getAccountHelper().switchToUserless();

            }*/
                // TODO CRASH HERE ON TAB BACK IN:
                // Caused by: java.lang.IllegalStateException: No unexpired OAuthData or refresh token available for user '<userless>'
                // check authentication stuff and redo if necessary
                // https://mattbdean.gitbooks.io/jraw/content/v/v1.1.0/oauth2.html


                App.getAccountHelper().switchToUserless();
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
