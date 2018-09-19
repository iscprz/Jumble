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

 */
public class ActivityMain extends AppCompatActivity  /*implements ActivityHome.ActivityHomeEventListener*/ {

    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.drawer_layout);
        setContentView(R.layout.activity_main);
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
            // Make sure the most recently logged in user is set as current user.
            // Go Userless if no log in has been made.
            SharedPreferences username_prefs
                    = getSharedPreferences(Constants.KEY_GET_PREFS_LOGIN_DATA, Context.MODE_PRIVATE);
            String currUsername = username_prefs.getString(Constants.KEY_CURR_USERNAME, Constants.USERNAME_USERLESS);

            if (Constants.USERNAME_USERLESS.equalsIgnoreCase(currUsername)) {
                App.getAccountHelper().switchToUserless();
            } else {
                // TODO CRASH HERE ON TAB BACK IN:
                // Caused by: java.lang.IllegalStateException: No unexpired OAuthData or refresh token available for user '<userless>'
                App.getAccountHelper().switchToUser(currUsername);

                // check authentication stuff and redo if necessary
                // https://mattbdean.gitbooks.io/jraw/content/v/v1.1.0/oauth2.html

                //App.getAccountHelper().isAuthenticated()
                 /*AuthManager authManager = redditClient.getAuthManager();
                if (authManager.canRenew()) {
                    authManager.renew();
                }*/
            }
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
