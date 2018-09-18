package com.sometimestwo.moxie;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;

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
public class ActivityHome extends AppCompatActivity implements FragmentHome.HomeEventListener,
        FragmentSubmissionViewer.SubmissionDisplayerEventListener {

    private final String TAG = ActivityHome.class.getSimpleName();

    // False if user has navigated to a submission or different subreddit.
    // This allows us to know if we should handle onBackPressed() or not
    private boolean isHome = true;

    //screen size metrics for flexibility in displaying dialogs
    private DisplayMetrics mDisplayMetrics;
    private int mScreenWidth;
    private int mScreenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;

        init();
    }

    private void init() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Bundle args = new Bundle();
        //args.putInt(Constants.ARGS_NUM_DISPLAY_COLS, numDisplayCols);

        Fragment fragment = FragmentHome.newInstance();
        fragment.setArguments(args);
        ft.add(R.id.fragment_container_home, fragment, Constants.TAG_FRAG_HOME);
        ft.commit();
    }

    protected void refreshFragment(String fragmentTag, boolean invalidateData) {
        Fragment frg = getSupportFragmentManager().findFragmentByTag(fragmentTag);
        Bundle args = new Bundle();
        args.putBoolean(Constants.ARGS_INVALIDATE_DATASOURCE, invalidateData);
        frg.setArguments(args);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.navigation_menu, menu);
        getMenuInflater().inflate(R.menu.menu_home_header, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        // If we're at home we prompt user if they really want to exit, else just pop back stack
        if (isHome) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.TransparentDialog);
            builder.setTitle("Confirm exit");
            builder.setMessage("Really exit app?");
            builder.setIcon(R.drawable.ic_white_exclamation);

            builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    ActivityHome.this.exitApp();
                }
            });
            builder.setNegativeButton(android.R.string.no, null);
            AlertDialog alertDialog = builder.create();

            // button color setup
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(getResources().getColor(R.color.colorWhite));
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            .setTextColor(getResources().getColor(R.color.colorWhite));
                }
            });

            // resize the alert dialog
            alertDialog.show();
            alertDialog.getWindow().setLayout((6 * mScreenWidth) / 7, (4 * mScreenHeight) / 18);
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    public void exitApp() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.INTENT_SETTINGS) {
            if (resultCode == RESULT_OK) {
                refreshFragment(Constants.TAG_FRAG_HOME, false);
                Log.e(TAG, "Returned from settings activity");
            }
            // Setting was changed that requires data to be invalidated(refreshed)
            else if (resultCode == Constants.RESULT_OK_INVALIDATE_DATA) {
                refreshFragment(Constants.TAG_FRAG_HOME, true);
            }
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

    @Override
    public void refreshFeed( boolean invalidateData) {
        refreshFragment(Constants.TAG_FRAG_HOME, invalidateData);
    }

    @Override
    public void isHome(boolean isHome) {
        this.isHome = isHome;
    }

    @Override
    public void goBack() {
        super.onBackPressed();
    }

    @Override
    public void set404(boolean is404) {
        // This is added for the sake of being useful in ActivitySubredditViewer
    }
}
