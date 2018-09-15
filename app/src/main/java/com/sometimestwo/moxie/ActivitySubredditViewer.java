package com.sometimestwo.moxie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.sometimestwo.moxie.Utils.Constants;

public class ActivitySubredditViewer extends AppCompatActivity implements FragmentHome.HomeEventListener,
        FragmentSubmissionViewer.SubmissionDisplayerEventListener {

    public final String TAG = this.getClass().getCanonicalName();

    private String mCurrSubbredit;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit_viewer);
        unpackExtras();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Bundle args = new Bundle();
//        args.putInt(Constants.ARGS_NUM_DISPLAY_COLS,numDisplayCols);
        Fragment fragment = FragmentHome.newInstance();
        fragment.setArguments(args);
        ft.add(R.id.fragment_container_subreddit_viewer, fragment, Constants.TAG_FRAG_SUBREDDIT_VIEWER);
        ft.commit();
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
        super.onBackPressed();
    }

    public void unpackExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mCurrSubbredit = (String) getIntent().getExtras().get(Constants.EXTRA_GOTO_SUBREDDIT);
        }
    }

    protected void refreshFragment(String fragmentTag) {
        Fragment frg = getSupportFragmentManager().findFragmentByTag(fragmentTag);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        try {
            ft.detach(frg);
            ft.attach(frg);
            ft.commit();
        } catch (NullPointerException e) {
            throw new NullPointerException(this.toString()
                    + ". Could not refresh fragment! Probably provided incorrect fragment tag. " +
                    " Fragment tag provided: " + fragmentTag);
        }
    }

    /*
        Interface implementations
     */
    @Override
    public void openSettings() {
        return;
       /* Intent settingsIntent = new Intent(this,ActivitySettings.class);
        //settingsIntent.putExtra()
        startActivityForResult(settingsIntent,Constants.INTENT_SETTINGS);*/
    }

    @Override
    public void refreshFeed(String fragmentTag) {
        this.refreshFragment(fragmentTag);
    }

    @Override
    public void isHome(boolean isHome) { }
}
