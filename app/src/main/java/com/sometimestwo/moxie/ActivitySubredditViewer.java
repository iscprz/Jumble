package com.sometimestwo.moxie;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.sometimestwo.moxie.Utils.Constants;
import com.sometimestwo.moxie.Utils.Utils;

public class ActivitySubredditViewer extends AppCompatActivity implements HomeEventListener,
        Fragment404.Fragment404EventListener,
        OnCloseClickEventListener, FragmentFullDisplay.OnCommentsEventListener {

    public final String TAG = this.getClass().getCanonicalName();
    private SharedPreferences prefs_settings;

    private String mCurrSubbredit;
    private String mExploreCategory;
    private boolean mIs404 = false;    // activity hosts a 404 page

    // Permissions we'll need to make use of
    private boolean mAllowCloseOnClick;
    //For handling back button press. Need to close comments view (not fragment)
    // which resides inside the FullDisplayer fragment.
    private boolean mCommentsOpen = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit_viewer);
        prefs_settings = this.getSharedPreferences(Constants.KEY_GET_PREFS_SETTINGS, Context.MODE_PRIVATE);
        mAllowCloseOnClick = prefs_settings.getBoolean(Constants.SETTINGS_ALLOW_BIGDISPLAY_CLOSE_CLICK, false);

        unpackExtras();
        displaySubreddit(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // This happens when we visit subreddit after subreddit, creating many
        // of these activities and eventually call startOver(). We need to make sure
        // to go all the way down the acitivty stack until we've removed all the
        // ActivitySubredditViewer activities so that we can return to ActivityHome.
        if (resultCode == Constants.RESULT_OK_START_OVER) {
            startOver();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.navigation_menu, menu);
        getMenuInflater().inflate(R.menu.menu_default_header, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Need to make sure user is authenticated
        new Utils.RedditHeartbeatTask(new RedditHeartbeatListener() {
            @Override
            public void redditUserAuthenticated() {
                // do nothing
            }
        }).execute();
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
        // Bookkeeping: We keep track of the subreddits we visit since using the backstack
        //              is not sufficient
        if (!App.getMoxieInfoObj().getmSubredditStack().isEmpty()) {
            App.getMoxieInfoObj().getmSubredditStack().pop();
        }
        super.finish();
    }

    @Override
    public void onBackPressed() {
        // Back button should close nav view drawers if they're open (on either side)
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START) || drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawers();
        } else if (mCommentsOpen) {
            Fragment fragment = getSupportFragmentManager()
                    .findFragmentByTag(Constants.TAG_FRAG_FULL_DISPLAYER);
            // should never be null since comments cannot be open
            if (fragment != null) {
                ((FragmentFullDisplay) fragment).closeComments();
            }
        }
        // If hosting a 404 page, we want to leave activity not just pop 404 page off backstack
        else if (mIs404) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    public void unpackExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mCurrSubbredit = (String) getIntent().getExtras().getString(Constants.EXTRA_GOTO_SUBREDDIT);
            mExploreCategory = (String) getIntent().getExtras().getString(Constants.EXTRA_GOTO_EXPLORE_CATEGORY);
        }
    }

    private void displaySubreddit(boolean invalidateData) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Bundle args = new Bundle();
        args.putString(Constants.ARGS_CURR_SUBREDDIT, mCurrSubbredit);
        args.putBoolean(Constants.ARGS_INVALIDATE_DATASOURCE, invalidateData);
        args.putString(Constants.EXTRA_GOTO_EXPLORE_CATEGORY, mExploreCategory);
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
        retrySubredditLoad(Constants.TAG_FRAG_SUBREDDIT_VIEWER, invalidateData);
    }


    @Override
    public void menuGoBack() {
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
        this.retrySubredditLoad(tag, true);
    }


    @Override
    public void onCloseClickDetected() {
        if (mAllowCloseOnClick) {
            if (!mCommentsOpen) {
                getSupportFragmentManager().popBackStack();
            } else {
                // Check mCommentsOpen prevents this scenario:
                // View submission if full displayer -> Open comments ->
                // Click zoomieview while comments open ->
                // Full displayer closes (should close comments before closing entire view)
                Fragment fragment = getSupportFragmentManager()
                        .findFragmentByTag(Constants.TAG_FRAG_FULL_DISPLAYER);
                // should never be null
                if (fragment != null) {
                    ((FragmentFullDisplay) fragment).closeComments();
                }
            }
        }
    }

    @Override
    public void startOver() {
        setResult(Constants.RESULT_OK_START_OVER);
        finish();
    }

    @Override
    public void isCommentsOpen(boolean commentsOpen) {
        mCommentsOpen = commentsOpen;
    }
}
