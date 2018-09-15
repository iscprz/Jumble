package com.sometimestwo.moxie;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/*
        TODOS:
            - toolbar random button
            - animated gifs
            - pinch zoomies
            - double tap to upvote
            - long press to show title of post
            - media viewer image options (download, etc)
            - view pager
            - invalid subreddit

            settings options:
            - browse mode (no comments, upvoting , etc) for lurkers
            - hide progress bar on exoplayer

            layout issues:
            - centering very tall image in large hover
            - hide toolbar on large hover preview
 */
public class ActivityMain extends AppCompatActivity  /*implements ActivityHome.ActivityHomeEventListener*/{

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

        public FetchRedditUser(ActivityMain activity){
            this.activity = new WeakReference<>(activity);
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            App.getAccountHelper().switchToUserless();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            Activity activity = this.activity.get();
            if(activity != null){
                activity.startActivity(new Intent(activity, ActivityHome.class));

            }
        }
    }
}
