package com.sometimestwo.moxie;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.sometimestwo.moxie.Model.SubmissionObj;
import com.sometimestwo.moxie.Utils.Constants;


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
public class MainActivity extends AppCompatActivity implements FragmentHome.HomeEventListener,
        FragmentSubmissionViewer.SubmissionDisplayerEventListener {

    private static final String TAG = "MainActivity";

    // indicates whether user has clicked a submissions to browse
    private boolean isViewingSubmission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.drawer_layout);
        setContentView(R.layout.activity_fragment);

        Log.d(TAG, "onCreate: starting.");
        init();
    }

    private void init(){
        // initialize user settings in case this is first time app is being run
        SharedPreferences prefs = getSharedPreferences(Constants.KEY_GETPREFS_SETTINGS, Context.MODE_PRIVATE);


        //TODO: Read this information from sharedprefs. Hardcoded for now
        App.getCurrSubredditObj().setSubreddit("pics");
        App.getCurrSubredditObj().setAllowNSFW(prefs.getString(Constants.KEY_ALLOW_NSFW,Constants.SETTINGS_NO)
                                                               .equalsIgnoreCase(Constants.SETTINGS_YES));
        int numDisplayCols = 3;

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = (FragmentHome) fm.findFragmentByTag(Constants.TAG_FRAG_HOME);
        FragmentTransaction ft = fm.beginTransaction();
        Bundle args = new Bundle();
        args.putInt(Constants.ARGS_NUM_DISPLAY_COLS,numDisplayCols);
        if (fragment != null) {
            //fragment.setArguments(args);
            ft.remove(fragment);
        }

        fragment = FragmentHome.newInstance();
        fragment.setArguments(args);
        ft.add(R.id.fragment_container, fragment, Constants.TAG_FRAG_HOME);
        ft.commit();
    }

    private void refreshFragment(String fragmentTag){
        Fragment frg = getSupportFragmentManager().findFragmentByTag(fragmentTag);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        try{
            ft.detach(frg);
            ft.attach(frg);
            ft.commit();
        }catch (NullPointerException e){
            throw new NullPointerException(this.toString()
                    + ". Could not refresh fragment! Probably provided incorrect fragment tag. " +
                    " Fragment tag provided: " + fragmentTag);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.navigation_menu, menu);
        getMenuInflater().inflate(R.menu.menu_subreddit_view, menu);
        return true;
    }
    @Override
    public void onBackPressed() {
        // ask if user is sure they want to exit
        isViewingSubmission = false;
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.INTENT_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Log.e(TAG,"Returned from settings activity");
                /*if ((int) data.getExtras().get(Constants.NUM_GALLERIE_DIRS_CHOSEN) < 1) {
                } */
            }
        }

    }
    /*
        Interface implementations
     */

    @Override
    public void openSettings(){
        Intent settingsIntent = new Intent(this,ActivitySettings.class);
        //settingsIntent.putExtra()
        startActivityForResult(settingsIntent,Constants.INTENT_SETTINGS);
    }

    @Override
    public void refreshFeed(String fragmentTag) {
        this.refreshFragment(fragmentTag);
    }
}
