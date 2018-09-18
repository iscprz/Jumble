package com.sometimestwo.moxie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.sometimestwo.moxie.Utils.Constants;

public class ActivitySubredditViewer extends AppCompatActivity implements FragmentHome.HomeEventListener,
        FragmentSubmissionViewer.SubmissionDisplayerEventListener,
        Fragment404.Fragment404EventListener {

    public final String TAG = this.getClass().getCanonicalName();

    private String mCurrSubbredit;
    private boolean mIs404 = false;    // activity hosts a 404 page

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit_viewer);
        unpackExtras();

        displaySubreddit(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    /*    super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.INTENT_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Log.e(TAG,"Returned from settings activity");
                *//*if ((int) data.getExtras().get(Constants.NUM_GALLERIE_DIRS_CHOSEN) < 1) {
                } *//*
            }
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.navigation_menu, menu);
        getMenuInflater().inflate(R.menu.menu_subreddit_viewer_header, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onBackPressed() {
        // if activity is hosting a 404 page, we want to leave entire
        // activity not just pop 404 page off backstack
        if (mIs404) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    public void unpackExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mCurrSubbredit = (String) getIntent().getExtras().get(Constants.EXTRA_GOTO_SUBREDDIT);
        }
    }

    private void displaySubreddit(boolean invalidateData) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Bundle args = new Bundle();
        args.putString(Constants.ARGS_CURR_SUBREDDIT, mCurrSubbredit);
        args.putBoolean(Constants.ARGS_INVALIDATE_DATASOURCE, invalidateData);
        Fragment fragment = FragmentHome.newInstance();
        fragment.setArguments(args);
        ft.add(R.id.fragment_container_subreddit_viewer, fragment, Constants.TAG_FRAG_SUBREDDIT_VIEWER);
        ft.commit();
    }

    protected void retrySubredditLoad(String fragmentTag, boolean invalidateData) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        try {
            ft.remove(fragment).commit();
            // try displaying subreddit again after removing 404 fragment
            displaySubreddit(invalidateData);
        } catch (NullPointerException e) {
            throw new NullPointerException(this.toString()
                    + ". Could not refresh fragment! Probably provided incorrect fragment tag. " +
                    " Fragment tag provided: " + Constants.TAG_FRAG_404);
        }
    }

    /*
        Interface implementations
     */
    @Override
    public void openSettings() {
        Intent settingsIntent = new Intent(this, ActivitySettings.class);
        //settingsIntent.putExtra()
        startActivityForResult(settingsIntent, Constants.INTENT_SETTINGS);
    }

    // Called on refresh swipe
    @Override
    public void refreshFeed(boolean invalidateData) {
        this.retrySubredditLoad(Constants.TAG_FRAG_SUBREDDIT_VIEWER, invalidateData);
    }

    @Override
    public void isHome(boolean isHome) { }

    @Override
    public void goBack() {
        onBackPressed();
    }

    /* Marks this activity as being an activity that hosts a 404 page. This will be useful when
     *  handling Back button presses to leave activity instead of only popping "404 message" */
    @Override
    public void set404(boolean is404) {
        this.mIs404 = is404;
    }

    // Called on Retry button click
    @Override
    public void refresh404(String tag) {
        this.retrySubredditLoad(tag,true);
    }
}
