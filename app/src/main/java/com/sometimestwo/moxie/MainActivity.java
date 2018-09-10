package com.sometimestwo.moxie;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import com.sometimestwo.moxie.Utils.Constants;
import net.dean.jraw.models.Submission;


/*
        TODOS:
            - everything lolz
            - toolbar random button
            - animated gifs
            - refreshFeed pulldown
            - pinch zoomies
            - long press to open, click to play gif
            - double tap to upvote
            - long press to show title of post
            - long press media viewer to show options (save, download, ?>??)
            - view pager
 */
public class MainActivity extends AppCompatActivity implements FragmentHome.HomeEventListener,
        FragmentMediaDisplayer.MediaDisplayerEventListener {

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
        //TODO: Read this information from sharedprefs. Hardcoded for now
        App.getCurrSubredditObj().setSubreddit("pics");
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
    /*
        Interface implementations
     */

    @Override
    public void openMediaViewer(Submission submission) {
        // prevent opening two media viewers (happens on quick double click)
        if(!isViewingSubmission) {
            FragmentManager fm = getSupportFragmentManager();
            Fragment mediaDisplayerFragment = (FragmentMediaDisplayer) fm.findFragmentByTag(Constants.TAG_FRAG_MEDIA_DISPLAY);
            FragmentTransaction ft = fm.beginTransaction();
            Bundle args = new Bundle();
            args.putSerializable(Constants.EXTRA_POST, submission);
            if (mediaDisplayerFragment != null) {
                ft.remove(mediaDisplayerFragment);
            }
            mediaDisplayerFragment = FragmentMediaDisplayer.newInstance();
            mediaDisplayerFragment.setArguments(args);
            ft.add(R.id.fragment_container, mediaDisplayerFragment, Constants.TAG_FRAG_MEDIA_DISPLAY);
            ft.addToBackStack(null);
            ft.commit();
            isViewingSubmission = true;
        }
    }


    @Override
    public void closeMediaDisplayer() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment mediaDisplayerFragment = (FragmentMediaDisplayer) fm.findFragmentByTag(Constants.TAG_FRAG_MEDIA_DISPLAY);
        if(mediaDisplayerFragment != null){
            fm.beginTransaction().remove(mediaDisplayerFragment).commit();
            // before adding this, pressing Back would re-open previously closed images
            fm.popBackStack();
        }
        isViewingSubmission = false;
    }

    @Override
    public void openSettings(){
        Intent settingsIntent = new Intent(this,ActivitySettings.class);
        //settingsIntent.putExtra()
        startActivity(settingsIntent);
    }

    @Override
    public void refreshFeed(String fragmentTag) {
        this.refreshFragment(fragmentTag);
    }
}
